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
    private String thumbnail;
    private String image;
    private String description;
    private int yearPublished;
    private int minPlayers;
    private int maxPlayers;
    private int playingTime;
    private int minAge;
    private List<String> boardgameCategoryList = new ArrayList<>();
    private List<String> boardgameDesignerList = new ArrayList<>();
    private List<String> boardgamePublisherList = new ArrayList<>();
    private List<ReviewModelMongo> reviewMongo = new ArrayList<>();

    public BoardgameModelMongo(){}

    public BoardgameModelMongo(String id, String boardgameName,
                               String thumbnail, String image, String description,
                               int yearPublished, int minPlayers, int maxPlayers,
                               int playingTime, int minAge, List<String> boardgameCategoryList,
                               List<String> boardgameDesignerList, List<String> boardgamePublisherList) {

        this.id = id;
        this.boardgameName = boardgameName;
        this.thumbnail = thumbnail;
        this.image = image;
        this.description = description;
        this.yearPublished = yearPublished;
        this.minPlayers = minPlayers;
        this.maxPlayers = maxPlayers;
        this.playingTime = playingTime;
        this.minAge = minAge;
        this.boardgameCategoryList = boardgameCategoryList;
        this.boardgameDesignerList = boardgameDesignerList;
        this.boardgamePublisherList = boardgamePublisherList;
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

    public List<String> getBoardgameCategoryList() {
        return boardgameCategoryList;
    }

    public void setBoardgameCategoryList(List<String> boardgameCategoryList) {
        this.boardgameCategoryList = boardgameCategoryList;
    }

    public List<String> getBoardgameDesignerList() {
        return boardgameDesignerList;
    }

    public void setBoardgameDesignerList(List<String> boardgameDesignerList) {
        this.boardgameDesignerList = boardgameDesignerList;
    }

    public List<String> getBoardgamePublisherList() {
        return boardgamePublisherList;
    }

    public void setBoardgamePublisherList(List<String> boardgamePublisherList) {
        this.boardgamePublisherList = boardgamePublisherList;
    }


    // --- Reviews Management ---
    public List<ReviewModelMongo> getReviews() {
        return reviewMongo;
    }
    public void setReviews(List<ReviewModelMongo> reviewMongo) {
        this.reviewMongo = reviewMongo;
    }

    public void addReview (ReviewModelMongo reviewMongo) {
        this.reviewMongo.add(0, reviewMongo);
    }

    public ReviewModelMongo getReviewInBoardgame(String id) {
        if (!this.reviewMongo.isEmpty()) {
            for (ReviewModelMongo reviewMongo : reviewMongo) {
                if (reviewMongo.getId().equals(id)) {
                    return reviewMongo;
                }
            }
        }
        return null;
    }

    public boolean deleteReview(String id) {
        ReviewModelMongo reviewMongo = this.getReviewInBoardgame(id);
        if (reviewMongo != null) {
            this.reviewMongo.remove(reviewMongo);
            return true;
        }
        return false;
    }

    @Override
    public String toString() {
        return "Boardgame{" +
                "id='" + id + '\'' +
                ", boardgameName='" + boardgameName + '\'' +
                ", thumbnail='" + thumbnail + '\'' +
                ", image='" + image + '\'' +
                ", description='" + description + '\'' +
                ", yearPublished=" + yearPublished +
                ", minPlayers=" + minPlayers +
                ", maxPlayers=" + maxPlayers +
                ", playingTime=" + playingTime +
                ", minAge=" + minAge +
                ", boardgameCategoryList=" + boardgameCategoryList +
                ", boardgameDesignerList=" + boardgameDesignerList +
                ", boardgamePublisherList=" + boardgamePublisherList +
                ", reviews=" + reviewMongo +
                '}';
    }
}