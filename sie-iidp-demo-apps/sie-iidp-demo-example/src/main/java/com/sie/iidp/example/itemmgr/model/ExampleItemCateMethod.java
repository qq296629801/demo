package com.sie.iidp.example.itemmgr.model;

import com.sie.iidp.example.datasoure.ExampleDataSource;
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
import com.sie.snest.sdk.annotation.orm.Option;
import com.sie.snest.sdk.annotation.orm.Selection;
import lombok.extern.slf4j.Slf4j;

/**
 * Example-物料分类方法
 *
 * @author cxh
 * @date 2024年04月09日 10:46
 */
@StaticVar
@Getter
@Setter
@Slf4j
@Model(name = "example_item_cate_method", displayName = "Example-物料分类方法", isLogicDelete = Bool.False, isAutoLog = Bool.True)
public class ExampleItemCateMethod extends BaseModel<ExampleItemCateMethod> {

    @Property(displayName = "分类方法名", displayForModel = true)
    private String name;

    @Property(displayName = "方法类型")
    @Selection(values = {@Option(label = "通用分类方法", value = "1"),
            @Option(label = "行业特定分类方法", value = "2"),
            @Option(label = "自定义分类方法", value = "3")})
    private String methodType;

    @ManyToOne(displayName = "物料分类", cascade = CascadeType.DELETE)
    @JoinColumn(name = "cate_id", referencedProperty = "id")
    private ExampleItemCate itemCate;

}
