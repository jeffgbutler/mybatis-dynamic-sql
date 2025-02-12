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
import org.apache.ibatis.type.EnumOrdinalTypeHandler
import org.apache.ibatis.type.JdbcType
import org.mybatis.dynamic.sql.select.render.SelectStatementProvider
import org.mybatis.dynamic.sql.util.SqlProviderAdapter

/**
 *
 * This is a mapper that shows coding a join
 *
 */
@Mapper
interface PersonWithAddressMapper {

    @SelectProvider(type = SqlProviderAdapter::class, method = "select")
    @Results(
        id = "PersonWithAddressResult",
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
            Result(column = "address_id", property = "address.id", jdbcType = JdbcType.INTEGER),
            Result(column = "street_address", property = "address.streetAddress", jdbcType = JdbcType.VARCHAR),
            Result(column = "city", property = "address.city", jdbcType = JdbcType.VARCHAR),
            Result(column = "state", property = "address.state", jdbcType = JdbcType.CHAR),
            Result(
                column = "address_type",
                property = "address.addressType",
                jdbcType = JdbcType.INTEGER,
                typeHandler = EnumOrdinalTypeHandler::class
            )
        ]
    )
    fun selectMany(selectStatement: SelectStatementProvider): List<PersonWithAddress>

    @SelectProvider(type = SqlProviderAdapter::class, method = "select")
    @ResultMap("PersonWithAddressResult")
    fun selectOne(selectStatement: SelectStatementProvider): PersonWithAddress?
}
