package com.sie.iidp.example.codesequence.model;

import com.sie.meta.plugin.Getter;
import com.sie.meta.plugin.Setter;
import com.sie.meta.plugin.StaticVar;
import com.sie.snest.engine.model.Bool;
import com.sie.snest.sdk.BaseModel;
import com.sie.snest.sdk.annotation.meta.Model;
import com.sie.snest.sdk.annotation.meta.Property;

/**
 * Example-编码序列表
 *
 * @author cxh
 * @date 2024年03月28日 15:56
 */
@StaticVar
@Getter
@Setter
@Model(tableName = "example_code_sequence", name = "example_code_sequence", description = "Example-编码序列表", displayName = "Example-编码序列表",
        isLogicDelete = Bool.False, isAutoLog = Bool.True)
public class ExampleCodeSequence extends BaseModel<ExampleCodeSequence> {


    /**
     * 当前值
     */
    @Property(displayName = "当前值")
    private Integer currValue;

    /**
     * 0=学生学号,1=读者编码(可扩展)
     */
    @Property(displayName = "类型")
    private String codeType;

}
