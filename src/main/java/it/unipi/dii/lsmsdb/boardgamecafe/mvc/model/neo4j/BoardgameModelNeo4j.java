package it.unipi.dii.lsmsdb.boardgamecafe.mvc.model.neo4j;

import org.springframework.data.neo4j.core.schema.Id;
import org.springframework.data.neo4j.core.schema.Node;

@Node("Boardgame")
public class BoardgameModelNeo4j {

    @Id
    public String id;
    public String boardgameName;
    public String thumbnail;
    public int yearPublished;

    public BoardgameModelNeo4j(){};

    public BoardgameModelNeo4j(String id, String boardgameName,
                               String thumbnail, int yearPublished) {

        this.id = id;
        this.boardgameName = boardgameName;
        this.thumbnail = thumbnail;
        this.yearPublished = yearPublished;
    }

    // Metodi setter/getter
    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getBoardgameName() {
        return boardgameName;
    }

    public void setBoardgameName(String boardgameName) {
        this.boardgameName = boardgameName;
    }

    public String getThumbnail() {
        return thumbnail;
    }

    public void setThumbnail(String thumbnail) {
        this.thumbnail = thumbnail;
    }

    public int getYearPublished() {
        return yearPublished;
    }

    public void setYearPublished(int yearPublished) {
        this.yearPublished = yearPublished;
    }

    @Override
    public String toString() {
        return "BoardgameNeo4j{" +
                "id='" + id + '\'' +
                ", name='" + boardgameName + '\'' +
                ", image='" + thumbnail + '\'' +
                ", yearpublished='" + yearPublished + '\'' +
                '}';
    }
}

