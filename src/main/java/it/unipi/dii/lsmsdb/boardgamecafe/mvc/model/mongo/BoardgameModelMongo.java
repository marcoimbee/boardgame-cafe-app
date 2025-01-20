package it.unipi.dii.lsmsdb.boardgamecafe.mvc.model.mongo;

import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;
import java.util.ArrayList;
import java.util.List;

@Document(collection = "boardgames")
public class BoardgameModelMongo {

    @Id
    private String id;
    private String boardgameName;
    private String image;
    private String description;
    private int yearPublished;
    private int minPlayers;
    private int maxPlayers;
    private int playingTime;
    private int minAge;
    private Double avgRating;
    private int reviewCount;
    private List<String> boardgameCategory = new ArrayList<>();
    private List<String> boardgameDesigner = new ArrayList<>();
    private List<String> boardgamePublisher = new ArrayList<>();

    public BoardgameModelMongo(){}

    public BoardgameModelMongo(String boardgameName, String image, String description,
                               int yearPublished, int minPlayers, int maxPlayers,
                               int playingTime, int minAge, List<String> boardgameCategory,
                               List<String> boardgameDesigner, List<String> boardgamePublisher) {

        this.boardgameName = boardgameName;
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
        this.avgRating = -1.0;
        this.reviewCount = 0;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getBoardgameName () {
        return boardgameName;
    }

    public void setBoardgameName(String boardgameName) {
        this.boardgameName = boardgameName;
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

    public List<String> getBoardgameCategory() {
        return boardgameCategory;
    }

    public void setBoardgameCategory(List<String> boardgameCategory) {
        this.boardgameCategory = boardgameCategory;
    }

    public List<String> getBoardgameDesigner() {
        return boardgameDesigner;
    }

    public void setBoardgameDesigner(List<String> boardgameDesigner) {
        this.boardgameDesigner = boardgameDesigner;
    }

    public List<String> getBoardgamePublisher() {
        return boardgamePublisher;
    }

    public void setBoardgamePublisher(List<String> boardgamePublisher) {
        this.boardgamePublisher = boardgamePublisher;
    }

    public void updateAvgRatingAfterReviewDeletion(int deletedRating) {
        if (this.reviewCount == 0) {
            return;
        }

        if (this.reviewCount == 1) {
            this.avgRating = -1.0;
        } else {
            this.avgRating = ((this.reviewCount * this.avgRating) - deletedRating) / (this.reviewCount - 1);
        }
        this.reviewCount--;
    }

    public void updateAvgRatingAfterReviewUpdate(int newRating, int oldRating) {
        this.avgRating = ((this.reviewCount * this.avgRating) - oldRating + newRating) / this.reviewCount;
    }

    public void updateAvgRatingAndReviewCount (int reviewRating) {
        Double newAvg = ((this.reviewCount * this.avgRating) + reviewRating) / (this.reviewCount + 1);
        this.reviewCount++;
        this.avgRating = newAvg;
    }

    public void updateAvgRatingAfterUserDeletion(List<Integer> ratings) {
        int ratingsSum = ratings.stream().mapToInt(Integer::intValue).sum();
        int reviewCount = ratings.size();

        if (this.reviewCount == reviewCount) {
            this.avgRating = -1.0;
            this.reviewCount = 0;
        } else {
            this.avgRating = ((this.reviewCount * this.avgRating) - ratingsSum) / (this.reviewCount - reviewCount);
            this.reviewCount -= reviewCount;
        }
    }

    public double getAvgRating() {
        return avgRating;
    }

    public void setAvgRating(double avgRating) {
        this.avgRating = avgRating;
    }

    public int getReviewCount() {
        return reviewCount;
    }

    public void setReviewCount(int reviewCount) {
        this.reviewCount = reviewCount;
    }

    @Override
    public String toString() {
        return "Boardgame{" +
                "id='" + id + '\'' +
                ", boardgameName='" + boardgameName + '\'' +
                ", image='" + image + '\'' +
                ", description='" + description + '\'' +
                ", yearPublished=" + yearPublished +
                ", minPlayers=" + minPlayers +
                ", maxPlayers=" + maxPlayers +
                ", playingTime=" + playingTime +
                ", minAge=" + minAge +
                ", boardgameCategoryList=" + boardgameCategory +
                ", boardgameDesignerList=" + boardgameDesigner +
                ", boardgamePublisherList=" + boardgamePublisher +
                '}';
    }
}
