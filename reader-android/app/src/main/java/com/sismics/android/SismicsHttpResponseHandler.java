package com.sismics.android;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import com.loopj.android.http.JsonHttpResponseHandler;

/**
 * Surcharge de JsonHttpResponseHandler pour intégrer les logs.
 * 
 * @author bgamard
 */
public class SismicsHttpResponseHandler extends JsonHttpResponseHandler {

    @Override
    protected void handleFailureMessage(Throwable e, String responseBody) {
        try {
            if (responseBody != null) {
                Object jsonResponse = parseResponse(responseBody);
                if (jsonResponse instanceof JSONObject) {
                    onFailure(e, (JSONObject) jsonResponse);
                } else if (jsonResponse instanceof JSONArray) {
                    onFailure(e, (JSONArray) jsonResponse);
                }
                
                // Quoi qu'il arrive on appelle ce callback
                onFailure(e, responseBody);
            } else {
                onFailure(e, "");
            }
        } catch (JSONException ex) {
            onFailure(e, responseBody);
        }
        
        Log.e("HttpError", "responseBody=" + responseBody, e);
    }
}
