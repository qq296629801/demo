package com.sie.iidp.example.studentmgr.model;

import cn.hutool.core.collection.CollUtil;
import cn.hutool.core.date.DateUtil;
import cn.hutool.core.util.ObjectUtil;
import cn.hutool.core.util.StrUtil;
import com.sie.iidp.common.util.CodeGenTempUtil;
import com.sie.iidp.example.classmgr.model.ExampleClass;
import com.sie.iidp.example.consts.*;
import com.sie.iidp.example.coursemgr.model.ExampleCourse;
import com.sie.iidp.example.enums.StudentEnum;
import com.sie.iidp.example.studentattachment.model.ExampleStudentAttachment;
import com.sie.iidp.example.studentmgr.excel.ExcelExampleStudentImportInfo;
import com.sie.iidp.example.studentmgr.excel.IExcelExampleStudent;
import com.sie.iidp.example.studentprofile.model.ExampleStudentProfile;
import com.sie.iidp.example.utils.ConvertUtil;
import com.sie.iidp.example.utils.database.DataBaseUtil;
import com.sie.iidp.example.utils.database.SelectUtil;
import com.sie.iidp.example.utils.database.UpdateUtil;
import com.sie.iidp.example.utils.dto.ExampleBaseOptions;
import com.sie.meta.plugin.Getter;
import com.sie.meta.plugin.Setter;
import com.sie.meta.plugin.StaticVar;
import com.sie.snest.engine.constant.MetaConstant;
import com.sie.snest.engine.container.Meta;
import com.sie.snest.engine.context.BaseContextHandler;
import com.sie.snest.engine.data.RecordSet;
import com.sie.snest.engine.data.access.BussModelDataAccess;
import com.sie.snest.engine.data.access.ModelDataAccessFactory;
import com.sie.snest.engine.data.access.ModelTypeEnum;
import com.sie.snest.engine.data.service.JsonUtil;
import com.sie.snest.engine.db.relationdb.RelationDBAccessor;
import com.sie.snest.engine.db.relationdb.provider.SqlProvider;
import com.sie.snest.engine.exception.ModelException;
import com.sie.snest.engine.exception.ValidationException;
import com.sie.snest.engine.model.Bool;
import com.sie.snest.engine.model.ModelMeta;
import com.sie.snest.engine.rule.Filter;
import com.sie.snest.engine.utils.ConfigUtils;
import com.sie.snest.sdk.BaseModel;
import com.sie.snest.sdk.CascadeType;
import com.sie.snest.sdk.DataType;
import com.sie.snest.sdk.annotation.Dict;
import com.sie.snest.sdk.annotation.meta.MethodService;
import com.sie.snest.sdk.annotation.meta.Model;
import com.sie.snest.sdk.annotation.meta.Property;
import com.sie.snest.sdk.annotation.orm.*;
import com.sie.snest.sdk.annotation.validate.Validate;
import com.sie.snest.sdk.cache.RedisHelper;
import com.sie.snest.sdk.db.DbUtils;
import lombok.extern.slf4j.Slf4j;
import org.springframework.util.CollectionUtils;

import java.net.URLDecoder;
import java.nio.charset.StandardCharsets;
import java.util.*;

/**
 * Example-学生
 *
 * @author zhoubin
 * @date 2024年03月27日 13:20
 */
@StaticVar
@Getter
@Setter
@Slf4j
@Model(tableName = "example_student", name = "example_student", description = "Example学生", displayName = "Example学生",
        isLogicDelete = Bool.True, isAutoLog = Bool.True, isPrint = Bool.True,
        isShard = Bool.False, shard = @Shard(shardType = "tables", shardPoint = "create_date:YEAR",
        shardValues = "2021"),
        indexes = {@Index(name = "IDX_CLASS_LEVEL", columnList = {"class_id", "student_level"})})
public class ExampleStudent extends BaseModel<ExampleStudent> implements IExcelExampleStudent {
    @Property(displayName = "学号", columnName = "stu_code", length = 30)
    private String stuCode;

    @Validate.Size(max = 30)
    @Validate.NotBlank(message = "姓名不能为空")
    @Property(displayName = "姓名", columnName = "name", length = 30)
    private String name;

    @Property(displayName = "性别", columnName = "sex", length = 1)
    @Selection(values = {
            @Option(label = "男", value = "1"),
            @Option(label = "女", value = "0")
    })
    @Validate.NotBlank(message = "性别不能为空")
    private String sex;

    @Validate.Size(max = 30)
    @Validate.NotBlank(message = "班级不能为空")
    @Selection(model = "example_class", properties = {"id", "className"})
    @Property(displayName = "班级")
    private String classId;

    @ManyToOne(displayName = "班级", cascade = CascadeType.DEL_SET_NULL)
    @JoinColumn(name = "class_id", referencedProperty = "id")
    private ExampleClass exampleClass;

    @Property(displayName = "班级编码", related = "exampleClass.classCode")
    @Selection(method = "selectClass")
    private String classCode;

    @Property(displayName = "班级名称", related = "exampleClass.className")
    private String className;

    @Property(columnName = "birth_day", displayName = "生日", dataType = DataType.DATE, dateFormat = "yyyy-MM-dd")
    @Validate.NotBlank(message = "生日不能为空")
    private Date birthDay;

    @Property(displayName = "启用状态", length = 10)
    @Validate.NotBlank(message = "启用状态不能为空")
    @Selection(values = {
            @Option(label = "启用", value = "1"),
            @Option(label = "禁用", value = "0")
    })
    private String isEnable;

    @Dict(typeCode = "studentLevel")
    @Property(displayName = "学生等级")
    @Validate.NotBlank(message = "学生等级不能为空")
    private Integer studentLevel;

    @Property(displayName = "是否4级学生", store = false, computeMethod = "isFourLevel")
    private Boolean isFourLevel;

