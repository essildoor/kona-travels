package org.iofstorm.konatravels.service;


import org.iofstorm.konatravels.model.Gender;
import org.iofstorm.konatravels.model.Location;
import org.iofstorm.konatravels.model.Mark;
import org.iofstorm.konatravels.model.Visit;

import java.math.BigDecimal;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.locks.ReadWriteLock;
import java.util.concurrent.locks.ReentrantReadWriteLock;

import static org.iofstorm.konatravels.Utils.SC_BAD_REQUEST;
import static org.iofstorm.konatravels.Utils.SC_NOT_FOUND;
import static org.iofstorm.konatravels.Utils.SC_OK;

public class LocationService {
    public static final Map<Integer, Location> locations = new HashMap<>(810_000, 1f);

    private final ReadWriteLock lock;
    private VisitService visitService;

    public LocationService() {
        lock = new ReentrantReadWriteLock(true);
    }

    public void setVisitService(VisitService visitService) {
        this.visitService = visitService;
    }

    public Location getLocationWithoutLock(Integer id) {
        return locations.get(id);
    }

    public int createLocation(Location location) {
        lock.readLock().lock();
        try {
            if (locations.containsKey(location.getId())) return SC_BAD_REQUEST;
            lock.readLock().unlock();
            lock.writeLock().lock();
            try {
                if (locations.containsKey(location.getId())) {
                    return SC_BAD_REQUEST;
                } else {
                    locations.put(location.getId(), location);
                    return SC_OK;
                }
            } finally {
                lock.readLock().lock();
                lock.writeLock().unlock();
            }
        } finally {
            lock.readLock().unlock();
        }
    }

    public int updateLocation(Integer locationId, Location newLocation) {
        lock.readLock().lock();
        try {
            if (!locations.containsKey(locationId)) {
                return SC_NOT_FOUND;
            } else if (newLocation == null) {
                return SC_BAD_REQUEST;
            } else {
                lock.readLock().unlock();
                lock.writeLock().lock();
                try {
                    remapLocation(locations.get(locationId), newLocation);
                    return SC_OK;
                } finally {
                    lock.readLock().lock();
                    lock.writeLock().unlock();

                }
            }
        } finally {
            lock.readLock().unlock();
        }
    }

    public Mark getAverageMark(Integer locationId, long fromDate, long toDate, int fromAge, int toAge, Gender gender) {
        if (!locations.containsKey(locationId)) return null;
        List<Visit> visitsByLocation = visitService.getVisitsByLocationId(locationId);
        int i = 0;
        int acc = 0;
        boolean fromDateIsPresent = fromDate != Long.MIN_VALUE;
        boolean toDateIsPresent = toDate != Long.MIN_VALUE;
        boolean genderIsPresent = gender != Gender.UNKNOWN;
        boolean fromAgeIsPresent = fromAge != Integer.MIN_VALUE;
        boolean toAgeIsPresent = toAge != Integer.MIN_VALUE;
        for (Visit v : visitsByLocation) {
            if (fromDateIsPresent && v.getVisitedAt() <= fromDate) continue;
            if (toDateIsPresent && v.getVisitedAt() >= toDate) continue;
            if (genderIsPresent && gender != v.getUserGender()) continue;
            if (fromAgeIsPresent && v.getUserAge() < fromAge) continue;
            if (toAgeIsPresent && v.getUserAge() >= toAge) continue;

            acc += v.getMark();
            i++;
        }
        BigDecimal avg;
        if (acc == 0) avg = BigDecimal.ZERO;
        else avg = BigDecimal.valueOf(acc).divide(BigDecimal.valueOf(i), 5, BigDecimal.ROUND_HALF_UP);
        return new Mark(avg);
    }

    public Mark getAverageMark(Integer locationId) {
        if (!locations.containsKey(locationId)) return null;
        List<Visit> visitsByLocation = visitService.getVisitsByLocationId(locationId);
        int i = 0;
        int acc = 0;
        for (Visit v : visitsByLocation) {
            acc += v.getMark();
            i++;
        }
        BigDecimal avg;
        if (acc == 0) avg = BigDecimal.ZERO;
        else avg = BigDecimal.valueOf(acc).divide(BigDecimal.valueOf(i), 5, BigDecimal.ROUND_HALF_UP);
        return new Mark(avg);
    }

    // used for data loading
    public void load(List<Location> locationList) {
        lock.writeLock().lock();
        try {
            for (Location location : locationList) {
                locations.put(location.getId(), location);
            }
        } finally {
            lock.writeLock().unlock();
        }
    }

    private Location remapLocation(Location oldLoc, Location newLoc) {
        if (newLoc.getPlace() != null) oldLoc.setPlace(newLoc.getPlace());
        if (newLoc.getCity() != null) oldLoc.setCity(newLoc.getCity());
        if (newLoc.getCountry() != null) oldLoc.setCountry(newLoc.getCountry());
        if (newLoc.getDistance() != Integer.MIN_VALUE) oldLoc.setDistance(newLoc.getDistance());
        return oldLoc;
    }
}
