package com.example.taskmanagementsystem.security;

import org.springframework.security.authorization.AuthorizationDecision;
import org.springframework.security.authorization.AuthorizationManager;
import org.springframework.security.core.Authentication;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.security.web.access.intercept.RequestAuthorizationContext;
import org.springframework.stereotype.Component;
import org.springframework.web.util.UriTemplate;

import java.util.Map;
import java.util.function.Supplier;

@Component
public class UserRequestAuthorizationManager implements AuthorizationManager<RequestAuthorizationContext> {

    private static final UriTemplate USER_URI_TEMPLATE = new UriTemplate("/user/{id}");

    @Override
    public AuthorizationDecision check(Supplier<Authentication> authenticationSupplier, RequestAuthorizationContext context) {
//        Extract userId from request URI
        Map<String, String> uriVariables = USER_URI_TEMPLATE.match(context.getRequest().getRequestURI());
        String uriUserId = uriVariables.get("id");
//        Extract userId from JWT token
        Authentication authentication = authenticationSupplier.get();
        String jwtUserId = ((Jwt) (authentication.getPrincipal())).getClaim("userId").toString();
//        Check has user role "ROLE_USER"
        boolean hasUserRole = authentication.getAuthorities().stream()
                .anyMatch(grantedAuthority -> grantedAuthority.getAuthority().equals("ROLE_USER"));
//        Check has user role "ROLE_ADMIN"
        boolean hasAdminRole = authentication.getAuthorities().stream()
                .anyMatch(grantedAuthority -> grantedAuthority.getAuthority().equals("ROLE_ADMIN"));
//        Compare two userIds
        boolean userIdsMatch = uriUserId != null && uriUserId.equals(jwtUserId);

        return new AuthorizationDecision(hasAdminRole || (hasUserRole && userIdsMatch));
    }
}