    @Dict(typeCode = "yes_no")
    @Property(displayName = "是否完成档案", defaultValue = "0")
    private String isHaveFile;

    @OneToMany
    private List<ExampleStudentAttachment> attachmentList;

    @ManyToMany(targetModel = "example_course")
    @JoinTable(name = "example_student_course",
            joinColumns = @JoinColumn(name = "student_id", nullable = false),
            inverseJoinColumns = @JoinColumn(name = "course_id", nullable = false))
    @Selection(multiple = true, properties = {"courseName"})
    @Property(displayName = "课程名称")
    private List<ExampleCourse> courseList;

    /**
     * 省+市+区+...
     */
    @Property(displayName = "所在地区", length = 100)
    private String area;

    @Property(displayName = "所在地区编码", length = 100)
    private String areaCode;

    @Property(displayName = "详细地址", length = 80)
    private String dtlAddress;

    private static final Map<String, String> sexMap = new HashMap<>();

    static {
        sexMap.put(SexConst.WOMAN, "女");
        sexMap.put(SexConst.MAN, "男");
    }


    public static final String FILED_ID = "id";
    public static final String FILED_CREATE_USER = "create_user";
    public static final String FILED_CREATE_DATE = "create_date";
    public static final String FILED_UPDATE_USER = "update_user";
    public static final String FILED_UPDATE_DATE = "update_date";

    /**
     * 列名常量
     */
    public static final String COLUMN_IS_HAVE_FILE = "is_have_file";
    public static final String COLUMN_STUDENT_LEVEL = "student_level";
    public static final String COLUMN_STU_CODE = "stu_code";

    /**
     * 方法名常量
     */
    public static final String METHOD_SAVE_DEMO_FILE_BY_IDS = "saveDemoFileByIds";
    public static final String METHOD_GET_STRING = "getString";
    public static final String METHOD_SET_STRING = "setString";
    public static final String METHOD_SAVE_READER_BY_STU = "saveReaderByStu";

    /**
     * 学生等级
     */
    public static final Integer ONE_LEVEL = 1;
    public static final Integer TWO_LEVEL = 2;

    /**
     * 其他常量
     */
    public static final String LABEL = "label";
    public static final String FOUR_LEVEL = "四级";
    public static final String STUDENT_IMPORT = "学生导入";
    public static final String STU_TYPE = "0";
    public static final String SUM_STUDENT_LEVEL = "sumStudentLevel";

    /**
     * 直接执行SQL
     */
    @MethodService(name = "fun1")
    public void fun1() {
        //获取数据库连接
        BussModelDataAccess bussModelDataAccess = (BussModelDataAccess) ModelDataAccessFactory.getDataAccess(ModelTypeEnum.Buss);
        RelationDBAccessor dataAccessor = bussModelDataAccess.getRelationDBAccessor();
        //执行sql
        dataAccessor.execute("SELECT * from example_student ");
        //获取执行结果
        List<Map<String, Object>> appMapList = dataAccessor.fetchMapAll();
        for (Map<String, Object> stringObjectMap : appMapList) {
            System.out.println(stringObjectMap.toString());
        }
    }

    /**
     * 多条件传参
     */
    @MethodService(name = "fun2")
    public void fun2() {
        //获取数据库连接
        BussModelDataAccess bussModelDataAccess = (BussModelDataAccess) ModelDataAccessFactory.getDataAccess(ModelTypeEnum.Buss);
        RelationDBAccessor dataAccessor = bussModelDataAccess.getRelationDBAccessor();
        //执行sql
        dataAccessor.execute("SELECT * from example_student WHERE name=%s and stuCode=%s", Arrays.asList("zs", "fs"));
        //获取执行结果
        List<Map<String, Object>> appMapList = dataAccessor.fetchMapAll();
        for (Map<String, Object> stringObjectMap : appMapList) {
            System.out.println(stringObjectMap.toString());
        }
    }

    /**
     * in查询传参
     */
    @MethodService(name = "fun3")
    public void fun3() {
        //获取数据库连接
        BussModelDataAccess bussModelDataAccess = (BussModelDataAccess) ModelDataAccessFactory.getDataAccess(ModelTypeEnum.Buss);
        RelationDBAccessor dataAccessor = bussModelDataAccess.getRelationDBAccessor();
        //执行sql
        dataAccessor.execute("SELECT * from example_student WHERE name=%s and stuCode=%s", Arrays.asList("zs", "fs"));
        dataAccessor.execute("SELECT * from example_student WHERE name in %s ",
                Arrays.asList(Arrays.asList("zs", "ls")));
        //获取执行结果
        List<Map<String, Object>> appMapList = dataAccessor.fetchMapAll();
        for (Map<String, Object> stringObjectMap : appMapList) {
            System.out.println(stringObjectMap.toString());
        }
    }

    @MethodService(description = "聚集函数示例")
    public List<Map<String, Object>> function(RecordSet rs) {
        //如果需要having变量，这样设置变量，不需要则不用
        //rs.getMeta().getArguments().put("studentLevel",1);
        //调用example_student_ref的search
        List<Map<String, Object>> list = rs.getMeta().get(ExampleStudentRef.MODEL_NAME).search(new Filter(),
                Arrays.asList(ExampleStudent.F_STUDENT_LEVEL, ExampleStudent.F_STU_CODE, SUM_STUDENT_LEVEL), 100, 0, null);
        return list;
    }


    /**
     * 启用禁用
     *
     * @param rs
     * @param statusFlag
     * @return
     */
    @MethodService(description = "启用禁用")
    public boolean enableDisable(RecordSet rs, String statusFlag) {
        String[] ids = rs.getIds();
        RecordSet recordSet = (RecordSet) rs.callSuper(null, MethodConst.FIND, Filter.in(FILED_ID, Arrays.asList(ids)), null, null, null);
        if (recordSet.any()) {
            Map<String, Object> valMap = new HashMap<>();
            valMap.put(ExampleStudent.F_IS_ENABLE, statusFlag);
            recordSet.callSuper(null, MethodConst.UPDATE, valMap);
        }
        return true;
    }

