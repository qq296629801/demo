package com.sie.iidp.example.mixmodel.model;

import com.sie.iidp.example.mixmodel.constant.HiddenApi;
import com.sie.iidp.example.mixmodel.model.request.MaterialInventoryExtendUpdateReq;
import com.sie.iidp.example.mixmodel.model.request.MbmMaterialInventoryAddReq;
import com.sie.iidp.example.mixmodel.model.request.MbmMaterialInventoryDtoReq;
import com.sie.iidp.example.mixmodel.model.response.MaterialInventoryRes;
import com.sie.iidp.example.mixmodel.model.response.MbmMaterialInventoryRes;
import com.sie.iidp.example.mixmodel.service.MbmMaterialInventoryService;
import com.sie.snest.engine.constant.MetaConstant;
import com.sie.snest.engine.rule.Filter;
import com.sie.snest.engine.utils.Utils;
import com.sie.snest.sdk.BaseModel;
import com.sie.snest.sdk.ResponseModel;
import com.sie.snest.sdk.annotation.meta.InjectMeta;
import com.sie.snest.sdk.annotation.meta.MethodService;
import com.sie.snest.sdk.annotation.meta.Model;
import com.sie.snest.sdk.annotation.meta.Property;
//import com.sie.snest.sdk.request.ListRequest;
//import com.sie.snest.sdk.response.BooleanResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;

/**
 * 继承示例
 */
@Model(displayName = "Mbm物料库存管理", name = "demo_mbm_material_inventory", parent = "demo_material_inventory",
        hiddenApi = {HiddenApi.EXIST_FOR_IGNORE_SG, HiddenApi.DELETE_BY_FILTER})
public class MbmMaterialInventory extends BaseModel<MbmMaterialInventory> {
    static Logger logger = LoggerFactory.getLogger(MbmMaterialInventory.class);

    @InjectMeta// 默认单例
    MbmMaterialInventoryService service = initService(MbmMaterialInventoryService.class);
//    //方案2-原型（暂不支持）
//    @InjectMeta(service = "userService", scope = InjectMeta.ScopeType.PROTOTYPE)
//    MaterialInventoryService service2 = initService("serviceName");

    @Property(displayName = "Mbm最大库存")
    private Integer maxCount;
    @Property(displayName = "Mbm最小库存")
    private Integer mbmMinCount;


    @MethodService(description = "新增（返回类型基础类，不能扩展）", doc = "./doc/05.二开能力专题.md")
    public boolean create(List<MbmMaterialInventoryAddReq> valuesList) {
        return service.create(valuesList);
    }

    @MethodService(description = "修改（入参出参可扩展）", doc = "http://iidp.chinasie.com:9999/iidpdoc/pages/904b3d")
    public MbmMaterialInventoryRes update(MaterialInventoryExtendUpdateReq values) {
        return service.update(values);
    }

//    @MethodService(description = "删除（可扩展的基础类型）")
//    public BooleanResponse delete(ListRequest<String> ids) {
//        if (ids != null && Utils.isNotBlank(ids.getList())) {
//            service.delete(Filter.in(MetaConstant.ID, ids.getList()));
//        }
//        return new BooleanResponse(true);
//    }

    public List<MbmMaterialInventory> search(Filter filter, List<String> properties, Integer limit, Integer offset, String order) {
        return service.searchMbm(filter, properties, limit, offset, order);
    }

    @MethodService(description = "插槽2（MBM扩展）", hiddenApi = true, doc = "./doc/05.二开能力专题.md")
    public ResponseModel slot2(MbmMaterialInventoryDtoReq req) {
        logger.info("工艺流程 model={}，工序={}", this.meta.getModelName(), "插槽2-------【MBM扩展实现】");
        return null;
    }

    @MethodService(description = "焊接（覆盖）", hiddenApi = true)
    public MaterialInventoryRes weld(MbmMaterialInventoryDtoReq req) {
        logger.info("工艺流程 model={}，工序={}", this.meta.getModelName(), "覆盖焊接，并且跳过装配步骤-------");
        return null;
    }
}
