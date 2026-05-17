package com.sie.iidp.example.utils.dto;

import lombok.Data;

import java.util.List;

/**
 * 分页查询基础参数
 *
 * @author cxh
 * @date 2024年03月27日 14:39
 */
@Data
public class PageDTO {

    /**
     * 当前页
     */

    private Integer current = 1;

    /**
     * 每页条数
     */
    private Integer size = 10;

    /**
     * 排序字段
     */
    private String sortField = "create_date";

    /**
     * 排序方式
     */
    private String sortOrder = "desc";

    /**
     * 预留多个排序条件时用，待开发
     */
    List<SortDTO> extSortList;

    /**
     * 名称关键词
     */
    private String searchKey;

    /**
     * 排序条件
     */
    private String orderStr;

    public String getBaseOrder() {
        return sortField + " " + sortOrder;
    }
}
