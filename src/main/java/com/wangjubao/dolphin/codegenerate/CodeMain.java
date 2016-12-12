package com.wangjubao.dolphin.codegenerate;

import java.sql.Connection;
import java.sql.DatabaseMetaData;
import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;

public class CodeMain {
    @Autowired
    private JdbcConnection      jdbcConnection;
    @Autowired
    private MetaDataLocator     metaDataLocator;
    @Autowired
    private ModelCreator        modelCreator;
    @Autowired
    private DaoCreator          daoCreator;
    @Autowired
    private DaoImplCreator      daoImplCreator;
    @Autowired
    private SqlmapCreator       sqlmapCreator;
    @Autowired
    private XmlFileCreator      xmlFileCreator;
    @Autowired
    private TestCaseFileCreator testCaseFileCreator;
    @Autowired
    private WriteFile           writeFile;

    //注入
    private List<String>        tables;
    private boolean             needXmlFile;
    private boolean             needModel;
    private boolean             needDao;
    private boolean             needDaoImpl;
    private boolean             needSqlmap;
    private boolean             needTestCaseFile;

    public void execute() throws SQLException
    {
        for (String table : tables)
        {
            execute(table);
        }

        if (needXmlFile)
        {
            xmlFileCreator.create(tables);
        }
    }

    public void listTables() throws SQLException
    {
        StringBuffer sb = new StringBuffer();
        List<String> tableList = new ArrayList<String>();
        ResultSet rs = null;
        Connection connection = jdbcConnection.getConnection();
        DatabaseMetaData dbmd = connection.getMetaData();

        String databaseName = dbmd.getDatabaseProductName(); //获取数据库名称
        String databaseVersion = connection.getCatalog(); //获取数据库版本号
        StringBuffer fileName = new StringBuffer(databaseName);
        fileName.append("-").append(databaseVersion);

        String[] typeList = new String[] { "TABLE" };
        rs = dbmd.getTables(null, null, null, typeList);
        for (boolean more = rs.next(); more; more = rs.next())
        {
            String tableName = rs.getString("TABLE_NAME");
            String type = rs.getString("TABLE_TYPE");
            //            System.out.println(tableName + "  " + type);
            if (type.equalsIgnoreCase("table") && tableName.indexOf("$") == -1 && tableName.startsWith("t_"))
                tableList.add(tableName);
        }

        System.out.println(">>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>");
        for (String name : tableList)
        {
            //            System.out.println(name);
            ResultSetMetaData metaData = metaDataLocator.retrieveResultSetMetaData(connection, name);
            ResultSet pkRSet = dbmd.getPrimaryKeys(null, null, name);
            int pkIndex = 1;
            String pkName = "id";
            String pkClass = "";
            boolean isAutoInc = metaData.isAutoIncrement(pkIndex);
            while (pkRSet.next())
            {
                pkName = pkRSet.getString(4);
                pkIndex = pkRSet.getInt(5);
                //                System.out.println("****** Comment ******");
                //                System.out.println("TABLE_CAT : " + pkRSet.getObject(1));
                //                System.out.println("TABLE_SCHEM: " + pkRSet.getObject(2));
                //                System.out.println("TABLE_NAME : " + pkRSet.getObject(3));
                //                System.out.println("COLUMN_NAME: " + pkRSet.getObject(4));
                //                System.out.println("KEY_SEQ : " + pkRSet.getObject(5));
                //                System.out.println("PK_NAME : " + pkRSet.getObject(6));
                //                System.out.println("****** ******* ******");
            }

            pkClass = metaData.getColumnClassName(pkIndex);
            System.out.println(String.format("[%s-%s-%s-%s-%s]", name, pkIndex, pkName, isAutoInc, pkClass));
            if (isAutoInc)
            {
                sb.append("ALTER TABLE `").append(name).append("` modify column `").append(pkName)
                        .append("`  bigint(20) unsigned NOT NULL;");
                sb.append("\r\n");
            }
            boolean modIndex = Boolean.FALSE;
            if (!hasField(metaData, "gmt_modified"))
            {
                sb.append("ALTER TABLE `").append(name).append("` ADD column gmt_modified datetime DEFAULT NOW();");
                sb.append("\r\n");
                modIndex = Boolean.TRUE;
            }
            if (!hasField(metaData, "gmt_create") && !hasField(metaData, "gmt_created"))
            {
                sb.append("ALTER TABLE `").append(name).append("` ADD column gmt_create datetime DEFAULT NULL;");
                sb.append("\r\n");
            }

            boolean sellerIdIndex = Boolean.FALSE;
            boolean modifiedIndex = Boolean.FALSE;

            if (!modIndex)
            {

                Statement statement = connection.createStatement();
                ResultSet resultSet = statement.executeQuery("SHOW INDEX FROM t_trade;");

                while (resultSet.next())
                {
                    String indexName = resultSet.getString(5);
                    if (indexName.equalsIgnoreCase("gmt_modified"))
                    {
                        modifiedIndex = Boolean.TRUE;
                    }
                    if (indexName.equalsIgnoreCase("sellerId"))
                    {
                        sellerIdIndex = Boolean.TRUE;
                    }
                    //                System.out.println(resultSet.getString(0));
                    //                System.out.println(resultSet.getString(1));
                    //                System.out.println(resultSet.getString(2));
                    //                System.out.println(resultSet.getString(3));
                    //                System.out.println(resultSet.getString(4));
                    //                System.out.println(resultSet.getString(5));
                    //                System.out.println(resultSet.getString(6));
                    //                System.out.println(resultSet.getString(7));
                }
            }

            if (!modifiedIndex && !sellerIdIndex)
            {
                //                ALTER TABLE `table_name` ADD INDEX index_name ( `column` )
                sb.append("ALTER TABLE `").append(name).append("` ADD INDEX sid_gmtmodified(`sellerId`,`gmt_modified`);");
                sb.append("\r\n");
            } else if (!modifiedIndex && sellerIdIndex)
            {
                sb.append("ALTER TABLE `").append(name).append("` ADD INDEX gmtmodified(`gmt_modified`);");
                sb.append("\r\n");
            }

        }
        connection.close();
        System.out.println(sb.toString());
        writeFile.write("sqls", fileName.toString(), "sql", sb.toString());
        System.out.println("<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<");
    }

