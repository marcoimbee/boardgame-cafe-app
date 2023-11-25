package it.unipi.dii.lsmsdb.boardgamecafe.mvc.model.neo4j;

import org.springframework.data.neo4j.core.schema.Id;
import org.springframework.data.neo4j.core.schema.Node;
import org.springframework.data.neo4j.core.schema.Relationship;

import java.util.List;


@Node
public class UserNeo4j {

    @Id
    public String id;
    public String username;

    @Relationship(type = "ADDS", direction = Relationship.Direction.OUTGOING)
    public List<BoardgameNeo4j> boardgamesNeo4j;

    public UserNeo4j() {}

    public UserNeo4j(String id, String username) {
        this.id = id;
        this.username = username;
    }
    public UserNeo4j(List<BoardgameNeo4j> boardgamesNeo4j) {
        this.boardgamesNeo4j = boardgamesNeo4j;
    }

    // Metodi setter/getter
    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public List<BoardgameNeo4j> getBoardgamesNeo4j() {
        return boardgamesNeo4j;
    }

    public void setBoardgamesNeo4j(List<BoardgameNeo4j> boardgamesNeo4j) {
        this.boardgamesNeo4j = boardgamesNeo4j;
    }
}

