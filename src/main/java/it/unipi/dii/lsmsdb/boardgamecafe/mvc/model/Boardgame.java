package it.unipi.dii.lsmsdb.boardgamecafe.mvc.model;

import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

import java.util.ArrayList;
import java.util.List;

@Document(collection = "boardgame")
public class Boardgame {

    @Id
    private String boardgameId;
    private String name;
    private String thumbnail;
    private String image;
    private String description;
    private int yearPublished;
    private int minPlayers;
    private int maxPlayers;
    private int playingTime;
    private int minAge;
    private String boardgameCategory;
    private String boardgameDesigner;
    private String boardgamePublisher;
    private List<Review> reviews = new ArrayList<>();

    public Boardgame(){}

    public Boardgame(String boardgameId, String name, String thumbnail, String image, String description, int yearPublished, int minPlayers, int maxPlayers, int playingTime, int minAge, String boardgameCategory, String boardgameDesigner, String boardgamePublisher) {
        this.boardgameId = boardgameId;
        this.name = name;
        this.thumbnail = thumbnail;
        this.image = image;
        this.description = description;
        this.yearPublished = yearPublished;
        this.minPlayers = minPlayers;
        this.maxPlayers = maxPlayers;
        this.playingTime = playingTime;
        this.minAge = minAge;
        this.boardgameCategory = boardgameCategory;
        this.boardgameDesigner = boardgameDesigner;
        this.boardgamePublisher = boardgamePublisher;
    }

    public String getBoardgameId() {
        return boardgameId;
    }

    public void setBoardgameId(String boardgameId) {
        this.boardgameId = boardgameId;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getThumbnail() {
        return thumbnail;
    }

    public void setThumbnail(String thumbnail) {
        this.thumbnail = thumbnail;
    }

    public String getImage() {
        return image;
    }

    public void setImage(String image) {
        this.image = image;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public int getYearPublished() {
        return yearPublished;
    }

    public void setYearPublished(int yearPublished) {
        this.yearPublished = yearPublished;
    }

    public int getMinPlayers() {
        return minPlayers;
    }

    public void setMinPlayers(int minPlayers) {
        this.minPlayers = minPlayers;
    }

    public int getMaxPlayers() {
        return maxPlayers;
    }

    public void setMaxPlayers(int maxPlayers) {
        this.maxPlayers = maxPlayers;
    }

    public int getPlayingTime() {
        return playingTime;
    }

    public void setPlayingTime(int playingTime) {
        this.playingTime = playingTime;
    }

    public int getMinAge() {
        return minAge;
    }

    public void setMinAge(int minAge) {
        this.minAge = minAge;
    }

    public String getBoardgameCategory() {
        return boardgameCategory;
    }

    public void setBoardgameCategory(String boardgameCategory) {
        this.boardgameCategory = boardgameCategory;
    }

    public String getBoardgameDesigner() {
        return boardgameDesigner;
    }

    public void setBoardgameDesigner(String boardgameDesigner) {
        this.boardgameDesigner = boardgameDesigner;
    }

    public String getBoardgamePublisher() {
        return boardgamePublisher;
    }

    public void setBoardgamePublisher(String boardgamePublisher) {
        this.boardgamePublisher = boardgamePublisher;
    }


    // --- Reviews Management ---
    public List<Review> getReviews() {
        return reviews;
    }
    public void setReviews(List<Review> reviews) {
        this.reviews = reviews;
    }

    public void addReview (Review review) {
        this.reviews.add(0, review);
    }

    public boolean deleteReview(String id) {
        Review review = this.getReviewInBoardgame(id);
        if (review != null) {
            reviews.remove(review);
            return true;
        }
        return false;
    }

    public Review getReviewInBoardgame(String id) {
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
        return "Boardgame{" +
                "boardgameId='" + boardgameId + '\'' +
                ", name='" + name + '\'' +
                ", thumbnail='" + thumbnail + '\'' +
                ", image='" + image + '\'' +
                ", description='" + description + '\'' +
                ", yearPublished=" + yearPublished +
                ", minPlayers=" + minPlayers +
                ", maxPlayers=" + maxPlayers +
                ", playingTime=" + playingTime +
                ", minAge=" + minAge +
                ", boardgameCategory='" + boardgameCategory + '\'' +
                ", boardgameDesigner='" + boardgameDesigner + '\'' +
                ", boardgamePublisher='" + boardgamePublisher + '\'' +
                ", reviews=" + reviews +
                '}';
    }
}