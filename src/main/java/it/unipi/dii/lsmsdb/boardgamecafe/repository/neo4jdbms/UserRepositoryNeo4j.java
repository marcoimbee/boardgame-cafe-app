package it.unipi.dii.lsmsdb.boardgamecafe.repository.neo4jdbms;

import it.unipi.dii.lsmsdb.boardgamecafe.mvc.model.neo4j.UserNeo4j;
import org.springframework.data.neo4j.repository.Neo4jRepository;
import org.springframework.data.neo4j.repository.query.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface UserRepositoryNeo4j extends Neo4jRepository<UserNeo4j, String> {

    Optional<UserNeo4j> findByUsername(String username);

    @Query("MATCH (u:User {username: $username}) DETACH DELETE u")
    void deleteUserDetachByUsername(@Param("username") String username);

    @Query("MATCH (u1:User {username: $userName})<-[:FOLLOWS]-(u2:User) RETURN DISTINCT u2")
    List<UserNeo4j> findFollowersByUsername(@Param("userName") String username);

    @Query("MATCH (u1:User {username: $userName})-[:FOLLOWS]->(u2:User) RETURN DISTINCT u2")
    List<UserNeo4j> findFollowingByUsername(@Param("userName") String username);

    @Query("MATCH (u1:User)-[a:ADDS]->(b:Boardgame {name: $boardgameName}) RETURN DISTINCT u1")
    List<UserNeo4j> findUsersByBoardgameName(@Param("boardgameName") String boardgamename);



}
