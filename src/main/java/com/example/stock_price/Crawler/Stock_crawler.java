package com.example.stock_price.Crawler;

import org.jsoup.Connection;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.apache.commons.csv.CSVFormat;
import org.apache.commons.csv.CSVParser;
import org.apache.commons.csv.CSVRecord;

import java.io.*;
import java.net.URL;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;

public class Stock_crawler {
    private ArrayList<String> stock_id;
    private String stock_site_url=  "https://goodinfo.tw/tw/StockDetail.asp?STOCK_ID=";

    public Stock_crawler(){
        stock_id =new ArrayList<String>();
        read_stock_id();
    }

    public ArrayList<String> getStock_id(){
        return stock_id;
    }
//    public static void crawl_stock_data(String url){
//        ArrayList<Stock> stock_data = new ArrayList<Stock>();
//        ArrayList<String> stock_code_link = new ArrayList<String>();
//        try{
//            Connection con = Jsoup.connect(url);
//            Document doc = con.get();
//            int i=1;
//            for(Element link : doc.select("a[href*=%E5%85%A8%E9%83%A8&STOCK_CODE]")){
//                String absUrl = link.absUrl("href");
//
//                System.out.println(absUrl);
////                if (!stock_code_link.contains(absUrl)){
////                    stock_code_link.add(absUrl);
////                }
//            }
////            stock_code_link.forEach(item->{
////                System.out.println(item);
////            });
//        }catch (IOException e){
//            e.printStackTrace();
//        }
//
//    }

    public static ArrayList fileList(String filePath){
        ArrayList list= new ArrayList();
        File file = new File(filePath);
        if(file.isDirectory()){
            if(!filePath.matches(".*\\\\$")) filePath += "\\";
            for(String fileName:file.list()){
                list.addAll(fileList(filePath + fileName));
            }
            return list;
        }else{
            list.add(filePath.toString());
            return list;
        }
    }


    public void read_stock_id() {

        URL url = this.getClass().getClassLoader().getResource("stock_id");
        String stock_id_file_path = url.getPath();
        ArrayList file_list = fileList(stock_id_file_path);
        file_list.forEach(e->{
            System.out.println(e.toString());
        });
        for (Object file : file_list) {
//            try  {
//                Charset charset = Charset.forName("MS950");
//
//                // 使用CSVFormat.DEFAULT.withHeader()來處理包含標題的CSV檔案
//                try (CSVParser parser = CSVParser.parse(new File(file.toString()), charset, CSVFormat.DEFAULT.withHeader())) {
//                    for (CSVRecord record : parser) {
//                        for (String value : record) {
//                            System.out.print(value + " ");
//                        }
//                        System.out.println();
//                    }
//                }
//            } catch (IOException e) {
//                e.printStackTrace();
//            }
            String file_path = file.toString();
            try {
                InputStreamReader isr = new InputStreamReader(new FileInputStream(file_path),"utf-8");//檔案讀取路徑
                System.out.println(isr.getEncoding());
                BufferedReader reader = new BufferedReader(isr);
                String line = null;
                FileReader fileReader = new FileReader(file_path);
                while ((line = reader.readLine()) != null) {
                    String item[] = line.split(",");
                    /** 讀取 **/
                    String id = item[0].toString();
                    String name = item[1].toString();
                    id=id.replace("\uFEFF","");
                    id=id.replace("=","");
                    id=id.replace("\"","");
                    stock_id.add(id+" "+name);

                    //可自行變化成存入陣列或arrayList方便之後存取
                }
                System.out.println("*******************************");
            }catch (IOException e){
                e.printStackTrace();
            }

        }
    }

    public String crawl_data_in_link(String id) {

            try {
                Connection con = Jsoup.connect(stock_site_url+id);
                Document doc = con.get();
                PrintStream out = new PrintStream("test.html", "UTF-8");
                out.print(doc.html());
                out.close();
                for(Element price: doc.select("td[style=font-weight:bold;color:red]")){
                    if (price != null) return price.text();
                }
                for(Element price: doc.select("td[style=font-weight:bold;color:green]")){
                    if (price != null) return price.text();
                }
                for(Element price: doc.select("td[style=font-weight:bold;color:black]")){
                    if (price != null) return price.text();
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
            return null;
    }

    public static void main(String[] args) {
        System.setProperty("console.encoding", "UTF-8");
//        crawl_stock_data("https://goodinfo.tw/tw/StockList.asp");
        Stock_crawler crawler = new Stock_crawler();
        System.out.println( crawler.crawl_data_in_link("00648R"));

    }
}
