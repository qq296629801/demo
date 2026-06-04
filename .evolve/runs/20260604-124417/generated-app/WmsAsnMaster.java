package com.sie.iidp.demo.wms.model;

import com.sie.meta.plugin.StaticVar;
import com.sie.meta.plugin.Getter;
import com.sie.meta.plugin.Setter;
import com.sie.snest.sdk.BaseModel;
import com.sie.snest.sdk.annotation.meta.Model;
import com.sie.snest.sdk.annotation.meta.Property;
import com.sie.snest.sdk.annotation.meta.MethodService;
import com.sie.snest.sdk.DataType;
import com.sie.snest.sdk.annotation.validate.Validate;
import com.sie.snest.sdk.annotation.orm.Index;
import com.sie.snest.sdk.annotation.orm.OneToMany;
import com.sie.snest.sdk.annotation.orm.CascadeType;
import com.sie.snest.engine.exception.ValidationException;
import com.sie.snest.engine.data.RecordSet;

/**
 * WMS 入库单主表
 * 对应需求文档：M-02 入库单（ASN）管理
 */
@StaticVar
@Getter
@Setter
@Model(name = "wms_asn_master", label = "入库单")
public class WmsAsnMaster extends BaseModel<WmsAsnMaster> {

    @Property(name = "asn_no", label = "入库单号", type = DataType.Char, len = 32, required = true)
    @Validate(unique = true)
    @Index(unique = true)
    private Object asnNo;

    @Property(name = "status", label = "状态", type = DataType.Integer, required = true)
    // 0-待收货 1-部分收货 2-已完成 9-已取消
    private Object status;

    @Property(name = "expected_arrival_date", label = "预计到货日期", type = DataType.Date)
    private Object expectedArrivalDate;

    @Property(name = "remark", label = "备注", type = DataType.Text, len = 500)
    private Object remark;

    @OneToMany(name = "details", target = "wms_asn_detail", joinField = "asn_id",
               cascade = {CascadeType.ALL})
    private Object details;

    /**
     * 收货操作
     * AC-005-01：收货数量不能超过预计数量
     * AC-005-02/03：更新入库单状态
     */
    @MethodService(name = "receive", label = "收货")
    public Object receive(RecordSet args) {
        // 状态校验：只有待收货或部分收货状态可收货
        Integer currentStatus = (Integer) this.get(F_STATUS);
        if (currentStatus == 2 || currentStatus == 9) {
            throw new ValidationException("当前入库单状态不允许收货");
        }
        return null;
    }

    /**
     * 取消入库单
     * AC-006-01：只有待收货状态可取消
     */
    @MethodService(name = "cancel", label = "取消")
    public Object cancel(RecordSet args) {
        Integer currentStatus = (Integer) this.get(F_STATUS);
        if (currentStatus != 0) {
            throw new ValidationException("只有待收货状态的入库单可以取消");
        }
        this.set(F_STATUS, 9);
        return this.save();
    }
}
