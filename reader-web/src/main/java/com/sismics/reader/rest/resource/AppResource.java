package com.sismics.reader.rest.resource;

import java.util.ResourceBundle;

import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

import org.codehaus.jettison.json.JSONException;
import org.codehaus.jettison.json.JSONObject;

import com.sismics.reader.core.model.context.AppContext;
import com.sismics.reader.core.util.ConfigUtil;
import com.sismics.rest.exception.ServerException;

/**
 * General app REST resource.
 * 
 * @author jtremeaux
 */
@Path("/app")
public class AppResource extends BaseResource {
    /**
     * Return the information about the API.
     * 
     * @return Response
     * @throws JSONException
     */
    @GET
    @Path("version")
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
    
    @GET
    @Path("log")
    @Produces(MediaType.APPLICATION_JSON)
    public Response log() throws JSONException {
        JSONObject response = new JSONObject();
        // TODO Not yet implemented
        return Response.ok().entity(response).build();
    }
    
    /**
     * Destroy and rebuild articles index.
     * 
     * @return Response
     * @throws JSONException
     */
    @GET
    @Path("batch/reindex")
    @Produces(MediaType.APPLICATION_JSON)
    public Response batchReindex() throws JSONException {
        JSONObject response = new JSONObject();
        try {
            AppContext.getInstance().getIndexingService().rebuildIndex();
        } catch (Exception e) {
            throw new ServerException("IndexingError", "Error rebuilding index", e);
        }
        response.put("status", "ok");
        return Response.ok().entity(response).build();
    }
}
