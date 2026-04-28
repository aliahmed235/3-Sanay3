package com.sany3.graduation_project.dto.request;

import com.sany3.graduation_project.entites.ServiceType;
import jakarta.validation.constraints.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.math.BigDecimal;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class CreateServiceRequestRequest {

    @NotNull(message = "Service type is required")
    private ServiceType serviceType;

    @NotBlank(message = "Title is required")
    @Size(min = 5, max = 255, message = "Title must be 5-255 characters")
    private String title;

    @NotBlank(message = "Description is required")
    @Size(min = 10, max = 1000, message = "Description must be 10-1000 characters")
    private String description;

    @NotBlank(message = "Address is required")
    private String address;

    @NotNull(message = "Latitude is required")
    @DecimalMin(value = "-90", message = "Invalid latitude")
    @DecimalMax(value = "90", message = "Invalid latitude")
    private BigDecimal latitude;

    @NotNull(message = "Longitude is required")
    @DecimalMin(value = "-180", message = "Invalid longitude")
    @DecimalMax(value = "180", message = "Invalid longitude")
    private BigDecimal longitude;
}