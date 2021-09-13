package rmit.ad.rmitrides;

import com.google.firebase.auth.FirebaseUser;

public class User {
    private static FirebaseUser fireBaseUser = null;
    private String email;
    private String username;
    private String fullName;
    private String id = "";
    String avatar;
    private String birthdate = "Not Specified";
    private String gender = "Not Specified";
    private String currentBooking_AsDriver;
    private String currentBooking_AsPassenger;
    private String role = "";

    public User() {
    }

    public String getCurrentBooking_AsDriver() {
        return currentBooking_AsDriver;
    }

    public User setCurrentBooking_AsDriver(String currentBooking_AsDriver) {
        this.currentBooking_AsDriver = currentBooking_AsDriver;
        return this;
    }

    public String getCurrentBooking_AsPassenger() {
        return currentBooking_AsPassenger;
    }

    public void setCurrentBooking_AsPassenger(String currentBooking_AsPassenger) {
        this.currentBooking_AsPassenger = currentBooking_AsPassenger;
    }

    public User(String email, String username, String fullName) {
        this.email = email;
        this.username = username;
        this.fullName = fullName;
        this.avatar = "default";
    }

    public User(User user) {
        this.email = user.getEmail();
        this.username = user.getUsername();
        this.fullName = user.getFullName();
        this.id = user.getId();
        this.birthdate = user.getBirthdate();
        this.gender = user.getGender();
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public String getFullName() {
        return fullName;
    }

    public void setFullName(String fullName) {
        this.fullName = fullName;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public User(String email, String username, String fullName, String id, String avatar, String birthdate, String gender, String currentBooking_AsDriver, String currentBooking_AsPassenger, String role) {
        this.email = email;
        this.username = username;
        this.fullName = fullName;
        this.id = id;
        this.avatar = avatar;
        this.birthdate = birthdate;
        this.gender = gender;
        this.currentBooking_AsDriver = currentBooking_AsDriver;
        this.currentBooking_AsPassenger = currentBooking_AsPassenger;
        this.role = role;
    }

    public String getAvatar() {
        return avatar;
    }

    public void setAvatar(String avatar) {
        this.avatar = avatar;
    }

    public static FirebaseUser getFireBaseUser() {
        return fireBaseUser;
    }

    public static void setFireBaseUser(FirebaseUser fireBaseUser) {
        User.fireBaseUser = fireBaseUser;
    }

    public String getBirthdate() {
        return birthdate;
    }

    public void setBirthdate(String birthdate) {
        this.birthdate = birthdate;
    }

    public String getGender() {
        return gender;
    }

    public void setGender(String gender) {
        this.gender = gender;
    }

    public String getRole() {
        return role;
    }

    public void setRole(String role) {
        this.role = role;
    }
}
