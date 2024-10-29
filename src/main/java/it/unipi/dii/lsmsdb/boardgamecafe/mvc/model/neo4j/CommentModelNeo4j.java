package it.unipi.dii.lsmsdb.boardgamecafe.mvc.model.neo4j;

import org.springframework.data.neo4j.core.schema.Id;
import org.springframework.data.neo4j.core.schema.Node;


@Node("Comment")
public class CommentModelNeo4j {

    @Id
    private String id;

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

    @Override
    public String toString() {
        return "CommentNeo4j{" +
                "id='" + id +
                "'}";
    }
}
