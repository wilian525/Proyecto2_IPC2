/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package com.proyecto2.app.backend.Backend.Dao;

import com.proyecto2.app.backend.Backend.ConexionDB.ConexionDB;
import com.proyecto2.app.backend.Backend.Exception.daoException;
import com.proyecto2.app.backend.Backend.Model.Asistencia;
import java.sql.Connection;
import java.sql.Date;
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
public class AsistenciaDao {

    private ConexionDB conexionDB;

    private static final String CREAR_TABLA = """
            CREATE TABLE IF NOT EXISTS ASISTENCIA (
                id_asistencia INT PRIMARY KEY AUTO_INCREMENT,
                id_asignacion INT NOT NULL,
                id_inscripcion INT NOT NULL,
                fecha DATE NOT NULL,
                estado VARCHAR(20) NOT NULL,

                CONSTRAINT fk_asistencia_asignacion
                    FOREIGN KEY (id_asignacion)
                    REFERENCES ASIGNACION(id_asignacion),

                CONSTRAINT fk_asistencia_inscripcion
                    FOREIGN KEY (id_inscripcion)
                    REFERENCES INSCRIPCION(id_inscripcion)
            )
            """;

    private static final String INSERTAR = "INSERT INTO ASISTENCIA (id_asignacion, id_inscripcion, fecha, estado) VALUES (?, ?, ?, ?)";
    private static final String CONSULTAR = "SELECT id_asistencia, id_asignacion, id_inscripcion, fecha, estado FROM ASISTENCIA ORDER BY fecha DESC";
    private static final String ACTUALIZAR = "UPDATE ASISTENCIA SET id_asignacion = ?, id_inscripcion = ?, fecha = ?, estado = ? WHERE id_asistencia = ?";

    public AsistenciaDao() {
        this.conexionDB = ConexionDB.getInstance();
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

    public boolean insertar(Asistencia asistencia) {
        if (asistencia == null) {
            return false;
        }
        if (asistencia.getIdAsignacion() <= 0  || asistencia.getIdInscripcion() <= 0) {
            return false;
        }
        if (asistencia.getFecha() == null) {
            return false;
        }
        if (asistencia.getEstado() == null || asistencia.getEstado().trim().isEmpty()) {
            return false;
        }

        Connection conexion = conexionDB.getConnection();
        PreparedStatement ps = null;

        try {
            ps = conexion.prepareStatement(INSERTAR);

            ps.setInt(1, asistencia.getIdAsignacion());
            ps.setInt(2, asistencia.getIdInscripcion());
            ps.setDate( 3, Date.valueOf(asistencia.getFecha()));
            ps.setString(4, asistencia.getEstado().trim());

            return ps.executeUpdate() > 0;

        } catch (SQLException e) {
            daoException.manejarError(e);
            return false;
        } finally {
            cerrar(ps);
        }
    }

    public Collection<Asistencia> consultar() {
        Collection<Asistencia> asistencias = new ArrayList<>();

        Connection conexion = conexionDB.getConnection();
        PreparedStatement ps = null;
        ResultSet rs = null;

        try {
            ps = conexion.prepareStatement(CONSULTAR);
            rs = ps.executeQuery();

            while (rs.next()) {
                asistencias.add(construirAsistencia(rs));
            }

        } catch (SQLException e) {
            daoException.manejarError(e);
        } finally {
            cerrar(rs);
            cerrar(ps);
        }
        return asistencias;
    }

    public boolean actualizar(Asistencia asistencia) {
        if (asistencia == null) {
            return false;
        }
        if (asistencia.getIdAsistencia() <= 0  || asistencia.getIdAsignacion() <= 0 || asistencia.getIdInscripcion() <= 0) {
            return false;
        }

        if (asistencia.getFecha() == null) {
            return false;
        }
        if (asistencia.getEstado() == null || asistencia.getEstado().trim().isEmpty()) {
            return false;
        }

        Connection conexion = conexionDB.getConnection();
        PreparedStatement ps = null;

        try {
            ps = conexion.prepareStatement(ACTUALIZAR);

            ps.setInt(1, asistencia.getIdAsignacion());
            ps.setInt(2, asistencia.getIdInscripcion());
            ps.setDate( 3, Date.valueOf(asistencia.getFecha()));
            ps.setString(4, asistencia.getEstado().trim());
            ps.setInt(5, asistencia.getIdAsistencia());

            return ps.executeUpdate() > 0;

        } catch (SQLException e) {
            daoException.manejarError(e);
            return false;
        } finally {
            cerrar(ps);
        }
    }

    private Asistencia construirAsistencia(ResultSet rs) throws SQLException {
        return new Asistencia(rs.getInt("id_asistencia"),  rs.getInt("id_asignacion"), rs.getInt("id_inscripcion"),  rs.getDate("fecha").toLocalDate(),  rs.getString("estado") );
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
