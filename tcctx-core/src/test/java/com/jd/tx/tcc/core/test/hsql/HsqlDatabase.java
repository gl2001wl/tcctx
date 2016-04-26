package com.jd.tx.tcc.core.test.hsql;

import lombok.Getter;
import org.hsqldb.jdbc.JDBCDataSource;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.sql.Connection;
import java.sql.SQLException;
import java.sql.Statement;

/**
 * @author Leon Guo
 *         Creation Date: 2016/4/15
 */
public class HsqlDatabase {

    private static final String CONNECTION_STRING = "jdbc:hsqldb:mem:testdb;shutdown=false";
    private static final String USER_NAME = "SA";
    private static final String PASSWORD = "";
    private static final Logger LOG = LoggerFactory.getLogger(HsqlDatabase.class);

    @Getter
    private JDBCDataSource dataSource;

    private HsqlDatabase() {}

    private static HsqlDatabase database = new HsqlDatabase();

    static {
        try {
            database.init();
        } catch (SQLException e) {
            LOG.error(e.getMessage(), e);
        }
    }

    public static HsqlDatabase getInstance() {
        return database;
    }

    public void init() throws SQLException {
        dataSource = new JDBCDataSource();
        dataSource.setUrl(CONNECTION_STRING);
        dataSource.setUser(USER_NAME);
        dataSource.setPassword(PASSWORD);
    }

    public void executeSql(String sql) throws SQLException {
        try (Connection conn = HsqlDatabase.database.getConnection(); Statement stat =  conn.createStatement();) {
            stat.execute(sql);
            conn.commit();
        } catch (SQLException e) {
            LOG.error(e.getMessage(), e);
            throw e;
        }
    }

    public void close() throws SQLException {
        try {
            if (dataSource != null && dataSource.getConnection() != null) {
                dataSource.getConnection().close();
            }
        } catch (SQLException e) {
            LOG.error(e.getMessage(), e);
            throw e;
        }
    }

    public Connection getConnection() throws SQLException {
        return dataSource.getConnection();
    }

}
