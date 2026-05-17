package com.sie.iidp.example.filemgr.model;

import com.sie.snest.engine.annotation.ModelMethod;
import com.sie.snest.engine.model.Bool;
import com.sie.snest.engine.utils.IdGenerator;
import com.sie.snest.sdk.BaseModel;
import com.sie.snest.sdk.annotation.meta.Model;
import com.sie.snest.sdk.annotation.meta.Property;
import org.springframework.beans.factory.annotation.Value;

import java.util.Map;

@Model(tableName = "example_file", name = "example_file", displayName = "文件管理", isLogicDelete = Bool.True)
public class ExampleFile extends BaseModel<ExampleFile> {

    @Property(displayName = "文件名")
    private String fileName;

    @Property(displayName = "文件地址")
    private String fileLocation;

    @Property(displayName = "文件Id")
    private String fileId;

    @Value("minio.bucketName")
    private String minioBucket;

    public String getFileName() {
        return getStr("fileName");
    }

    public ExampleFile setFileName(String fileName) {
        this.set("fileName", fileName);
        return this;
    }

    public String getFileLocation() {
        return getStr("fileLocation");
    }

    public ExampleFile setFileLocation(String fileLocation) {
        this.set("fileLocation", fileLocation);
        return this;
    }

    public String getFileId() {
        return getStr("fileId");
    }

    public ExampleFile setFileId(String fileId) {
        this.set("fileId", fileId);
        return this;
    }

    public ExampleFile createFile(){
        Map<String, Object> arguments = getMeta().getArguments();
        ExampleFile exampleFile = new ExampleFile();
        exampleFile.setId(IdGenerator.nextId());
        exampleFile.setFileName(String.valueOf(arguments.get("fileName")));
        exampleFile.setFileLocation(minioBucket + "/" + arguments.get("fileLocation"));
        exampleFile.setFileId(String.valueOf(arguments.get("fileId")));

        return exampleFile;
    }

}
