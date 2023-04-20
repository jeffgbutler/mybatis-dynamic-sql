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
package examples.r2dbc.raw;

import static examples.animal.data.AnimalDataDynamicSqlSupport.animalData;
import static examples.animal.data.AnimalDataDynamicSqlSupport.animalName;
import static examples.animal.data.AnimalDataDynamicSqlSupport.bodyWeight;
import static examples.animal.data.AnimalDataDynamicSqlSupport.brainWeight;
import static examples.animal.data.AnimalDataDynamicSqlSupport.id;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.entry;
import static org.mybatis.dynamic.sql.SqlBuilder.*;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import examples.animal.data.AnimalData;
import io.r2dbc.spi.ColumnMetadata;
import io.r2dbc.spi.Row;
import io.r2dbc.spi.RowMetadata;
import org.junit.jupiter.api.Test;
import org.mybatis.dynamic.sql.delete.render.DeleteStatementProvider;
import org.mybatis.dynamic.sql.insert.render.GeneralInsertStatementProvider;
import org.mybatis.dynamic.sql.insert.render.MultiRowInsertStatementProvider;
import org.mybatis.dynamic.sql.render.RenderingStrategy;
import org.mybatis.dynamic.sql.select.render.SelectStatementProvider;
import org.mybatis.dynamic.sql.update.render.UpdateStatementProvider;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.context.junit.jupiter.SpringJUnitConfig;

@SpringJUnitConfig(classes = SpringConfiguration.class)
class RawR2DBCTest {

    @Autowired
    private ConnectionFactoryExtensions connectionFactory;

    private static final RenderingStrategy R2DBC = new R2DBCRenderingStrategy();

    private AnimalData rowMapper(Row row, RowMetadata rowMetadata) {
        Integer id = row.get("id", Integer.class);
        String animalName = row.get("animal_name", String.class);
        Double bodyWeight = row.get("body_weight", Double.class);
        Double brainWeight = row.get("brain_weight", Double.class);

        AnimalData animal = new AnimalData();
        animal.setId(id);
        animal.setAnimalName(animalName);
        animal.setBodyWeight(bodyWeight);
        animal.setBrainWeight(brainWeight);
        return animal;
    }

    private Map<String, Object> rawMapper(Row row, RowMetadata rowMetadata) {
        List<? extends ColumnMetadata> columnMetadatas = rowMetadata.getColumnMetadatas();
        Map<String, Object> answer = new HashMap<>(columnMetadatas.size());

        for (ColumnMetadata columnMetadata : columnMetadatas) {
            String name = columnMetadata.getName();
            answer.put(name, row.get(name));
        }

        return answer;
    }

    @Test
    void testSelectAllRows() {
        SelectStatementProvider selectStatement = select(id, animalName, bodyWeight, brainWeight)
                .from(animalData)
                .build()
                .render(R2DBC);

        List<AnimalData> animals = connectionFactory.selectMany(selectStatement, this::rowMapper).collectList().block();

        assertThat(animals).hasSize(65);
        assertThat(animals.get(0).getId()).isEqualTo(1);
    }

    @Test
    void testSelectAllRowsWithOrder() {
        SelectStatementProvider selectStatement = select(id, animalName, bodyWeight, brainWeight)
                .from(animalData)
                .orderBy(id.descending())
                .build()
                .render(R2DBC);
        List<AnimalData> animals = connectionFactory.selectMany(selectStatement, this::rowMapper).collectList().block();
        assertThat(animals).hasSize(65);
        assertThat(animals.get(0).getId()).isEqualTo(65);
    }

    @Test
    void testSelectAllRowsAllColumnsWithOrder() {
        SelectStatementProvider selectStatement = select(animalData.allColumns())
                .from(animalData)
                .orderBy(id.descending())
                .build()
                .render(R2DBC);
        List<AnimalData> animals = connectionFactory.selectMany(selectStatement, this::rowMapper).collectList().block();
        assertThat(selectStatement.getSelectStatement()).isEqualTo("select * from AnimalData order by id DESC");
        assertThat(animals).hasSize(65);
        assertThat(animals.get(0).getId()).isEqualTo(65);
    }

    @Test
    void testSelectAllRowsAllColumnsWithOrderAndAlias() {
        SelectStatementProvider selectStatement = select(animalData.allColumns())
                .from(animalData, "ad")
                .orderBy(id.descending())
                .build()
                .render(R2DBC);
        List<AnimalData> animals = connectionFactory.selectMany(selectStatement, this::rowMapper).collectList().block();
        assertThat(selectStatement.getSelectStatement()).isEqualTo("select ad.* from AnimalData ad order by id DESC");
        assertThat(animals).hasSize(65);
        assertThat(animals.get(0).getId()).isEqualTo(65);
    }

    @Test
    void testSelectRowsLessThan20() {
        SelectStatementProvider selectStatement = select(id, animalName, bodyWeight, brainWeight)
                .from(animalData)
                .where(id, isLessThan(20))
                .build()
                .render(R2DBC);

        String expected = "select id, animal_name, body_weight, brain_weight from AnimalData where id < $1";
        assertThat(selectStatement.getSelectStatement()).isEqualTo(expected);
        assertThat(selectStatement.getParameters()).containsExactly(entry("$1", 20));

        List<AnimalData> animals = connectionFactory.selectMany(selectStatement, this::rowMapper).collectList().block();

        assertThat(animals).hasSize(19);
    }

    @Test
    void testSelectRowsBetween30And40() {
        SelectStatementProvider selectStatement = select(id, animalName, bodyWeight, brainWeight)
                .from(animalData)
                .where(id, isBetween(30).and(40))
                .build()
                .render(R2DBC);

        List<AnimalData> animals = connectionFactory.selectMany(selectStatement, this::rowMapper).collectList().block();
        assertThat(animals).hasSize(11);
    }

    @Test
    void testSelectRowsNotBetween() {
        SelectStatementProvider selectStatement = select(id, animalName, bodyWeight, brainWeight)
                .from(animalData)
                .where(id, isNotBetween(10).and(60))
                .build()
                .render(R2DBC);

        List<AnimalData> animals = connectionFactory.selectMany(selectStatement, this::rowMapper).collectList().block();
        assertThat(animals).hasSize(14);
    }

    @Test
    void testSelectRowsNotBetweenWithNewNot() {
        SelectStatementProvider selectStatement = select(id, animalName, bodyWeight, brainWeight)
                .from(animalData)
                .where(not(id, isBetween(10).and(60)))
                .build()
                .render(R2DBC);

        String expected = "select id, animal_name, body_weight, brain_weight"
                + " from AnimalData"
                + " where not id between $1 and $2";

        assertThat(selectStatement.getSelectStatement()).isEqualTo(expected);
        List<AnimalData> animals = connectionFactory.selectMany(selectStatement, this::rowMapper).collectList().block();
        assertThat(animals).hasSize(14);
    }

    @Test
    void testSelectRowsNotBetweenWithNotGroup() {
        SelectStatementProvider selectStatement = select(id, animalName, bodyWeight, brainWeight)
                .from(animalData)
                .where(not(id, isBetween(10).and(60),
                        or(animalName, isEqualTo("Little brown bat"))))
                .build()
                .render(R2DBC);

        String expected = "select id, animal_name, body_weight, brain_weight"
                + " from AnimalData"
                + " where not (id between $1 and $2"
                + " or animal_name = $3)";

        assertThat(selectStatement.getSelectStatement()).isEqualTo(expected);
        List<AnimalData> animals = connectionFactory.selectMany(selectStatement, this::rowMapper).collectList().block();
        assertThat(animals).hasSize(13);
    }

    @Test
    void testSelectRowsNotBetweenWithNotAndGroup() {
        SelectStatementProvider selectStatement = select(id, animalName, bodyWeight, brainWeight)
                .from(animalData)
                .where(not(group(id, isBetween(10).and(60),
                        or(animalName, isEqualTo("Little brown bat")))))
                .build()
                .render(R2DBC);

        String expected = "select id, animal_name, body_weight, brain_weight"
                + " from AnimalData"
                + " where not (id between $1 and $2"
                + " or animal_name = $3)";

        assertThat(selectStatement.getSelectStatement()).isEqualTo(expected);
        List<AnimalData> animals = connectionFactory.selectMany(selectStatement, this::rowMapper).collectList().block();
        assertThat(animals).hasSize(13);
    }

