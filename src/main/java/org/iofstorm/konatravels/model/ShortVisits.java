package org.iofstorm.konatravels.model;

import java.util.Collection;
import java.util.List;

public class ShortVisits {

    private Collection<ShortVisit> visits;

    public ShortVisits() {
    }

    public ShortVisits(Collection<ShortVisit> visits) {
        this.visits = visits;
    }

    public Collection<ShortVisit> getVisits() {
        return visits;
    }

    public void setVisits(List<ShortVisit> visits) {
        this.visits = visits;
    }
}
