package it.unipi.dii.lsmsdb.boardgamecafe.mvc.model.mongo;

import org.springframework.data.annotation.TypeAlias;
import org.springframework.data.mongodb.core.mapping.Document;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

@Document(collection = "users")
@TypeAlias("user")
public class UserModelMongo extends GenericUserModelMongo {
    private String name;
    private String surname;
    private String gender;
    private Date dateOfBirth;
    private String nationality;
    private boolean banned;
    private List<ReviewModelMongo> reviews = new ArrayList<>();

    public UserModelMongo(){};

    public UserModelMongo(String id, String username, String passwordHash,
                          String salt, String _class, String email,
                          String name, String surname, String gender,
                          Date dateOfBirth, String nationality, boolean banned) {

        super(id, username, email, salt, passwordHash, _class);
        this.name = name;
        this.surname = surname;
        this.gender = gender;
        this.dateOfBirth = dateOfBirth;
        this.nationality = nationality;
        this.banned = banned;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getSurname() {
        return surname;
    }

    public void setSurname(String surname) {
        this.surname = surname;
    }

    public String getGender() {
        return gender;
    }

    public void setGender(String gender) {
        this.gender = gender;
    }

    public Date getDateOfBirth() {
        return dateOfBirth;
    }

    public void setDateOfBirth(Date dateOfBirth) {
        this.dateOfBirth = dateOfBirth;
    }

    public String getNationality() {
        return nationality;
    }

    public void setNationality(String nationality) {
        this.nationality = nationality;
    }

    public boolean isBanned() {
        return banned;
    }

    public void setBanned(boolean banned) {
        this.banned = banned;
    }

    public List<ReviewModelMongo> getReviews() {
        return reviews;
    }

    public void setReviews(List<ReviewModelMongo> reviews) {
        this.reviews = reviews;
    }

    public void addReview(ReviewModelMongo review) {
        this.reviews.add(0, review);
    }

    public ReviewModelMongo getReviewInUser(String id)
    {
        for (ReviewModelMongo review : reviews)
            if (review.getId().equals(id))
                return review;
        return null;
    }

    public boolean deleteReview(String id) {
        ReviewModelMongo review = this.getReviewInUser(id);
        if (review != null) {
            reviews.remove(review);
            return true;
        }
        return false;
    }

    public boolean deleteReview(ReviewModelMongo review)
    {
        if (this.reviews.contains(review))
        {
            reviews.remove(review);
            return true;
        }
        return false;
    }

    @Override
    public String toString() {
        return "UserModelMongo{" +
                "id='" + id + '\'' +
                ", username='" + username + '\'' +
                ", salt='" + salt + '\'' +
                ", passwordHash='" + passwordHash + '\'' +
                ", email='" + email + '\'' +
                ", name='" + name + '\'' +
                ", surname='" + surname + '\'' +
                ", gender='" + gender + '\'' +
                ", dateOfBirth=" + dateOfBirth +
                ", nationality='" + nationality + '\'' +
                ", banned=" + banned +
                ", reviews=" + reviews +
                '}';
    }


}

