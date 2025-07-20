/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Record.java to edit this template
 */
package it.univaq.soccorsowebservices.dto;
import com.fasterxml.jackson.annotation.JsonFormat;
import java.util.List;
import java.time.OffsetDateTime;

/**
 *
 * @author surro
 */

public record Mission(
        
        Long Id,
        Long RequestId,
        String Status,   //CLOSED, IN_PROGRESS
        List<Long> OperatorIds,
        @JsonFormat(shape = JsonFormat.Shape.STRING,
                    pattern = "yyyy-MM-dd HH:mm:ss")
        OffsetDateTime start,
        
        @JsonFormat(shape = JsonFormat.Shape.STRING,pattern = "yyyy-MM-dd HH:mm:ss")
        OffsetDateTime end,
        
        Integer SuccessLevel
        ) {
}
