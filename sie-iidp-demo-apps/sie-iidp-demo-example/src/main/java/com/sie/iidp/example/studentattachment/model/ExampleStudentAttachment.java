package com.sie.iidp.example.studentattachment.model;

import cn.hutool.core.util.StrUtil;
import com.sie.iidp.common.util.errors.Exceptions;
import com.sie.iidp.example.studentmgr.model.ExampleStudent;
import com.sie.meta.plugin.Getter;
import com.sie.meta.plugin.Setter;
import com.sie.meta.plugin.StaticVar;
import com.sie.snest.engine.container.Meta;
import com.sie.snest.engine.data.RecordSet;
import com.sie.snest.engine.exception.ValidationException;
import com.sie.snest.engine.model.Bool;
import com.sie.snest.engine.model.ModelMeta;
import com.sie.snest.engine.rule.Filter;
import com.sie.snest.sdk.BaseModel;
import com.sie.snest.sdk.CascadeType;
import com.sie.snest.sdk.annotation.meta.MethodService;
import com.sie.snest.sdk.annotation.meta.Model;
import com.sie.snest.sdk.annotation.orm.JoinColumn;
import com.sie.snest.sdk.annotation.orm.ManyToOne;
import org.apache.commons.collections.CollectionUtils;

import java.util.*;

/**
 * Example-学生附件
 *
 * @author cxh
 * @date 2024年03月27日 13:20
 */
@StaticVar
@Getter
@Setter
@Model(tableName = "attachment_data", name = "example_student_attachment",
        displayName = "学生附件", description = "学生附件", isAutoLog = Bool.True, parent = {"example_base_attachment"})
public class ExampleStudentAttachment extends BaseModel<ExampleStudentAttachment> {
    @ManyToOne(displayName = "学生id", cascade = CascadeType.DELETE)
    @JoinColumn(name = "parent_id", referencedProperty = "id")
    private ExampleStudent student;

    @MethodService(description = "上传")
    public boolean fileUpload(RecordSet rs, String fileId, String studentId) {
        Meta meta = rs.getMeta();

        RecordSet rs1 = meta.get(meta.getModelName());
        if (Objects.isNull(rs1)) {
            throw new ValidationException(String.format("文件导入异常，RecordSet %s 空", meta.getModelName()));
        }

        ModelMeta modelMeta = rs1.getModel();
        if (Objects.isNull(modelMeta)) {
            throw new ValidationException(String.format("文件导入异常，modelMeta %s 空", meta.getModelName()));
        }

        Filter filter = new Filter();
        if (StrUtil.isNotBlank(fileId)) {
            filter.and(Filter.equal("key", fileId));
        } else {
            throw Exceptions.IMPORT_FILE_ID_NULL_EXCEPTION;
        }

        RecordSet metaAttachmentRs = meta.get("meta_attachment");
        Object resultObj = metaAttachmentRs.call("search",
                filter, Collections.singletonList("*"), 0, 0, null);

        if (Optional.ofNullable(resultObj).isPresent()) {
            List<Map<String, String>> listMaps = (List<Map<String, String>>) resultObj;
            if (CollectionUtils.isNotEmpty(listMaps)) {
                Map<String, String> attachmentMap = listMaps.get(0);
                ExampleStudentAttachment examProductAttachment = new ExampleStudentAttachment();
                examProductAttachment.set("fileName", attachmentMap.get("name"));
                examProductAttachment.set("fileExtName", attachmentMap.get("content_type"));
                examProductAttachment.set("key", fileId);
                examProductAttachment.set("fileId", attachmentMap.get("id"));
                examProductAttachment.set("fileSize", attachmentMap.get("size"));
                examProductAttachment.set("modelId", "example_student_attachment");
                examProductAttachment.set("student", studentId);
                examProductAttachment.create();
            }
        }
        return true;
    }

    @Override
    public List<ExampleStudentAttachment> search(Filter filter, List<String> properties, Integer limit, Integer offset, String order) {
        filter.and(Filter.equal("modelId","example_student_attachment"));
        List<ExampleStudentAttachment> dataList = super.search(filter, properties, limit, offset, order);
        return dataList;
    }
}
