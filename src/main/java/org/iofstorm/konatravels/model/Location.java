package org.iofstorm.konatravels.model;

import com.google.gson.TypeAdapter;
import com.google.gson.stream.JsonReader;
import com.google.gson.stream.JsonWriter;

import java.io.IOException;
import java.time.LocalDate;
import java.time.ZoneId;

public class Location {
    static final String ID = "id";
    static final String PLACE = "place";
    static final String COUNTRY = "country";
    static final String CITY = "city";
    static final String DISTANCE = "distance";

    static final Integer COUNTRY_LENGTH = 50;
    static final Integer CITY_LENGTH = 50;
    static final Long VISITED_AT_MIN = LocalDate.of(2000, 1, 1).atStartOfDay().atZone(ZoneId.systemDefault()).toEpochSecond();
    static final Long VISITED_AT_MAX = LocalDate.of(2015, 1, 1).atStartOfDay().atZone(ZoneId.systemDefault()).toEpochSecond();

    // 32 bit int unique
    Integer id;

    // unbounded string
    String place;

    // unicode string 0-50
    String country;

    // unicode string 0-50
    private String city;

    // 32 bit int
    int distance = Integer.MIN_VALUE;

    private boolean isValidUpdate = true;

    public Integer getId() {
        return id;
    }

    public void setId(Integer id) {
        this.id = id;
    }

    public String getPlace() {
        return place;
    }

    public void setPlace(String place) {
        this.place = place;
    }

    public String getCountry() {
        return country;
    }

    public void setCountry(String country) {
        this.country = country;
    }

    public String getCity() {
        return city;
    }

    public void setCity(String city) {
        this.city = city;
    }

    public int getDistance() {
        return distance;
    }

    public void setDistance(int distance) {
        this.distance = distance;
    }

    public boolean isValidUpdate() {
        return isValidUpdate;
    }

    public static class LocationAdapter extends TypeAdapter<Location> {

        @Override
        public void write(JsonWriter out, Location value) throws IOException {
            out.beginObject();
            out.name(ID).value(value.id);
            out.name(PLACE).value(value.place);
            out.name(COUNTRY).value(value.country);
            out.name(CITY).value(value.city);
            out.name(DISTANCE).value(value.distance);
            out.endObject();
        }

        @Override
        public Location read(JsonReader in) throws IOException {
            Location location = new Location();
            in.beginObject();
            while (in.hasNext()) {
                switch (in.nextName()) {
                    case ID:
                        location.setId(in.nextInt());
                        location.isValidUpdate = false;
                        break;
                    case PLACE:
                        location.setPlace(in.nextString());
                        if (location.place == null) location.isValidUpdate = false;
                        break;
                    case COUNTRY:
                        location.setCountry(in.nextString());
                        if (location.country == null || location.country.length() > Location.COUNTRY_LENGTH) location.isValidUpdate = false;
                        break;
                    case CITY:
                        location.setCity(in.nextString());
                        if (location.city == null || location.city.length() > Location.CITY_LENGTH) location.isValidUpdate = false;
                        break;
                    case DISTANCE:
                        location.setDistance(in.nextInt());
                        if (location.distance == Integer.MIN_VALUE) location.isValidUpdate = false;
                }
            }
            in.endObject();
            return location;
        }
    }
}
