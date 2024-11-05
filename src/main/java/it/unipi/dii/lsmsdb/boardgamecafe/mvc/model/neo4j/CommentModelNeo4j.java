package it.unipi.dii.lsmsdb.boardgamecafe.mvc.model.neo4j;

import org.springframework.data.neo4j.core.schema.Id;
import org.springframework.data.neo4j.core.schema.Node;
import org.springframework.data.neo4j.core.schema.Relationship;


@Node("Comment")
public class CommentModelNeo4j {

    @Id
    private String id;

    @Relationship(type = "WRITES", direction = Relationship.Direction.INCOMING)
    private UserModelNeo4j author;
    @Relationship(type = "REPLY", direction = Relationship.Direction.OUTGOING)
    private PostModelNeo4j postCommented;

    public CommentModelNeo4j() {}

    public CommentModelNeo4j(String id) {
        this.id = id;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public UserModelNeo4j getAuthor() {
        return author;
    }

    public void setAuthor(UserModelNeo4j author) {
        this.author = author;
    }

    public PostModelNeo4j getPostCommented() {
        return postCommented;
    }
    public void setPostCommented(PostModelNeo4j postCommented) {
        this.postCommented = postCommented;
    }


    @Override
    public String toString() {
        return "CommentNeo4j{" +
                "id='" + id +
                "'}";
    }
}
