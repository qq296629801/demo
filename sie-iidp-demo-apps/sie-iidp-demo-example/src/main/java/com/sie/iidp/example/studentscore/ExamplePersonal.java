package com.sie.iidp.example.studentscore;

import com.sie.snest.engine.data.RecordSet;
import com.sie.snest.engine.model.Bool;
import com.sie.snest.sdk.BaseModel;
import com.sie.snest.sdk.annotation.meta.MethodService;
import com.sie.snest.sdk.annotation.meta.Model;
import com.sie.snest.sdk.annotation.meta.Property;

import java.util.Map;

@Model(tableName = "example_personal", name = "example_personal", description = "Example人", displayName = "Example人",
        isLogicDelete = Bool.True, isAutoLog = Bool.True)
public class ExamplePersonal extends BaseModel<ExamplePersonal> {

    @Property(columnName = "color", displayName = "颜色")
    private String color;

    @Property(columnName = "age", displayName = "年龄")
    private int age;


    @MethodService(description = "导出")
    public Map<String, Object> export(RecordSet rs) {
        return null;
    }


}
