package com.sismics.reader.rest.resource;

import java.text.MessageFormat;
import java.util.Date;

import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

import org.codehaus.jettison.json.JSONException;
import org.codehaus.jettison.json.JSONObject;

import com.sismics.reader.core.dao.jpa.UserArticleDao;
import com.sismics.reader.core.model.jpa.UserArticle;
import com.sismics.rest.exception.ClientException;
import com.sismics.rest.exception.ForbiddenClientException;

/**
 * Article REST resources.
 * 
 * @author jtremeaux
 */
@Path("/article")
public class ArticleResource extends BaseResource {
    /**
     * Marks an article as read.
     * 
     * @param id Article ID
     * @return Response
     * @throws JSONException
     */
    @POST
    @Path("{id: [a-z0-9\\-]+}/read")
    @Produces(MediaType.APPLICATION_JSON)
    public Response read(
            @PathParam("id") String id) throws JSONException {
        if (!authenticate()) {
            throw new ForbiddenClientException();
        }
        
        // Get the article
        UserArticleDao userArticleDao = new UserArticleDao();
        UserArticle userArticle = userArticleDao.getUserArticle(id, principal.getId());
        if (userArticle == null) {
            throw new ClientException("ArticleNotFound", MessageFormat.format("Article not found: {0}", id));
        }
        
        // Update the article
        userArticle.setReadDate(new Date());
        userArticleDao.update(userArticle);
        
        // Always return ok
        JSONObject response = new JSONObject();
        response.put("status", "ok");
        return Response.ok().entity(response).build();
    }

    /**
     * Marks an article as unread.
     * 
     * @param id Article ID
     * @return Response
     * @throws JSONException
     */
    @POST
    @Path("{id: [a-z0-9\\-]+}/unread")
    @Produces(MediaType.APPLICATION_JSON)
    public Response unread(
            @PathParam("id") String id) throws JSONException {
        if (!authenticate()) {
            throw new ForbiddenClientException();
        }
        
        // Get the article
        UserArticleDao userArticleDao = new UserArticleDao();
        UserArticle userArticle = userArticleDao.getUserArticle(id, principal.getId());
        if (userArticle == null) {
            throw new ClientException("ArticleNotFound", MessageFormat.format("Article not found: {0}", id));
        }
        
        // Update the article
        userArticle.setReadDate(null);
        userArticleDao.update(userArticle);
        
        // Always return ok
        JSONObject response = new JSONObject();
        response.put("status", "ok");
        return Response.ok().entity(response).build();
    }
}
