/*
package it.unipi.dii.lsmsdb.boardgamecafe.repository.neo4jdbms;

import org.neo4j.driver.Record;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.List;

import static org.neo4j.driver.Values.parameters;

public class UserNeo4jDB {

    private final static Logger logger = LoggerFactory.getLogger(UserNeo4jDB.class);
    private final GraphNeo4jDB graphNeo4j;

    public GraphNeo4jDB getGraphNeo4j() {
        return graphNeo4j;
    }

    public UserNeo4jDB(GraphNeo4jDB graphNeo4j) {
        this.graphNeo4j = graphNeo4j;
    }

    public boolean addUser(String id, String username) {
        boolean result = true;
        try {
            graphNeo4j.write("MERGE (u:User {id: $id, username: $username})",
                    parameters( "id", id, "username", username));
        } catch (Exception e) {
            e.printStackTrace();
            result = false;
        }
        return result;
    }

    public List<Record> findUserById(String id) {
        try {
            return graphNeo4j.read("MATCH (u:User {id: $id}) " +
                            "RETURN u.id AS id, u.username AS username",
                    parameters("id", id));
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }

    public boolean updateUser(String id, String username) {
        boolean result = true;
        try {
            graphNeo4j.write("MATCH (u:User {id: $id}) SET u.username = $username " +
                                   "RETURN u.id AS id, u.username AS username",
                    parameters("id", id, "username", username));
        } catch (Exception e) {
            e.printStackTrace();
            result = false;
        }
        return result;
    }

    public boolean deleteUserById(String id) {
        boolean result = true;
        try {
            graphNeo4j.write(("MATCH (u:User {id: $id}) DETACH DELETE u"),
                    parameters( "id", id));
        } catch (Exception e) {
            e.printStackTrace();
            result = false;
        }
        return result;
    }

    public boolean deleteUserOnly(String id) {
        boolean result = true;
        try {
            graphNeo4j.write(("MATCH (u:User {id: $id}) DELETE u"),
                    parameters( "id", id));
        } catch (Exception e) {
            e.printStackTrace();
            result = false;
        }
        return result;
    }

    public boolean followRelationship(String id1, String id2) {
        boolean result = true;
        try {
            graphNeo4j.write("MATCH (u1:User {id: $id1})" +
                            "MATCH (u2:User {id: $id2})" +
                            "CREATE (u1)-[:FOLLOWS]->(u2)",
                    parameters("id1", id1, "id2", id2));
        } catch (Exception e) {
            e.printStackTrace();
            result = false;
        }
        return result;
    }

    public boolean unfollowRelationship(String id1, String id2) {
        boolean result = true;
        try {
            graphNeo4j.write("MATCH (u1:User {id: $id1})-[r:FOLLOWS]->(u2:User {id: $id2}) DELETE r",
                    parameters("id1", id1, "id2", id2));
        } catch (Exception e) {
            e.printStackTrace();
            result = false;
        }
        return result;
    }

    public boolean addRelationship(String userId, String phoneId) {
        boolean result = true;
        try {
            graphNeo4j.write("MATCH (u:User {id: $userId})" +
                            "MATCH (p:Phone {id: $phoneId})" +
                            "CREATE (u)-[:ADDS]->(p)",
                    parameters("userId", userId, "phoneId", phoneId));
        } catch (Exception e) {
            e.printStackTrace();
            result = false;
        }
        return result;
    }

    public boolean removeRelationship(String userId, String phoneId) {
        boolean result = true;
        try {
            graphNeo4j.write("MATCH (u:User {id: $userId})-[r:ADDS]->(p:Phone {id: $phoneId}) DELETE r",
                    parameters("userId", userId, "phoneId", phoneId));
        } catch (Exception e) {
            e.printStackTrace();
            result = false;
        }
        return result;
    }

    public List<Record> getAddRelationship(String userId, String phoneId) {
        List<Record> result = new ArrayList<>();
        try {
            return graphNeo4j.read("MATCH (u1:User {id: $userId})-[r:ADDS]->(p:Phone {id: $phoneId})" +
                            "RETURN r",
                    parameters("userId", userId, "phoneId", phoneId));
        } catch (Exception e) {
            e.printStackTrace();
        }
        return result;
    }

    public List<Record> getFollowRelationship(String userId1, String userId2) {
        List<Record> result = new ArrayList<>();
        try {
            return graphNeo4j.read("MATCH (u1:User {id: $userId1})-[r:FOLLOWS]->(u2:User {id: $userId2})" +
                            "RETURN r",
                    parameters("userId1", userId1, "userId2", userId2));
        } catch (Exception e) {
            e.printStackTrace();
        }
        return result;
    }

    public List<Record> getFollowed(String userId) {
        List<Record> result = new ArrayList<>();
        try {
            return graphNeo4j.read("MATCH (u1:User {id: $userId})-[:FOLLOWS]->(u2:User) " +
                            "RETURN DISTINCT u2",
                    parameters("userId", userId));
        } catch (Exception e) {
            e.printStackTrace();
        }
        return result;
    }

    public boolean deleteUserAddsRelationships(String id) {
        boolean result = true;
        try {
            graphNeo4j.write("MATCH (u:User {id: $id})-[r:ADDS]->() " +
                                   "DELETE r",
                    parameters("id", id));
        } catch (Exception e) {
            e.printStackTrace();
            result = false;
        }
        return result;
    }

    public boolean deleteUserFollowsRelationships(String id) {
        boolean result = true;
        try {
            graphNeo4j.write("MATCH (u:User {id: $id})-[r:FOLLOWS]->() " +
                                   "DELETE r",
                    parameters("id", id));
            graphNeo4j.write("MATCH (u:User {id: $id})<-[r:FOLLOWS]-() " +
                                    "DELETE r",
                    parameters("id", id));
        } catch (Exception e) {
            e.printStackTrace();
            result = false;
        }
        return result;
    }

    public List<Record> getWatchlist(String id) {
        List<Record> result = new ArrayList<>();
        try {
            return graphNeo4j.read("MATCH (u1:User{id:$id})-[:ADDS]->(p:Phone)" +
                    "RETURN DISTINCT p " +
                    "LIMIT 10", parameters("id", id));
        } catch (Exception e) {
            e.printStackTrace();
        }
        return result;
    }

    public List<Record> findMostFollowedUsers(int results) {
        List<Record> result = new ArrayList<>();
        try {
            return graphNeo4j.read("MATCH (u1:User)<-[r:FOLLOWS]-(u2:User) " +
                    "RETURN u1.username AS username, u1.id AS id, COUNT(r) AS followers " +
                    "ORDER BY followers DESC " +
                    "LIMIT $results", parameters("results", results));
        } catch (Exception e) {
            e.printStackTrace();
        }
        return result;
    }

    public List<Record> findSuggestedUsersByFriends(String id) {
        List<Record> result = new ArrayList<>();
        try {
            return graphNeo4j.read
                    ("MATCH (u1:User{id: $id})-[:FOLLOWS]->(u2:User)-[:FOLLOWS]->(u3:User) " +
                            "WHERE NOT EXISTS ((u1)-[:FOLLOWS]->(u3)) AND u1.id <> u3.id " +
                            "WITH u3, rand() AS number " +
                            "ORDER BY number " +
                            "RETURN DISTINCT u3.id AS id, u3.username AS username " +
                            "LIMIT 9", parameters("id",id));
        } catch (Exception e) {
            e.printStackTrace();
        }
        return result;
    }

    public List<Record> findSuggestedUsersByBrand(String id) {
        List<Record> result = new ArrayList<>();
        try {
            return graphNeo4j.read("MATCH (u1:User{id:$id})-[:ADDS]->(p:Phone) " +
                    "WITH COUNT (p.brand) AS numPhones, p.brand AS favBrand, u1 " +
                    "ORDER BY numPhones DESC " +
                    "LIMIT 1 " +
                    "MATCH (u2:User)-[:ADDS]->(p:Phone{brand:favBrand}) " +
                    "WHERE u2.id <> u1.id AND NOT EXISTS ((u1)-[:FOLLOWS]->(u2)) " +
                    "RETURN COUNT (p.brand) AS phones, p.brand AS favouriteBrand, u2.id AS id, u2.username AS username " +
                    "ORDER BY phones DESC " +
                    "LIMIT 9", parameters("id",id));
        } catch (Exception e) {
            e.printStackTrace();
        }
        return result;
    }

}
*/