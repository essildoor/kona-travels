package org.iofstorm.konatravels.model;


import com.google.gson.TypeAdapter;
import com.google.gson.stream.JsonReader;
import com.google.gson.stream.JsonWriter;

import java.io.IOException;

import static org.iofstorm.konatravels.model.Location.PLACE;
import static org.iofstorm.konatravels.model.Visit.MARK;
import static org.iofstorm.konatravels.model.Visit.VISITED_AT;

public class ShortVisit {

    private final int mark;
    private final long visitedAt;
    private final String place;

    public ShortVisit(int mark, long visitedAt, String place) {
        this.mark = mark;
        this.visitedAt = visitedAt;
        this.place = place;
    }

    public int getMark() {
        return mark;
    }

    public long getVisitedAt() {
        return visitedAt;
    }

    public String getPlace() {
        return place;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        ShortVisit that = (ShortVisit) o;

        if (visitedAt != that.visitedAt) return false;
        return place != null ? place.equals(that.place) : that.place == null;
    }

    @Override
    public int hashCode() {
        int result = (int) (visitedAt ^ (visitedAt >>> 32));
        result = 31 * result + (place != null ? place.hashCode() : 0);
        return result;
    }

    public static class ShortVisitAdapter extends TypeAdapter<ShortVisit> {

        @Override
        public void write(JsonWriter out, ShortVisit value) throws IOException {
            out.beginObject();
            out.name(MARK).value(value.getMark());
            out.name(VISITED_AT).value(value.getVisitedAt());
            out.name(PLACE).value(value.getPlace());
            out.endObject();
        }

        @Override
        public ShortVisit read(JsonReader in) throws IOException {
            return null; // not used
        }
    }
}
