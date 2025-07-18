/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package it.univaq.soccorsowebservices.security;

import jakarta.ws.rs.Consumes;
import jakarta.ws.rs.DELETE;
import jakarta.ws.rs.FormParam;
import jakarta.ws.rs.GET;
import jakarta.ws.rs.POST;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.container.ContainerRequestContext;
import jakarta.ws.rs.core.Context;
import jakarta.ws.rs.core.HttpHeaders;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.NewCookie;
import jakarta.ws.rs.core.Response;
import static jakarta.ws.rs.core.Response.Status.UNAUTHORIZED;
import jakarta.ws.rs.core.UriInfo;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;


/**
 *
 * @author didattica
 */
@Path("/auth")
public class AuthResource {
    private static final Map<String,String> Users = new HashMap<String,String>();
    {
    Users.put("admin","adminpass");
    Users.put("operator","operatorpass");
    }
    
    @POST
    @Path("/login")
    @Consumes(MediaType.APPLICATION_FORM_URLENCODED)
    public Response login(@FormParam("username") String username,
                          @FormParam("password") String password) {
        // Controllo credenziali
        if (username != null
            && password.equals(Users.get(username))) {

            // Genero token e lo salvo insieme all'username
            String authToken = UUID.randomUUID().toString();
            AuthMap.saveToken(authToken, username);

            // Ritorno 200 OK con token nel body, nel cookie e nell'header
            return Response.ok(authToken)
                    //path "/" per renderlo globale
                    .cookie(new NewCookie.Builder("token")
                                .value(authToken)
                                .path("/")
                                .build())
                    .header(HttpHeaders.AUTHORIZATION, "Bearer " + authToken)
                    .build();
        }

        // credenziali errate
        return Response.status(Response.Status.UNAUTHORIZED).build();
    }

    @POST
    @Path("/logout")
    @Logged
    public Response logout(@Context ContainerRequestContext req) {
        // Rimuovo il token da AuthMap
        String token = (String) req.getProperty("token");
        AuthMap.removeToken(token);

        return Response.noContent()
                .cookie(new NewCookie.Builder("token")
                            .value("")
                            .path("/")
                            .maxAge(0)      
                            .build())
                .build();
    }
     //Metodo per fare "refresh" del token senza ritrasmettere le credenziali
    @GET
    @Path("/refresh")
    @Logged
    public Response refresh(@Context ContainerRequestContext req, @Context UriInfo uriinfo) {
        //proprietà iniettata nella request dal filtro di autenticazione
        String token = (String) req.getProperty("token");
        String username = (String) req.getProperty("username");
        AuthMap.removeToken(token);
        String newtoken = UUID.randomUUID().toString();
                AuthMap.saveToken(newtoken, username);
        return Response.ok(newtoken)
                       .cookie(new NewCookie.Builder("token").value(newtoken)
                                .path("/").build())
                                .header(HttpHeaders.AUTHORIZATION, "Bearer " + newtoken).build();  
    }
    
}

