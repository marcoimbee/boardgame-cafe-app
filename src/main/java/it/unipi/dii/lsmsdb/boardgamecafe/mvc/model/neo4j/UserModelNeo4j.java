package it.unipi.dii.lsmsdb.boardgamecafe.mvc.model.neo4j;

import org.springframework.data.neo4j.core.schema.Id;
import org.springframework.data.neo4j.core.schema.Node;
import org.springframework.data.neo4j.core.schema.Relationship;

import java.util.List;


@Node("Users")
public class UserModelNeo4j {

    @Id
    public String id;
    public String username;

    @Relationship(type = "WRITES", direction = Relationship.Direction.OUTGOING)
    public List<PostModelNeo4j> posts;

    @Relationship(type = "WRITES", direction = Relationship.Direction.OUTGOING)
    public List<CommentModelNeo4j> comments;
    @Relationship(type = "FOLLOWS", direction = Relationship.Direction.OUTGOING)
    public List<UserModelNeo4j> followedUsers;
    @Relationship(type = "FOLLOWS", direction = Relationship.Direction.INCOMING)
    public List<UserModelNeo4j> followers;

    public UserModelNeo4j() {}

    public UserModelNeo4j(String id, String username) {
        this.id = id;
        this.username = username;
    }

    // Metodi setter/getter
    public String getId() {
        return id;
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public void setId(String id) {
        this.id = id;
    }

    public List<UserModelNeo4j> getFollowedUsers() {
        return followedUsers;
    }
    public List<UserModelNeo4j> getFollowers() {
        return followers;
    }

    @Override
    public String toString() {
        return "UserNeo4j{" +
                "id='" + id + '\'' +
                ", username='" + username + '\'' +
                '}';
    }
}

