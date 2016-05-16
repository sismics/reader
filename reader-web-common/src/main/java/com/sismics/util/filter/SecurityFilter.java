package com.sismics.util.filter;

import com.sismics.reader.core.constant.Constants;
import com.sismics.reader.core.dao.jpa.RoleBaseFunctionDao;
import com.sismics.reader.core.model.jpa.User;
import com.sismics.security.AnonymousPrincipal;
import com.sismics.security.UserPrincipal;
import com.sismics.util.LocaleUtil;
import org.joda.time.DateTimeZone;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.servlet.*;
import javax.servlet.http.HttpServletRequest;
import java.io.IOException;
import java.util.Locale;
import java.util.Set;

/**
 * An abstract security filter for user authentication, that injects corresponding users into the request.
 * Successfully authenticated users are injected as UserPrincipal, or as AnonymousPrincipal otherwise.
 * If an user has already been authenticated for the request, no further authentication attempt is made.
 *
 * @author pacien
 * @author jtremeaux
 */
public abstract class SecurityFilter implements Filter {

    /**
     * Name of the attribute containing the principal.
     */
    public static final String PRINCIPAL_ATTRIBUTE = "principal";

    /**
     * Logger.
     */
    public static final Logger LOG = LoggerFactory.getLogger(SecurityFilter.class);

    /**
     * Returns the truth value of "the supplied request has an UserPrincipal".
     *
     * @param request HTTP request
     * @return T(the supplied request has an UserPrincipal)
     */
    private static boolean hasIdentifiedUser(HttpServletRequest request) {
        return request.getAttribute(PRINCIPAL_ATTRIBUTE) instanceof UserPrincipal;
    }

    /**
     * Injects the given user into the request, with the appropriate authentication state.
     *
     * @param request HTTP request
     * @param user    nullable User to inject
     */
    private static void injectUser(HttpServletRequest request, User user) {
        // Check if the user is still valid
        if (user != null && user.getDeleteDate() == null)
            injectAuthenticatedUser(request, user);
        else
            injectAnonymousUser(request);
    }

    /**
     * Inject an authenticated user into the request attributes.
     *
     * @param request HTTP request
     * @param user    User to inject
     */
    private static void injectAuthenticatedUser(HttpServletRequest request, User user) {
        UserPrincipal userPrincipal = new UserPrincipal(user.getId(), user.getUsername());

        // Add locale
        Locale locale = LocaleUtil.getLocale(user.getLocaleId());
        userPrincipal.setLocale(locale);

        // Add base functions
        RoleBaseFunctionDao userBaseFunction = new RoleBaseFunctionDao();
        Set<String> baseFunctionSet = userBaseFunction.findByRoleId(user.getRoleId());
        userPrincipal.setBaseFunctionSet(baseFunctionSet);

        request.setAttribute(PRINCIPAL_ATTRIBUTE, userPrincipal);
    }

    /**
     * Inject an anonymous user into the request attributes.
     *
     * @param request HTTP request
     */
    private static void injectAnonymousUser(HttpServletRequest request) {
        AnonymousPrincipal anonymousPrincipal = new AnonymousPrincipal();
        anonymousPrincipal.setLocale(request.getLocale());
        anonymousPrincipal.setDateTimeZone(DateTimeZone.forID(Constants.DEFAULT_TIMEZONE_ID));

        request.setAttribute(PRINCIPAL_ATTRIBUTE, anonymousPrincipal);
    }

    @Override
    public void init(FilterConfig filterConfig) throws ServletException {
        // NOP
    }

    @Override
    public void destroy() {
        // NOP
    }

    @Override
    public void doFilter(ServletRequest req, ServletResponse response, FilterChain filterChain) throws IOException, ServletException {
        HttpServletRequest request = (HttpServletRequest) req;

        if (!hasIdentifiedUser(request)) {
            User user = this.authenticate(request);
            injectUser(request, user);
        }

        filterChain.doFilter(request, response);
    }

    /**
     * Authenticates an user from the given request parameters.
     *
     * @param request HTTP request
     * @return nullable User
     */
    protected abstract User authenticate(HttpServletRequest request);

}
