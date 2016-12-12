package com.wangjubao.dolphin.codegenerate;

import com.wangjubao.dolphin.codegenerate.util.StringUtil;
import org.springframework.beans.factory.annotation.Autowired;

import java.sql.ResultSetMetaData;
import java.sql.SQLException;
import java.text.SimpleDateFormat;
import java.util.Date;

public class ModelCreator {
    private String    modelPackage;

    @Autowired
    private WriteFile writeFile;

    public void create(ResultSetMetaData meta) throws SQLException {
        String modelClassName = GenerateHelper.getModelClassName(meta.getTableName(1));

        int count = meta.getColumnCount();
        StringBuilder sb = new StringBuilder("package ").append(modelPackage).append(";\r\n");
        sb.append("\r\n");
        sb.append("import java.io.Serializable;\r\n");
        sb.append("import java.math.BigDecimal;\r\n");
        sb.append("import java.util.Date;\r\n");
        sb.append("public class ").append(modelClassName).append(" implements Serializable {\r\n");
        sb.append("    private static final long serialVersionUID = " + System.currentTimeMillis()
                + "L;\r\n");
        sb.append("\r\n");

        //生成字段
        for (int i = 1; i <= count; i++) {
            String columnClassName = meta.getColumnClassName(i);
            String columnName = meta.getColumnName(i);
            columnName = GenerateHelper.getColumnName(columnName);
            if (meta.getColumnType(i) == 1 && columnClassName.equals("java.lang.String")) {
                columnClassName = "java.lang.Boolean";
            }

            sb.append("    private ").append(GenerateHelper.getJavaType(columnClassName))
                    .append(" ").append(columnName).append(";\r\n");
        }

        //生成setter, getter
        for (int i = 1; i <= count; i++) {
            sb.append("\r\n");

            String columnName = meta.getColumnName(i);
            columnName = GenerateHelper.getColumnName(columnName);
            String columnClassName = meta.getColumnClassName(i);
            if (meta.getColumnType(i) == 1 && columnClassName.equals("java.lang.String")) {
                columnClassName = "java.lang.Boolean";
            }

            String setterName = "set" + StringUtil.first2UpperCase(columnName);
            String getterName = "get" + StringUtil.first2UpperCase(columnName);

            sb.append("    public void ").append(setterName).append("(");
            sb.append(GenerateHelper.getJavaType(columnClassName)).append(" ").append(columnName)
                    .append(") {\r\n");
            sb.append("        this.").append(columnName).append(" = ").append(columnName)
                    .append(";\r\n");
            sb.append("    }\r\n");

            sb.append("\r\n");

            sb.append("    public ").append(GenerateHelper.getJavaType(columnClassName))
                    .append(" ").append(getterName).append("() {\r\n");
            sb.append("        return ").append(columnName).append(";\r\n");
            sb.append("    }\r\n");
        }

        //生成hashCode()
        sb.append("\r\n");
        sb.append("    @Override\r\n");
        sb.append("    public int hashCode() {\r\n");
        sb.append("        final int prime = 31;\r\n");
        sb.append("        int result = 1;\r\n");
        for (int i = 1; i <= count; i++) {
            String columnName = meta.getColumnName(i);
            columnName = GenerateHelper.getColumnName(columnName);
            sb.append("        result = prime * result + ((" + columnName + " == null) ? 0 : "
                    + columnName + ".hashCode());\r\n");
        }
        sb.append("        return result;\r\n");
        sb.append("    }\r\n");

        //生成equals
        sb.append("\r\n");
        sb.append("    @Override\r\n");
        sb.append("    public boolean equals(Object obj) {\r\n");
        sb.append("        if (this == obj)\r\n");
        sb.append("            return true;\r\n");
        sb.append("        if (obj == null)\r\n");
        sb.append("            return false;\r\n");
        sb.append("        " + modelClassName + " other = (" + modelClassName + ") obj;\r\n");
        for (int i = 1; i <= count; i++) {
            String columnName = meta.getColumnName(i);
            columnName = GenerateHelper.getColumnName(columnName);
            sb.append("        if (" + columnName + " == null) {\r\n");
            sb.append("            if(other." + columnName + " != null)\r\n");
            sb.append("                return false;\r\n");
            sb.append("        } else if (!" + columnName + ".equals(other." + columnName
                    + "))\r\n");
            sb.append("            return false;\r\n");
        }
        sb.append("        return true;\r\n");
        sb.append("    }\r\n");

        //生成toString
        sb.append("\r\n");
        sb.append("    @Override\r\n");
        sb.append("    public String toString() {\r\n");
        sb.append("        return \"" + modelClassName + "[");
        for (int i = 1; i <= count; i++) {
            String columnName = meta.getColumnName(i);
            columnName = GenerateHelper.getColumnName(columnName);
            if (i > 1) {
                sb.append(" + \", ");
            }
            sb.append(columnName + "=\" + " + columnName);
        }
        for (int i = 1; i <= count; i++) {
            String columnName = meta.getColumnName(i);
            columnName = GenerateHelper.getColumnName(columnName);
//            String hasserName = "has" + StringUtil.first2UpperCase(columnName);
//            sb.append(" + \", ");
//            sb.append(hasserName + "=\" + " + hasserName);
        }

        sb.append("+ \"]\";\r\n");
        sb.append("    }\r\n");

        sb.append("}\r\n");

        // System.out.println(sb.toString());

        writeFile.write(modelPackage, modelClassName, "java", sb.toString());
    }

    /** setter */
    public void setModelPackage(String modelPackage) {
        this.modelPackage = modelPackage;
    }
}
