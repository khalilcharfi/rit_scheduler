package me.jlurena.ritscheduler.networking;

import com.fasterxml.jackson.databind.ObjectMapper;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import me.jlurena.ritscheduler.models.Course;

/**
 * Serializer for Course.
 */
class Serializers {

    /**
     * URL to query a GET request for Course lookup with TigerCenter API.
     */
    static final String COURSE_QUERY_URL = "https://tigercenter.rit.edu/tigerCenterSearch/api/search?map=";
    static final String AUTO_COMPLETE_URL = "https://tigercenter.rit.edu/tigerCenterSearch/api/auto?terms=";
    /**
     * Key value of JSON Array containing the query results coming from TigerCenter API.
     */
    private static final String SEARCH_RESULTS_KEY = "searchResults";
    private static final int MAX_RESULTS = 10;

    /**
     * Builds the required parameter in JSON format for a TigerCenter query of a Course.
     *
     * @param query Query string search term.
     * @param term Term represented as numerical coded String.
     * @return JSONObject representing the parameter required for Get request.
     */
    static JSONObject buildCourseQueryParameter(String query, String term) {
        final String career = "";
        JSONObject json = new JSONObject();
        try {
            json.put("query", query);
            json.put("rows", MAX_RESULTS);
            json.put("term", term);
            json.put("career", career);
        } catch (JSONException e) {
            //            Log.e("Serializers", "Error creating JSON", e);
        }
        return json;
    }

    static List<String> toAutoCompleteList(JSONObject json) throws JSONException {
        JSONArray jsonArray = json.getJSONArray("autoResults");
        ArrayList<String> list = new ArrayList<>();
        int length = Math.min(MAX_RESULTS, jsonArray.length());
        for (int i = 0; i < length; i++) {
            // Only get top 10
            // For some reason the response has an extra space which causes issues.
            list.add(jsonArray.getString(i).trim().replaceAll(" +", " "));
        }
        return list;
    }

    /**
     * Convert JSON object received from TigerCenter API into a List of Courses.
     *
     * @param json A JSON object representative of the JSON received from TigerCenter API.
     * @return A list of Courses.
     * @throws IOException Read error.
     * @throws JSONException Invalid JSON or invalid JSONObject method calls.
     */
    static List<Course> toCourseResults(JSONObject json) throws IOException, JSONException {
        ArrayList<Course> courses = new ArrayList<>();
        ObjectMapper objectMapper = new ObjectMapper();
        JSONArray coursesJsonResults = json.getJSONArray(SEARCH_RESULTS_KEY);
        int length = Math.min(MAX_RESULTS, coursesJsonResults.length());

        // Only get top 10 results
        for (int i = 0; i < length; i++) {
            courses.add(objectMapper.readValue(coursesJsonResults.get(i).toString(), Course.class));
        }
        return courses;
    }
}
