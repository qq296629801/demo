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
 * Example-物料分类步骤
 *
 * @author cxh
 * @date 2024年04月09日 10:46
 */
@StaticVar
@Getter
@Setter
@Slf4j
@Model(name = "example_item_cate_step", description = "Example-物料分类步骤", displayName = "Example-物料分类步骤",
        isLogicDelete = Bool.False, isAutoLog = Bool.True, isPrint = Bool.True)
public class ExampleItemCateStep extends BaseModel<ExampleItemCateStep> {

    @Property(displayName = "分类目标", displayForModel = true)
    private String des;

    @Property(displayName = "收集物料信息")
    private String collect;

    @Property(displayName = "分类标准")
    private String standard;

    @ManyToOne(displayName = "物料分类", cascade = CascadeType.DELETE)
    @JoinColumn(name = "cate_id", referencedProperty = "id")
    private ExampleItemCate itemCate;

}
