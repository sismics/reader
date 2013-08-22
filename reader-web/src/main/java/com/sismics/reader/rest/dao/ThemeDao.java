package com.sismics.reader.rest.dao;

import javax.servlet.ServletContext;
import java.io.File;
import java.io.FilenameFilter;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;

/**
 * Theme DAO.
 *
 * @author jtremeaux 
 */
public class ThemeDao {
    public static final String STYLESHEETS_THEME_DIR = "/stylesheets/theme/";

    private final static FilenameFilter CSS_FILTER = new FilenameFilter() {
        @Override
        public boolean accept(File dir, String name) {
            return name.endsWith(".css") || name.endsWith(".less");
        }
    };

    /**
     * Return the list of all themes.
     *
     * @param servletContext Servlet context
     * @return List of themes
     */
    public List<String> findAll(ServletContext servletContext) {
        Set<String> fileList = null;
        List<String> themeList = new ArrayList<String>();
        if (servletContext != null) {
            fileList = servletContext.getResourcePaths("/stylesheets/theme/");
            for (String file : fileList) {
                if (CSS_FILTER.accept(null, file)) {
                    themeList.add(new File(file).getName());
                }
            }
        } else {
            File dir = new File(this.getClass().getResource(STYLESHEETS_THEME_DIR).getFile());
            for (File file : dir.listFiles(CSS_FILTER)) {
               themeList.add(file.getName());
            }
        }
        return themeList;
    }
}