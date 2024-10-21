package com.boostmytool.store.model;

import java.util.ArrayList;
import java.util.List;

public class Product {
    private final Integer id;
    private final String vendor;
    private String content;
    private final List<Review> reviews = new ArrayList<>();

    public Product(Integer id, String author, String content) {
        this.id = id;
        this.vendor = author;
        this.content = content;
    }

    public Integer getId() {
        return id;
    }

    public String getVendor() {
        return vendor;
    }

    public void setContent(String content) {
        this.content = content;
    }

    public List<Review> getReviews() {
        return reviews;
    }

    public void addReview(Review review) {
        reviews.add(review);
    }
}
