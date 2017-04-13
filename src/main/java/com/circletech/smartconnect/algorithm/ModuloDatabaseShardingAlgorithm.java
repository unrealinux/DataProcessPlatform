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

//split database algorithm

import com.circletech.smartconnect.config.CustomConfig;
import com.dangdang.ddframe.rdb.sharding.api.ShardingValue;
import com.dangdang.ddframe.rdb.sharding.api.strategy.database.SingleKeyDatabaseShardingAlgorithm;
import com.google.common.collect.Range;

import java.util.Collection;
import java.util.LinkedHashSet;

public final class ModuloDatabaseShardingAlgorithm implements SingleKeyDatabaseShardingAlgorithm<Long> {

    private CustomConfig customConfig;

    public ModuloDatabaseShardingAlgorithm(CustomConfig customConfig) {
        this.customConfig = customConfig;
    }

    @Override
    public String doEqualSharding(final Collection<String> dataSourceNames, final ShardingValue<Long> shardingValue) {
        for (String each : dataSourceNames) {
            int shardingcode = customConfig.getDatasourcenum();
            if (each.endsWith(shardingValue.getValue() % shardingcode + "")) {
                return each;
            }
        }
        throw new IllegalArgumentException();
    }
    
    @Override
    public Collection<String> doInSharding(final Collection<String> dataSourceNames, final ShardingValue<Long> shardingValue) {
        Collection<String> result = new LinkedHashSet<>(dataSourceNames.size());
        for (Long value : shardingValue.getValues()) {
            for (String dataSourceName : dataSourceNames) {
                int shardingcode = customConfig.getDatasourcenum();
                if (dataSourceName.endsWith(value % shardingcode + "")) {
                    result.add(dataSourceName);
                }
            }
        }
        return result;
    }
    
    @Override
    public Collection<String> doBetweenSharding(final Collection<String> dataSourceNames, final ShardingValue<Long> shardingValue) {
        Collection<String> result = new LinkedHashSet<>(dataSourceNames.size());
        Range<Long> range = shardingValue.getValueRange();
        for (Long i = range.lowerEndpoint(); i <= range.upperEndpoint(); i++) {
            for (String each : dataSourceNames) {
                int shardingcode = customConfig.getDatasourcenum();
                if (each.endsWith(i % shardingcode + "")) {
                    result.add(each);
                }
            }
        }
        return result;
    }
}
