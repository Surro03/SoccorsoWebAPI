/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package it.univaq.soccorsowebservices.security;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;





public class AuthMap {

    private static final Map<String, String> tokens = new ConcurrentHashMap<>();
       private static final Map<String,String> role  = new ConcurrentHashMap<>();

    static {
        role.put("admin",    "admin");
        role.put("operator", "operator");
    }

    public static void saveToken(String token, String username) {
        tokens.put(token, username);
    }

    public static boolean isTokenValid(String token) {
        return tokens.containsKey(token);
    }

    public static void removeToken(String token) {
        tokens.remove(token);
    }
    
    public static String getUsernameFromToken(String token){
        return tokens.get(token);
    }
    
    public static String getRoleForUser(String user) {
        return role.get(user);
    }
}

