package it.unipi.dii.lsmsdb.boardgamecafe.repository.mongodbms;

import org.bson.Document;
import org.jetbrains.annotations.NotNull;
import org.springframework.data.mongodb.core.aggregation.AggregationOperation;
import org.springframework.data.mongodb.core.aggregation.AggregationOperationContext;

public class CustomAggregationOperation implements AggregationOperation {
    private final Document operation;

    public CustomAggregationOperation(Document operation) {
        this.operation = operation;
    }

    @Override
    public @NotNull Document toDocument(AggregationOperationContext context) {
        return context.getMappedObject(operation);
    }
}
