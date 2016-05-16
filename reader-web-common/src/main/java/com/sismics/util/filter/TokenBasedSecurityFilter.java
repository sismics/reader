package com.sismics.util.filter;

import com.sismics.reader.core.dao.jpa.AuthenticationTokenDao;
import com.sismics.reader.core.dao.jpa.UserDao;
import com.sismics.reader.core.model.jpa.AuthenticationToken;
import com.sismics.reader.core.model.jpa.User;

import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServletRequest;
import java.text.MessageFormat;
import java.util.Date;

/**
 * This filter is used to authenticate the user having an active session via an authentication token stored in database.
 * The filter extracts the authentication token stored in a cookie.
 * If the cookie exists and the token is valid, the filter injects a UserPrincipal into a request attribute.
 * If not, the user is anonymous, and the filter injects a AnonymousPrincipal into the request attribute.
 *
 * @author jtremeaux
 * @author pacien
 */
public class TokenBasedSecurityFilter extends SecurityFilter {

    /**
     * Name of the cookie used to store the authentication token.
     */
    public static final String COOKIE_NAME = "auth_token";

    /**
     * Lifetime of the authentication token in seconds, since login.
     */
    public static final int TOKEN_LONG_LIFETIME = 3600 * 24 * 365 * 20;

    /**
     * Lifetime of the authentication token in seconds, since last connection.
     */
    public static final int TOKEN_SESSION_LIFETIME = 3600 * 24;

    /**
     * Extracts and returns an authentication token from a cookie list.
     *
     * @param cookies Cookie list
     * @return nullable auth token
     */
    private static String extractAuthToken(Cookie[] cookies) {
        if (cookies != null)
            for (Cookie cookie : cookies)
                if (COOKIE_NAME.equals(cookie.getName()) && !cookie.getValue().isEmpty())
                    return cookie.getValue();

        return null;
    }

    /**
     * Deletes an expired authentication token.
     *
     * @param authTokenID auth token ID
     */
    private static void handleExpiredToken(AuthenticationTokenDao dao, String authTokenID) {
        try {
            dao.delete(authTokenID);
        } catch (Exception e) {
            if (LOG.isErrorEnabled())
                LOG.error(MessageFormat.format("Error deleting authentication token {0} ", authTokenID), e);
        }
    }

    /**
     * Returns true if the token is expired.
     *
     * @param authenticationToken Authentication token
     * @return Token expired
     */
    private static boolean isTokenExpired(AuthenticationToken authenticationToken) {
        final long now = new Date().getTime();
        final long creationDate = authenticationToken.getCreationDate().getTime();
        if (authenticationToken.isLongLasted()) {
            return now >= creationDate + ((long) TOKEN_LONG_LIFETIME) * 1000L;
        } else {
            long date = authenticationToken.getLastConnectionDate() != null ?
                    authenticationToken.getLastConnectionDate().getTime() : creationDate;
            return now >= date + ((long) TOKEN_SESSION_LIFETIME) * 1000L;
        }
    }

    @Override
    protected User authenticate(HttpServletRequest request) {
        // Get the value of the client authentication token
        String authTokenID = extractAuthToken(request.getCookies());
        if (authTokenID == null)
            return null;

        // Get the corresponding server token
        AuthenticationTokenDao authTokenDao = new AuthenticationTokenDao();
        AuthenticationToken authToken = authTokenDao.get(authTokenID);
        if (authToken == null)
            return null;

        if (isTokenExpired(authToken)) {
            handleExpiredToken(authTokenDao, authTokenID);
            return null;
        }

        authTokenDao.updateLastConnectionDate(authToken.getId());
        String userID = authToken.getUserId();
        return (new UserDao()).getById(userID);
    }

}
