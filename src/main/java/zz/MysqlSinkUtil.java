package zz;

import java.sql.*;
import java.util.List;

/**
 * Created by guojiaozhen on 2017/8/28.
 */
public class MysqlSinkUtil {
    public static String getSqlReplace(String context, String table, String fields) {
        String[] s = context.split("\001", -1);
        String values = "";
        for (int i = 1; i < s.length; i++) {
            if (i == s.length - 1) {
                if (s[i].startsWith("\"") && s[i].endsWith("\"")) {
                    values += s[i];
                } else {
                    values += "\"" + s[i] + "\"";
                }

            } else {
                if (s[i].startsWith("\"") && s[i].endsWith("\"")) {
                    values += s[i] + ",";
                } else {
                    values += "\"" + s[i] + "\"" + ",";
                }

            }

        }
        return "replace into " + table + " (" + fields + ") values(" + values + ")";
    }

    public static String getSqlDelete(String context, String table, String fields) {
        String WHERE_SQL = " where ";
        String[] fs = fields.split(",");
        String[] vs = context.split("\001", -1);
        for (int i = 1; i < vs.length; i++) {
            if (i == vs.length - 1) {
                WHERE_SQL += fs[i - 1] + "=" + vs[i];
            } else {
                WHERE_SQL += fs[i - 1] + "=" + vs[i] + " and ";
            }
        }
        String sql = "delete from " + table + WHERE_SQL;
        return sql;
    }


    public static void toMysql(String hostname, String port, String databaseName, String user, String password, List<String> sqls) {
        Connection conn = null;
        Statement statement = null;
        try {
            Class.forName("com.mysql.jdbc.Driver");
            String url = "jdbc:mysql://" + hostname + ":" + port + "/" + databaseName + "?useUnicode=true&characterEncoding=UTF-8";
            conn = DriverManager.getConnection(url, user, password);
            statement = conn.createStatement();
            for (String sql : sqls) {
                statement.addBatch(sql);
            }
            statement.executeBatch();
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            if (null != statement) {
                try {
                    statement.close();
                } catch (SQLException e) {
                    e.printStackTrace();
                }
            }
            if (null != conn) {
                try {
                    conn.close();
                } catch (SQLException e) {
                    e.printStackTrace();
                }
            }
        }
    }
}
