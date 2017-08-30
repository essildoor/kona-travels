package org.iofstorm.konatravels.model;

import org.iofstorm.konatravels.Utils;

public enum Gender {
    MALE(Utils.MALE),
    FEMALE(Utils.FEMALE),
    UNKNOWN(null);

    private String val;

    Gender(String val) {
        this.val = val;
    }

    public String getVal() {
        return val;
    }

    public static Gender fromString(String gender) {
        if (gender == null) return UNKNOWN;
        switch (gender) {
            case Utils.MALE:
                return MALE;
            case Utils.FEMALE:
                return FEMALE;
            default:
                return UNKNOWN;
        }
    }
}
