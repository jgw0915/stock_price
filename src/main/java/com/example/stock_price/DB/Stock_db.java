package com.example.stock_price.DB;

import com.example.stock_price.Model.Stock;
import com.example.stock_price.Model.Stock_State;
import com.mongodb.client.*;
import com.mongodb.client.model.Filters;
import com.mongodb.client.model.Updates;
import org.bson.Document;
import java.util.Date;

import java.util.ArrayList;

public class Stock_db {
    private MongoClient mongoClient;
    private MongoDatabase database ; //= mongoClient.getDatabase("myMongoDb");

    public Stock_db(String database_name){
        mongoClient = MongoClients.create("mongodb://localhost:27017");
        database = mongoClient.getDatabase(database_name);
        if(database.getCollection("stock").equals(null)){
            database.createCollection("stock");
        }
    }

    public void insert_stock(Stock stock){
        MongoCollection<Document> collection = database.getCollection("stock");
        Document document = new Document();
        document.put("id", stock.getId());
        document.put("name", stock.getName());
        document.put("price", stock.getPrice());
        document.put("date", stock.getDate());
        collection.insertOne(document);
    }

    public ArrayList<Stock> get_all_stock(){
        MongoCollection<Document> collection = database.getCollection("stock");
        FindIterable<Document> documents = collection.find();
        ArrayList<Stock> stock_list = new ArrayList<Stock>();

        // Print each document
        for (Document document : documents) {
            String price;
            String id = document.get("id").toString();
            String name = document.get("name").toString();
            if (document.get("price") == null)  price = null;
            else  price = document.get("price").toString();
            Date date = (Date) document.get("date");
            stock_list.add(new Stock(name,id,price,date, Stock_State.FLAT));
        }
        return stock_list;
    }

    public void delete_by_id(String id){
        MongoCollection<Document> collection = database.getCollection("stock");
        collection.deleteOne(Filters.eq("id",id));
    }
    public void update_price(String id,String price){
        MongoCollection<Document> collection = database.getCollection("stock");

        // Update a document where key1 equals "value1"
        collection.updateOne(Filters.eq("id", "id"), Updates.set("price", price));
    }

    public static void main(String[] args) {
        Stock_db db = new Stock_db("Stock_db");
        db.mongoClient.listDatabaseNames().forEach(System.out::println);
    }
}
