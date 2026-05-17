package com.sie.iidp.example.mixmodel.model.response;

import com.sie.meta.plugin.Getter;
import com.sie.meta.plugin.Setter;
import com.sie.snest.sdk.ResponseModel;
import com.sie.snest.sdk.annotation.meta.Model;
import com.sie.snest.sdk.annotation.meta.Property;

@Getter
@Setter
@Model(displayName = "物料库存出参", name = "d_material_inventory_res")
public class MaterialInventoryRes extends ResponseModel {

    @Property(displayName = "ID")
    protected String id;
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

}
