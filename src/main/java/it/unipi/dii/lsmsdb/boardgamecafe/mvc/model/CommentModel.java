package it.unipi.dii.lsmsdb.boardgamecafe.mvc.model;

import org.bson.types.ObjectId;
import java.util.Date;

public class CommentModel {
//    @Id
    private String id;
    private String username;
    private String text;
    private Date timestamp;

    public CommentModel() {}

    public CommentModel(String id,
                        String username,
                        String text,
                        Date timestamp) {
        this.id = id;
        this.username = username;
        this.text = text;
        this.timestamp = timestamp;
    }

    public CommentModel(String username,
                        String text,
                        Date timestamp)
    {
        this.id = new ObjectId().toString();
        this.username = username;
        this.text = text;
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

    @Override
    public String toString() {
        return "Comment{" +
                "id='" + id + '\'' +
                ", username='" + username + '\'' +
                ", text='" + text + '\'' +
                ", timestamp=" + timestamp +
                '}';
    }
}