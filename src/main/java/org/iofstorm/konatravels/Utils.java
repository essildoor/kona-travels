package org.iofstorm.konatravels;

import org.apache.commons.lang3.StringUtils;
import org.iofstorm.konatravels.model.Visit;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.ZoneOffset;
import java.time.temporal.ChronoUnit;
import java.util.Collections;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.Map;
import java.util.Queue;
import java.util.TreeSet;
import java.util.concurrent.ConcurrentLinkedQueue;

public class Utils {
    private static final String PATH_DELIMITER = "/";
    private static final String QUERY_DELIMITER = "&";
    private static final String PARAM_DELIMITER = "=";

    public static final int SC_OK = 200;
    public static final int SC_NOT_FOUND = 404;
    public static final int SC_BAD_REQUEST = 400;

    public static final String MALE = "m";
    public static final String FEMALE = "f";

    private static final Queue<TreeSet<Visit>> treeSetPool = new ConcurrentLinkedQueue<>();

    static {
        for(int i = 0; i < 1200_000; i++) {
            treeSetPool.add(new TreeSet<>());
        }
    }

    public static int calcAge(long birthDate) {
        LocalDate bd = LocalDateTime.ofEpochSecond(birthDate, 0, ZoneOffset.UTC).toLocalDate();
        return (int)ChronoUnit.YEARS.between(bd, DataLoader.NOW_TS);
    }

    public static TreeSet<Visit> newTreeSet() {
        TreeSet<Visit> res = treeSetPool.poll();
        return res != null ? res : new TreeSet<>();
    }

    public static Integer extractId(String path) {
        if (path.charAt(0) != '/') return null;
        String[] elements = path.split(PATH_DELIMITER);
        if (elements.length == 2 || elements.length > 4) return null; // bad path
        if (!StringUtils.isNumeric(elements[2])) return -1; // bad id 404
        return Integer.valueOf(elements[2]);
    }

    public static Map<String, String> parseQueryParams(String queryString) {
        if (queryString.endsWith(QUERY_DELIMITER)) return null;
        String[] pairs = queryString.split(QUERY_DELIMITER);
        Map<String, String> params = new HashMap<>(pairs.length);
        for (String pair : pairs) {
            String[] pairr = pair.split(PARAM_DELIMITER);
            if (pairr.length != 2 || pairr[0].isEmpty() || pairr[1].isEmpty()) return null;
            params.put(pairr[0], pairr[1]);
        }
        return params;
    }
}
