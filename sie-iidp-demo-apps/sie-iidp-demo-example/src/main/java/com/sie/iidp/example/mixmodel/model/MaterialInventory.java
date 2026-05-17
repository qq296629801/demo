package com.sie.iidp.example.mixmodel.model;

import com.sie.snest.engine.data.RecordSet;
import com.sie.snest.engine.rule.Filter;
import com.sie.snest.sdk.BaseModel;
import com.sie.snest.sdk.annotation.meta.MethodService;
import com.sie.snest.sdk.annotation.meta.Model;
import com.sie.snest.sdk.annotation.meta.Property;
import com.sie.snest.sdk.db.DbUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * 以旧方式(非强类型dto)实现示例
 */
@Model(displayName = "物料库存管理A", name = "demo_material_inventory")
public class MaterialInventory extends BaseModel<MaterialInventory> {
    static Logger logger = LoggerFactory.getLogger(MaterialInventory.class);
    @Property(displayName = "物料编码")
    private String invCode;
    @Property(displayName = "物料名称")
    private String invName;
    @Property(displayName = "单位")
    private String unit;
    @Property( displayName = "期初数量")
    private Integer initCount;
    @Property(displayName = "入库数量")
    private Integer inCount;
    @Property(displayName = "出库数量")
    private Integer outCount;
    @Property(displayName = "库存数量")
    private Integer stockCount;


    public boolean create(List<MaterialInventory> valuesList) {
        Set<String> fields = this.getMeta().getModel().getModel().getPropertiesMap().keySet();
        logger.info("新增 model={}，字段：{}", this.getMeta().getModelName(), fields);

        // 。。。。业务处理。。。。
        super.batchCreate(valuesList);
        return true;
    }

    @MethodService(description = "修改-重写", auth = "update")
    public RecordSet update(RecordSet rs, Map<String, Object> values) {
        logger.info("修改 model={}， ids={}", this.getMeta().getModelName(), rs.getIds());

        // 。。。。业务处理。。。。
        rs = (RecordSet) rs.callSuper(MaterialInventory.class, "update", new Object[]{values});
        return rs;
    }

    @MethodService(description = "修改-重写")
    public List<MaterialInventory> search(Filter filter, List<String> properties, Integer limit, Integer offset, String order) {
        Set<String> fields = this.getMeta().getModel().getModel().getPropertiesMap().keySet();
        logger.info("查询 model={}，字段={}", this.getMeta().getModelName(), fields);

        // 。。。。业务处理。。。。
        return DbUtils.search(filter, properties, limit, offset, order, this.getClass());
    }



    @MethodService(description = "工艺流程")
    public Object technological(Map<String, Object> req) {
        Set<String> fields = this.getMeta().getModel().getModel().getPropertiesMap().keySet();
        logger.info("工艺流程 model={}， 字段={}", this.getMeta().getModelName(), fields);

        // 。。。。业务处理。。。。
        return this.getMeta().call(this.getMeta().getApp(), this.getMeta().getModelName(), "weld", this.getMeta().getArguments(), Object.class);
    }

    @MethodService(description = "焊接")
    public Object weld(Map<String, Object> req) {
        logger.info("工艺流程 model={}，工序={}", this.getMeta().getModelName(), "焊接");

        // 。。。。业务处理。。。。
        return this.getMeta().call(this.getMeta().getApp(), this.getMeta().getModelName(), "assemble", this.getMeta().getArguments(), Object.class);
    }

    @MethodService(description = "装配")
    public Object assemble(Map<String, Object> req) {
        logger.info("工艺流程 model={}，工序={}", this.getMeta().getModelName(), "装配");

        // 。。。。业务处理。。。。
        return this.getMeta().call(this.getMeta().getApp(), this.getMeta().getModelName(), "preliminaryToSlot2", this.getMeta().getArguments(), Object.class);
    }

    @MethodService(description = "初测-老化-插槽1-中测-包装-插槽2")
    public Object preliminaryToSlot2(Map<String, Object> req) {
        logger.info("工艺流程 model={}，工序={}", this.getMeta().getModelName(), "初测");
        logger.info("工艺流程 model={}，工序={}", this.getMeta().getModelName(), "老化");
        String app = this.getMeta().getApp();
        String model = this.getMeta().getModelName();
        this.getMeta().call(app, model, "slot1", this.getMeta().getArguments(), String.class);
        logger.info("工艺流程 model={}，工序={}", this.getMeta().getModelName(), "中测");
        logger.info("工艺流程 model={}，工序={}", this.getMeta().getModelName(), "包装");
        this.getMeta().call(app, model, "slot2", this.getMeta().getArguments(), Object.class);

        return this.getMeta().call(this.getMeta().getApp(), this.getMeta().getModelName(), "packing", this.getMeta().getArguments(), Object.class);
    }

    @MethodService(description = "插槽1")
    public String slot1(Map<String, Object> req) {
        logger.info("工艺流程 model={}，工序={}", this.getMeta().getModelName(), "插槽1");
        return null;
    }

    @MethodService(description = "插槽2")
    public Object slot2(Map<String, Object> req) {
        logger.info("工艺流程 model={}，工序={}", this.getMeta().getModelName(), "插槽2(空实现)");
        return null;
    }

    @MethodService(description = "装箱")
    public Object packing(Map<String, Object> req) {
        logger.info("工艺流程 model={}，工序={}", this.getMeta().getModelName(), "装箱");
        return null;
    }

}
