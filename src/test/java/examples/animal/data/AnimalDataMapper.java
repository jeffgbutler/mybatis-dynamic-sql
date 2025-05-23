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
package examples.animal.data;

import java.util.List;

import org.apache.ibatis.annotations.Arg;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;
import org.apache.ibatis.annotations.SelectProvider;
import org.apache.ibatis.session.RowBounds;
import org.mybatis.dynamic.sql.select.render.SelectStatementProvider;
import org.mybatis.dynamic.sql.util.SqlProviderAdapter;
import org.mybatis.dynamic.sql.util.mybatis3.CommonDeleteMapper;
import org.mybatis.dynamic.sql.util.mybatis3.CommonInsertMapper;
import org.mybatis.dynamic.sql.util.mybatis3.CommonUpdateMapper;
import org.mybatis.dynamic.sql.where.render.WhereClauseProvider;

public interface AnimalDataMapper extends CommonDeleteMapper, CommonInsertMapper<AnimalData>, CommonUpdateMapper {

    @SelectProvider(type=SqlProviderAdapter.class, method="select")
    @Arg(column = "id", javaType = int.class, id = true)
    @Arg(column = "animal_name", javaType = String.class)
    @Arg(column = "brain_weight", javaType = double.class)
    @Arg(column = "body_weight", javaType = double.class)
    List<AnimalData> selectMany(SelectStatementProvider selectStatement);

    @SelectProvider(type = SqlProviderAdapter.class, method = "select")
    @Arg(column = "id", javaType = int.class, id = true)
    @Arg(column = "animal_name", javaType = String.class)
    @Arg(column = "brain_weight", javaType = double.class)
    @Arg(column = "body_weight", javaType = double.class)
    List<AnimalData> selectManyWithRowBounds(SelectStatementProvider selectStatement, RowBounds rowBounds);

    @SelectProvider(type = SqlProviderAdapter.class, method = "select")
    @Arg(column = "id", javaType = int.class, id = true)
    @Arg(column = "animal_name", javaType = String.class)
    @Arg(column = "brain_weight", javaType = double.class)
    @Arg(column = "body_weight", javaType = double.class)
    AnimalData selectOne(SelectStatementProvider selectStatement);

    @Select({
        "select id, animal_name, brain_weight, body_weight",
        "from AnimalData",
        "${whereClause}"
    })
    @Arg(column = "id", javaType = int.class, id = true)
    @Arg(column = "animal_name", javaType = String.class)
    @Arg(column = "brain_weight", javaType = double.class)
    @Arg(column = "body_weight", javaType = double.class)
    List<AnimalData> selectWithWhereClause(WhereClauseProvider whereClause);

    @Select({
        "select a.id, a.animal_name, a.brain_weight, a.body_weight",
        "from AnimalData a",
        "${whereClause}"
    })
    @Arg(column = "id", javaType = int.class, id = true)
    @Arg(column = "animal_name", javaType = String.class)
    @Arg(column = "brain_weight", javaType = double.class)
    @Arg(column = "body_weight", javaType = double.class)
    List<AnimalData> selectWithWhereClauseAndAlias(WhereClauseProvider whereClause);

    @Select({
        "select id, animal_name, brain_weight, body_weight",
        "from AnimalData",
        "${whereClauseProvider.whereClause}",
        "order by id",
        "OFFSET #{offset,jdbcType=INTEGER} LIMIT #{limit,jdbcType=INTEGER}"
    })
    @Arg(column = "id", javaType = int.class, id = true)
    @Arg(column = "animal_name", javaType = String.class)
    @Arg(column = "brain_weight", javaType = double.class)
    @Arg(column = "body_weight", javaType = double.class)
    List<AnimalData> selectWithWhereClauseLimitAndOffset(@Param("whereClauseProvider") WhereClauseProvider whereClause,
            @Param("limit") int limit, @Param("offset") int offset);

    @Select({
        "select b.id, b.animal_name, b.brain_weight, b.body_weight",
        "from AnimalData b",
        "${whereClauseProvider.whereClause}",
        "order by id",
        "OFFSET #{offset,jdbcType=INTEGER} LIMIT #{limit,jdbcType=INTEGER}"
    })
    @Arg(column = "id", javaType = int.class, id = true)
    @Arg(column = "animal_name", javaType = String.class)
    @Arg(column = "brain_weight", javaType = double.class)
    @Arg(column = "body_weight", javaType = double.class)
    List<AnimalData> selectWithWhereClauseAliasLimitAndOffset(@Param("whereClauseProvider") WhereClauseProvider whereClause,
                                                              @Param("limit") int limit, @Param("offset") int offset);
}
