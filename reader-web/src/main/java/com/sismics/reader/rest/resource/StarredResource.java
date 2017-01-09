package com.sismics.reader.rest.resource;

import com.sismics.reader.core.dao.jpa.UserArticleDao;
import com.sismics.reader.core.dao.jpa.criteria.UserArticleCriteria;
import com.sismics.reader.core.dao.jpa.dto.UserArticleDto;
import com.sismics.reader.core.model.jpa.UserArticle;
import com.sismics.reader.core.util.jpa.PaginatedList;
import com.sismics.reader.core.util.jpa.PaginatedLists;
import com.sismics.reader.rest.assembler.ArticleAssembler;
import com.sismics.rest.exception.ClientException;
import com.sismics.rest.exception.ForbiddenClientException;
import org.codehaus.jettison.json.JSONException;
import org.codehaus.jettison.json.JSONObject;

import javax.ws.rs.*;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import java.text.MessageFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

/**
 * Starred articles REST resources.
 * 
 * @author jtremeaux
 */
@Path("/starred")
public class StarredResource extends BaseResource {
    /**
     * Returns starred articles.
     *
     * @param limit Page limit
     * @param afterArticle Start the list after this article
     * @return Response
     */
    @GET
    @Produces(MediaType.APPLICATION_JSON)
    public Response get(
            @QueryParam("limit") Integer limit,
            @QueryParam("after_article") String afterArticle) throws JSONException {
        if (!authenticate()) {
            throw new ForbiddenClientException();
        }

        // Get the articles
        UserArticleDao userArticleDao = new UserArticleDao();
        UserArticleCriteria userArticleCriteria = new UserArticleCriteria()
            .setStarred(true)
            .setVisible(true)
            .setUserId(principal.getId());
        if (afterArticle != null) {
            // Paginate after this user article
            UserArticleCriteria afterArticleCriteria = new UserArticleCriteria()
                    .setUserArticleId(afterArticle)
                    .setUserId(principal.getId());
            List<UserArticleDto> userArticleDtoList = userArticleDao.findByCriteria(afterArticleCriteria);
            if (userArticleDtoList.isEmpty()) {
                throw new ClientException("ArticleNotFound", MessageFormat.format("Can't find user article {0}", afterArticle));
            }
            UserArticleDto userArticleDto = userArticleDtoList.iterator().next();

            userArticleCriteria.setUserArticleStarredDateMax(new Date(userArticleDto.getStarTimestamp()));
            userArticleCriteria.setUserArticleIdMax(userArticleDto.getId());
        }

        PaginatedList<UserArticleDto> paginatedList = PaginatedLists.create(limit, null);
        userArticleDao.findByCriteria(paginatedList, userArticleCriteria, null, null);
        
        // Build the response
        JSONObject response = new JSONObject();

        List<JSONObject> articles = new ArrayList<JSONObject>();
        for (UserArticleDto userArticle : paginatedList.getResultList()) {
            articles.add(ArticleAssembler.asJson(userArticle));
        }
        response.put("articles", articles);

        return Response.ok().entity(response).build();
    }

    /**
     * Marks an article as starred.
     * 
     * @param id User article ID
     * @return Response
     */
    @PUT
    @Path("{id: [a-z0-9\\-]+}")
    @Produces(MediaType.APPLICATION_JSON)
    public Response star(
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
        if (userArticle.getStarredDate() != null) {
            throw new ClientException("ArticleAlreadyStarred", MessageFormat.format("Article already starred: {0}", id));
        }
        
        // Update the article
        userArticle.setStarredDate(new Date());
        userArticleDao.update(userArticle);
        
        // Always return ok
        JSONObject response = new JSONObject();
        response.put("status", "ok");
        return Response.ok().entity(response).build();
    }

    /**
     * Marks an article as unstarred.
     * 
     * @param id User article ID
     * @return Response
     */
    @DELETE
    @Path("{id: [a-z0-9\\-]+}")
    @Produces(MediaType.APPLICATION_JSON)
    public Response unstar(
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
        if (userArticle.getStarredDate() == null) {
            throw new ClientException("ArticleNotStarred", MessageFormat.format("The article is not starred: {0}", id));
        }
        
        // Update the article
        userArticle.setStarredDate(null);
        userArticleDao.update(userArticle);
        
        // Always return ok
        JSONObject response = new JSONObject();
        response.put("status", "ok");
        return Response.ok().entity(response).build();
    }
    
    /**
     * Marks multiple articles as starred.
     * 
     * @param idList List of article ID
     * @return Response
     */
    @POST
    @Path("star")
    @Produces(MediaType.APPLICATION_JSON)
    public Response starMultiple(
            @FormParam("id") List<String> idList) throws JSONException {
        if (!authenticate()) {
            throw new ForbiddenClientException();
        }
        
        for (String id : idList) {
            // Get the article
            UserArticleDao userArticleDao = new UserArticleDao();
            UserArticle userArticle = userArticleDao.getUserArticle(id, principal.getId());
            if (userArticle == null) {
                throw new ClientException("ArticleNotFound", MessageFormat.format("Article not found: {0}", id));
            }
            
            // Update the article
            userArticle.setStarredDate(new Date());
            userArticleDao.update(userArticle);
        }
        
        // Always return ok
        JSONObject response = new JSONObject();
        response.put("status", "ok");
        return Response.ok().entity(response).build();
    }
    
    /**
     * Marks multiple articles as unstarred.
     * 
     * @param idList List of article ID
     * @return Response
     */
    @POST
    @Path("unstar")
    @Produces(MediaType.APPLICATION_JSON)
    public Response unstarMultiple(
            @FormParam("id") List<String> idList) throws JSONException {
        if (!authenticate()) {
            throw new ForbiddenClientException();
        }
        
        for (String id : idList) {
            // Get the article
            UserArticleDao userArticleDao = new UserArticleDao();
            UserArticle userArticle = userArticleDao.getUserArticle(id, principal.getId());
            if (userArticle == null) {
                throw new ClientException("ArticleNotFound", MessageFormat.format("Article not found: {0}", id));
            }
            
            // Update the article
            userArticle.setStarredDate(null);
            userArticleDao.update(userArticle);
        }
        
        // Always return ok
        JSONObject response = new JSONObject();
        response.put("status", "ok");
        return Response.ok().entity(response).build();
    }
}
