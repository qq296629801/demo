package com.sie.iidp.demo.wms.model;

import com.sie.meta.plugin.StaticVar;
import com.sie.meta.plugin.Getter;
import com.sie.meta.plugin.Setter;
import com.sie.snest.sdk.BaseModel;
import com.sie.snest.sdk.annotation.meta.Model;
import com.sie.snest.sdk.annotation.meta.Property;
import com.sie.snest.sdk.DataType;
import com.sie.snest.sdk.annotation.validate.Validate;
import com.sie.snest.sdk.annotation.orm.Index;

/**
 * WMS SKU 商品模型
 * 对应需求文档：M-01 SKU 商品管理
 */
@StaticVar
@Getter
@Setter
@Model(name = "wms_sku", label = "SKU商品")
public class WmsSku extends BaseModel<WmsSku> {

    @Property(name = "sku_code", label = "商品编码", type = DataType.Char, len = 64, required = true)
    @Validate(unique = true, message = "编码已存在")
    @Index(unique = true)
    private Object skuCode;

    @Property(name = "sku_name", label = "商品名称", type = DataType.Char, len = 200, required = true)
    @Validate(maxLength = 200)
    private Object skuName;

    @Property(name = "unit", label = "计量单位", type = DataType.Char, len = 20, required = true)
    private Object unit;

    @Property(name = "category", label = "商品分类", type = DataType.Char, len = 64)
    private Object category;

    @Property(name = "safety_stock_qty", label = "安全库存下限", type = DataType.Float, digits = 18, decimal = 4)
    @Validate(min = 0, message = "安全库存不能为负数")
    private Object safetyStockQty;

    @Property(name = "is_valid", label = "是否启用", type = DataType.Bool, required = true)
    private Object isValid;

    @Property(name = "remark", label = "备注", type = DataType.Text, len = 500)
    private Object remark;
}
