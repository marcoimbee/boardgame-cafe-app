package it.unipi.dii.lsmsdb.boardgamecafe.mvc.model.mongo;

import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;
import com.fasterxml.jackson.annotation.JsonIgnore;

import java.util.Date;


@Document("comments")
public class CommentModelMongo {
    @Id
    private String id;
    private String post;
    private String username;
    private String text;
    private Date timestamp;

    @JsonIgnore
    private String _class;          // Makes the model compatible with documents which present the '_class' attributes

    public CommentModelMongo() {}

    public CommentModelMongo(String id,
                             String post,
                             String username,
                             String text,
                             Date timestamp) {
        this.id = id;
        this.post = post;
        this.username = username;
        this.text = text;
        this.timestamp = timestamp;
    }

    public CommentModelMongo(String post,
                             String username,
                             String text,
                             Date timestamp) {
        this.post = post;
        this.username = username;
        this.text = text;
        this.timestamp = timestamp;
    }

    // GETTERS AND SETTERS
    public String getId(){
        return id;
    }
    public void setId(String id){
        this.id = id;
    }

    public String getPost(){
        return post;
    }
    public void setPost(String post){
        this.post = post;
    }

    public String getUsername(){
        return username;
    }
    public void setUsername(String username){
        this.username = username;
    }

    public String getText(){
        return text;
    }
    public void setText(String text){
        this.text = text;
    }

    public Date getTimestamp(){
        return timestamp;
    }

    public void setTimestamp(Date timestamp){
        this.timestamp = timestamp;
    }

    // Other
    @Override
    public String toString() {
        return "Comment{" +
                "id='" + id + '\'' +
                ", post='" + post + '\'' +
                ", username='" + username + '\'' +
                ", text='" + text + '\'' +
                ", timestamp=" + timestamp +
                '}';
    }
}