package it.unipi.dii.lsmsdb.boardgamecafe.services.neo4jdbms;

import org.neo4j.driver.Record;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.List;

import static org.neo4j.driver.Values.parameters;

public class BoardgameNeo4jDB{

    private final static Logger logger = LoggerFactory.getLogger(BoardgameNeo4jDB.class);

    private final GraphNeo4jDB graphNeo4j;

    public GraphNeo4jDB getGraphNeo4j() {
        return graphNeo4j;
    }

    public BoardgameNeo4jDB(GraphNeo4jDB graphNeo4j) {
        this.graphNeo4j = graphNeo4j;
    }

    public boolean addPhone(String id, String brand, String name, String picture, int releaseYear) {
        boolean result = true;
        try {
            graphNeo4j.write("MERGE (p:Phone {id: $id, brand: $brand, name: $name, " +
                            "picture: $picture, releaseYear: $releaseYear})",
                    parameters("id", id, "brand", brand, "name", name,
                            "picture", picture, "releaseYear", releaseYear));
        } catch (Exception e) {
            e.printStackTrace();
            result = false;
        }
        return result;
    }

    public List<Record> findPhoneById(String id) {
        try {
            return graphNeo4j.read("MATCH (p:Phone {id: $id})" +
                            "RETURN p.id AS id, p.brand AS brand, p.name AS name, p.picture " +
                            "AS picture, p.releaseYear AS releaseYear",
                    parameters("id", id));
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }

    public boolean updatePhone(String id, String brand, String picture, int releaseYear) {
        boolean result = true;
        try {
            graphNeo4j.write("MATCH (p:Phone {id: $id}) " +
                            "SET p.brand = $brand, p.picture = $picture, p.releaseYear = $releaseYear " +
                            "RETURN p.id AS id, p.brand AS brand, p.name AS name, p.picture AS picture, " +
                            "p.releaseYear AS releaseYear",
                    parameters("id", id, "brand", brand, "picture", picture,
                            "releaseYear", releaseYear));
        } catch (Exception e) {
            e.printStackTrace();
            result = false;
        }
        return result;
    }

    public boolean deletePhoneById(String id) {
        boolean result = true;
        try {
            graphNeo4j.write("MATCH (p:Phone {id: $id}) DETACH DELETE p",
                    parameters("id", id));
        } catch (Exception e) {
            e.printStackTrace();
            result = false;
        }
        return result;
    }

    public boolean deletePhoneOnly(String id) {
        boolean result = true;
        try {
            graphNeo4j.write("MATCH (p:Phone {id: $id}) DELETE p",
                    parameters("id", id));
        } catch (Exception e) {
            e.printStackTrace();
            result = false;
        }
        return result;
    }

    public boolean deletePhoneRelationships(String id) {
        boolean result = true;
        try {
            graphNeo4j.write("MATCH (p:Phone {id: $id})<-[r:ADDS]-() delete r",
                    parameters("id", id));
        } catch (Exception e) {
            e.printStackTrace();
            result = false;
        }
        return result;
    }

    public List<Record> findBestBrands(int results) {
        List<Record> result = new ArrayList<>();
        try {
            return graphNeo4j.read("MATCH (:User)-[:ADDS]->(p:Phone) " +
                    "RETURN p.brand AS brand, COUNT(p.brand) AS numPhones " +
                    "ORDER BY numPhones DESC " +
                    "LIMIT $results", parameters("results", results));
        } catch (Exception e) {
            e.printStackTrace();
        }
        return result;
    }

    public List<Record> findSuggestedPhonesByFriends(String id) {
        List<Record> result = new ArrayList<>();
        try {
            return graphNeo4j.read("MATCH (u1:User{id:$id})-[:FOLLOWS]->(u2:User)-[:ADDS]->(newPhone:Phone) " +
                    "WHERE u1.id <> u2.id AND NOT EXISTS ((u1)-[:ADDS]->(newPhone)) " +
                    "WITH newPhone " +
                    "ORDER BY newPhone.releaseYear DESC " +
                    "RETURN DISTINCT newPhone " +
                    "LIMIT 9", parameters("id",id));
        } catch (Exception e) {
            e.printStackTrace();
        }
        return result;
    }

    public List<Record> findSuggestedPhonesByBrand(String id) {
        List<Record> result = new ArrayList<>();
        try {
            return graphNeo4j.read("MATCH (u1:User{id:$id})-[:ADDS]->(p:Phone) " +
                    "WITH COUNT (p.brand) AS numPhones, p.brand AS favBrand, u1 " +
                    "ORDER BY numPhones DESC " +
                    "LIMIT 1 " +
                    "MATCH (newPhone:Phone{brand:favBrand}) " +
                    "WHERE NOT EXISTS ((u1)-[:ADDS]->(newPhone)) " +
                    "WITH newPhone " +
                    "ORDER BY newPhone.releaseYear DESC " +
                    "RETURN DISTINCT newPhone " +
                    "LIMIT 9", parameters("id",id));
        } catch (Exception e) {
            e.printStackTrace();
        }
        return result;
    }

}