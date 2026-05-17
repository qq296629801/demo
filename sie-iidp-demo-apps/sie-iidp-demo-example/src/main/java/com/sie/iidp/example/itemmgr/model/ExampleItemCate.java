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
import com.sie.snest.sdk.annotation.orm.OneToMany;
import lombok.extern.slf4j.Slf4j;

import java.util.List;

/**
 * Example-物料分类
 *
 * @author cxh
 * @date 2024年04月09日 10:46
 */
@StaticVar
@Getter
@Setter
@Slf4j
@Model(tableName = "example_item_cate", name = "example_item_cate", description = "Example-物料分类", displayName = "Example-物料分类",
        isLogicDelete = Bool.False, isAutoLog = Bool.True)
public class ExampleItemCate extends BaseModel<ExampleItemCate> {

    @Property(displayName = "分类ID", columnName = "cate_id", length = 20)
    private String cateId;

    @Property(displayName = "分类类型", columnName = "cate_type", length = 20)
    private String cateType;

    @Property(displayName = "分类编码", columnName = "cate_code", length = 60)
    private String cateCode;

    @Property(displayName = "分类名称", columnName = "cate_name", length = 60, displayForModel = true)
    private String cateName;

    @ManyToOne(displayName = "物料", cascade = CascadeType.DELETE)
    @JoinColumn(name = "item_id", referencedProperty = "id")
    private ExampleItem exampleItem;

    /**
     * 物料分类方法
     */
    @OneToMany
    private List<ExampleItemCateMethod> itemCateMethods;

    /**
     * 物料分类步骤
     */
    @OneToMany
    private List<ExampleItemCateStep> itemCateSteps;

}
