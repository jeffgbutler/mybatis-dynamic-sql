/*
 *    Copyright 2016-2023 the original author or authors.
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

import static examples.animal.data.AnimalDataDynamicSqlSupport.animalData;
import static examples.animal.data.AnimalDataDynamicSqlSupport.animalName;
import static examples.animal.data.AnimalDataDynamicSqlSupport.bodyWeight;
import static examples.animal.data.AnimalDataDynamicSqlSupport.brainWeight;
import static examples.animal.data.AnimalDataDynamicSqlSupport.id;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatExceptionOfType;
import static org.assertj.core.api.Assertions.fail;
import static org.mybatis.dynamic.sql.SqlBuilder.and;
import static org.mybatis.dynamic.sql.SqlBuilder.deleteFrom;
import static org.mybatis.dynamic.sql.SqlBuilder.insert;
import static org.mybatis.dynamic.sql.SqlBuilder.insertBatch;
import static org.mybatis.dynamic.sql.SqlBuilder.insertInto;
import static org.mybatis.dynamic.sql.SqlBuilder.insertMultiple;
import static org.mybatis.dynamic.sql.SqlBuilder.isBetween;
import static org.mybatis.dynamic.sql.SqlBuilder.isEqualTo;
import static org.mybatis.dynamic.sql.SqlBuilder.isGreaterThan;
import static org.mybatis.dynamic.sql.SqlBuilder.isIn;
import static org.mybatis.dynamic.sql.SqlBuilder.isLessThan;
import static org.mybatis.dynamic.sql.SqlBuilder.isLike;
import static org.mybatis.dynamic.sql.SqlBuilder.isNotBetween;
import static org.mybatis.dynamic.sql.SqlBuilder.isNotInCaseInsensitive;
import static org.mybatis.dynamic.sql.SqlBuilder.not;
import static org.mybatis.dynamic.sql.SqlBuilder.or;
import static org.mybatis.dynamic.sql.SqlBuilder.select;
import static org.mybatis.dynamic.sql.SqlBuilder.sortColumn;
import static org.mybatis.dynamic.sql.SqlBuilder.update;
import static org.mybatis.dynamic.sql.SqlBuilder.where;

import java.io.InputStream;
import java.io.InputStreamReader;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.JDBCType;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Types;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import org.apache.ibatis.jdbc.ScriptRunner;
import org.junit.jupiter.api.Test;
import org.mybatis.dynamic.sql.SqlTable;
import org.mybatis.dynamic.sql.delete.render.DeleteStatementProvider;
import org.mybatis.dynamic.sql.exception.DynamicSqlException;
import org.mybatis.dynamic.sql.insert.BatchInsertModel;
import org.mybatis.dynamic.sql.insert.InsertModel;
import org.mybatis.dynamic.sql.insert.MultiRowInsertModel;
import org.mybatis.dynamic.sql.insert.render.GeneralInsertStatementProvider;
import org.mybatis.dynamic.sql.insert.render.InsertSelectStatementProvider;
import org.mybatis.dynamic.sql.render.ParameterBinding;
import org.mybatis.dynamic.sql.render.RenderingStrategies;
import org.mybatis.dynamic.sql.select.render.SelectStatementProvider;
import org.mybatis.dynamic.sql.update.render.UpdateStatementProvider;
import org.mybatis.dynamic.sql.util.Messages;
import org.mybatis.dynamic.sql.where.render.WhereClauseProvider;

class RawJDBCTest {

    private static final String JDBC_URL = "jdbc:hsqldb:mem:aname";
    private static final String JDBC_DRIVER = "org.hsqldb.jdbcDriver";

    @Test
    void testSelectAllRows() {
        try (Connection connection = getConnection()) {
            SelectStatementProvider selectStatement = select(id, animalName, bodyWeight, brainWeight)
                    .from(animalData)
                    .build()
                    .render(RenderingStrategies.RAW_JDBC);

            String expected = "select id, animal_name, body_weight, brain_weight from AnimalData";
            assertThat(selectStatement.getSelectStatement()).isEqualTo(expected);

            List<AnimalData> animals = executeQuery(connection, selectStatement);

            assertThat(animals).hasSize(65);
            assertThat(animals.get(0).getId()).isEqualTo(1);
        } catch (SQLException e) {
            fail("SQL Exception", e);
        }
    }

    @Test
    void testSelectAllRowsWithLimit() {
        try (Connection connection = getConnection()) {
            SelectStatementProvider selectStatement = select(id, animalName, bodyWeight, brainWeight)
                    .from(animalData)
                    .limit(20)
                    .build()
                    .render(RenderingStrategies.RAW_JDBC);

            String expected = "select id, animal_name, body_weight, brain_weight from AnimalData limit ?";
            assertThat(selectStatement.getSelectStatement()).isEqualTo(expected);

            List<AnimalData> animals = executeQuery(connection, selectStatement);

            assertThat(animals).hasSize(20);
            assertThat(animals.get(0).getId()).isEqualTo(1);
        } catch (SQLException e) {
            fail("SQL Exception", e);
        }
    }

    @Test
    void testSelectRowsLessThan20() {
        try (Connection connection = getConnection()) {
            SelectStatementProvider selectStatement = select(id, animalName, bodyWeight, brainWeight)
                    .from(animalData)
                    .where(id, isLessThan(20))
                    .build()
                    .render(RenderingStrategies.RAW_JDBC);

            String expected = "select id, animal_name, body_weight, brain_weight from AnimalData where id < ?";
            assertThat(selectStatement.getSelectStatement()).isEqualTo(expected);

            List<AnimalData> animals = executeQuery(connection, selectStatement);
            assertThat(animals).hasSize(19);
        } catch (SQLException e) {
            fail("SQL Exception", e);
        }
    }

    @Test
    void testSelectRowsBetween30And40() {
        try (Connection connection = getConnection()) {
            SelectStatementProvider selectStatement = select(id, animalName, bodyWeight, brainWeight)
                    .from(animalData)
                    .where(id, isBetween(30).and(40))
                    .build()
                    .render(RenderingStrategies.RAW_JDBC);

            String expected = "select id, animal_name, body_weight, brain_weight from AnimalData where id between ? and ?";
            assertThat(selectStatement.getSelectStatement()).isEqualTo(expected);

            List<AnimalData> animals = executeQuery(connection, selectStatement);
            assertThat(animals).hasSize(11);
        } catch (SQLException e) {
            fail("SQL Exception", e);
        }
    }

    @Test
    void testSelectRowsNotBetweenWithNotGroup() {
        try (Connection connection = getConnection()) {
            SelectStatementProvider selectStatement = select(id, animalName, bodyWeight, brainWeight)
                    .from(animalData)
                    .where(not(id, isBetween(10).and(60),
                            or(animalName, isEqualTo("Little brown bat"))))
                    .build()
                    .render(RenderingStrategies.RAW_JDBC);

            String expected = "select id, animal_name, body_weight, brain_weight"
                    + " from AnimalData"
                    + " where not (id between ? and ?"
                    + " or animal_name = ?)";

            assertThat(selectStatement.getSelectStatement()).isEqualTo(expected);
            assertThat(selectStatement.getParameterBindings().get(0)).isEqualTo(
                    ParameterBinding.withMapKey("1").withJdbcType(JDBCType.INTEGER).withValue(10).build());
            assertThat(selectStatement.getParameterBindings().get(1)).isEqualTo(
                    ParameterBinding.withMapKey("2").withJdbcType(JDBCType.INTEGER).withValue(60).build());
            assertThat(selectStatement.getParameterBindings().get(2)).isEqualTo(
                    ParameterBinding.withMapKey("3").withJdbcType(JDBCType.VARCHAR).withValue("Little brown bat").build());
            List<AnimalData> animals = executeQuery(connection, selectStatement);
            assertThat(animals).hasSize(13);
        } catch (SQLException e) {
            fail("SQL Exception", e);
        }
    }

    @Test
    void testSelectRowsNotBetweenWithStandaloneWhereClause() {
        Optional<WhereClauseProvider> whereClause = where(id, isNotBetween(10).and(60))
                .or(id, isIn(25, 27))
                .build()
                .render(RenderingStrategies.RAW_JDBC);

        assertThat(whereClause).hasValueSatisfying(wc -> {
            assertThat(wc.getWhereClause()).isEqualTo("where id not between ? and ? or id in (?,?)");
            assertThat(wc.getParameterBindings().get(0)).isEqualTo(
                    ParameterBinding.withMapKey("1").withJdbcType(JDBCType.INTEGER).withValue(10).build());
            assertThat(wc.getParameterBindings().get(1)).isEqualTo(
                    ParameterBinding.withMapKey("2").withJdbcType(JDBCType.INTEGER).withValue(60).build());
            assertThat(wc.getParameterBindings().get(2)).isEqualTo(
                    ParameterBinding.withMapKey("3").withJdbcType(JDBCType.INTEGER).withValue(25).build());
            assertThat(wc.getParameterBindings().get(3)).isEqualTo(
                    ParameterBinding.withMapKey("4").withJdbcType(JDBCType.INTEGER).withValue(27).build());
        });
    }

    @Test
    void testUnionSelectWithTableAndColumnAliases() {
        try (Connection connection = getConnection()) {
            SelectStatementProvider selectStatement = select(id.as("animalId"), animalName, bodyWeight, brainWeight)
                    .from(animalData, "a")
                    .where(id, isLessThan(20))
                    .union()
                    .select(id.as("animalId"), animalName, bodyWeight, brainWeight)
                    .from(animalData, "b")
                    .where(id, isGreaterThan(40))
                    .orderBy(sortColumn("animalId"))
                    .build()
                    .render(RenderingStrategies.RAW_JDBC);

            String expected = "select a.id as animalId, a.animal_name, a.body_weight, a.brain_weight "
                    + "from AnimalData a "
                    + "where a.id < ? "
                    + "union "
                    + "select b.id as animalId, b.animal_name, b.body_weight, b.brain_weight "
                    + "from AnimalData b "
                    + "where b.id > ? "
                    + "order by animalId";

            assertThat(selectStatement.getSelectStatement()).isEqualTo(expected);
            assertThat(selectStatement.getParameterBindings()).hasSize(2);
            assertThat(selectStatement.getParameterBindings().get(0)).isEqualTo(
                    ParameterBinding.withMapKey("1").withValue(20).withJdbcType(JDBCType.INTEGER).build());
            assertThat(selectStatement.getParameterBindings().get(1)).isEqualTo(
                    ParameterBinding.withMapKey("2").withValue(40).withJdbcType(JDBCType.INTEGER).build());
            assertThat(selectStatement.getParameters()).containsEntry("2", 40);

            List<AnimalData> animals = executeQuery(connection, selectStatement);
            assertThat(animals).hasSize(44);
        } catch (SQLException e) {
            fail("SQL Exception", e);
        }
    }

    @Test
    void testInCondition() {
        try (Connection connection = getConnection()) {
            SelectStatementProvider selectStatement = select(id, animalName, bodyWeight, brainWeight)
                    .from(animalData)
                    .where(id, isIn(5, 8, 10))
                    .build()
                    .render(RenderingStrategies.RAW_JDBC);

            assertThat(selectStatement.getParameterBindings().get(0)).isEqualTo(
                    ParameterBinding.withMapKey("1").withJdbcType(JDBCType.INTEGER).withValue(5).build());
            assertThat(selectStatement.getParameterBindings().get(1)).isEqualTo(
                    ParameterBinding.withMapKey("2").withJdbcType(JDBCType.INTEGER).withValue(8).build());
            assertThat(selectStatement.getParameterBindings().get(2)).isEqualTo(
                    ParameterBinding.withMapKey("3").withJdbcType(JDBCType.INTEGER).withValue(10).build());
            List<AnimalData> animals = executeQuery(connection, selectStatement);
            assertThat(animals).hasSize(3);
        } catch (SQLException e) {
            fail("SQL Exception", e);
        }
    }

    @Test
    void testNotInCaseSensitiveCondition() {
        try (Connection connection = getConnection()) {
            SelectStatementProvider selectStatement = select(id, animalName, bodyWeight, brainWeight)
                    .from(animalData)
                    .where(animalName, isNotInCaseInsensitive("yellow-bellied marmot", "verbet"))
                    .build()
                    .render(RenderingStrategies.RAW_JDBC);

            String expected = "select id, animal_name, body_weight, brain_weight " +
                    "from AnimalData where upper(animal_name) not in (?,?)";
            assertThat(selectStatement.getSelectStatement()).isEqualTo(expected);
            assertThat(selectStatement.getParameterBindings().get(0)).isEqualTo(
                    ParameterBinding.withMapKey("1").withJdbcType(JDBCType.VARCHAR).withValue("YELLOW-BELLIED MARMOT").build());
            assertThat(selectStatement.getParameterBindings().get(1)).isEqualTo(
                    ParameterBinding.withMapKey("2").withJdbcType(JDBCType.VARCHAR).withValue("VERBET").build());

            List<AnimalData> animals = executeQuery(connection, selectStatement);
            assertThat(animals).hasSize(63);
        } catch (SQLException e) {
            fail("SQL Exception", e);
        }
    }

    @Test
    void testComplexDelete() {
        try (Connection connection = getConnection()) {
            DeleteStatementProvider deleteStatement = deleteFrom(animalData)
                    .where(id, isLessThan(10))
                    .or(id, isGreaterThan(60))
                    .build()
                    .render(RenderingStrategies.RAW_JDBC);

            int rowCount = executeDelete(connection, deleteStatement);
            assertThat(rowCount).isEqualTo(14);
        } catch (SQLException e) {
            fail("SQL Exception", e);
        }
    }

    @Test
    void testUpdate() {
        try (Connection connection = getConnection()) {
            UpdateStatementProvider updateStatement = update(animalData)
                    .set(bodyWeight).equalTo(2.6)
                    .set(animalName).equalToNull()
                    .where(id, isIn(1, 5, 7))
                    .or(id, isIn(2, 6, 8), and(animalName, isLike("%bat")))
                    .or(id, isGreaterThan(60))
                    .and(bodyWeight, isBetween(1.0).and(3.0))
                    .build()
                    .render(RenderingStrategies.RAW_JDBC);

            String expected = "update AnimalData set body_weight = ?, animal_name = null " +
                    "where id in (?,?,?) or (id in (?,?,?) and animal_name like ?) " +
                    "or id > ? and body_weight between ? and ?";
            assertThat(updateStatement.getUpdateStatement()).isEqualTo(expected);

            assertThat(updateStatement.getParameterBindings().get(0)).isEqualTo(
                    ParameterBinding.withMapKey("1").withJdbcType(JDBCType.DOUBLE).withValue(2.6).build());
            assertThat(updateStatement.getParameterBindings().get(1)).isEqualTo(
                    ParameterBinding.withMapKey("2").withJdbcType(JDBCType.INTEGER).withValue(1).build());
            assertThat(updateStatement.getParameterBindings().get(2)).isEqualTo(
                    ParameterBinding.withMapKey("3").withJdbcType(JDBCType.INTEGER).withValue(5).build());
            assertThat(updateStatement.getParameterBindings().get(3)).isEqualTo(
                    ParameterBinding.withMapKey("4").withJdbcType(JDBCType.INTEGER).withValue(7).build());
            assertThat(updateStatement.getParameterBindings().get(4)).isEqualTo(
                    ParameterBinding.withMapKey("5").withJdbcType(JDBCType.INTEGER).withValue(2).build());
            assertThat(updateStatement.getParameterBindings().get(5)).isEqualTo(
                    ParameterBinding.withMapKey("6").withJdbcType(JDBCType.INTEGER).withValue(6).build());
            assertThat(updateStatement.getParameterBindings().get(6)).isEqualTo(
                    ParameterBinding.withMapKey("7").withJdbcType(JDBCType.INTEGER).withValue(8).build());
            assertThat(updateStatement.getParameterBindings().get(7)).isEqualTo(
                    ParameterBinding.withMapKey("8").withJdbcType(JDBCType.VARCHAR).withValue("%bat").build());
            assertThat(updateStatement.getParameterBindings().get(8)).isEqualTo(
                    ParameterBinding.withMapKey("9").withJdbcType(JDBCType.INTEGER).withValue(60).build());
            assertThat(updateStatement.getParameterBindings().get(9)).isEqualTo(
                    ParameterBinding.withMapKey("10").withJdbcType(JDBCType.DOUBLE).withValue(1.0).build());
            assertThat(updateStatement.getParameterBindings().get(10)).isEqualTo(
                    ParameterBinding.withMapKey("11").withJdbcType(JDBCType.DOUBLE).withValue(3.0).build());

            int rows = executeUpdate(connection, updateStatement);
            assertThat(rows).isEqualTo(4);
        } catch (SQLException e) {
            fail("SQL Exception", e);
        }
    }

    @Test
    void testUpdateValueWithNull() {
        try (Connection connection = getConnection()) {
            UpdateStatementProvider updateStatement = update(animalData)
                    .set(animalName).equalTo((String) null)
                    .where(id, isEqualTo(1))
                    .build()
                    .render(RenderingStrategies.RAW_JDBC);

            assertThat(updateStatement.getUpdateStatement()).isEqualTo(
                    "update AnimalData set animal_name = ? where id = ?");
            int rows = executeUpdate(connection, updateStatement);
            assertThat(rows).isEqualTo(1);
        } catch (SQLException e) {
            fail("SQL Exception", e);
        }
    }

    @Test
    void testInsert() {
        AnimalData record = new AnimalData();
        record.setId(100);
        record.setAnimalName("Old Shep");
        record.setBodyWeight(22.5);
        record.setBrainWeight(1.2);

        InsertModel<AnimalData> insertModel = insert(record)
                .into(animalData)
                .map(id).toProperty("id")
                .map(animalName).toProperty("animalName")
                .map(bodyWeight).toProperty("bodyWeight")
                .map(brainWeight).toProperty("brainWeight")
                .build();

        assertThatExceptionOfType(DynamicSqlException.class).isThrownBy(
                () -> insertModel.render(RenderingStrategies.RAW_JDBC))
                .withMessage(Messages.getString("ERROR.38"));
    }

    @Test
    void testBatchInsert() {
        List<AnimalData> records = new ArrayList<>();
        AnimalData record = new AnimalData();
        record.setId(100);
        record.setAnimalName("Old Shep");
        record.setBodyWeight(22.5);
        records.add(record);

        record = new AnimalData();
        record.setId(101);
        record.setAnimalName("Old Dan");
        record.setBodyWeight(22.5);
        records.add(record);

        BatchInsertModel<AnimalData> batchInsertModel = insertBatch(records)
                .into(animalData)
                .map(id).toProperty("id")
                .map(animalName).toNull()
                .map(bodyWeight).toProperty("bodyWeight")
                .map(brainWeight).toConstant("1.2")
                .build();

        assertThatExceptionOfType(DynamicSqlException.class).isThrownBy(
                        () -> batchInsertModel.render(RenderingStrategies.RAW_JDBC))
                .withMessage(Messages.getString("ERROR.38"));
    }

    @Test
    void testMultipleInsert() {
        List<AnimalData> records = new ArrayList<>();
        AnimalData record = new AnimalData();
        record.setId(100);
        record.setAnimalName("Old Shep");
        record.setBodyWeight(22.5);
        records.add(record);

        record = new AnimalData();
        record.setId(101);
        record.setAnimalName("Old Dan");
        record.setBodyWeight(22.5);
        records.add(record);

        MultiRowInsertModel<AnimalData> multiRowInsertModel = insertMultiple(records)
                .into(animalData)
                .map(id).toProperty("id")
                .map(animalName).toNull()
                .map(bodyWeight).toProperty("bodyWeight")
                .map(brainWeight).toConstant("1.2")
                .build();

        assertThatExceptionOfType(DynamicSqlException.class).isThrownBy(
                        () -> multiRowInsertModel.render(RenderingStrategies.RAW_JDBC))
                .withMessage(Messages.getString("ERROR.38"));
    }

    @Test
    void testMultipleInsertBindToRow() {
        List<Integer> ids = new ArrayList<>();
        ids.add(1);
        ids.add(2);

        MultiRowInsertModel<Integer> multiRowInsertModel = insertMultiple(ids)
                .into(animalData)
                .map(id).toRow()
                .build();

        assertThatExceptionOfType(DynamicSqlException.class).isThrownBy(
                        () -> multiRowInsertModel.render(RenderingStrategies.RAW_JDBC))
                .withMessage(Messages.getString("ERROR.38"));
    }

    @Test
    void testInsertSelectWithColumnList() {
        try (Connection connection = getConnection()) {
            SqlTable animalDataCopy = SqlTable.of("AnimalDataCopy");

            InsertSelectStatementProvider insertSelectStatement = insertInto(animalDataCopy)
                    .withColumnList(id, animalName, bodyWeight, brainWeight)
                    .withSelectStatement(select(id, animalName, bodyWeight, brainWeight).from(animalData).where(id, isLessThan(22)))
                    .build()
                    .render(RenderingStrategies.RAW_JDBC);

            String expected = "insert into AnimalDataCopy (id, animal_name, body_weight, brain_weight) "
                    + "select id, animal_name, body_weight, brain_weight "
                    + "from AnimalData "
                    + "where id < ?";

            assertThat(insertSelectStatement.getInsertStatement()).isEqualTo(expected);
            assertThat(insertSelectStatement.getParameterBindings()).hasSize(1);
            assertThat(insertSelectStatement.getParameterBindings().get(0)).isEqualTo(
                    ParameterBinding.withMapKey("1").withJdbcType(JDBCType.INTEGER).withValue(22).build());

            int rows = executeInsertSelect(connection, insertSelectStatement);
            assertThat(rows).isEqualTo(21);
        } catch (SQLException e) {
            fail("SQL Exception", e);
        }
    }

    @Test
    void testGeneralInsert() {
        try (Connection connection = getConnection()) {
            GeneralInsertStatementProvider insertStatement = insertInto(animalData)
                    .set(id).toValue(101)
                    .set(animalName).toStringConstant("Fred")
                    .set(brainWeight).toConstant("2.2")
                    .set(bodyWeight).toValue(4.5)
                    .build()
                    .render(RenderingStrategies.RAW_JDBC);

            String expected = "insert into AnimalData (id, animal_name, brain_weight, body_weight) "
                    + "values (?, 'Fred', 2.2, ?)";

            assertThat(insertStatement.getInsertStatement()).isEqualTo(expected);
            assertThat(insertStatement.getParameterBindings()).hasSize(2);
            assertThat(insertStatement.getParameterBindings().get(0)).isEqualTo(
                    ParameterBinding.withMapKey("1").withJdbcType(JDBCType.INTEGER).withValue(101).build());
            assertThat(insertStatement.getParameterBindings().get(1)).isEqualTo(
                    ParameterBinding.withMapKey("2").withJdbcType(JDBCType.DOUBLE).withValue(4.5).build());

            int rows = executeGeneralInsert(connection, insertStatement);
            assertThat(rows).isEqualTo(1);
        } catch (SQLException e) {
            fail("SQL Exception", e);
        }
    }

    private Connection getConnection() throws SQLException {
        try {
            Class.forName(JDBC_DRIVER);
            InputStream is = getClass().getResourceAsStream("/examples/animal/data/CreateAnimalData.sql");
            assert is != null;
            Connection connection = DriverManager.getConnection(JDBC_URL, "sa", "");
            ScriptRunner sr = new ScriptRunner(connection);
            sr.setLogWriter(null);
            sr.runScript(new InputStreamReader(is));

            return connection;
        } catch (ClassNotFoundException e) {
            throw new RuntimeException(e);
        }
    }

    private int executeDelete(Connection connection, DeleteStatementProvider deleteStatementProvider)
            throws SQLException {
        try (PreparedStatement ps = connection.prepareStatement(deleteStatementProvider.getDeleteStatement())) {
            bindParameters(ps, deleteStatementProvider.getParameterBindings());
            return ps.executeUpdate();
        }
    }

    private int executeGeneralInsert(Connection connection, GeneralInsertStatementProvider generalInsertStatementProvider)
            throws SQLException {
        try (PreparedStatement ps = connection.prepareStatement(generalInsertStatementProvider.getInsertStatement())) {
            bindParameters(ps, generalInsertStatementProvider.getParameterBindings());
            return ps.executeUpdate();
        }
    }

    private int executeInsertSelect(Connection connection, InsertSelectStatementProvider insertSelectStatementProvider)
            throws SQLException {
        try (PreparedStatement ps = connection.prepareStatement(insertSelectStatementProvider.getInsertStatement())) {
            bindParameters(ps, insertSelectStatementProvider.getParameterBindings());
            return ps.executeUpdate();
        }
    }

    private int executeUpdate(Connection connection, UpdateStatementProvider updateStatementProvider)
            throws SQLException {
        try (PreparedStatement ps = connection.prepareStatement(updateStatementProvider.getUpdateStatement())) {
            bindParameters(ps, updateStatementProvider.getParameterBindings());
            return ps.executeUpdate();
        }
    }

    private List<AnimalData> executeQuery(Connection connection, SelectStatementProvider selectStatementProvider)
            throws SQLException {
        try (PreparedStatement ps = connection.prepareStatement(selectStatementProvider.getSelectStatement())) {
            bindParameters(ps, selectStatementProvider.getParameterBindings());
            try (ResultSet rs = ps.executeQuery()) {
                return processResultSet(rs);
            }
        }
    }

    private void bindParameters(PreparedStatement ps, List<ParameterBinding> parameterBindings) throws SQLException {
        for (int i = 0; i < parameterBindings.size(); i++) {
            ParameterBinding parameterBinding = parameterBindings.get(i);

            Object value = parameterBinding.getValue();
            Optional<JDBCType> jdbcType = parameterBinding.getJdbcType();

            if (value == null) {
                if (jdbcType.isPresent()) {
                    ps.setNull(i + 1, jdbcType.get().getVendorTypeNumber());
                } else {
                    ps.setNull(i + 1, Types.OTHER);
                }
            } else {
                ps.setObject(i + 1, value);
            }
        }
    }

    private List<AnimalData> processResultSet(ResultSet rs) throws SQLException {
        List<AnimalData> answer = new ArrayList<>();
        while (rs.next()) {
            AnimalData animalData = new AnimalData();
            animalData.setId(rs.getInt(1));
            animalData.setAnimalName(rs.getString(2));
            animalData.setBodyWeight(rs.getDouble(3));
            animalData.setBrainWeight(rs.getDouble(4));
            answer.add(animalData);
        }

        return answer;
    }
}
