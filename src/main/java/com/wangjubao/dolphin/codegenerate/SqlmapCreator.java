package com.wangjubao.dolphin.codegenerate;

import com.wangjubao.dolphin.codegenerate.util.StringUtil;
import org.springframework.beans.factory.annotation.Autowired;

import java.sql.ResultSetMetaData;
import java.sql.SQLException;

public class SqlmapCreator {
    private String    daoPackage;
    private String    modelPackage;
    @Autowired
    private WriteFile writeFile;

    /** setter */
    public void setDaoPackage(String daoPackage)
    {
        this.daoPackage = daoPackage;
    }

    /** setter */
    public void setModelPackage(String modelPackage)
    {
        this.modelPackage = modelPackage;
    }

    public void create(ResultSetMetaData meta) throws SQLException
    {
        String tableName = meta.getTableName(1);
        String modelClassName = GenerateHelper.getModelClassName(tableName);
        String resultClassName = modelClassName.substring(0, modelClassName.length() - 2);
        String resultClassNameFull = resultClassName + "Result";
        String pkClassType = getPKType(meta);
        String pkName = meta.getColumnName(1);
        String pkClassTypePrimitive = null;
        if (pkClassType.equalsIgnoreCase("Integer") || pkClassType.equalsIgnoreCase("java.lang.Integer"))
        {
            pkClassTypePrimitive = "int";
        } else if (pkClassType.equalsIgnoreCase("Long") || pkClassType.equalsIgnoreCase("java.lang.Long"))
        {
            pkClassTypePrimitive = "long";
        } else if (pkClassType.equalsIgnoreCase("java.math.BigInteger"))
        {
            pkClassTypePrimitive = "long";
        }
        {
            pkClassTypePrimitive = "String";
        }

        StringBuilder sb = new StringBuilder("<?xml version=\"1.0\" encoding=\"UTF-8\" ?>\r\n");
        sb.append("<!DOCTYPE sqlMap PUBLIC \"-//ibatis.apache.org//DTD SQL Map 2.0//EN\" \"http://ibatis.apache.org/dtd/sql-map-2.dtd\" >\r\n");
        sb.append("<sqlMap namespace=\"").append(tableName).append("\">\r\n");
        sb.append("    <typeAlias alias=\"").append(resultClassName).append("\" type=\"");
        sb.append(modelPackage).append(".").append(modelClassName).append("\" />\r\n");
        sb.append("    <resultMap id=\"").append(resultClassNameFull).append("\" class=\"").append(resultClassName).append("\">\r\n");

        int count = meta.getColumnCount();
        //生成字段
        for (int i = 1; i <= count; i++)
        {
            String columnClassName = meta.getColumnClassName(i);
            String sqlmapJavaType = GenerateHelper.getSqlmapJavaType(columnClassName);
            String columnName = meta.getColumnName(i);

            sb.append("        <result column=\"").append(columnName).append("\"");
            sb.append(" property=\"").append(GenerateHelper.getColumnName(columnName)).append("\"");

            if (meta.getColumnType(i) == 1 && columnClassName.equals("java.lang.String"))
            {
                sb.append(" jdbcType=\"CHAR\" javaType=\"boolean\" nullValue=\"N\"");
            } else
            {
                sb.append(" jdbcType=\"").append(sqlmapJavaType).append("\"");
                if (sqlmapJavaType.equalsIgnoreCase("DECIMAL"))
                {
                    sb.append(" nullValue=\"0.00\"");
                }
            }

            sb.append(" />\r\n");
        }
        sb.append("    </resultMap>\r\n");

        //load方法
        sb.append("\r\n");
        sb.append("    <select id=\"load\" resultMap=\"").append(resultClassNameFull).append("\" parameterClass=\"")
                .append("java.util.HashMap").append("\">\r\n");

        sb.append("        select ");
        for (int i = 1; i <= count; i++)
        {
            String columnName = meta.getColumnName(i);
            if (i > 1)
            {
                sb.append(", ");
            }
            sb.append(columnName);
        }
        sb.append("\r\n");
        sb.append("        FROM ").append(tableName).append("\r\n");
        sb.append("        WHERE " + pkName + " = #" + pkName + ":").append(pkClassType.toUpperCase()).append("#\r\n");
        sb.append("    </select>");

        //delete
        sb.append("\r\n");
        sb.append("\r\n");
        sb.append("    <delete id=\"delete\" parameterClass=\"").append("java.util.HashMap").append("\">\r\n");
        sb.append("        delete from ").append(tableName).append("\r\n");
        sb.append("        WHERE " + pkName + " = #" + pkName + ":").append(pkClassType.toUpperCase()).append("#\r\n");
        sb.append("    </delete>");

        //create
        sb.append("\r\n");
        sb.append("\r\n");
        sb.append("    <insert id=\"create\" parameterClass=\"").append(resultClassName).append("\">\r\n");
        sb.append("        insert into ").append(tableName).append(" (");
        for (int i = 1; i <= count; i++)
        {
            String columnName = meta.getColumnName(i);
            if (i > 1)
            {
                sb.append(", ");
            }
            sb.append(columnName);
        }
        sb.append(") \r\n        values (");
        for (int i = 1; i <= count; i++)
        {
            String columnClassName = meta.getColumnClassName(i);
            String sqlmapJavaType = GenerateHelper.getSqlmapJavaType(columnClassName);
            String columnName = meta.getColumnName(i);
            if (i > 1)
            {
                sb.append(", ");
            }
            if (columnName.equalsIgnoreCase("createTime") || columnName.equalsIgnoreCase("updateTime")
                    || columnName.equalsIgnoreCase("gmt_create") || columnName.equalsIgnoreCase("gmt_modified"))
            {
                sb.append("now()");
            } else
            {
                if (meta.getColumnType(i) == 1 && columnClassName.equals("java.lang.String"))
                {
                    sb.append("#").append(GenerateHelper.getColumnName(columnName)).append(":CHAR#");
                } else
                {
                    sb.append("#").append(GenerateHelper.getColumnName(columnName)).append(":").append(sqlmapJavaType).append("#");
                }
            }
        }
        sb.append(")\r\n");

        //        sb.append("        <selectKey keyProperty=\"id\" type=\"post\" resultClass=\"")
        //                .append(pkClassTypePrimitive).append("\">\r\n");
        //        sb.append("            select LAST_INSERT_ID() as id\r\n");
        //        sb.append("        </selectKey>\r\n");

        sb.append("    </insert>");

        //update
        sb.append("\r\n");
        sb.append("\r\n");
        sb.append("    <update id=\"update\" parameterClass=\"").append(resultClassName).append("\">\r\n");
        sb.append("        update ").append(tableName).append(" set gmt_modified = now()\r\n");
        boolean fk = Boolean.FALSE;
        for (int i = 1; i <= count; i++)
        {
            String columnName = meta.getColumnName(i);
            if (columnName.equalsIgnoreCase("sellerId") || columnName.equalsIgnoreCase("seller_id"))
            {
                fk = Boolean.TRUE;
            }
            if (columnName.equalsIgnoreCase("id") || columnName.equalsIgnoreCase("sellerId")
                    || columnName.equalsIgnoreCase("seller_id"))
            {
                continue;
            }
            String hasserName = "has" + StringUtil.first2UpperCase(GenerateHelper.getColumnName(columnName));
            String columnClassName = meta.getColumnClassName(i);
            String sqlmapJavaType = GenerateHelper.getSqlmapJavaType(columnClassName);
            if (columnName.equalsIgnoreCase("createTime") || columnName.equalsIgnoreCase("updateTime")
                    || columnName.equalsIgnoreCase("gmt_create") || columnName.equalsIgnoreCase("gmt_modified"))
            {
                continue;
            }

            sb.append("        <isNotNull prepend=\",\" property=\"").append(hasserName).append("\">\r\n");
            if (meta.getColumnType(i) == 1 && columnClassName.equals("java.lang.String"))
            {
                sqlmapJavaType = "CHAR";
            }
            sb.append("            ").append(columnName).append(" = ").append("#").append(GenerateHelper.getColumnName(columnName))
                    .append(":").append(sqlmapJavaType).append("#\r\n");
            sb.append("        </isNotNull>\r\n");
        }
        String columnClassName = meta.getColumnClassName(1);
        String sqlmapJavaType = GenerateHelper.getSqlmapJavaType(columnClassName);
        sb.append("        WHERE " + pkName + " = #" + pkName + ":" + sqlmapJavaType + "#\r\n");
        sb.append("    </update>");

        sb.append("\r\n");

        //listByPage方法
        sb.append("\r\n");
        sb.append("    <select id=\"listByPage\" resultMap=\"").append(resultClassNameFull).append("\" parameterClass=\"")
                .append("java.util.HashMap").append("\">\r\n");

        sb.append("        SELECT ");
        for (int i = 1; i <= count; i++)
        {
            String columnName = meta.getColumnName(i);
            if (i > 1)
            {
                sb.append(", ");
            }
            sb.append("t2.").append(columnName);
        }
        sb.append("\r\n");
        sb.append("        FROM (").append("\r\n");
        sb.append("                 SELECT " + pkName + " FROM ").append(tableName);
        sb.append("                 ORDER BY " + pkName).append("\r\n");
        sb.append("                 LIMIT #startIndex:INTEGER#,#pageSize:INTEGER#");
        sb.append(")t1,").append(tableName).append(" t2 ");
        sb.append("WHERE t1." + pkName + " = t2." + pkName).append("#\r\n");
        sb.append("    </select>");

        sb.append("\r\n");

        //listByPageCount方法
        sb.append("\r\n");
        sb.append("    <select id=\"listByPageCount\" resultClass=\"").append("java.lang.Integer").append("\" parameterClass=\"")
                .append("java.util.HashMap").append("\">\r\n");

        sb.append("        SELECT count(*) ").append("\r\n");
        sb.append("        FROM ").append(tableName).append("\r\n");

        sb.append("    </select>");

        sb.append("\r\n");

        sb.append("</sqlMap>");

        sb.append("\r\n");

        System.out.println(sb.toString());

        writeFile.write(daoPackage, tableName, "xml", sb.toString());
    }

    private String getPKType(ResultSetMetaData meta) throws SQLException
    {
        int count = meta.getColumnCount();
        for (int i = 1; i <= count; i++)
        {
            if (meta.getColumnName(i).equalsIgnoreCase("id"))
            {
                return GenerateHelper.getJavaType(meta.getColumnClassName(i));
            }
        }
        return meta.getColumnClassName(1);
        //        throw new RuntimeException("getPKType error, no column[id].");
    }
}