    /**
     * 获取 班级信息
     *
     * @return
     */
    @MethodService(description = "selectClass")
    public List<ExampleBaseOptions> selectClass() {
        RecordSet classRs = getMeta().get(ExampleClass.MODEL_NAME);
        List<Map<String, Object>> searchList = classRs.search(new Filter(), Collections.singletonList("*"), 0, 0, "");
        List<ExampleBaseOptions> list = new ArrayList<>();
        searchList.forEach(item -> {
            ExampleBaseOptions ops = new ExampleBaseOptions();
            ops.setId(item.get(FILED_ID).toString());
            ops.setCode(item.get(ExampleClass.F_CLASS_CODE).toString());
            ops.setName(item.get(ExampleClass.F_CLASS_NAME).toString());
            ops.setlabel(item.get(ExampleClass.F_CLASS_CODE).toString());
            ops.setValue(item.get(ExampleClass.F_CLASS_CODE).toString());
            list.add(ops);
        });
        return list;
    }

    /**
     * 设置学生等级
     *
     * @param rs
     * @param flag
     * @return
     */
    @MethodService(description = "设置学生等级")
    public boolean updateStudentLevel(RecordSet rs, String flag) {
        String[] ids = rs.getIds();
        RecordSet results = (RecordSet) rs.callSuper(null, MethodConst.FIND, Filter.in(FILED_ID, Arrays.asList(ids)), null, null, null);
        if (results.any()) {
            Map<String, Object> valMap = new HashMap<>();
            valMap.put(ExampleStudent.F_STUDENT_LEVEL, flag);
            results.callSuper(null, MethodConst.UPDATE, valMap);
        }
        return true;
    }

    /**
     * 是否为四级学生
     *
     * @param valMap
     * @return
     * @throws IllegalAccessException
     */
    @MethodService(description = "是否为四级学生")
    public Boolean isFourLevel(Map<String, Object> valMap) throws IllegalAccessException {
        boolean isFourLevel = false;
        Object object = valMap.get(ExampleStudent.F_STUDENT_LEVEL);
        if (ObjectUtil.isNotNull(object)) {
            // 对象转换为 Map
            Map<String, Object> studentLevel = ConvertUtil.convertObjectToMap(object);
            isFourLevel = ObjectUtil.isNotNull(studentLevel.get(LABEL)) && FOUR_LEVEL.equals(studentLevel.get(LABEL));
        }
        return isFourLevel;
    }

    /**
     * 测试单模块事务
     *
     * <p>
     * 逻辑说明：
     * 1、取一条学生信息，将学生等级更新为二级【更新】
     * 2、新增一条学生信息【新增】
     * 3、制造异常，查看数据是否会回滚
     *
     * @param rs
     * @return
     */
    @MethodService(description = "单APP事务自动提交")
    public boolean testTransactional(RecordSet rs) {
        // 获取数据库连接
        BussModelDataAccess bussModelDataAccess = (BussModelDataAccess) ModelDataAccessFactory.getDataAccess(ModelTypeEnum.Buss);
        RelationDBAccessor dataAccessor = bussModelDataAccess.getRelationDBAccessor();
        // 获取当前的数据库连接方式(mysql或oracle或其他)
        SqlProvider sqlProvider = dataAccessor.getSqlProvider();
        // 获取学生信息
        List<ExampleStudent> stuList = getDemoStudents(sqlProvider, dataAccessor);
        if (CollUtil.isNotEmpty(stuList)) {
            // 更新学生
            ExampleStudent student = stuList.get(0);
            student.set(ExampleStudent.F_STUDENT_LEVEL, TWO_LEVEL);
            student.update();
        }
        // 新增学生
        ExampleStudent addStudent = new ExampleStudent();
        addStudent.setName("单事物测试学生");
        addStudent.setIsEnable(EnableConst.ENABLE);
        addStudent.setSex(SexConst.MAN);
        addStudent.setStudentLevel(ONE_LEVEL);
        List<ExampleClass> classList = getDemoClasses(sqlProvider, dataAccessor);
        if (CollUtil.isNotEmpty(classList)) {
            addStudent.setClassId(classList.get(0).getId());
            addStudent.create();
        }
        try {
            int i = 1 / 0; // 制造异常测试事务
        } catch (Exception e) {
            throw new ModelException("抛出异常-单APP事务自动提交");
        }
        return true;
    }

    /**
     * 测试单模块事务单独提交
     * <p>
     * 1、取一条学生信息，将学生等级更新为二级，且手动提交事物【更新】
     * 2、新增一条学生信息，且手动提交事物【新增】
     * 3、制造异常，查看数据是否会回滚
     *
     * @param rs
     * @return
     */
    @MethodService(description = "单APP事务手动提交")
    public boolean testTransactionalSubCommit(RecordSet rs) {
        // 获取数据库连接
        BussModelDataAccess bussModelDataAccess = (BussModelDataAccess) ModelDataAccessFactory.getDataAccess(ModelTypeEnum.Buss);
        RelationDBAccessor dataAccessor = bussModelDataAccess.getRelationDBAccessor();
        // 获取当前的数据库连接方式(mysql或oracle或其他)
        SqlProvider sqlProvider = dataAccessor.getSqlProvider();
        try (Meta meta = new Meta(null, new HashMap<>())) {
            // 更新学生信息，且手动提交事物
            List<ExampleStudent> stuList = getDemoStudents(sqlProvider, dataAccessor);
            if (CollUtil.isNotEmpty(stuList)) {
                ExampleStudent student = stuList.get(0);
                student.set(ExampleStudent.F_STUDENT_LEVEL, TWO_LEVEL);
                student.update();
                meta.flush();
                meta.commit();
            }
            // 创建学生信息，且手动提交事物
            List<ExampleClass> classList = getDemoClasses(sqlProvider, dataAccessor);
            if (CollUtil.isNotEmpty(classList)) {
                ExampleStudent addStudent = new ExampleStudent();
                addStudent.setName("单事物测试学生");
                addStudent.setIsEnable(EnableConst.ENABLE);
                addStudent.setSex(SexConst.MAN);
                addStudent.setStudentLevel(ONE_LEVEL);
                addStudent.setClassId(classList.get(0).getId());
                addStudent.create();
                meta.flush();
                meta.commit();
            }
            try {
                int i = 1 / 0; // 制造异常测试事务
            } catch (Exception e) {
                throw new ModelException("抛出异常-单APP事务手动提交");
            }
        } finally {
            BaseContextHandler.remove();
        }
        return true;
    }

