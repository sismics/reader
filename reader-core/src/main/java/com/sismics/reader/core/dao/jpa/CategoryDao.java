package com.sismics.reader.core.dao.jpa;

import com.sismics.reader.core.model.jpa.Category;
import com.sismics.util.context.ThreadLocalContext;

import javax.persistence.EntityManager;
import javax.persistence.Query;
import java.util.Date;
import java.util.List;
import java.util.UUID;

/**
 * Category DAO.
 * 
 * @author jtremeaux
 */
public class CategoryDao {
    /**
     * Creates a new category.
     * 
     * @param category Category
     * @return New ID
     */
    public String create(Category category) {
        // Create the UUID
        category.setId(UUID.randomUUID().toString());
        
        // Create the category
        EntityManager em = ThreadLocalContext.get().getEntityManager();
        category.setCreateDate(new Date());
        em.persist(category);
        
        return category.getId();
    }
    
    /**
     * Updates a category.
     * 
     * @param category Category
     * @return Updated category
     */
    public Category update(Category category) {
        EntityManager em = ThreadLocalContext.get().getEntityManager();
        
        // Get the category
        Query q = em.createQuery("select c from Category c where c.id = :id and c.deleteDate is null")
            .setParameter("id", category.getId());
        Category categoryFromDb = (Category) q.getSingleResult();

        // Update the category
        categoryFromDb.setName(category.getName());
        categoryFromDb.setOrder(category.getOrder());
        categoryFromDb.setFolded(category.isFolded());
        
        return category;
    }
    
    /**
     * Moves the category to the specified display order, and reorders adjacent categories.
     * 
     * @param category Category to move
     * @param order New display order
     */
    @SuppressWarnings("unchecked")
    public void reorder(Category category, int order) {
        EntityManager em = ThreadLocalContext.get().getEntityManager();
        
        // Find categories with the same parent
        Query q = em.createQuery("select c from Category c where c.parentId = :parentId and c.userId = :userId and c.deleteDate is null order by c.order")
                .setParameter("parentId", category.getParentId())
                .setParameter("userId", category.getUserId());
        List<Category> categoryList = (List<Category>) q.getResultList();
        for (int i = 0; i < categoryList.size(); i++) {
            Category currentCategory = categoryList.get(i);
            if (currentCategory.getId().equals(category.getId())) {
                categoryList.remove(i);
            }
        }
        categoryList.add(order > categoryList.size() ? categoryList.size() : order, category);
        for (int i = 0; i < categoryList.size(); i++) {
            Category currentCategory = categoryList.get(i);
            currentCategory.setOrder(i);
        }
    }
    
    /**
     * Deletes a category.
     * 
     * @param id Category ID
     */
    public void delete(String id) {
        EntityManager em = ThreadLocalContext.get().getEntityManager();
        
        // Get the category
        Query q = em.createQuery("select c from Category c where c.id = :id and c.deleteDate is null")
                .setParameter("id", id);
        Category categoryFromDb = (Category) q.getSingleResult();

        // Delete the category
        categoryFromDb.setDeleteDate(new Date());
    }
    
    /**
     * Returns the root category of a user.
     * 
     * @param userId User ID
     * @return Root category
     */
    public Category getRootCategory(String userId) {
        EntityManager em = ThreadLocalContext.get().getEntityManager();
        Query q = em.createQuery("select c from Category c where c.userId = :userId and c.parentId is null and c.deleteDate is null")
                .setParameter("userId", userId);
        return (Category) q.getSingleResult();
    }
    
    /**
     * Returns an active category.
     * 
     * @param id Category ID
     * @param userId User ID
     * @return Category
     */
    public Category getCategory(String id, String userId) {
        EntityManager em = ThreadLocalContext.get().getEntityManager();
        Query q = em.createQuery("select c from Category c where c.id = :id and c.userId = :userId and c.deleteDate is null")
                .setParameter("id", id)
                .setParameter("userId", userId);
        return (Category) q.getSingleResult();
    }

    /**
     * Returns the number of sub-categories in a parent category.
     * 
     * @param parentId Category ID
     * @param userId User ID
     * @return Category
     */
    public int getCategoryCount(String parentId, String userId) {
        EntityManager em = ThreadLocalContext.get().getEntityManager();
        Query q = em.createQuery("select count(c.id) from Category c where c.parentId = :parentId and c.userId = :userId and c.deleteDate is null")
                .setParameter("parentId", parentId)
                .setParameter("userId", userId);
        return ((Long) q.getSingleResult()).intValue();
    }

    /**
     * Returns the list of all categories of a user.
     * 
     * @param userId User ID
     * @return List of categories
     */
    @SuppressWarnings("unchecked")
    public List<Category> findAllCategory(String userId) {
        EntityManager em = ThreadLocalContext.get().getEntityManager();
        Query q = em.createQuery("select c from Category c where c.userId = :userId and c.deleteDate is null order by c.order")
                .setParameter("userId", userId);
        return q.getResultList();
    }

    /**
     * Returns the list of sub-categories in a given category.
     * 
     * @param parentId Parent category
     * @param userId User ID
     * @return List of categories
     */
    @SuppressWarnings("unchecked")
    public List<Category> findSubCategory(String parentId, String userId) {
        EntityManager em = ThreadLocalContext.get().getEntityManager();
        Query q = em.createQuery("select c from Category c where c.parentId = :parentId and c.userId = :userId and c.deleteDate is null order by c.order")
                .setParameter("parentId", parentId)
                .setParameter("userId", userId);
        return q.getResultList();
    }

}
