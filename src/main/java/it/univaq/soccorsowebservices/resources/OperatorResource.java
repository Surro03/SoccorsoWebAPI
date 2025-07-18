/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package it.univaq.soccorsowebservices.resources;
import it.univaq.soccorsowebservices.dto.Mission;
import it.univaq.soccorsowebservices.dto.Operator;
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
import java.util.stream.Collectors;
import java.util.concurrent.ConcurrentHashMap;
/**
 *
 * @author surro
 */
@Logged
@Path("/operators")
@Produces(MediaType.APPLICATION_JSON)
@Consumes(MediaType.APPLICATION_JSON)
public class OperatorResource {
    
    //simuliamo la persistenza sul DB
  private static volatile long SEQ = 1;
private static final ConcurrentHashMap<Long, Operator> Database 
    = new ConcurrentHashMap<>();

static {
    Database.put(SEQ, new Operator(SEQ++, "Nicolas", "Rossi", "FREE"));
    Database.put(SEQ, new Operator(SEQ++, "Alessandro", "Bianchi", "FREE"));
    Database.put(SEQ, new Operator(SEQ++, "Daniela", "Verdi", "BUSY"));
}
    
    
    
    //Lista generale di operatori in base allo status cercato
    @GET
    public Response getListOp(@QueryParam("status") String status){
        if (status != null && !status.isBlank()) {
            List<Operator> Operators = Database.values().stream()
                     .filter(op -> op.Status().equalsIgnoreCase(status))
                     .collect(Collectors.toList());
            if(!Operators.isEmpty()){
                return Response.ok(Operators).build();
            }
            else{
                return Response.status(404).entity("no-Operators-found").build();
            }
        }
        else {
             return Response.status(Response.Status.BAD_REQUEST).type("text/plain").entity("status query parameter must be specified").build();
        }
            
        
    }
    
    //Dettagli di un Operatore
    @GET
    @Path("/{id}")
    public Response getOpDetails(@PathParam("id") long id){
            Operator Op = Database.get(id);
            if(Op == null){
                return Response.status(404).entity("no-Operator-found").build();
           }
           else{
               return Response.ok(Op).build();
           }
        }
    
    //Metodo per tornare tutte le missioni a cui un operatore ha partecipato
    @GET
    @Logged
    @Path("/{opId}/missions")
    public Response getListOpMission(@PathParam ("opId") long opId){
        Operator Op = Database.values().stream().filter(op-> op.id() == opId).findFirst()
                      .orElse(null);
        if (Op == null){
            return Response.status(404).entity("no-Operator-found").build();
        }
        List<Mission> missions = MissionResource.getAllMissions().stream()
                                .filter(m -> m.OperatorIds().contains(opId))
                                .collect(Collectors.toList());
        if ( missions.isEmpty()){
            return Response.status(404).entity("the-Operator-did-not-partecipate-in-any-missions-yet").build();
        }
        return Response.ok(missions).build();

    }
   
   
    
    
    
    //Metodo per cambiare lo stato di un Operatore se inizia/finisce una missione. Essendo record verrà creato una nuova istanza con lo stesso id.
    //Se fosse stata un'entità standard bastava usare un setter
    public static void setStatus(long id, String Status){
        Operator Op = Database.get(id);
        Database.remove(Op.id());
        Database.put(id, new Operator(id, Op.Name(), Op.Surname(), Status));
    }
    
    //Metodo per tornare tutti gli Operatori
    public static List<Operator> getAllOp() {
        return (Database.values().stream().collect(Collectors.toList()));
    }
    
    
    
    
    

    
    
    
    
    
    
}
