package com.wangjubao.dolphin.codegenerate;

import com.wangjubao.dolphin.codegenerate.util.StringUtil;

import java.sql.ResultSetMetaData;
import java.sql.SQLException;

public class GenerateHelper {
    public static String getModelClassName(String tableName) {
        String result = StringUtil.toCamelCase(tableName) + "Do";
        return result.substring(0,1).toUpperCase()+ result.substring(1);
    }

    public static String getJavaType(String columnClassName) {
        if (columnClassName.startsWith("java.lang.")) {
            return columnClassName.substring("java.lang.".length());
        } else if (columnClassName.equals("java.sql.Timestamp")
                || columnClassName.equals("java.sql.Date")) {
            return "Date";
        } else if (columnClassName.equals("java.math.BigInteger")) {
            return "Long";
        } else if (columnClassName.equals("java.math.BigDecimal")) {
            return "BigDecimal";
        } else {
            throw new RuntimeException("not supported type : " + columnClassName);
        }
    }

    public static String getSqlmapJavaType(String columnClassName) {
        if (columnClassName.equals("java.lang.Integer")) {
            return "INTEGER";
        } else if (columnClassName.equals("java.lang.String")) {
            return "VARCHAR";
        } else if (columnClassName.equals("java.sql.Timestamp")
                || columnClassName.equals("java.sql.Date")) {
            return "TIMESTAMP";
        } else if (columnClassName.equals("java.lang.Long")
                || columnClassName.equals("java.math.BigInteger")) {
            return "BIGINT";
        } else if (columnClassName.equals("java.lang.Float")) {
            return "NUMBER";
        } else if (columnClassName.equals("java.math.BigDecimal")) {
            return "DECIMAL";
        } else if (columnClassName.equals("java.lang.Double")) {
            return "DECIMAL";
        } else {
            throw new RuntimeException("not supported type : " + columnClassName);
        }
    }

    public static String getDaoClassName(String tableName) {
        String str = StringUtil.toCamelCase(tableName);
        return str.substring(0,1).toUpperCase()+str.substring(1) + "Dao";
    }

    public static String getPKType(ResultSetMetaData meta) throws SQLException {
        int count = meta.getColumnCount();
        for (int i = 1; i <= count; i++) {
            if (meta.getColumnName(i).equalsIgnoreCase("id")) {
                return GenerateHelper.getJavaType(meta.getColumnClassName(i));
            }
        }
        return meta.getColumnClassName(1);
        //        throw new RuntimeException("getPKType error, no column[id]. table is "
        //                + meta.getTableName(1));
    }

    public static String getColumnName(String columnNameInMeta) {
        return StringUtil.toCamelCase(columnNameInMeta);
    }
}