    public void showCreateTables() throws Exception
    {
        StringBuilder sb = new StringBuilder();
        sb.append("\r\n");
        for (String table : tables)
        {
            showCreateTables(sb, table);
        }
    }

    private void showCreateTables(StringBuilder sb, String table) throws Exception
    {
        String sql = "show create table " + table;
        Connection connection = jdbcConnection.getConnection();
        Statement statement = connection.createStatement();
        ResultSet resultSet = null;
        resultSet = statement.executeQuery(sql);
        while (resultSet.next())
        {
            sb.append("-- " + resultSet.getObject(1));
            sb.append("\r\n");
            sb.append(resultSet.getObject(2));
            sb.append("\r\n");
        }
        System.out.println(sb.toString());
        writeFile.write("sqls", "showCreateTables", "sql", sb.toString());
    }

    public void printPKsql() throws SQLException
    {
        StringBuilder sb = new StringBuilder();
        StringBuilder sb2 = new StringBuilder();
        Connection connection = jdbcConnection.getConnection();
        DatabaseMetaData dbmd = connection.getMetaData();

        for (String table : tables)
        {
            ResultSetMetaData metaData = metaDataLocator.retrieveResultSetMetaData(connection, table);
            if (!hasField(metaData, "gmt_modified"))
            {
                sb2.append("ALTER TABLE `").append(table).append("` ADD column gmt_modified datetime DEFAULT '2014-08-08 08:08:08';");
                sb2.append("\r\n");
                //                sb2.append("UPDATE ").append(table).append(" SET gmt_modified = now(); ");
                //                sb2.append("\r\n");
            }
            if (!hasField(metaData, "gmt_create") && !hasField(metaData, "gmt_created"))
            {
                sb2.append("ALTER TABLE `").append(table).append("` ADD column gmt_create datetime DEFAULT  '2014-08-08 08:08:08';");
                sb2.append("\r\n");
                //                sb2.append("UPDATE ").append(table).append(" SET gmt_create = now(); ");
                //                sb2.append("\r\n");
            }
            ResultSet pkRSet = dbmd.getPrimaryKeys(null, null, table);
            int pkIndex = 1;
            String pkName = "id";
            String pkClass = "";
            boolean isAutoInc = metaData.isAutoIncrement(pkIndex);
            while (pkRSet.next())
            {
                pkName = pkRSet.getString(4);
                pkIndex = pkRSet.getInt(5);
                //                System.out.println(table + "\t   " + pkName + "\t          " + pkIndex + "\t         "
                //                        + metaData.getColumnClassName(pkIndex) + "\t " + isAutoInc);
                sb.append(" UPDATE ").append(table).append(" SET ").append(pkName).append(" = ").append(pkName)
                        .append(" + 1000000000  where ");
                if (hasField(metaData, "gmt_created"))
                {
                    sb.append(" gmt_created ");
                } else
                {
                    sb.append(" gmt_create ");
                }
                sb.append("  < '2014-08-09 00:00:00' ; \t\r\n");
                break;
            }

        }
        System.out.println(sb2.toString());
        System.out.println(sb.toString());
    }