    @Test
    void testUnionSelectWithWhere() {
        SelectStatementProvider selectStatement = select(id, animalName, bodyWeight, brainWeight)
                .from(animalData)
                .where(id, isLessThan(20))
                .union()
                .select(id, animalName, bodyWeight, brainWeight)
                .from(animalData)
                .where(id, isGreaterThan(40))
                .build()
                .render(R2DBC);

        String expected = "select id, animal_name, body_weight, brain_weight "
                + "from AnimalData "
                + "where id < $1 "
                + "union "
                + "select id, animal_name, body_weight, brain_weight "
                + "from AnimalData "
                + "where id > $2";

        List<AnimalData> animals = connectionFactory.selectMany(selectStatement, this::rowMapper).collectList().block();
        assertThat(selectStatement.getSelectStatement()).isEqualTo(expected);
        assertThat(animals).hasSize(44);
        assertThat(selectStatement.getParameters()).hasSize(2);
        assertThat(selectStatement.getParameters()).containsEntry("$1", 20);
        assertThat(selectStatement.getParameters()).containsEntry("$2", 40);
    }

//    @Test
//    void testUnionSelectWithoutWhere() {
//        try (SqlSession sqlSession = sqlSessionFactory.openSession()) {
//            AnimalDataMapper mapper = sqlSession.getMapper(AnimalDataMapper.class);
//
//            SelectStatementProvider selectStatement = select(id, animalName, bodyWeight, brainWeight)
//                    .from(animalData)
//                    .union()
//                    .selectDistinct(id, animalName, bodyWeight, brainWeight)
//                    .from(animalData)
//                    .orderBy(id)
//                    .build()
//                    .render(RenderingStrategies.MYBATIS3);
//
//            String expected = "select id, animal_name, body_weight, brain_weight "
//                    + "from AnimalData "
//                    + "union "
//                    + "select distinct id, animal_name, body_weight, brain_weight "
//                    + "from AnimalData "
//                    + "order by id";
//
//            List<ReactiveAnimalData> animals = mapper.selectMany(selectStatement);
//            assertAll(
//                    () -> assertThat(selectStatement.getSelectStatement()).isEqualTo(expected),
//                    () -> assertThat(animals).hasSize(65),
//                    () -> assertThat(selectStatement.getParameters()).isEmpty()
//            );
//        }
//    }
//
//    @Test
//    void testUnionAllSelectWithoutWhere() {
//        try (SqlSession sqlSession = sqlSessionFactory.openSession()) {
//            AnimalDataMapper mapper = sqlSession.getMapper(AnimalDataMapper.class);
//
//            SelectStatementProvider selectStatement = select(id, animalName, bodyWeight, brainWeight)
//                    .from(animalData)
//                    .unionAll()
//                    .selectDistinct(id, animalName, bodyWeight, brainWeight)
//                    .from(animalData)
//                    .orderBy(id)
//                    .build()
//                    .render(RenderingStrategies.MYBATIS3);
//
//            String expected = "select id, animal_name, body_weight, brain_weight "
//                    + "from AnimalData "
//                    + "union all "
//                    + "select distinct id, animal_name, body_weight, brain_weight "
//                    + "from AnimalData "
//                    + "order by id";
//
//            List<ReactiveAnimalData> animals = mapper.selectMany(selectStatement);
//            assertAll(
//                    () -> assertThat(selectStatement.getSelectStatement()).isEqualTo(expected),
//                    () -> assertThat(animals).hasSize(130),
//                    () -> assertThat(selectStatement.getParameters()).isEmpty()
//            );
//        }
//    }
//
//    @Test
//    void testUnionSelectWithTableAliases() {
//        try (SqlSession sqlSession = sqlSessionFactory.openSession()) {
//            AnimalDataMapper mapper = sqlSession.getMapper(AnimalDataMapper.class);
//
//            SelectStatementProvider selectStatement = select(id, animalName, bodyWeight, brainWeight)
//                    .from(animalData, "a")
//                    .where(id, isLessThan(20))
//                    .union()
//                    .select(id, animalName, bodyWeight, brainWeight)
//                    .from(animalData, "b")
//                    .where(id, isGreaterThan(40))
//                    .orderBy(id)
//                    .build()
//                    .render(RenderingStrategies.MYBATIS3);
//
//            String expected = "select a.id, a.animal_name, a.body_weight, a.brain_weight "
//                    + "from AnimalData a "
//                    + "where a.id < #{parameters.p1,jdbcType=INTEGER} "
//                    + "union "
//                    + "select b.id, b.animal_name, b.body_weight, b.brain_weight "
//                    + "from AnimalData b "
//                    + "where b.id > #{parameters.p2,jdbcType=INTEGER} "
//                    + "order by id";
//
//            List<ReactiveAnimalData> animals = mapper.selectMany(selectStatement);
//
//            assertAll(
//                    () -> assertThat(selectStatement.getSelectStatement()).isEqualTo(expected),
//                    () -> assertThat(animals).hasSize(44),
//                    () -> assertThat(selectStatement.getParameters()).hasSize(2),
//                    () -> assertThat(selectStatement.getParameters()).containsEntry("p1", 20),
//                    () -> assertThat(selectStatement.getParameters()).containsEntry("p2", 40)
//            );
//        }
//    }
//
//    @Test
//    void testUnionAllSelectWithTableAliases() {
//        try (SqlSession sqlSession = sqlSessionFactory.openSession()) {
//            AnimalDataMapper mapper = sqlSession.getMapper(AnimalDataMapper.class);
//
//            SelectStatementProvider selectStatement = select(id, animalName, bodyWeight, brainWeight)
//                    .from(animalData, "a")
//                    .where(id, isLessThan(20))
//                    .unionAll()
//                    .select(id, animalName, bodyWeight, brainWeight)
//                    .from(animalData, "b")
//                    .where(id, isGreaterThan(40))
//                    .orderBy(id)
//                    .build()
//                    .render(RenderingStrategies.MYBATIS3);
//
//            String expected = "select a.id, a.animal_name, a.body_weight, a.brain_weight "
//                    + "from AnimalData a "
//                    + "where a.id < #{parameters.p1,jdbcType=INTEGER} "
//                    + "union all "
//                    + "select b.id, b.animal_name, b.body_weight, b.brain_weight "
//                    + "from AnimalData b "
//                    + "where b.id > #{parameters.p2,jdbcType=INTEGER} "
//                    + "order by id";
//
//            List<ReactiveAnimalData> animals = mapper.selectMany(selectStatement);
//
//            assertAll(
//                    () -> assertThat(selectStatement.getSelectStatement()).isEqualTo(expected),
//                    () -> assertThat(animals).hasSize(44),
//                    () -> assertThat(selectStatement.getParameters()).hasSize(2),
//                    () -> assertThat(selectStatement.getParameters()).containsEntry("p1", 20),
//                    () -> assertThat(selectStatement.getParameters()).containsEntry("p2", 40)
//            );
//        }
//    }
//
//    @Test
//    void testUnionSelectWithTableAndColumnAliases() {
//        try (SqlSession sqlSession = sqlSessionFactory.openSession()) {
//            AnimalDataMapper mapper = sqlSession.getMapper(AnimalDataMapper.class);
//
//            SelectStatementProvider selectStatement = select(id.as("animalId"), animalName, bodyWeight, brainWeight)
//                    .from(animalData, "a")
//                    .where(id, isLessThan(20))
//                    .union()
//                    .select(id.as("animalId"), animalName, bodyWeight, brainWeight)
//                    .from(animalData, "b")
//                    .where(id, isGreaterThan(40))
//                    .orderBy(sortColumn("animalId"))
//                    .build()
//                    .render(RenderingStrategies.MYBATIS3);
//
//            String expected = "select a.id as animalId, a.animal_name, a.body_weight, a.brain_weight "
//                    + "from AnimalData a "
//                    + "where a.id < #{parameters.p1,jdbcType=INTEGER} "
//                    + "union "
//                    + "select b.id as animalId, b.animal_name, b.body_weight, b.brain_weight "
//                    + "from AnimalData b "
//                    + "where b.id > #{parameters.p2,jdbcType=INTEGER} "
//                    + "order by animalId";
//
//            List<ReactiveAnimalData> animals = mapper.selectMany(selectStatement);
//
//            assertAll(
//                    () -> assertThat(selectStatement.getSelectStatement()).isEqualTo(expected),
//                    () -> assertThat(animals).hasSize(44),
//                    () -> assertThat(selectStatement.getParameters()).hasSize(2),
//                    () -> assertThat(selectStatement.getParameters()).containsEntry("p1", 20),
//                    () -> assertThat(selectStatement.getParameters()).containsEntry("p2", 40)
//            );
//        }
//    }
//
//    @Test
//    void testIsEqualCondition() {
//        try (SqlSession sqlSession = sqlSessionFactory.openSession()) {
//            AnimalDataMapper mapper = sqlSession.getMapper(AnimalDataMapper.class);
//
//            SelectStatementProvider selectStatement = select(id, animalName, bodyWeight, brainWeight)
//                    .from(animalData)
//                    .where(id, isEqualTo(5))
//                    .build()
//                    .render(RenderingStrategies.MYBATIS3);
//
//            List<ReactiveAnimalData> animals = mapper.selectMany(selectStatement);
//            assertThat(animals).hasSize(1);
//        }
//    }
//
//    @Test
//    void testIsNotEqualCondition() {
//        try (SqlSession sqlSession = sqlSessionFactory.openSession()) {
//            AnimalDataMapper mapper = sqlSession.getMapper(AnimalDataMapper.class);
//
//            SelectStatementProvider selectStatement = select(id, animalName, bodyWeight, brainWeight)
//                    .from(animalData)
//                    .where(id, isNotEqualTo(5))
//                    .build()
//                    .render(RenderingStrategies.MYBATIS3);
//
//            List<ReactiveAnimalData> animals = mapper.selectMany(selectStatement);
//            assertThat(animals).hasSize(64);
//        }
//    }
//
//    @Test
//    void testIsGreaterThanOrEqualToCondition() {
//        try (SqlSession sqlSession = sqlSessionFactory.openSession()) {
//            AnimalDataMapper mapper = sqlSession.getMapper(AnimalDataMapper.class);
//
//            SelectStatementProvider selectStatement = select(id, animalName, bodyWeight, brainWeight)
//                    .from(animalData)
//                    .where(id, isGreaterThanOrEqualTo(60))
//                    .build()
//                    .render(RenderingStrategies.MYBATIS3);
//
//            List<ReactiveAnimalData> animals = mapper.selectMany(selectStatement);
//            assertThat(animals).hasSize(6);
//        }
//    }
//
//    @Test
//    void testIsLessThanOrEqualToCondition() {
//        try (SqlSession sqlSession = sqlSessionFactory.openSession()) {
//            AnimalDataMapper mapper = sqlSession.getMapper(AnimalDataMapper.class);
//
//            SelectStatementProvider selectStatement = select(id, animalName, bodyWeight, brainWeight)
//                    .from(animalData)
//                    .where(id, isLessThanOrEqualTo(10))
//                    .build()
//                    .render(RenderingStrategies.MYBATIS3);
//
//            List<ReactiveAnimalData> animals = mapper.selectMany(selectStatement);
//            assertThat(animals).hasSize(10);
//        }
//    }
//
//    @Test
//    void testInCondition() {
//        try (SqlSession sqlSession = sqlSessionFactory.openSession()) {
//            AnimalDataMapper mapper = sqlSession.getMapper(AnimalDataMapper.class);
//
//            SelectStatementProvider selectStatement = select(id, animalName, bodyWeight, brainWeight)
//                    .from(animalData)
//                    .where(id, isIn(5, 8, 10))
//                    .build()
//                    .render(RenderingStrategies.MYBATIS3);
//
//            List<ReactiveAnimalData> animals = mapper.selectMany(selectStatement);
//            assertThat(animals).hasSize(3);
//        }
//    }
//
//    @Test
//    void testInConditionWithEventuallyEmptyList() {
//        try (SqlSession sqlSession = sqlSessionFactory.openSession()) {
//            AnimalDataMapper mapper = sqlSession.getMapper(AnimalDataMapper.class);
//
//            SelectStatementProvider selectStatement = select(id, animalName, bodyWeight, brainWeight)
//                    .from(animalData)
//                    .where(id, isIn(null, 22, null).filter(Objects::nonNull).filter(i -> i != 22))
//                    .configureStatement(c -> c.setNonRenderingWhereClauseAllowed(true))
//                    .build()
//                    .render(RenderingStrategies.MYBATIS3);
//
//            assertThat(selectStatement.getSelectStatement())
//                    .isEqualTo("select id, animal_name, body_weight, brain_weight from AnimalData");
//            List<ReactiveAnimalData> animals = mapper.selectMany(selectStatement);
//            assertThat(animals).hasSize(65);
//        }
//    }
//
//    @Test
//    void testInConditionWithEventuallyEmptyListForceRendering() {
//        List<Integer> inValues = new ArrayList<>();
//        inValues.add(null);
//        inValues.add(22);
//        inValues.add(null);
//
//        SelectModel selectModel = select(id, animalName, bodyWeight, brainWeight)
//                .from(animalData)
//                .where(id, isIn(inValues).filter(Objects::nonNull).filter(i -> i != 22))
//                .build();
//
//        assertThatExceptionOfType(NonRenderingWhereClauseException.class).isThrownBy(() ->
//                selectModel.render(RenderingStrategies.MYBATIS3)
//        );
//    }
//
//    @Test
//    void testInConditionWithEmptyList() {
//        SelectModel selectModel = select(id, animalName, bodyWeight, brainWeight)
//                .from(animalData)
//                .where(id, isIn(Collections.emptyList()))
//                .build();
//
//        assertThatExceptionOfType(NonRenderingWhereClauseException.class).isThrownBy(() ->
//                selectModel.render(RenderingStrategies.MYBATIS3)
//        );
//    }
//
//    @Test
//    void testInCaseSensitiveCondition() {
//        try (SqlSession sqlSession = sqlSessionFactory.openSession()) {
//            AnimalDataMapper mapper = sqlSession.getMapper(AnimalDataMapper.class);
//
//            SelectStatementProvider selectStatement = select(id, animalName, bodyWeight, brainWeight)
//                    .from(animalData)
//                    .where(animalName, isInCaseInsensitive("yellow-bellied marmot", "verbet", null))
//                    .build()
//                    .render(RenderingStrategies.MYBATIS3);
//
//            List<ReactiveAnimalData> animals = mapper.selectMany(selectStatement);
//            assertThat(animals).hasSize(2);
//        }
//    }
//
//    @Test
//    void testNotInCondition() {
//        try (SqlSession sqlSession = sqlSessionFactory.openSession()) {
//            AnimalDataMapper mapper = sqlSession.getMapper(AnimalDataMapper.class);
//
//            SelectStatementProvider selectStatement = select(id, animalName, bodyWeight, brainWeight)
//                    .from(animalData)
//                    .where(id, isNotIn(5, 8, 10))
//                    .build()
//                    .render(RenderingStrategies.MYBATIS3);
//
//            List<ReactiveAnimalData> animals = mapper.selectMany(selectStatement);
//            assertThat(animals).hasSize(62);
//        }
//    }
//
//    @Test
//    void testNotInCaseSensitiveCondition() {
//        try (SqlSession sqlSession = sqlSessionFactory.openSession()) {
//            AnimalDataMapper mapper = sqlSession.getMapper(AnimalDataMapper.class);
//
//            SelectStatementProvider selectStatement = select(id, animalName, bodyWeight, brainWeight)
//                    .from(animalData)
//                    .where(animalName, isNotInCaseInsensitive("yellow-bellied marmot", "verbet"))
//                    .build()
//                    .render(RenderingStrategies.MYBATIS3);
//
//            List<ReactiveAnimalData> animals = mapper.selectMany(selectStatement);
//            assertThat(animals).hasSize(63);
//        }
//    }
//
//    @Test
//    void testNotInCaseSensitiveConditionWithNull() {
//        try (SqlSession sqlSession = sqlSessionFactory.openSession()) {
//            AnimalDataMapper mapper = sqlSession.getMapper(AnimalDataMapper.class);
//
//            SelectStatementProvider selectStatement = select(id, animalName, bodyWeight, brainWeight)
//                    .from(animalData)
//                    .where(animalName, isNotInCaseInsensitive((String)null))
//                    .build()
//                    .render(RenderingStrategies.MYBATIS3);
//
//            List<ReactiveAnimalData> animals = mapper.selectMany(selectStatement);
//            assertThat(animals).isEmpty();
//        }
//    }
//
//    @Test
//    void testNotInConditionWithEventuallyEmptyList() {
//        try (SqlSession sqlSession = sqlSessionFactory.openSession()) {
//            AnimalDataMapper mapper = sqlSession.getMapper(AnimalDataMapper.class);
//
//            SelectStatementProvider selectStatement = select(id, animalName, bodyWeight, brainWeight)
//                    .from(animalData)
//                    .where(id, isNotIn(null, 22, null).filter(Objects::nonNull).filter(i -> i != 22))
//                    .configureStatement(c -> c.setNonRenderingWhereClauseAllowed(true))
//                    .build()
//                    .render(RenderingStrategies.MYBATIS3);
//
//            assertThat(selectStatement.getSelectStatement())
//                    .isEqualTo("select id, animal_name, body_weight, brain_weight from AnimalData");
//            List<ReactiveAnimalData> animals = mapper.selectMany(selectStatement);
//            assertThat(animals).hasSize(65);
//        }
//    }
//
//    @Test
//    void testNotInConditionWithEventuallyEmptyListForceRendering() {
//        SelectModel selectModel = select(id, animalName, bodyWeight, brainWeight)
//                .from(animalData)
//                .where(id, isNotIn(null, 22, null)
//                        .filter(Objects::nonNull).filter(i -> i != 22))
//                .build();
//
//        assertThatExceptionOfType(NonRenderingWhereClauseException.class).isThrownBy(() ->
//                selectModel.render(RenderingStrategies.MYBATIS3)
//        );
//    }
//
//    @Test
//    void testLikeCondition() {
//        try (SqlSession sqlSession = sqlSessionFactory.openSession()) {
//            AnimalDataMapper mapper = sqlSession.getMapper(AnimalDataMapper.class);
//
//            SelectStatementProvider selectStatement = select(id, animalName, bodyWeight, brainWeight)
//                    .from(animalData)
//                    .where(animalName, isLike("%squirrel"))
//                    .build()
//                    .render(RenderingStrategies.MYBATIS3);
//
//            List<ReactiveAnimalData> animals = mapper.selectMany(selectStatement);
//            assertThat(animals).hasSize(2);
//        }
//    }
//
//    @Test
//    void testLikeCaseInsensitive() {
//        try (SqlSession sqlSession = sqlSessionFactory.openSession()) {
//            AnimalDataMapper mapper = sqlSession.getMapper(AnimalDataMapper.class);
//
//            SelectStatementProvider selectStatement = select(id, animalName, bodyWeight, brainWeight)
//                    .from(animalData)
//                    .where(animalName, isLikeCaseInsensitive("%squirrel"))
//                    .build()
//                    .render(RenderingStrategies.MYBATIS3);
//
//            List<ReactiveAnimalData> animals = mapper.selectMany(selectStatement);
//
//            assertAll(
//                    () -> assertThat(animals).hasSize(2),
//                    () -> assertThat(animals.get(0).getAnimalName()).isEqualTo("Ground squirrel"),
//                    () -> assertThat(animals.get(1).getAnimalName()).isEqualTo("Artic ground squirrel")
//            );
//        }
//    }
//
//    @Test
//    void testLikeLowerCase() {
//        try (SqlSession sqlSession = sqlSessionFactory.openSession()) {
//            CommonSelectMapper mapper = sqlSession.getMapper(CommonSelectMapper.class);
//
//            SelectStatementProvider selectStatement = select(id, lower(animalName).as("AnimalName"), bodyWeight, brainWeight)
//                    .from(animalData)
//                    .where(lower(animalName), isLike("%squirrel"))
//                    .build()
//                    .render(RenderingStrategies.MYBATIS3);
//
//            List<Map<String, Object>> animals = mapper.selectManyMappedRows(selectStatement);
//
//            assertAll(
//                    () -> assertThat(animals).hasSize(2),
//                    () -> assertThat(animals.get(0)).containsEntry("ANIMALNAME", "ground squirrel"),
//                    () -> assertThat(animals.get(1)).containsEntry("ANIMALNAME", "artic ground squirrel")
//            );
//        }
//    }
//
//    @Test
//    void testLikeUpperCase() {
//        try (SqlSession sqlSession = sqlSessionFactory.openSession()) {
//            CommonSelectMapper mapper = sqlSession.getMapper(CommonSelectMapper.class);
//
//            SelectStatementProvider selectStatement = select(id, upper(animalName).as("animalname"), bodyWeight, brainWeight)
//                    .from(animalData)
//                    .where(upper(animalName), isLike("%SQUIRREL"))
//                    .build()
//                    .render(RenderingStrategies.MYBATIS3);
//
//            List<Map<String, Object>> animals = mapper.selectManyMappedRows(selectStatement);
//
//            assertAll(
//                    () -> assertThat(animals).hasSize(2),
//                    () -> assertThat(animals.get(0)).containsEntry("ANIMALNAME", "GROUND SQUIRREL"),
//                    () -> assertThat(animals.get(1)).containsEntry("ANIMALNAME", "ARTIC GROUND SQUIRREL")
//            );
//        }
//    }
//
//    @Test
//    void testLength() {
//        try (SqlSession sqlSession = sqlSessionFactory.openSession()) {
//            CommonSelectMapper mapper = sqlSession.getMapper(CommonSelectMapper.class);
//
//            SelectStatementProvider selectStatement = select(id, upper(animalName).as("animalname"), bodyWeight, brainWeight)
//                    .from(animalData)
//                    .where(Length.length(animalName), isGreaterThan(22))
//                    .orderBy(id)
//                    .build()
//                    .render(RenderingStrategies.MYBATIS3);
//
//            List<Map<String, Object>> animals = mapper.selectManyMappedRows(selectStatement);
//
//            assertThat(animals).hasSize(2);
//            assertThat(animals.get(0)).containsEntry("ANIMALNAME", "LESSER SHORT-TAILED SHREW");
//            assertThat(animals.get(1)).containsEntry("ANIMALNAME", "AFRICAN GIANT POUCHED RAT");
//        }
//    }
//
//    @Test
//    void testNumericConstant() {
//        try (SqlSession sqlSession = sqlSessionFactory.openSession()) {
//            CommonSelectMapper mapper = sqlSession.getMapper(CommonSelectMapper.class);
//
//            SelectStatementProvider selectStatement = select(id, animalName, constant("3").as("some_number"))
//                    .from(animalData, "a")
//                    .where(add(bodyWeight, brainWeight), isGreaterThan(10000.0))
//                    .build()
//                    .render(RenderingStrategies.MYBATIS3);
//
//            String expected = "select a.id, a.animal_name, 3 as some_number "
//                    + "from AnimalData a "
//                    + "where (a.body_weight + a.brain_weight) > #{parameters.p1,jdbcType=DOUBLE}";
//
//            List<Map<String, Object>> animals = mapper.selectManyMappedRows(selectStatement);
//
//            assertAll(
//                    () -> assertThat(selectStatement.getSelectStatement()).isEqualTo(expected),
//                    () -> assertThat(animals).hasSize(3),
//                    () -> assertThat(animals.get(0)).containsEntry("ANIMAL_NAME", "African elephant"),
//                    () -> assertThat(animals.get(0)).containsEntry("SOME_NUMBER", 3),
//                    () -> assertThat(animals.get(1)).containsEntry("ANIMAL_NAME", "Dipliodocus"),
//                    () -> assertThat(animals.get(1)).containsEntry("SOME_NUMBER", 3),
//                    () -> assertThat(animals.get(2)).containsEntry("ANIMAL_NAME", "Brachiosaurus"),
//                    () -> assertThat(animals.get(2)).containsEntry("SOME_NUMBER", 3)
//            );
//        }
//    }
//
//    @Test
//    void testStringConstant() {
//        try (SqlSession sqlSession = sqlSessionFactory.openSession()) {
//            CommonSelectMapper mapper = sqlSession.getMapper(CommonSelectMapper.class);
//
//            SelectStatementProvider selectStatement = select(id, animalName, stringConstant("fred").as("some_string"))
//                    .from(animalData, "a")
//                    .where(add(bodyWeight, brainWeight), isGreaterThan(10000.0))
//                    .build()
//                    .render(RenderingStrategies.MYBATIS3);
//
//            String expected = "select a.id, a.animal_name, 'fred' as some_string "
//                    + "from AnimalData a "
//                    + "where (a.body_weight + a.brain_weight) > #{parameters.p1,jdbcType=DOUBLE}";
//
//            List<Map<String, Object>> animals = mapper.selectManyMappedRows(selectStatement);
//
//            assertAll(
//                    () -> assertThat(selectStatement.getSelectStatement()).isEqualTo(expected),
//                    () -> assertThat(animals).hasSize(3),
//                    () -> assertThat(animals.get(0)).containsEntry("ANIMAL_NAME", "African elephant"),
//                    () -> assertThat(animals.get(0)).containsEntry("SOME_STRING", "fred"),
//                    () -> assertThat(animals.get(1)).containsEntry("ANIMAL_NAME", "Dipliodocus"),
//                    () -> assertThat(animals.get(1)).containsEntry("SOME_STRING", "fred"),
//                    () -> assertThat(animals.get(2)).containsEntry("ANIMAL_NAME", "Brachiosaurus"),
//                    () -> assertThat(animals.get(2)).containsEntry("SOME_STRING", "fred")
//            );
//        }
//    }
//
//    @Test
//    void testAdd() {
//        try (SqlSession sqlSession = sqlSessionFactory.openSession()) {
//            CommonSelectMapper mapper = sqlSession.getMapper(CommonSelectMapper.class);
//
//            SelectStatementProvider selectStatement = select(id, animalName, add(bodyWeight, brainWeight).as("calculated_weight"))
//                    .from(animalData, "a")
//                    .where(add(bodyWeight, brainWeight), isGreaterThan(10000.0))
//                    .build()
//                    .render(RenderingStrategies.MYBATIS3);
//
//            String expected = "select a.id, a.animal_name, (a.body_weight + a.brain_weight) as calculated_weight "
//                    + "from AnimalData a "
//                    + "where (a.body_weight + a.brain_weight) > #{parameters.p1,jdbcType=DOUBLE}";
//
//            List<Map<String, Object>> animals = mapper.selectManyMappedRows(selectStatement);
//
//            assertAll(
//                    () -> assertThat(selectStatement.getSelectStatement()).isEqualTo(expected),
//                    () -> assertThat(animals).hasSize(3),
//                    () -> assertThat(animals.get(0)).containsEntry("ANIMAL_NAME", "African elephant"),
//                    () -> assertThat(animals.get(0)).containsEntry("CALCULATED_WEIGHT", 12366.0),
//                    () -> assertThat(animals.get(1)).containsEntry("ANIMAL_NAME", "Dipliodocus"),
//                    () -> assertThat(animals.get(1)).containsEntry("CALCULATED_WEIGHT", 11750.0),
//                    () -> assertThat(animals.get(2)).containsEntry("ANIMAL_NAME", "Brachiosaurus"),
//                    () -> assertThat(animals.get(2)).containsEntry("CALCULATED_WEIGHT", 87154.5)
//            );
//        }
//    }
//
//    @Test
//    void testAddConstant() {
//        try (SqlSession sqlSession = sqlSessionFactory.openSession()) {
//            CommonSelectMapper mapper = sqlSession.getMapper(CommonSelectMapper.class);
//
//            SelectStatementProvider selectStatement = select(id, animalName, add(bodyWeight, constant("22"), constant("33")).as("calculated_weight"))
//                    .from(animalData, "a")
//                    .where(add(bodyWeight, brainWeight), isGreaterThan(10000.0))
//                    .build()
//                    .render(RenderingStrategies.MYBATIS3);
//
//            String expected = "select a.id, a.animal_name, (a.body_weight + 22 + 33) as calculated_weight "
//                    + "from AnimalData a "
//                    + "where (a.body_weight + a.brain_weight) > #{parameters.p1,jdbcType=DOUBLE}";
//
//            List<Map<String, Object>> animals = mapper.selectManyMappedRows(selectStatement);
//
//            assertAll(
//                    () -> assertThat(selectStatement.getSelectStatement()).isEqualTo(expected),
//                    () -> assertThat(animals).hasSize(3),
//                    () -> assertThat(animals.get(0)).containsEntry("ANIMAL_NAME", "African elephant"),
//                    () -> assertThat(animals.get(0)).containsEntry("CALCULATED_WEIGHT", 5767.0),
//                    () -> assertThat(animals.get(1)).containsEntry("ANIMAL_NAME", "Dipliodocus"),
//                    () -> assertThat(animals.get(1)).containsEntry("CALCULATED_WEIGHT", 105.0),
//                    () -> assertThat(animals.get(2)).containsEntry("ANIMAL_NAME", "Brachiosaurus"),
//                    () -> assertThat(animals.get(2)).containsEntry("CALCULATED_WEIGHT", 209.5)
//            );
//        }
//    }
//
//    @Test
//    void testAddConstantWithConstantFirst() {
//        try (SqlSession sqlSession = sqlSessionFactory.openSession()) {
//            CommonSelectMapper mapper = sqlSession.getMapper(CommonSelectMapper.class);
//
//            SelectStatementProvider selectStatement = select(id, animalName, add(constant("22"), bodyWeight, constant("33")).as("calculated_weight"))
//                    .from(animalData, "a")
//                    .where(add(bodyWeight, brainWeight), isGreaterThan(10000.0))
//                    .build()
//                    .render(RenderingStrategies.MYBATIS3);
//
//            String expected = "select a.id, a.animal_name, (22 + a.body_weight + 33) as calculated_weight "
//                    + "from AnimalData a "
//                    + "where (a.body_weight + a.brain_weight) > #{parameters.p1,jdbcType=DOUBLE}";
//
//            List<Map<String, Object>> animals = mapper.selectManyMappedRows(selectStatement);
//
//            assertAll(
//                    () -> assertThat(selectStatement.getSelectStatement()).isEqualTo(expected),
//                    () -> assertThat(animals).hasSize(3),
//                    () -> assertThat(animals.get(0)).containsEntry("ANIMAL_NAME", "African elephant"),
//                    () -> assertThat(animals.get(0)).containsEntry("CALCULATED_WEIGHT", 5767.0),
//                    () -> assertThat(animals.get(1)).containsEntry("ANIMAL_NAME", "Dipliodocus"),
//                    () -> assertThat(animals.get(1)).containsEntry("CALCULATED_WEIGHT", 105.0),
//                    () -> assertThat(animals.get(2)).containsEntry("ANIMAL_NAME", "Brachiosaurus"),
//                    () -> assertThat(animals.get(2)).containsEntry("CALCULATED_WEIGHT", 209.5)
//            );
//        }
//    }
//
//    @Test
//    void testConcat() {
//        try (SqlSession sqlSession = sqlSessionFactory.openSession()) {
//            CommonSelectMapper mapper = sqlSession.getMapper(CommonSelectMapper.class);
//
//            SelectStatementProvider selectStatement = select(id, concat(animalName, stringConstant(" - The Legend")).as("display_name"))
//                    .from(animalData, "a")
//                    .where(add(bodyWeight, brainWeight), isGreaterThan(10000.0))
//                    .build()
//                    .render(RenderingStrategies.MYBATIS3);
//
//            String expected = "select a.id, concat(a.animal_name, ' - The Legend') as display_name "
//                    + "from AnimalData a "
//                    + "where (a.body_weight + a.brain_weight) > #{parameters.p1,jdbcType=DOUBLE}";
//
//            List<Map<String, Object>> animals = mapper.selectManyMappedRows(selectStatement);
//
//            assertAll(
//                    () -> assertThat(selectStatement.getSelectStatement()).isEqualTo(expected),
//                    () -> assertThat(animals).hasSize(3),
//                    () -> assertThat(animals.get(0)).containsEntry("DISPLAY_NAME", "African elephant - The Legend"),
//                    () -> assertThat(animals.get(1)).containsEntry("DISPLAY_NAME", "Dipliodocus - The Legend"),
//                    () -> assertThat(animals.get(2)).containsEntry("DISPLAY_NAME", "Brachiosaurus - The Legend")
//            );
//        }
//    }
//
//    @Test
//    void testConcatenate() {
//        try (SqlSession sqlSession = sqlSessionFactory.openSession()) {
//            CommonSelectMapper mapper = sqlSession.getMapper(CommonSelectMapper.class);
//
//            SelectStatementProvider selectStatement = select(id, concatenate(animalName, stringConstant(" - The Legend")).as("display_name"))
//                    .from(animalData, "a")
//                    .where(add(bodyWeight, brainWeight), isGreaterThan(10000.0))
//                    .build()
//                    .render(RenderingStrategies.MYBATIS3);
//
//            String expected = "select a.id, (a.animal_name || ' - The Legend') as display_name "
//                    + "from AnimalData a "
//                    + "where (a.body_weight + a.brain_weight) > #{parameters.p1,jdbcType=DOUBLE}";
//
//            List<Map<String, Object>> animals = mapper.selectManyMappedRows(selectStatement);
//
//            assertAll(
//                    () -> assertThat(selectStatement.getSelectStatement()).isEqualTo(expected),
//                    () -> assertThat(animals).hasSize(3),
//                    () -> assertThat(animals.get(0)).containsEntry("DISPLAY_NAME", "African elephant - The Legend"),
//                    () -> assertThat(animals.get(1)).containsEntry("DISPLAY_NAME", "Dipliodocus - The Legend"),
//                    () -> assertThat(animals.get(2)).containsEntry("DISPLAY_NAME", "Brachiosaurus - The Legend")
//            );
//        }
//    }
//
//    @Test
//    void testConcatenateConstantFirst() {
//        try (SqlSession sqlSession = sqlSessionFactory.openSession()) {
//            CommonSelectMapper mapper = sqlSession.getMapper(CommonSelectMapper.class);
//
//            SelectStatementProvider selectStatement = select(id, concatenate(stringConstant("Name: "), animalName).as("display_name"))
//                    .from(animalData, "a")
//                    .where(add(bodyWeight, brainWeight), isGreaterThan(10000.0))
//                    .build()
//                    .render(RenderingStrategies.MYBATIS3);
//
//            String expected = "select a.id, ('Name: ' || a.animal_name) as display_name "
//                    + "from AnimalData a "
//                    + "where (a.body_weight + a.brain_weight) > #{parameters.p1,jdbcType=DOUBLE}";
//
//            List<Map<String, Object>> animals = mapper.selectManyMappedRows(selectStatement);
//
//            assertAll(
//                    () -> assertThat(selectStatement.getSelectStatement()).isEqualTo(expected),
//                    () -> assertThat(animals).hasSize(3),
//                    () -> assertThat(animals.get(0)).containsEntry("DISPLAY_NAME", "Name: African elephant"),
//                    () -> assertThat(animals.get(1)).containsEntry("DISPLAY_NAME", "Name: Dipliodocus"),
//                    () -> assertThat(animals.get(2)).containsEntry("DISPLAY_NAME", "Name: Brachiosaurus")
//            );
//        }
//    }
//
//    @Test
//    void testDivide() {
//        try (SqlSession sqlSession = sqlSessionFactory.openSession()) {
//            CommonSelectMapper mapper = sqlSession.getMapper(CommonSelectMapper.class);
//
//            SelectStatementProvider selectStatement = select(id, animalName, divide(bodyWeight, brainWeight).as("calculated_weight"))
//                    .from(animalData, "a")
//                    .where(add(bodyWeight, brainWeight), isGreaterThan(10000.0))
//                    .build()
//                    .render(RenderingStrategies.MYBATIS3);
//
//            String expected = "select a.id, a.animal_name, (a.body_weight / a.brain_weight) as calculated_weight "
//                    + "from AnimalData a "
//                    + "where (a.body_weight + a.brain_weight) > #{parameters.p1,jdbcType=DOUBLE}";
//
//            List<Map<String, Object>> animals = mapper.selectManyMappedRows(selectStatement);
//
//            assertAll(
//                    () -> assertThat(selectStatement.getSelectStatement()).isEqualTo(expected),
//                    () -> assertThat(animals).hasSize(3),
//                    () -> assertThat(animals.get(0)).containsEntry("ANIMAL_NAME", "African elephant"),
//                    () -> assertThat((Double) animals.get(0).get("CALCULATED_WEIGHT")).isEqualTo(0.858, within(0.001)),
//                    () -> assertThat(animals.get(1)).containsEntry("ANIMAL_NAME", "Dipliodocus"),
//                    () -> assertThat((Double) animals.get(1).get("CALCULATED_WEIGHT")).isEqualTo(0.004, within(0.001)),
//                    () -> assertThat(animals.get(2)).containsEntry("ANIMAL_NAME", "Brachiosaurus"),
//                    () -> assertThat((Double) animals.get(2).get("CALCULATED_WEIGHT")).isEqualTo(0.001, within(0.001))
//            );
//        }
//    }
//
//    @Test
//    void testDivideConstant() {
//        try (SqlSession sqlSession = sqlSessionFactory.openSession()) {
//            CommonSelectMapper mapper = sqlSession.getMapper(CommonSelectMapper.class);
//
//            SelectStatementProvider selectStatement = select(id, animalName, divide(bodyWeight, constant("10.0")).as("calculated_weight"))
//                    .from(animalData, "a")
//                    .where(add(bodyWeight, brainWeight), isGreaterThan(10000.0))
//                    .build()
//                    .render(RenderingStrategies.MYBATIS3);
//
//            String expected = "select a.id, a.animal_name, (a.body_weight / 10.0) as calculated_weight "
//                    + "from AnimalData a "
//                    + "where (a.body_weight + a.brain_weight) > #{parameters.p1,jdbcType=DOUBLE}";
//
//            List<Map<String, Object>> animals = mapper.selectManyMappedRows(selectStatement);
//
//            assertAll(
//                    () -> assertThat(selectStatement.getSelectStatement()).isEqualTo(expected),
//                    () -> assertThat(animals).hasSize(3),
//                    () -> assertThat(animals.get(0)).containsEntry("ANIMAL_NAME", "African elephant"),
//                    () -> assertThat(animals.get(0)).containsEntry("CALCULATED_WEIGHT", 571.2),
//                    () -> assertThat(animals.get(1)).containsEntry("ANIMAL_NAME", "Dipliodocus"),
//                    () -> assertThat(animals.get(1)).containsEntry("CALCULATED_WEIGHT", 5.0),
//                    () -> assertThat(animals.get(2)).containsEntry("ANIMAL_NAME", "Brachiosaurus"),
//                    () -> assertThat(animals.get(2)).containsEntry("CALCULATED_WEIGHT", 15.45)
//            );
//        }
//    }
//
//    @Test
//    void testMultiply() {
//        try (SqlSession sqlSession = sqlSessionFactory.openSession()) {
//            CommonSelectMapper mapper = sqlSession.getMapper(CommonSelectMapper.class);
//
//            SelectStatementProvider selectStatement = select(id, animalName, multiply(bodyWeight, brainWeight).as("calculated_weight"))
//                    .from(animalData, "a")
//                    .where(add(bodyWeight, brainWeight), isGreaterThan(10000.0))
//                    .build()
//                    .render(RenderingStrategies.MYBATIS3);
//
//            String expected = "select a.id, a.animal_name, (a.body_weight * a.brain_weight) as calculated_weight "
//                    + "from AnimalData a "
//                    + "where (a.body_weight + a.brain_weight) > #{parameters.p1,jdbcType=DOUBLE}";
//
//            List<Map<String, Object>> animals = mapper.selectManyMappedRows(selectStatement);
//
//            assertAll(
//                    () -> assertThat(selectStatement.getSelectStatement()).isEqualTo(expected),
//                    () -> assertThat(animals).hasSize(3),
//                    () -> assertThat(animals.get(0)).containsEntry("ANIMAL_NAME", "African elephant"),
//                    () -> assertThat(animals.get(0)).containsEntry("CALCULATED_WEIGHT", 38007648.0),
//                    () -> assertThat(animals.get(1)).containsEntry("ANIMAL_NAME", "Dipliodocus"),
//                    () -> assertThat(animals.get(1)).containsEntry("CALCULATED_WEIGHT", 585000.0),
//                    () -> assertThat(animals.get(2)).containsEntry("ANIMAL_NAME", "Brachiosaurus"),
//                    () -> assertThat(animals.get(2)).containsEntry("CALCULATED_WEIGHT", 13441500.0)
//            );
//        }
//    }
//
//    @Test
//    void testMultiplyConstant() {
//        try (SqlSession sqlSession = sqlSessionFactory.openSession()) {
//            CommonSelectMapper mapper = sqlSession.getMapper(CommonSelectMapper.class);
//
//            SelectStatementProvider selectStatement = select(id, animalName, multiply(bodyWeight, constant("2.0")).as("calculated_weight"))
//                    .from(animalData, "a")
//                    .where(add(bodyWeight, brainWeight), isGreaterThan(10000.0))
//                    .build()
//                    .render(RenderingStrategies.MYBATIS3);
//
//            String expected = "select a.id, a.animal_name, (a.body_weight * 2.0) as calculated_weight "
//                    + "from AnimalData a "
//                    + "where (a.body_weight + a.brain_weight) > #{parameters.p1,jdbcType=DOUBLE}";
//
//            List<Map<String, Object>> animals = mapper.selectManyMappedRows(selectStatement);
//
//            assertAll(
//                    () -> assertThat(selectStatement.getSelectStatement()).isEqualTo(expected),
//                    () -> assertThat(animals).hasSize(3),
//                    () -> assertThat(animals.get(0)).containsEntry("ANIMAL_NAME", "African elephant"),
//                    () -> assertThat(animals.get(0)).containsEntry("CALCULATED_WEIGHT", 11424.0),
//                    () -> assertThat(animals.get(1)).containsEntry("ANIMAL_NAME", "Dipliodocus"),
//                    () -> assertThat(animals.get(1)).containsEntry("CALCULATED_WEIGHT", 100.0),
//                    () -> assertThat(animals.get(2)).containsEntry("ANIMAL_NAME", "Brachiosaurus"),
//                    () -> assertThat(animals.get(2)).containsEntry("CALCULATED_WEIGHT", 309.0)
//            );
//        }
//    }
//
//    @Test
//    void testSubtract() {
//        try (SqlSession sqlSession = sqlSessionFactory.openSession()) {
//            CommonSelectMapper mapper = sqlSession.getMapper(CommonSelectMapper.class);
//
//            SelectStatementProvider selectStatement = select(id, animalName, subtract(bodyWeight, brainWeight).as("calculated_weight"))
//                    .from(animalData, "a")
//                    .where(add(bodyWeight, brainWeight), isGreaterThan(10000.0))
//                    .build()
//                    .render(RenderingStrategies.MYBATIS3);
//
//            String expected = "select a.id, a.animal_name, (a.body_weight - a.brain_weight) as calculated_weight "
//                    + "from AnimalData a "
//                    + "where (a.body_weight + a.brain_weight) > #{parameters.p1,jdbcType=DOUBLE}";
//
//            List<Map<String, Object>> animals = mapper.selectManyMappedRows(selectStatement);
//
//            assertAll(
//                    () -> assertThat(selectStatement.getSelectStatement()).isEqualTo(expected),
//                    () -> assertThat(animals).hasSize(3),
//                    () -> assertThat(animals.get(0)).containsEntry("ANIMAL_NAME", "African elephant"),
//                    () -> assertThat(animals.get(0)).containsEntry("CALCULATED_WEIGHT", -942.0),
//                    () -> assertThat(animals.get(1)).containsEntry("ANIMAL_NAME", "Dipliodocus"),
//                    () -> assertThat(animals.get(1)).containsEntry("CALCULATED_WEIGHT", -11650.0),
//                    () -> assertThat(animals.get(2)).containsEntry("ANIMAL_NAME", "Brachiosaurus"),
//                    () -> assertThat(animals.get(2)).containsEntry("CALCULATED_WEIGHT", -86845.5)
//            );
//        }
//    }
//
//    @Test
//    void testSubtractConstant() {
//        try (SqlSession sqlSession = sqlSessionFactory.openSession()) {
//            CommonSelectMapper mapper = sqlSession.getMapper(CommonSelectMapper.class);
//
//            SelectStatementProvider selectStatement = select(id, animalName, subtract(bodyWeight, constant("5.5")).as("calculated_weight"))
//                    .from(animalData, "a")
//                    .where(add(bodyWeight, brainWeight), isGreaterThan(10000.0))
//                    .build()
//                    .render(RenderingStrategies.MYBATIS3);
//
//            String expected = "select a.id, a.animal_name, (a.body_weight - 5.5) as calculated_weight "
//                    + "from AnimalData a "
//                    + "where (a.body_weight + a.brain_weight) > #{parameters.p1,jdbcType=DOUBLE}";
//
//            List<Map<String, Object>> animals = mapper.selectManyMappedRows(selectStatement);
//
//            assertAll(
//                    () -> assertThat(selectStatement.getSelectStatement()).isEqualTo(expected),
//                    () -> assertThat(animals).hasSize(3),
//                    () -> assertThat(animals.get(0)).containsEntry("ANIMAL_NAME", "African elephant"),
//                    () -> assertThat(animals.get(0)).containsEntry("CALCULATED_WEIGHT", 5706.5),
//                    () -> assertThat(animals.get(1)).containsEntry("ANIMAL_NAME", "Dipliodocus"),
//                    () -> assertThat(animals.get(1)).containsEntry("CALCULATED_WEIGHT", 44.5),
//                    () -> assertThat(animals.get(2)).containsEntry("ANIMAL_NAME", "Brachiosaurus"),
//                    () -> assertThat(animals.get(2)).containsEntry("CALCULATED_WEIGHT", 149.0)
//            );
//        }
//    }
//
//    @Test
//    void testGeneralOperator() {
//        try (SqlSession sqlSession = sqlSessionFactory.openSession()) {
//            CommonSelectMapper mapper = sqlSession.getMapper(CommonSelectMapper.class);
//
//            SelectStatementProvider selectStatement = select(id, animalName, applyOperator("-", bodyWeight, brainWeight).as("calculated_weight"))
//                    .from(animalData, "a")
//                    .where(add(bodyWeight, brainWeight), isGreaterThan(10000.0))
//                    .build()
//                    .render(RenderingStrategies.MYBATIS3);
//
//            String expected = "select a.id, a.animal_name, (a.body_weight - a.brain_weight) as calculated_weight "
//                    + "from AnimalData a "
//                    + "where (a.body_weight + a.brain_weight) > #{parameters.p1,jdbcType=DOUBLE}";
//
//            List<Map<String, Object>> animals = mapper.selectManyMappedRows(selectStatement);
//
//            assertAll(
//                    () -> assertThat(selectStatement.getSelectStatement()).isEqualTo(expected),
//                    () -> assertThat(animals).hasSize(3),
//                    () -> assertThat(animals.get(0)).containsEntry("ANIMAL_NAME", "African elephant"),
//                    () -> assertThat(animals.get(0)).containsEntry("CALCULATED_WEIGHT", -942.0),
//                    () -> assertThat(animals.get(1)).containsEntry("ANIMAL_NAME", "Dipliodocus"),
//                    () -> assertThat(animals.get(1)).containsEntry("CALCULATED_WEIGHT", -11650.0),
//                    () -> assertThat(animals.get(2)).containsEntry("ANIMAL_NAME", "Brachiosaurus"),
//                    () -> assertThat(animals.get(2)).containsEntry("CALCULATED_WEIGHT", -86845.5)
//            );
//        }
//    }

