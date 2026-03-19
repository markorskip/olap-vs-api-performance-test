package com.example.benchmark.product;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
public class ProductController {
    private final ProductRepository repository;

    public ProductController(ProductRepository repository) {
        this.repository = repository;
    }

    @GetMapping("/api/products")
    @ResponseStatus(HttpStatus.OK)
    public List<ProductRecord> getProducts(@RequestParam List<Long> ids) {
        return repository.findAllByIds(ids);
    }

    @PostMapping("/api/products/batch")
    @ResponseStatus(HttpStatus.OK)
    public List<ProductRecord> batchLoad(@RequestBody List<Long> ids) {
        return repository.findAllByIds(ids);
    }

}
