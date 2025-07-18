/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package it.univaq.soccorsowebservices.security;
import jakarta.annotation.Priority;
import jakarta.ws.rs.Priorities;
import jakarta.ws.rs.container.ContainerRequestContext;
import jakarta.ws.rs.container.ContainerRequestFilter;
import jakarta.ws.rs.core.HttpHeaders;
import jakarta.ws.rs.core.Response;
import jakarta.ws.rs.core.SecurityContext;
import jakarta.ws.rs.ext.Provider;
import java.io.IOException;
import java.security.Principal;

/**
 *
 * @author surro
 */
@Provider
@Logged
@Priority(Priorities.AUTHENTICATION)
public class LoggedFilt implements ContainerRequestFilter {

    @Override
    public void filter(ContainerRequestContext requestContext) throws IOException {
        String token = null;
        final String path = requestContext.getUriInfo().getAbsolutePath().toString();

        // Estrai il token dall'header Authorization
        String authorizationHeader = requestContext.getHeaderString(HttpHeaders.AUTHORIZATION);
        if (authorizationHeader != null && authorizationHeader.startsWith("Bearer ")) {
            token = authorizationHeader.substring("Bearer".length()).trim();
        }

        if (token != null && !token.isEmpty() && AuthMap.isTokenValid(token)) {
            try {
                // Recupera username dal AuthMap (dato che non lo ricavi dal token). 
                //Di norma questi dati (e.g. il ruolo) li andremmo a recuperare dal db durante il login
                String username = AuthMap.getUsernameFromToken(token);
                String userRole = AuthMap.getRoleForUser(username);

                if (username != null && !username.isBlank()) {
                    requestContext.setProperty("token", token);
                    requestContext.setProperty("username", username);
                    requestContext.setProperty("role", userRole);

                    final String userFinal = username;

                    requestContext.setSecurityContext(new SecurityContext() {
                        @Override
                        public Principal getUserPrincipal() {
                            return () -> userFinal;
                        }

                        @Override
                        public boolean isUserInRole(String role) {
                            return role.equalsIgnoreCase(userRole);
                        }

                        @Override
                        public boolean isSecure() {
                            //return path.startsWith("https"); //siamo su localhost quindi darebbe errore
                            return true;
                        }

                        @Override
                        public String getAuthenticationScheme() {
                            return "Token-Based-Auth-Scheme";
                        }
                    });
                    return; 
                }
            } catch (Exception e) {
                
            }
        }

        // Token mancante, invalido o username non trovato → 401
        requestContext.abortWith(Response.status(Response.Status.UNAUTHORIZED).build());
    }
}
