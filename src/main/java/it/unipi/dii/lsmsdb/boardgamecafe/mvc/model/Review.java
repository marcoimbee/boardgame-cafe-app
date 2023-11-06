/*
package it.unipi.dii.lsmsdb.boardgamecafe.mvc.model;

import org.springframework.data.annotation.Id;
import org.springframework.data.annotation.PersistenceConstructor;
import org.springframework.data.mongodb.core.mapping.Document;

import java.util.Calendar;
import java.util.Date;
import java.util.GregorianCalendar;

@Document(collection = "reviews")
public class Review {

    @Id
    private String id;
    private int rating;
    private Date dateOfReview;
    private String title;
    private String body;
    private String username;
    private String phoneName;

    @PersistenceConstructor
    public Review(String id, int rating, Date dateOfReview, String title, String body, String username,
                  String phoneName) {
        this.id = id;
        this.rating = rating;
        this.dateOfReview = dateOfReview;
        this.title = title;
        this.body = body;
        this.username = username;
        this.phoneName = phoneName;
    }

    public Review(ReviewBuilder builder) {
        this.id = builder.id;
        this.rating = builder.rating;
        this.dateOfReview = builder.dateOfReview;
        this.title = builder.title;
        this.body = builder.body;
        this.username = builder.username;
        this.phoneName = builder.phoneName;
    }

    public String getId() {
        return id;
    }

    public int getRating() {
        return rating;
    }

    public Date getDateOfReview() {
        return dateOfReview;
    }

    public String getTitle() {
        return title;
    }

    public String getBody() {
        return body;
    }

    public String getUsername() {
        return username;
    }

    public String getPhoneName() {
        return phoneName;
    }

    public void setId(String id) {
        this.id = id;
    }

    public void setRating(int rating) {
        this.rating = rating;
    }

    public void setDateOfReview(Date dateOfReview) {
        this.dateOfReview = dateOfReview;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public void setBody(String body) {
        this.body = body;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public void setPhoneName(String phoneName) {
        this.phoneName = phoneName;
    }

    public String toStringTable(boolean isPhone) {
        StringBuilder sb = new StringBuilder();
        if (isPhone) {
            sb.append("Username: ").append(this.username).append("\n");
        } else {
            sb.append("Phone: ").append(this.phoneName).append("\n");
        }
        this.setFields(sb);
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
        sb.append("Phone: ").append(this.phoneName).append("\n");
        this.setFields(sb);
        StringBuilder sBody = new StringBuilder(this.body);
        int i = 0;
        while ((i = sBody.indexOf(" ", i + 80)) != -1) {
            sBody.replace(i, i+1, "\n");
        }
        sb.append("Body: ").append(sBody);
        return sb.toString();
    }

    private String setFields(StringBuilder sb) {
        sb.append("Title: ").append(this.title).append("\n");
        Calendar calendar = new GregorianCalendar();
        calendar.setTime(this.dateOfReview);
        sb.append("Date: ").append(calendar.get(Calendar.YEAR)).append("-").append((calendar.get(Calendar.MONTH))+1).
                append("-").append(calendar.get(Calendar.DAY_OF_MONTH)).append("\n");
        sb.append("Vote: ").append(this.rating).append("\n");
        return sb.toString();
    }

    @Override
    public String toString() {
        return "Review{" +
                "id='" + id + '\'' +
                ", rating=" + rating +
                ", dateOfReview=" + dateOfReview +
                ", title='" + title + '\'' +
                ", body='" + body + '\'' +
                ", username='" + username + '\'' +
                ", phoneName='" + phoneName + '\'' +
                '}';
    }

    public static class ReviewBuilder {
        private String id;
        private int rating;
        private Date dateOfReview;
        private String title;
        private String body;
        private String username;
        private String phoneName;

        public ReviewBuilder(int rating, Date dateOfReview, String title, String body) {
            this.rating = rating;
            this.dateOfReview = dateOfReview;
            this.title = title;
            this.body = body;
        }

        public ReviewBuilder(Review review) {
            this.rating = review.rating;
            this.dateOfReview = review.dateOfReview;
            this.title = review.title;
            this.body = review.body;
        }
        public ReviewBuilder id (String id) {
            this.id = id;
            return this;
        }

        public ReviewBuilder username (String username) {
            this.username = username;
            return this;
        }

        public ReviewBuilder phoneName (String phoneName) {
            this.phoneName = phoneName;
            return this;
        }

        public Review build() {
            return new Review(this);
        }

    }
}
*/

