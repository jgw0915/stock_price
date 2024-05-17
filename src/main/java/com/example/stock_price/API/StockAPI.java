package com.example.stock_price.API;

import java.util.*;
import java.util.stream.Collectors;

import org.json.JSONArray;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;

public class StockAPI {
    private ArrayList<String> stock_list_tse ;
    private ArrayList<String> stock_list_otc ;
    private String query_url;
    private final Object lock = new Object();

    public StockAPI(){
        stock_list_tse = new ArrayList<String>();
        stock_list_otc = new ArrayList<String>();
    }

    public void add_stock_to_api(ArrayList<String> s_l){
        synchronized (lock) {

            for (String s : s_l) {
                if (s.startsWith("6")) stock_list_otc.add(s);
                else stock_list_tse.add(s);
            }
            String stock_list1 = stock_list_tse.stream()
                    .map(stock -> "tse_" + stock + ".tw")
                    .collect(Collectors.joining("|"));

            String stock_list2 = stock_list_otc.stream()
                    .map(stock -> "otc_" + stock + ".tw")
                    .collect(Collectors.joining("|"));

            String stock_list = stock_list1 + "|" + stock_list2;
            query_url = "https://mis.twse.com.tw/stock/api/getStockInfo.jsp?ex_ch=" + stock_list;
        }
    }
    public void delete_stock_in_api(String id){
        synchronized (lock) {
            try {
                for (String s : stock_list_otc) {
                    if (s.equals(id)) stock_list_otc.remove(s);
                }
                for (String s : stock_list_tse) {
                    if (s.equals(id)) stock_list_tse.remove(s);
                }
                String stock_list1 = stock_list_tse.stream()
                        .map(stock -> "tse_" + stock + ".tw")
                        .collect(Collectors.joining("|"));

                String stock_list2 = stock_list_otc.stream()
                        .map(stock -> "otc_" + stock + ".tw")
                        .collect(Collectors.joining("|"));

                String stock_list = stock_list1 + "|" + stock_list2;
                query_url = "https://mis.twse.com.tw/stock/api/getStockInfo.jsp?ex_ch=" + stock_list;
            }catch (ConcurrentModificationException e){
                e.printStackTrace();
            }


        }
    }


    public List<Map<String,Object>> get_response(){
        try {
            URL url = new URL(query_url);
            HttpURLConnection connection = (HttpURLConnection) url.openConnection();
            connection.setRequestMethod("GET");

            int responseCode = connection.getResponseCode();
            if (responseCode != 200) {
                System.out.println("Query URL: " + query_url);
                System.out.println("HTTP Response Code: " + responseCode);
                System.out.println(connection.getHeaderField("location"));
                throw new Exception("取得股票資訊失敗.");
            } else {
                BufferedReader reader = new BufferedReader(new InputStreamReader(connection.getInputStream()));
                String line;
                StringBuilder response = new StringBuilder();
                while ((line = reader.readLine()) != null) {
                    response.append(line);
                }
                reader.close();


                JSONObject data = new JSONObject(response.toString());

                List<String> columns = List.of("c", "n", "z", "tv", "v", "o", "h", "l", "y", "tlong");
                List<String> newColumns = List.of("股票代號", "公司簡稱", "成交價", "成交量", "累積成交量", "開盤價", "最高價", "最低價", "昨收價", "資料更新時間");
                JSONArray msgArray = data.getJSONArray("msgArray");

                List<Map<String, Object>> dataList = new ArrayList<>();
                for (int i = 0; i < msgArray.length(); i++) {
                    JSONObject stockData = msgArray.getJSONObject(i);
                    Map<String, Object> stockMap = new HashMap<>();
                    for (int k = 0; k < columns.size(); k++) {
//                        System.out.println(columns.get(k)+":"+stockData.get(columns.get(k)));
                        stockMap.put(newColumns.get(k), stockData.get(columns.get(k)));
                    }
                    dataList.add(stockMap);
                }


                for (Map<String, Object> stockMap : dataList) {
                    stockMap.put("漲跌百分比", 0.0);
                }

                for (Map<String, Object> stockMap : dataList) {
                    System.out.println(stockMap.get("成交價"));
                    double price = Double.parseDouble(stockMap.get("成交價") == null && !stockMap.get("成交價").toString().equals("-") ? stockMap.get("成交價").toString() : "0");
                    double prevPrice = Double.parseDouble(stockMap.get("���收價") != null ? stockMap.get("���收價").toString():"1");
                    double result = (price - prevPrice) / prevPrice * 100;
                    stockMap.put("���跌百分比", result == -100 ? "-" : result);
                }

                for (Map<String, Object> stockMap : dataList) {
                    Date date =new Date();
                    stockMap.put("資料更新時間", date.toString());
                }
//
                for (Map<String, Object> stockMap : dataList) {
                    System.out.println(stockMap);
                }
                return dataList;
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }
    public static void main(String[] args) {
        System.setProperty("file.encoding", "UTF-8");
        ArrayList<String> al = new ArrayList<String>();
        al.add("0050");
        al.add("6789");
        StockAPI api = new StockAPI();
        api.add_stock_to_api(al);
        List<Map<String,Object>> l_m = api.get_response();
    }
}