    @Test
    void testComplexExpression() {
        SelectStatementProvider selectStatement = select(id, animalName, add(multiply(bodyWeight, constant("5.5")), subtract(brainWeight, constant("2"))).as("calculated_weight"))
                .from(animalData, "a")
                .where(add(bodyWeight, brainWeight), isGreaterThan(10000.0))
                .build()
                .render(R2DBC);

        String expected = "select a.id, a.animal_name, ((a.body_weight * 5.5) + (a.brain_weight - 2)) as calculated_weight "
                + "from AnimalData a "
                + "where (a.body_weight + a.brain_weight) > $1";

        List<Map<String, Object>> animals = connectionFactory.selectMany(selectStatement, this::rawMapper).collectList().block();

        assertThat(selectStatement.getSelectStatement()).isEqualTo(expected);
        assertThat(animals).hasSize(3);
        assertThat(animals.get(0)).containsEntry("ANIMAL_NAME","African elephant");
        assertThat(animals.get(0)).containsEntry("CALCULATED_WEIGHT", new BigDecimal(38068));
        assertThat(animals.get(1)).containsEntry("ANIMAL_NAME", "Dipliodocus");
        assertThat(animals.get(1)).containsEntry("CALCULATED_WEIGHT", new BigDecimal(11973));
        assertThat(animals.get(2)).containsEntry("ANIMAL_NAME", "Brachiosaurus");
        assertThat(animals.get(2)).containsEntry("CALCULATED_WEIGHT", new BigDecimal("87847.75"));
    }

//    @Test
//    void testNotLikeCondition() {
//        try (SqlSession sqlSession = sqlSessionFactory.openSession()) {
//            AnimalDataMapper mapper = sqlSession.getMapper(AnimalDataMapper.class);
//
//            SelectStatementProvider selectStatement = select(id, animalName, bodyWeight, brainWeight)
//                    .from(animalData)
//                    .where(animalName, isNotLike("%squirrel"))
//                    .build()
//                    .render(RenderingStrategies.MYBATIS3);
//
//            List<ReactiveAnimalData> animals = mapper.selectMany(selectStatement);
//            assertThat(animals).hasSize(63);
//        }
//    }
//
//    @Test
//    void testNotLikeCaseInsensistveCondition() {
//        try (SqlSession sqlSession = sqlSessionFactory.openSession()) {
//            AnimalDataMapper mapper = sqlSession.getMapper(AnimalDataMapper.class);
//
//            SelectStatementProvider selectStatement = select(id, animalName, bodyWeight, brainWeight)
//                    .from(animalData)
//                    .where(animalName, isNotLikeCaseInsensitive("%squirrel"))
//                    .build()
//                    .render(RenderingStrategies.MYBATIS3);
//
//            List<ReactiveAnimalData> animals = mapper.selectMany(selectStatement);
//            assertThat(animals).hasSize(63);
//        }
//    }

