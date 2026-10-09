/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package com.proyecto2.app.backend.Backend.seguridad;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.JwtException;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.io.Decoders;
import io.jsonwebtoken.security.Keys;

import javax.crypto.SecretKey;
import java.util.Date;

/**
 *
 * @author wilian
 */
public class JwtUtil {
    
      private static final String EMISOR = "app-Backend";

    // 2 horas
    private static final long TIEMPO_EXPIRACION = 2 * 60 * 60 * 1000;

    private final SecretKey clave;

    public JwtUtil() {

        String claveBase64 = System.getenv("JWT_SECRET");

        if (claveBase64 == null
                || claveBase64.trim().isEmpty()) {

            throw new IllegalStateException(
                    "No se encontró la variable de entorno JWT_SECRET"
            );
        }

        byte[] claveBytes = Decoders.BASE64.decode(claveBase64);

        this.clave = Keys.hmacShaKeyFor(claveBytes);
    }

    public String generarToken(
            int idUsuario,
            String rol) {

        if (idUsuario <= 0) {
            throw new IllegalArgumentException(
                    "El id del usuario no es válido"
            );
        }

        if (rol == null || rol.trim().isEmpty()) {
            throw new IllegalArgumentException(
                    "El rol no puede estar vacío"
            );
        }

        Date fechaActual = new Date();

        Date fechaExpiracion = new Date(
                fechaActual.getTime() + TIEMPO_EXPIRACION
        );

        return Jwts.builder()
                .issuer(EMISOR)
                .subject(String.valueOf(idUsuario))
                .claim("rol", rol.trim())
                .issuedAt(fechaActual)
                .expiration(fechaExpiracion)
                .signWith(clave)
                .compact();
    }

    public boolean validarToken(String token) {

        if (token == null || token.trim().isEmpty()) {
            return false;
        }

        try {

            obtenerClaims(token);

            return true;

        } catch (JwtException
                 | IllegalArgumentException e) {

            return false;
        }
    }

    public int obtenerIdUsuario(String token) {

        Claims claims = obtenerClaims(token);

        String idUsuario = claims.getSubject();

        if (idUsuario == null) {
            throw new IllegalArgumentException(
                    "El token no contiene id de usuario"
            );
        }

        try {

            return Integer.parseInt(idUsuario);

        } catch (NumberFormatException e) {

            throw new IllegalArgumentException(
                    "El id del usuario contenido en el token no es válido"
            );
        }
    }

    public String obtenerRol(String token) {

        Claims claims = obtenerClaims(token);

        String rol = claims.get(
                "rol",
                String.class
        );

        if (rol == null || rol.trim().isEmpty()) {
            throw new IllegalArgumentException(
                    "El token no contiene rol"
            );
        }

        return rol;
    }

    public Date obtenerFechaExpiracion(String token) {

        Claims claims = obtenerClaims(token);

        return claims.getExpiration();
    }

    private Claims obtenerClaims(String token) {

        return Jwts.parser()
                .requireIssuer(EMISOR)
                .verifyWith(clave)
                .build()
                .parseSignedClaims(token)
                .getPayload();
    }
}
