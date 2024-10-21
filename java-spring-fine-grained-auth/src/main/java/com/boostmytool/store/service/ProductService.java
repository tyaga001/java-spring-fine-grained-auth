package com.boostmytool.store.service;

import com.boostmytool.store.exception.ResourceNotFoundException;
import com.boostmytool.store.model.Product;
import com.boostmytool.store.model.Review;
import io.permit.sdk.Permit;
import io.permit.sdk.api.PermitApiError;
import io.permit.sdk.api.PermitContextError;
import io.permit.sdk.enforcement.Resource;
import io.permit.sdk.enforcement.User;
import io.permit.sdk.openapi.models.RelationshipTupleCreate;
import io.permit.sdk.openapi.models.ResourceInstanceCreate;
import io.permit.sdk.openapi.models.RoleAssignmentCreate;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;

@Service  // Marks this class as a Spring service
public class ProductService {
    private final List<Product> products = new ArrayList<>();  // Stores a list of products
    private final AtomicInteger productIdCounter = new AtomicInteger();  // Counter to generate unique product IDs
    private final AtomicInteger reviewIdCounter = new AtomicInteger();   // Counter to generate unique review IDs

    // Builders for Permit resource instances for product and review
    private final Resource.Builder productResourceBuilder = new Resource.Builder("product");
    private final Resource.Builder reviewResourceBuilder = new Resource.Builder("review");

    private final UserService userService;  // UserService for handling user operations
    private final Permit permit;  // Permit SDK instance

    // Constructor to inject dependencies
    public ProductService(UserService userService, Permit permit) {
        this.userService = userService;
        this.permit = permit;
    }

    // Authorizes a user to perform an action on a general resource
    private void authorize(User user, String action, Resource resource) {
        userService.authorize(user, action, resource);
    }

    // Authorizes a user to perform an action on a specific product
    private void authorize(User user, String action, Product product) {
        var attributes = new HashMap<String, Object>();
        attributes.put("vendor", product.getVendor());  // Attaches vendor attribute to the product
        userService.authorize(user, action, productResourceBuilder.withKey(product.getId().toString()).withAttributes(attributes).build());
    }

    // Authorizes a user to perform an action on a specific review
    private void authorize(User user, String action, Review review) {
        var attributes = new HashMap<String, Object>();
        attributes.put("customer", review.getCustomer());  // Attaches customer attribute to the review
        userService.authorize(user, action, reviewResourceBuilder.withKey(review.getId().toString()).withAttributes(attributes).build());
    }

    // Retrieves a product by its ID, throws an exception if not found
    private Product getProductById(int id) {
        return products.stream().filter(product -> product.getId().equals(id))
                .findFirst().orElseThrow(() -> new ResourceNotFoundException("Product with id " + id + " not found"));
    }

    // Retrieves all products, authorizes the user for the "read" action
    public List<Product> getAllProducts(User user) {
        authorize(user, "read", productResourceBuilder.build());  // Checks if the user can read products
        return new ArrayList<>(products);
    }

    // Retrieves a single product by ID, authorizes the user for the "read" action
    public Product getProduct(User user, int id) {
        authorize(user, "read", productResourceBuilder.build());
        return getProductById(id);
    }

    // Adds a new product, creates resource instances and assigns roles via the Permit API
    public Product addProduct(User user, String content) {
        authorize(user, "create", productResourceBuilder.build());  // Checks if the user can create products
        Product product = new Product(productIdCounter.incrementAndGet(), user.getKey(), content);  // Creates a new product

        try {
            // Creates a Permit resource instance for the product and assigns the vendor role to the user
            permit.api.resourceInstances.create(new ResourceInstanceCreate(product.getId().toString(), "product").withTenant("default"));
            permit.api.roleAssignments.assign(new RoleAssignmentCreate("vendor", user.getKey()).withResourceInstance("product:" + product.getId()).withTenant("default"));
        } catch (IOException | PermitApiError | PermitContextError e) {
            // Handles failures in creating resource instances or role assignments
            throw new RuntimeException("Failed to create resource instance or role assignment: " + e.getMessage());
        }
        products.add(product);  // Adds the product to the list
        return product;
    }

    // Updates an existing product's content, authorizes the user for the "update" action
    public Product updateProduct(User user, int id, String content) {
        Product product = getProductById(id);  // Retrieves the product by ID
        authorize(user, "update", product);  // Checks if the user can update the product
        product.setContent(content);  // Updates the product content
        return product;
    }

    // Deletes a product, authorizes the user for the "delete" action
    public void deleteProduct(User user, int id) {
        boolean isDeleted = products.removeIf(product -> {
            if (product.getId().equals(id)) {
                authorize(user, "delete", product);  // Checks if the user can delete the product
                return true;
            } else {
                return false;
            }
        });
        if (!isDeleted) {
            throw new ResourceNotFoundException("Product with id " + id + " not found");
        }
        try {
            permit.api.resourceInstances.delete("product:" + id);  // Deletes the product resource instance from Permit
        } catch (IOException | PermitApiError | PermitContextError e) {
            throw new RuntimeException(e);
        }
    }

    // Adds a new review to a product, creates a resource instance and relationship via the Permit API
    public Review addReview(User user, int productId, String content) {
        authorize(user, "create", reviewResourceBuilder.build());  // Checks if the user can create reviews
        Product product = getProductById(productId);  // Retrieves the product by ID
        Review review = new Review(reviewIdCounter.incrementAndGet(), user.getKey(), content);  // Creates a new review
        try {
            // Creates a Permit resource instance for the review and sets the relationship with the product
            permit.api.resourceInstances.create(new ResourceInstanceCreate(review.getId().toString(), "review").withTenant("default"));
            permit.api.relationshipTuples.create(new RelationshipTupleCreate("product:" + productId, "parent", "review:" + reviewIdCounter));
        } catch (IOException | PermitApiError | PermitContextError e) {
            throw new RuntimeException(e);
        }
        product.addReview(review);  // Adds the review to the product
        return review;
    }

    // Updates an existing review's content, authorizes the user for the "update" action
    public Review updateReview(User user, int productId, int reviewId, String content) {
        Product product = getProductById(productId);  // Retrieves the product by ID
        Review review = product.getReviews().stream().filter(c -> c.getId().equals(reviewId))
                .findFirst().orElseThrow(() -> new ResourceNotFoundException("Review with id " + reviewId + " not found"));
        authorize(user, "update", review);  // Checks if the user can update the review
        review.setContent(content);  // Updates the review content
        return review;
    }

    // Deletes a review, authorizes the user for the "delete" action
    public void deleteReview(User user, int productId, int reviewId) {
        Product product = getProductById(productId);  // Retrieves the product by ID
        boolean isDeleted = product.getReviews().removeIf(review -> {
            if (review.getId().equals(reviewId)) {
                authorize(user, "delete", review);  // Checks if the user can delete the review
                return true;
            } else {
                return false;
            }
        });
        if (!isDeleted) {
            throw new ResourceNotFoundException("Review with id " + reviewId + " not found");
        }
        try {
            permit.api.resourceInstances.delete("review:" + reviewId);  // Deletes the review resource instance from Permit
        } catch (IOException | PermitApiError | PermitContextError e) {
            throw new RuntimeException(e);
        }
    }
}
