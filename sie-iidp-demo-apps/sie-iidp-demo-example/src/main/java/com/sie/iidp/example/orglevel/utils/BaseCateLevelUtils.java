package com.sie.iidp.example.orglevel.utils;

import com.sie.iidp.common.util.database.BaseModelExtUtils;
import com.sie.iidp.example.orglevel.consts.OrgConst;
import com.sie.iidp.example.orglevel.enums.OrgCateType;
import com.sie.iidp.example.orglevel.model.ExampleOrgLevel;
import com.sie.snest.engine.data.RecordSet;
import com.sie.snest.engine.rule.Filter;
import org.apache.commons.lang3.StringUtils;

import java.util.*;

public class BaseCateLevelUtils {
    public static Set<String> obtainChildrenOrgCateIdSet(String hlCateId, OrgCateType orgCateType) {
        ExampleOrgLevel levelConf = new ExampleOrgLevel();
        String cateTypeId = String.valueOf(orgCateType.getId());
        Filter filter = new Filter();
        filter.and(Filter.equal(OrgConst.HL_ORG_CATE_ID_FIELD_NAME, hlCateId));
        filter.and(Filter.equal(OrgConst.CATE_TYPE_FIELD_NAME, cateTypeId));
        RecordSet modelRs = levelConf.getMeta().get(levelConf.getModelName());
        List<Map<String, Object>> resultList = (List<Map<String, Object>>) modelRs.callSuper(ExampleOrgLevel.class, "search", filter, Collections.singletonList("*"), 0, 0, null);
        Set<String> resultSet = new HashSet<>();
        for (Map<String, Object> data : resultList) {
            String orgCateId = String.valueOf(data.get(OrgConst.ORG_CATE_ID_FIELD_NAME));
            resultSet.add(orgCateId);
        }
        return resultSet;
    }

    public static Optional<String> obtainHlCateType(String hlId, OrgCateType orgCateType) {
        if (StringUtils.isEmpty(hlId)) {
            return Optional.empty();
        }
        ExampleOrgLevel levelConf = new ExampleOrgLevel();
        String cateTypeId = String.valueOf(orgCateType.getId());
        Filter filter = new Filter();
        filter.and(Filter.equal(OrgConst.HL_ORG_ID_FIELD_NAME, levelConf));
        filter.and(Filter.equal(OrgConst.CATE_TYPE_FIELD_NAME, cateTypeId));
        Optional<ExampleOrgLevel> confOp = BaseModelExtUtils.findById(ExampleOrgLevel.class, levelConf.getModelName(), hlId, ExampleOrgLevel::new);
        if (!confOp.isPresent()) {
            return Optional.empty();
        }
        String orgCateId = String.valueOf(confOp.get().get(OrgConst.ORG_CATE_ID_FIELD_NAME));
        return Optional.of(orgCateId);
    }
}
