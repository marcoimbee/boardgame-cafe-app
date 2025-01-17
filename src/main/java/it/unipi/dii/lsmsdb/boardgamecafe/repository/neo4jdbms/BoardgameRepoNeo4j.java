package it.unipi.dii.lsmsdb.boardgamecafe.repository.neo4jdbms;

import it.unipi.dii.lsmsdb.boardgamecafe.mvc.model.neo4j.BoardgameModelNeo4j;
import org.springframework.data.neo4j.repository.Neo4jRepository;
import org.springframework.data.neo4j.repository.query.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;

@Repository
public interface BoardgameRepoNeo4j extends Neo4jRepository<BoardgameModelNeo4j, String>{

    //@Query("MATCH (b:Boardgames {boardgameName: $bordgameName}) DETACH DELETE b, (b)-[r]-()")
    //void deleteAndDetachBoardgameByName(@Param("bordgameName") String boardgameName);

    @Query("MATCH (b:Boardgame {boardgameName: $boardgameName}) DETACH DELETE b")
    void deleteAndDetachBoardgameByBoardgameName(@Param("boardgameName") String boardgameName);

    @Query("MATCH (b:Boardgame {boardgameName: $boardgameName}) RETURN b")
    Optional<BoardgameModelNeo4j> findByBoardgameName(@Param("boardgameName") String boardgameName);

    @Query("MATCH (b:Boardgame {id: $boardgameId}) RETURN b")
    Optional<BoardgameModelNeo4j> findById(@Param("boardgameId") String boardgameId);

    @Query("MATCH (you:User {username: $username})-[:FOLLOWS]->(otherUser:User)-[:WRITES_POST]->(post:Post)\n" +
            "-[:REFERS_TO]->(game:Boardgame) \n" +
            "RETURN DISTINCT game \n" +
            "SKIP $skipCounter \n"+
            "LIMIT $limit")
    List<BoardgameModelNeo4j> getBoardgamesWithPostsByFollowedUsers
            (@Param("username") String username, @Param("limit") int limit, @Param("skipCounter")int skipCounter);

    @Query("MATCH (b:Boardgame) RETURN b ORDER BY b.yearPublished DESC, b.id ASC SKIP $skip LIMIT $limit")
    Optional<List<BoardgameModelNeo4j>> findRecentBoardgames(@Param("skip") int skip, @Param("limit") int limit);
}
