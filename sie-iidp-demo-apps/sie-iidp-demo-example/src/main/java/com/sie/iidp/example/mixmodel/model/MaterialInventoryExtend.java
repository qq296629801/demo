package com.sie.iidp.example.mixmodel.model;

import com.sie.iidp.example.mixmodel.model.request.MaterialInventoryExtendAddReq;
import com.sie.iidp.example.mixmodel.model.request.MaterialInventoryExtendDtoReq;
import com.sie.iidp.example.mixmodel.model.request.MaterialInventoryExtendStoreReq;
import com.sie.iidp.example.mixmodel.model.request.MaterialInventoryExtendUpdateReq;
import com.sie.iidp.example.mixmodel.model.response.MaterialInventoryRes;
import com.sie.iidp.example.mixmodel.service.MaterialInventoryService;
import com.sie.meta.plugin.Getter;
import com.sie.meta.plugin.Setter;
import com.sie.snest.engine.rule.Filter;
import com.sie.snest.engine.utils.Utils;
import com.sie.snest.sdk.BaseModel;
import com.sie.snest.sdk.annotation.meta.InjectMeta;
import com.sie.snest.sdk.annotation.meta.MethodService;
import com.sie.snest.sdk.annotation.meta.Model;
import com.sie.snest.sdk.annotation.meta.Property;
//import com.sie.snest.sdk.request.ListRequest;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;
import java.util.Set;

@Getter
@Setter
@Model( displayName = "物料库存管理——扩展", name = "demo_material_inventory", parent = {"demo_material_inventory"})
public class MaterialInventoryExtend extends BaseModel<MaterialInventoryExtend> {
    static Logger logger = LoggerFactory.getLogger(MaterialInventoryExtend.class);
    @InjectMeta
    MaterialInventoryService service = this.initService(MaterialInventoryService.class);

    @Property(displayName = "最大库存")
    private Integer maxCount;
    @Property(displayName = "最小库存")
    private Integer minCount;


    @MethodService(description = "新增-入参强类型", doc = "./doc/05.二开能力专题.md")
    public boolean create(List<MaterialInventoryExtendAddReq> valuesList) {
        return service.create(valuesList);
    }

    @MethodService(description = "修改-入参出参强类型", doc = "http://iidp.chinasie.com:9999/iidpdoc/pages/904b3d")
    public MaterialInventoryRes update(MaterialInventoryExtendUpdateReq values) {
        return service.update(values);
    }

//    @MethodService(description = "删除", hiddenApi = true)
//    public boolean delete(ListRequest<String> ids) {
//        if (ids != null && Utils.isNotBlank(ids.getList())) {
//            this.delete(Filter.in("id", ids.getList()));
//        }
//        return true;
//    }
//
//    public List<MaterialInventoryExtend> search(Filter filter, List<String> properties, Integer limit, Integer offset, String order) {
//        Set<String> fields = this.getMeta().getModel().getModel().getPropertiesMap().keySet();
//        logger.info("查询 model={}，字段={}", service.getModelName().get(), fields);
//
//        return service.search(filter, properties, limit, offset, order);
//    }




    @MethodService(description = "预警")
    public MaterialInventoryRes warning(MaterialInventoryExtendStoreReq req) {
        if (req.getStockCount() >= req.getMaxCount()) {
            logger.info(">>>>>触发预警逻辑 model={}，当前库存={}, 最小库存={}", this.getMeta().getModelName(),
                    req.getStockCount(), req.getMinCount());
        }
        return null;
    }

    @MethodService(description = "补货")
    public boolean replenish(MaterialInventoryExtendStoreReq req) {
        if (req.getStockCount() <= req.getMinCount()) {
            logger.info(">>>>>触发补货逻辑 model={}，当前库存={}, 最小库存={}", this.getMeta().getModelName(),
                    req.getStockCount(), req.getMinCount());
        }
        return true;
    }

    @MethodService(description = "插槽2（扩展了）", hiddenApi = true)
    public MaterialInventoryRes slot2(MaterialInventoryExtendDtoReq req, String name) {
        logger.info("工艺流程 model={}，工序={}", this.getMeta().getModelName(), "插槽2-------【扩展实现】");

        return null;
    }

}