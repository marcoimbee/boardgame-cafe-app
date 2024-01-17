package it.unipi.dii.lsmsdb.boardgamecafe.repository.neo4jdbms;

import it.unipi.dii.lsmsdb.boardgamecafe.mvc.model.neo4j.BoardgameModelNeo4j;
import org.springframework.data.neo4j.repository.Neo4jRepository;
import org.springframework.data.neo4j.repository.query.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface BoardgameRepoNeo4j extends Neo4jRepository<BoardgameModelNeo4j, String>{


    @Query("MATCH (u1:User {username: $userName})-[a:ADDS]->(b:Boardgame) RETURN DISTINCT b")
    List<BoardgameModelNeo4j> findBoardgamesByUsername(@Param("userName") String username);

    //Below is retrieved relationship as well (for graph, not useful - toCheck)
    @Query("Match (u:User)-[r:ADDS]->(b:Boardgame) where u.username = $userName RETURN u, collect(r), collect(b)")
    List<BoardgameModelNeo4j> findBoardgamesAndUserByUsername(@Param("userName") String personName);
}
