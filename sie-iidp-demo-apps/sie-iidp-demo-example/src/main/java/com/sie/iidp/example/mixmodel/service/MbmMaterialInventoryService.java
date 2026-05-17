package com.sie.iidp.example.mixmodel.service;

import com.sie.iidp.example.mixmodel.model.MbmMaterialInventory;
import com.sie.iidp.example.mixmodel.model.request.MaterialInventoryExtendUpdateReq;
import com.sie.iidp.example.mixmodel.model.request.MbmMaterialInventoryAddReq;
import com.sie.iidp.example.mixmodel.model.response.MbmMaterialInventoryRes;
import com.sie.snest.engine.data.RecordSet;
import com.sie.snest.engine.rule.Filter;
import com.sie.snest.engine.utils.Utils;
import com.sie.snest.sdk.annotation.meta.Service;
import com.sie.snest.sdk.exception.SdkAppException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

@Service
public class MbmMaterialInventoryService extends MbmService<MbmMaterialInventory> {
    static Logger logger = LoggerFactory.getLogger(MaterialInventoryService.class);

    public boolean create(List<MbmMaterialInventoryAddReq> valueList) throws SdkAppException {
//        Set<String> fields = this.getModel(true).getPropertiesMap().keySet();
//        logger.info("执行新增：model={}，当前{}的所有列：{}", getModelName().get(), fields);
//
//        // 调用base根节点
//        RecordSet rs = (RecordSet) getMeta().get("demo_mbm_material_inventory")
//                .callSuper(null, "create", copyList(valueList, MbmMaterialInventory.class));
//        logger.info("新增返回ID：{}", rs.getIds());
//
//        List<String> invCodes = valueList.stream().map(MbmMaterialInventoryAddReq::getInvCode).collect(Collectors.toList());
//        super.search(Filter.in("invCode", invCodes), null, null, 0, null);
        return true;
    }

    public MbmMaterialInventoryRes update(MaterialInventoryExtendUpdateReq values) throws SdkAppException {
//        Set<String> fields = this.getModel(true).getPropertiesMap().keySet();
//        logger.info("执行修改：model={}, 列：{}", getModelName().get(), fields);
//
//        // 调用base根节点
//        RecordSet rs = (RecordSet) getMeta().get("demo_mbm_material_inventory")
//                .callSuper(null, "update", copy(values, MbmMaterialInventory.class));
//        logger.info("返回ID：{}", rs.getIds());
//
//        List<MbmMaterialInventory> list = super.search(null, Arrays.asList("id", "stock_count", "max_count",
//                "min_count"), 1, 0, "id desc");
//        MbmMaterialInventoryRes res = null;
//        if (Utils.isNotEmpty(list)) {
//            res = super.copyToRes(list.get(0), MbmMaterialInventoryRes.class);
//        }
//        return res;
        return null;
    }

    public List<MbmMaterialInventory> searchMbm(Filter filter, List<String> properties, Integer limit, Integer offset, String order) {
//        Set<String> fields = this.getMeta().getModel().getModel().getPropertiesMap().keySet();
//        logger.info("查询 model={}，字段={}", this.getMeta().getModelName(), fields);
//
//        List<Map<String, Object>> list = (List<Map<String, Object>>) getMeta().get("demo_mbm_material_inventory")
//                .callSuper(null, "search", filter, properties, limit, offset, order);
//        logger.info("返回ID：{}", list);
//
//        return super.copyListFromMap(list, MbmMaterialInventory.class);
        return null;
    }

}
