package com.boostmytool.store.model;

public class Review {
    private final Integer id;
    private final String customer;
    private String content;

    public Review(Integer id, String customer, String content) {
        this.id = id;
        this.customer = customer;
        this.content = content;
    }

    public Integer getId() {
        return id;
    }

    public void setContent(String content) {
        this.content = content;
    }

    public String getCustomer() {
        return customer;
    }
}
