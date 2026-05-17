package com.sie.iidp.example.orglevel.enums;


import com.sie.iidp.example.orglevel.consts.OrgConst;

/**
 * 组织类别分类
 */
public enum OrgCateType {

    //企业
    ENTERPRISE(OrgConst.CATE_ENTERPRISE_ID, OrgConst.CATE_ENTERPRISE_DESC),
    //组织
    ORGANIZATION(OrgConst.CATE_ORGANIZATION_ID, OrgConst.CATE_ORGANIZATION_DESC),

    ;

    private final Integer id;
    private final String description;

    OrgCateType(Integer id, String description) {
        this.id = id;
        this.description = description;
    }

    public Integer getId() {
        return id;
    }

    public String getName() {
        return description;
    }
}
