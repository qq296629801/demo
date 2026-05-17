package com.sie.iidp.example.utils.dto;

import lombok.Data;

/**
 * @author cxh
 * @date 2024年03月27日 14:15
 */
@Data
public class EncoderRuleDummyDTO {
    /**
     * 编码规则编码
     */
    private String code;

    /**
     * 编码规则名称
     */

    private String name;

    /**
     * 起始编码号
     */
    private String startCodeNumber;

    /**
     * 结束编码号
     */
    private String endCodeNumber;
}
