package com.sie.iidp.example.mixmodel.model.request;

import com.sie.meta.plugin.Getter;
import com.sie.meta.plugin.Setter;
import com.sie.snest.sdk.RequestModel;
import com.sie.snest.sdk.annotation.meta.Model;
import com.sie.snest.sdk.annotation.meta.Property;

@Getter
@Setter
@Model(displayName = "物料库存修改dto", name = "d_material_inventory_update_req")
public class MaterialInventoryExtendUpdateReq extends RequestModel {

    @Property(displayName = "ID", readonly = true)
    protected String id;
    @Property(displayName = "物料编码", readonly = true)
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
