package org.iofstorm.konatravels;

import com.google.gson.Gson;
import com.google.gson.stream.JsonReader;
import org.iofstorm.konatravels.model.Locations;
import org.iofstorm.konatravels.model.Users;
import org.iofstorm.konatravels.model.Visits;
import org.iofstorm.konatravels.service.LocationService;
import org.iofstorm.konatravels.service.UserService;
import org.iofstorm.konatravels.service.VisitService;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.Objects;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;

public class DataLoader {

    public static LocalDateTime NOW_TS;

    private final Gson gson;
    private final String zipFilePath;
    private final UserService userService;
    private final LocationService locationService;
    private final VisitService visitService;

    public DataLoader(Context context, String zipFilePath) {
        this.gson = context.getGson();
        this.userService = context.getUserService();
        this.locationService = context.getLocationService();
        this.visitService = context.getVisitService();
        this.zipFilePath = Objects.requireNonNull(zipFilePath);
    }

    public void loadData() throws IOException, InterruptedException {
        NOW_TS = LocalDateTime.ofInstant(Instant.ofEpochSecond(1503695452L), ZoneId.systemDefault());

        if (!new File(zipFilePath).exists()) {
            System.err.println(String.format("data file %s not found", zipFilePath));
            return;
        }

        System.out.println("loading users and locations...");

        long startTs = System.currentTimeMillis();

        int usersCount = 0;
        int locationsCount = 0;

        try (ZipInputStream zipIn = new ZipInputStream(new FileInputStream(zipFilePath))) {
            ZipEntry entry = zipIn.getNextEntry();
            // iterates over entries in the zip file
            while (entry != null) {
                if (entry.getName().startsWith("users_")) {
                    JsonReader jsonReader = new JsonReader(new InputStreamReader(zipIn));
                    Users users = gson.fromJson(jsonReader, Users.class);
                    userService.load(users.getUsers());
                    usersCount += users.getUsers().size();
                } else if (entry.getName().startsWith("locations_")) {
                    JsonReader jsonReader = new JsonReader(new InputStreamReader(zipIn));
                    Locations locations = gson.fromJson(jsonReader, Locations.class);
                    locationService.load(locations.getLocations());
                    locationsCount += locations.getLocations().size();
                }
                zipIn.closeEntry();
                entry = zipIn.getNextEntry();
            }
        }

        System.out.println(String.format("%d users and %d locations were loaded in %.3f sec", usersCount, locationsCount, (System.currentTimeMillis() - startTs) / 1000f));

        System.gc();

        System.out.println("loading visits...");

        startTs = System.currentTimeMillis();

        int visitsCount = 0;

        try (ZipInputStream zipIn = new ZipInputStream(new FileInputStream(zipFilePath))) {
            ZipEntry entry = zipIn.getNextEntry();
            // iterates over entries in the zip file
            while (entry != null) {
                if (entry.getName().startsWith("visits_")) {
                    JsonReader jsonReader = new JsonReader(new InputStreamReader(zipIn));
                    Visits visits = gson.fromJson(jsonReader, Visits.class);
                    visitService.load(visits);
                    visitsCount += visits.getVisits().size();
                }
                zipIn.closeEntry();
                entry = zipIn.getNextEntry();
            }
        }

        System.out.println(String.format("%d visits were loaded in %.3f sec", visitsCount, (System.currentTimeMillis() - startTs) / 1000f));

        System.gc();
    }
}
