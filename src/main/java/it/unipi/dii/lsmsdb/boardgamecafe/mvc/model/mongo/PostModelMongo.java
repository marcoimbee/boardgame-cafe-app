package it.unipi.dii.lsmsdb.boardgamecafe.mvc.model.mongo;

import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

import java.util.ArrayList;
import java.util.List;
import java.util.Date;

@Document(collection = "posts")
public class PostModelMongo {
    @Id
    private String id;
    private String username;
    private String title;
    private String text;
    private String tag;
    private Date timestamp;
    private List<CommentModelMongo> comments = new ArrayList<>();

    public PostModelMongo(String id, String username,
                          String title, String text,
                          String tag, Date timestamp) {
        this.id = id;
        this.username = username;
        this.title = title;
        this.text = text;
        this.tag = tag;
        this.timestamp = timestamp;
    }

    public String getId(){
        return id;
    }

    public void setId(String id){
        this.id = id;
    }

    public String getUsername(){
        return username;
    }
    public void setUsername(String username){
        this.username = username;
    }

    public String getTitle(){
        return title;
    }
    public void setTitle(String title){
        this.title = title;
    }

    public String getText(){
        return text;
    }
    public void setText(String text){
        this.text = text;
    }

    public String getTag(){
        return tag;
    }
    public void setTag(String tag){
        this.tag = tag;
    }

    public Date getTimestamp(){
        return timestamp;
    }
    public void setTimestamp(Date timestamp) {
        this.timestamp = timestamp;
    }

    // Comments Management

    public List<CommentModelMongo> getComments(){
        return comments;
    }

    public CommentModelMongo getCommentInPost(String id) {
        if (!this.comments.isEmpty()) {
            for (CommentModelMongo comment : comments) {
                if (comment.getId().equals(id)) {
                    return comment;
                }
            }
        }
        return null;
    }

    public void setComments(List<CommentModelMongo> comments) {
        this.comments = comments;
    }

    public void addComment(CommentModelMongo comment) {
        this.comments.add(0, comment);
    }

    public boolean deleteCommentInPost(String id) {
        CommentModelMongo comment = this.getCommentInPost(id);
        if (comment != null) {
            comments.remove(comment);
            return true;
        }
        return false;
    }


    // Other
    @Override
    public String toString() {
        return "Post{" +
                "id='" + id + '\'' +
                ", username='" + username + '\'' +
                ", title='" + title + '\'' +
                ", tag='" + tag + '\'' +
                //", text='" + text + '\'' +
                ", timestamp=" + timestamp +
                //", comments=" + comments +
                '}';
    }
}