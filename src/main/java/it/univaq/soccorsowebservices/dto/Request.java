package it.univaq.soccorsowebservices.dto;

/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Record.java to edit this template
 */
import com.fasterxml.jackson.annotation.JsonFormat;
import java.time.OffsetDateTime;
/**
 *
 * @author surro
 */
public record Request( 
    Long id,
    String Status,  //"ACTIVE", "IN_PROGRESS", "CLOSED", "IGNORED"
    Integer SuccessLevel,
    String Type,
    String Location,
    
    @JsonFormat(shape = JsonFormat.Shape.STRING,pattern = "yyyy-MM-dd HH:mm:ss")
    OffsetDateTime RequestTime,
    
    Boolean Validated
    ) {
}
