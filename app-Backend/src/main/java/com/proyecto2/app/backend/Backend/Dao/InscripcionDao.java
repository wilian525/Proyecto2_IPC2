/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package com.proyecto2.app.backend.Backend.Dao;

import com.proyecto2.app.backend.Backend.Exception.daoException;
import com.proyecto2.app.backend.Backend.Model.Inscripcion;
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
public class InscripcionDao {

    private DBConnectionSingleton conexionDB;

    private static final String CREAR_TABLA = """
            CREATE TABLE IF NOT EXISTS INSCRIPCION (
                id_inscripcion INT PRIMARY KEY AUTO_INCREMENT,
                id_estudiante INT NOT NULL,
                id_ciclo INT NOT NULL,
                id_grado INT NOT NULL,
                id_seccion INT NOT NULL,

                CONSTRAINT fk_inscripcion_estudiante
                    FOREIGN KEY (id_estudiante)
                    REFERENCES ESTUDIANTE(id_estudiante),

                CONSTRAINT fk_inscripcion_ciclo
                    FOREIGN KEY (id_ciclo)
                    REFERENCES CICLO(id_ciclo),

                CONSTRAINT fk_inscripcion_grado
                    FOREIGN KEY (id_grado)
                    REFERENCES GRADO(id_grado),

                CONSTRAINT fk_inscripcion_seccion
                    FOREIGN KEY (id_seccion)
                    REFERENCES SECCION(id_seccion),

                CONSTRAINT uk_inscripcion_estudiante_ciclo
                    UNIQUE (id_estudiante, id_ciclo)
            )
            """;

    private static final String INSERTAR = "INSERT INTO INSCRIPCION (id_estudiante, id_ciclo, id_grado, id_seccion) VALUES (?, ?, ?, ?)";
    private static final String CONSULTAR = "SELECT id_inscripcion, id_estudiante, id_ciclo, id_grado, id_seccion FROM INSCRIPCION ORDER BY id_inscripcion";
    private static final String CONSULTAR_ESTUDIANTE= "SELECT estado FROM ESTUDIANTE WHERE id_estudiante = ?";
    private static final String CONSULTAR_CICLO= "SELECT estado FROM CICLO WHERE id_ciclo = ?";
    private static final String CONSULTAR_SECCION= "SELECT id_grado FROM SECCION WHERE id_seccion = ?";
    private static final String CONSULTAR_INSCRIPCION_EXISTENTE = "SELECT id_inscripcion FROM INSCRIPCION WHERE id_estudiante = ? AND id_ciclo = ?";

    public InscripcionDao() {
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

    public boolean insertar(Inscripcion inscripcion) {

        if (inscripcion == null) {
            return false;
        }

        if (inscripcion.getIdEstudiante() <= 0 || inscripcion.getIdCiclo() <= 0 || inscripcion.getIdGrado() <= 0 || inscripcion.getIdSeccion() <= 0) {
            return false;
        }

        if (!estudianteActivo(inscripcion.getIdEstudiante())) {
            return false;
        }

        if (!cicloActivo(inscripcion.getIdCiclo())) {
            return false;
        }

        if (!seccionPerteneceGrado(
                inscripcion.getIdSeccion(),
                inscripcion.getIdGrado())) {
            return false;
        }

        if (yaEstaInscrito(
                inscripcion.getIdEstudiante(),
                inscripcion.getIdCiclo())) {
            return false;
        }

        Connection conexion = conexionDB.getConnection();
        PreparedStatement ps = null;

        try {
            ps = conexion.prepareStatement(INSERTAR);

            ps.setInt(1, inscripcion.getIdEstudiante());
            ps.setInt(2, inscripcion.getIdCiclo());
            ps.setInt(3, inscripcion.getIdGrado());
            ps.setInt(4, inscripcion.getIdSeccion());

            return ps.executeUpdate() > 0;

        } catch (SQLException e) {
            daoException.manejarError(e);
            return false;
        } finally {
            cerrar(ps);
        }
    }

    public Collection<Inscripcion> consultar() {
        Collection<Inscripcion> inscripciones = new ArrayList<>();

        Connection conexion = conexionDB.getConnection();
        PreparedStatement ps = null;
        ResultSet rs = null;

        try {
            ps = conexion.prepareStatement(CONSULTAR);
            rs = ps.executeQuery();

            while (rs.next()) {
                inscripciones.add(construirInscripcion(rs));
            }

        } catch (SQLException e) {
            daoException.manejarError(e);
        } finally {
            cerrar(rs);
            cerrar(ps);
        }

        return inscripciones;
    }

    private boolean estudianteActivo(int idEstudiante) {
        Connection conexion = conexionDB.getConnection();
        PreparedStatement ps = null;
        ResultSet rs = null;

        try {
            ps = conexion.prepareStatement(CONSULTAR_ESTUDIANTE);
            ps.setInt(1, idEstudiante);

            rs = ps.executeQuery();

            if (rs.next()) {
                String estado = rs.getString("estado");
                return estado != null&& estado.equalsIgnoreCase("ACTIVO");
            }

        } catch (SQLException e) {
            daoException.manejarError(e);
        } finally {
            cerrar(rs);
            cerrar(ps);
        }
        return false;
    }

    private boolean cicloActivo(int idCiclo) {
        Connection conexion = conexionDB.getConnection();
        PreparedStatement ps = null;
        ResultSet rs = null;

        try {
            ps = conexion.prepareStatement(CONSULTAR_CICLO);
            ps.setInt(1, idCiclo);
            rs = ps.executeQuery();

            if (rs.next()) {
                String estado = rs.getString("estado");
                return estado != null && estado.equalsIgnoreCase("ACTIVO");
            }

        } catch (SQLException e) {
            daoException.manejarError(e);
        } finally {
            cerrar(rs);
            cerrar(ps);
        }
        return false;
    }

    private boolean seccionPerteneceGrado(int idSeccion, int idGrado) {
        Connection conexion = conexionDB.getConnection();
        PreparedStatement ps = null;
        ResultSet rs = null;

        try {
            ps = conexion.prepareStatement(CONSULTAR_SECCION);
            ps.setInt(1, idSeccion);

            rs = ps.executeQuery();

            if (rs.next()) {
                return rs.getInt("id_grado") == idGrado;
            }

        } catch (SQLException e) {
            daoException.manejarError(e);
        } finally {
            cerrar(rs);
            cerrar(ps);
        }
        return false;
    }

    private boolean yaEstaInscrito( int idEstudiante, int idCiclo) {
        Connection conexion = conexionDB.getConnection();
        PreparedStatement ps = null;
        ResultSet rs = null;

        try {
            ps = conexion.prepareStatement(CONSULTAR_INSCRIPCION_EXISTENTE);

            ps.setInt(1, idEstudiante);
            ps.setInt(2, idCiclo);
            rs = ps.executeQuery();

            return rs.next();

        } catch (SQLException e) {
            daoException.manejarError(e);
        } finally {
            cerrar(rs);
            cerrar(ps);
        }
        return false;
    }

    private Inscripcion construirInscripcion(ResultSet rs)throws SQLException {
        int idInscripcion = rs.getInt("id_inscripcion");
        int idEstudiante = rs.getInt("id_estudiante");
        int idCiclo = rs.getInt("id_ciclo");
        int idGrado = rs.getInt("id_grado");
        int idSeccion = rs.getInt("id_seccion");

        return new Inscripcion( idInscripcion, idEstudiante, idCiclo, idGrado, idSeccion);
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
