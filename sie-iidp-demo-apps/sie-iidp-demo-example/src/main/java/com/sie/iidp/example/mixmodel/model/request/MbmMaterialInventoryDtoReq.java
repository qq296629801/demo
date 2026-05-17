package com.sie.iidp.example.mixmodel.model.request;

import com.sie.meta.plugin.Getter;
import com.sie.meta.plugin.Setter;
import com.sie.snest.sdk.RequestModel;
import com.sie.snest.sdk.annotation.meta.Model;

@Getter
@Setter
@Model(displayName = "物料库存修改dto（扩展）", name = "mbm_material_inventory_dto_req",
        parent = "d_material_inventory_update_req")
public class MbmMaterialInventoryDtoReq extends RequestModel {

    protected String invName;
    private Integer mbmInCount;

}
