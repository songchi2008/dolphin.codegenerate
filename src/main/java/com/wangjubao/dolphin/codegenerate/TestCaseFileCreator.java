package com.wangjubao.dolphin.codegenerate;

import com.wangjubao.dolphin.codegenerate.util.StringUtil;
import org.springframework.beans.factory.annotation.Autowired;

import java.sql.ResultSetMetaData;
import java.sql.SQLException;
import java.text.SimpleDateFormat;
import java.util.Date;

public class TestCaseFileCreator {
    private String    daoPackage;
    private String    modelPackage;
    @Autowired
    private WriteFile writeFile;

    /** setter */
    public void setDaoPackage(String daoPackage) {
        this.daoPackage = daoPackage;
    }

    /** setter */
    public void setModelPackage(String modelPackage) {
        this.modelPackage = modelPackage;
    }

    public void create(String table, ResultSetMetaData meta) throws SQLException {
        String className = GenerateHelper.getDaoClassName(table);
        String daoInstanceName = StringUtil.first2LowerCase(className);
        String modelClassName = GenerateHelper.getModelClassName(meta.getTableName(1));
        String pkClassType = GenerateHelper.getPKType(meta);
        String pkName = meta.getColumnName(1);

        StringBuilder sb = new StringBuilder("package ").append(daoPackage).append(".impl;\r\n");
        sb.append("\r\n");
        sb.append("import org.junit.After;\r\n");
        sb.append("import org.junit.Assert;\r\n");
        sb.append("import org.junit.Before;\r\n");
        sb.append("import org.junit.Test;\r\n");
        sb.append("import com.wangjubao.dolphin.biz.dao.AbstractWangjubaoTest;\r\n");
        sb.append("import " + modelPackage + "." + modelClassName + ";\r\n");
        sb.append("\r\n");
        sb.append("/**").append("\r\n");
        sb.append(" * @author WangJuBao created ")
                .append(new SimpleDateFormat("yyyy-MM-dd HH:mm:ss:sss").format(new Date()))
                .append("\r\n");
        sb.append(" * @explain -").append("\r\n");
        sb.append(" */").append("\r\n");
        sb.append("public class " + className + "ImplTest extends AbstractWangjubaoTest {\r\n");
        sb.append("    private " + pkClassType + " " + pkName).append("  =  5000000000L;\r\n");
        sb.append("    private " + pkClassType + " sellerId").append("  =  120055122L;\r\n");
        sb.append("\r\n");

        //@Before
        sb.append("    @Before\r\n");
        sb.append("    public void testSetup() {\r\n");
        sb.append("        " + modelClassName + " record = new " + modelClassName + "();\r\n");
        String getName = getGetName(pkName);
        sb.append("        record.set" + getName + "(" + pkName + ");\r\n");
        sb.append("        record.setSellerId(sellerId);\r\n");
        sb.append("        record = " + daoInstanceName + ".create(record);\r\n");
        sb.append("    }\r\n");

        //@After
        sb.append("    @After\r\n");
        sb.append("    public void testTeardown() {\r\n");
        sb.append("        " + daoInstanceName + ".delete(" + pkName + ",sellerId);\r\n");
        sb.append("    }\r\n");

        //load
        sb.append("    @Test\r\n");
        sb.append("    public void testLoad() {\r\n");
        sb.append("        " + modelClassName + " load = " + daoInstanceName + ".load(" + pkName
                + ",sellerId);\r\n");
        sb.append("        Assert.assertNotNull(load);\r\n");
        sb.append("        Assert.assertEquals(\"feiyingtest\", load.getXxxx());\r\n");
        sb.append("        logger.debug(load.toString());\r\n");
        sb.append("    }\r\n");

        //update
        sb.append("    @Test\r\n");
        sb.append("    public void testUpdate() {\r\n");
        sb.append("        " + modelClassName + " record = new " + modelClassName + "();\r\n");
        sb.append("        record.set" + getName + "(" + pkName + ");\r\n");
        sb.append("        record.setSellerId(sellerId);\r\n");
        sb.append("        record.setXxxx(\"hellofeiying\");\r\n");
        sb.append("        " + daoInstanceName + ".update(record);\r\n");
        sb.append("\r\n");
        sb.append("        " + modelClassName + " load = " + daoInstanceName + ".load(" + pkName
                + ",sellerId);\r\n");
        sb.append("        Assert.assertNotNull(load);\r\n");
        sb.append("        Assert.assertEquals(\"hellofeiying\", load.getXxxx());\r\n");
        sb.append("        logger.debug(load.toString());\r\n");
        sb.append("    }\r\n");

        //listByPage
        sb.append("    @Test\r\n");
        sb.append("    public void testListByPage() {\r\n");
        sb.append("        com.wangjubao.dolphin.biz.common.model.PageQuery pageQuery = new com.wangjubao.dolphin.biz.common.model.PageQuery(0,10);\r\n");
        sb.append("        Integer count = null;\r\n");
        sb.append("        com.wangjubao.dolphin.common.util.PageList<").append(modelClassName)
                .append("> result = ");
        sb.append("        " + daoInstanceName + ".listByPage(sellerId,pageQuery,count);\r\n");
        sb.append("        Assert.assertNotNull(result);\r\n");
        //        for (FeiyingTestDo bean : result) {
        //            logger.debug(bean.toString());
        //        }
        sb.append("          for (" + modelClassName + " bean : result) {\r\n");
        sb.append("              logger.debug(bean.toString());\r\n");
        sb.append("          }\r\n");
        sb.append("    }\r\n");

        sb.append("}\r\n");

        System.out.println(sb.toString());

        writeFile.write(daoPackage + ".impl", className + "ImplTest", "java", sb.toString());
    }

    private String getGetName(String pkName) {
        StringBuffer sBuffer = new StringBuffer(pkName.substring(0, 1).toUpperCase());
        sBuffer.append(pkName.substring(1));
        return sBuffer.toString();
    }
}
