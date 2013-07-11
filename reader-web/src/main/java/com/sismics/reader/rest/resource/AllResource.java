package com.sismics.reader.rest.resource;

import java.util.ArrayList;
import java.util.List;

import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.QueryParam;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

import org.codehaus.jettison.json.JSONException;
import org.codehaus.jettison.json.JSONObject;

import com.sismics.reader.core.dao.jpa.UserArticleDao;
import com.sismics.reader.core.dao.jpa.criteria.UserArticleCriteria;
import com.sismics.reader.core.dao.jpa.dto.UserArticleDto;
import com.sismics.reader.core.util.jpa.PaginatedList;
import com.sismics.reader.core.util.jpa.PaginatedLists;
import com.sismics.reader.rest.assembler.ArticleAssembler;
import com.sismics.rest.exception.ForbiddenClientException;

/**
 * All articles REST resources.
 * 
 * @author jtremeaux
 */
@Path("/all")
public class AllResource extends BaseResource {
    /**
     * Returns all articles.
     * 
     * @param unread Returns only unread articles
     * @param limit Page limit
     * @param offset Page offset
     * @return Response
     * @throws JSONException
     */
    @GET
    @Produces(MediaType.APPLICATION_JSON)
    public Response get(
            @QueryParam("unread") boolean unread,
            @QueryParam("limit") Integer limit,
            @QueryParam("offset") Integer offset,
            @QueryParam("total") Integer total) throws JSONException {
        if (!authenticate()) {
            throw new ForbiddenClientException();
        }
        
        // Get the articles
        UserArticleCriteria userArticleCriteria = new UserArticleCriteria();
        userArticleCriteria.setUnread(unread);
        userArticleCriteria.setUserId(principal.getId());
        userArticleCriteria.setSubscribed(true);
        userArticleCriteria.setVisible(true);

        UserArticleDao userArticleDao = new UserArticleDao();
        PaginatedList<UserArticleDto> paginatedList = PaginatedLists.create(limit, offset);
        if(total != null) {
            userArticleDao.countByCriteria(userArticleCriteria, paginatedList);
            if (paginatedList.getResultCount() != total) {
                offset += paginatedList.getResultCount() - total;
                paginatedList = PaginatedLists.create(limit, offset);
            }
        }
        userArticleDao.findByCriteria(userArticleCriteria, paginatedList);
        
        // Build the response
        JSONObject response = new JSONObject();

        List<JSONObject> articles = new ArrayList<JSONObject>();
        for (UserArticleDto userArticle : paginatedList.getResultList()) {
            articles.add(ArticleAssembler.asJson(userArticle));
        }
        response.put("total", paginatedList.getResultCount());
        response.put("articles", articles);

        return Response.ok().entity(response).build();
    }

    /**
     * Marks all articles as read.
     * 
     * @return Response
     * @throws JSONException
     */
    @POST
    @Path("/read")
    @Produces(MediaType.APPLICATION_JSON)
    public Response read() throws JSONException {
        if (!authenticate()) {
            throw new ForbiddenClientException();
        }
        
        // Marks all articles of this user as read
        UserArticleCriteria userArticleCriteria = new UserArticleCriteria();
        userArticleCriteria.setUserId(principal.getId());
        userArticleCriteria.setSubscribed(true);

        UserArticleDao userArticleDao = new UserArticleDao();
        userArticleDao.markAsRead(userArticleCriteria);
        
        // Always return ok
        JSONObject response = new JSONObject();
        response.put("status", "ok");
        return Response.ok().entity(response).build();
    }

}
