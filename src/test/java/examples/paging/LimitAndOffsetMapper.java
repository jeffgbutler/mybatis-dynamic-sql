/*
 *    Copyright 2016-2025 the original author or authors.
 *
 *    Licensed under the Apache License, Version 2.0 (the "License");
 *    you may not use this file except in compliance with the License.
 *    You may obtain a copy of the License at
 *
 *       https://www.apache.org/licenses/LICENSE-2.0
 *
 *    Unless required by applicable law or agreed to in writing, software
 *    distributed under the License is distributed on an "AS IS" BASIS,
 *    WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *    See the License for the specific language governing permissions and
 *    limitations under the License.
 */
package examples.paging;

import static examples.animal.data.AnimalDataDynamicSqlSupport.*;

import java.util.List;

import examples.animal.data.AnimalData;
import org.apache.ibatis.annotations.Result;
import org.apache.ibatis.annotations.SelectProvider;
import org.mybatis.dynamic.sql.render.RenderingStrategies;
import org.mybatis.dynamic.sql.select.SelectDSL;
import org.mybatis.dynamic.sql.select.SelectDSLCompleter;
import org.mybatis.dynamic.sql.select.render.SelectStatementProvider;
import org.mybatis.dynamic.sql.util.SqlProviderAdapter;

public interface LimitAndOffsetMapper {

    @SelectProvider(type=SqlProviderAdapter.class, method="select")
    @Result(column="id", property="id", id=true)
    @Result(column="animal_name", property="animalName")
    @Result(column="brain_weight", property="brainWeight")
    @Result(column="body_weight", property="bodyWeight")
    List<AnimalData> selectMany(SelectStatementProvider selectStatement);

    default List<AnimalData> selectWithLimitAndOffset(int limit, int offset, SelectDSLCompleter completer) {
        var dslStart = SelectDSL.select(id, animalName, brainWeight, bodyWeight).from(animalData);
        var selectStatement = completer.apply(dslStart).build().render(RenderingStrategies.MYBATIS3);
        var decorator = new LimitAndOffsetDecorator(limit, offset, selectStatement);
        return selectMany(decorator);
    }
}