    /**
     * 获取学生信息
     *
     * @param sqlProvider
     * @param dataAccessor
     * @return
     */
    private List<ExampleStudent> getDemoStudents(SqlProvider sqlProvider, RelationDBAccessor dataAccessor) {
        // 构建查询学生信息的SQL
        String sql = getString(sqlProvider);
        log.info("sql == {}", sql);
        // 执行SQL
        dataAccessor.execute(sql);
        // 获取执行结果
        List<Map<String, Object>> appMapList = dataAccessor.fetchMapAll();
        log.info("appMapList == {}", JsonUtil.toString(appMapList));
        // 将查询结果封装为学生对象集合并返回
        List<ExampleStudent> stuList = new ArrayList<>();
        for (Map<String, Object> map : appMapList) {
            log.info("map == {}", map);
            Object studentLevel = map.get(COLUMN_STUDENT_LEVEL);
            log.info("studentLevel == {}", String.valueOf(studentLevel));
            ExampleStudent stu = new ExampleStudent();
            if (ObjectUtil.isNotNull(studentLevel)) {
                stu.setStudentLevel(Integer.parseInt(studentLevel.toString()));
            }
            stu.setId(map.get(FILED_ID).toString());
            stuList.add(stu);
        }
        return stuList;
    }

    /**
     * 构建查询学生信息的SQL
     *
     * @param sqlProvider
     * @return
     */
    private String getString(SqlProvider sqlProvider) {
        return "select id,student_level from " + sqlProvider.quote(ExampleStudent.MODEL_NAME) +
                " where " + sqlProvider.quote("delete_flag")
                + "= '0' order by " + sqlProvider.quote("create_date") + " desc";
    }

    /**
     * 获取课程信息
     *
     * @param sqlProvider
     * @param dataAccessor
     * @return
     */
    private List<ExampleClass> getDemoClasses(SqlProvider sqlProvider, RelationDBAccessor dataAccessor) {
        // 构建查询课程信息的SQL
        String classSql = getClassSql(sqlProvider);
        // 执行SQL
        dataAccessor.execute(classSql);
        // 获取执行结果
        List<Map<String, Object>> mapList = dataAccessor.fetchMapAll();
        // 将查询结果封装为课程对象集合并返回
        List<ExampleClass> classList = new ArrayList<>();
        for (Map<String, Object> map : mapList) {
            ExampleClass demoClass = new ExampleClass();
            demoClass.setId(map.get(FILED_ID).toString());
            classList.add(demoClass);
        }
        return classList;
    }

    /**
     * 构建查询课程信息的SQL
     *
     * @param sqlProvider
     * @return
     */
    private String getClassSql(SqlProvider sqlProvider) {
        return "select id,class_name from " + sqlProvider.quote(ExampleClass.MODEL_NAME) +
                " where " + sqlProvider.quote("delete_flag") + " = '0' order by " + sqlProvider.quote("create_date") + " desc";
    }

    /**
     * 同步学生信息到档案表(调用远程RPC)
     *
     * @param rs
     * @return
     */
    @MethodService(description = "调用远程RPC")
    public Boolean callRpc(RecordSet rs) {
        // 获取学生档案远程接口模型
        RecordSet processInstance = getMeta().get(ExampleStudentProfile.MODEL_NAME);
        String[] idsArr = rs.getIds();
        List<String> ids = Arrays.asList(idsArr);
        // 根据ids查询学生信息
        String sql = new SelectUtil(ExampleStudent.class).in(FILED_ID, ids).getSql();
        List<ExampleStudent> stuList = DataBaseUtil.executeSelectSQL(sql, ExampleStudent.class);
        // 调用学生档案模型中的保存档案方法
        processInstance.call(ExampleStudentProfile.M_SAVE_DEMO_FILE_BY_IDS, stuList);
        // 更新学生是否完成档案为是
        String updateSql = new UpdateUtil(ExampleStudent.class).in(FILED_ID, ids).set("is_have_file", Boolean.TRUE).getSql();
        DataBaseUtil.executeUpdateSql(updateSql);
        return true;
    }

    /**
     * 同步学生信息读者管理(跨APP调用)
     *
     * @param rs
     * @return
     */
    @MethodService(description = "同步学生信息读者管理")
    public Boolean syncReader(RecordSet rs) throws Exception {
        // 获取读者管理接口模型
        RecordSet readerRs = getMeta().get(ModelConst.READER_MANAGEMENT);
        String[] idsArr = rs.getIds();
        List<String> ids = Arrays.asList(idsArr);
        // 根据ids查询学生信息
        String sql = new SelectUtil(ExampleStudent.class).in(FILED_ID, ids).getSql();
        List<ExampleStudent> stuList = DataBaseUtil.executeSelectSQL(sql, ExampleStudent.class);
        // 调用读者管理中的保存学生到读者方法
        try {
            readerRs.call(METHOD_SAVE_READER_BY_STU, stuList);
        } catch (ModelException me) {
            throw me;
        } catch (Exception e) {
            throw new Exception(StudentEnum.SYNC_READER_ERROR.getMsg());
        }
        return true;
    }

