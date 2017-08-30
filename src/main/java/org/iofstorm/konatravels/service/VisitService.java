package org.iofstorm.konatravels.service;

import org.iofstorm.konatravels.Utils;
import org.iofstorm.konatravels.model.Location;
import org.iofstorm.konatravels.model.ShortVisit;
import org.iofstorm.konatravels.model.ShortVisits;
import org.iofstorm.konatravels.model.User;
import org.iofstorm.konatravels.model.Visit;
import org.iofstorm.konatravels.model.Visits;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeSet;
import java.util.concurrent.locks.ReadWriteLock;
import java.util.concurrent.locks.ReentrantReadWriteLock;

import static org.iofstorm.konatravels.Utils.SC_BAD_REQUEST;
import static org.iofstorm.konatravels.Utils.SC_OK;

public class VisitService {
    private static final ShortVisits EMPTY_SHORT_VISITS = new ShortVisits(Collections.emptyList());
    private static final List<Visit> EMPTY_VISITS = Collections.emptyList();

    private final Map<Integer, Visit> visits;
    private final Map<Integer, Set<Visit>> visitsByUser;
    private final Map<Integer, List<Visit>> visitsByLocation;
    private final ReadWriteLock lock;

    private UserService userService;
    private LocationService locationService;

    public VisitService() {
        visits = new HashMap<>(10_041_000, 1f);
        visitsByUser = new HashMap<>(1_041_000, 1f);
        visitsByLocation = new HashMap<>(810_000, 1f);
        lock = new ReentrantReadWriteLock(true);
    }

    public void setUserService(UserService userService) {
        this.userService = userService;
    }

    public void setLocationService(LocationService locationService) {
        this.locationService = locationService;
    }

    public boolean visitExist(Integer id) {
        lock.readLock().lock();
        try {
            return visits.containsKey(id);
        } finally {
            lock.readLock().unlock();
        }
    }

    public Visit getVisitWithoutLock(Integer id) {
        return visits.get(id);
    }

    public int createVisit(Visit visit) {
        lock.readLock().lock();
        try {
            if (visits.containsKey(visit.getId())) return SC_BAD_REQUEST; // visit already exist
            lock.readLock().unlock();
            lock.writeLock().lock();
            try {
                if (visits.containsKey(visit.getId())) return SC_BAD_REQUEST; // visit already exist

                User user = userService.getUserWithoutLock(visit.getUserId());
                Location location = locationService.getLocationWithoutLock(visit.getLocationId());
                if (user != null && location != null) {

                    visit.setUser(user);
                    visit.setLocation(location);

                    saveVisit(visit);
                    return SC_OK;
                } else {
                    return SC_BAD_REQUEST; // either user or location linked for this visit doesn't exist
                }
            } finally {
                lock.readLock().lock();
                lock.writeLock().unlock();
            }
        } finally {
            lock.readLock().unlock();
        }
    }

    @SuppressWarnings("Java8MapApi")
    public void updateVisit(Integer visitId, Visit newVisit) {
        lock.writeLock().lock();
        try {
            boolean userHasChanged = newVisit.user != null;
            boolean visitedAtHasChanged = newVisit.getVisitedAt() != Long.MIN_VALUE;
            boolean locationHasChanged = newVisit.location != null;

            Visit visit = visits.get(visitId);

            if (userHasChanged || visitedAtHasChanged) visitsByUser.get(visit.getUserId()).remove(visit);
            if (locationHasChanged) visitsByLocation.get(visit.getLocationId()).remove(visit);

            remapVisit(visit, newVisit);

            if (userHasChanged || visitedAtHasChanged) {
                Set<Visit> userVisits = visitsByUser.get(visit.getUserId());
                if (userVisits == null) {
                    userVisits = Utils.newTreeSet();
                    userVisits.add(visit);
                    visitsByUser.put(visit.getUserId(), userVisits);
                } else {
                    userVisits.add(visit);
                }
            }

            if (locationHasChanged) {
                List<Visit> locationVisits = visitsByLocation.get(visit.getLocationId());
                if (locationVisits == null) {
                    locationVisits = new ArrayList<>();
                    locationVisits.add(visit);
                    visitsByLocation.put(visit.getLocationId(), locationVisits);
                } else if (!locationVisits.contains(visit)) {
                    locationVisits.add(visit);
                }
            }
        } finally {
            lock.writeLock().unlock();
        }
    }

