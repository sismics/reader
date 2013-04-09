package com.sismics.reader.rest.resource;

import java.text.MessageFormat;
import java.util.ArrayList;
import java.util.List;

import javax.persistence.NoResultException;
import javax.ws.rs.DELETE;
import javax.ws.rs.FormParam;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.PUT;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.QueryParam;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

import org.codehaus.jettison.json.JSONException;
import org.codehaus.jettison.json.JSONObject;

import com.sismics.reader.core.dao.jpa.CategoryDao;
import com.sismics.reader.core.dao.jpa.UserArticleDao;
import com.sismics.reader.core.dao.jpa.criteria.UserArticleCriteria;
import com.sismics.reader.core.dao.jpa.dto.UserArticleDto;
import com.sismics.reader.core.model.jpa.Category;
import com.sismics.reader.core.model.jpa.UserArticle;
import com.sismics.reader.core.util.jpa.PaginatedList;
import com.sismics.reader.core.util.jpa.PaginatedLists;
import com.sismics.reader.rest.assembler.ArticleAssembler;
import com.sismics.rest.exception.ClientException;
import com.sismics.rest.exception.ForbiddenClientException;
import com.sismics.rest.util.ValidationUtil;

/**
 * Category REST resources.
 * 
 * @author jtremeaux
 */
@Path("/category")
public class CategoryResource extends BaseResource {
    /**
     * Returns all categories.
     * 
     * @return Response
     * @throws JSONException
     */
    @GET
    @Produces(MediaType.APPLICATION_JSON)
    public Response list() throws JSONException {
        if (!authenticate()) {
            throw new ForbiddenClientException();
        }
        
        // Get the root category
        CategoryDao categoryDao = new CategoryDao();
        Category rootCategory = categoryDao.getRootCategory(principal.getId());
        
        // Get the subcategories
        List<Category> categoryList = categoryDao.findSubCategory(rootCategory.getId(), principal.getId());
        
        // Build the response
        List<JSONObject> rootCategories = new ArrayList<JSONObject>();

        JSONObject rootCategoryJson = new JSONObject();
        rootCategoryJson.put("id", rootCategory.getId());
        rootCategories.add(rootCategoryJson);

        List<JSONObject> categoriesJson = new ArrayList<JSONObject>();
        for (Category category : categoryList) {
            JSONObject categoryJson = new JSONObject();
            categoryJson.put("id", category.getId());
            categoryJson.put("name", category.getName());
            categoriesJson.add(categoryJson);
        }
        rootCategoryJson.put("categories", categoriesJson);
        
        JSONObject response = new JSONObject();
        response.put("categories", rootCategories);
        return Response.ok().entity(response).build();
    }
    
