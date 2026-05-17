package com.sie.iidp.example.utils.dto;

import lombok.Data;

/**
 * 排序DTO
 *
 * @author cxh
 * @date 2024年03月27日 14:40
 */
@Data
public class SortDTO {

    /**
     * 排序字段
     */
    private String sortField;

    /**
     * 排序方式
     */
    private String sortOrder;

}