    public ShortVisits getUserVisits(Integer userId, long fromDate, long toDate, String country, int toDistance) {
        if (UserService.users.get(userId) == null) return null; // user not found
        Set<Visit> userVisits = visitsByUser.get(userId);
        if (userVisits == null) return EMPTY_SHORT_VISITS; // user has no visits, return empty visits list, ok response

        List<ShortVisit> result = new ArrayList<>(userVisits.size());

        boolean fromDateIsPresent = fromDate != Long.MIN_VALUE;
        boolean toDateIsPresent = toDate != Long.MIN_VALUE;
        boolean countryIsPresent = country != null;
        boolean toDistanceIsPresent = toDistance != Integer.MIN_VALUE;

        for (Visit v : userVisits) {
            if (fromDateIsPresent && v.getVisitedAt() <= fromDate) continue;
            if (toDateIsPresent && v.getVisitedAt() >= toDate) continue;
            if (countryIsPresent && !country.equals(v.getLocationCountry())) continue;
            if (toDistanceIsPresent && v.getLocationDistance() >= toDistance) continue;
            result.add(new ShortVisit(v.getMark(), v.getVisitedAt(), v.getLocationPlace()));
        }
        return new ShortVisits(result);
    }

    public ShortVisits getUserVisits(Integer userId) {
        if (UserService.users.get(userId) == null) return null; // user not found
        Set<Visit> userVisits = visitsByUser.get(userId);
        if (userVisits == null) return EMPTY_SHORT_VISITS; // user has no visits, return empty visits list, ok response

        List<ShortVisit> result = new ArrayList<>(userVisits.size());

        for (Visit v : userVisits) {
            result.add(new ShortVisit(v.getMark(), v.getVisitedAt(), v.getLocationPlace()));
        }
        return new ShortVisits(result);
    }

    // used for data loading
    public void load(Visits visits) {
        for (Visit visit : visits.getVisits()) {
            saveVisit(visit);
        }
    }

    List<Visit> getVisitsByLocationId(Integer locationId) {
        List<Visit> locationVisits = visitsByLocation.get(locationId);
        if (locationVisits == null) return EMPTY_VISITS; // no visits for this location
        return locationVisits;
    }

    @SuppressWarnings("Java8MapApi")
    private void saveVisit(Visit visit) {
        visits.put(visit.getId(), visit);

        Set<Visit> userVisits = visitsByUser.get(visit.getUserId());
        if (userVisits == null) {
            userVisits = new TreeSet<>();
            visitsByUser.put(visit.getUserId(), userVisits);
        }
        userVisits.add(visit);

        List<Visit> locVisits = visitsByLocation.get(visit.getLocationId());
        if (locVisits == null) {
            locVisits = new ArrayList<>();
            visitsByLocation.put(visit.getLocationId(), locVisits);
            locVisits.add(visit);
        } else if (!locVisits.contains(visit)) {
            locVisits.add(visit);
        }
    }

    private void remapVisit(Visit oldVisit, Visit newVisit) {
        if (newVisit.getLocationId() != null) oldVisit.setLocation(newVisit.location);
        if (newVisit.getUserId() != null) oldVisit.setUser(newVisit.user);
        if (newVisit.getVisitedAt() != Long.MIN_VALUE) oldVisit.setVisitedAt(newVisit.getVisitedAt());
        if (newVisit.getMark() != Integer.MIN_VALUE) oldVisit.setMark(newVisit.getMark());
    }
}
