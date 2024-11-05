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
    private List<UserModelNeo4j> followedUsers = new ArrayList<>();

    @Relationship(type = "WRITES_POST", direction = Relationship.Direction.OUTGOING)
    public List<PostModelNeo4j> writtenPosts = new ArrayList<>();

    @Relationship(type = "LIKES", direction = Relationship.Direction.OUTGOING)
    private List<PostModelNeo4j> likedPosts = new ArrayList<>();

    @Relationship(type = "WRITES", direction = Relationship.Direction.OUTGOING)
    private List<CommentModelNeo4j> writtenComments = new ArrayList<>();

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

    public List<PostModelNeo4j> getWrittenPosts() {
        return writtenPosts;
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

    public boolean deleteWrittenPost(String id) {
        PostModelNeo4j post = this.getPostWrittenByUser(id);
        if (post != null) {
            writtenComments.remove(post);
            return true;
        }
        return false;
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

    public boolean deleteLikedPost(String id) {
        PostModelNeo4j post = this.getPostLikedByUser(id);
        if (post != null) {
            writtenComments.remove(post);
            return true;
        }
        return false;
    }

    public List<CommentModelNeo4j> getWrittenComments() {
        return writtenComments;
    }

    public void setWrittenComments(List<CommentModelNeo4j> comments) {
        this.writtenComments = comments;
    }

    public CommentModelNeo4j getCommentWrittenByUser(String id) {
        if (!this.writtenComments.isEmpty()) {
            for (CommentModelNeo4j comment : writtenComments) {
                if (comment.getId().equals(id)) {
                    return comment;
                }
            }
        }
        return null;
    }

    public void addWrittenComment(CommentModelNeo4j comment) {
        this.writtenComments.add(0, comment);
    }

    public boolean deleteWrittenComment(String id) {
        CommentModelNeo4j comment = this.getCommentWrittenByUser(id);
        if (comment != null) {
            writtenComments.remove(comment);
            return true;
        }
        return false;
    }

    @Override
    public String toString() {
        return "id: '" + id + "', " +
                "username: '" + username;
    }
}
