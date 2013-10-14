package com.sismics.reader.rest.resource;

import java.util.ArrayList;
import java.util.List;

import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.QueryParam;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

import org.codehaus.jettison.json.JSONException;
import org.codehaus.jettison.json.JSONObject;

import com.sismics.reader.core.dao.jpa.dto.UserArticleDto;
import com.sismics.reader.core.model.context.AppContext;
import com.sismics.reader.core.service.IndexingService;
import com.sismics.reader.core.util.jpa.PaginatedList;
import com.sismics.reader.rest.assembler.ArticleAssembler;
import com.sismics.rest.exception.ForbiddenClientException;
import com.sismics.rest.exception.ServerException;
import com.sismics.rest.util.ValidationUtil;

/**
 * Search articles REST resources.
 * 
 * @author jtremeaux
 */
@Path("/search")
public class SearchResource extends BaseResource {
    
    /**
     * Returns articles matching a search query.
     * 
     * @param query Search query
     * @param limit Page limit
     * @param offset Page offset
     * @return Response
     * @throws JSONException
     */
    @GET
    @Path("{query: .+}")
    @Produces(MediaType.APPLICATION_JSON)
    public Response get(
            @PathParam("query") String query,
            @QueryParam("limit") Integer limit,
            @QueryParam("offset") Integer offset) throws JSONException {
        if (!authenticate()) {
            throw new ForbiddenClientException();
        }
        
        ValidationUtil.validateRequired(query, "query");
        
        // Search in index
        IndexingService indexingService = AppContext.getInstance().getIndexingService();
        PaginatedList<UserArticleDto> paginatedList = null;
        try {
            paginatedList = indexingService.searchArticles(principal.getId(), query, offset, limit);
        } catch (Exception e) {
            throw new ServerException("SearchError", "Error searching articles", e);
        }
        
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
}
