package com.sie.iidp.example.studentscore.model;

import com.sie.snest.engine.model.Bool;
import com.sie.snest.sdk.BaseModel;
import com.sie.snest.sdk.annotation.meta.Model;
import com.sie.snest.sdk.annotation.meta.Property;

/**
 * Example-学生扩展
 *
 * @author cxh
 * @date 2024年04月25日 15:10
 */
@Model(tableName = "example_student", name = "example_student", description = "Example学生", displayName = "Example学生",
        isLogicDelete = Bool.True, isAutoLog = Bool.True, parent = "example_student,example_personal")
public class ExampleStudentExt extends BaseModel<ExampleStudentExt> {

    @Property(displayName = "学分")
    private long score;

}
