package haven.model;

import java.util.ArrayList;
import java.util.List;

public class Owner extends User {
    private List<String> propertyIds;

    public Owner(String username, String password, String fullName, String phone, List<String> propertyIds, List<String> wishlist) {
        super(username, password, "OWNER", fullName, phone, wishlist);
        this.propertyIds = (propertyIds != null) ? propertyIds : new ArrayList<>();
    }

    public Owner(String username, String password, String fullName, String phone, List<String> wishlist) {
        this(username, password, fullName, phone, new ArrayList<>(), wishlist);
    }

    public List<String> getPropertyIds() {
        return (propertyIds != null) ? new ArrayList<>(propertyIds) : new ArrayList<>();
    }

    public void setPropertyIds(List<String> propertyIds) {
        this.propertyIds = (propertyIds != null) ? new ArrayList<>(propertyIds) : new ArrayList<>();
    }

    public void addPropertyId(String propertyId) {
        if (propertyIds == null) propertyIds = new ArrayList<>();
        if (!propertyIds.contains(propertyId)) {
            propertyIds.add(propertyId);
        }
    }
}
