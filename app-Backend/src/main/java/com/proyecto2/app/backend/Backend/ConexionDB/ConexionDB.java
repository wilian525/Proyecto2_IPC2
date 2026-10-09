/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package com.proyecto2.app.backend.Backend.ConexionDB;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

/**
 *
 * @author wilian
 */
public class ConexionDB {
    
    
   private static final String IP = "localhost";
    private static final int PUERTO = 3306;
    private static final String SCHEMA = "sistema_escuela";
    public static final String USER_NAME = "wilian";
    public static final String PASSWORD = "Clasic.Mysql";
    private static final String URL = "jdbc:mysql://" + IP + ":" + PUERTO + "/" + SCHEMA;

    private static ConexionDB instance;

    private Connection connection;

    private ConexionDB() {
        try {
            connection = DriverManager.getConnection(URL, USER_NAME, PASSWORD);
        } catch (SQLException e) {
            // manejamos la exception
            System.out.println("Error al conectarse");
            e.printStackTrace();
        }
    }

    public Connection getConnection() {
        return connection;
    }

    public static ConexionDB getInstance() {
        if (instance == null) {
            instance = new ConexionDB();
        }
        return instance;
    }
}

    

