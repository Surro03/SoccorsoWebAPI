package it.univaq.soccorsowebservices.entities;

/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */

/**
 *
 * @author surro
 */
public class UserEntity {
    
    Long id;
    String Name;
    String Surname;
    String email;
    String Status;  //per indicare se l'operatore attualmente è libero (FREE) o impegnato (OCCUPIED)
    
    
    public Long getId() { return id; }
    public String getEmail() { return email; }
    public String getName() { return Name; }
    public String getSurname() { return Surname; }
    public String getStatus() {return Status;}
}
