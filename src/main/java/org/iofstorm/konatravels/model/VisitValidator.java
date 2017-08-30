package org.iofstorm.konatravels.model;

import static org.iofstorm.konatravels.model.Location.VISITED_AT_MAX;
import static org.iofstorm.konatravels.model.Location.VISITED_AT_MIN;

public class VisitValidator {

    public static boolean validateOnCreate(Visit visit) {
        if (visit == null) return false;
        if (visit.getId() == null) return false;
        if (visit.getLocationId() == null) return false;
        if (visit.getUserId() == null) return false;
        if (visit.getVisitedAt() < VISITED_AT_MIN || visit.getVisitedAt() > VISITED_AT_MAX) return false;
        if (visit.getMark() < 0 || visit.getMark() > 5) return false;
        return true;
    }

    public static boolean validateOnUpdate(Visit visit) {
        return visit != null && visit.isValidUpdate();
    }
}
