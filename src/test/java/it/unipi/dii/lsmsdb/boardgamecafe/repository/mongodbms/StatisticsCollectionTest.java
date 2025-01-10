package it.unipi.dii.lsmsdb.boardgamecafe.repository.mongodbms;

import org.junit.jupiter.api.MethodOrderer;
import org.junit.jupiter.api.Order;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestMethodOrder;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.data.convert.ReadingConverter;
import org.springframework.data.convert.WritingConverter;
import org.springframework.data.mongodb.core.MongoOperations;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.aggregation.*;
import org.springframework.data.mongodb.core.query.Criteria;
import org.bson.Document;

import java.util.ArrayList;
import java.util.List;

@SpringBootTest
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
@WritingConverter
public class StatisticsCollectionTest {

    @Autowired
    private ReviewDBMongo reviewDBMongo;
    @Autowired
    private MongoOperations mongoOperations;
    @Autowired
    private MongoTemplate mongoTemplate;

    @Test
    @Order(10)
    public void GIVEN_CollectionAndAggregationQuery_WHEN_Executed_THEN_ExplainResults() {
        String collection = "reviews";  // Nome della collection

        // Definisci una query di aggregazione di esempio
        Aggregation aggregation = Aggregation.newAggregation(
                Aggregation.match(Criteria.where("boardgameName").is("The Princes of Florence")),
                Aggregation.group("boardgameName").avg("rating").as("averageRating")
        );

        // Crea il comando per l'explain
        Document explainAggDocument = new Document();
        explainAggDocument.put("aggregate", collection);
        explainAggDocument.put("pipeline", aggregation.getPipeline());  // Passiamo la pipeline corretta
        explainAggDocument.put("explain", true);
        explainAggDocument.put("executionStats", true);

        // Esegui il comando explain per ottenere le statistiche di esecuzione
        Document command = new Document("explain", explainAggDocument);
        Document explainResult = mongoTemplate.getDb().runCommand(command);

        System.out.println("Stats doc -> " + explainResult.toJson());
    }
}
