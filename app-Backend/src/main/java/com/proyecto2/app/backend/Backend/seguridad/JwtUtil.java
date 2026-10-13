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
import java.util.Date;
import java.util.Locale;
import javax.crypto.SecretKey;

/**
 *
 * @author wilian
 */
public class JwtUtil {

    private static final String EMISOR = "app-Backend";
    private static final long TIEMPO_EXPIRACION = 2 * 60 * 60 * 1000;
    private final SecretKey clave;

    public JwtUtil() {
        String claveBase64 = System.getenv("JWT_SECRET");

        if (claveBase64 == null || claveBase64.trim().isEmpty()) {
            throw new IllegalStateException("No se encontró la variable de entorno JWT_SECRET");
        }

        byte[] claveBytes = Decoders.BASE64.decode(claveBase64);
        clave = Keys.hmacShaKeyFor(claveBytes);
    }

    public String generarToken(int idUsuario, String rol) {
        if (idUsuario <= 0) {
            throw new IllegalArgumentException("El id del usuario no es válido");
        }
        if (rol == null || rol.trim().isEmpty()) {
            throw new IllegalArgumentException("El rol no puede estar vacío");
        }

        Date fechaActual = new Date();
        Date fechaExpiracion = new Date(fechaActual.getTime() + TIEMPO_EXPIRACION);

        return Jwts.builder()
                .issuer(EMISOR)
                .subject(String.valueOf(idUsuario))
                .claim("rol", rol.trim().toUpperCase(Locale.ROOT))
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
            Claims claims = obtenerClaims(token);
            validarIdUsuario(claims);
            validarRol(claims);
            return true;
        } catch (JwtException | IllegalArgumentException e) {
            return false;
        }
    }

    public int obtenerIdUsuario(String token) {
        return validarIdUsuario(obtenerClaims(token));
    }

    public String obtenerRol(String token) {
        return validarRol(obtenerClaims(token));
    }

    public Date obtenerFechaExpiracion(String token) {
        return obtenerClaims(token).getExpiration();
    }

    private Claims obtenerClaims(String token) {
        if (token == null || token.trim().isEmpty()) {
            throw new IllegalArgumentException("El token no es válido");
        }

        return Jwts.parser()
                .requireIssuer(EMISOR)
                .verifyWith(clave)
                .build()
                .parseSignedClaims(token.trim())
                .getPayload();
    }

    private int validarIdUsuario(Claims claims) {
        String id = claims.getSubject();
        if (id == null || id.trim().isEmpty()) {
            throw new IllegalArgumentException("El token no contiene id de usuario");
        }

        try {
            int idUsuario = Integer.parseInt(id);
            if (idUsuario <= 0) {
                throw new IllegalArgumentException("El id del usuario contenido en el token no es válido");
            }
            return idUsuario;
        } catch (NumberFormatException e) {
            throw new IllegalArgumentException("El id del usuario contenido en el token no es válido", e);
        }
    }

    private String validarRol(Claims claims) {
        String rol = claims.get("rol", String.class);
        if (rol == null || rol.trim().isEmpty()) {
            throw new IllegalArgumentException("El token no contiene rol");
        }
        return rol.trim().toUpperCase(Locale.ROOT);
    }
}