    /**
     * 测试多模块事务
     * <p>
     * 1、远程调用学生档案模块中的保存档案方案【远程调用】
     * 2、新增一条默认学生信息【新增】
     * 3、制造异常，查看数据是否会回滚
     *
     * @param rs
     * @return
     */
    @MethodService(description = "多APP事务自动提交")
    public Boolean testTransactionalRpc(RecordSet rs) {
        // 获取数据库连接
        BussModelDataAccess dataAccess = (BussModelDataAccess) ModelDataAccessFactory.getDataAccess(ModelTypeEnum.Buss);
        RelationDBAccessor dataAccessor = dataAccess.getRelationDBAccessor();
        // 获取当前的数据库连接方式(mysql或oracle或其他)
        SqlProvider sqlProvider = dataAccessor.getSqlProvider();
        // 获取学生档案远程接口模型
        RecordSet processInstance = getMeta().get(ExampleStudentProfile.MODEL_NAME);
        String[] ids = rs.getIds();
        List<String> arr = Arrays.asList(ids);
        // 根据学生ID获取学生信息
        String sql = new SelectUtil(ExampleStudent.class).in(FILED_ID, arr).getSql();
        List<ExampleStudent> studentList = DataBaseUtil.executeSelectSQL(sql, ExampleStudent.class);
        // 调用保存学生档案模型
        processInstance.call(ExampleStudentProfile.M_SAVE_DEMO_FILE_BY_IDS, studentList);
        // 新增学生
        ExampleStudent addStudent = new ExampleStudent();
        addStudent.setName("测试多模块事务学生");
        addStudent.setIsEnable(EnableConst.ENABLE);
        addStudent.setSex(SexConst.MAN);
        addStudent.setStudentLevel(ONE_LEVEL);
        List<ExampleClass> demoClassList = getDemoClasses(sqlProvider, dataAccessor);
        if (CollUtil.isNotEmpty(demoClassList)) {
            addStudent.setClassId(demoClassList.get(0).getId());
            addStudent.create();
        }
        try {
            int i = 1 / 0; // 制造异常测试事务
        } catch (Exception e) {
            throw new ModelException("抛出异常-多APP事务自动提交");
        }
        return true;
    }

    /**
     * 测试多模块事务手动提交
     * <p>
     * 1、远程调用学生档案模块中的保存档案方案且手动提交事物【远程调用】
     * 2、新增一条默认学生信息且手动提交事物【新增】
     * 3、制造异常，查看数据是否会回滚
     *
     * @param rs
     * @return
     */
    @MethodService(description = "多APP事务手动提交")
    public Boolean testTransactionalSubRpc(RecordSet rs) {
        // 获取数据库连接
        BussModelDataAccess bussModelDataAccess = (BussModelDataAccess) ModelDataAccessFactory.getDataAccess(ModelTypeEnum.Buss);
        RelationDBAccessor dataAccessor = bussModelDataAccess.getRelationDBAccessor();
        SqlProvider sqlProvider = dataAccessor.getSqlProvider();
        // 获取学生档案模型
        RecordSet processInstance = getMeta().get(ExampleStudentProfile.MODEL_NAME);
        String[] ids = rs.getIds();
        List<String> arr = Arrays.asList(ids);
        // 根据学生ID获取学生信息
        String sql = new SelectUtil(ExampleStudent.class).in(FILED_ID, arr).getSql();
        List<ExampleStudent> studentList = DataBaseUtil.executeSelectSQL(sql, ExampleStudent.class);
        // 调用保存学生档案模型
        processInstance.call(ExampleStudentProfile.M_SAVE_DEMO_FILE_BY_IDS, studentList);
        try (Meta meta = new Meta(null, new HashMap<>())) {
            // 新增学生信息且手动提交事物
            ExampleStudent addStudent = new ExampleStudent();
            addStudent.setName("测试多模块事务手动提交学生");
            addStudent.setIsEnable(EnableConst.ENABLE);
            addStudent.setSex(SexConst.MAN);
            addStudent.setStudentLevel(ONE_LEVEL);
            List<ExampleClass> demoClassList = getDemoClasses(sqlProvider, dataAccessor);
            if (CollUtil.isNotEmpty(demoClassList)) {
                addStudent.setClassId(demoClassList.get(0).getId());
                addStudent.create();
            }
            meta.flush();
            meta.commit();
        } finally {
            BaseContextHandler.remove();
        }
        try {
            int i = 1 / 0; // 制造异常测试事务
        } catch (Exception e) {
            throw new ModelException("抛出异常-多APP事务手动提交");
        }
        return true;
    }


    /**
     * 提供远程保存RPC
     *
     * @param id
     * @return
     */
    @MethodService(description = "提供远程保存RPC")
    public Boolean saveStudentById(String id) {
        String sql = new UpdateUtil(ExampleStudent.class).set(COLUMN_IS_HAVE_FILE, YesOrNo.YES).eq(FILED_ID, id).getSql();
        DataBaseUtil.executeUpdateSql(sql);
        return true;
    }

    /**
     * 提供远程查询RPC
     *
     * @param limit
     * @param offset
     * @return
     */
    @MethodService(description = "提供远程查询RPC")
    public List<ExampleStudent> searchStudent(Integer limit, Integer offset) {
        List<String> properties = new ArrayList<>();
        properties.add(FILED_ID);
        properties.add(ExampleStudent.F_NAME);
        Filter filter = new Filter();
        return DbUtils.search(filter, properties, limit, offset, FILED_ID, ExampleStudent.class);
    }

