package com.wangjubao.dolphin.codegenerate;

import com.wangjubao.dolphin.codegenerate.util.StringUtil;
import org.springframework.beans.factory.annotation.Autowired;

import java.sql.SQLException;
import java.util.List;

public class XmlFileCreator {
    private String    daoPackage;
    @Autowired
    private WriteFile writeFile;

    /** setter */
    public void setDaoPackage(String daoPackage) {
        this.daoPackage = daoPackage;
    }

    public void create(List<String> tables) throws SQLException {
        StringBuilder sb = new StringBuilder("\r\n");

        for (String table : tables) {
            String className = GenerateHelper.getDaoClassName(table);
            sb.append("    @Autowired\r\n");
            sb.append("    protected " + className + " " + StringUtil.first2LowerCase(className)
                    + ";\r\n");
        }

        sb.append("\r\n");
        sb.append("\r\n");

        for (String table : tables) {
            sb.append("    <sqlMap resource=\"ibatis/sqlmap/" + table + ".xml\" />\r\n");
        }

        sb.append("\r\n");
        sb.append("\r\n");

        for (String table : tables) {
            String className = GenerateHelper.getDaoClassName(table);
            sb.append("    <bean name=\"" + StringUtil.first2LowerCase(className) + "\" class=\""
                    + daoPackage + ".impl." + className + "Impl\"");
            sb.append(" parent=\"abstractDao\" />\r\n");
        }

        System.out.println(sb.toString());

        writeFile.write(daoPackage, "config", "xml", sb.toString());
    }
}
