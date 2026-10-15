package haven.model;

import java.util.List;

public class Student extends User {
    private String collegeName;
    private String genderPreference;
    private double maxBudget;

    public Student(String username, String password, String fullName, String phone, 
                   String collegeName, String genderPreference, double maxBudget, List<String> wishlist) {
        super(username, password, "STUDENT", fullName, phone, wishlist);
        this.collegeName = collegeName;
        this.genderPreference = genderPreference;
        this.maxBudget = maxBudget;
    }

    public Student(String username, String password, String fullName, String phone, List<String> wishlist) {
        this(username, password, fullName, phone, "SCTCE", "Any", 8000.0, wishlist);
    }

    public String getCollegeName() { return collegeName; }
    public void setCollegeName(String collegeName) { this.collegeName = collegeName; }

    public String getGenderPreference() { return genderPreference; }
    public void setGenderPreference(String genderPreference) { this.genderPreference = genderPreference; }

    public double getMaxBudget() { return maxBudget; }
    public void setMaxBudget(double maxBudget) { this.maxBudget = maxBudget; }
}