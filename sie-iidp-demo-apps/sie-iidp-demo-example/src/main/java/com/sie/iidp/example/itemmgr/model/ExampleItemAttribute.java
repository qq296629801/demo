package com.sie.iidp.example.itemmgr.model;

import com.sie.meta.plugin.Getter;
import com.sie.meta.plugin.Setter;
import com.sie.meta.plugin.StaticVar;
import com.sie.snest.engine.model.Bool;
import com.sie.snest.sdk.BaseModel;
import com.sie.snest.sdk.CascadeType;
import com.sie.snest.sdk.annotation.Dict;
import com.sie.snest.sdk.annotation.meta.Model;
import com.sie.snest.sdk.annotation.meta.Property;
import com.sie.snest.sdk.annotation.orm.JoinColumn;
import com.sie.snest.sdk.annotation.orm.ManyToOne;
import lombok.extern.slf4j.Slf4j;

import java.math.BigDecimal;

/**
 * Example-物料扩展属性
 *
 * @author cxh
 * @date 2024年04月09日 9:09
 */
@StaticVar
@Getter
@Setter
@Slf4j
@Model(tableName = "example_item_attribute", name = "example_item_attribute", description = "Example-物料扩展属性", displayName = "Example-物料扩展属性",
        isLogicDelete = Bool.False, isAutoLog = Bool.True)
public class ExampleItemAttribute extends BaseModel<ExampleItemAttribute> {

    @Property(displayName = "颜色", columnName = "color", length = 60)
    private String color;

    @Property(displayName = "价格", columnName = "price", length = 20)
    private BigDecimal price;

    @Property(displayName = "重量", columnName = "weight", length = 20)
    private BigDecimal weight;

    @Dict(typeCode = "itemAttributeType")
    @Property(displayName = "物料属性类型", columnName = "attribute_type", length = 10)
    private Integer attributeType;

    @ManyToOne(displayName = "物料", cascade = CascadeType.DELETE)
    @JoinColumn(name = "item_id", referencedProperty = "id")
    private ExampleItem exampleItem;
}