    /**
     * 自定义SQL测试
     * <p>
     * 1、mysqlProvider.asQuote()        设置别名
     * 2、mysqlProvider.quote()          拼接表名或字段
     * 3、mysqlProvider.quoteValue()     拼接字段值
     *
     * @return
     */
    @MethodService(description = "customSqlStudentDtl")
    public List<Map<String, Object>> customSqlStudentDtl(RecordSet recordSet) {
        String name1 = "张三";
        String name2 = "李四";
        String nameLike = "李";
        String sex = "1";
        // 获取数据库连接
        BussModelDataAccess bussModelDataAccess = (BussModelDataAccess) ModelDataAccessFactory.getDataAccess(ModelTypeEnum.Buss);
        RelationDBAccessor dataAccessor = bussModelDataAccess.getRelationDBAccessor();
        SqlProvider sqlProvider = dataAccessor.getSqlProvider();
        // 查询学生数量
        String sqlOne = selectCount(sqlProvider, name1, dataAccessor);
        // 分页查询返回限制条数
        pageLimitSelect(sqlProvider, sqlOne, dataAccessor);
        // 多条件传参
        multiCondition(name1, sex, sqlProvider, dataAccessor);
        // in查询传参
        inSelect(sqlProvider, dataAccessor, name1, name2);
        // 模糊查询
        return likeSelect(sqlProvider, nameLike, dataAccessor);
    }

    /**
     * 查询学生数量
     *
     * @param sqlProvider
     * @param name1
     * @param dataAccessor
     * @return
     */
    private String selectCount(SqlProvider sqlProvider, String name1, RelationDBAccessor dataAccessor) {
        String sqlOne = "select count(1) " + sqlProvider.asQuote("qty ") + "from " +
                sqlProvider.quote(ExampleStudent.MODEL_NAME) +
                "t where t." + sqlProvider.quote(ExampleStudent.F_NAME) + "=" + sqlProvider.quoteValue(name1);
        log.info("sqlOne 执行的SQL为:{}", sqlOne);
        dataAccessor.execute(sqlOne);
        return sqlOne;
    }

    /**
     * 分页查询返回限制条数
     *
     * @param sqlProvider
     * @param sqlOne
     * @param dataAccessor
     */
    private void pageLimitSelect(SqlProvider sqlProvider, String sqlOne, RelationDBAccessor dataAccessor) {
        String sqlOnePage = sqlProvider.getPaging(sqlOne, 100, 0);
        log.info("sqlOne 执行的分页查询SQL为:{}", sqlOnePage);
        dataAccessor.execute(sqlOnePage);
        List<Map<String, Object>> sqlOneResult = dataAccessor.fetchMapAll();
        log.info("sqlOne 执行的结果为:{}", sqlOneResult);
    }

    /**
     * 模糊查询
     *
     * @param mysqlProvider
     * @param nameLike
     * @param dataAccessor
     * @return
     */
    private List<Map<String, Object>> likeSelect(SqlProvider mysqlProvider, String nameLike, RelationDBAccessor dataAccessor) {
        StringBuilder sqlFour = new StringBuilder();
        sqlFour.append("SELECT %s.* ");
        sqlFour.append("FROM %s AS %s ");
        sqlFour.append("WHERE %s.%s like ");
        log.info("sqlFour is:{}", sqlFour);
        String sqlFourStr = String.format(sqlFour.toString(),
                mysqlProvider.quote("t1"),
                mysqlProvider.quote("example_student"),
                mysqlProvider.quote("t1"),
                mysqlProvider.quote("t1"),
                mysqlProvider.quote("name"));
        sqlFourStr = sqlFourStr + "'%%%s%%'";
        sqlFourStr = String.format(sqlFourStr, nameLike);
        log.info("sqlFour 执行的SQL为:{}", sqlFourStr);
        dataAccessor.execute(sqlFourStr);
        List<Map<String, Object>> sqlFourResult = dataAccessor.fetchMapAll();
        log.info("sqlFour 执行的结果为:{}", sqlFourResult);
        return sqlFourResult;
    }

    /**
     * in查询传参(参数格式为 List<List<String>> ，两层List集合)
     *
     * @param mysqlProvider
     * @param dataAccessor
     * @param name1
     * @param name2
     */
    private void inSelect(SqlProvider mysqlProvider, RelationDBAccessor dataAccessor, String name1, String name2) {
        StringBuilder sqlThree = new StringBuilder();
        sqlThree.append("SELECT %s.* ");
        sqlThree.append("FROM %s AS %s ");
        sqlThree.append("WHERE %s.%s in ");
        String sqlThreeStr = String.format(sqlThree.toString(),
                mysqlProvider.quote("t1"),
                mysqlProvider.quote("example_student"),
                mysqlProvider.quote("t1"),
                mysqlProvider.quote("t1"),
                mysqlProvider.quote("name"));
        sqlThreeStr = sqlThreeStr + "%s";
        log.info("sqlThree 执行的SQL为:{}", sqlThreeStr);
        dataAccessor.execute(sqlThreeStr, Arrays.asList(Arrays.asList(name1, name2)));
        List<Map<String, Object>> sqlThreeResult = dataAccessor.fetchMapAll();
        log.info("sqlThree 执行的结果为:{}", sqlThreeResult);
    }

