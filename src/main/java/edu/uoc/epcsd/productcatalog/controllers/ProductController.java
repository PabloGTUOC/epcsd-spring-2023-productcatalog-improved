package edu.uoc.epcsd.productcatalog.controllers;

import edu.uoc.epcsd.productcatalog.controllers.dtos.CreateProductRequest;
import edu.uoc.epcsd.productcatalog.controllers.dtos.GetProductResponse;
import edu.uoc.epcsd.productcatalog.entities.Product;
import edu.uoc.epcsd.productcatalog.services.ProductService;
import edu.uoc.epcsd.productcatalog.repositories.ProductRepository;
import lombok.extern.log4j.Log4j2;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.support.ServletUriComponentsBuilder;

import javax.validation.constraints.NotNull;
import java.net.URI;
import java.util.List;
import java.util.stream.Collectors;

@Log4j2
@RestController
@RequestMapping("/products")
public class ProductController {

    @Autowired
    private ProductService productService;

    @GetMapping("/")
    @ResponseStatus(HttpStatus.OK)
    public List<Product> getAllProducts() {
        log.trace("getAllProducts");

        return productService.findAll();
    }

    @GetMapping("/{productId}")
    @ResponseStatus(HttpStatus.OK)
    public ResponseEntity<GetProductResponse> getProductById(@PathVariable @NotNull Long productId) {
        log.trace("getProductById");

        return productService.findById(productId).map(product -> ResponseEntity.ok().body(GetProductResponse.fromDomain(product)))
                .orElse(ResponseEntity.notFound().build());
    }

    @PostMapping
    public ResponseEntity<Long> createProduct(@RequestBody CreateProductRequest createProductRequest) {
        log.trace("createProduct");

        log.trace("Creating product " + createProductRequest);
        Long productId = productService.createProduct(
                createProductRequest.getCategoryId(),
                createProductRequest.getName(),
                createProductRequest.getDescription(),
                createProductRequest.getDailyPrice(),
                createProductRequest.getBrand(),
                createProductRequest.getModel()).getId();
        URI uri = ServletUriComponentsBuilder.fromCurrentRequest()
                .path("/{id}")
                .buildAndExpand(productId)
                .toUri();

        return ResponseEntity.created(uri).body(productId);
    }

    // TODO: add the code for the missing system operations here:
    // 1. remove product (use DELETE HTTP verb). Must remove the associated items
    @DeleteMapping("/{productId}")
    public ResponseEntity<Void> removeProduct(@PathVariable @NotNull Long productId) {
        log.trace("removeProduct");
        Product removedproduct = productService.removeProduct(productId);
        if (removedproduct != null) {
            return ResponseEntity.noContent().build(); // Product removed
        } else {
            return ResponseEntity.notFound().build(); // Product not found
        }
    }
    // 2. query products by name
    @Autowired
    private ProductRepository ProductRepository;
    @GetMapping("/byName/{productName}")
    @ResponseStatus(HttpStatus.OK)
    public ResponseEntity<List<GetProductResponse>> getProductsByName(@PathVariable @NotNull String productName) {
        log.trace("getProductsByName");

        List<Product> products = ProductRepository.findByNameContainingIgnoreCase(productName);

        if (!products.isEmpty()) {
            List<GetProductResponse> productResponses = products.stream()
                    .map(GetProductResponse::fromDomain)
                    .collect(Collectors.toList());
            return ResponseEntity.ok(productResponses);
        } else {
            return ResponseEntity.notFound().build();
        }
    }
    // 3. query products by category/subcategory
    @GetMapping("/byCategory/{categoryId}")
    @ResponseStatus(HttpStatus.OK)
    public ResponseEntity<List<GetProductResponse>> getProductsByCategory(@PathVariable Long categoryId) {
        log.trace("getProductsByCategory");
        List<Product> products = productService.getProductsByCategoryID(categoryId);
        if (!products.isEmpty()) {
            List<GetProductResponse> productResponses = products.stream()
                    .map(GetProductResponse::fromDomain)
                    .collect(Collectors.toList());
            return ResponseEntity.ok(productResponses);
        } else {
            return ResponseEntity.notFound().build();
        }
    }
}