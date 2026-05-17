package com.sie.iidp.example.joblevel.model;

import com.sie.snest.engine.model.Bool;
import com.sie.snest.sdk.BaseModel;
import com.sie.snest.sdk.CascadeType;
import com.sie.snest.sdk.annotation.meta.Model;
import com.sie.snest.sdk.annotation.meta.Property;
import com.sie.snest.sdk.annotation.orm.*;
import lombok.Data;

@Model(tableName = "job_level", name = "job_level", displayName = "职位管理", isLogicDelete = Bool.True)
public class JobLevel extends BaseModel<JobLevel> {

    @Property(displayName = "职位名称")
    private String jobTitle;

    @Property(displayName = "上级职位", store = false, related = "jobLevel.jobTitle")
    private String parentJobTitle;

    @Property(displayName = "父Id")
    private String parentId;

    @Property(displayName = "职位描述")
    private String description;

    @ManyToOne(displayName = "父职位", cascade = CascadeType.DEL_SET_NULL, targetModel = "job_level")
    @JoinColumn(name = "parent_id", referencedProperty = "id")
    @Property(displayName = "父职位", store = false)
    private JobLevel jobLevel;

}
