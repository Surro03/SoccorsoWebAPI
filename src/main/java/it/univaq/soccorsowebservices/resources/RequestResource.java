/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package it.univaq.soccorsowebservices.resources;
import it.univaq.soccorsowebservices.dto.Operator;
import it.univaq.soccorsowebservices.dto.Request;
import it.univaq.soccorsowebservices.security.Logged;
import java.time.OffsetDateTime;
import java.io.OutputStream;
import jakarta.ws.rs.Consumes;
import jakarta.ws.rs.DELETE;
import jakarta.ws.rs.GET;
import jakarta.ws.rs.POST;
import jakarta.ws.rs.PUT;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.PathParam;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.core.Context;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;
import jakarta.ws.rs.core.SecurityContext;
import jakarta.ws.rs.core.StreamingOutput;
import jakarta.ws.rs.core.UriInfo;
import java.io.InputStream;
import java.net.URI;
import java.util.List;
import java.util.ArrayList;
import jakarta.ws.rs.QueryParam;
import jakarta.ws.rs.DefaultValue;
import jakarta.ws.rs.container.ContainerRequestContext;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import java.util.concurrent.ConcurrentHashMap;
/**
 *
 * @author surro
 */

@Path("/requests")
@Produces(MediaType.APPLICATION_JSON)
@Consumes(MediaType.APPLICATION_JSON)
public class RequestResource {
    //simuliamo la persistenza sul DB
    private static volatile long SEQ = 1;
    private static final ConcurrentHashMap<Long, Request> Database = new ConcurrentHashMap<>();
    
    
    @POST
    public Response create(Request req, @Context UriInfo uri) {
        long id = SEQ++;

        Request rq = new Request(
            id,
            "ACTIVE",
            null,
            req.Type(),
            req.Location(),
            java.time.OffsetDateTime.now(),
            false
        );

        Database.put(id, rq);

        URI location = uri.getAbsolutePathBuilder().path(String.valueOf(id)).build();
        return Response.created(location).entity(rq).build();
    }
    
    
    @GET
    @Logged
    @Path("/{id}")
    public Response getRequest(@PathParam("id") long id){
        Request hr = Database.get(id);
        if(hr == null){
                return Response.status(404).entity("no-Request-found").build();
           }
           else{
               return Response.ok(hr).build();
           }
    }
    
    //static version for MissionResource
    public static Request getStaticRequest(long id){
        Request hr = Database.get(id);
        if(hr == null){
                return null;
           }
           else{
               return hr;
           }
        }
    
    
    public static void setStatus(long id, String Status){
        Request hr = Database.get(id);
        Database.remove(hr.id());
        Database.put(id, new Request(id, Status, hr.SuccessLevel(), hr.Type(), hr.Location(), hr.RequestTime(), hr.Validated()));
    }
    
    public static void validate(long id){
        Request hr = Database.get(id);
        Database.remove(hr.id());
        Database.put(id, new Request(id, hr.Status(), hr.SuccessLevel(), hr.Type(), hr.Location(), hr.RequestTime(), true));
    }
        
    @GET
    @Logged
    public Response getStatusRequestList(
                                   @QueryParam("status") String status,
                                   @QueryParam("page") @DefaultValue("1") int page,
                                   @QueryParam("size") @DefaultValue("15") int size){
        
        
        Stream<Request> Requests = Database.values().stream();
        if (status != null) {
            Requests = Requests.filter(rq -> rq.Status().equalsIgnoreCase(status));
        }
        else{
            return Response.status(Response.Status.BAD_REQUEST).type("text/plain").entity("The query param used is not valid").build();
        } 
        
        List<Request> filteredRequests = Requests.toList();
        
        // Facciamo la paginazione
        int total = filteredRequests.size();
        int from = Math.max((page-1)*size, 0);
        int to = Math.min(from+size, total);
        //Estrazione della sotto‐lista:
        //    – se from >= total → lista vuota
        //    – altrimenti subList(from, to)
        List<Request> pageItems = (from >= total) ? List.of() : filteredRequests.subList(from, to);
        
        return Response.ok(pageItems)
                       .header("X-Total-Count", total)
                       .build();
        }
    
    
    @GET
    @Logged
    @Path("/notPositive")
    public Response getNotPositiveRequests(){
         List<Request> notPositive = Database.values().stream()
                                                      .filter(rq -> rq.SuccessLevel() != null 
                                                              && rq.SuccessLevel() <= 5)
                                                      .collect(Collectors.toList());
        if (notPositive.isEmpty()) {
                    return Response.status(404).entity("no-Negative-requests").build();    
            }
        else{
            return Response.ok(notPositive).build();
        }
    }
    
    @GET
    @Logged
    @Path("{id}/validate")
    public Response validateRequest(@PathParam ("id") long id){
        Request hr = Database.get(id);
        if (hr == null || hr.Validated()){
            return Response.status(404).entity("no-Request-to-validate").build();
        }
        RequestResource.validate(id);
        return Response.noContent().build();
        
    }
    
    @DELETE
    @Logged
    @Path("{id}/remove")
    public Response deleteRequest(@PathParam ("id") long id, @Context SecurityContext sc){
         if (!sc.isUserInRole("admin")) {
            return Response.status(Response.Status.FORBIDDEN)
                           .entity("must-be-admin")
                           .build();
        }
        
        Request hr = Database.remove(id);
        if(hr == null){
            return Response.status(404).entity("no-Request-found").build();
        }
        
        return Response.noContent().build();
    }
    
   

}
