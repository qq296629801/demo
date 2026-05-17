package com.sie.iidp.example.orglevel.model;

import com.sie.iidp.example.orglevel.consts.OrgConst;
import com.sie.iidp.example.orglevel.enums.OrgCateType;
import com.sie.iidp.example.orglevel.utils.OrgCategoryUtils;
import com.sie.iidp.example.orglevel.utils.OrgLevelUtils;
import com.sie.meta.plugin.Getter;
import com.sie.meta.plugin.Setter;
import com.sie.meta.plugin.StaticVar;
import com.sie.snest.engine.container.Meta;
import com.sie.snest.engine.data.RecordSet;
import com.sie.snest.engine.model.Bool;
import com.sie.snest.engine.rule.Filter;
import com.sie.snest.engine.utils.Options;
import com.sie.snest.sdk.BaseModel;
import com.sie.snest.sdk.annotation.meta.MethodService;
import com.sie.snest.sdk.annotation.meta.Model;
import com.sie.snest.sdk.annotation.meta.Property;
import com.sie.snest.sdk.annotation.orm.JoinColumn;
import com.sie.snest.sdk.annotation.orm.ManyToOne;
import com.sie.snest.sdk.annotation.orm.OneToMany;
import com.sie.snest.sdk.annotation.orm.Selection;
import com.sie.snest.sdk.annotation.validate.Validate;

import java.util.List;
import java.util.Map;

/**
 * @Author syh
 * @Date 2024/4/8 10:16
 */
@StaticVar
@Getter
@Setter
@Model(tableName = "example_org_level", name = "example_org_level", description = "Example-企业层级(树形菜单)", displayName = "Example-企业层级(树形菜单)",
        isLogicDelete = Bool.True, isAutoLog = Bool.True)
public class ExampleOrgLevel extends BaseModel<ExampleOrgLevel> {

    /**
     * 组织类别
     */
    @Validate.NotBlank(message = "类型不能为空")
    @Selection(method = "orgCategoryList")
    @Property(displayName = "类型", columnName = "org_cate_id", length = 64)
    private String orgCateId;

    /**
     * 上级组织类别ID
     */
    @Validate.Unique(properties = {"orgCateId", "hlOrgCateId", "cateType"}, message = "已存在相同的层级关系")
    @Selection(method = "hlOrgCategoryList")
    @Property(displayName = "上级类别 ", columnName = "hl_org_cate_id", length = 64)
    private String hlOrgCateId;

    @Property(displayName = "类型名称", columnName = "org_cate_name", store = false, computeMethod = "obtainOrgCateName")
    private String orgCateName;

    @Property(displayName = "上级类型名称", columnName = "hl_org_cate_name", store = false, computeMethod = "obtainHlOrgCateName")
    private String hlOrgCateName;

    @Property(displayName = "层级类型", columnName = "cate_type")
    @Validate.NotBlank(message = "层级类型不能为空")
    private String cateType;

    /**
     * 备注
     */
    @Property(displayName = "备注", columnName = "remark", length = 200)
    @Validate.Size(max = 200)
    private String remark;

    /**
     * 数据同步ID
     */
    @Property(displayName = "数据同步ID", columnName = "sync_id", length = 64)
    private String syncId;


    @ManyToOne(displayName = "上级类型名称")
    @JoinColumn(name = "hl_org_cate_id", referencedProperty = "treeOrgCateId")
    private ExampleOrgLevel hlOrgCateIdp;

    @OneToMany
    private List<ExampleOrgLevel> children;

    @MethodService(description = "获取上级分类列表")
    public List<Options> hlOrgCategoryList(Object value) {
        return OrgCategoryUtils.obtainOrgCategoryList(value, OrgCateType.ENTERPRISE);
    }

    @MethodService(description = "分类列表")
    public List<Options> orgCategoryList(Object value) {
        return OrgCategoryUtils.obtainOrgCategoryList(value, OrgCateType.ENTERPRISE);
    }

    private static final String CATE_TYPE_ID = String.valueOf(OrgCateType.ENTERPRISE.getId());

    /**
     * 重写查询
     * @param filter 入参
     * @param properties 参数
     * @param limit 分页
     * @param offset 每页数量
     * @param order
     * @return
     */
    @Override
    public List<ExampleOrgLevel> search(Filter filter, List<String> properties, Integer limit, Integer offset, String order) {
        filter.and(Filter.equal(ExampleOrgLevel.F_CATE_TYPE, CATE_TYPE_ID));
        return super.search(filter, properties, limit, offset, order);
    }

    public Object create(List<Map<String, Object>> valuesList) {
        Meta meta = this.getMeta();
        RecordSet rs = meta.get(this.getModelName());
        valuesList.stream().forEach(values -> {
            values.put(ExampleOrgLevel.F_CATE_TYPE, CATE_TYPE_ID);
        });
        OrgLevelUtils.createCheck(this, OrgCateType.ENTERPRISE, valuesList);
        return rs.callSuper(ExampleOrgLevel.class, "create", valuesList);
    }


    @MethodService(description = "统计数量")
    @Override
    public long count(Filter filter) {
        filter.and(Filter.equal(ExampleOrgLevel.F_CATE_TYPE, CATE_TYPE_ID));
        return super.count(filter);

    }


    public String obtainOrgCateName(Map<String, Object> data) {
        return OrgCategoryUtils.convertCateName(data, ExampleOrgLevel.F_ORG_CATE_ID);
    }

    public String obtainHlOrgCateName(Map<String, Object> data) {
        return OrgCategoryUtils.convertCateName(data, ExampleOrgLevel.F_HL_ORG_CATE_ID);
    }
}
