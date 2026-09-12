package haven.model;

import java.util.ArrayList;
import java.util.List;

public class Owner extends User {
    private String contactNumber;
    private List<String> propertyIds;

        public Owner(String id, String name, String email, String password, String contactNumber) {
            super(id, name, email, password, "OWNER");
            this.contactNumber = contactNumber;
            this.propertyIds = new ArrayList<>();
        }
    public String getContactNumber() {return contactNumber; }
    public void setConatctNumber(String contactNumber) { this.contactNumber = contactNumber;}
    public List<String> getPropertyIds() { return propertyIds; }
    public void addPropertyId(String propertyId) {
        if(!propertyIds.contains(propertyId)){
            propertyIds.add(propertyId);
        }
    }
}


