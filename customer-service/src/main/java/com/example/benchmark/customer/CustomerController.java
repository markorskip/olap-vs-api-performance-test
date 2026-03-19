package com.example.benchmark.customer;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
public class CustomerController {
    private final CustomerRepository repository;

    public CustomerController(CustomerRepository repository) {
        this.repository = repository;
    }

    @GetMapping("/api/customers")
    @ResponseStatus(HttpStatus.OK)
    public List<CustomerRecord> getCustomers(@RequestParam List<Long> ids) {
        return repository.findAllByIds(ids);
    }

    @PostMapping("/api/customers/batch")
    @ResponseStatus(HttpStatus.OK)
    public List<CustomerRecord> batchLoad(@RequestBody List<Long> ids) {
        return repository.findAllByIds(ids);
    }

}
