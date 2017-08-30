package org.iofstorm.konatravels.model;

import com.google.gson.TypeAdapter;
import com.google.gson.stream.JsonReader;
import com.google.gson.stream.JsonWriter;
import org.iofstorm.konatravels.service.LocationService;
import org.iofstorm.konatravels.service.UserService;

import java.io.IOException;

public class Visit implements Comparable<Visit> {
    static final String LOCATION_ID = "location";
    static final String USER_ID = "user";
    static final String VISITED_AT = "visited_at";
    static final String MARK = "mark";
    static final String ID = "id";

    // 32 bit int unique
    private Integer id;

    // timestamp, 01.01.2000 - 01.01.2015
    private long visitedAt = Long.MIN_VALUE;

    // int 0 - 5
    private int mark = Integer.MIN_VALUE;

    public Location location;

    public User user;

    private boolean isValidUpdate = true;

    public Integer getId() {
        return id;
    }

    public void setId(Integer id) {
        this.id = id;
    }

    public Integer getLocationId() {
        return location == null ? null : location.id;
    }

    public Integer getUserId() {
        return user == null ? null : user.id;
    }

    public long getVisitedAt() {
        return visitedAt;
    }

    public void setVisitedAt(long visitedAt) {
        this.visitedAt = visitedAt;
    }

    public int getMark() {
        return mark;
    }

    public void setMark(int mark) {
        this.mark = mark;
    }

    public void setLocation(Location location) {
        this.location = location;
    }

    public void setUser(User user) {
        this.user = user;
    }

    public String getLocationCountry() {
        return location.country;
    }

    public int getLocationDistance() {
        return location.distance;
    }

    public String getLocationPlace() {
        return location.place;
    }

    public int getUserAge() {
        return user.age;
    }

    public Gender getUserGender() {
        return user.gender;
    }

    public boolean isValidUpdate() {
        return isValidUpdate;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        Visit visit = (Visit) o;

        return id != null ? id.equals(visit.id) : visit.id == null;
    }

    @Override
    public int hashCode() {
        return id != null ? id.hashCode() : 0;
    }

    @Override
    public int compareTo(Visit o) {
        if (o.id.intValue() == this.id.intValue()) return 0;
        if (this.visitedAt >= o.visitedAt) return 1;
        return -1;
    }

    @Override
    public String toString() {
        return "Visit[" +
                "id=" + id +
                ", visitedAt=" + visitedAt +
                ", mark=" + mark +
                ", locationId=" + location.id +
                ", userId=" + user.id +
                ", isValidUpdate=" + isValidUpdate +
                ']';
    }

    public static class VisitAdapter extends TypeAdapter<Visit> {

        @Override
        public void write(JsonWriter out, Visit value) throws IOException {
            out.beginObject();
            out.name(ID).value(value.id);
            out.name(USER_ID).value(value.user.id);
            out.name(LOCATION_ID).value(value.location.id);
            out.name(VISITED_AT).value(value.visitedAt);
            out.name(MARK).value(value.mark);
            out.endObject();
        }

        @Override
        public Visit read(JsonReader in) throws IOException {
            Visit visit = new Visit();
            in.beginObject();
            while (in.hasNext()) {
                switch (in.nextName()) {
                    case ID:
                        visit.setId(in.nextInt());
                        visit.isValidUpdate = false;
                        break;
                    case USER_ID:
                        visit.setUser(UserService.users.get(in.nextInt()));
                        if (visit.user == null) visit.isValidUpdate = false;
                        break;
                    case LOCATION_ID:
                        visit.setLocation(LocationService.locations.get(in.nextInt()));
                        if (visit.location == null) visit.isValidUpdate = false;
                        break;
                    case VISITED_AT:
                        visit.setVisitedAt(in.nextLong());
                        if (visit.visitedAt == Long.MIN_VALUE) visit.isValidUpdate = false;
                        break;
                    case MARK:
                        visit.setMark(in.nextInt());
                        if (visit.mark < 0 || visit.mark > 5) visit.isValidUpdate = false;
                }
            }
            in.endObject();
            return visit;
        }
    }
}
