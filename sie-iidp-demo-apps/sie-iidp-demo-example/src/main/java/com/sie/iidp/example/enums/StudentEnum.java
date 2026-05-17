package com.sie.iidp.example.enums;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

/**
 * 学生枚举类
 *
 * @author cxh
 * @date 2024年04月07日 16:07
 */
@Getter
@NoArgsConstructor
@AllArgsConstructor
public enum StudentEnum {

    CREATE_STUDENT_ERROR("create.student.error", "创建学生信息发生未知错误，请联系运维人员!"),
    STU_CODE_EXIST("stu.code.exist", "学号【%s】已存在"),
    SYNC_READER_ERROR("sync.reader.error", "同步到读者管理发生异常"),

    ;

    private String code;

    private String msg;

}
