package com.example.demo.dto.category;

import lombok.*;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class BreadcrumbDTO {

    private Integer categoryId;
    private String catCode;
    private String categoryName;
    private Integer level;
}
