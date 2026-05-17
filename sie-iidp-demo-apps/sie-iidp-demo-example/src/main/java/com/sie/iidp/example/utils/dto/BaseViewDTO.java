package com.sie.iidp.example.utils.dto;

/**
 * @author cxh
 * @date 2024年03月27日 14:36
 */

import lombok.Data;

import java.io.Serializable;

/**
 * 基础返回DTO
 */
@Data
public class BaseViewDTO implements Serializable {
    /**
     * id
     */

    private String id;

    /**
     * 创建人
     */
    private String creator;

    /**
     * 修改人
     */
    private String modifier;


    /**
     * 创建人
     */
    private String creatorByName;

    /**
     * 修改人
     */
    private String modifierByName;


    /**
     * 创建时间
     */
    private Long createTime;

    /**
     * 修改时间
     */
    private Long lastUpdateTime;
    
}
