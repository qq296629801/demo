package com.sie.iidp.example.exampleunitext.model;

import com.sie.iidp.example.consts.MethodConst;
import com.sie.iidp.example.unitmgr.model.ExampleUnit;
import com.sie.meta.plugin.Getter;
import com.sie.meta.plugin.Setter;
import com.sie.meta.plugin.StaticVar;
import com.sie.snest.engine.data.RecordSet;
import com.sie.snest.engine.model.Bool;
import com.sie.snest.engine.rule.Filter;
import com.sie.snest.sdk.BaseModel;
import com.sie.snest.sdk.DataType;
import com.sie.snest.sdk.annotation.meta.MethodService;
import com.sie.snest.sdk.annotation.meta.Model;
import com.sie.snest.sdk.annotation.meta.Property;
import com.sie.snest.sdk.annotation.orm.Option;
import com.sie.snest.sdk.annotation.orm.Selection;
import com.sie.snest.sdk.annotation.validate.Validate;
import lombok.extern.slf4j.Slf4j;

import java.util.*;

/**
 * Example-单位-时间-扩展
 *
 * @author tangch
 * @date 2024年04月26日
 */
@Slf4j
@StaticVar
@Getter
@Setter
@Model(tableName = "example_unit_time_ext", name = "example_unit_time_ext", description = "Example-单位-时间-扩展", displayName = "Example-单位-时间-扩展", isLogicDelete = Bool.False, isAutoLog = Bool.True,parent = {"example_unit"})
public class ExampleUnitTimeExt extends BaseModel<ExampleUnitTimeExt> {
    @Validate.NotBlank(message = "状态不能为空")
    @Property(displayName = "状态", defaultValue = "1")
    @Selection(values = {
            @Option(label = "启用", value = "1"),
            @Option(label = "禁用", value = "0")
    })
    private String isEnable;


    @Property(columnName = "effect_time", displayName = "生效时间")
    private String effectTime;

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
            valMap.put(ExampleUnitTimeExt.F_IS_ENABLE, statusFlag);
            recordSet.callSuper(null, MethodConst.UPDATE, valMap);
        }
        return true;
    }


    /**
     * 重写查询
     * @param filter 入参
     * @param properties 参数
     * @param limit 分页
     * @param offset 每页数量
     * @param order
     * @return
     */
    @Override
    public List<ExampleUnitTimeExt> search(Filter filter, List<String> properties, Integer limit, Integer offset, String order) {
        return super.search(filter, properties, limit, offset, order);
    }
}
