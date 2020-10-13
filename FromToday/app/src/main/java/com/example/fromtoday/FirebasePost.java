package com.example.fromtoday;

import com.google.firebase.database.Exclude;

import java.util.HashMap;
import java.util.Map;

public class FirebasePost {

    public String profile;
    public String name;
    public String email;
    public String gender;
    public String age;
    public String birthday;
    public Double total_kcal;

    public FirebasePost(){
        // Default constructor required for calls to DataSnapshot.getValue(FirebasePost.class)
    }

    public FirebasePost(String profile, String name, String email, String gender, String age, String birthday) {
        this.profile = profile;
        this.name = name;
        this.email = email;
        this.gender = gender;
        this.age = age;
        this.birthday = birthday;
    }

    public FirebasePost(String profile, String name, String email, String gender, String age, String birthday, Double total_kcal) {
        this.profile = profile;
        this.name = name;
        this.email = email;
        this.gender = gender;
        this.age = age;
        this.birthday = birthday;
        this.total_kcal = total_kcal;

    }

    public FirebasePost(double total_kcal) {
        this.total_kcal = total_kcal;
    }


    @Exclude
    public Map<String, Object> toMap() {
        HashMap<String, Object> result = new HashMap<>();
        result.put("name", name);
        result.put("age", email);
        result.put("height", gender);
        result.put("gender", age);
        result.put("birthday", birthday);
        return result;
    }
}