    @Test
    @DirtiesContext
    void testDeleteThreeRows() {
        DeleteStatementProvider deleteStatement = deleteFrom(animalData)
                .where(id, isIn(5, 8, 10))
                .build()
                .render(R2DBC);

        Long rowCount = connectionFactory.delete(deleteStatement).block();
        assertThat(rowCount).isEqualTo(3);
    }

//    @Test
//    void testComplexDelete() {
//        try (SqlSession sqlSession = sqlSessionFactory.openSession()) {
//            AnimalDataMapper mapper = sqlSession.getMapper(AnimalDataMapper.class);
//
//            DeleteStatementProvider deleteStatement = deleteFrom(animalData)
//                    .where(id, isLessThan(10))
//                    .or(id, isGreaterThan(60))
//                    .build()
//                    .render(RenderingStrategies.MYBATIS3);
//
//            int rowCount = mapper.delete(deleteStatement);
//            assertThat(rowCount).isEqualTo(14);
//        }
//    }
//
//    @Test
//    void testIsNullCondition() {
//        try (SqlSession sqlSession = sqlSessionFactory.openSession()) {
//            AnimalDataMapper mapper = sqlSession.getMapper(AnimalDataMapper.class);
//
//            SelectStatementProvider selectStatement = select(id, animalName, bodyWeight, brainWeight)
//                    .from(animalData)
//                    .where(id, isNull())
//                    .build()
//                    .render(RenderingStrategies.MYBATIS3);
//
//            List<ReactiveAnimalData> animals = mapper.selectMany(selectStatement);
//            assertThat(animals).isEmpty();
//        }
//    }
//
//    @Test
//    void testIsNotNullCondition() {
//        try (SqlSession sqlSession = sqlSessionFactory.openSession()) {
//            AnimalDataMapper mapper = sqlSession.getMapper(AnimalDataMapper.class);
//
//            SelectStatementProvider selectStatement = select(id, animalName, bodyWeight, brainWeight)
//                    .from(animalData)
//                    .where(id, isNotNull())
//                    .build()
//                    .render(RenderingStrategies.MYBATIS3);
//
//            List<ReactiveAnimalData> animals = mapper.selectMany(selectStatement);
//            assertThat(animals).hasSize(65);
//        }
//    }
//
//    @Test
//    void testComplexCondition() {
//        try (SqlSession sqlSession = sqlSessionFactory.openSession()) {
//            AnimalDataMapper mapper = sqlSession.getMapper(AnimalDataMapper.class);
//
//            SelectStatementProvider selectStatement = select(id, animalName, bodyWeight, brainWeight)
//                    .from(animalData)
//                    .where(id, isIn(1, 5, 7))
//                    .or(id, isIn(2, 6, 8), and(animalName, isLike("%bat")))
//                    .or(id, isGreaterThan(60))
//                    .and(bodyWeight, isBetween(1.0).and(3.0))
//                    .orderBy(id.descending(), bodyWeight)
//                    .build()
//                    .render(RenderingStrategies.MYBATIS3);
//
//            List<ReactiveAnimalData> animals = mapper.selectMany(selectStatement);
//            assertThat(animals).hasSize(4);
//        }
//    }