    /**
     * Returns all articles in a category.
     * 
     * @param id Category ID
     * @param unread Returns only unread articles
     * @param limit Page limit
     * @param offset Page offset
     * @return Response
     * @throws JSONException
     */
    @GET
    @Path("{id: [a-z0-9\\-]+}")
    @Produces(MediaType.APPLICATION_JSON)
    public Response get(
            @PathParam("id") String id,
            @QueryParam("unread") boolean unread,
            @QueryParam("limit") Integer limit,
            @QueryParam("offset") Integer offset) throws JSONException {
        if (!authenticate()) {
            throw new ForbiddenClientException();
        }
        
        // Get the category
        CategoryDao categoryDao = new CategoryDao();
        Category category = null;
        try {
            category = categoryDao.getCategory(id, principal.getId());
        } catch (NoResultException e) {
            throw new ClientException("CategoryNotFound", MessageFormat.format("Category not found: {0}", id));
        }

        // Get the articles
        UserArticleCriteria userArticleCriteria = new UserArticleCriteria();
        userArticleCriteria.setUnread(unread);
        userArticleCriteria.setUserId(principal.getId());
        if (category.getParentId() != null) {
            userArticleCriteria.setCategoryId(id);
        }

        UserArticleDao userArticleDao = new UserArticleDao();
        PaginatedList<UserArticleDto> paginatedList = PaginatedLists.create(limit, offset);
        userArticleDao.findByCriteria(userArticleCriteria, paginatedList);
        
        // Create article subscriptions for this user
        for (UserArticleDto userArticleDto : paginatedList.getResultList()) {
            if (userArticleDto.getId() == null) {
                UserArticle userArticle = new UserArticle();
                userArticle.setArticleId(userArticleDto.getArticleId());
                userArticle.setUserId(principal.getId());
                String userArticleId = userArticleDao.create(userArticle);
                userArticleDto.setId(userArticleId);
            }
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
    
    /**
     * Creates a new category.
     * 
     * @param name Category name
     * @return Response
     * @throws JSONException
     */
    @PUT
    @Produces(MediaType.APPLICATION_JSON)
    public Response add(
            @FormParam("name") String name) throws JSONException {
        if (!authenticate()) {
            throw new ForbiddenClientException();
        }
        
        // Validate input data
        name = ValidationUtil.validateLength(name, "name", 1, 100, false);
        
        // Get the root category
        CategoryDao categoryDao = new CategoryDao();
        Category rootCategory = categoryDao.getRootCategory(principal.getId());
        
        // Get the display order
        int displayOrder = categoryDao.getCategoryCount(rootCategory.getId(), principal.getId());
        
        // Create the category
        Category category = new Category();
        category.setUserId(principal.getId());
        category.setParentId(rootCategory.getId());
        category.setName(name);
        category.setOrder(displayOrder);
        String categoryId = categoryDao.create(category);
        
        JSONObject response = new JSONObject();
        response.put("id", categoryId);
        return Response.ok().entity(response).build();
    }

    /**
     * Deletes a category.
     * 
     * @param id Category ID
     * @return Response
     * @throws JSONException
     */
    @DELETE
    @Path("{id: [a-z0-9\\-]+}")
    @Produces(MediaType.APPLICATION_JSON)
    public Response delete(
            @PathParam("id") String id) throws JSONException {
        if (!authenticate()) {
            throw new ForbiddenClientException();
        }

        // Get the category
        CategoryDao categoryDao = new CategoryDao();
        try {
            categoryDao.getCategory(id, principal.getId());
        } catch (NoResultException e) {
            throw new ClientException("CategoryNotFound", MessageFormat.format("Category not found: {0}", id));
        }
        
        // Delete the category
        categoryDao.delete(id);
        //TODO move subscriptions in this category to root
        
        // Always return ok
        JSONObject response = new JSONObject();
        response.put("status", "ok");
        return Response.ok().entity(response).build();
    }

    /**
     * Marks all articles in this category as read.
     * 
     * @param id Category ID
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
        
        // Get the category
        CategoryDao categoryDao = new CategoryDao();
        try {
            categoryDao.getCategory(id, principal.getId());
        } catch (NoResultException e) {
            throw new ClientException("CategoryNotFound", MessageFormat.format("Category not found: {0}", id));
        }

        // Marks all articles as read in this category
        UserArticleCriteria userArticleCriteria = new UserArticleCriteria();
        userArticleCriteria.setUserId(principal.getId());
        userArticleCriteria.setCategoryId(id);

        UserArticleDao userArticleDao = new UserArticleDao();
        userArticleDao.markAsRead(userArticleCriteria);
        
        // Always return ok
        JSONObject response = new JSONObject();
        response.put("status", "ok");
        return Response.ok().entity(response).build();
    }

    /**
     * Updates the category.
     * 
     * @param id Category ID
     * @param name Category name
     * @param order Display order of this category
     * @param folded True if this category is folded in the subscriptions tree.
     * @return Response
     * @throws JSONException
     */
    @POST
    @Path("{id: [a-z0-9\\-]+}")
    @Produces(MediaType.APPLICATION_JSON)
    public Response update(
            @PathParam("id") String id,
            @FormParam("name") String name,
            @FormParam("order") Integer order,
            @FormParam("folded") Boolean folded) throws JSONException {
        if (!authenticate()) {
            throw new ForbiddenClientException();
        }
        
        // Validate input data
        name = ValidationUtil.validateLength(name, "name", 1, 100, true);
        
        // Get the category
        CategoryDao categoryDao = new CategoryDao();
        Category category = null;
        try {
            category = categoryDao.getCategory(id, principal.getId());
        } catch (NoResultException e) {
            throw new ClientException("CategoryNotFound", MessageFormat.format("Category not found: {0}", id));
        }
        
        // Update the category
        if (name != null) {
            category.setName(name);
        }
        if (folded != null) {
            category.setFolded(folded);
        }
        categoryDao.update(category);
        
        // Reorder categories
        if (order != null) {
            categoryDao.reorder(category, order);
        }
        
        // Always return ok
        JSONObject response = new JSONObject();
        response.put("status", "ok");
        return Response.ok().entity(response).build();
    }
}
