package com.wangjubao.dolphin.codegenerate;

import org.springframework.context.ApplicationContext;
import org.springframework.context.support.ClassPathXmlApplicationContext;

public class Main {

    public static void main(final String[] args) throws Exception
    {
        ApplicationContext context = new ClassPathXmlApplicationContext("application-context-codegenerate.xml");
        CodeMain codeMain = context.getBean(CodeMain.class);

        codeMain.execute();
        //        codeMain.listTables();
        //        codeMain.showCreateTables();
        //        codeMain.printPKsql();
        //        codeMain.printSql();
        System.out.println("done....");
    }
}
