package it.unipi.dii.lsmsdb.boardgamecafe.repository.neo4jdbms;

import it.unipi.dii.lsmsdb.boardgamecafe.mvc.model.neo4j.BoardgameNeo4j;
import org.springframework.data.neo4j.repository.Neo4jRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface BoardgameRepositoryNeo4j extends Neo4jRepository<BoardgameNeo4j, String>{

    //List<BoardgameNeo4j> findAllByBoardgameNameCustom( String gameName);
}
