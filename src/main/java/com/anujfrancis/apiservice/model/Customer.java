package com.anujfrancis.apiservice.model;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class Customer {
    private String id;
    private String name;
    private String alias;
    private String dob;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}
