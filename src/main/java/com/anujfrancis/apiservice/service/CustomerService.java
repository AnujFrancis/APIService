package com.anujfrancis.apiservice.service;

import com.anujfrancis.apiservice.model.Customer;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Mono;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
public class CustomerService {
    private final WebClient webClient;
    private static final ParameterizedTypeReference<Map<String, Object>> MAP_TYPE_REF = 
            new ParameterizedTypeReference<Map<String, Object>>() {};

    public CustomerService(@Value("${data.service.url}") String dataServiceUrl) {
        this.webClient = WebClient.builder()
                .baseUrl(dataServiceUrl)
                .build();
    }

    public Mono<Customer> createCustomer(Customer customer) {
        // Validate date format
        if (!isValidDate(customer.getDob())) {
            return Mono.error(new IllegalArgumentException("Date of birth must be in a valid date format"));
        }

        return webClient.post()
                .uri("/customers")
                .bodyValue(customer)
                .retrieve()
                .onStatus(
                        status -> status.equals(HttpStatus.BAD_REQUEST) || status.equals(HttpStatus.CONFLICT),
                        response -> response.bodyToMono(MAP_TYPE_REF)
                                .flatMap(errorBody -> Mono.error(new RuntimeException((String) errorBody.get("error"))))
                )
                .bodyToMono(Customer.class);
    }

    public Mono<List<Customer>> getAllCustomers() {
        return webClient.get()
                .uri("/customers")
                .retrieve()
                .bodyToMono(Customer[].class)
                .map(Arrays::asList);
    }

    public Mono<Customer> searchCustomer(String id, String name, String alias) {
        return webClient.get()
                .uri(uriBuilder -> uriBuilder
                        .path("/customers/search")
                        .queryParamIfPresent("id", java.util.Optional.ofNullable(id))
                        .queryParamIfPresent("name", java.util.Optional.ofNullable(name))
                        .queryParamIfPresent("alias", java.util.Optional.ofNullable(alias))
                        .build())
                .retrieve()
                .onStatus(
                        status -> status.equals(HttpStatus.NOT_FOUND) || status.equals(HttpStatus.BAD_REQUEST),
                        response -> response.bodyToMono(MAP_TYPE_REF)
                                .flatMap(errorBody -> Mono.error(new RuntimeException((String) errorBody.get("error"))))
                )
                .bodyToMono(Customer.class);
    }

    public Mono<Customer> updateCustomer(String id, Customer customerDetails) {
        // Validate date format if provided
        if (customerDetails.getDob() != null && !isValidDate(customerDetails.getDob())) {
            return Mono.error(new IllegalArgumentException("Date of birth must be in a valid date format"));
        }

        return webClient.put()
                .uri("/customers/{id}", id)
                .bodyValue(customerDetails)
                .retrieve()
                .onStatus(
                        status -> status.equals(HttpStatus.NOT_FOUND) || status.equals(HttpStatus.BAD_REQUEST) || status.equals(HttpStatus.CONFLICT),
                        response -> response.bodyToMono(MAP_TYPE_REF)
                                .flatMap(errorBody -> Mono.error(new RuntimeException((String) errorBody.get("error"))))
                )
                .bodyToMono(Customer.class);
    }

    public Mono<Map<String, Object>> deleteCustomer(String id) {
        return webClient.delete()
                .uri("/customers/{id}", id)
                .retrieve()
                .onStatus(
                        status -> status.equals(HttpStatus.NOT_FOUND),
                        response -> response.bodyToMono(MAP_TYPE_REF)
                                .flatMap(errorBody -> Mono.error(new RuntimeException((String) errorBody.get("error"))))
                )
                .bodyToMono(MAP_TYPE_REF);
    }

    private boolean isValidDate(String dateStr) {
        try {
            LocalDate.parse(dateStr);
            return true;
        } catch (DateTimeParseException e) {
            try {
                // Try with formatter for more flexible parsing
                DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd");
                LocalDate.parse(dateStr, formatter);
                return true;
            } catch (DateTimeParseException ex) {
                return false;
            }
        }
    }
}
