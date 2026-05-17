package com.sie.iidp.example.orglevel.errors;

import com.sie.snest.engine.exception.ValidationException;

public class Exceptions {
    public static ValidationException LEVEL_DELETE_QUOTED_EXCEPTION = new ValidationException("无法删除，层级仍存在关联数据");
    public static ValidationException LEVEL_DELETE_CHILD_EXIST_EXCEPTION = new ValidationException("无法删除，层级存在子级关系");
    public static ValidationException LEVEL_CREATE_HL_SAME_EXCEPTION = new ValidationException("无法新增，类型与上级类型相同");
    public static ValidationException LEVEL_CREATE_CYCLE_EXCEPTION = new ValidationException("无法新增，层级关系成环");
    public static ValidationException FRAME_CREATE_LEVEL_NOT_EXISTS_EXCEPTION = new ValidationException("无法新增，不存在对应的层级关系");
    public static ValidationException INV_DELETE_QUOTED_EXCEPTION = new ValidationException("无法删除，库存组织存在关联企业模型");
    public static ValidationException INV_DELETE_ENABLE_EXCEPTION = new ValidationException("无法删除，启用状态的库存组织无法删除");
    public static ValidationException ENT_DELETE_QUOTED_EXCEPTION = new ValidationException("无法删除，企业模型存在关联库存组织");
    public static ValidationException ENT_DELETE_QUOTED_CHILDREN_EXCEPTION = new ValidationException("无法删除，企业模型存在关联子企业模型");

    public static ValidationException ORG_DELETE_QUOTED_CHILDREN_EXCEPTION = new ValidationException("无法删除，组织存在关联子组织");
    public static ValidationException ORG_DELETE_QUOTED_ENT_EXCEPTION = new ValidationException("无法删除，组织存在关联企业模型");

    public static ValidationException EMP_CODE_EXISTS_EXCEPTION = new ValidationException("工号重复，请核对数据");
    public static ValidationException EMP_QUOTED_EXISTS_EXCEPTION = new ValidationException("该员工已经关联用户，请重新选择关联");
    public static ValidationException EMP_DELETE_QUOTED_ENT_EXCEPTION = new ValidationException("无法删除，员工已关联用户");

    public static ValidationException IMPORT_ERROR = new ValidationException("文件列头有误");

    public static ValidationException EMP_NO_CODE_EXCEPTION = new ValidationException("工号不能为空");
}
