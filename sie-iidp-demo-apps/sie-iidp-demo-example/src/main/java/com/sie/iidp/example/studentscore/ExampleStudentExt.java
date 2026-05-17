package com.sie.iidp.example.studentscore;

import com.sie.snest.engine.model.Bool;
import com.sie.snest.sdk.BaseModel;
import com.sie.snest.sdk.annotation.meta.Model;
import com.sie.snest.sdk.annotation.meta.Property;

@Model(tableName = "example_student", name = "example_student", description = "Example学生", displayName = "Example学生",
        isLogicDelete = Bool.True, isAutoLog = Bool.True, parent = "example_student,example_personal")
public class ExampleStudentExt extends BaseModel<ExampleStudentExt> {
    @Property(displayName = "学分")
    private long score;

}
