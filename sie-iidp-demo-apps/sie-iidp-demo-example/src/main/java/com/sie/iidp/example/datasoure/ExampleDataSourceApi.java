package com.sie.iidp.example.datasoure;

import com.sie.snest.sdk.BaseModel;
import com.sie.snest.sdk.annotation.meta.Model;
import com.sie.snest.sdk.annotation.meta.Property;
import com.sie.snest.sdk.annotation.validate.Validate;

@Model(name = "ExampleDataSource", parent = "ExampleDataSource")
public class ExampleDataSourceApi extends BaseModel<ExampleDataSourceApi> {
    @Property(columnName = "header", displayName = "请求头")
    @Validate.NotBlank(groups = ExampleDataSource.Api.class)
    private String header;

    @Validate.NotBlank(groups = ExampleDataSource.Api.class)
    @Property(columnName = "parameters", displayName = "请求参数")
    private String parameters;
}
