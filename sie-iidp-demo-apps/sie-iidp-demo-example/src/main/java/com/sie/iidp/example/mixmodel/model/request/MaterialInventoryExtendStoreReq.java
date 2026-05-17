package com.sie.iidp.example.mixmodel.model.request;

import com.sie.meta.plugin.Getter;
import com.sie.meta.plugin.Setter;
import com.sie.snest.sdk.RequestModel;
import com.sie.snest.sdk.annotation.meta.Model;

@Getter
@Setter
@Model(name = "d_MaterialInventoryStoreReq", displayName = "请求入参(DTO)模型")
public class MaterialInventoryExtendStoreReq extends RequestModel {
    private Integer maxCount;
    private Integer minCount;
    private Integer stockCount;


    public MaterialInventoryExtendStoreReq(Integer maxCount, Integer minCount, Integer stockCount) {
        this.put("maxCount", maxCount == null ? 0 : maxCount);
        this.put("minCount", minCount == null ? 0 : minCount);
        this.put("stockCount", stockCount == null ? 0 : stockCount);
    }

    public int getMaxCount() {
        return (Integer)this.getOrDefault("maxCount", 0);
    }

    public MaterialInventoryExtendStoreReq setMaxCount(int maxCount) {
        this.put("maxCount", maxCount);
        return this;
    }

    public int getMinCount() {
        return (Integer)this.getOrDefault("minCount", 0);
    }

    public MaterialInventoryExtendStoreReq setMinCount(int minCount) {
        this.put("minCount", minCount);
        return this;
    }

    public int getStockCount() {
        return (Integer)this.getOrDefault("stockCount", 0);
    }

    public MaterialInventoryExtendStoreReq setStockCount(int stockCount) {
        this.put("stockCount", stockCount);
        return this;
    }
}
