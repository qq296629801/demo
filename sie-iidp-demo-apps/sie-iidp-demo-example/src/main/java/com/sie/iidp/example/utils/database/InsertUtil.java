package com.sie.iidp.example.utils.database;

import cn.hutool.core.exceptions.ValidateException;
import cn.hutool.core.util.StrUtil;
import com.sie.snest.sdk.annotation.meta.Model;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import static com.sie.iidp.example.utils.database.DataBaseUtil.changeObjToSqlChar;
import static com.sie.iidp.example.utils.database.DataBaseUtil.executeUpdateSql;

/**
 * @author cxh
 * @date 2024年03月27日 14:00
 */
public class InsertUtil {

    public static void insert(Class clazz, Map<String, Object> map) {
        String insertSql = insertSql(clazz, map);
        executeUpdateSql(insertSql);
    }

    public static void insert(String tableName, Map<String, Object> map) {
        String insertSql = insertSql(tableName, map);
        executeUpdateSql(insertSql);
    }

    public static void batchInsert(Class clazz, List<Map<String, Object>> list) {
        String batchInsertSql = batchInsertSql(clazz, list);
        executeUpdateSql(batchInsertSql);
    }

    public static void batchInsert(String tableName, List<Map<String, Object>> list) {
        String batchInsertSql = batchInsertSql(tableName, list);
        executeUpdateSql(batchInsertSql);
    }

    public static String insertSql(Class clazz, Map<String, Object> map) {
        Model model = (Model) clazz.getAnnotation(Model.class);
        String tableName = model.tableName();
        if (StrUtil.isEmpty(tableName)) {
            throw new ValidateException("表名不能为空");
        }
        List<Map<String, Object>> list = new ArrayList<>();
        list.add(map);
        return createBatchInsertSql(list, tableName);
    }

    public static String insertSql(String tableName, Map<String, Object> map) {
        if (StrUtil.isEmpty(tableName)) {
            throw new ValidateException("表名不能为空");
        }
        List<Map<String, Object>> list = new ArrayList<>();
        list.add(map);
        return createBatchInsertSql(list, tableName);
    }

    public static String batchInsertSql(Class clazz, List<Map<String, Object>> list) {
        Model model = (Model) clazz.getAnnotation(Model.class);
        String tableName = model.tableName();
        if (StrUtil.isEmpty(tableName)) {
            throw new ValidateException("表名不能为空");
        }
        return createBatchInsertSql(list, tableName);
    }

    public static String batchInsertSql(String tableName, List<Map<String, Object>> list) {
        if (StrUtil.isEmpty(tableName)) {
            throw new ValidateException("表名不能为空");
        }
        return createBatchInsertSql(list, tableName);
    }

    private static String createBatchInsertSql(List<Map<String, Object>> list, String tableName) {
        StringBuilder sqlBuilder = new StringBuilder();
        sqlBuilder.append("INSERT INTO ").append(tableName).append(" (");

        // 构建字段列表
        Map<String, Object> firstMap = list.get(0);
        for (String key : firstMap.keySet()) {
            sqlBuilder.append(key).append(", ");
        }
        sqlBuilder.delete(sqlBuilder.length() - 2, sqlBuilder.length()); // 删除最后的逗号和空格
        sqlBuilder.append(") VALUES ");

        // 构建值列表
        for (Map<String, Object> map : list) {
            sqlBuilder.append("(");
            for (Object value : map.values()) {
                String sqlChar = changeObjToSqlChar(value);
                sqlBuilder.append(StrUtil.isEmpty(sqlChar) ? value : sqlChar).append(", ");
            }
            sqlBuilder.delete(sqlBuilder.length() - 2, sqlBuilder.length()); // 删除最后的逗号和空格
            sqlBuilder.append("), ");
        }
        sqlBuilder.delete(sqlBuilder.length() - 2, sqlBuilder.length()); // 删除最后的逗号和空格
        sqlBuilder.append(";");

        return sqlBuilder.toString();
    }
}
