package com.hrms.portal.entity;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import java.time.LocalDate;

@Entity
@Table(name = "holidays")
public class Holiday {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @NotBlank(message = "Holiday name is required")
    @Column(nullable = false)
    private String name;

    @NotNull(message = "Date is required")
    @Column(nullable = false)
    private LocalDate date;

    @Column(nullable = false)
    private String type = "PUBLIC"; // PUBLIC, RESTRICTED, COMPANY

    private String description;

    private boolean optional = false;

    public Holiday() {}

    public Holiday(String name, LocalDate date, String type, String description) {
        this.name = name;
        this.date = date;
        this.type = type;
        this.description = description;
    }

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public String getName() { return name; }
    public void setName(String name) { this.name = name; }

    public LocalDate getDate() { return date; }
    public void setDate(LocalDate date) { this.date = date; }

    public String getType() { return type; }
    public void setType(String type) { this.type = type; }

    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }

    public boolean isOptional() { return optional; }
    public void setOptional(boolean optional) { this.optional = optional; }
}
