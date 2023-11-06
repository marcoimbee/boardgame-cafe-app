/*
package it.unipi.dii.lsmsdb.boardgamecafe.repository.neo4jdbms;

import org.neo4j.driver.*;

import java.util.ArrayList;
import java.util.List;

import static org.neo4j.driver.Values.parameters;

public class GraphNeo4jDB implements AutoCloseable {

    private final Driver driver;

    public GraphNeo4jDB(String uri, String user, String password) {
        driver = GraphDatabase.driver(uri, AuthTokens.basic(user, password));
    }

    @Override
    public void close() {
        driver.close();
    }

    public void write(final String query, final Value parameters) {
        try (Session session = driver.session()) {
            session.writeTransaction(tx ->
            {
                tx.run(query, parameters).consume();
                return null;
            });
        }
    }

    public void write(String query) {
        write(query, parameters());
    }

    public List<Record> read(final String query, final Value parameters) {
        List<Record> recordsList;
        try (Session session = driver.session()) {
            recordsList = session.readTransaction(tx -> {
                Result result = tx.run( query, parameters );
                List<Record> records = new ArrayList<>();
                while (result.hasNext()) {
                    Record r = result.next();
                    records.add(r);
                }
                return records;
            });
        }
        return recordsList;
    }

    public List<Record> read(String query) {
        return read(query, parameters());
    }

}
*/