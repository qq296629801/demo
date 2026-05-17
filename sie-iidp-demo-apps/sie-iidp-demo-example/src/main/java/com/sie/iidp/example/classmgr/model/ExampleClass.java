package com.sie.iidp.example.classmgr.model;

import com.sie.iidp.example.studentmgr.model.ExampleStudent;
import com.sie.meta.plugin.Getter;
import com.sie.meta.plugin.Setter;
import com.sie.meta.plugin.StaticVar;
import com.sie.snest.engine.model.Bool;
import com.sie.snest.engine.rule.Filter;
import com.sie.snest.sdk.BaseModel;
import com.sie.snest.sdk.annotation.meta.MethodService;
import com.sie.snest.sdk.annotation.meta.Model;
import com.sie.snest.sdk.annotation.meta.Property;
import com.sie.snest.sdk.annotation.orm.OneToMany;
import com.sie.snest.sdk.annotation.orm.Shard;
import com.sie.snest.sdk.annotation.validate.Validate;
import com.sie.snest.sdk.db.DbUtils;

import java.util.ArrayList;
import java.util.List;

/**
 * Example-班级
 *
 * @author cxh
 * @date 2024年03月27日 13:20
 */
@StaticVar
@Getter
@Setter
@Model(tableName = "example_class", name = "example_class", description = "Example班级", displayName = "Example班级",
        isLogicDelete = Bool.True, isAutoLog = Bool.True,isShard = Bool.False,
        shard = @Shard(shardType = "partition", shardPoint = "class_name:LIST",
                shardValues = "one;two;three"))
public class ExampleClass extends BaseModel<ExampleClass> {

    @Validate.Size(max = 10)
    @Validate.NotBlank(message = "班级编码不能为空")
    @Property(displayName = "班级编码")
    private String classCode;

    @Validate.Size(max = 10)
    @Validate.NotBlank(message = "班级不能为空")
    @Property(displayName = "班级名称")
    private String className;

    @OneToMany
    private List<ExampleStudent> studentList;

    /**
     * 字段常量
     */
    public static final String ID = "id";
    public static final String STUDENT_ID = "studentId";

    /**
     * 提供班级远程查询RPC
     *
     * @param studentId
     * @return
     */
    @MethodService(description = "提供班级远程查询RPC")
    public List<ExampleClass> searchClass(String studentId) {
        List<String> properties = new ArrayList<>();
        properties.add(ID);
        properties.add(ExampleClass.F_CLASS_NAME);
        Filter filter = new Filter();
        filter.and(Filter.equal(STUDENT_ID, studentId));
        return DbUtils.search(filter, properties, 0, 0, ID, ExampleClass.class);
    }

}
