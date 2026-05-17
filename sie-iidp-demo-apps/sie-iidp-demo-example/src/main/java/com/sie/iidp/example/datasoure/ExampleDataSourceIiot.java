package com.sie.iidp.example.datasoure;

import com.sie.snest.sdk.BaseModel;
import com.sie.snest.sdk.annotation.meta.Model;
import com.sie.snest.sdk.annotation.meta.Property;
import com.sie.snest.sdk.annotation.validate.Validate;

@Model(name = "ExampleDataSource", parent = "ExampleDataSource")
public class ExampleDataSourceIiot extends BaseModel<ExampleDataSourceIiot> {
    @Property(columnName = "uri", displayName = "URI")
    @Validate.NotBlank(groups = ExampleDataSource.Iiot.class)
    private String uri;
}
