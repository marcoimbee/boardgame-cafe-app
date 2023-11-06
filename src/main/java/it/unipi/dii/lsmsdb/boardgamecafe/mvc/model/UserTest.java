package it.unipi.dii.lsmsdb.boardgamecafe.mvc.model;

import org.springframework.data.annotation.Id;
import org.springframework.data.annotation.TypeAlias;
import org.springframework.data.mongodb.core.mapping.Document;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

@Document(collection = "users")
//@TypeAlias("user")
public class UserTest {
    @Id
    private String id;
    private String username;
    private String email;
    private String passwordHash;
    private String salt;
    private String name;
    private String surname;
    private String gender;
    private Date dateOfBirth;
    private String nationality;
    private boolean banned;

    public UserTest(){};

    public UserTest(String id, String username, String email, String passwordHash,
                    String salt, String name, String surname, String gender,
                    Date dateOfBirth, String nationality, boolean banned) {
        this.id = id;
        this.username = username;
        this.email = email;
        this.passwordHash = passwordHash;
        this.salt = salt;
        this.name = name;
        this.surname = surname;
        this.gender = gender;
        this.dateOfBirth = dateOfBirth;
        this.nationality = nationality;
        this.banned = banned;
    }

    public String getUsername() {
        return username;
    }

    public String getEmail() {
        return email;
    }

    public String getPasswordHash() {
        return passwordHash;
    }

    public String getSalt() {
        return salt;
    }

    public String getName() {
        return name;
    }

    public String getSurname() {
        return surname;
    }

    public String getGender() {
        return gender;
    }

    public Date getDateOfBirth() {
        return dateOfBirth;
    }

    public String getNationality() {
        return nationality;
    }

    public boolean isBanned() {
        return banned;
    }

    public String getId() {
        return id;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public void setPasswordHash(String passwordHash) {
        this.passwordHash = passwordHash;
    }

    public void setSalt(String salt) {
        this.salt = salt;
    }

    public void setName(String name) {
        this.name = name;
    }

    public void setSurname(String surname) {
        this.surname = surname;
    }

    public void setGender(String gender) {
        this.gender = gender;
    }

    public void setDateOfBirth(Date dateOfBirth) {
        this.dateOfBirth = dateOfBirth;
    }

    public void setNationality(String nationality) {
        this.nationality = nationality;
    }

    public void setBanned(boolean banned) {
        this.banned = banned;
    }

    public void setId(String id) {
        this.id = id;
    }

    @Override
    public String toString() {
        return "User{" +
                "id='" + id + '\'' +
                ", username='" + username + '\'' +
                ", email='" + email + '\'' +
                ", passwordHash='" + passwordHash + '\'' +
                ", salt='" + salt + '\'' +
                ", name='" + name + '\'' +
                ", surname='" + surname + '\'' +
                ", gender='" + gender + '\'' +
                ", dateOfBirth='" + dateOfBirth + '\'' +
                ", nationality='" + nationality + '\'' +
                ", banned=" + banned + '\'' +
                '}';
    }
}

