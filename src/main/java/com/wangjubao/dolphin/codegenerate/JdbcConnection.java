package com.wangjubao.dolphin.codegenerate;

import java.sql.Connection;
import java.sql.DriverManager;

public class JdbcConnection {
    private String     url;
    private String     user;
    private String     password;
    private String     driverClass;

    private Connection connection;

    public Connection getConnection()
    {
        return connection;
    }

    public void init()
    {
        try
        {
            Class.forName(driverClass);
            connection = DriverManager.getConnection(url, user, password);
        } catch (final Throwable t)
        {
            t.printStackTrace();
            System.err.println(String.format("driverClass=%s url=%s user=%s password=%s", driverClass, url, user, password));
            throw new RuntimeException("", t);
        }
    }

    public void destroy()
    {
        if (connection == null)
        {
            return;
        }

        try
        {
            connection.close();
        } catch (Throwable t)
        {
            throw new RuntimeException("", t);
        }
    }

    /** setter */
    public void setUrl(String url)
    {
        this.url = url;
    }

    /** setter */
    public void setUser(String user)
    {
        this.user = user;
    }

    /** setter */
    public void setPassword(String password)
    {
        this.password = password;
    }

    /** setter */
    public void setDriverClass(String driverClass)
    {
        this.driverClass = driverClass;
    }
}
