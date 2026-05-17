package com.sie.iidp.example.orglevel.utils;

import com.sie.iidp.example.orglevel.consts.OrgConst;
import com.sie.iidp.example.orglevel.enums.OrgCateType;
import com.sie.iidp.example.orglevel.errors.Exceptions;
import com.sie.snest.engine.data.RecordSet;
import com.sie.snest.engine.rule.Filter;
import com.sie.snest.engine.utils.Options;
import com.sie.snest.sdk.BaseModel;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class OrgLevelUtils {


    /**
     * 新增校验：判断新增层级是否成环
     *
     * @param valuesList
     */
    public static <T extends BaseModel> void createCheck(BaseModel<T> baseModel, OrgCateType orgCateType, List<Map<String, Object>> valuesList) {
        DirectedGraph graph = obtainCurrentDirectedGraph(baseModel, orgCateType);
        for (Map<String, Object> value : valuesList) {
            String orgCateId = value.get(OrgConst.ORG_CATE_ID_FIELD_NAME).toString();
            if (!value.containsKey(OrgConst.HL_ORG_CATE_ID_FIELD_NAME) || value.get(OrgConst.HL_ORG_CATE_ID_FIELD_NAME) == null) {
                continue;
            }
            String hlOrgCateId = value.get(OrgConst.HL_ORG_CATE_ID_FIELD_NAME).toString();
            if (orgCateId.equals(hlOrgCateId)) {
                throw Exceptions.LEVEL_CREATE_HL_SAME_EXCEPTION;
            }
            graph.addEdge(hlOrgCateId, orgCateId);
            if (graph.hasCycle()) {
                throw Exceptions.LEVEL_CREATE_CYCLE_EXCEPTION;
            }
        }
    }


    /**
     * 删除校验： 判断层级关系是否已经被引用
     *
     * @param ids
     * @param rs
     * @param baseModel
     * @param <T>
     */
    public static <T extends BaseModel> void deleteCheck(List<String> ids, RecordSet rs, BaseModel<T> baseModel, OrgCateType orgCateType) {
        Filter filter = new Filter();
        filter.and(Filter.in("id", ids));
        List<String> propertiesList = new ArrayList<>();
        propertiesList.add(OrgConst.ORG_CATE_ID_FIELD_NAME);
        propertiesList.add(OrgConst.HL_ORG_CATE_ID_FIELD_NAME);
        List<T> entities = baseModel.search(filter, propertiesList, 0, 0, null);
        Filter numFilter = new Filter();
        for (T entity : entities) {
            String orgCateId = obtainOptionVal(entity, OrgConst.ORG_CATE_ID_FIELD_NAME);
            String hlOrgCateId = obtainOptionVal(entity, OrgConst.HL_ORG_CATE_ID_FIELD_NAME);
            numFilter.or(Filter.equal(OrgConst.ORG_CATE_ID_FIELD_NAME, orgCateId).and(Filter.equal(OrgConst.HL_ORG_CATE_ID_FIELD_NAME, hlOrgCateId)));
        }
        boolean deleteEdgeCheck = deleteEdgeCheck(entities, baseModel, orgCateType);
        if (!deleteEdgeCheck) {
            throw Exceptions.LEVEL_DELETE_CHILD_EXIST_EXCEPTION;
        }
        long num = (long) rs.call("count", rs, numFilter);
        if (num != 0) {
            throw Exceptions.LEVEL_DELETE_QUOTED_EXCEPTION;
        }
    }

    private static <T extends BaseModel> boolean deleteEdgeCheck(List<T> entities, BaseModel<T> baseModel, OrgCateType orgCateType) {
        DirectedGraph graph = obtainCurrentDirectedGraph(baseModel, orgCateType);
        for (T entity : entities) {
            String orgCateId = obtainOptionVal(entity, OrgConst.ORG_CATE_ID_FIELD_NAME);
            String hlOrgCateId = obtainOptionVal(entity, OrgConst.HL_ORG_CATE_ID_FIELD_NAME);
            if (!graph.canRemoveEdge(hlOrgCateId, orgCateId)) {
                return false;
            }
        }
        return true;
    }


    /**
     * 获取当前数据库中的数据，形成有向图
     *
     * @param baseModel
     * @param orgCateType
     * @param <T>
     * @return
     */
    private static <T extends BaseModel> DirectedGraph obtainCurrentDirectedGraph(BaseModel<T> baseModel, OrgCateType orgCateType) {
        Filter filter = new Filter();
        filter.and(Filter.equal(OrgConst.CATE_TYPE_FIELD_NAME, orgCateType.getId()));
        List<String> propertiesList = new ArrayList<>();
        propertiesList.add(OrgConst.ORG_CATE_ID_FIELD_NAME);
        propertiesList.add(OrgConst.HL_ORG_CATE_ID_FIELD_NAME);
        List<T> entities = baseModel.search(filter, propertiesList, 0, 0, null);
        DirectedGraph graph = new DirectedGraph();
        for (T entity : entities) {
            String orgCateId = obtainOptionVal(entity, OrgConst.ORG_CATE_ID_FIELD_NAME);
            String hlOrgCateId = obtainOptionVal(entity, OrgConst.HL_ORG_CATE_ID_FIELD_NAME);
            graph.addEdge(hlOrgCateId, orgCateId);
        }
        return graph;
    }

    private static <T extends BaseModel> String obtainOptionVal(T entity, String name) {
        Object dbVal = entity.get(name);
        if (dbVal instanceof String) {
            return dbVal.toString();
        } else if (dbVal instanceof Options) {
            return ((Options) dbVal).getValue();
        }
        return null;
    }

}
