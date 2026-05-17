package com.sie.iidp.example.studentprofile.model;

import com.sie.iidp.example.consts.MethodConst;
import com.sie.iidp.example.consts.ModelConst;
import com.sie.iidp.example.studentmgr.model.ExampleStudent;
import com.sie.iidp.example.studentprofile.dto.ClassViewDTO;
import com.sie.iidp.example.studentprofile.dto.StudentPageViewDTO;
import com.sie.meta.plugin.Getter;
import com.sie.meta.plugin.Setter;
import com.sie.meta.plugin.StaticVar;
import com.sie.snest.engine.context.BaseContextHandler;
import com.sie.snest.engine.data.RecordSet;
import com.sie.snest.engine.exception.ModelException;
import com.sie.snest.engine.exception.ValidationException;
import com.sie.snest.engine.model.Bool;
import com.sie.snest.sdk.BaseModel;
import com.sie.snest.sdk.annotation.meta.MethodService;
import com.sie.snest.sdk.annotation.meta.Model;
import com.sie.snest.sdk.annotation.meta.Property;
import com.sie.snest.sdk.annotation.orm.Shard;
import com.sie.snest.sdk.annotation.validate.Validate;
import com.sie.snest.sdk.db.DbUtils;
import lombok.extern.slf4j.Slf4j;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * Example-学生档案
 *
 * @author zhoubin
 * @date 2024年03月27日 13:20
 */
@StaticVar
@Getter
@Setter
@Slf4j
@Model(tableName = "example_student_profile", name = "example_student_profile", description = "Demo学生档案", displayName = "Demo学生档案", isLogicDelete = Bool.True, isAutoLog = Bool.True,
        isShard = Bool.False, shard = @Shard(shardType = "partition", shardPoint = "create_at:YEAR",
        shardValues = "2021"))
public class ExampleStudentProfile extends BaseModel<ExampleStudentProfile> {
    @Validate.Size(max = 10)
    @Validate.NotBlank(message = "学生id不能为空")
    @Property(displayName = "学生id")
    private String studentId;

    @Validate.Size(max = 10)
    @Validate.NotBlank(message = "学生姓名不能为空")
    @Property(displayName = "学生姓名")
    private String studentName;

    @Validate.Size(max = 200)
    @Property(displayName = "档案内容", length = 200)
    private String content;

    private String classId;

    /**
     * 方法名常量
     */
    public static final String METHOD_SAVE_DEMO_FILE_BY_IDS = "saveDemoFileByIds";
    public static final String METHOD_SAVE_STUDENT_BY_ID = "saveStudentById";
    public static final String METHOD_SEARCH_STUDENT = "searchStudent";
    public static final String METHOD_SEARCH_CLASS = "searchClass";

    /**
     * 字段常量
     */
    public static final String FILED_ID = "id";
    public static final String FILED_STUDENT_ID = "studentId";


    /**
     * RPC将学生ID同步到档案表
     *
     * @param stuList
     * @return
     */
    @MethodService(description = "RPC将学生ID同步到档案表")
    public Boolean saveDemoFileByIds(List<ExampleStudent> stuList) {
        List<ExampleStudentProfile> list = new ArrayList<>();
        for (ExampleStudent stu : stuList) {
            ExampleStudentProfile profile = new ExampleStudentProfile();
            profile.setStudentId(stu.getId());
            profile.setStudentName(stu.getName());
            profile.setContent("学生同步");
            list.add(profile);
        }
        DbUtils.batchCreate(list);
        return true;
    }

    /**
     * 重写新增方法，用于rpc调用
     *
     * @param valuesList
     * @return
     */
    @MethodService(description = "create")
    public Object create(List<Map<String, Object>> valuesList) {
        RecordSet modelRs = BaseContextHandler.getMeta().get(this.getMeta().getModelName());
        try {
            //调用rpc
            RecordSet processInstance = getMeta().get(ModelConst.EXAMPLE_STUDENT);
            processInstance.call(METHOD_SAVE_DEMO_FILE_BY_IDS, modelRs.getId());
            return modelRs.callSuper(ExampleStudentProfile.class, MethodConst.CREATE, valuesList);
        } catch (ModelException e) {
            throw e;
        } catch (Exception e) {
            throw new ValidationException("创建档案发生未知错误，请联系运维人员!");
        }
    }

    /**
     * 重写更新方法，用于rpc调用
     *
     * @param rs
     * @param values
     * @return
     */
    @MethodService(description = "update")
    public Object update(RecordSet rs, Map<String, Object> values) {
        RecordSet modelRs = BaseContextHandler.getMeta().get(this.getMeta().getModelName());
        try {
            //调用rpc
            RecordSet processInstance = getMeta().get(ModelConst.EXAMPLE_STUDENT);
            String studentId = values.get(FILED_STUDENT_ID).toString();
            processInstance.call(METHOD_SAVE_STUDENT_BY_ID, studentId);
            return modelRs.callSuper(ExampleStudentProfile.class, MethodConst.UPDATE, rs, values);
        } catch (ModelException e) {
            throw e;
        } catch (Exception e) {
            throw new ValidationException("更新档案发生未知错误，请联系运维人员!");
        }
    }

    /**
     * 调用学生查询远程RPC
     *
     * @return
     */
    @MethodService(description = "调用学生查询远程RPC")
    public List<StudentPageViewDTO> callStudentListRpc() {
        //获取远程接口模型
        RecordSet processInstance = getMeta().get(ModelConst.EXAMPLE_STUDENT);
        //调用rpc接口
        return (List<StudentPageViewDTO>) processInstance.call(METHOD_SEARCH_STUDENT, 0, 0);
    }

    /**
     * 调用班级查询远程RPC
     *
     * @param studentId
     * @return
     */
    @MethodService(description = "调用班级查询远程RPC")
    public List<ClassViewDTO> callClassListRpc(String studentId) {
        //获取远程接口模型
        RecordSet processInstance = getMeta().get(ModelConst.EXAMPLE_CLASS);
        //调用rpc接口
        return (List<ClassViewDTO>) processInstance.call(METHOD_SEARCH_CLASS, studentId);
    }
}
