/*
package it.unipi.dii.lsmsdb.boardgamecafe.mvc.model;

import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

import java.util.ArrayList;
import java.util.List;

@Document(collection = "phones")
public class Boardgame {

    @Id
    private String id;
    private String brand;
    private String name;
    private String picture;
    private String body;
    private String os;
    private String storage;
    private String displaySize;
    private String displayResolution;
    private String cameraPixels;
    private String videoPixels;
    private String ram;
    private String chipset;
    private String batterySize;
    private String batteryType;
    private int releaseYear;
    private List<Review> reviews = new ArrayList<>();

    public Boardgame() {
    }

    public Boardgame (String brand, String name, String picture, String body, String os, String storage,
                 String displaySize, String displayResolution, String cameraPixels, String videoPixels,
                 String ram, String chipset, String batterySize, String batteryType, int releaseYear) {
        this.brand = brand;
        this.name = name;
        this.picture = picture;
        this.releaseYear = releaseYear;
        this.body = body;
        this.os = os;
        this.storage = storage;
        this.displaySize = displaySize;
        this.displayResolution = displayResolution;
        this.cameraPixels = cameraPixels;
        this.videoPixels = videoPixels;
        this.ram = ram;
        this.chipset = chipset;
        this.batterySize = batterySize;
        this.batteryType = batteryType;
    }

    public String getId() {
        return id;
    }

    public String getBrand() {
        return brand;
    }

    public String getName() {
        return name;
    }

    public String getPicture() {
        return picture;
    }

    public int getReleaseYear() {
        return releaseYear;
    }

    public String getBody() {
        return body;
    }

    public String getOs() {
        return os;
    }

    public String getStorage() {
        return storage;
    }

    public String getDisplaySize() {
        return displaySize;
    }

    public String getDisplayResolution() {
        return displayResolution;
    }

    public String getCameraPixels() {
        return cameraPixels;
    }

    public String getVideoPixels() {
        return videoPixels;
    }

    public String getRam() {
        return ram;
    }

    public String getChipset() {
        return chipset;
    }

    public String getBatterySize() {
        return batterySize;
    }

    public String getBatteryType() {
        return batteryType;
    }

    public List<Review> getReviews() {
        return reviews;
    }

    public void setId(String id) {
        this.id = id;
    }

    public void setBrand(String brand) {
        this.brand = brand;
    }

    public void setName(String name) {
        this.name = name;
    }

    public void setPicture(String picture) {
        this.picture = picture;
    }

    public void setBody(String body) {
        this.body = body;
    }

    public void setOs(String os) {
        this.os = os;
    }

    public void setStorage(String storage) {
        this.storage = storage;
    }

    public void setDisplaySize(String displaySize) {
        this.displaySize = displaySize;
    }

    public void setDisplayResolution(String displayResolution) {
        this.displayResolution = displayResolution;
    }

    public void setCameraPixels(String cameraPixels) {
        this.cameraPixels = cameraPixels;
    }

    public void setVideoPixels(String videoPixels) {
        this.videoPixels = videoPixels;
    }

    public void setRam(String ram) {
        this.ram = ram;
    }

    public void setChipset(String chipset) {
        this.chipset = chipset;
    }

    public void setBatterySize(String batterySize) {
        this.batterySize = batterySize;
    }

    public void setBatteryType(String batteryType) {
        this.batteryType = batteryType;
    }

    public void setReleaseYear(int releaseYear) {
        this.releaseYear = releaseYear;
    }

    public void setReviews(List<Review> reviews) {
        this.reviews = reviews;
    }

    public void addReview (Review review) {
        this.reviews.add(0, review);
    }

    public boolean deleteReview(String id) {
        Review review = this.getReviewInPhone(id);
        if (review != null) {
            reviews.remove(review);
            return true;
        }
        return false;
    }

    public Review getReviewInPhone(String id) {
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
        return "Phone{" +
                "id='" + id + '\'' +
                ", brand='" + brand + '\'' +
                ", name='" + name + '\'' +
                ", picture='" + picture + '\'' +
                ", releaseDate=" + releaseYear +
                ", body='" + body + '\'' +
                ", os='" + os + '\'' +
                ", storage='" + storage + '\'' +
                ", displaySize='" + displaySize + '\'' +
                ", displayResolution='" + displayResolution + '\'' +
                ", cameraPixels='" + cameraPixels + '\'' +
                ", videoPixels='" + videoPixels + '\'' +
                ", ram='" + ram + '\'' +
                ", chipset='" + chipset + '\'' +
                ", batterySize='" + batterySize + '\'' +
                ", batteryType='" + batteryType + '\'' +
                ", reviews=" + reviews +
                '}';
    }
}
*/