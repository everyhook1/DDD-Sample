/**
 * @(#)CdcStorage.java, 2月 03, 2021.
 * <p>
 * Copyright 2021 fenbi.com. All rights reserved.
 * FENBI.COM PROPRIETARY/CONFIDENTIAL. Use is subject to license terms.
 */
package org.example.service;

import lombok.Data;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;
import org.springframework.stereotype.Repository;

import java.util.List;

/**
 * @author liubin01
 */
@Slf4j
@Repository
public class CdcStorage {

    @Autowired
    private NamedParameterJdbcTemplate jdbcTemplate;

    @Data
    class A {
        String reader_id;
        Long last_time;
    }

    public List<A> getCdcAll() {
        return jdbcTemplate.query("select * from cdc_monitoring", (resultSet, i) -> {
            A a = new A();
            a.setReader_id(resultSet.getString("reader_id"));
            a.setLast_time(resultSet.getLong("last_time"));
            return a;
        });
    }
}
