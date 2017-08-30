package org.iofstorm.konatravels;

import org.testng.annotations.DataProvider;
import org.testng.annotations.Test;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.is;
import static org.testng.Assert.assertEquals;
import static org.testng.Assert.assertNull;
import static org.testng.Assert.assertTrue;

public class UtilsTest {

    @Test(dataProvider = "mapResourceDataProvider")
    public void testMapResource(String path, Integer expected) throws Exception {
        assertEquals(Utils.extractId(path), expected);
    }

    @Test(dataProvider = "parseQueryParamsDataProvider")
    public void testParseQueryParams(String query, Map<String, String> expected) throws Exception {
        Map<String, String> actual = Utils.parseQueryParams(query);
        if (expected == null) {
            assertNull(actual);
        } else if (expected.isEmpty()) {
            assertTrue(actual != null && actual.isEmpty());
        } else {
            assertThat(actual, is(equalTo(expected)));
        }
    }

    @DataProvider(name = "mapResourceDataProvider")
    public static Object[][] mapResourceDataProvider() {
        return new Object[][]{
                {"/users/123", 123},
                {"/users/123/visits", 123},
                {"/users/bad", -1},
                {"/users", null},
                {"/users/123/visits/1", null},
                {"/locations/123", 123},
                {"/locations/123/avg", 123},
                {"/visits/123", 123},
        };
    }

    @DataProvider(name = "parseQueryParamsDataProvider")
    public static Object[][] parseQueryParamsDataProvider() {
        return new Object[][]{
                {"fromAge=12", map("fromAge", "12")},
                {"fromAge=12&toAge=23", map("fromAge", "12", "toAge", "23")},

                {"fromAge=12&toAge=23&fromDate=123&toDate=456", map("fromAge","12", "toAge", "23", "fromDate", "123", "toDate","456")},
                {"", null},
                {"=", null},
                {"abc=", null},
                {"=abc", null},
                {"abc=123abb=122", null},
                {"a=1&b=2&", null}
        };
    }

    private static Map<String, String> map(String... args) {
        Map<String, String> map = new HashMap<>();
        for (int i = 0; i < args.length - 1; i += 2) {
            map.put(args[i], args[i + 1]);
        }
        return map;
    }
}
