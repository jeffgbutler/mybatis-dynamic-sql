package examples.r2dbc.spring;

import static examples.animal.data.AnimalDataDynamicSqlSupport.animalData;
import static examples.animal.data.AnimalDataDynamicSqlSupport.animalName;
import static examples.animal.data.AnimalDataDynamicSqlSupport.bodyWeight;
import static examples.animal.data.AnimalDataDynamicSqlSupport.brainWeight;
import static examples.animal.data.AnimalDataDynamicSqlSupport.id;
import static examples.generated.always.spring.GeneratedAlwaysDynamicSqlSupport.generatedAlways;
import static org.assertj.core.api.Assertions.assertThat;
import static org.mybatis.dynamic.sql.SqlBuilder.concat;
import static org.mybatis.dynamic.sql.SqlBuilder.deleteFrom;
import static org.mybatis.dynamic.sql.SqlBuilder.insertInto;
import static org.mybatis.dynamic.sql.SqlBuilder.isEqualTo;
import static org.mybatis.dynamic.sql.SqlBuilder.isLessThan;
import static org.mybatis.dynamic.sql.SqlBuilder.select;
import static org.mybatis.dynamic.sql.SqlBuilder.stringConstant;
import static org.mybatis.dynamic.sql.SqlBuilder.update;

import java.util.List;
import java.util.Map;

import examples.animal.data.AnimalData;
import io.r2dbc.spi.ConnectionFactory;
import io.r2dbc.spi.Row;
import io.r2dbc.spi.RowMetadata;
import org.junit.jupiter.api.Test;
import org.mybatis.dynamic.sql.delete.DeleteModel;
import org.mybatis.dynamic.sql.insert.GeneralInsertModel;
import org.mybatis.dynamic.sql.select.SelectModel;
import org.mybatis.dynamic.sql.update.UpdateModel;
import org.mybatis.dynamic.sql.util.Buildable;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.context.junit.jupiter.SpringJUnitConfig;

@SpringJUnitConfig(classes = SpringConfiguration.class)
class SpringR2DBCTest {
    private R2DBCMyBatisDSQLTemplate template;

    @Autowired
    private void setConnectionFactory(ConnectionFactory connectionFactory) {
        template = new R2DBCMyBatisDSQLTemplate(connectionFactory);
    }

    @Test
    void testSelect() {
        Buildable<SelectModel> selectStatement = select(id, animalName, bodyWeight, brainWeight)
                .from(animalData)
                .orderBy(id);

        List<AnimalData> animals  = template.selectMany(selectStatement, this::rowMapper)
                .collectList()
                .block();

        assertThat(animals).hasSize(65);
        assertThat(animals.get(0).getId()).isEqualTo(1);
    }

    @Test
    void testSelectOne() {
        Buildable<SelectModel> selectStatement = select(id, animalName, bodyWeight, brainWeight)
                .from(animalData)
                .where(id, isEqualTo(3))
                .and(animalName, isEqualTo("Big brown bat"));

        AnimalData animal = template.selectOne(selectStatement, this::rowMapper).block();

        assertThat(animal).isNotNull();
    }

    @Test
    @DirtiesContext
    void testDelete() {
        Buildable<DeleteModel> deleteStatement = deleteFrom(animalData)
                .where(id, isEqualTo(3));

        Long rows = template.delete(deleteStatement).block();

        assertThat(rows).isEqualTo(1);
    }

    @Test
    @DirtiesContext
    void testGeneralInsert() {
        Buildable<GeneralInsertModel> insertStatement = insertInto(generatedAlways)
                .set(generatedAlways.id).toValue(100)
                .set(generatedAlways.firstName).toValue("Tom")
                .set(generatedAlways.lastName).toValue("Jones");

        Long rows = template.generalInsert(insertStatement).block();

        assertThat(rows).isEqualTo(1);
    }

    @Test
    @DirtiesContext
    void testGeneralInsertWithReturnedValue() {
        Buildable<GeneralInsertModel> insertStatement = insertInto(generatedAlways)
                .set(generatedAlways.id).toValue(100)
                .set(generatedAlways.firstName).toValue("Tom")
                .set(generatedAlways.lastName).toValue("Jones");

        Map<String, Object> generatedValues = template.prepareGeneralInsert(insertStatement)
                .filter(s -> s.returnGeneratedValues(generatedAlways.fullName.name()))
                .map(R2DBCUtils::rawMapper)
                .first()
                .block();

        assertThat(generatedValues).containsEntry("FULL_NAME", "Tom Jones");
    }

    @Test
    @DirtiesContext
    void testUpdate() {
        Buildable<UpdateModel> updateStatement = update(generatedAlways)
                .set(generatedAlways.lastName).equalTo(concat(generatedAlways.lastName, stringConstant("-DDD")))
                .where(generatedAlways.id, isLessThan(4));

        Long rows = template.update(updateStatement).block();

        assertThat(rows).isEqualTo(3);
    }

    @Test
    @DirtiesContext
    void testUpdateWithReturnedValues() {
        Buildable<UpdateModel> updateStatement = update(generatedAlways)
                .set(generatedAlways.lastName).equalTo(concat(generatedAlways.lastName, stringConstant("-DDD")))
                .where(generatedAlways.id, isLessThan(4));

        List<Map<String, Object>> generatedValues = template.prepareUpdate(updateStatement)
                .filter(s -> s.returnGeneratedValues(generatedAlways.id.name(), generatedAlways.fullName.name()))
                .map(R2DBCUtils::rawMapper)
                .all()
                .collectList()
                .block();

        assertThat(generatedValues).hasSize(3);
        assertThat(generatedValues.get(0)).containsEntry("ID", 1);
        assertThat(generatedValues.get(0)).containsEntry("FULL_NAME", "Fred Flintstone-DDD");
    }

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
}
