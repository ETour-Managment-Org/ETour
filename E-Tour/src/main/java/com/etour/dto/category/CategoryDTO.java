package com.etour.dto.category;

import lombok.*;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class CategoryDTO {
    private Integer categoryId;
    private String catCode;
    private String categoryName;
    private String catImagePath;
    private String description;
    private String suitableFor;
    private String status;

    private Integer parentId;
    private String parentName;

    private Boolean flag;

    private String nextAction;

    private Long childCount;
    private Long tourCount;
}
