package it.unipi.dii.lsmsdb.boardgamecafe.mvc.model.neo4j;

import org.springframework.data.neo4j.core.schema.Id;
import org.springframework.data.neo4j.core.schema.Node;
import org.springframework.data.neo4j.core.schema.Relationship;

import java.util.List;

@Node("Post")
public class PostModelNeo4j {
    @Id
    private String id;

    @Relationship(type = "REFERS_TO", direction = Relationship.Direction.OUTGOING)
    private BoardgameModelNeo4j taggedGame;
    @Relationship(type = "WRITES_POST", direction = Relationship.Direction.INCOMING)
    private UserModelNeo4j author;
    @Relationship(type = "LIKES", direction = Relationship.Direction.INCOMING)
    private UserModelNeo4j likes;

    public PostModelNeo4j(){}
    public PostModelNeo4j(String id) {
        this.id = id;
    }

    public UserModelNeo4j getAuthor() {
        return author;
    }

    public void setAuthor(UserModelNeo4j author) {
        this.author = author;
    }

    public UserModelNeo4j getLikes() {
        return likes;
    }

    public void setLikes(UserModelNeo4j likes) {
        this.likes = likes;
    }

    public String getId() {
        return id;
    }
    public void setId(String id) {
        this.id = id;
    }
    public BoardgameModelNeo4j getTaggedGame() {
        return taggedGame;
    }
    public void setTaggedGame(BoardgameModelNeo4j taggedGame) {
        this.taggedGame = taggedGame;
    }

    @Override
    public String toString() {
        return "id: '" + id + "', " +
                "tagged game: " + taggedGame;
    }
}