    @Test
    @DirtiesContext
    void testUpdate() {
        UpdateStatementProvider updateStatement = update(animalData)
                .set(bodyWeight).equalTo(2.6)
                .set(animalName).equalToNull()
                .where(id, isIn(1, 5, 7))
                .or(id, isIn(2, 6, 8), and(animalName, isLike("%bat")))
                .or(id, isGreaterThan(60))
                .and(bodyWeight, isBetween(1.0).and(3.0))
                .build()
                .render(R2DBC);

        Long rows = connectionFactory.update(updateStatement).block();
        assertThat(rows).isEqualTo(4);
    }

    @Test
    @DirtiesContext
    void testUpdateValueOrNullWithValue() {
        UpdateStatementProvider updateStatement = update(animalData)
                .set(animalName).equalToOrNull("fred")
                .where(id, isEqualTo(1))
                .build()
                .render(R2DBC);

        assertThat(updateStatement.getUpdateStatement()).isEqualTo(
                "update AnimalData set animal_name = $1 where id = $2");
        Long rows = connectionFactory.update(updateStatement).block();
        assertThat(rows).isEqualTo(1);
    }

    @Test
    @DirtiesContext
    void testUpdateValueOrNullWithNull() {
        UpdateStatementProvider updateStatement = update(animalData)
                .set(animalName).equalToOrNull((String) null)
                .where(id, isEqualTo(1))
                .build()
                .render(R2DBC);

        assertThat(updateStatement.getUpdateStatement()).isEqualTo(
                "update AnimalData set animal_name = null where id = $1");
        Long rows = connectionFactory.update(updateStatement).block();
        assertThat(rows).isEqualTo(1);
    }

//    @Test
//    void testInsert() {
//        AnimalData record = new AnimalData();
//        record.setId(100);
//        record.setAnimalName("Old Shep");
//        record.setBodyWeight(22.5);
//        record.setBrainWeight(1.2);
//
//        InsertStatementProvider<AnimalData> insertStatement = insert(record)
//                .into(animalData)
//                .map(id).toProperty("id")
//                .map(animalName).toProperty("animalName")
//                .map(bodyWeight).toProperty("bodyWeight")
//                .map(brainWeight).toProperty("brainWeight")
//                .build()
//                .render(RenderingStrategies.R2DBC);
//
//        int rows = mapper.insert(insertStatement);
//        assertThat(rows).isEqualTo(1);
//    }

//    @Test
//    void testInsertNull() {
//        try (SqlSession sqlSession = sqlSessionFactory.openSession()) {
//            AnimalDataMapper mapper = sqlSession.getMapper(AnimalDataMapper.class);
//            ReactiveAnimalData record = new ReactiveAnimalData();
//            record.setId(100);
//            record.setAnimalName("Old Shep");
//            record.setBodyWeight(22.5);
//            record.setBrainWeight(1.2);
//
//            InsertStatementProvider<ReactiveAnimalData> insertStatement = insert(record)
//                    .into(animalData)
//                    .map(id).toProperty("id")
//                    .map(animalName).toNull()
//                    .map(bodyWeight).toProperty("bodyWeight")
//                    .map(brainWeight).toProperty("brainWeight")
//                    .build()
//                    .render(RenderingStrategies.MYBATIS3);
//
//            int rows = mapper.insert(insertStatement);
//            assertThat(rows).isEqualTo(1);
//        }
//    }
//
//    @Test
//    void testBulkInsert() {
//        try (SqlSession sqlSession = sqlSessionFactory.openSession(ExecutorType.BATCH)) {
//            AnimalDataMapper mapper = sqlSession.getMapper(AnimalDataMapper.class);
//            List<ReactiveAnimalData> records = new ArrayList<>();
//            ReactiveAnimalData record = new ReactiveAnimalData();
//            record.setId(100);
//            record.setAnimalName("Old Shep");
//            record.setBodyWeight(22.5);
//            records.add(record);
//
//            record = new ReactiveAnimalData();
//            record.setId(101);
//            record.setAnimalName("Old Dan");
//            record.setBodyWeight(22.5);
//            records.add(record);
//
//            BatchInsert<ReactiveAnimalData> batchInsert = insertBatch(records)
//                    .into(animalData)
//                    .map(id).toProperty("id")
//                    .map(animalName).toNull()
//                    .map(bodyWeight).toProperty("bodyWeight")
//                    .map(brainWeight).toConstant("1.2")
//                    .build()
//                    .render(RenderingStrategies.MYBATIS3);
//
//            batchInsert.insertStatements().forEach(mapper::insert);
//            sqlSession.commit();
//
//            SelectStatementProvider selectStatement = select(id, animalName, bodyWeight, brainWeight)
//                    .from(animalData)
//                    .where(id, isIn(100, 101))
//                    .orderBy(id)
//                    .build()
//                    .render(RenderingStrategies.MYBATIS3);
//
//            List<ReactiveAnimalData> animals = mapper.selectMany(selectStatement);
//
//            assertAll(
//                    () -> assertThat(animals).hasSize(2),
//                    () -> assertThat(animals.get(0).getId()).isEqualTo(100),
//                    () -> assertThat(animals.get(0).getBrainWeight()).isEqualTo(1.2),
//                    () -> assertThat(animals.get(0).getAnimalName()).isNull(),
//                    () -> assertThat(animals.get(1).getId()).isEqualTo(101),
//                    () -> assertThat(animals.get(1).getBrainWeight()).isEqualTo(1.2),
//                    () -> assertThat(animals.get(1).getAnimalName()).isNull()
//            );
//        }
//    }

