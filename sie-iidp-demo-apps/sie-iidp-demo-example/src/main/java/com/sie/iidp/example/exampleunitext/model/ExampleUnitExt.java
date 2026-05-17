package com.sie.iidp.example.exampleunitext.model;

import com.sie.iidp.example.consts.MethodConst;
import com.sie.meta.plugin.Getter;
import com.sie.meta.plugin.Setter;
import com.sie.meta.plugin.StaticVar;
import com.sie.snest.engine.data.RecordSet;
import com.sie.snest.engine.model.Bool;
import com.sie.snest.engine.rule.Filter;
import com.sie.snest.sdk.BaseModel;
import com.sie.snest.sdk.annotation.meta.MethodService;
import com.sie.snest.sdk.annotation.meta.Model;
import com.sie.snest.sdk.annotation.meta.Property;
import com.sie.snest.sdk.annotation.orm.Option;
import com.sie.snest.sdk.annotation.orm.Selection;
import com.sie.snest.sdk.annotation.validate.Validate;
import lombok.extern.slf4j.Slf4j;

import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;

/**
 * Example-单位
 *
 * @author shenyihao
 * @date 2024年04月07日 11:20
 */
@StaticVar
@Getter
@Setter
@Slf4j
@Model(tableName = "example_unit_ext", name = "example_unit_ext", description = "Example-单位-扩展", displayName = "Example-单位-扩展", isLogicDelete = Bool.False, isAutoLog = Bool.True,parent = {"example_unit"})
public class ExampleUnitExt extends BaseModel<ExampleUnitExt> {
    @Validate.NotBlank(message = "状态不能为空")
    @Property(displayName = "状态", defaultValue = "1")
    @Selection(values = {
            @Option(label = "启用", value = "1"),
            @Option(label = "禁用", value = "0")
    })
    private String isEnable;

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
        RecordSet recordSet = (RecordSet) rs.callSuper(null, MethodConst.FIND, Filter.in("id", Arrays.asList(ids)), null, null, null);
        if (recordSet.any()) {
            Map<String, Object> valMap = new HashMap<>();
            valMap.put("isEnable", statusFlag);
            recordSet.callSuper(null, MethodConst.UPDATE, valMap);
        }
        return true;
    }

}
