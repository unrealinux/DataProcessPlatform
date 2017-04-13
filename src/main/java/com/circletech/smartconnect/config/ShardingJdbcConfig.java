package com.circletech.smartconnect.config;

import com.alibaba.druid.pool.DruidDataSource;
import com.circletech.smartconnect.algorithm.ModuloDatabaseShardingAlgorithm;
import com.circletech.smartconnect.algorithm.ModuloTableShardingAlgorithm;
import com.dangdang.ddframe.rdb.sharding.api.ShardingDataSourceFactory;
import com.dangdang.ddframe.rdb.sharding.api.rule.DataSourceRule;
import com.dangdang.ddframe.rdb.sharding.api.rule.ShardingRule;
import com.dangdang.ddframe.rdb.sharding.api.rule.TableRule;
import com.dangdang.ddframe.rdb.sharding.api.strategy.database.DatabaseShardingStrategy;
import com.dangdang.ddframe.rdb.sharding.api.strategy.table.TableShardingStrategy;
import com.google.common.reflect.TypeToken;
import com.google.gson.Gson;
import org.apache.commons.io.IOUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;
import org.springframework.core.io.Resource;

import javax.sql.DataSource;
import java.io.IOException;
import java.nio.charset.Charset;
import java.sql.SQLException;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;

/**
 * Created by xieyingfei on 2017/1/31.split database and table configuration
 */
@Configuration
public class ShardingJdbcConfig {

    @Value("${spring.datasource.filters}")
    private String druidFilters;

    @Value("${spring.datasource.driver-class-name}")
    private String driver;

    @Value("${ds0.datasource.url}")
    private String url;

    @Value("${ds0.datasource.username}")
    private String username;

    @Value("${ds0.datasource.password}")
    private String password;

    @Value("${ds1.datasource.url}")
    private String url1;

    @Value("${ds1.datasource.username}")
    private String username1;

    @Value("${ds1.datasource.password}")
    private String password1;

    @Value("classpath:database.json")
    private Resource databaseFile;

    @Value("${custom.sharddevicedistancetables}")
    private String deviceDistanceTables;

    @Value("${custom.sharddevicebaddistancetables}")
    private String deviceBadDistanceTables;

    @Value("${custom.sharddevicepositiontables}")
    private String devicePositionTables;

    @Value("${custom.sharddevicetransducerdatatables}")
    private String deviceTransducerdataTables;

    @Bean
    public List<Database> databases() throws IOException {
        String databasesString = IOUtils.toString(databaseFile.getInputStream(), Charset.forName("UTF-8"));
        List<Database> databases = new Gson().fromJson(databasesString, new TypeToken<List<Database>>() {
        }.getType());
        return databases;
    }

    @Bean
    public HashMap<String, DataSource> dataSourceMap(List<Database> databases) {
        HashMap<String, DataSource> dataSourceMap = new HashMap<>();
        for (Database database : databases) {

            DruidDataSource ds0druidDataSource = new DruidDataSource();
            ds0druidDataSource.setDriverClassName(database.getDriveClassName());
            ds0druidDataSource.setUrl(database.getUrl());
            ds0druidDataSource.setUsername(database.getUsername());
            ds0druidDataSource.setPassword(database.getPassword());
            try {
                ds0druidDataSource.setFilters(druidFilters);
            } catch (SQLException e) {
                e.printStackTrace();
            }
            dataSourceMap.put(database.getName(), ds0druidDataSource);
        }

        return dataSourceMap;
    }

    @Autowired
    private CustomConfig customConfig;

    @Bean
    @Primary
    public DataSource shardingDataSource(HashMap<String, DataSource> dataSourceHashMap) {

        DataSourceRule dataSourceRule = new DataSourceRule(dataSourceHashMap, "ds0");

        String[] distanceTables = deviceDistanceTables.split(";");
        TableRule deviceDistanceTableRule = TableRule.builder("device_distance").actualTables(Arrays.asList(distanceTables)).dataSourceRule(dataSourceRule)
                .databaseShardingStrategy(new DatabaseShardingStrategy("base_id", new ModuloDatabaseShardingAlgorithm(customConfig)))
                .tableShardingStrategy(new TableShardingStrategy("device_id", new ModuloTableShardingAlgorithm(customConfig)))
                .autoIncrementColumns("id")
                .build();

        String[] positionTables = devicePositionTables.split(";");
        TableRule devicePositionTableRule = TableRule.builder("device_position").actualTables(Arrays.asList(positionTables)).dataSourceRule(dataSourceRule)
                .databaseShardingStrategy(new DatabaseShardingStrategy("device_id", new ModuloDatabaseShardingAlgorithm(customConfig)))
                .tableShardingStrategy(new TableShardingStrategy("id", new ModuloTableShardingAlgorithm(customConfig)))
                .autoIncrementColumns("id")
                .build();

        String[] transducerdataTables = deviceTransducerdataTables.split(";");
        TableRule deviceTransducerDataTableRule = TableRule.builder("device_transducer_data").actualTables(Arrays.asList(transducerdataTables)).dataSourceRule(dataSourceRule)
                .databaseShardingStrategy(new DatabaseShardingStrategy("device_id", new ModuloDatabaseShardingAlgorithm(customConfig)))
                .tableShardingStrategy(new TableShardingStrategy("code", new ModuloTableShardingAlgorithm(customConfig)))
                .autoIncrementColumns("id")
                .build();

        String[] baddistanceTables = deviceBadDistanceTables.split(";");
        TableRule deviceBadDistanceTableRule = TableRule.builder("device_bad_distance").actualTables(Arrays.asList(baddistanceTables)).dataSourceRule(dataSourceRule)
                .databaseShardingStrategy(new DatabaseShardingStrategy("base_id", new ModuloDatabaseShardingAlgorithm(customConfig)))
                .tableShardingStrategy(new TableShardingStrategy("device_id", new ModuloTableShardingAlgorithm(customConfig)))
                .autoIncrementColumns("id")
                .build();

        ShardingRule shardingRule = ShardingRule.builder()
                .dataSourceRule(dataSourceRule)
                .tableRules(Arrays.asList(deviceDistanceTableRule, devicePositionTableRule, deviceTransducerDataTableRule, deviceBadDistanceTableRule))
                .idGenerator(com.dangdang.ddframe.rdb.sharding.id.generator.self.CommonSelfIdGenerator.class)
                .build();

        return ShardingDataSourceFactory.createDataSource(shardingRule);
    }
}
