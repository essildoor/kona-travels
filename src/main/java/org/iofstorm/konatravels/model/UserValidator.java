package org.iofstorm.konatravels.model;

import org.apache.commons.lang3.StringUtils;
import org.iofstorm.konatravels.Utils;

import java.util.Map;

import static org.iofstorm.konatravels.model.User.BIRTH_DATE_MAX;
import static org.iofstorm.konatravels.model.User.BIRTH_DATE_MIN;
import static org.iofstorm.konatravels.model.User.EMAIL_LENGTH;
import static org.iofstorm.konatravels.model.User.NAME_LENGTH;

public class UserValidator {

    public static final String FROM_DATE = "fromDate";
    public static final String TO_DATE = "toDate";
    public static final String COUNTRY = "country";
    public static final String TO_DISTANCE = "toDistance";

    public static boolean validateGetUserVisitsQueryParams(Map<String, String> params) {
        if (params.containsKey(FROM_DATE) && !StringUtils.isNumeric(params.get(FROM_DATE))) return false;
        if (params.containsKey(TO_DATE) && !StringUtils.isNumeric(params.get(TO_DATE))) return false;
        if (params.containsKey(TO_DISTANCE) && !StringUtils.isNumeric(params.get(TO_DISTANCE))) return false;
        return true;
    }

    public static boolean validateUserOnCreate(User user) {
        if (user == null) return false;
        if (user.getId() == null) return false;
        if (user.getEmail() == null || user.getEmail().length() > EMAIL_LENGTH) return false;
        if (user.getFirstName() == null || user.getFirstName().length() > NAME_LENGTH) return false;
        if (user.getLastName() == null || user.getLastName().length() > NAME_LENGTH) return false;
        if (user.getBirthDate() == Long.MIN_VALUE || (user.getBirthDate() < BIRTH_DATE_MIN || user.getBirthDate() > BIRTH_DATE_MAX)) {
            return false;
        } else {
            user.setAge(Utils.calcAge(user.getBirthDate()));
        }
        if (user.getGender() == Gender.UNKNOWN) return false;
        return true;
    }

    public static boolean validateUserOnUpdate(User user) {
        return user != null && user.isValidUpdate();
    }
}
