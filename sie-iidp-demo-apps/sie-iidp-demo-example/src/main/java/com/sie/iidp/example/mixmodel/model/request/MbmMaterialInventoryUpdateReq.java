package com.sie.iidp.example.mixmodel.model.request;

import com.sie.meta.plugin.Getter;
import com.sie.meta.plugin.Setter;
import com.sie.snest.sdk.RequestModel;
import com.sie.snest.sdk.annotation.meta.Model;
import com.sie.snest.sdk.annotation.meta.PropertyRef;

@Getter
@Setter
@Model(displayName = "物料库存修改dto", name = "d_mbm_material_inventory_update_req")
public class MbmMaterialInventoryUpdateReq extends RequestModel {
    @PropertyRef(dst = "MaterielInventory", name = "id")
    protected String id;
    @PropertyRef(dst = "MaterielInventory", name = "invCode")
    protected String invCode;
    @PropertyRef(dst = "MaterielInventory", name = "invName")
    protected String invName;
    @PropertyRef(dst = "MaterielInventory", name = "inCount")
    private Integer inCount;

}
