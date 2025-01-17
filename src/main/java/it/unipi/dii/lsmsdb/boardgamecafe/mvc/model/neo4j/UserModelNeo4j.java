package it.unipi.dii.lsmsdb.boardgamecafe.mvc.model.neo4j;

import org.springframework.data.neo4j.core.schema.Id;
import org.springframework.data.neo4j.core.schema.Node;
import org.springframework.data.neo4j.core.schema.Relationship;

import java.util.ArrayList;
import java.util.List;


@Node("User")
public class UserModelNeo4j {
    @Id
    private String id;
    private String username;

    @Relationship(type = "FOLLOWS", direction = Relationship.Direction.OUTGOING)
    private List<UserModelNeo4j> followedUsers;
    @Relationship(type = "FOLLOWS", direction = Relationship.Direction.INCOMING)
    private List<UserModelNeo4j> followerUsers;
    @Relationship(type = "WRITES_POST", direction = Relationship.Direction.OUTGOING)
    private List<PostModelNeo4j> writtenPosts;
    @Relationship(type = "LIKES", direction = Relationship.Direction.OUTGOING)
    private List<PostModelNeo4j> likedPosts;

    public UserModelNeo4j() {}

    public UserModelNeo4j(String id, String username) {
        this.id = id;
        this.username = username;
    }

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

    public void setFollowedUsers(List<UserModelNeo4j> followedUsers) {
        this.followedUsers = followedUsers;
    }

    public UserModelNeo4j getFollowedUser(String id) {
        if (!this.followedUsers.isEmpty()) {
            for (UserModelNeo4j user : followedUsers) {
                if (user.getId().equals(id)) {
                    return user;
                }
            }
        }
        return null;
    }

    public void addFollowedUser(UserModelNeo4j user) {
        this.followedUsers.add(0, user);
    }

    public boolean deleteFollowedUser(String id) {
        UserModelNeo4j user = this.getFollowedUser(id);
        if (user != null) {
            followedUsers.remove(user);
            return true;
        }
        return false;
    }

    public List<UserModelNeo4j> getFollowerUsers() {
        return followerUsers;
    }

    public void setFollowerUsers(List<UserModelNeo4j> followerUsers) {
        this.followerUsers = followerUsers;
    }

    public List<PostModelNeo4j> getWrittenPosts() {
        return this.writtenPosts;
    }

    public List<PostModelNeo4j> newGetWrittenPosts(String username) {
        List<PostModelNeo4j> posts = new ArrayList<>();
        if (!this.writtenPosts.isEmpty()) {
            for (PostModelNeo4j post : this.writtenPosts) {
                if (post.getAuthor().equals(username)) {
                    posts.add(post);
                }
            }
        } else { return null;}
        return posts;
    }

    public void setWrittenPosts(List<PostModelNeo4j> writtenPosts) {
        this.writtenPosts = writtenPosts;
    }

    public PostModelNeo4j getPostWrittenByUser(String id) {
        if (!this.writtenPosts.isEmpty()) {
            for (PostModelNeo4j post : writtenPosts) {
                if (post.getId().equals(id)) {
                    return post;
                }
            }
        }
        return null;
    }

    public void addWrittenPost(PostModelNeo4j post) {
        this.writtenPosts.add(post);

    }

    public List<PostModelNeo4j> getLikedPosts() {
        return likedPosts;
    }

    public void setLikedPosts(List<PostModelNeo4j> likedPosts) {
        this.likedPosts = likedPosts;
    }

    public PostModelNeo4j getPostLikedByUser(String id) {
        if (!this.likedPosts.isEmpty()) {
            for (PostModelNeo4j post : likedPosts) {
                if (post.getId().equals(id)) {
                    return post;
                }
            }
        }
        return null;
    }

    public void addLikedPost(PostModelNeo4j post) {
        this.likedPosts.add(0, post);
    }

    @Override
    public String toString() {
        return "UserNeo4j{" +
                "id='" + id + '\'' +
                ", username='" + username + '\'' +
                '}';
    }
}

