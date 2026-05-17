package com.sie.iidp.example.mixmodel.service;

import com.sie.iidp.example.mixmodel.model.MaterialInventoryExtend;
import com.sie.iidp.example.mixmodel.model.request.MaterialInventoryExtendAddReq;
import com.sie.iidp.example.mixmodel.model.request.MaterialInventoryExtendUpdateReq;
import com.sie.iidp.example.mixmodel.model.response.MaterialInventoryRes;
import com.sie.snest.engine.data.service.JsonUtil;
import com.sie.snest.engine.rule.Filter;
import com.sie.snest.engine.utils.Utils;
import com.sie.snest.sdk.SdkService;
import com.sie.snest.sdk.annotation.meta.Service;
import com.sie.snest.sdk.exception.SdkAppException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Arrays;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

@Service
public class MaterialInventoryService extends SdkService<MaterialInventoryExtend> {
    static Logger logger = LoggerFactory.getLogger(MaterialInventoryService.class);

    public boolean create(List<MaterialInventoryExtendAddReq> valueList) throws SdkAppException {
//        Set<String> fields = this.getModel(true).getPropertiesMap().keySet();
//        logger.info("执行新增：model={}，当前{}的所有列：{}", getModelName().get(), fields);
//
//        super.batchCreate(valueList);
//
//        List<String> invCodes = valueList.stream().map(MaterialInventoryExtendAddReq::getInvCode).collect(Collectors.toList());
//        List<MaterialInventoryExtend> list = super.search(Filter.in("invCode", invCodes), null, null, 0, null);
//        System.out.println("当前新增：" + JsonUtil.toString(list));

        return true;
    }

    public MaterialInventoryRes update(MaterialInventoryExtendUpdateReq values) throws SdkAppException {
//        Set<String> fields = this.getModel(true).getPropertiesMap().keySet();
//        logger.info("执行修改：model={}，当前{}的所有列：{}", getModelName().get(), fields);
//
//        super.update(values);
//
//        List<MaterialInventoryExtend> list = super.search(Filter.equal("id", values.get("id")),
//                Arrays.asList("id", "stock_count", "max_count", "min_count"), 1, 0, "id desc");
//        MaterialInventoryRes res = null;
//        if (Utils.isNotEmpty(list)) {
//            res = super.copyToRes(list.get(0), MaterialInventoryRes.class);
//        }
//        return res;
        return null;
    }

}