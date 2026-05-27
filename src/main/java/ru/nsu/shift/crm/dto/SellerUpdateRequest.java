package ru.nsu.shift.crm.dto;

import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class SellerUpdateRequest {

    @Size(min = 1, max = 255, message = "name must be between 1 and 255 characters")
    private String name;

    @Size(min = 1, max = 500, message = "contact info must be between 1 and 500 characters")
    private String contactInfo;
}
