package com.sie.iidp.example.mixmodel.model.response;

import com.sie.meta.plugin.Getter;
import com.sie.meta.plugin.Setter;
import com.sie.snest.sdk.ResponseModel;
import com.sie.snest.sdk.annotation.meta.Model;
import com.sie.snest.sdk.annotation.meta.Property;

@Getter
@Setter
@Model(displayName = "物料库存出参", name = "d_mbm_material_inventory_res", parent = "d_material_inventory_res")
public class MbmMaterialInventoryRes extends ResponseModel {

    @Property(displayName = "Mbm最大库存")
    private Integer maxCount;
    @Property(displayName = "Mbm最小库存")
    private Integer mbmMinCount;

}
