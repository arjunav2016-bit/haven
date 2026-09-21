package haven.model;

import java.util.ArrayList;
import java.util.List;

public class Student extends User{
    private String collegeName;
    private String genderPreference;
    private double maxBudget;
    private List<String> wishlistIds;

    public Student(String id, String name, String username, String email, String phoneNumber, String password, String collegeName, String genderPreference, double maxBudget){
        super(id,name,username,email,phoneNumber,password, "STUDENT");
        this.collegeName = collegeName;
        this.genderPreference = genderPreference;
        this.maxBudget = maxBudget;
        this.wishlistIds = new ArrayList<>();
    }
    public String getCollegeName(){ return collegeName; }
    public void setCollegeName(String collegeName) { this.collegeName = collegeName; }

    public String getGenderPreference() {return genderPreference; }
    public void setGenderPreference(String genderPreference) {this.genderPreference = genderPreference; }
    
    public double getMaxBudget() {return maxBudget;}
    public void setMaxBudget(double maxBudget) {this.maxBudget = maxBudget;}

    public List<String> getWishlistIds() {return wishlistIds; }
    public void addWishlistId(String accommodationId) {
        if (!wishlistIds.contains(accommodationId)) {
            wishlistIds.add(accommodationId);
        }
    }

}