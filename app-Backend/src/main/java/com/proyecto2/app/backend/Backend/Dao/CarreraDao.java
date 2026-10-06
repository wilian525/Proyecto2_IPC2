/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package com.proyecto2.app.backend.Backend.Dao;

import com.proyecto2.app.backend.Backend.Exception.daoException;
import com.proyecto2.app.backend.Backend.Model.Carrera;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.Collection;

/**
 *
 * @author wilian
 */
public class CarreraDao {
    
     private DBConnectionSingleton conexionDB;

    private static final String CREAR_TABLA = """
            CREATE TABLE IF NOT EXISTS CARRERA (
                id_carrera INT PRIMARY KEY AUTO_INCREMENT,
                nombre VARCHAR(100) NOT NULL,
                estado VARCHAR(20) NOT NULL,
                id_nivel INT NOT NULL,

                CONSTRAINT fk_carrera_nivel
                    FOREIGN KEY (id_nivel)
                    REFERENCES NIVEL(id_nivel)
            )
            """;

    private static final String INSERTAR = "INSERT INTO CARRERA (nombre, estado, id_nivel) VALUES (?, ?, ?)";
    private static final String CONSULTAR = "SELECT id_carrera, nombre, estado, id_nivel FROM CARRERA ORDER BY nombre";
    private static final String ACTUALIZAR = "UPDATE CARRERA SET nombre = ?, id_nivel = ? WHERE id_carrera = ?";
    private static final String ACTIVAR = "UPDATE CARRERA SET estado = 'ACTIVO' WHERE id_carrera = ?";
    private static final String DESACTIVAR = "UPDATE CARRERA SET estado = 'INACTIVO' WHERE id_carrera = ?";
    private static final String CONSULTAR_NIVEL =  "SELECT nombre FROM NIVEL WHERE id_nivel = ?";

    public CarreraDao() {
        this.conexionDB = DBConnectionSingleton.getInstance();
    }

    public void crearTabla() {
        Connection conexion = conexionDB.getConnection();
        Statement statement = null;

        try {
            statement = conexion.createStatement();
            statement.execute(CREAR_TABLA);

        } catch (SQLException e) {
            daoException.manejarError(e);
        } finally {
            cerrar(statement);
        }
    }

    public boolean insertar(Carrera carrera) {
        if (carrera == null) {
            return false;
        }

        if (carrera.getNombre() == null
                || carrera.getNombre().trim().isEmpty()) {
            return false;
        }

        if (carrera.getEstado() == null
                || carrera.getEstado().trim().isEmpty()) {
            return false;
        }

        if (carrera.getIdNivel() <= 0) {
            return false;
        }

  // carreras pertendes solo diversificado
        if (!esNivelDiversificado(carrera.getIdNivel())) {
            return false;
        }
        Connection conexion = conexionDB.getConnection();
        PreparedStatement ps = null;

        try {
            ps = conexion.prepareStatement(INSERTAR);
            ps.setString(1, carrera.getNombre().trim());
            ps.setString(2, carrera.getEstado().trim());
            ps.setInt(3, carrera.getIdNivel());

            return ps.executeUpdate() > 0;

        } catch (SQLException e) {
            daoException.manejarError(e);
            return false;
        } finally {
            cerrar(ps);
        }
    }

    public Collection<Carrera> consultar() {
        Collection<Carrera> carreras = new ArrayList<>();

        Connection conexion = conexionDB.getConnection();
        PreparedStatement ps = null;
        ResultSet rs = null;

        try {
            ps = conexion.prepareStatement(CONSULTAR);
            rs = ps.executeQuery();

            while (rs.next()) {
                carreras.add(construirCarrera(rs));
            }

        } catch (SQLException e) {
            daoException.manejarError(e);
        } finally {
            cerrar(rs);
            cerrar(ps);
        }

        return carreras;
    }

    public boolean actualizar(Carrera carrera) {

        if (carrera == null) {
            return false;
        }

        if (carrera.getIdCarrera() <= 0) {
            return false;
        }

        if (carrera.getNombre() == null || carrera.getNombre().trim().isEmpty()) {
            return false;
        }

        if (carrera.getIdNivel() <= 0) {
            return false;
        }

        if (!esNivelDiversificado(carrera.getIdNivel())) {
            return false;
        }

        Connection conexion = conexionDB.getConnection();
        PreparedStatement ps = null;

        try {
            ps = conexion.prepareStatement(ACTUALIZAR);
            ps.setString(1, carrera.getNombre().trim());
            ps.setInt(2, carrera.getIdNivel());
            ps.setInt(3, carrera.getIdCarrera());

            return ps.executeUpdate() > 0;

        } catch (SQLException e) {
            daoException.manejarError(e);
            return false;
        } finally {
            cerrar(ps);
        }
    }

    public boolean activar(int idCarrera) {

        if (idCarrera <= 0) {
            return false;
        }

        Connection conexion = conexionDB.getConnection();
        PreparedStatement ps = null;

        try {
            ps = conexion.prepareStatement(ACTIVAR);
            ps.setInt(1, idCarrera);

            return ps.executeUpdate() > 0;

        } catch (SQLException e) {
            daoException.manejarError(e);
            return false;
        } finally {
            cerrar(ps);
        }
    }

    public boolean desactivar(int idCarrera) {

        if (idCarrera <= 0) {
            return false;
        }
        Connection conexion = conexionDB.getConnection();
        PreparedStatement ps = null;

        try {
            ps = conexion.prepareStatement(DESACTIVAR);
            ps.setInt(1, idCarrera);

            return ps.executeUpdate() > 0;

        } catch (SQLException e) {
            daoException.manejarError(e);
            return false;
        } finally {
            cerrar(ps);
        }
    }

    private boolean esNivelDiversificado(int idNivel) {
        Connection conexion = conexionDB.getConnection();
        PreparedStatement ps = null;
        ResultSet rs = null;

        try {
            ps = conexion.prepareStatement(CONSULTAR_NIVEL);
            ps.setInt(1, idNivel);

            rs = ps.executeQuery();

            if (rs.next()) {

                String nombreNivel = rs.getString("nombre");
                return nombreNivel != null && nombreNivel.equalsIgnoreCase("Diversificado");
            }

        } catch (SQLException e) {
            daoException.manejarError(e);
        } finally {
            cerrar(rs);
            cerrar(ps);
        }
        return false;
    }

    private Carrera construirCarrera(ResultSet rs)throws SQLException {
        int idCarrera = rs.getInt("id_carrera");
        String nombre = rs.getString("nombre");
        String estado = rs.getString("estado");
        int idNivel = rs.getInt("id_nivel");

        return new Carrera( idCarrera, nombre, estado,idNivel);
    }

    private void cerrar(Statement statement) {
        if (statement != null) {
            try {
                statement.close();
            } catch (SQLException e) {
                daoException.manejarError(e);
            }
        }
    }

    private void cerrar(ResultSet resultSet) {
        if (resultSet != null) {
            try {
                resultSet.close();
            } catch (SQLException e) {
                daoException.manejarError(e);
            }
        }
    }
}
