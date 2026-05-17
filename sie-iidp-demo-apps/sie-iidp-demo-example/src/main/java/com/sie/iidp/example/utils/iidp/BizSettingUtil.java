package com.sie.iidp.example.utils.iidp;

import com.sie.iidp.example.consts.ComConst;
import com.sie.iidp.example.utils.errors.Exceptions;
import com.sie.snest.engine.context.BaseContextHandler;
import com.sie.snest.engine.data.RecordSet;
import com.sie.snest.engine.rule.Filter;
import org.springframework.util.CollectionUtils;

import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.Optional;

/**
 * 业务配置项的工具类
 *
 * @author cxh
 * @date 2024年03月27日 14:24
 */
public final class BizSettingUtil {

    private static final String CONFIGURABLE_VAL = "1";

    /**
     * 获取配置项的值
     *
     * @param modelName   模块编码
     * @param settingName 配置项编码
     * @return 配置项的值
     */
    public static Optional<String> getBizSettingValue(String modelName, String settingName, boolean ignoreUnConfigured) {
        Filter filter = new Filter();
        filter.and(Filter.equal(ComConst.MODEL_CODE_FIELD_NAME, modelName));
        filter.and(Filter.equal(ComConst.CODE_FIELD_NAME, settingName));

        if (ignoreUnConfigured) {
            filter.and(Filter.equal(ComConst.CONFIGURED_FIELD_NAME, CONFIGURABLE_VAL));
        }

        //查找生成规则
        RecordSet recordSet = BaseContextHandler.getMeta().get(ComConst.BIZ_SETTING);
        if (recordSet.getModel() == null) {
            throw Exceptions.getModelNotFoundException(ComConst.BIZ_SETTING);
        }

        List<Map<String, Object>> ruleList = recordSet
                .search(filter, Arrays.asList(ComConst.SETTING_VALUE), 1, 0, "");

        if (CollectionUtils.isEmpty(ruleList)) {
            return Optional.empty();
        }

        Map<String, Object> rule = ruleList.get(0);
        String ruleSettingValue = String.valueOf(rule.get(ComConst.SETTING_VALUE));
        return Optional.of(ruleSettingValue);
    }

}
