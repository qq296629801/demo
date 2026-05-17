package com.sie.iidp.example.model;

import com.sie.meta.plugin.Getter;
import com.sie.meta.plugin.Setter;
import com.sie.meta.plugin.StaticVar;
import com.sie.snest.engine.rule.Filter;
import com.sie.snest.sdk.BaseModel;
import com.sie.snest.sdk.annotation.meta.MethodService;
import com.sie.snest.sdk.annotation.meta.Model;
import com.sie.snest.sdk.annotation.meta.Property;
import com.sie.snest.sdk.annotation.orm.Index;
import org.apache.commons.lang3.StringUtils;

import java.util.List;

/**
 * 地理位置设置（省-市-区县市-街道镇）
 */
@StaticVar
@Getter
@Setter
@Model(tableName = "area_model", name = "demo_area_model", orderBy = "code asc",
        displayName = "地理位置设置（省-市-区县市-街道镇）", description = "地理位置设置（省-市-区县市-街道镇）",
        indexes = { @Index(name = "IDX_CODE", columnList = {"code"}, unique = true)})
public class Area extends BaseModel<Area> {

    @Property(displayName = "code代码", columnName = "code", length = 40)
    private String code;

    @Property(displayName = "名称", columnName = "name", length = 50, displayForModel = true)
    private String name;

    /**
     * 1-省份 2-城市 3-区县 4-街道
     */
    @Property(displayName = "类型", columnName = "type", length = 1)
    private int type;

    @Property(displayName = "父级编码", columnName = "parent_code", length = 80)
    private String parentCode;

    /**
     * 0-否，1-是
     */
    @Property(displayName = "叶子节点", columnName = "leaf", length = 1)
    private int leaf;

    /**
     * 查询地区（省-市-区县市-街道镇）
     * @param parentCode
     * @return
     */
    @MethodService(description = "selectArea")
    public List<Area> selectArea(String parentCode){
        if(StringUtils.isNotBlank(parentCode)){
            return this.search(Filter.equal("parentCode", parentCode), this.getAllProperties(), 0, 0, "code asc");
        }else {
            return this.search(Filter.equal("parentCode", "00"), this.getAllProperties(), 0, 0, "code asc");
        }
    }


//    @MethodService(description = "updateArea")
//    public long updateArea(){
//        List<Area> areaList = this.search(Filter.equal("type",3), Arrays.asList("code","name"),4000,0,null);
//        List<String> codeList = new ArrayList<>();
//        List<String> nameList = new ArrayList<>();
//        for(Area area : areaList){
//            String code = area.getStr("code");
//            Long count =  this.count(Filter.equal("parentCode",code));
//            if (count<1){
//                nameList.add(code+","+area.getStr("name"));
//                codeList.add(code);
//                area.set("leaf",1);
//                area.update();;
//            }
//        }
//        return codeList.size();
//    }
}
