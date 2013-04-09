package com.sismics.reader.rest.resource;

import java.util.ResourceBundle;

import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

import org.codehaus.jettison.json.JSONException;
import org.codehaus.jettison.json.JSONObject;

import com.sismics.reader.core.util.ConfigUtil;

/**
 * API information REST resource.
 * 
 * @author jtremeaux
 */
@Path("/api")
public class ApiResource extends BaseResource {
    /**
     * Return the information about the API.
     * 
     * @return Response
     * @throws JSONException
     */
    @GET
    @Path("info")
    @Produces(MediaType.APPLICATION_JSON)
    public Response version() throws JSONException {
        ResourceBundle configBundle = ConfigUtil.getConfigBundle();
        String currentVersion = configBundle.getString("api.current_version");
        String minVersion = configBundle.getString("api.min_version");

        JSONObject response = new JSONObject();
        response.put("current_version", currentVersion.replace("-SNAPSHOT", ""));
        response.put("min_version", minVersion);
        return Response.ok().entity(response).build();
    }
}