    @Test
    @DirtiesContext
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

        MultiRowInsertStatementProvider<AnimalData> multiRowInsert = insertMultiple(records)
                .into(animalData)
                .map(id).toProperty("id")
                .map(animalName).toNull()
                .map(bodyWeight).toProperty("bodyWeight")
                .map(brainWeight).toConstant("1.2")
                .build()
                .render(R2DBC);

        String expected = "insert into AnimalData (id, animal_name, body_weight, brain_weight) values ($1, null, $2, 1.2) ($3, null, $4, 1.2)";
        assertThat(multiRowInsert.getInsertStatement()).isEqualTo(expected);
//        batchInsert.insertStatements().forEach(mapper::insert);
//        sqlSession.commit();

        SelectStatementProvider selectStatement = select(id, animalName, bodyWeight, brainWeight)
                .from(animalData)
                .where(id, isIn(100, 101))
                .orderBy(id)
                .build()
                .render(R2DBC);

        List<AnimalData> animals = connectionFactory.selectMany(selectStatement, this::rowMapper).collectList().block();

        assertThat(animals).hasSize(2);
        assertThat(animals.get(0).getId()).isEqualTo(100);
        assertThat(animals.get(0).getBrainWeight()).isEqualTo(1.2);
        assertThat(animals.get(0).getAnimalName()).isNull();
        assertThat(animals.get(1).getId()).isEqualTo(101);
        assertThat(animals.get(1).getBrainWeight()).isEqualTo(1.2);
        assertThat(animals.get(1).getAnimalName()).isNull();
    }

