package it.unipi.dii.lsmsdb.boardgamecafe.mvc.model.mongo;

import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;
import java.util.Date;

@Document(collection = "reviews")
public class ReviewModelMongo {

    @Id
    private String id;
    private String boardgameName;
    private String username;
    private int rating;
    private String body;
    private Date dateOfReview;

    public ReviewModelMongo() {}

    public ReviewModelMongo(String id, int rating) {
        this.id = id;
        this.rating = rating;
    }

    public ReviewModelMongo(String id, String boardgameName,
                            String username, int rating,
                            String body, Date dateOfReview)
    {
        this.id = id;
        this.boardgameName = boardgameName;
        this.username = username;
        this.rating = rating;
        this.body = body;
        this.dateOfReview = dateOfReview;
    }

    public ReviewModelMongo(String boardgameName,
                            String username, int rating,
                            String body, Date dateOfReview)
    {
        this.boardgameName = boardgameName;
        this.username = username;
        this.rating = rating;
        this.body = body;
        this.dateOfReview = dateOfReview;
    }

    public String getBoardgameName() {
        return boardgameName;
    }

    public void setBoardgameName(String boardgameName) {
        this.boardgameName = boardgameName;
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public int getRating() {
        return rating;
    }

    public void setRating(int rating) {
        this.rating = rating;
    }

    public String getBody() {
        return body;
    }

    public void setBody(String body) {
        this.body = body;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public Date getDateOfReview() {
        return dateOfReview;
    }

    public void setDateOfReview(Date dateOfReview) {
        this.dateOfReview = dateOfReview;
    }

    @Override
    public String toString() {
        return "Review{" +
                "id='" + id + '\'' +
                ", boardgameName='" + boardgameName + '\'' +
                ", username='" + username + '\'' +
                ", rating=" + rating +
                ", body='" + body + '\'' +
                ", dateOfReview=" + dateOfReview +
                '}';
    }
}
