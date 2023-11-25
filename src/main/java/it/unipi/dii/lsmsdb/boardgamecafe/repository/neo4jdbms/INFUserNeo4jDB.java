package it.unipi.dii.lsmsdb.boardgamecafe.repository.neo4jdbms;

import it.unipi.dii.lsmsdb.boardgamecafe.mvc.model.mongo.UserTest;
import it.unipi.dii.lsmsdb.boardgamecafe.mvc.model.neo4j.UserNeo4j;
import org.springframework.data.neo4j.repository.Neo4jRepository;
import org.springframework.data.neo4j.repository.query.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface INFUserNeo4jDB extends Neo4jRepository<UserNeo4j, String> {

    @Query("Match (n:User) where n.username = $userName RETURN n")
    List<UserNeo4j> findUserByUsername(@Param("userName") String personName);
    List<UserNeo4j> findByUsername(String personName);

    @Query("MATCH (u1:User {id: $userId})<-[:FOLLOWS]-(u2:User) RETURN DISTINCT u2")
    List<UserNeo4j> findFollowers(@Param("userId") String userId);
    @Query("MATCH (u1:User {id: $userId})-[:FOLLOWS]->(u2:User) RETURN DISTINCT u2")
    List<UserNeo4j> findFollowed(@Param("userId") String userId);

    @Query("Match (n:User)-[r:ADDS]->(b:Boardgame) where n.username = $userName RETURN n, collect(r), collect(b)")
    UserNeo4j findUserAndBoardgamesAdded(@Param("userName") String personName);

}