//    @Test
//    void testBulkInsert2() {
//        try (SqlSession sqlSession = sqlSessionFactory.openSession(ExecutorType.BATCH)) {
//            AnimalDataMapper mapper = sqlSession.getMapper(AnimalDataMapper.class);
//            List<ReactiveAnimalData> records = new ArrayList<>();
//            ReactiveAnimalData record = new ReactiveAnimalData();
//            record.setId(100);
//            record.setAnimalName("Old Shep");
//            record.setBodyWeight(22.5);
//            records.add(record);
//
//            record = new ReactiveAnimalData();
//            record.setId(101);
//            record.setAnimalName("Old Dan");
//            record.setBodyWeight(22.5);
//            records.add(record);
//
//            BatchInsert<ReactiveAnimalData> batchInsert = insertBatch(records)
//                    .into(animalData)
//                    .map(id).toProperty("id")
//                    .map(animalName).toStringConstant("Old Fred")
//                    .map(bodyWeight).toProperty("bodyWeight")
//                    .map(brainWeight).toConstant("1.2")
//                    .build()
//                    .render(RenderingStrategies.MYBATIS3);
//
//            batchInsert.insertStatements().forEach(mapper::insert);
//            mapper.flush();
//
//            SelectStatementProvider selectStatement = select(id, animalName, bodyWeight, brainWeight)
//                    .from(animalData)
//                    .where(id, isIn(100, 101))
//                    .orderBy(id)
//                    .build()
//                    .render(RenderingStrategies.MYBATIS3);
//
//            List<ReactiveAnimalData> animals = mapper.selectMany(selectStatement);
//
//            assertAll(
//                    () -> assertThat(animals).hasSize(2),
//                    () -> assertThat(animals.get(0).getId()).isEqualTo(100),
//                    () -> assertThat(animals.get(0).getBrainWeight()).isEqualTo(1.2),
//                    () -> assertThat(animals.get(0).getAnimalName()).isEqualTo("Old Fred"),
//                    () -> assertThat(animals.get(1).getId()).isEqualTo(101),
//                    () -> assertThat(animals.get(1).getBrainWeight()).isEqualTo(1.2),
//                    () -> assertThat(animals.get(1).getAnimalName()).isEqualTo("Old Fred")
//            );
//        }
//    }
//
//    @Test
//    void testOrderByAndDistinct() {
//        try (SqlSession sqlSession = sqlSessionFactory.openSession()) {
//            AnimalDataMapper mapper = sqlSession.getMapper(AnimalDataMapper.class);
//
//            SelectStatementProvider selectStatement = selectDistinct(id, animalName, bodyWeight, brainWeight)
//                    .from(animalData)
//                    .where(id, isLessThan(10))
//                    .or(id,  isGreaterThan(60))
//                    .orderBy(id.descending(), animalName)
//                    .build()
//                    .render(RenderingStrategies.MYBATIS3);
//
//            List<ReactiveAnimalData> rows = mapper.selectMany(selectStatement);
//
//            assertAll(
//                    () -> assertThat(rows).hasSize(14),
//                    () -> assertThat(rows.get(0).getId()).isEqualTo(65)
//            );
//        }
//    }
//
//    @Test
//    void testOrderByWithFullClause() {
//        try (SqlSession sqlSession = sqlSessionFactory.openSession()) {
//            AnimalDataMapper mapper = sqlSession.getMapper(AnimalDataMapper.class);
//
//            SelectStatementProvider selectStatement = select(id, animalName, bodyWeight, brainWeight)
//                    .from(animalData)
//                    .where(id, isLessThan(10))
//                    .or(id,  isGreaterThan(60))
//                    .orderBy(id.descending())
//                    .build()
//                    .render(RenderingStrategies.MYBATIS3);
//
//            List<ReactiveAnimalData> rows = mapper.selectMany(selectStatement);
//
//            assertAll(
//                    () -> assertThat(rows).hasSize(14),
//                    () -> assertThat(rows.get(0).getId()).isEqualTo(65)
//            );
//        }
//    }
//
//    @Test
//    void testCount() {
//        try (SqlSession sqlSession = sqlSessionFactory.openSession()) {
//            CommonSelectMapper mapper = sqlSession.getMapper(CommonSelectMapper.class);
//
//            SelectStatementProvider selectStatement = select(count().as("total"))
//                    .from(animalData, "a")
//                    .build()
//                    .render(RenderingStrategies.MYBATIS3);
//
//            Long count = mapper.selectOneLong(selectStatement);
//
//            assertAll(
//                    () -> assertThat(selectStatement.getSelectStatement()).isEqualTo("select count(*) as total from AnimalData a"),
//                    () -> assertThat(count).isEqualTo(65)
//            );
//        }
//    }
//
//    @Test
//    void testCountField() {
//        try (SqlSession sqlSession = sqlSessionFactory.openSession()) {
//            CommonSelectMapper mapper = sqlSession.getMapper(CommonSelectMapper.class);
//
//            SelectStatementProvider selectStatement = select(count(brainWeight).as("total"))
//                    .from(animalData, "a")
//                    .build()
//                    .render(RenderingStrategies.MYBATIS3);
//
//            Long count = mapper.selectOneLong(selectStatement);
//
//            assertAll(
//                    () -> assertThat(selectStatement.getSelectStatement()).isEqualTo("select count(a.brain_weight) as total from AnimalData a"),
//                    () -> assertThat(count).isEqualTo(65)
//            );
//        }
//    }
//
//    @Test
//    void testCountNoAlias() {
//        try (SqlSession sqlSession = sqlSessionFactory.openSession()) {
//            CommonSelectMapper mapper = sqlSession.getMapper(CommonSelectMapper.class);
//
//            SelectStatementProvider selectStatement = select(count())
//                    .from(animalData)
//                    .build()
//                    .render(RenderingStrategies.MYBATIS3);
//
//            Long count = mapper.selectOneLong(selectStatement);
//
//            assertAll(
//                    () -> assertThat(selectStatement.getSelectStatement()).isEqualTo("select count(*) from AnimalData"),
//                    () -> assertThat(count).isEqualTo(65)
//            );
//        }
//    }
//
//    @Test
//    void testMax() {
//        try (SqlSession sqlSession = sqlSessionFactory.openSession()) {
//            CommonSelectMapper mapper = sqlSession.getMapper(CommonSelectMapper.class);
//
//            SelectStatementProvider selectStatement = select(max(brainWeight).as("total"))
//                    .from(animalData, "a")
//                    .build()
//                    .render(RenderingStrategies.MYBATIS3);
//
//            Double max = mapper.selectOneDouble(selectStatement);
//
//            assertAll(
//                    () -> assertThat(selectStatement.getSelectStatement()).isEqualTo("select max(a.brain_weight) as total from AnimalData a"),
//                    () -> assertThat(max).isEqualTo(87000.0)
//            );
//        }
//    }
//
//    @Test
//    void testMaxNoAlias() {
//        try (SqlSession sqlSession = sqlSessionFactory.openSession()) {
//            CommonSelectMapper mapper = sqlSession.getMapper(CommonSelectMapper.class);
//
//            SelectStatementProvider selectStatement = select(max(brainWeight))
//                    .from(animalData)
//                    .build()
//                    .render(RenderingStrategies.MYBATIS3);
//
//            Double max = mapper.selectOneDouble(selectStatement);
//
//            assertAll(
//                    () -> assertThat(selectStatement.getSelectStatement()).isEqualTo("select max(brain_weight) from AnimalData"),
//                    () -> assertThat(max).isEqualTo(87000.0)
//            );
//        }
//    }
//
//    @Test
//    void testMaxSubselect() {
//        try (SqlSession sqlSession = sqlSessionFactory.openSession()) {
//            AnimalDataMapper mapper = sqlSession.getMapper(AnimalDataMapper.class);
//
//            SelectStatementProvider selectStatement = select(id, animalName, bodyWeight, brainWeight)
//                    .from(animalData, "a")
//                    .where(brainWeight, isEqualTo(select(max(brainWeight)).from(animalData, "b")))
//                    .build()
//                    .render(RenderingStrategies.MYBATIS3);
//
//            List<ReactiveAnimalData> records = mapper.selectMany(selectStatement);
//
//            assertAll(
//                    () -> assertThat(selectStatement.getSelectStatement()).isEqualTo("select a.id, a.animal_name, a.body_weight, a.brain_weight from AnimalData a where a.brain_weight = (select max(b.brain_weight) from AnimalData b)"),
//                    () -> assertThat(records).hasSize(1),
//                    () -> assertThat(records.get(0).getAnimalName()).isEqualTo("Brachiosaurus")
//            );
//        }
//    }
//
//    @Test
//    void testMin() {
//        try (SqlSession sqlSession = sqlSessionFactory.openSession()) {
//            CommonSelectMapper mapper = sqlSession.getMapper(CommonSelectMapper.class);
//
//            SelectStatementProvider selectStatement = select(min(brainWeight).as("total"))
//                    .from(animalData, "a")
//                    .build()
//                    .render(RenderingStrategies.MYBATIS3);
//
//            Double min = mapper.selectOneDouble(selectStatement);
//
//            assertAll(
//                    () -> assertThat(selectStatement.getSelectStatement()).isEqualTo("select min(a.brain_weight) as total from AnimalData a"),
//                    () -> assertThat(min).isEqualTo(0.005)
//            );
//        }
//    }
//
//    @Test
//    void testMinNoAlias() {
//        try (SqlSession sqlSession = sqlSessionFactory.openSession()) {
//            CommonSelectMapper mapper = sqlSession.getMapper(CommonSelectMapper.class);
//
//            SelectStatementProvider selectStatement = select(min(brainWeight))
//                    .from(animalData)
//                    .build()
//                    .render(RenderingStrategies.MYBATIS3);
//
//            Double min = mapper.selectOneDouble(selectStatement);
//
//            assertAll(
//                    () -> assertThat(selectStatement.getSelectStatement()).isEqualTo("select min(brain_weight) from AnimalData"),
//                    () -> assertThat(min).isEqualTo(0.005)
//            );
//        }
//    }
//
//    @Test
//    void testMinSubselect() {
//        try (SqlSession sqlSession = sqlSessionFactory.openSession()) {
//            AnimalDataMapper mapper = sqlSession.getMapper(AnimalDataMapper.class);
//
//            SelectStatementProvider selectStatement = select(id, animalName, bodyWeight, brainWeight)
//                    .from(animalData, "a")
//                    .where(brainWeight, isNotEqualTo(select(min(brainWeight)).from(animalData, "b")))
//                    .orderBy(animalName)
//                    .build()
//                    .render(RenderingStrategies.MYBATIS3);
//
//            List<ReactiveAnimalData> records = mapper.selectMany(selectStatement);
//
//            assertAll(
//                    () -> assertThat(selectStatement.getSelectStatement()).isEqualTo("select a.id, a.animal_name, a.body_weight, a.brain_weight from AnimalData a where a.brain_weight <> (select min(b.brain_weight) from AnimalData b) order by animal_name"),
//                    () -> assertThat(records).hasSize(64),
//                    () -> assertThat(records.get(0).getAnimalName()).isEqualTo("African elephant")
//            );
//        }
//    }
//
//    @Test
//    void testMinSubselectNoAlias() {
//        try (SqlSession sqlSession = sqlSessionFactory.openSession()) {
//            AnimalDataMapper mapper = sqlSession.getMapper(AnimalDataMapper.class);
//
//            SelectStatementProvider selectStatement = select(id, animalName, bodyWeight, brainWeight)
//                    .from(animalData)
//                    .where(brainWeight, isNotEqualTo(select(min(brainWeight)).from(animalData)))
//                    .orderBy(animalName)
//                    .build()
//                    .render(RenderingStrategies.MYBATIS3);
//
//            List<ReactiveAnimalData> records = mapper.selectMany(selectStatement);
//
//            assertAll(
//                    () -> assertThat(selectStatement.getSelectStatement()).isEqualTo("select id, animal_name, body_weight, brain_weight from AnimalData where brain_weight <> (select min(brain_weight) from AnimalData) order by animal_name"),
//                    () -> assertThat(records).hasSize(64),
//                    () -> assertThat(records.get(0).getAnimalName()).isEqualTo("African elephant")
//            );
//        }
//    }
//
//    @Test
//    void testAvg() {
//        try (SqlSession sqlSession = sqlSessionFactory.openSession()) {
//            CommonSelectMapper mapper = sqlSession.getMapper(CommonSelectMapper.class);
//
//            SelectStatementProvider selectStatement = select(avg(brainWeight).as("average"))
//                    .from(animalData, "a")
//                    .build()
//                    .render(RenderingStrategies.MYBATIS3);
//
//            Double average = mapper.selectOneDouble(selectStatement);
//
//            assertAll(
//                    () -> assertThat(selectStatement.getSelectStatement()).isEqualTo("select avg(a.brain_weight) as average from AnimalData a"),
//                    () -> assertThat(average).isEqualTo(1852.69, within(.01))
//            );
//        }
//    }
//
//    @Test
//    void testSum() {
//        try (SqlSession sqlSession = sqlSessionFactory.openSession()) {
//            CommonSelectMapper mapper = sqlSession.getMapper(CommonSelectMapper.class);
//
//            SelectStatementProvider selectStatement = select(sum(brainWeight).as("total"))
//                    .from(animalData)
//                    .build()
//                    .render(RenderingStrategies.MYBATIS3);
//
//            Double total = mapper.selectOneDouble(selectStatement);
//
//            assertAll(
//                    () -> assertThat(selectStatement.getSelectStatement()).isEqualTo("select sum(brain_weight) as total from AnimalData"),
//                    () -> assertThat(total).isEqualTo(120424.97, within(.01))
//            );
//        }
//    }
//
//    @Test
//    void testLessThanSubselect() {
//        try (SqlSession sqlSession = sqlSessionFactory.openSession()) {
//            AnimalDataMapper mapper = sqlSession.getMapper(AnimalDataMapper.class);
//
//            SelectStatementProvider selectStatement = select(id, animalName, bodyWeight, brainWeight)
//                    .from(animalData, "a")
//                    .where(brainWeight, isLessThan(select(max(brainWeight)).from(animalData, "b")))
//                    .build()
//                    .render(RenderingStrategies.MYBATIS3);
//
//            List<ReactiveAnimalData> records = mapper.selectMany(selectStatement);
//
//            assertAll(
//                    () -> assertThat(selectStatement.getSelectStatement()).isEqualTo("select a.id, a.animal_name, a.body_weight, a.brain_weight from AnimalData a where a.brain_weight < (select max(b.brain_weight) from AnimalData b)"),
//                    () -> assertThat(records).hasSize(64)
//            );
//        }
//    }
//
//    @Test
//    void testLessThanOrEqualToSubselect() {
//        try (SqlSession sqlSession = sqlSessionFactory.openSession()) {
//            AnimalDataMapper mapper = sqlSession.getMapper(AnimalDataMapper.class);
//
//            SelectStatementProvider selectStatement = select(id, animalName, bodyWeight, brainWeight)
//                    .from(animalData, "a")
//                    .where(brainWeight, isLessThanOrEqualTo(select(max(brainWeight)).from(animalData, "b")))
//                    .build()
//                    .render(RenderingStrategies.MYBATIS3);
//
//            List<ReactiveAnimalData> records = mapper.selectMany(selectStatement);
//
//            assertAll(
//                    () -> assertThat(selectStatement.getSelectStatement()).isEqualTo("select a.id, a.animal_name, a.body_weight, a.brain_weight from AnimalData a where a.brain_weight <= (select max(b.brain_weight) from AnimalData b)"),
//                    () -> assertThat(records).hasSize(65)
//            );
//        }
//    }
//
//    @Test
//    void testGreaterThanSubselect() {
//        try (SqlSession sqlSession = sqlSessionFactory.openSession()) {
//            AnimalDataMapper mapper = sqlSession.getMapper(AnimalDataMapper.class);
//
//            SelectStatementProvider selectStatement = select(id, animalName, bodyWeight, brainWeight)
//                    .from(animalData, "a")
//                    .where(brainWeight, isGreaterThan(select(min(brainWeight)).from(animalData, "b")))
//                    .build()
//                    .render(RenderingStrategies.MYBATIS3);
//
//            List<ReactiveAnimalData> records = mapper.selectMany(selectStatement);
//
//            assertAll(
//                    () -> assertThat(selectStatement.getSelectStatement()).isEqualTo("select a.id, a.animal_name, a.body_weight, a.brain_weight from AnimalData a where a.brain_weight > (select min(b.brain_weight) from AnimalData b)"),
//                    () -> assertThat(records).hasSize(64)
//            );
//        }
//    }
//
//    @Test
//    void testGreaterThanOrEqualToSubselect() {
//        try (SqlSession sqlSession = sqlSessionFactory.openSession()) {
//            AnimalDataMapper mapper = sqlSession.getMapper(AnimalDataMapper.class);
//
//            SelectStatementProvider selectStatement = select(id, animalName, bodyWeight, brainWeight)
//                    .from(animalData, "a")
//                    .where(brainWeight, isGreaterThanOrEqualTo(select(min(brainWeight)).from(animalData, "b")))
//                    .build()
//                    .render(RenderingStrategies.MYBATIS3);
//
//            List<ReactiveAnimalData> records = mapper.selectMany(selectStatement);
//
//            assertAll(
//                    () -> assertThat(selectStatement.getSelectStatement()).isEqualTo("select a.id, a.animal_name, a.body_weight, a.brain_weight from AnimalData a where a.brain_weight >= (select min(b.brain_weight) from AnimalData b)"),
//                    () -> assertThat(records).hasSize(65)
//            );
//        }
//    }
//
//    @Test
//    void testInsertSelectWithColumnList() {
//        try (SqlSession sqlSession = sqlSessionFactory.openSession()) {
//            SqlTable animalDataCopy = SqlTable.of("AnimalDataCopy");
//            AnimalDataMapper mapper = sqlSession.getMapper(AnimalDataMapper.class);
//
//            InsertSelectStatementProvider insertSelectStatement = insertInto(animalDataCopy)
//                    .withColumnList(id, animalName, bodyWeight, brainWeight)
//                    .withSelectStatement(select(id, animalName, bodyWeight, brainWeight).from(animalData).where(id, isLessThan(22)))
//                    .build()
//                    .render(RenderingStrategies.MYBATIS3);
//
//            String expected = "insert into AnimalDataCopy (id, animal_name, body_weight, brain_weight) "
//                    + "select id, animal_name, body_weight, brain_weight "
//                    + "from AnimalData "
//                    + "where id < #{parameters.p1,jdbcType=INTEGER}";
//
//            int rows = mapper.insertSelect(insertSelectStatement);
//
//            assertAll(
//                    () -> assertThat(insertSelectStatement.getInsertStatement()).isEqualTo(expected),
//                    () -> assertThat(insertSelectStatement.getParameters()).hasSize(1),
//                    () -> assertThat(insertSelectStatement.getParameters()).containsEntry("p1", 22),
//                    () -> assertThat(rows).isEqualTo(21)
//            );
//        }
//    }
//
//    @Test
//    void testInsertSelectWithoutColumnList() {
//        try (SqlSession sqlSession = sqlSessionFactory.openSession()) {
//            SqlTable animalDataCopy = SqlTable.of("AnimalDataCopy");
//            AnimalDataMapper mapper = sqlSession.getMapper(AnimalDataMapper.class);
//
//            InsertSelectStatementProvider insertSelectStatement = insertInto(animalDataCopy)
//                    .withSelectStatement(select(id, animalName, bodyWeight, brainWeight).from(animalData).where(id, isLessThan(33)))
//                    .build()
//                    .render(RenderingStrategies.MYBATIS3);
//
//            String expected = "insert into AnimalDataCopy "
//                    + "select id, animal_name, body_weight, brain_weight "
//                    + "from AnimalData "
//                    + "where id < #{parameters.p1,jdbcType=INTEGER}";
//            int rows = mapper.insertSelect(insertSelectStatement);
//
//            assertAll(
//                    () -> assertThat(insertSelectStatement.getInsertStatement()).isEqualTo(expected),
//                    () -> assertThat(insertSelectStatement.getParameters()).hasSize(1),
//                    () -> assertThat(insertSelectStatement.getParameters()).containsEntry("p1", 33),
//                    () -> assertThat(rows).isEqualTo(32)
//            );
//        }
//    }

    @Test
    @DirtiesContext
    void testGeneralInsert() {
        GeneralInsertStatementProvider insertStatement = insertInto(animalData)
                .set(id).toValue(101)
                .set(animalName).toStringConstant("Fred")
                .set(brainWeight).toConstant("2.2")
                .set(bodyWeight).toValue(4.5)
                .build()
                .render(R2DBC);

        String expected = "insert into AnimalData (id, animal_name, brain_weight, body_weight) "
                + "values ($1, 'Fred', 2.2, $2)";

        assertThat(insertStatement.getInsertStatement()).isEqualTo(expected);
        assertThat(insertStatement.getParameters()).hasSize(2);
        assertThat(insertStatement.getParameters()).containsEntry("$1", 101);
        assertThat(insertStatement.getParameters()).containsEntry("$2", 4.5);

        Long rows = connectionFactory.generalInsert(insertStatement).block();
        assertThat(rows).isEqualTo(1);
    }

