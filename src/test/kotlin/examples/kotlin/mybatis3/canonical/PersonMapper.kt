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
package examples.kotlin.mybatis3.canonical

import org.apache.ibatis.annotations.Mapper
import org.apache.ibatis.annotations.Result
import org.apache.ibatis.annotations.ResultMap
import org.apache.ibatis.annotations.Results
import org.apache.ibatis.annotations.SelectProvider
import org.apache.ibatis.type.JdbcType
import org.mybatis.dynamic.sql.select.render.SelectStatementProvider
import org.mybatis.dynamic.sql.util.SqlProviderAdapter
import org.mybatis.dynamic.sql.util.mybatis3.CommonCountMapper
import org.mybatis.dynamic.sql.util.mybatis3.CommonDeleteMapper
import org.mybatis.dynamic.sql.util.mybatis3.CommonInsertMapper
import org.mybatis.dynamic.sql.util.mybatis3.CommonUpdateMapper

/**
 *
 * Note: this is the canonical mapper with the new style methods
 * and represents the desired output for MyBatis Generator
 *
 */
@Mapper
interface PersonMapper : CommonCountMapper, CommonDeleteMapper, CommonInsertMapper<PersonRecord>, CommonUpdateMapper {

    @SelectProvider(type = SqlProviderAdapter::class, method = "select")
    @Results(
        id = "PersonResult",
        value = [
            Result(column = "A_ID", property = "id", jdbcType = JdbcType.INTEGER, id = true),
            Result(column = "first_name", property = "firstName", jdbcType = JdbcType.VARCHAR),
            Result(
                column = "last_name",
                property = "lastName",
                jdbcType = JdbcType.VARCHAR,
                typeHandler = LastNameTypeHandler::class
            ),
            Result(column = "birth_date", property = "birthDate", jdbcType = JdbcType.DATE),
            Result(
                column = "employed",
                property = "employed",
                jdbcType = JdbcType.VARCHAR,
                typeHandler = YesNoTypeHandler::class
            ),
            Result(column = "occupation", property = "occupation", jdbcType = JdbcType.VARCHAR),
            Result(column = "address_id", property = "addressId", jdbcType = JdbcType.INTEGER)
        ]
    )
    fun selectMany(selectStatement: SelectStatementProvider): List<PersonRecord>

    @SelectProvider(type = SqlProviderAdapter::class, method = "select")
    @ResultMap("PersonResult")
    fun selectOne(selectStatement: SelectStatementProvider): PersonRecord?
}
