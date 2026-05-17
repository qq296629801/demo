package com.sie.iidp.example.mixmodel.model.request;

import com.sie.meta.plugin.Getter;
import com.sie.meta.plugin.Setter;
import com.sie.snest.engine.utils.IdGenerator;
import com.sie.snest.sdk.RequestModel;
import com.sie.snest.sdk.annotation.meta.Model;
import com.sie.snest.sdk.annotation.meta.Property;
import com.sie.snest.sdk.annotation.validate.Validate;

@Getter
@Setter
@Model(displayName = "物料库存新增dto", name = "d_material_inventory_add_req")
public class MaterialInventoryExtendAddReq extends RequestModel {

    @Property(displayName = "物料编码", computeMethod = "generateCode")
    private String invCode;
    @Validate.NotBlank
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

    public String generateCode() {
        return IdGenerator.nextId();
    }

}
