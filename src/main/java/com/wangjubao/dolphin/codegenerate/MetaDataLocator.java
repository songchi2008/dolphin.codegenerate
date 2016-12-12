package com.wangjubao.dolphin.codegenerate;

import java.sql.*;

public class MetaDataLocator {
    public ResultSetMetaData retrieveResultSetMetaData(Connection conn, String table) {
        ResultSetMetaData metaData = null;
        String sql = "SELECT * FROM " + table + " limit 1";
        try {
            PreparedStatement pstmt = conn.prepareStatement(sql);
            ResultSet rs = pstmt.executeQuery();

            metaData = rs.getMetaData();

            rs.close();
            pstmt.close();
 
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return metaData;
    }
}
