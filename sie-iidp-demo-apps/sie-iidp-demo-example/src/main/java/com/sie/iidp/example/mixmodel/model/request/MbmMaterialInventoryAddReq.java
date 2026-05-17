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
@Model(displayName = "物料库存新增dto", name ="d_MbmMaterialInventoryAddReq")
public class MbmMaterialInventoryAddReq extends RequestModel {
    @Property(displayName = "物料编码", computeMethod = "generateCode")
    private String invCode;

    @Validate.NotBlank
    @Validate.Max(10)
    @Property(displayName = "物料名称")
    private String invName;

    private Integer mbmMinCount;
    //@PropertyRef(dst = "XXXModel", name = "invName" )// 暂不支持
    private Integer invName2;

    public String generateCode() {
        return IdGenerator.nextId();
    }

}
