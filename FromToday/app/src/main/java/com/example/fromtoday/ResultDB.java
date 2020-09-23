package com.example.fromtoday;

public class ResultDB {
    public String id;
    public String item;
    public String calorie;

    public void setId(String id){
        this.id=id;
    }
    public void setItem(String item){
        this.item=item;
    }
    public void setCalorie(String calorie){
        this.calorie=calorie;
    }

    public String getId() {
        return id;
    }

    public String getCalorie() {
        return calorie;
    }

    public String getItem() {
        return item;
    } //주석
}
