package com.jd.tx.tcc.core.test.hsql;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;

/**
 * @author Leon Guo
 *         Creation Date: 2016/4/26
 */
public class TestTableManager {

    private static final Logger LOG = LoggerFactory.getLogger(TestTableManager.class);

    private static final String CREATE_TBL_SQL = "CREATE TABLE test_tcctx (\n" +
            "  id VARCHAR(32) NOT NULL,\n" +
            "  status INTEGER NOT NULL,\n" +
            "  process_msg VARCHAR(500),\n" +
            "  last_handle_time TIMESTAMP NOT NULL,\n" +
            "  PRIMARY KEY (id))";

    private static final String DROP_TBL_SQL = "DROP TABLE test_tcctx";

    public static void createTestTables() throws SQLException {
        HsqlDatabase.getInstance().executeSql(CREATE_TBL_SQL);
    }

    public static void dropTestTables() throws SQLException {
        HsqlDatabase.getInstance().executeSql(DROP_TBL_SQL);
    }

    public static String query(String id) throws SQLException {
        List<String> list = new ArrayList<>();
        try (Connection conn = HsqlDatabase.getInstance().getConnection(); Statement stat = conn.createStatement(); ) {
            String sql = "select * from test_tcctx where id = '" + id + "'";
            LOG.info("query sql: " + sql);
            ResultSet resultSet = stat.executeQuery(sql);
            while (resultSet.next()) {
                list.add(resultSet.getString(1));
                list.add(resultSet.getString(2));
                list.add(resultSet.getString(3));
                list.add(resultSet.getDate(4).toString());
            }
        }
        return list.toString();
    }

}
