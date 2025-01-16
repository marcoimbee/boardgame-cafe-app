package it.unipi.dii.lsmsdb.boardgamecafe.mvc.model.neo4j;

import org.springframework.data.neo4j.core.schema.Id;
import org.springframework.data.neo4j.core.schema.Node;
import org.springframework.data.neo4j.core.schema.Relationship;

import java.util.List;

@Node("Boardgame")
public class BoardgameModelNeo4j {

    @Id
    public String id;
    public String boardgameName;
    public String image;
    public String description;
    public int yearPublished;

    // Relazione in entrata dai nodi Post
    @Relationship(type = "REFERS_TO", direction = Relationship.Direction.INCOMING)
    private List<PostModelNeo4j> posts;

    public BoardgameModelNeo4j() {}

    public BoardgameModelNeo4j(String id, String boardgameName, String image, String description, int yearPublished) {

        this.id = id;
        this.boardgameName = boardgameName;
        this.image = image;
        this.description = description;
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

    public String getImage() {
        return image;
    }

    public void setImage(String image) {
        this.image = image;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public int getYearPublished() {
        return yearPublished;
    }

    public void setYearPublished(int yearPublished) {
        this.yearPublished = yearPublished;
    }


    public List<PostModelNeo4j> getPosts() { return posts; }
    public void setPosts(List<PostModelNeo4j> posts) { this.posts = posts; }

    @Override
    public String toString() {
        return "BoardgameNeo4j{" +
                "id='" + id + '\'' +
                ", name='" + boardgameName + '\'' +
                ", posts=" + posts +
                '}';
    }
}

