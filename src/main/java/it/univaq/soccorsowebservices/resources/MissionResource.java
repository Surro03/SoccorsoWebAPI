/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Record.java to edit this template
 */
package it.univaq.soccorsowebservices.resources;
import it.univaq.soccorsowebservices.dto.Mission;
import it.univaq.soccorsowebservices.security.Logged;
import it.univaq.soccorsowebservices.dto.Operator;
import it.univaq.soccorsowebservices.dto.Request;
import java.time.OffsetDateTime;
import java.io.OutputStream;
import jakarta.ws.rs.Consumes;
import jakarta.ws.rs.DELETE;
import jakarta.ws.rs.GET;
import jakarta.ws.rs.POST;
import jakarta.ws.rs.PUT;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.PathParam;
import jakarta.ws.rs.QueryParam;
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
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Collectors;
/**
 *
 * @author surro
 */
@Path("/missions")
@Consumes({MediaType.APPLICATION_JSON})
@Produces({MediaType.APPLICATION_JSON})
public class MissionResource {
     private static volatile long SEQ = 1;
     private static final ConcurrentHashMap<Long, Mission> Database = new ConcurrentHashMap<>();
    
        @POST
        @Logged
        public Response Create(Mission data, @Context UriInfo uri){
            
            //Verifichiamo che la richiesta sia valida
            var Request = RequestResource.getStaticRequest(data.RequestId());
            if (Request == null){
                return Response.status(404).entity("no-Request-found").build();
            }
            if(!"ACTIVE".equalsIgnoreCase(Request.Status()) || !Request.Validated()){
                return Response.status(Response.Status.BAD_REQUEST).entity("The request has yet to be validated or is not ACTIVE").build();
            }
            
            //Recuperiamo gli operatori liberi
            List<Operator> Operators = OperatorResource.getAllOp();
            List<Operator> freeOps = Operators.stream()
                                     .filter(op -> op.Status().equalsIgnoreCase("FREE"))
                                     .collect(Collectors.toList());
            if (freeOps.isEmpty()){
                return Response.status(404).entity("no-free-Operators").build();
            }
            
            List<Long> freeOpIds = freeOps.stream()
                                           .map(Operator::id)
                                           .collect(Collectors.toList());
            
            //Creiamo la missione e salviamola sul DB fittizio
            long id = SEQ++;
            Mission m = new Mission(
                id,
                data.RequestId(),
                "IN_PROGRESS",
                freeOpIds,       // List<Long> di id
                OffsetDateTime.now(),
                null,
                null
            );
            
            Database.put(id, m);
            
            //Aggiorniamo lo stato della richiesta
            RequestResource.setStatus(data.RequestId(), "IN_PROGRESS");
            
            //Aggiorniamo lo stato degli operatori
            freeOps.forEach(op -> OperatorResource.setStatus(op.id(), "OCCUPIED"));
            
            URI location = uri.getAbsolutePathBuilder().path(String.valueOf(id)).build();
            return Response.created(location).entity(m).build();
        }
        
        @PUT
        @Logged
        @Path("{id}/close")
        public Response closeMission(@PathParam ("id") long id, @QueryParam("successLevel") int successLevel){
            Mission m = Database.get(id);
            if(m == null){
                return Response.status(404).entity("no-Mission-to-close").build();
            }
            if(m.Status().equalsIgnoreCase("CLOSED")){
                return Response.status(Response.Status.BAD_REQUEST).entity("Mission-already-closed").build();
            }
            if(successLevel<0 || successLevel>10){
                return Response.status(Response.Status.BAD_REQUEST).entity("successLevel-must-be-between-1-and-10").build();
            }
            List<Long> freeOpIds = m.OperatorIds();
            for (Long opId : freeOpIds) {
                OperatorResource.setStatus(opId, "FREE"); 
            }
            RequestResource.setStatus(m.RequestId(), "CLOSED");
            
            Mission cm = new Mission(m.Id(), m.RequestId(), "CLOSED", m.OperatorIds(), m.start(), java.time.OffsetDateTime.now(), successLevel);
            Database.remove(id);
            Database.put(cm.Id(), cm);
            
            return Response.noContent().build();
        }
        
        @GET
        @Logged
        @Path("/{id}")
        public Response getMission(@PathParam ("id") long id){
            Mission m = Database.get(id);
            if (m == null){
                return Response.status(404).entity("no-Mission-dound").build();
            }
            return Response.ok(m).build();

        }
    //metodo statico per tornare tutte le missioni
    public static List<Mission> getAllMissions() {
        return (Database.values().stream().collect(Collectors.toList()));
    }
        
      
        
        
        
}
