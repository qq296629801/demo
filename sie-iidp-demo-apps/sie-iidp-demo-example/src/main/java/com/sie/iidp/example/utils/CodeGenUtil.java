package com.sie.iidp.example.utils;

import cn.hutool.core.util.StrUtil;
import com.alibaba.fastjson.JSONObject;
import com.sie.iidp.example.consts.ComConst;
import com.sie.iidp.example.utils.dto.EncoderRuleDummyDTO;
import com.sie.iidp.example.utils.errors.Exceptions;
import com.sie.iidp.example.utils.iidp.BizSettingUtil;
import com.sie.snest.engine.context.BaseContextHandler;
import com.sie.snest.engine.exception.ValidationException;
import org.springframework.util.CollectionUtils;

import java.util.*;

/**
 * 编码生成工具类
 *
 * @author cxh
 * @date 2024年03月27日 14:12
 */
public class CodeGenUtil {
    /**
     * 新增时对实体进行编码赋值
     *
     * @param valuesList
     * @param modelName
     * @param ruleName
     * @param codeFieldName
     * @return
     */
    public static List<Map<String, Object>> codeAssign(List<Map<String, Object>> valuesList, String modelName, String ruleName, String codeFieldName) {
        if (CollectionUtils.isEmpty(valuesList)) {
            return valuesList;
        }
        Optional<String> encoderRuleIdOp = CodeGenUtil.obtainEncoderRuleId(modelName,
                ruleName, true);
        if (!encoderRuleIdOp.isPresent()) {
            throw Exceptions.CODE_GEN_SETTING_DISABLED_EXCEPTION;
        }
        Queue<String> codeQueue = CodeGenUtil.generateCodeQueue(encoderRuleIdOp.get(), valuesList.size());
        for (Map<String, Object> value : valuesList) {
            String code = codeQueue.poll();
            value.put(codeFieldName, code);
        }
        return valuesList;
    }

    /**
     * 生成单个编码
     *
     * @param modelName
     * @param settingName
     * @return
     */
    public static Optional<String> genSingleCode(String modelName, String settingName) {
        Optional<String> encoderRuleIdOp = obtainEncoderRuleId(modelName, settingName, true);
        if (!encoderRuleIdOp.isPresent()) {
            return Optional.empty();
        }
        String encoderRuleId = encoderRuleIdOp.get();
        List<String> codeList = generateCodeList(encoderRuleId, 1);
        String code = codeList.get(0);
        return Optional.of(code);
    }

    /**
     * 获取编码生成规则配置项
     *
     * @param modelName
     * @param settingName
     * @return
     */
    public static Optional<String> obtainEncoderRuleId(String modelName, String settingName, boolean ignoreUnConfigured) {
        return BizSettingUtil.getBizSettingValue(modelName, settingName, ignoreUnConfigured);
    }

    /**
     * 生成多个编码工具类，返回队列
     *
     * @param encoderRuleId
     * @param genNum
     * @return
     */

    public static Queue<String> generateCodeQueue(String encoderRuleId, int genNum) {
        List<String> codeList = generateCodeList(encoderRuleId, genNum);
        Queue<String> codeQueue = new LinkedList<>(codeList);
        return codeQueue;
    }

    /**
     * 获取生成的编码
     *
     * @param encoderRuleId
     * @param genNum
     * @return
     */
    public static List<String> generateCodeList(String encoderRuleId, int genNum) {
        if (genNum < 1) {
            throw Exceptions.GEN_NUM_EXCEPTION;
        }
        JSONObject jsonObject = new JSONObject();
        jsonObject.put(ComConst.ENCODER_COUNT, genNum);
        jsonObject.put(ComConst.ENCODER_RULE_ID, encoderRuleId);
        //返回数据接口 {"code":0,"msg":null,"data":["M2309200010"]}
        Map<String, Object> callResult = (Map<String, Object>) BaseContextHandler.getMeta().get(ComConst.ENCODER_RULE)
                .call(ComConst.GET_ENCODER_API, jsonObject);
        Integer returnCode = (Integer) callResult.get(ComConst.RETURN_CODE);
        if (!returnCode.equals(0)) {
            String returnMsg = (String) callResult.get(ComConst.RETURN_MSG);
            throw new ValidationException(String.format("生成编码失败，详细信息：%s", returnMsg));
        } else {
            List<String> dataList = (List<String>) callResult.get(ComConst.RETURN_DATA);
            if (CollectionUtils.isEmpty(dataList) || genNum > dataList.size()) {
                throw Exceptions.INSUFFICIENT_NUM_EXCEPTION;
            }
            return dataList.subList(0, genNum);
        }
    }

    /**
     * 获取生成的编码
     *
     * @param encoderRuleId
     * @param genNum
     * @return
     */
    public static EncoderRuleDummyDTO generateCodeRangePreview(String encoderRuleId, Integer genNum) {
        if (genNum < 1) {
            throw Exceptions.GEN_NUM_EXCEPTION;
        }

        JSONObject jsonObject = new JSONObject();
        jsonObject.put(ComConst.ENCODER_COUNT, genNum);
        jsonObject.put(ComConst.ENCODER_RULE_ID, encoderRuleId);

        Map<String, Object> callResult = (Map<String, Object>) BaseContextHandler.getMeta().get(ComConst.ENCODER_RULE)
                .call(ComConst.GET_ENCODER_RANGE_NO_SAVE_API, jsonObject);
        Integer returnCode = (Integer) callResult.get(ComConst.RETURN_CODE);
        if (!returnCode.equals(0)) {
            String returnMsg = (String) callResult.get(ComConst.RETURN_MSG);
            throw new ValidationException(String.format("生成编码失败，详细信息：%s", returnMsg));
        } else {
            JSONObject jsonObjectOfResult = (JSONObject) callResult.get(ComConst.RETURN_DATA);
            if (jsonObjectOfResult == null) {
                throw Exceptions.INSUFFICIENT_NUM_EXCEPTION;
            }

            EncoderRuleDummyDTO encoderRuleDummyDTO = new EncoderRuleDummyDTO();
            encoderRuleDummyDTO.setCode(jsonObjectOfResult.getString("code"));
            encoderRuleDummyDTO.setName(jsonObjectOfResult.getString("name"));
            encoderRuleDummyDTO.setStartCodeNumber(jsonObjectOfResult.getString("startCodeNumber"));
            encoderRuleDummyDTO.setEndCodeNumber(jsonObjectOfResult.getString("endCodeNumber"));

            if (StrUtil.isEmpty(encoderRuleDummyDTO.getStartCodeNumber())
                    || StrUtil.isEmpty(encoderRuleDummyDTO.getEndCodeNumber())) {
                throw Exceptions.INSUFFICIENT_NUM_EXCEPTION;
            }

            return encoderRuleDummyDTO;
        }
    }
}
