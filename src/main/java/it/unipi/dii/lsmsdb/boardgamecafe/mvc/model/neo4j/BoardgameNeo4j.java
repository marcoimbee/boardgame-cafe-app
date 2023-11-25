package it.unipi.dii.lsmsdb.boardgamecafe.mvc.model.neo4j;

import org.springframework.data.neo4j.core.schema.Id;
import org.springframework.data.neo4j.core.schema.Node;

@Node
public class BoardgameNeo4j {

    @Id
    private String boardgameId;
    private String name;
    private String image;
    private String yearPublished;

    public BoardgameNeo4j(){};

    public BoardgameNeo4j(String boardgameId, String name, String image, String yearPublished) {
        this.boardgameId = boardgameId;
        this.name = name;
        this.image = image;
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

