package com.anujfrancis.apiservice.controller;

import com.anujfrancis.apiservice.model.Customer;
import com.anujfrancis.apiservice.service.CustomerService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import reactor.core.publisher.Mono;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestController
@CrossOrigin
@RequestMapping("/api")
public class CustomerController {
    private final CustomerService customerService;

    public CustomerController(CustomerService customerService) {
        this.customerService = customerService;
    }

    @GetMapping("/health")
    public ResponseEntity<Map<String, Object>> healthCheck() {
        Map<String, Object> response = new HashMap<>();
        response.put("status", "healthy");
        
        Map<String, String> dependencies = new HashMap<>();
        dependencies.put("dataService", "healthy");
        response.put("dependencies", dependencies);
        
        return ResponseEntity.ok(response);
    }

    @PostMapping("/customers")
    public Mono<ResponseEntity<Object>> createCustomer(@RequestBody Customer customer) {
        // Validate input
        if (customer.getName() == null || customer.getAlias() == null || customer.getDob() == null) {
            Map<String, String> errorResponse = new HashMap<>();
            errorResponse.put("error", "Name, alias, and date of birth are required");
            return Mono.just(ResponseEntity.status(HttpStatus.BAD_REQUEST).body((Object)errorResponse));
        }

        return customerService.createCustomer(customer)
                .map(createdCustomer -> ResponseEntity.status(HttpStatus.CREATED).body((Object)createdCustomer))
                .onErrorResume(e -> {
                    Map<String, String> errorResponse = new HashMap<>();
                    errorResponse.put("error", e.getMessage());
                    return Mono.just(ResponseEntity.status(HttpStatus.BAD_REQUEST).body((Object)errorResponse));
                });
    }

    @GetMapping("/customers")
    public Mono<ResponseEntity<Object>> getAllCustomers() {
        return customerService.getAllCustomers()
                .map(customers -> ResponseEntity.ok().body((Object)customers))
                .onErrorResume(e -> {
                    Map<String, String> errorResponse = new HashMap<>();
                    errorResponse.put("error", "Failed to retrieve customers");
                    return Mono.just(ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body((Object)errorResponse));
                });
    }

    @GetMapping("/customers/search")
    public Mono<ResponseEntity<Object>> searchCustomer(
            @RequestParam(required = false) String id,
            @RequestParam(required = false) String name,
            @RequestParam(required = false) String alias) {

        if (id == null && name == null && alias == null) {
            Map<String, String> errorResponse = new HashMap<>();
            errorResponse.put("error", "At least one search parameter (id, name, or alias) is required");
            return Mono.just(ResponseEntity.status(HttpStatus.BAD_REQUEST).body((Object)errorResponse));
        }

        return customerService.searchCustomer(id, name, alias)
                .map(customer -> ResponseEntity.ok().body((Object)customer))
                .onErrorResume(e -> {
                    Map<String, String> errorResponse = new HashMap<>();
                    errorResponse.put("error", e.getMessage());
                    return Mono.just(ResponseEntity.status(HttpStatus.NOT_FOUND).body((Object)errorResponse));
                });
    }

    @PutMapping("/customers/{id}")
    public Mono<ResponseEntity<Object>> updateCustomer(@PathVariable String id, @RequestBody Customer customerDetails) {
        if (customerDetails.getName() == null && customerDetails.getAlias() == null && customerDetails.getDob() == null) {
            Map<String, String> errorResponse = new HashMap<>();
            errorResponse.put("error", "At least one field to update is required");
            return Mono.just(ResponseEntity.status(HttpStatus.BAD_REQUEST).body((Object)errorResponse));
        }

        return customerService.updateCustomer(id, customerDetails)
                .map(updatedCustomer -> ResponseEntity.ok().body((Object)updatedCustomer))
                .onErrorResume(e -> {
                    Map<String, String> errorResponse = new HashMap<>();
                    errorResponse.put("error", e.getMessage());
                    return Mono.just(ResponseEntity.status(HttpStatus.BAD_REQUEST).body((Object)errorResponse));
                });
    }

    @DeleteMapping("/customers/{id}")
    public Mono<ResponseEntity<Object>> deleteCustomer(@PathVariable String id) {
        return customerService.deleteCustomer(id)
                .map(response -> ResponseEntity.ok().body((Object)response))
                .onErrorResume(e -> {
                    Map<String, String> errorResponse = new HashMap<>();
                    errorResponse.put("error", e.getMessage());
                    return Mono.just(ResponseEntity.status(HttpStatus.NOT_FOUND).body((Object)errorResponse));
                });
    }
}
