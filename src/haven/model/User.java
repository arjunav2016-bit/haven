package haven.model;

public abstract class User{
    private String id;
    private String name;
    private String username;
    private String email;
    private String phoneNumber;
    private String password;
    private String role; //role can be "Student", or "Owner"

    public User(String id, String name , String username, String email, String phoneNumber, String password, String role){
        this.id = id;
        this.name = name;
        this.username = username;
        this.email = email;
        this.phoneNumber = phoneNumber;
        this.password = password;
        this.role = role;
    }

    public String getId() { return id; }
    public String getName() {return name; }
    public String getUsername() { return username; }  
    public String getEmail() { return email; }
    public String getPhoneNumber() { return phoneNumber; }
    public String getPassword() { return password; }
    public String getRole() { return role; }

    public void setName(String name) { this.name = name; }
    public void setUsername(String username) { this.username = username; }
    public void setEmail(String email) { this.email = email; }
    public void setPhoneNumber(String phoneNumber) { this.phoneNumber = phoneNumber; }
    public void setPassword(String password) { this.password = password; }
    
}