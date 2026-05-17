package com.sie.iidp.example.attachment.model;

import cn.hutool.core.util.StrUtil;
import com.sie.iidp.common.util.errors.Exceptions;
import com.sie.iidp.example.studentattachment.model.ExampleStudentAttachment;
import com.sie.snest.engine.container.Meta;
import com.sie.snest.engine.data.RecordSet;
import com.sie.snest.engine.exception.ValidationException;
import com.sie.snest.engine.model.Bool;
import com.sie.snest.engine.model.ModelMeta;
import com.sie.snest.engine.rule.Filter;
import com.sie.snest.sdk.BaseModel;
import com.sie.snest.sdk.annotation.meta.MethodService;
import com.sie.snest.sdk.annotation.meta.Model;
import com.sie.snest.sdk.annotation.meta.Property;
import com.sie.snest.sdk.annotation.validate.Validate;
import org.apache.commons.collections.CollectionUtils;

import java.util.*;

/**
 * 附件基础类
 *
 * @author cxh
 * @date 2024年03月28日 10:53
 */
@Model(name = "example_base_attachment", displayName = "附件基础类", description = "example_base_attachment", isAutoLog = Bool.True, isAbstract = Bool.True)
public class ExampleBaseAttachment extends BaseModel<ExampleBaseAttachment> {
    
    /**
     * 文件名称
     */
    @Validate.NotBlank(message = "文件名称不能为空")
    @Property(displayName = "文件名称", length = 100)
    private String fileName;
    /**
     * 文件拓展名
     */
    @Validate.NotBlank(message = "文件拓展名不能为空")
    @Property(displayName = "文件拓展名", length = 100)
    private String fileExtName;
    /**
     * 文件id
     */
    @Property(displayName = "文件id", length = 100)
    private String fileId;
    /**
     * 文件大小
     */
    @Validate.NotBlank(message = "文件大小不能为空")
    @Property(displayName = "文件大小", length = 100)
    private String fileSize;
    /**
     * 备注
     */
    @Property(displayName = "备注", length = 200)
    private String remark;

    /**
     * 模型
     */
    @Property(displayName = "模型", length = 64)
    private String modelId;

    @MethodService(description = "上传")
    public boolean fileUpload(RecordSet rs, String fileId, String parentId) {
        Meta meta = rs.getMeta();

        String modelName=meta.getModelName();
        RecordSet rs1 = meta.get(modelName);
//        RecordSet recordSet1 = meta.get(modelName);
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
                Map<String, Object> examProductAttachment = new HashMap<>();
                List<Map<String, Object>> list=new ArrayList<>();
                examProductAttachment.put("fileName", attachmentMap.get("name"));
                examProductAttachment.put("fileExtName", attachmentMap.get("content_type"));
                examProductAttachment.put("key", fileId);
                examProductAttachment.put("fileId", attachmentMap.get("id"));
                examProductAttachment.put("fileSize", attachmentMap.get("size"));
                examProductAttachment.put("modelId", attachmentMap.get("model_id"));
                examProductAttachment.put("parent", parentId);
                list.add(examProductAttachment);
                rs1.call("create",list);
            }
        }
        return true;
    }
}
