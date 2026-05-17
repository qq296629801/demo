package com.sie.iidp.example.orglevel.utils;

import com.sie.iidp.example.orglevel.enums.OrgCateType;
import com.sie.iidp.example.orglevel.enums.OrgCategory;
import com.sie.snest.engine.utils.Options;
import org.apache.commons.lang3.StringUtils;
import org.apache.logging.log4j.util.Strings;

import java.util.*;
import java.util.stream.Collectors;

public class OrgCategoryUtils {
    private static Map<String, String> cateIdAndDescMap = new HashMap<>();

    static {
        for (OrgCategory category : OrgCategory.values()) {
            cateIdAndDescMap.put(String.valueOf(category.getId()), category.getDesc());
        }
    }

    public static String convertCateName(Map<String, Object> data, String fieldName) {
        Object val = data.get(fieldName);
        if (val == null || !cateIdAndDescMap.containsKey(val)) {
            return Strings.EMPTY;
        }
        return cateIdAndDescMap.get(val);
    }


    public static List<Options> obtainOrgCategoryList(Object value, OrgCateType orgCateType) {
        List<Options> categoryList = new ArrayList<>();
        List<Options> options = new ArrayList<>();
        String valueStr = Objects.toString(value, null);
        for (OrgCategory cate : OrgCategory.values()) {
            switch (orgCateType) {
                case ENTERPRISE:
                    if (!cate.isEntCateType()) {
                        continue;
                    }
                    break;
                case ORGANIZATION:
                    if (!cate.isOrgCateType()) {
                        continue;
                    }
            }
            Options option = Options.of(cate.getDesc(), String.valueOf(cate.getId()));
            categoryList.add(option);
        }
        if (StringUtils.isNotBlank(valueStr)) {
            Optional<Options> matchValOptional = categoryList.stream().filter(p -> p.getValue().equals(valueStr)).findFirst();
            if (matchValOptional.isPresent()) {
                options.add(matchValOptional.get());
            }
            return options;
        }
        return categoryList;
    }

    public static List<Options> obtainOrgCategoryList(String hlCateId, Object value, OrgCateType orgCateType) {
        List<Options> allOptions = obtainOrgCategoryList(value, orgCateType);
        if (StringUtils.isEmpty(hlCateId)) {
            return allOptions;
        }
        Set<String> childSet = BaseCateLevelUtils.obtainChildrenOrgCateIdSet(hlCateId, orgCateType);
        allOptions = allOptions.stream().filter(e -> childSet.contains(e.getValue())).collect(Collectors.toList());
        return allOptions;
    }
}
