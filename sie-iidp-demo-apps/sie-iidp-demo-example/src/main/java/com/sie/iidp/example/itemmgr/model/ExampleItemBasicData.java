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
 * Example-物料基本资料
 *
 * @author cxh
 * @date 2024年04月09日 10:44
 */
@StaticVar
@Getter
@Setter
@Slf4j
@Model(tableName = "example_item_basic_data", name = "example_item_basic_data", description = "Example-物料基本资料", displayName = "Example-物料基本资料",
        isLogicDelete = Bool.False, isAutoLog = Bool.True)
public class ExampleItemBasicData extends BaseModel<ExampleItemBasicData> {

    @Property(displayName = "描述", columnName = "desc", length = 300)
    private String desc;

    @Property(displayName = "英文描述", columnName = "english_desc", length = 300)
    private String englishDesc;

    @Property(displayName = "物料简称", columnName = "item_short", length = 60)
    private String itemShort;

    @Property(displayName = "单位精度", columnName = "unit_accuracy", length = 10)
    private Integer unitAccuracy;

    @Property(displayName = "基准编码", columnName = "basic_code", length = 60)
    private String basicCode;

    @Property(displayName = "机型", columnName = "machine_type", length = 60)
    private String machineType;

    @Property(displayName = "图号", columnName = "drawing_no", length = 60)
    private String drawingNo;

    @ManyToOne(displayName = "物料", cascade = CascadeType.DELETE)
    @JoinColumn(name = "item_id", referencedProperty = "id")
    private ExampleItem exampleItem;

}
