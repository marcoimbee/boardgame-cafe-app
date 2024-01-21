package it.unipi.dii.lsmsdb.boardgamecafe.mvc.model.mongo;

import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

import java.util.Calendar;
import java.util.Date;
import java.util.GregorianCalendar;

@Document(collection = "reviews")
public class ReviewModelMongo {

    @Id
    private String id;
    private String boardgameName;
    private String username;
    private int rating;
    private String body;
    private Date dateOfReview;

    // - Costruttore vuoto utile per la corretta deserializzazione JSON in Spring -
    public ReviewModelMongo() {}

    // - Costruttore con parametri -
    public ReviewModelMongo(String id, String boardgameName,
                            String username, int rating,
                            String body, Date dateOfReview) {
        this.id = id;
        this.boardgameName = boardgameName;
        this.username = username;
        this.rating = rating;
        this.body = body;
        this.dateOfReview = dateOfReview;
    }

    // - Constructor Builder-Pattern-Based -
    public ReviewModelMongo(ReviewBuilder builder) {
        this.id = builder.id;
        this.boardgameName = builder.boardgameName;
        this.rating = builder.rating;
        this.username = builder.username;
        this.body = builder.body;
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

        StringBuilder sBody = new StringBuilder(this.body);
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

        StringBuilder sBody = new StringBuilder(this.body);
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
        private int rating;
        private String body;
        private Date dateOfReview;

        public ReviewBuilder(int rating, String username,
                             String body, Date dateOfReview)
        {
            this.rating = rating;
            this.username = username;
            this.body = body;
            this.dateOfReview = dateOfReview;
        }
        public ReviewBuilder(ReviewModelMongo reviewMongo) {
            this.rating = reviewMongo.rating;
            this.username = reviewMongo.username;
            this.body = reviewMongo.body;
            this.dateOfReview = reviewMongo.dateOfReview;
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
        public ReviewModelMongo build() {
            return new ReviewModelMongo(this);
        }
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
