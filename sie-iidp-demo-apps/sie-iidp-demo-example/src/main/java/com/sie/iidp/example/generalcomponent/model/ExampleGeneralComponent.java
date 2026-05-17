package com.sie.iidp.example.generalcomponent.model;

import com.sie.meta.plugin.Getter;
import com.sie.meta.plugin.Setter;
import com.sie.meta.plugin.StaticVar;
import com.sie.snest.engine.model.Bool;
import com.sie.snest.sdk.BaseModel;
import com.sie.snest.sdk.annotation.meta.Model;
import com.sie.snest.sdk.annotation.meta.Property;
import com.sie.snest.sdk.annotation.validate.Validate;

/**
 * Example-用于展示前端通用组件
 *
 * @author cxh
 * @date 2024年04月17日 14:18
 */
@StaticVar
@Getter
@Setter
@Model(tableName = "example_general_component", name = "example_general_component", description = "Example通用组件", displayName = "Example通用组件",
        isLogicDelete = Bool.True, isAutoLog = Bool.True)
public class ExampleGeneralComponent extends BaseModel<ExampleGeneralComponent> {

    @Validate.Size(max = 20)
    @Validate.NotBlank(message = "组件名称不能为空")
    @Property(displayName = "组件名称", columnName = "component_name")
    private String componentName;

}
