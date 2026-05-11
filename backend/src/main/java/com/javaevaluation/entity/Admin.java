package com.javaevaluation.entity;

import com.fasterxml.jackson.annotation.JsonIgnore;
import lombok.Data;
import java.time.LocalDateTime;

@Data
public class Admin {
    private Integer id;
    private String username;
    @JsonIgnore
    private String password;
    private String name;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}