package org.iofstorm.konatravels.model;

import org.apache.commons.lang3.StringUtils;

import java.util.Map;

import static org.iofstorm.konatravels.model.Location.CITY_LENGTH;
import static org.iofstorm.konatravels.model.Location.COUNTRY_LENGTH;

public class LocationValidator {
    public static final String FROM_DATE = "fromDate";
    public static final String TO_DATE = "toDate";
    public static final String FROM_AGE = "fromAge";
    public static final String TO_AGE = "toAge";
    public static final String GENDER = "gender";


    public static boolean validateGetAvgMarkParams(Map<String, String> params) {
        if (params.containsKey(FROM_DATE) && !StringUtils.isNumeric(params.get(FROM_DATE))) return false;
        if (params.containsKey(TO_DATE) && !StringUtils.isNumeric(params.get(TO_DATE))) return false;
        if (params.containsKey(FROM_AGE) && !StringUtils.isNumeric(params.get(FROM_AGE))) return false;
        if (params.containsKey(TO_AGE) && !StringUtils.isNumeric(params.get(TO_AGE))) return false;
        if (params.containsKey(GENDER) && Gender.UNKNOWN == Gender.fromString(params.get(GENDER))) return false;
        return true;
    }

    public static boolean validateOnCreate(Location location) {
        if (location == null) return false;
        if (location.getId() == null) return false;
        if (location.getPlace() == null) return false;
        if (location.getCountry() == null || location.getCountry().length() > COUNTRY_LENGTH) return false;
        if (location.getCity() == null || location.getCity().length() > CITY_LENGTH) return false;
        if (location.getDistance() == Integer.MIN_VALUE) return false;
        return true;
    }

    public static boolean validateOnUpdate(Location location) {
        return location != null && location.isValidUpdate();
    }
}
