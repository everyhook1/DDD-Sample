/**
 * @(#)TestExtract.java, 3月 02, 2021.
 * <p>
 * Copyright 2021 fenbi.com. All rights reserved.
 * FENBI.COM PROPRIETARY/CONFIDENTIAL. Use is subject to license terms.
 */
package org.example.cdc;

import com.zaxxer.hikari.HikariConfig;
import com.zaxxer.hikari.HikariDataSource;
import org.example.binlog.ColumnOrderExtractor;
import org.junit.jupiter.api.Test;

import java.sql.SQLException;
import java.util.Properties;

/**
 * @author liubin01
 */
public class TestExtract {

    @Test
    public void testDataSource() throws SQLException {
        Properties config = new Properties();

        config.setProperty("jdbcUrl", config.getProperty("jdbcUrl", "jdbc:mysql://localhost/eventuate"));
        config.setProperty("driverClassName", config.getProperty("driverClassName", "com.mysql.cj.jdbc.Driver"));
        config.setProperty("username", config.getProperty("username", "mysqluser"));
        config.setProperty("password", config.getProperty("password", "mysqlpw"));
        config.setProperty("initializationFailTimeout", String.valueOf(Long.MAX_VALUE));
        config.setProperty("connectionTestQuery", "select 1");

        HikariDataSource hikariDataSource = new HikariDataSource(new HikariConfig(config));
        ColumnOrderExtractor extractor = new ColumnOrderExtractor(hikariDataSource);
        SchemaAndTable schemaAndTable = new SchemaAndTable("eventuate", "cdc_monitoring");
        System.out.println(extractor.extractColumnOrders(schemaAndTable));
    }
}
