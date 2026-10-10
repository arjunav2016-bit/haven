package haven.model;

import java.util.ArrayList;
import java.util.List;

public class Owner extends User {
    private List<String> propertyIds;

        public Owner(String id, String name, String username, String email, String phoneNumber, String password) {
            super(id, name, username, email, phoneNumber, password, "OWNER");
            this.propertyIds = new ArrayList<>();
        }
    public List<String> getPropertyIds() { return propertyIds; }
    public void addPropertyId(String propertyId) {
        if(!propertyIds.contains(propertyId)){
            propertyIds.add(propertyId);
        }
    }
}


