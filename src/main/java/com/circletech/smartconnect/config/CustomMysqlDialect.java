package com.circletech.smartconnect.config;

import org.hibernate.dialect.MySQL5InnoDBDialect;


/**
 *
 * Extends MySQL5InnoDBDialect and sets the default charset to be UTF-8
 */
public class CustomMysqlDialect extends MySQL5InnoDBDialect {
    public String getTableTypeString() {
        return " ENGINE=InnoDB DEFAULT CHARSET=utf8";
    }
}
