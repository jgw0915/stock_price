package com.example.stock_price.Model;

import jakarta.annotation.Nullable;

import java.util.Date;
import java.util.Optional;

public class Stock {
    private String name;
    private String Id;
    private Date date;
    private String price;
    private Stock_State state;

    public Stock(String name, String id, String price, Date date,Stock_State state){
        this.Id = id;
        this.name = name;
        this.price = price;
        this.date = date;
        this.state = state;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getId() {
        return Id;
    }

    public void setId(String id) {
        Id = id;
    }

    public Date getDate() {
        return date;
    }

    public void setDate(Date date) {
        this.date = date;
    }

    public String getPrice() {
        return price;
    }

    public void setPrice(String price) {
        this.price = price;
    }

    public Stock_State getState() {
        return state;
    }

    public void setState(Stock_State state) {
        this.state = state;
    }
}
