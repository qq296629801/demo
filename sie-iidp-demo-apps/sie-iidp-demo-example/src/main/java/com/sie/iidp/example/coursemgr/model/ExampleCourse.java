package com.sie.iidp.example.coursemgr.model;

import com.sie.iidp.example.studentmgr.model.ExampleStudent;
import com.sie.meta.plugin.Getter;
import com.sie.meta.plugin.Setter;
import com.sie.meta.plugin.StaticVar;
import com.sie.snest.engine.model.Bool;
import com.sie.snest.sdk.BaseModel;
import com.sie.snest.sdk.annotation.meta.Model;
import com.sie.snest.sdk.annotation.meta.Property;
import com.sie.snest.sdk.annotation.orm.JoinColumn;
import com.sie.snest.sdk.annotation.orm.JoinTable;
import com.sie.snest.sdk.annotation.orm.ManyToMany;
import com.sie.snest.sdk.annotation.orm.Selection;
import com.sie.snest.sdk.annotation.validate.Validate;

import java.util.List;

/**
 * Example-课程
 *
 * @author cxh
 * @date 2024年03月27日 15:20
 */
@StaticVar
@Getter
@Setter
@Model(tableName = "example_course", name = "example_course", description = "Example课程", displayName = "Example课程",
        isLogicDelete = Bool.True, isAutoLog = Bool.True)
public class ExampleCourse extends BaseModel<ExampleCourse> {

    @Validate.Size(max = 10)
    @Validate.NotBlank(message = "课程名称不能为空")
    @Property(displayName = "课程名称", columnName = "course_name")
    private String courseName;


    @ManyToMany(targetModel = "example_student")
    @JoinTable(name = "example_student_course",
            joinColumns = @JoinColumn(name = "course_id", nullable = false),
            inverseJoinColumns = @JoinColumn(name = "student_id", nullable = false))
    @Selection(multiple = true, properties = {"name"})
    @Property(displayName = "学生")
    private List<ExampleStudent> studentList;
}
