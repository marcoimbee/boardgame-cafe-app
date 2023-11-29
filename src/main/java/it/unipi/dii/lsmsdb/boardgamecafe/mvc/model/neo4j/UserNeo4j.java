package it.unipi.dii.lsmsdb.boardgamecafe.mvc.model.neo4j;

import org.springframework.data.neo4j.core.schema.Id;
import org.springframework.data.neo4j.core.schema.Node;
import org.springframework.data.neo4j.core.schema.Relationship;

import java.util.List;


@Node("User")
public class UserNeo4j {

    @Id
    public String id;
    public String username;

    @Relationship(type = "ADDS", direction = Relationship.Direction.OUTGOING)
    public List<BoardgameNeo4j> boardgames;
    @Relationship(type = "FOLLOWS", direction = Relationship.Direction.OUTGOING)
    public List<UserNeo4j> followedUsers;
    @Relationship(type = "FOLLOWS", direction = Relationship.Direction.INCOMING)
    public List<UserNeo4j> followers;

    public UserNeo4j() {}

    public UserNeo4j(String id, String username) {
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

    public List<BoardgameNeo4j> getBoardgames() {
        return this.boardgames;
    }
    public List<UserNeo4j> getFollowedUsers() {
        return followedUsers;
    }
    public List<UserNeo4j> getFollowers() {
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

