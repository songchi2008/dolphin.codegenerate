package com.wangjubao.dolphin.codegenerate;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.InputStreamReader;
import java.sql.ResultSetMetaData;
import java.sql.SQLException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import com.wangjubao.dolphin.codegenerate.util.StringUtil;
import org.springframework.beans.factory.annotation.Autowired;

public class DaoImplCreator {
    private String    daoPackage;
    private String    modelPackage;
    private String    pageListPackage;
    private String    pageListClassName;
    private String    pageQueryPackage;
    private String    pageQueryClassName;
    private String    searchMapClassName;
    private String    integerPackage;
    private String    integerClassName;
    private String    utilPackage;
    private String    listClassName;
    private String    collClassName;
    private String    paginatorPackage;
    private String    paginatorClassName;

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

    /** setter */
    public void setPageListPackage(String pageListPackage) {
        this.pageListPackage = pageListPackage;
    }

    /** setter */
    public void setPageListClassName(String pageListClassName) {
        this.pageListClassName = pageListClassName;
    }

    /** setter */
    public void setPageQueryPackage(String pageQueryPackage) {
        this.pageQueryPackage = pageQueryPackage;
    }

    /** setter */
    public void setPageQueryClassName(String pageQueryClassName) {
        this.pageQueryClassName = pageQueryClassName;
    }

    /** setter */
    public void setSearchMapClassName(String searchMapClassName) {
        this.searchMapClassName = searchMapClassName;
    }

    /** setter */
    public void setIntegerPackage(String integerPackage) {
        this.integerPackage = integerPackage;
    }

    /** setter */
    public void setIntegerClassName(String integerClassName) {
        this.integerClassName = integerClassName;
    }

    /** setter */
    public void setUtilPackage(String utilPackage) {
        this.utilPackage = utilPackage;
    }

    /** setter */
    public void setListClassName(String listClassName) {
        this.listClassName = listClassName;
    }

    /** setter */
    public void setCollClassName(String collClassName) {
        this.collClassName = collClassName;
    }

    /** setter */
    public void setPaginatorPackage(String paginatorPackage) {
        this.paginatorPackage = paginatorPackage;
    }

    /** setter */
    public void setPaginatorClassName(String paginatorClassName) {
        this.paginatorClassName = paginatorClassName;
    }

    public void create(ResultSetMetaData meta) {
       try{
           String tableName = meta.getTableName(1);
           String daoClassName = GenerateHelper.getDaoClassName(tableName);
           String lowerDaoClassName = StringUtil.first2LowerCase(daoClassName);
           String daoImplClassName = daoClassName + "Impl";
           String daoImplPackage = daoPackage + ".impl";
           String modelClassName = GenerateHelper.getModelClassName(tableName);
           String pkClassType = getPKType(meta);
           String pkName = meta.getColumnName(1);

           StringBuilder sb = new StringBuilder();

           File file  = new File("src/main/resources/tpl/daoImplTpl.txt");
           BufferedReader reader = new BufferedReader(new InputStreamReader(new FileInputStream(file)));
           String str = "";
           while((str =reader.readLine())!=null){
               sb.append(str);
               sb.append("\r\n");
           }
           String tpl = sb.toString();
           Map<String,String> map = new HashMap<>();
           map.put("tableName",tableName);
           map.put("daoClassName",daoClassName);
           map.put("lowerDaoClassName",lowerDaoClassName);
           map.put("daoImplClassName",daoImplClassName);
           map.put("daoImplPackage",daoImplPackage);
           map.put("modelClassName",modelClassName);
           map.put("pkClassType",pkClassType);
           map.put("pkName",pkName);
           String result = composeMessage(tpl,map);

           System.out.println(result);

           writeFile.write(daoImplPackage, daoImplClassName, "java", result);
       }catch (Exception e){
           e.printStackTrace();
       }

    }

    private String getPKType(ResultSetMetaData meta) throws SQLException {
        int count = meta.getColumnCount();
        for (int i = 1; i <= count; i++) {
            if (meta.getColumnName(i).equalsIgnoreCase("id")) {
                return GenerateHelper.getJavaType(meta.getColumnClassName(i));
            }
        }
        return meta.getColumnClassName(1);

        //        throw new RuntimeException("getPKType error, no column[id].");
    }
    
    public static void main(String[] args) {

	}

    public static String composeMessage(String template, Map data)
            throws Exception {
        String regex = "\\$\\{(.+?)\\}";
        Pattern pattern = Pattern.compile(regex);
        Matcher matcher = pattern.matcher(template);
        /*
         * sb用来存储替换过的内容，它会把多次处理过的字符串按源字符串序
         * 存储起来。
         */
        StringBuffer sb = new StringBuffer();
        while (matcher.find()) {
            String name = matcher.group(1);//键名
            String value = (String) data.get(name);//键值
            if (value == null) {
                value = "";
            } else {
                /*
                 * 由于$出现在replacement中时，表示对捕获组的反向引用，所以要对上面替换内容
                 * 中的 $ 进行替换，让它们变成 "\$1000.00" 或 "\$1000000000.00" ，这样
                 * 在下面使用 matcher.appendReplacement(sb, value) 进行替换时就不会把
                 * $1 看成是对组的反向引用了，否则会使用子匹配项值amount 或 balance替换 $1
                 * ，最后会得到错误结果：
                 *
                 * 尊敬的客户刘明你好！本次消费金额amount000.00，您帐户888888888上的余额
                 * 为balance000000.00，欢迎下次光临！
                 *
                 * 要把 $ 替换成 \$ ，则要使用 \\\\\\& 来替换，因为一个 \ 要使用 \\\ 来进
                 * 行替换，而一个 $ 要使用 \\$ 来进行替换，因 \ 与  $ 在作为替换内容时都属于
                 * 特殊字符：$ 字符表示反向引用组，而 \ 字符又是用来转义 $ 字符的。
                 */
                value = value.replaceAll("\\$", "\\\\\\$");
                //System.out.println("value=" + value);
            }
            /*
             * 经过上面的替换操作，现在的 value 中含有 $ 特殊字符的内容被换成了"\$1000.00"
             * 或 "\$1000000000.00" 了，最后得到下正确的结果：
             *
             * 尊敬的客户刘明你好！本次消费金额$1000.00，您帐户888888888上的
             * 余额为$1000000.00，欢迎下次光临！
             *
             * 另外，我们在这里使用Matcher对象的appendReplacement()方法来进行替换操作，而
             * 不是使用String对象的replaceAll()或replaceFirst()方法来进行替换操作，因为
             * 它们都能只能进行一次性简单的替换操作，而且只能替换成一样的内容，而这里则是要求每
             * 一个匹配式的替换值都不同，所以就只能在循环里使用appendReplacement方式来进行逐
             * 个替换了。
             */
            matcher.appendReplacement(sb, value);
            System.out.println("sb = " + sb.toString());
        }
        //最后还得要把尾串接到已替换的内容后面去，这里尾串为“，欢迎下次光临！”
        matcher.appendTail(sb);
        return sb.toString();
    }
}
