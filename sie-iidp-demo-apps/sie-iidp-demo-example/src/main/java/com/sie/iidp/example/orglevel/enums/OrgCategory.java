package com.sie.iidp.example.orglevel.enums;


import com.sie.iidp.example.orglevel.consts.OrgConst;

/**
 * 组织类别
 */
public enum OrgCategory {
    // 集团，公司，事业部，制造园区，工厂
    //集团
    GROUP(OrgConst.CATE_TYPE_GROUP_ID, OrgConst.CATE_TYPE_GROUP_DESC, true, true, true, true),
    //公司
    COMPANY(OrgConst.CATE_TYPE_COMPANY_ID, OrgConst.CATE_TYPE_COMPANY_DESC, true, true, true, true),
    //事业部
    BUSINESS_GROUP(OrgConst.CATE_TYPE_BUSINESS_GROUP_ID, OrgConst.CATE_TYPE_BUSINESS_GROUP_DESC, true, true, true, true),
    //工厂
    FACTORY(OrgConst.CATE_TYPE_FACTORY_ID, OrgConst.CATE_TYPE_FACTORY_DESC, true, true, true, true),
    //部门
    DEPT(OrgConst.CATE_TYPE_DEPT_ID, OrgConst.CATE_TYPE_DEPT_DESC, true, true, true, false),
    //车间
    WORKSHOP(OrgConst.CATE_TYPE_WORKSHOP_ID, OrgConst.CATE_TYPE_WORKSHOP_DESC, true, true, false, false),
    //线体
    LINE(OrgConst.CATE_TYPE_LINE_ID, OrgConst.CATE_TYPE_LINE_DESC, true, true, false, false),
    //班组
    WORK_TERM(OrgConst.CATE_TYPE_WORK_TERM_ID, OrgConst.CATE_TYPE_WORK_TERM_DESC, true, true, false, false),
    //科室
    DEPARTMENT(OrgConst.CATE_TYPE_DEPARTMENT_ID, OrgConst.CATE_TYPE_DEPARTMENT_DESC, true, true, true, false),
    //制造园区
    CATE_TYPE_MANUFACTURING_PARK(OrgConst.CATE_TYPE_MANUFACTURING_PARK_ID, OrgConst.CATE_TYPE_MANUFACTURING_PARK_DESC, true, true, false, true),
    //制造基地
    CATE_TYPE_MANUFACTURING_BASE(OrgConst.CATE_TYPE_MANUFACTURING_BASE_ID, OrgConst.CATE_TYPE_MANUFACTURING_BASE_DESC, true, true, false, true);

    OrgCategory(Integer id, String desc, Boolean isRoot, Boolean isBase, Boolean isOrgCateType, Boolean isEntCateType) {
        this.id = id;
        this.desc = desc;
        this.isRoot = isRoot;
        this.isBase = isBase;
        this.isOrgCateType = isOrgCateType;
        this.isEntCateType = isEntCateType;
    }

    private final Integer id;
    private final String desc;
    private final Boolean isRoot;
    private final Boolean isBase;
    private final Boolean isOrgCateType;
    private final Boolean isEntCateType;

    public Integer getId() {
        return id;
    }

    public String getDesc() {
        return desc;
    }

    public Boolean getRoot() {
        return isRoot;
    }

    public Boolean getBase() {
        return isBase;
    }

    public Boolean isOrgCateType() {
        return isOrgCateType;
    }

    public Boolean isEntCateType() {
        return isEntCateType;
    }
}
