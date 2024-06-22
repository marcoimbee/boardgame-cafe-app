package it.unipi.dii.lsmsdb.boardgamecafe.mvc.model.neo4j;

import org.springframework.data.neo4j.core.schema.Id;
import org.springframework.data.neo4j.core.schema.Node;

@Node("Boardgame")
public class BoardgameModelNeo4j {

    @Id
    public String id;
    public String name;
    public String image;
    public int yearpublished;

    public BoardgameModelNeo4j(){};

    public BoardgameModelNeo4j(String id, String name,
                               String image, int yearpublished) {

        this.id = id;
        this.name = name;
        this.image = image;
        this.yearpublished = yearpublished;
    }

    // Metodi setter/getter
    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
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

    public int getYearPublished() {
        return yearpublished;
    }

    public void setYearPublished(int yearpublished) {
        this.yearpublished = yearpublished;
    }

    @Override
    public String toString() {
        return "BoardgameNeo4j{" +
                "id='" + id + '\'' +
                ", name='" + name + '\'' +
                ", image='" + image + '\'' +
                ", yearpublished='" + yearpublished + '\'' +
                '}';
    }
}