    public void printSql()
    {
        StringBuilder sb = new StringBuilder();
        for (String table : tables)
        {
            sb.append(" DELETE FROM ").append(table).append(" WHERE sellerId IS NULL ; ");
            sb.append("\t\r\n");
        }
        System.out.println(sb.toString());
    }

    private boolean hasField(ResultSetMetaData metaData, String column) throws SQLException
    {
        int count = metaData.getColumnCount();
        for (int i = 1; i <= count; i++)
        {
            if (metaData.getColumnName(i).equals(column))
            {
                return Boolean.TRUE;
            }
        }
        return false;
    }

    public void execute(String tableName) throws SQLException
    {
        Connection connection = jdbcConnection.getConnection();

        ResultSetMetaData metaData = metaDataLocator.retrieveResultSetMetaData(connection, tableName);

        if (needModel)
        {
            modelCreator.create(metaData);
        }

        if (needDao)
        {
            daoCreator.create(metaData);
        }

        if (needDaoImpl)
        {
            daoImplCreator.create(metaData);
        }

        if (needSqlmap)
        {
            sqlmapCreator.create(metaData);
        }

        if (needTestCaseFile)
        {
            testCaseFileCreator.create(tableName, metaData);
        }
    }

    /** setter */
    public void setTables(List<String> tables)
    {
        this.tables = tables;
    }

    /** setter */
    public void setSqlmapCreator(SqlmapCreator sqlmapCreator)
    {
        this.sqlmapCreator = sqlmapCreator;
    }

    /** setter */
    public void setNeedXmlFile(boolean needXmlFile)
    {
        this.needXmlFile = needXmlFile;
    }

    /** setter */
    public void setNeedModel(boolean needModel)
    {
        this.needModel = needModel;
    }

    /** setter */
    public void setNeedDao(boolean needDao)
    {
        this.needDao = needDao;
    }

    /** setter */
    public void setNeedDaoImpl(boolean needDaoImpl)
    {
        this.needDaoImpl = needDaoImpl;
    }

    /** setter */
    public void setNeedSqlmap(boolean needSqlmap)
    {
        this.needSqlmap = needSqlmap;
    }

    /** setter */
    public void setNeedTestCaseFile(boolean needTestCaseFile)
    {
        this.needTestCaseFile = needTestCaseFile;
    }

}
