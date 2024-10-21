package com.boostmytool.store.controllers;

import com.boostmytool.store.model.Product;
import com.boostmytool.store.model.Review;
import com.boostmytool.store.service.ProductService;
import io.permit.sdk.enforcement.User;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController  // Indicates that this class is a Spring REST controller
@RequestMapping("/api/products")  // Base URL for all endpoints in this controller
public class ProductController {

    private final ProductService productService;  // ProductService instance to handle product-related operations

    @Autowired  // Autowires ProductService bean automatically
    public ProductController(ProductService productService) {
        this.productService = productService;
    }

    // GET request to retrieve all products
    @GetMapping
    public List<Product> getAllProducts(HttpServletRequest request) {
        User currentUser = (User) request.getAttribute("user");  // Gets the authenticated user from the request
        return productService.getAllProducts(currentUser);  // Calls ProductService to get all products for the user
    }

    // GET request to retrieve a product by its ID
    @GetMapping("/{id}")
    public Product getProductById(HttpServletRequest request, @PathVariable("id") int id) {
        User currentUser = (User) request.getAttribute("user");  // Gets the authenticated user from the request
        return productService.getProduct(currentUser, id);  // Calls ProductService to get the product by ID for the user
    }

    // POST request to add a new product
    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)  // Sets the response status to 201 (Created)
    public Product addProduct(HttpServletRequest request, @RequestBody String content) {
        User currentUser = (User) request.getAttribute("user");  // Gets the authenticated user from the request
        return productService.addProduct(currentUser, content);  // Calls ProductService to add a new product
    }

    // PUT request to update an existing product by its ID
    @PutMapping("/{id}")
    public Product updateBlog(HttpServletRequest request, @PathVariable("id") int id, @RequestBody String content) {
        User currentUser = (User) request.getAttribute("user");  // Gets the authenticated user from the request
        return productService.updateProduct(currentUser, id, content);  // Calls ProductService to update the product by ID
    }

    // DELETE request to delete a product by its ID
    @DeleteMapping("/{id}")
    public String deleteProduct(HttpServletRequest request, @PathVariable("id") int id) {
        User currentUser = (User) request.getAttribute("user");  // Gets the authenticated user from the request
        productService.deleteProduct(currentUser, id);  // Calls ProductService to delete the product by ID
        return "Deleted product with id " + id;  // Returns a success message after deletion
    }

    // POST request to add a new review to a product by product ID
    @PostMapping("/{id}/review")
    public Review addReview(HttpServletRequest request, @PathVariable("id") int id, @RequestBody String content) {
        User currentUser = (User) request.getAttribute("user");  // Gets the authenticated user from the request
        return productService.addReview(currentUser, id, content);  // Calls ProductService to add a review to the product
    }

    // PUT request to update an existing review by product and review ID
    @PutMapping("/{id}/review/{reviewId}")
    public Review updateReview(HttpServletRequest request, @PathVariable("id") int id, @PathVariable("reviewId") int reviewId, @RequestBody String content) {
        User currentUser = (User) request.getAttribute("user");  // Gets the authenticated user from the request
        return productService.updateReview(currentUser, id, reviewId, content);  // Calls ProductService to update the review
    }

    // DELETE request to delete a review by product and review ID
    @DeleteMapping("/{id}/review/{reviewId}")
    public String deleteReview(HttpServletRequest request, @PathVariable("id") int id, @PathVariable("reviewId") int reviewId) {
        User currentUser = (User) request.getAttribute("user");  // Gets the authenticated user from the request
        productService.deleteReview(currentUser, id, reviewId);  // Calls ProductService to delete the review
        return "Deleted review with id " + reviewId + " from product " + id;  // Returns a success message after deletion
    }
}