    /**
     * 多条件传参(学生姓名 和 性别)
     *
     * @param name1
     * @param sex
     * @param mysqlProvider
     * @param dataAccessor
     */
    private void multiCondition(String name1, String sex, SqlProvider mysqlProvider, RelationDBAccessor dataAccessor) {
        StringBuilder sqlTwo = new StringBuilder();
        sqlTwo.append("SELECT %s.* ");
        sqlTwo.append("FROM %s AS %s ");
        sqlTwo.append("WHERE %s.%s = '" + name1 + "' ");
        sqlTwo.append("AND %s.%s = '" + sex + "'");
        String sqlTwoStr = String.format(sqlTwo.toString(),
                mysqlProvider.quote("t1"),
                mysqlProvider.quote("example_student"),
                mysqlProvider.quote("t1"),
                mysqlProvider.quote("t1"),
                mysqlProvider.quote("name"),
                mysqlProvider.quote("t1"),
                mysqlProvider.quote("sex"));
        log.info("sqlTwo 执行的SQL为:{}", sqlTwoStr);
        dataAccessor.execute(sqlTwoStr);
        List<Map<String, Object>> sqlTwoResult = dataAccessor.fetchMapAll();
        log.info("sqlTwo 执行的结果为:{}", sqlTwoResult);
    }

    /**
     * excel导入
     *
     * @param rs
     * @param fileId
     * @return
     * @throws Exception
     */
    @MethodService(description = "excel导入")
    public boolean excelImport(RecordSet rs, String fileId) throws Exception {
        return isHasImportData(rs, fileId);
    }

    /**
     * 导入学生信息
     *
     * @param rs
     * @param fileId
     * @return
     * @throws Exception
     */
    private boolean isHasImportData(RecordSet rs, String fileId) throws Exception {
        ExcelExampleStudentImportInfo<ExampleStudent> studentInfo = new ExcelExampleStudentImportInfo<>(STUDENT_IMPORT, ExampleStudent::new);
        List<ExcelExampleStudentImportInfo<?>> infoList = new ArrayList<>();
        infoList.add(studentInfo);
        Meta meta = rs.getMeta();
        String modelName = meta.getModelName();
        RecordSet recordSet = meta.get(modelName);
        if (ObjectUtil.isNull(recordSet)) {
            throw new Exception(String.format("文件导入异常，RecordSet %s 空", modelName));
        }
        ModelMeta modelMeta = recordSet.getModel();
        if (ObjectUtil.isNull(modelMeta)) {
            throw new Exception(String.format("文件导入异常，modelMeta %s 空", modelName));
        }
        Map<String, List<Map<String, Object>>> fileMap = (Map<String, List<Map<String, Object>>>) rs.getMeta().get(ImportConst.BASE_EXCEL).call(ImportConst.FILE_IMPORT, fileId);
        boolean hasImportData = false;
        for (ExcelExampleStudentImportInfo importInfo : infoList) {
            String sheetName = importInfo.getSheetName();
            List<Map<String, Object>> rawDataList = fileMap.get(sheetName);
            if (!CollectionUtils.isEmpty(rawDataList)) {
                hasImportData = true;
            }
            IExcelExampleStudent demoStudentImport = (IExcelExampleStudent) importInfo.getEntitySupplier().get();
            demoStudentImport.doExcelImport(rawDataList);
        }
        return hasImportData;
    }

    /**
     * 导入
     *
     * @param rawDataList
     * @return
     * @throws Exception
     */
    @Override
    public boolean doExcelImport(List<Map<String, Object>> rawDataList) throws Exception {
        if (CollUtil.isNotEmpty(rawDataList)) {
            BaseContextHandler.getMeta().get(this.getModelName()).create(rawDataList);
        }
        return true;
    }

    /**
     * excel导出
     *
     * @param rs
     * @param filter
     * @param limit
     * @param offset
     * @param order
     * @throws Exception
     */
    @MethodService(description = "excel导出")
    public void excelExport(RecordSet rs, Filter filter, Integer limit, Integer offset, String order) throws Exception {
        getMeta().addArgument(MetaConstant.USE_DISPLAY_FOR_MODEL, true);
        Map<String, List<Map<String, Object>>> exportDataList = new LinkedHashMap<>();
        // 定义要导出的列
        String[] handleProperties = new String[]{ExampleStudent.F_NAME, ExampleStudent.F_SEX, ExampleStudent.F_CLASS_ID, ExampleStudent.F_BIRTH_DAY, ExampleStudent.FILED_CREATE_USER,
                ExampleStudent.FILED_CREATE_DATE, ExampleStudent.FILED_UPDATE_USER, ExampleStudent.FILED_UPDATE_DATE};
        List<ExampleStudent> studentList = this.search(filter, Arrays.asList(handleProperties), limit, offset, order);
        if (CollectionUtils.isEmpty(studentList)) {
            throw new Exception("没有符合数据");
        }
        List<Map<String, Object>> result = new ArrayList<>();
        // 获取要导出的字段数据信息
        for (ExampleStudent student : studentList) {
            LinkedHashMap<String, Object> map = new LinkedHashMap<>();
            map.put("姓名", student.getName());
            map.put("性别", sexMap.get(student.getSex()));
            map.put("生日", student.getBirthDay());
            String classInfo = student.getClassId();
            String className = classInfo.substring(1, classInfo.indexOf(","));
            map.put("班级", className);
            result.add(map);
        }
        // 导出执行
        exportDataList.put("学生导出", result);
        String fileName = URLDecoder.decode("学生-", StandardCharsets.UTF_8.toString()) + DateUtil.format(
                DateUtil.date(), ExportConst.YYYY_MM_DD_HH_MM_SS) + ExportConst.XLSX;
        rs.getMeta().get(ExportConst.BASE_EXCEL).call(ExportConst.FILE_EXPORT, exportDataList, fileName);
    }

    /**
     * 获取配置中心值
     *
     * @return
     */
    @MethodService(description = "获取配置中心值")
    public String getConfigCenterValue() {
        // 通过 appName appTag appConfigKey 获取对应的配置中心值，也可以只通过 appConfigKey 获取，实质上是查的 config_center 配置中心表
        String logPath = ConfigUtils.get("mbm-demo-common", "master", "xxl.job.executor.logpath").get();
        log.info("logPath is:{}", logPath);
        return logPath;
    }

