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
package examples.simple;

import static examples.simple.AddressDynamicSqlSupport.address;
import static examples.simple.PersonDynamicSqlSupport.birthDate;
import static examples.simple.PersonDynamicSqlSupport.employed;
import static examples.simple.PersonDynamicSqlSupport.firstName;
import static examples.simple.PersonDynamicSqlSupport.id;
import static examples.simple.PersonDynamicSqlSupport.lastName;
import static examples.simple.PersonDynamicSqlSupport.occupation;
import static examples.simple.PersonDynamicSqlSupport.person;
import static org.mybatis.dynamic.sql.SqlBuilder.*;

import java.util.List;
import java.util.Optional;

import org.apache.ibatis.annotations.Arg;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.ResultMap;
import org.apache.ibatis.annotations.Results;
import org.apache.ibatis.annotations.Select;
import org.apache.ibatis.annotations.SelectProvider;
import org.apache.ibatis.type.EnumOrdinalTypeHandler;
import org.apache.ibatis.type.JdbcType;
import org.mybatis.dynamic.sql.BasicColumn;
import org.mybatis.dynamic.sql.SqlBuilder;
import org.mybatis.dynamic.sql.select.CountDSL;
import org.mybatis.dynamic.sql.select.CountDSLCompleter;
import org.mybatis.dynamic.sql.select.QueryExpressionDSL;
import org.mybatis.dynamic.sql.select.SelectDSLCompleter;
import org.mybatis.dynamic.sql.select.SelectModel;
import org.mybatis.dynamic.sql.select.render.SelectStatementProvider;
import org.mybatis.dynamic.sql.util.SqlProviderAdapter;
import org.mybatis.dynamic.sql.util.mybatis3.CommonCountMapper;
import org.mybatis.dynamic.sql.util.mybatis3.MyBatis3Utils;

/**
 * This is a mapper that shows coding a join for a composed record.
 *
 * <p><code>PersonWithAddress</code> is a Java record that uses composition - it holds an instance of
 * another record class (<code>AddressRecord</code>). This mapper requires MyBatis 3.6.0 or later to function properly.
 */
@Mapper
public interface PersonWithAddressMapper extends CommonCountMapper {

    @SelectProvider(type=SqlProviderAdapter.class, method="select")
    @Results(id = "PersonWithAddressResult")
    @Arg(column = "A_ID", jdbcType=JdbcType.INTEGER, id=true)
    @Arg(column = "first_name", jdbcType=JdbcType.VARCHAR)
    @Arg(column = "last_name", jdbcType=JdbcType.VARCHAR, typeHandler=LastNameTypeHandler.class)
    @Arg(column = "birth_date", jdbcType=JdbcType.DATE)
    @Arg(column =  "employed", jdbcType=JdbcType.VARCHAR, typeHandler=YesNoTypeHandler.class)
    @Arg(column = "occupation", jdbcType=JdbcType.VARCHAR)
    @Arg(resultMap = "AddressResult")
    List<PersonWithAddress> selectMany(SelectStatementProvider selectStatement);

    @Results(id = "AddressResult")
    @Arg(column = "address_id", jdbcType=JdbcType.INTEGER)
    @Arg(column = "street_address", jdbcType=JdbcType.VARCHAR)
    @Arg(column = "city", jdbcType=JdbcType.VARCHAR)
    @Arg(column = "state", jdbcType=JdbcType.CHAR)
    @Arg(column = "address_type", jdbcType=JdbcType.INTEGER, javaType = AddressRecord.AddressType.class,
            typeHandler = EnumOrdinalTypeHandler.class)
    @Select("-- dummy statement: result map holder only")
    @SuppressWarnings("unused")
    AddressRecord addressResult();

    @SelectProvider(type=SqlProviderAdapter.class, method="select")
    @ResultMap("PersonWithAddressResult")
    Optional<PersonWithAddress> selectOne(SelectStatementProvider selectStatement);

    BasicColumn[] selectList =
            BasicColumn.columnList(id.as("A_ID"), firstName, lastName, birthDate, employed, occupation, address.id,
                    address.streetAddress, address.city, address.state, address.addressType);

    default Optional<PersonWithAddress> selectOne(SelectDSLCompleter completer) {
        QueryExpressionDSL<SelectModel> start = SqlBuilder.select(selectList).from(person)
                .join(address, on(person.addressId, isEqualTo(address.id)));
        return MyBatis3Utils.selectOne(this::selectOne, start, completer);
    }

    default List<PersonWithAddress> select(SelectDSLCompleter completer) {
        QueryExpressionDSL<SelectModel> start = SqlBuilder.select(selectList).from(person)
                .join(address, on(person.addressId, isEqualTo(address.id)));
        return MyBatis3Utils.selectList(this::selectMany, start, completer);
    }

    default Optional<PersonWithAddress> selectByPrimaryKey(Integer recordId) {
        return selectOne(c ->
            c.where(id, isEqualTo(recordId))
        );
    }

    default long count(CountDSLCompleter completer) {
        CountDSL<SelectModel> start = countFrom(person)
                .join(address, on(person.addressId, isEqualTo(address.id)));
        return MyBatis3Utils.countFrom(this::count, start, completer);
    }
}
