/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package com.proyecto2.app.backend.Backend.ConexionDB;

import java.sql.Connection;
import java.sql.SQLException;
import javax.naming.InitialContext;
import javax.naming.NamingException;
import javax.sql.DataSource;

/**
 *
 * @author wilian
 */
public class ConexionDB {

    private static final String JNDI_NAME = "java:comp/env/jdbc/SistemaEscuelaDBPool";

    private static ConexionDB instance;

    private DataSource dataSource;

    private ConexionDB() {
        try {
            dataSource = (DataSource) new InitialContext().lookup(JNDI_NAME);
        } catch (NamingException e) {
            throw new IllegalStateException(
                    "No se pudo resolver el DataSource JNDI: " + JNDI_NAME, e
            );
        }
    }

    public Connection getConnection() throws SQLException {
        return dataSource.getConnection();
    }

    public static ConexionDB getInstance() {
        if (instance == null) {
            instance = new ConexionDB();
        }

        return instance;
    }
}