    /**
     * redis 分布式锁测试(上锁)
     *
     * @return
     */
    @MethodService(description = "redis分布式锁测试(上锁)")
    public String redisTestLock(RecordSet recordSet) {
        String msg = "上锁成功";
        String id = recordSet.getId();
        if (StrUtil.isEmpty(id)) {
            throw new ValidationException("请先勾选中一条记录再点击按钮");
        }
        // 通过key获取锁
        String key = "redisTestLock:" + id;
        Boolean isLocked = false;
        try {
            isLocked = RedisHelper.tryLock(key, 2L, 30L);
            if (isLocked) {
                // 成功获取锁，执行业务逻辑
                Thread.sleep(3000L);
                System.out.println("成功获取锁，执行业务逻辑...");
            } else {
                // 获取锁失败
                throw new ValidationException(String.format("该学生信息正在被修改，请%s秒后重试", 3L));
            }
            return msg;
        } catch (Exception e) {
            throw new ValidationException(String.format("该学生信息正在被修改，请%s秒后重试", 3L));
        } finally {
            if (isLocked) {
                RedisHelper.unlock(key);
                System.out.println("锁已释放");
            }
        }
    }

    /**
     * redis 存值取值测试
     *
     * @return
     */
    @MethodService(description = "redis存值取值测试")
    public String redisTestTwo(RecordSet recordSet) {
        String id = recordSet.getId();
        if (StrUtil.isEmpty(id)) {
            throw new ValidationException("请先勾选中一条记录再点击按钮");
        }
        // 根据学生ID获取学生信息
        String sql = new SelectUtil(ExampleStudent.class).eq(FILED_ID, id).getSql();
        ExampleStudent stu = DataBaseUtil.getSelectOne(sql, ExampleStudent.class);
        // 存值
        String key = "redisTestTwo:" + id;
        String value = stu.getName();
        RedisHelper.set(key, value);
        // 取值
        String name = (String) RedisHelper.get(key);
        return String.valueOf(name);
    }

    /**
     * 读取业务配置项生成学号
     *
     * @return
     */
    @MethodService(description = "genStuCode")
    public String genStuCode() {
        return CodeGenTempUtil.genOneCode(STU_TYPE);
    }

    /**
     * 重写新增方法
     *
     * @param valuesList
     * @return
     */
    @MethodService(description = "create")
    public Object create(List<Map<String, Object>> valuesList) {
        RecordSet rs = BaseContextHandler.getMeta().get(this.getMeta().getModelName());
        try {
            // 校验学号是否唯一
            validStuCodeUnique(valuesList);
            return rs.callSuper(ExampleStudent.class, MethodConst.CREATE, valuesList);
        } catch (ModelException e) {
            throw e;
        } catch (ValidationException ve) {
            throw ve;
        } catch (Exception e) {
            log.error("创建学生信息失败", e);
            throw new ValidationException(StudentEnum.CREATE_STUDENT_ERROR.getMsg());
        }
    }

    /**
     * 校验学号是否唯一
     *
     * @param valuesList
     */
    private void validStuCodeUnique(List<Map<String, Object>> valuesList) {
        if (CollUtil.isNotEmpty(valuesList)) {
            Object obj = valuesList.get(0).get(ExampleStudent.F_STU_CODE);
            if (ObjectUtil.isNotNull(obj)) {
                String stuCode = obj.toString();
                log.info("stuCode == {}", stuCode);
                // 根据学号查询学生信息
                String sql = new SelectUtil(ExampleStudent.class).eq(COLUMN_STU_CODE, stuCode).getSql();
                ExampleStudent stu = DataBaseUtil.getSelectOne(sql, ExampleStudent.class);
                if (ObjectUtil.isNotNull(stu)) {
                    throw new ValidationException(String.format(StudentEnum.STU_CODE_EXIST.getMsg(), stuCode));
                }
            }
        }
    }


    @MethodService(description = "手动控制嵌套事务")
    public boolean testNestingTransactional(RecordSet rs) {
        //获取meta
        Meta meta = BaseContextHandler.getMeta();
        // 更新学生信息，且手动提交事物
        List<ExampleStudent> stuList = this.search(new Filter(), Arrays.asList("*"), 0, 0, null);
        if (CollUtil.isNotEmpty(stuList)) {
            ExampleStudent student = stuList.get(0);
            student.set(ExampleStudent.F_STUDENT_LEVEL, TWO_LEVEL);
            student.update();
        }
        //新建一个事务点,oracle事务点名称规则有限制可以使用String point1 = "SP"+IdGenerator.nextId();
        String point1 = UUID.randomUUID().toString();
        // 创建学生信息，且手动提交事物
        List<ExampleClass> classList = (List<ExampleClass>) this.getMeta().get(ExampleClass.MODEL_NAME).call("search", new Filter(), Arrays.asList("*"), 0, 0, null);
        if (CollUtil.isNotEmpty(classList)) {
            ExampleStudent addStudent = new ExampleStudent();
            addStudent.setName("单事物测试学生");
            addStudent.setIsEnable(EnableConst.ENABLE);
            addStudent.setSex(SexConst.MAN);
            addStudent.setStudentLevel(ONE_LEVEL);
            addStudent.setClassId((String) ((LinkedHashMap) classList.get(0)).get("id"));
            try {
                meta.setSavepoint(point1);
                addStudent.create();
                int i = 1 / 0;
                //执行成功，直接commit(会提交整个point及之前的代码事务)
                meta.commit();
            } catch (Exception e) {
                //失败，可以回滚当前事务点的代码，不影响其他代码事务执行
                meta.rollback(point1);
            }
        }
        return true;
    }
}
