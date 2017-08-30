package org.iofstorm.konatravels.model;

import com.google.gson.TypeAdapter;
import com.google.gson.stream.JsonReader;
import com.google.gson.stream.JsonWriter;
import org.iofstorm.konatravels.Utils;

import java.io.IOException;
import java.time.LocalDate;
import java.time.ZoneId;

public class User {
    public static final String ID = "id";
    public static final String FIRST_NAME = "first_name";
    public static final String LAST_NAME = "last_name";
    public static final String GENDER = "gender";
    public static final String BIRTH_DATE = "birth_date";
    public static final String EMAIL = "email";

    public static final Integer EMAIL_LENGTH = 100;
    public static final Integer NAME_LENGTH = 50;
    public static final Long BIRTH_DATE_MIN = LocalDate.of(1930, 1, 1).atStartOfDay().atZone(ZoneId.systemDefault()).toEpochSecond();
    public static final Long BIRTH_DATE_MAX = LocalDate.of(1999, 1, 1).atStartOfDay().atZone(ZoneId.systemDefault()).toEpochSecond();

    // 32 bit int unique
    Integer id;

    // 0-100 unicode string unique
    private String email;

    // 0-50 unicode string
    private String firstName;

    // 0-50 unicode string
    private String lastName;

    // m - male, f - female
    Gender gender;

    // long ms from 1970
    private long birthDate = Long.MIN_VALUE;

    int age = Integer.MIN_VALUE;

    private boolean isValidUpdate = true;

    public static User invalidUpdate() {
        User u = new User();
        u.isValidUpdate = false;
        return u;
    }

    public Integer getId() {
        return id;
    }

    public void setId(Integer id) {
        this.id = id;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getFirstName() {
        return firstName;
    }

    public void setFirstName(String firstName) {
        this.firstName = firstName;
    }

    public String getLastName() {
        return lastName;
    }

    public void setLastName(String lastName) {
        this.lastName = lastName;
    }

    public Gender getGender() {
        return gender;
    }

    public void setGender(Gender gender) {
        this.gender = gender;
    }

    public long getBirthDate() {
        return birthDate;
    }

    public void setBirthDate(long birthDate) {
        this.birthDate = birthDate;
    }

    public int getAge() {
        return age;
    }

    public void setAge(int age) {
        this.age = age;
    }

    public boolean isValidUpdate() {
        return isValidUpdate;
    }

    public static class UserAdapter extends TypeAdapter<User> {

        @Override
        public void write(JsonWriter out, User value) throws IOException {
            out.beginObject();
            out.name(ID).value(value.id);
            out.name(EMAIL).value(value.email);
            out.name(FIRST_NAME).value(value.firstName);
            out.name(LAST_NAME).value(value.lastName);
            out.name(GENDER).value(value.gender.getVal());
            out.name(BIRTH_DATE).value(value.birthDate);
            out.endObject();
        }

        @Override
        public User read(JsonReader in) throws IOException {
            User user = new User();
            in.beginObject();
            while (in.hasNext()) {
                switch (in.nextName()) {
                    case ID:
                        user.setId(in.nextInt());
                        user.isValidUpdate = false;
                        break;
                    case EMAIL:
                        user.setEmail(in.nextString());
                        if (user.email == null || user.email.length() > User.EMAIL_LENGTH) user.isValidUpdate = false;
                        break;
                    case FIRST_NAME:
                        user.setFirstName(in.nextString());
                        if (user.firstName == null || user.firstName.length() > User.NAME_LENGTH) user.isValidUpdate = false;
                        break;
                    case LAST_NAME:
                        user.setLastName(in.nextString());
                        if (user.lastName == null || user.lastName.length() > User.NAME_LENGTH) user.isValidUpdate = false;
                        break;
                    case GENDER:
                        String genderStr = in.nextString();
                        user.setGender(Gender.fromString(genderStr));
                        if (user.gender == Gender.UNKNOWN) user.isValidUpdate = false;
                        break;
                    case BIRTH_DATE:
                        user.setBirthDate(in.nextLong());
                        user.setAge(Utils.calcAge(user.birthDate));
                        if (user.birthDate == Long.MIN_VALUE) user.isValidUpdate = false;
                }
            }
            in.endObject();
            return user;
        }
    }
}
