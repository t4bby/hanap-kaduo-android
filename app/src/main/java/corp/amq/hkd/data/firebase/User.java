package corp.amq.hkd.data.firebase;

import com.google.firebase.database.PropertyName;

import java.io.Serializable;

public class User {

    @PropertyName("display_name")
    String display_name;

    @PropertyName("featured_image")
    String[] featured_image;

    @PropertyName("gender")
    String gender;

    @PropertyName("profile_img_url")
    String profile_img_url;

    @PropertyName("rank")
    String rank;

    @PropertyName("role")
    String role;

    @PropertyName("bio")
    String bio;

    @PropertyName("bio")
    public String getBio() {
        return bio;
    }

    @PropertyName("bio")
    public void setBio(String bio) {
        this.bio = bio;
    }

    @PropertyName("display_name")
    public String getDisplay_name() {
        return display_name;
    }

    @PropertyName("display_name")
    public void setDisplay_name(String display_name) {
        this.display_name = display_name;
    }

    @PropertyName("featured_image")
    public String[] getFeatured_image() {
        return featured_image;
    }

    @PropertyName("featured_image")
    public void setFeatured_image(String[] featured_image) {
        this.featured_image = featured_image;
    }

    @PropertyName("gender")
    public String getGender() {
        return gender;
    }
    @PropertyName("gender")
    public void setGender(String gender) {
        this.gender = gender;
    }

    @PropertyName("profile_img_url")
    public String getProfile_img_url() {
        return profile_img_url;
    }

    @PropertyName("profile_img_url")
    public void setProfile_img_url(String profile_img_url) {
        this.profile_img_url = profile_img_url;
    }

    @PropertyName("rank")
    public String getRank() {
        return rank;
    }

    @PropertyName("rank")
    public void setRank(String rank) {
        this.rank = rank;
    }

    @PropertyName("role")
    public String getRole() {
        return role;
    }

    @PropertyName("role")
    public void setRole(String role) {
        this.role = role;
    }
}
