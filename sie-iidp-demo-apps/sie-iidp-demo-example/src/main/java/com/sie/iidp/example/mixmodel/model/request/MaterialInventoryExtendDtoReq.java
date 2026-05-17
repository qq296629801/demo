package com.sie.iidp.example.mixmodel.model.request;

import com.sie.meta.plugin.Getter;
import com.sie.meta.plugin.Setter;
import com.sie.snest.sdk.RequestModel;
import com.sie.snest.sdk.annotation.meta.Model;

@Setter
@Getter
@Model(name = "d_MaterialInventoryDtoReq", displayName = "请求入参(DTO)模型")
public class MaterialInventoryExtendDtoReq extends RequestModel {
    protected String id;
    protected String invCode;
    protected String invName;
    private Integer inCount;
}
