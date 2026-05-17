package com.sie.iidp.example.itemmgr.model;

import com.sie.meta.plugin.Getter;
import com.sie.meta.plugin.Setter;
import com.sie.meta.plugin.StaticVar;
import com.sie.snest.engine.model.Bool;
import com.sie.snest.sdk.BaseModel;
import com.sie.snest.sdk.CascadeType;
import com.sie.snest.sdk.annotation.meta.Model;
import com.sie.snest.sdk.annotation.meta.Property;
import com.sie.snest.sdk.annotation.orm.JoinColumn;
import com.sie.snest.sdk.annotation.orm.ManyToOne;
import lombok.extern.slf4j.Slf4j;

/**
 * Example-物料打印设置
 *
 * @author cxh
 * @date 2024年04月09日 10:49
 */
@StaticVar
@Getter
@Setter
@Slf4j
@Model(tableName = "example_item_print_config", name = "example_item_print_config", description = "Example-物料打印设置", displayName = "Example-物料打印设置",
        isLogicDelete = Bool.False, isAutoLog = Bool.True)
public class ExampleItemPrintConfig extends BaseModel<ExampleItemPrintConfig> {

    @Property(displayName = "条码规则ID", columnName = "barcode_rule_id", length = 20)
    private String barcodeRuleId;

    @Property(displayName = "标签模板ID", columnName = "label_template_id", length = 20)
    private String labelTemplateId;

    @Property(displayName = "包装模板ID", columnName = "packing_template_id", length = 20)
    private String packingTemplateId;

    @ManyToOne(displayName = "物料", cascade = CascadeType.DELETE)
    @JoinColumn(name = "item_id", referencedProperty = "id")
    private ExampleItem exampleItem;

    // todo 待编码规则和打印模板功能迁移进来后，此处改为从编码规则和打印模板中获取
}
