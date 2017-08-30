package org.iofstorm.konatravels;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import org.iofstorm.konatravels.model.Location;
import org.iofstorm.konatravels.model.Mark;
import org.iofstorm.konatravels.model.ShortVisit;
import org.iofstorm.konatravels.model.User;
import org.iofstorm.konatravels.model.Visit;
import org.iofstorm.konatravels.service.LocationService;
import org.iofstorm.konatravels.service.UserService;
import org.iofstorm.konatravels.service.VisitService;

public class Context {
    private final UserService userService;
    private final LocationService locationService;
    private final VisitService visitService;

    private final Gson gson;

    public Context() {
        userService = new UserService();
        locationService = new LocationService();
        visitService = new VisitService();

        userService.setVisitService(visitService);

        locationService.setVisitService(visitService);

        visitService.setUserService(userService);
        visitService.setLocationService(locationService);

        gson = new GsonBuilder()
                .disableHtmlEscaping()
                .disableInnerClassSerialization()
                .registerTypeAdapter(Location.class, new Location.LocationAdapter())
                .registerTypeAdapter(User.class, new User.UserAdapter())
                .registerTypeAdapter(Visit.class, new Visit.VisitAdapter())
                .registerTypeAdapter(ShortVisit.class, new ShortVisit.ShortVisitAdapter())
                .registerTypeAdapter(Mark.class, new Mark.MarkAdapter())
                .create();
    }

    public UserService getUserService() {
        return userService;
    }

    public LocationService getLocationService() {
        return locationService;
    }

    public VisitService getVisitService() {
        return visitService;
    }

    public Gson getGson() {
        return gson;
    }
}
