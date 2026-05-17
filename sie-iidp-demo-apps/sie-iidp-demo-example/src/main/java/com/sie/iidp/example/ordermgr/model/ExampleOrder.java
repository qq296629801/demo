package com.sie.iidp.example.ordermgr.model;

import com.sie.iidp.common.util.CodeGenUtil;
import com.sie.iidp.example.consts.MethodConst;
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
 * Example-订单
 *
 * @author tangjichen
 * @date 2024年5月20日 09点11分
 */
@Slf4j
@Model(name = "example_order", displayName = "Example-订单", isLogicDelete = Bool.True)
public class ExampleOrder extends BaseModel<ExampleOrder> {

    @Validate.NotBlank(message = "订单名称")
    @Property(displayName = "订单名称", columnName = "order_name", length = 200)
    private String orderName;

    @Property(displayName = "订单流水号", columnName = "order_serial", length = 200)
    private String orderSerial;

    @Property(displayName = "订单编码", columnName = "order_code", length = 200)
    private String orderCode;

    @Property(displayName = "启用状态", defaultValue = "0", length = 10)
    @Selection(values = {
            @Option(label = "启用", value = "1"),
            @Option(label = "禁用", value = "0")
    })
    @Validate.NotBlank(message = "启用状态不能为空")
    private String isEnable;

    /**
     * 字段常量
     */
    public static final String FILED_ID = "id";
    public static final String FILED_IS_ENABLE = "isEnable";


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
        RecordSet recordSet = (RecordSet) rs.callSuper(null, MethodConst.FIND, Filter.in(FILED_ID, Arrays.asList(ids)), null, null, null);
        if (recordSet.any()) {
            Map<String, Object> valMap = new HashMap<>();
            valMap.put(FILED_IS_ENABLE, statusFlag);
            recordSet.callSuper(null, MethodConst.UPDATE, valMap);
        }
        return true;
    }

    @MethodService(description = "创建订单")
    public ExampleOrder createOrder(RecordSet rs) {
        Map<String, Object> arguments = getMeta().getArguments();

        String orderSerial = (String) getMeta().get("biz_setting").call("genCode", "example_order", "demo_example_order_serial_encoder_rule");
        String orderSnowflake = (String) getMeta().get("biz_setting").call("genCode", "example_order", "demo_example_order_snowflake_encoder_rule");


        ExampleOrder exampleOrder = new ExampleOrder();
        exampleOrder.set("orderName", arguments.get("orderName"));
        exampleOrder.set("orderSerial", orderSerial);
        exampleOrder.set("orderCode", orderSnowflake);
        exampleOrder.set("isEnable", 0);
        exampleOrder.create();

        return exampleOrder;
    }

}
