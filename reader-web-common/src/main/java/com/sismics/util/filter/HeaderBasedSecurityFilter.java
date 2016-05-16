package com.sismics.util.filter;

import com.sismics.reader.core.dao.jpa.UserDao;
import com.sismics.reader.core.model.jpa.User;

import javax.servlet.FilterConfig;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;

/**
 * A header-based security filter that authenticates an user using the "X-Authenticated-User" request header as the user ID.
 * This filter is intended to be used in conjunction with an external authenticating proxy.
 *
 * @author pacien
 */
public class HeaderBasedSecurityFilter extends SecurityFilter {

    public static final String AUTHENTICATED_USER_HEADER = "X-Authenticated-User";

    private boolean enabled;

    @Override
    public void init(FilterConfig filterConfig) throws ServletException {
        this.enabled = Boolean.parseBoolean(filterConfig.getInitParameter("enabled"))
                || Boolean.parseBoolean(System.getProperty("reader.header_authentication"));
    }

    @Override
    protected User authenticate(HttpServletRequest request) {
        if (!this.enabled) return null;

        String username = request.getHeader(AUTHENTICATED_USER_HEADER);
        return (new UserDao()).getActiveByUsername(username);
    }

}
