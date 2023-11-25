package it.unipi.dii.lsmsdb.boardgamecafe.mvc.model.mongo;

import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

import java.util.Calendar;
import java.util.Date;
import java.util.GregorianCalendar;

@Document(collection = "reviews")
public class Review {

    @Id
    private String id;
    private String boardgameName;
    private String username;
    private Double rating;
    private String comment;
    private Date dateOfReview;

    // - Costruttore vuoto necessario per la corretta deserializzazione JSON in Spring -
    public Review() {}

    // - Costruttore con parametri -
    public Review(String id, String boardgameName, String username, Double rating, String comment, Date dateOfReview) {
        this.id = id;
        this.boardgameName = boardgameName;
        this.username = username;
        this.rating = rating;
        this.comment = comment;
        this.dateOfReview = dateOfReview;
    }

    // - Constructor Builder-Pattern-Based -
    public Review(ReviewBuilder builder) {
        this.id = builder.id;
        this.boardgameName = builder.boardgameName;
        this.rating = builder.rating;
        this.username = builder.username;
        this.comment = builder.comment;
        this.dateOfReview = builder.dateOfReview;
    }

    // - Metodi di accesso -
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

    public double getRating() {
        return rating;
    }

    public void setRating(double rating) {
        this.rating = rating;
    }

    public String getComment() {
        return comment;
    }

    public void setComment(String comment) {
        this.comment = comment;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    private String setFields(StringBuilder sb)
    {
        Calendar calendar = new GregorianCalendar();
        calendar.setTime(this.dateOfReview);
        sb.append("Date: ").append(calendar.get(Calendar.YEAR)).append("-").
                            append((calendar.get(Calendar.MONTH))+1).append("-").
                            append(calendar.get(Calendar.DAY_OF_MONTH)).append("\n");
        sb.append("Rating: ").append(this.rating).append("\n");
        return sb.toString();
    }

    // - toString() Methods Customized -
    public String toStringTable(boolean isBoardgame) {

        StringBuilder sb = new StringBuilder();
        if (isBoardgame) {
            sb.append("Username: ").append(this.username).append("\n");
        } else {
            sb.append("Boardgame: ").append(this.boardgameName).append("\n");
        }

        //************** Other fields **************
        this.setFields(sb);
        //******************************************

        StringBuilder sBody = new StringBuilder(this.comment);
        int i = 0;
        while ((i = sBody.indexOf(" ", i + 125)) != -1) {
            sBody.replace(i, i+1, "\n");
        }
        sb.append("Body: ").append(sBody);
        return sb.toString();
    }

    public String toStringFind() {

        StringBuilder sb = new StringBuilder();
        sb.append("Username: ").append(this.username).append("\n");
        sb.append("Boardgame: ").append(this.boardgameName).append("\n");

        //************** Other fields **************
        this.setFields(sb);
        //******************************************

        StringBuilder sBody = new StringBuilder(this.comment);
        int i = 0;
        while ((i = sBody.indexOf(" ", i + 80)) != -1) {
            sBody.replace(i, i+1, "\n");
        }
        sb.append("Body: ").append(sBody);
        return sb.toString();
    }


    // - Builder-Pattern Constructor Inner Class -
    public static class ReviewBuilder {
        private String id;
        private String boardgameName;
        private String username;
        private Double rating;
        private String comment;
        private Date dateOfReview;

        public ReviewBuilder(Double rating, String username, String comment, Date dateOfReview)
        {
            this.rating = rating;
            this.username = username;
            this.comment = comment;
            this.dateOfReview = dateOfReview;
        }
        public ReviewBuilder(Review review) {
            this.rating = review.rating;
            this.username = review.username;
            this.comment = review.comment;
            this.dateOfReview = review.dateOfReview;
        }
        public ReviewBuilder id (String id) {
            this.id = id;
            return this;
        }
        public ReviewBuilder username (String username) {
            this.username = username;
            return this;
        }
        public ReviewBuilder boardgameName (String boardgameName) {
            this.boardgameName = boardgameName;
            return this;
        }
        public Review build() {
            return new Review(this);
        }
    }

    @Override
    public String toString() {
        return "Review{" +
                "id='" + id + '\'' +
                ", boardgameName='" + boardgameName + '\'' +
                ", username='" + username + '\'' +
                ", rating=" + rating +
                ", comment='" + comment + '\'' +
                ", dateOfReview=" + dateOfReview +
                '}';
    }
}
