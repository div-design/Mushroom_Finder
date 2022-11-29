package com.example.mushroommanager;

public class TicketModel {
    private String mushroomType;
    private String creatorID;
    private String creatorName;
    private String dateCreated;
    private String image1;
    private String image2;

    public TicketModel(){
        super();
    }

    public TicketModel(String mushroomType, String creatorID, String creatorName,String dateCreated, String image1, String image2) {
        this.mushroomType = mushroomType;
        this.creatorID = creatorID;
        this.creatorName = creatorName;
        this.dateCreated = dateCreated;
        this.image1 = image1;
        this.image2 = image2;
    }

    public String getDateCreated() {
        return dateCreated;
    }

    public String getMushroomType() {
        return mushroomType;
    }

    public String getCreatorID() {
        return creatorID;
    }

    public String getCreatorName() {
        return creatorName;
    }

    public String getImage1() {
        return image1;
    }

    public String getImage2() {
        return image2;
    }

    public void setMushroomType(String mushroomType) {
        this.mushroomType = mushroomType;
    }

    public void setCreatorID(String creatorID) {
        this.creatorID = creatorID;
    }

    public void setCreatorName(String creatorName) {
        this.creatorName = creatorName;
    }

    public void setImage1(String image1) {
        this.image1 = image1;
    }

    public void setImage2(String image2) {
        this.image2 = image2;
    }

    public void setDateCreated(String dateCreated) {
        this.dateCreated = dateCreated;
    }
}
