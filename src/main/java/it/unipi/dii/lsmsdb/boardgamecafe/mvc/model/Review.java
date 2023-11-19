package it.unipi.dii.lsmsdb.boardgamecafe.mvc.model;

import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

import java.util.Calendar;
import java.util.Date;
import java.util.GregorianCalendar;

@Document(collection = "reviews")
public class Review {
    @Id
    private int boardgameId;
    private String username;
    private Double rating;
    private String comment;

    // Costruttore vuoto necessario per la corretta deserializzazione JSON
    public Review() {}

    // Costruttore con parametri
    public Review(int boardgameId, String username, Double rating, String comment) {
        this.boardgameId = boardgameId;
        this.username = username;
        this.rating = rating;
        this.comment = comment;
    }

    // --- Costruttore Buldier pattern-based ---
    public static class ReviewBuilder {
        private int boardgameId;
        private String username;
        private Double rating;
        private String comment;

        public ReviewBuilder(int boardgameId, Double rating, String username, String comment) {
            this.boardgameId = boardgameId;
            this.rating = rating;
            this.username = username;
            this.comment = comment;
        }

        public ReviewBuilder(Review review) {
            this.rating = review.rating;
            this.comment = review.comment;
        }

        public Review.ReviewBuilder boardgameId (int id) {
            this.boardgameId = id;
            return this;
        }

        public Review.ReviewBuilder username (String username) {
            this.username = username;
            return this;
        }

        public Review build() {
            return new Review(this);
        }
    }

    public Review(ReviewBuilder builder) {
        this.boardgameId = builder.boardgameId;
        this.rating = builder.rating;
        this.username = builder.username;
        this.comment = builder.comment;
    }


    // Metodi di accesso

    public int getBoardgameId() {
        return boardgameId;
    }

    public void setBoardgameId(int boardgameId) {
        this.boardgameId = boardgameId;
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

    // --- toString() Customized --- //TODO

    public String toStringTable(boolean isBoardgame) {
        StringBuilder sb = new StringBuilder();
        if (isBoardgame) {
            sb.append("Username: ").append(this.username).append("\n");
        } else {
            sb.append("Phone: ").append(this.boardgameId).append("\n");
        }
        //this.setFields(sb);
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
        sb.append("Phone: ").append(this.boardgameId).append("\n");
        //this.setFields(sb);
        StringBuilder sBody = new StringBuilder(this.comment);
        int i = 0;
        while ((i = sBody.indexOf(" ", i + 80)) != -1) {
            sBody.replace(i, i+1, "\n");
        }
        sb.append("Body: ").append(sBody);
        return sb.toString();
    }

    /*
    private String setFields(StringBuilder sb) {
        sb.append("Title: ").append(this.title).append("\n");
        Calendar calendar = new GregorianCalendar();
        calendar.setTime(this.dateOfReview);
        sb.append("Date: ").append(calendar.get(Calendar.YEAR)).append("-").append((calendar.get(Calendar.MONTH))+1).
                append("-").append(calendar.get(Calendar.DAY_OF_MONTH)).append("\n");
        sb.append("Vote: ").append(this.rating).append("\n");
        return sb.toString();
    }*/

    @Override
    public String toString() {
        return "Recensione{" +
                "boardgameId=" + boardgameId +
                ", username='" + username + '\'' +
                ", rating=" + rating +
                ", comment='" + comment + '\'' +
                '}';
    }
}
