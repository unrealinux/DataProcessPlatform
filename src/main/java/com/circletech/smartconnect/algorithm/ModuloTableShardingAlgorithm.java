/*
 * Copyright 1999-2015 dangdang.com.
 * <p>
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 * </p>
 */

package com.circletech.smartconnect.algorithm;

//split table algorithm

import com.circletech.smartconnect.config.CustomConfig;
import com.dangdang.ddframe.rdb.sharding.api.ShardingValue;
import com.dangdang.ddframe.rdb.sharding.api.strategy.table.SingleKeyTableShardingAlgorithm;
import com.google.common.collect.Range;

import java.util.Collection;
import java.util.LinkedHashSet;

public final class ModuloTableShardingAlgorithm implements SingleKeyTableShardingAlgorithm<Long> {

    private CustomConfig customConfig;

    public ModuloTableShardingAlgorithm(CustomConfig customConfig) {
        this.customConfig = customConfig;
    }

    @Override
    public String doEqualSharding(final Collection<String> tableNames, final ShardingValue<Long> shardingValue) {
        for (String each : tableNames) {
            int shardingcode = customConfig.getDatasourcenum();
            if (each.endsWith(shardingValue.getValue() % shardingcode + "")) {
                return each;
            }
        }
        throw new UnsupportedOperationException();
    }
    
    @Override
    public Collection<String> doInSharding(final Collection<String> tableNames, final ShardingValue<Long> shardingValue) {
        Collection<String> result = new LinkedHashSet<>(tableNames.size());
        for (Long value : shardingValue.getValues()) {
            for (String tableName : tableNames) {
                int shardingcode = customConfig.getDatasourcenum();
                if (tableName.endsWith(value % shardingcode + "")) {
                    result.add(tableName);
                }
            }
        }
        return result;
    }
    
    @Override
    public Collection<String> doBetweenSharding(final Collection<String> tableNames, final ShardingValue<Long> shardingValue) {
        Collection<String> result = new LinkedHashSet<>(tableNames.size());
        Range<Long> range = shardingValue.getValueRange();
        for (Long i = range.lowerEndpoint(); i <= range.upperEndpoint(); i++) {
            for (String each : tableNames) {
                int shardingcode = customConfig.getDatasourcenum();
                if (each.endsWith(i % shardingcode + "")) {
                    result.add(each);
                }
            }
        }
        return result;
    }
}
