package it.unipi.dii.lsmsdb.boardgamecafe.mvc.model.neo4j;

import org.springframework.data.neo4j.core.schema.Id;
import org.springframework.data.neo4j.core.schema.Node;

@Node
public class BoardgameNeo4j {

    @Id
    public String boardgameId;
    public String name;
    public String image;
    public String yearPublished;

    public BoardgameNeo4j(String boardgameId, String name, String image) {
        this.boardgameId = boardgameId;
        this.name = name;
        this.image = image;
    }

    public BoardgameNeo4j(String yearPublished) {
        this.yearPublished = yearPublished;
    }

    public String getBoardgameId() {
        return boardgameId;
    }

    public void setBoardgameId(String boardgameId) {
        this.boardgameId = boardgameId;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getImage() {
        return image;
    }

    public void setImage(String image) {
        this.image = image;
    }

    public String getYearPublished() {
        return yearPublished;
    }

    public void setYearPublished(String yearPublished) {
        this.yearPublished = yearPublished;
    }
}

