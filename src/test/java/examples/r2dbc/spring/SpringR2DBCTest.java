package examples.r2dbc.spring;

import static examples.animal.data.AnimalDataDynamicSqlSupport.animalData;
import static examples.animal.data.AnimalDataDynamicSqlSupport.animalName;
import static examples.animal.data.AnimalDataDynamicSqlSupport.bodyWeight;
import static examples.animal.data.AnimalDataDynamicSqlSupport.brainWeight;
import static examples.animal.data.AnimalDataDynamicSqlSupport.id;
import static examples.generated.always.spring.GeneratedAlwaysDynamicSqlSupport.generatedAlways;
import static org.assertj.core.api.Assertions.assertThat;
import static org.mybatis.dynamic.sql.SqlBuilder.deleteFrom;
import static org.mybatis.dynamic.sql.SqlBuilder.insertInto;
import static org.mybatis.dynamic.sql.SqlBuilder.isEqualTo;
import static org.mybatis.dynamic.sql.SqlBuilder.select;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import examples.animal.data.AnimalData;
import io.r2dbc.spi.ColumnMetadata;
import io.r2dbc.spi.ConnectionFactory;
import io.r2dbc.spi.Row;
import io.r2dbc.spi.RowMetadata;
import org.junit.jupiter.api.Test;
import org.mybatis.dynamic.sql.delete.DeleteModel;
import org.mybatis.dynamic.sql.insert.GeneralInsertModel;
import org.mybatis.dynamic.sql.select.SelectModel;
import org.mybatis.dynamic.sql.util.Buildable;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.r2dbc.core.DatabaseClient;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.context.junit.jupiter.SpringJUnitConfig;

@SpringJUnitConfig(classes = SpringConfiguration.class)
class SpringR2DBCTest {
    private DatabaseClient db;

    @Autowired
    private void setConnectionFactory(ConnectionFactory connectionFactory) {
        db = DatabaseClient.builder().connectionFactory(connectionFactory).build();
    }

    @Test
    void testSelect() {
        Buildable<SelectModel> selectStatement = select(id, animalName, bodyWeight, brainWeight)
                .from(animalData)
                .orderBy(id);

        List<AnimalData> animals  = db.sql(OperationFactory.fromSelectModel(selectStatement))
                .map(this::rowMapper)
                .all()
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

        AnimalData animal = db.sql(OperationFactory.fromSelectModel(selectStatement))
                .map(this::rowMapper)
                .one()
                .block();

        assertThat(animal).isNotNull();
    }

    @Test
    @DirtiesContext
    void testDelete() {
        Buildable<DeleteModel> deleteStatement = deleteFrom(animalData)
                .where(id, isEqualTo(3));

        Long rows = db.sql(OperationFactory.fromDeleteModel(deleteStatement))
                .fetch()
                .rowsUpdated()
                .block();

        assertThat(rows).isEqualTo(1);
    }

    @Test
    @DirtiesContext
    void testGeneralInsert() {
        Buildable<GeneralInsertModel> insertStatement = insertInto(generatedAlways)
                .set(generatedAlways.id).toValue(100)
                .set(generatedAlways.firstName).toValue("Tom")
                .set(generatedAlways.lastName).toValue("Jones");

        Long rows = db.sql(OperationFactory.fromGeneralInsertModel(insertStatement))
                .fetch()
                .rowsUpdated()
                .block();

        assertThat(rows).isEqualTo(1);
    }

    @Test
    @DirtiesContext
    void testGeneralInsertWithReturnedValue() {
        Buildable<GeneralInsertModel> insertStatement = insertInto(generatedAlways)
                .set(generatedAlways.id).toValue(100)
                .set(generatedAlways.firstName).toValue("Tom")
                .set(generatedAlways.lastName).toValue("Jones");

        Map<String, Object> generatedValues = db.sql(OperationFactory.fromGeneralInsertModel(insertStatement))
                .filter(s -> s.returnGeneratedValues(generatedAlways.fullName.name()))
                .map(this::rawMapper)
                .first()
                .block();

        assertThat(generatedValues).containsEntry("FULL_NAME", "Tom Jones");
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

    private Map<String, Object> rawMapper(Row row, RowMetadata rowMetadata) {
        List<? extends ColumnMetadata> columnMetadataList = rowMetadata.getColumnMetadatas();
        Map<String, Object> answer = new HashMap<>(columnMetadataList.size());

        for (ColumnMetadata columnMetadata : columnMetadataList) {
            String name = columnMetadata.getName();
            answer.put(name, row.get(name));
        }

        return answer;
    }
}
