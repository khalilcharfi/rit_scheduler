package me.jlurena.ritscheduler.networking;

import android.content.Context;
import android.util.Log;

import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonObjectRequest;
import com.android.volley.toolbox.Volley;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.util.List;

import me.jlurena.ritscheduler.models.Course;

/**
 * Manages network tasks including API calls.
 */
public class NetworkManager {

    private static final String TAG = "NetworkManager";
    private static NetworkManager instance = null;
    private final RequestQueue requestQueue;

    /**
     * Instantiate a NetworkManager with the application's context.
     *
     * @param context Application's context.
     */
    private NetworkManager(Context context) {
        requestQueue = Volley.newRequestQueue(context.getApplicationContext());
    }

    /**
     * Get instance of NetworkManager.
     *
     * @param context Application's context.
     * @return The instance of NetworkManager.
     */
    public static synchronized NetworkManager getInstance(Context context) {
        if (instance == null)
            instance = new NetworkManager(context);
        return instance;
    }

    /**
     * Get the existing instance of NetworkManager.
     *
     * @return The NetworkManager instance.
     * @throws IllegalStateException When a NetworkManager instance has not yet been instantiated.
     */
    public static synchronized NetworkManager getInstance() {
        if (null == instance) {
            throw new IllegalStateException(NetworkManager.class.getSimpleName() + " is not initialized");
        }
        return instance;
    }

    /**
     * Getter of the RequestQueue.
     *
     * @return The RequestQueue.
     */
    public RequestQueue getRequestQueue() {
        return requestQueue;
    }

    /**
     * Sends GET request query call to TigerCenter API to search for a Course.
     *
     * @param query Query term.
     * @param term Term as a coded numerical String.
     * @param responseListener ResponseListener callback after response from API is received.
     */
    public void queryCourses(String query, String term, final ResponseListener<List<Course>> responseListener) {

        String url = buildUrl(CourseSerializer.QUERY_URL, CourseSerializer.buildCourseQueryParameter(query, term));

        JsonObjectRequest request = new JsonObjectRequest(Request.Method.GET, url, null, new Response.Listener<JSONObject>() {
            @Override
            public void onResponse(JSONObject response) {
                try {
                    responseListener.getResult(CourseSerializer.toCourseResults(response), 200, null);
                } catch (JSONException | IOException e) {
                    Log.e(TAG, "Error parsing response", e);
                }
            }
        }, new Response.ErrorListener() {

            @Override
            public void onErrorResponse(VolleyError error) {
                Log.d(TAG, error.getMessage());
                responseListener.getResult(null, error.networkResponse.statusCode, error);
            }
        });
        requestQueue.add(request);
    }

    /**
     * Builds and encodes a URL.
     *
     * @param queryUrl URL path.
     * @param parameters Parameters of URL.
     * @return An encoded URL.
     */
    private String buildUrl(String queryUrl, JSONObject parameters) {
        String url = null;
        try {
            url = queryUrl + URLEncoder.encode(parameters.toString(), "utf-8");
        } catch (UnsupportedEncodingException e) {
            Log.e(TAG, "Error building URL", e);
        }

        return url;
    }
}