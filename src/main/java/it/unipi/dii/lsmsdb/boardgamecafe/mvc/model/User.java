/*
package it.unipi.dii.lsmsdb.boardgamecafe.mvc.model;

import org.springframework.data.annotation.TypeAlias;
import org.springframework.data.mongodb.core.mapping.Document;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

@Document(collection = "users")
@TypeAlias("user")
public class User extends GenericUser{

    public User() {
    }

    public User(String username, String salt, String hashedPassword, String _class, String gender,
                String firstName, String lastName, int streetNumber, String streetName, String city,
                String country, String email, Date dateOfBirth, int age) {
        super(username, salt, hashedPassword, _class);
        this.gender = gender;
        this.firstName = firstName;
        this.lastName = lastName;
        this.streetNumber = streetNumber;
        this.streetName = streetName;
        this.city = city;
        this.country = country;
        this.email = email;
        this.dateOfBirth = dateOfBirth;
        this.age = age;
    }

    private String gender;
    private String firstName;
    private String lastName;
    private int streetNumber;
    private String streetName;
    private String city;
    private String country;
    private String email;
    private Date dateOfBirth;
    private int age;
    private List<Review> reviews = new ArrayList<>();

    public String getId() {
        return id;
    }

    public String getGender() {
        return gender;
    }

    public String getFirstName() {
        return firstName;
    }

    public String getLastName() {
        return lastName;
    }

    public int getStreetNumber() {
        return streetNumber;
    }

    public String getStreetName() {
        return streetName;
    }

    public String getCity() {
        return city;
    }

    public String getCountry() {
        return country;
    }

    public String getEmail() {
        return email;
    }

    public Date getDateOfBirth() {
        return dateOfBirth;
    }

    public int getAge() {
        return age;
    }

    public List<Review> getReviews() {
        return reviews;
    }

    public void setGender(String gender) {
        this.gender = gender;
    }

    public void setFirstName(String firstName) {
        this.firstName = firstName;
    }

    public void setLastName(String lastName) {
        this.lastName = lastName;
    }

    public void setStreetNumber(int streetNumber) {
        this.streetNumber = streetNumber;
    }

    public void setStreetName(String streetName) {
        this.streetName = streetName;
    }

    public void setCity(String city) {
        this.city = city;
    }

    public void setCountry(String country) {
        this.country = country;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public void setDateOfBirth(Date dateOfBirth) {
        this.dateOfBirth = dateOfBirth;
    }

    public void setAge(int age) {
        this.age = age;
    }

    public void setReviews(List<Review> reviews) {
        this.reviews = reviews;
    }

    public void addReview(Review review) {
        this.reviews.add(0, review);
    }

    public boolean deleteReview(String id) {
        Review review = this.getReviewInUser(id);
        if (review != null) {
            reviews.remove(review);
            return true;
        }
        return false;
    }

    public Review getReviewInUser(String id) {
        if (!this.reviews.isEmpty()) {
            for (Review review : reviews) {
                if (review.getId().equals(id)) {
                    return review;
                }
            }
        }
        return null;
    }

    @Override
    public String toString() {
        return "User{" +
                "id='" + id + '\'' +
                ", username='" + username + '\'' +
                ", salt='" + salt + '\'' +
                ", sha='" + hashedPassword + '\'' +
                ", gender='" + gender + '\'' +
                ", firstName='" + firstName + '\'' +
                ", lastName='" + lastName + '\'' +
                ", streetNumber='" + streetNumber + '\'' +
                ", streetName='" + streetName + '\'' +
                ", city='" + city + '\'' +
                ", country='" + country + '\'' +
                ", email='" + email + '\'' +
                ", dateOfBirth=" + dateOfBirth +
                ", age=" + age +
                ", reviews=" + reviews +
                '}';
    }
}
*/