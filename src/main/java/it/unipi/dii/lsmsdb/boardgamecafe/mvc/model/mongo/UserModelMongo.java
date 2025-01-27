package it.unipi.dii.lsmsdb.boardgamecafe.mvc.model.mongo;

import org.springframework.data.annotation.TypeAlias;
import org.springframework.data.mongodb.core.mapping.Document;
import java.time.LocalDate;
import java.time.ZoneId;
import java.util.Date;

@Document(collection = "users")
@TypeAlias("user")
public class UserModelMongo extends GenericUserModelMongo {

    private String name;
    private String surname;
    private String gender;
    private Date dateOfBirth;
    private String nationality;
    private boolean banned;

    public UserModelMongo(){}

    public UserModelMongo(String id, String username, String email,
                          String name, String surname, String gender,
                          Date dateOfBirth, String nationality, boolean banned,
                          String salt, String passwordHash, String _class)
    {
        super(id, username, email, salt, passwordHash, _class);
        this.name = name;
        this.surname = surname;
        this.gender = gender;
        this.dateOfBirth = dateOfBirth;
        this.nationality = nationality;
        this.banned = banned;
    }

    public UserModelMongo(String username, String email,
                          String name, String surname, String gender,
                          Date dateOfBirth, String nationality, boolean banned,
                          String salt, String passwordHash, String _class)
    {
        super(username, email, salt, passwordHash, _class);
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

    /*
        Dates need to be manually manipulated in order to prevent Spring and MongoDB managing them.
     */
    public LocalDate getDateOfBirth() {
        Date dateOfBirth = this.dateOfBirth;
        // Converting Date in LocalDate to avoid jet lag problems
        return dateOfBirth.toInstant().atZone(ZoneId.of("UTC")).toLocalDate();
    }
    public void setDateOfBirth(LocalDate dateOfBirth) {
        // Converting LocalDate to Date in UTC to be able to insert into MongoDB
        this.dateOfBirth = Date.from(dateOfBirth.atStartOfDay(ZoneId.of("UTC")).toInstant());
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

    @Override
    public String toString() {
        return "UserModelMongo{" +
                "id='" + id + '\'' +
                ", username='" + username + '\'' +
                ", email='" + email + '\'' +
                ", name='" + name + '\'' +
                ", surname='" + surname + '\'' +
                ", gender='" + gender + '\'' +
                ", dateOfBirth=" + dateOfBirth +
                ", nationality='" + nationality + '\'' +
                ", banned=" + banned +
                ", salt='" + salt + '\'' +
                ", passwordHash='" + passwordHash + '\'' +
                '}';
    }
}