//    @Test
//    void testGeneralInsertValueOrNullWithValue() {
//        try (SqlSession sqlSession = sqlSessionFactory.openSession()) {
//            AnimalDataMapper mapper = sqlSession.getMapper(AnimalDataMapper.class);
//
//            GeneralInsertStatementProvider insertStatement = insertInto(animalData)
//                    .set(id).toValue(101)
//                    .set(animalName).toValueOrNull("Fred")
//                    .set(brainWeight).toConstant("2.2")
//                    .set(bodyWeight).toValue(4.5)
//                    .build()
//                    .render(RenderingStrategies.MYBATIS3);
//
//            String expected = "insert into AnimalData (id, animal_name, brain_weight, body_weight) "
//                    + "values (#{parameters.p1,jdbcType=INTEGER}, #{parameters.p2,jdbcType=VARCHAR}, 2.2, "
//                    + "#{parameters.p3,jdbcType=DOUBLE})";
//
//            assertThat(insertStatement.getInsertStatement()).isEqualTo(expected);
//            assertThat(insertStatement.getParameters()).hasSize(3);
//            assertThat(insertStatement.getParameters()).containsEntry("p1", 101);
//            assertThat(insertStatement.getParameters()).containsEntry("p2", "Fred");
//            assertThat(insertStatement.getParameters()).containsEntry("p3", 4.5);
//
//            int rows = mapper.generalInsert(insertStatement);
//            assertThat(rows).isEqualTo(1);
//        }
//    }
//
//    @Test
//    void testGeneralInsertValueOrNullWithNull() {
//        try (SqlSession sqlSession = sqlSessionFactory.openSession()) {
//            AnimalDataMapper mapper = sqlSession.getMapper(AnimalDataMapper.class);
//
//            GeneralInsertStatementProvider insertStatement = insertInto(animalData)
//                    .set(id).toValue(101)
//                    .set(animalName).toValueOrNull((String) null)
//                    .set(brainWeight).toConstant("2.2")
//                    .set(bodyWeight).toValue(4.5)
//                    .build()
//                    .render(RenderingStrategies.MYBATIS3);
//
//            String expected = "insert into AnimalData (id, animal_name, brain_weight, body_weight) "
//                    + "values (#{parameters.p1,jdbcType=INTEGER}, null, 2.2, "
//                    + "#{parameters.p2,jdbcType=DOUBLE})";
//
//            assertThat(insertStatement.getInsertStatement()).isEqualTo(expected);
//            assertThat(insertStatement.getParameters()).hasSize(2);
//            assertThat(insertStatement.getParameters()).containsEntry("p1", 101);
//            assertThat(insertStatement.getParameters()).containsEntry("p2", 4.5);
//
//            int rows = mapper.generalInsert(insertStatement);
//            assertThat(rows).isEqualTo(1);
//        }
//    }
//
//    @Test
//    void testUpdateWithSelect() {
//        try (SqlSession sqlSession = sqlSessionFactory.openSession()) {
//            AnimalDataMapper mapper = sqlSession.getMapper(AnimalDataMapper.class);
//
//            UpdateStatementProvider updateStatement = update(animalData)
//                    .set(brainWeight).equalTo(select(avg(brainWeight)).from(animalData).where(brainWeight, isGreaterThan(22.0)))
//                    .where(brainWeight, isLessThan(1.0))
//                    .build()
//                    .render(RenderingStrategies.MYBATIS3);
//
//            String expected = "update AnimalData "
//                    + "set brain_weight = (select avg(brain_weight) from AnimalData where brain_weight > #{parameters.p1,jdbcType=DOUBLE}) "
//                    + "where brain_weight < #{parameters.p2,jdbcType=DOUBLE}";
//            int rows = mapper.update(updateStatement);
//
//            assertAll(
//                    () -> assertThat(updateStatement.getUpdateStatement()).isEqualTo(expected),
//                    () -> assertThat(updateStatement.getParameters()).hasSize(2),
//                    () -> assertThat(updateStatement.getParameters()).containsEntry("p1", 22.0),
//                    () -> assertThat(updateStatement.getParameters()).containsEntry("p2", 1.0),
//                    () -> assertThat(rows).isEqualTo(20)
//            );
//        }
//    }
//
//    @Test
//    void testUpdateWithAddAndSubtract() {
//        try (SqlSession sqlSession = sqlSessionFactory.openSession()) {
//            AnimalDataMapper mapper = sqlSession.getMapper(AnimalDataMapper.class);
//
//            UpdateStatementProvider updateStatement = update(animalData)
//                    .set(brainWeight).equalTo(add(brainWeight, constant("2")))
//                    .set(bodyWeight).equalTo(subtract(bodyWeight, constant("3")))
//                    .set(animalName).equalToWhenPresent((String) null)
//                    .where(id, isEqualTo(1))
//                    .build()
//                    .render(RenderingStrategies.MYBATIS3);
//
//            String expected = "update AnimalData "
//                    + "set brain_weight = (brain_weight + 2), body_weight = (body_weight - 3) "
//                    + "where id = #{parameters.p1,jdbcType=INTEGER}";
//
//            assertThat(updateStatement.getUpdateStatement()).isEqualTo(expected);
//            assertThat(updateStatement.getParameters()).hasSize(1);
//            assertThat(updateStatement.getParameters()).containsEntry("p1", 1);
//
//            int rows = mapper.update(updateStatement);
//            assertThat(rows).isEqualTo(1);
//
//            ReactiveAnimalData record = MyBatis3Utils.selectOne(mapper::selectOne,
//                    BasicColumn.columnList(id, bodyWeight, brainWeight),
//                    animalData,
//                    c -> c.where(id, isEqualTo(1))
//            );
//
//            assertThat(record.getBodyWeight()).isEqualTo(-2.86);
//            assertThat(record.getBrainWeight()).isEqualTo(2.005);
//        }
//    }
//
//    @Test
//    void testUpdateWithMultiplyAndDivide() {
//        try (SqlSession sqlSession = sqlSessionFactory.openSession()) {
//            AnimalDataMapper mapper = sqlSession.getMapper(AnimalDataMapper.class);
//
//            UpdateStatementProvider updateStatement = update(animalData)
//                    .set(brainWeight).equalTo(divide(brainWeight, constant("2")))
//                    .set(bodyWeight).equalTo(multiply(bodyWeight, constant("3")))
//                    .where(id, isEqualTo(1))
//                    .build()
//                    .render(RenderingStrategies.MYBATIS3);
//
//            String expected = "update AnimalData "
//                    + "set brain_weight = (brain_weight / 2), body_weight = (body_weight * 3) "
//                    + "where id = #{parameters.p1,jdbcType=INTEGER}";
//            assertThat(updateStatement.getUpdateStatement()).isEqualTo(expected);
//            assertThat(updateStatement.getParameters()).hasSize(1);
//            assertThat(updateStatement.getParameters()).containsEntry("p1", 1);
//
//            int rows = mapper.update(updateStatement);
//            assertThat(rows).isEqualTo(1);
//
//            ReactiveAnimalData record = MyBatis3Utils.selectOne(mapper::selectOne,
//                    BasicColumn.columnList(id, bodyWeight, brainWeight),
//                    animalData,
//                    c -> c.where(id, isEqualTo(1))
//            );
//
//            assertThat(record.getBodyWeight()).isEqualTo(0.42, within(.001));
//            assertThat(record.getBrainWeight()).isEqualTo(.0025);
//        }
//    }
}
