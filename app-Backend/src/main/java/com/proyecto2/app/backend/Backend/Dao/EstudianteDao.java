/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package com.proyecto2.app.backend.Backend.Dao;

import com.proyecto2.app.backend.Backend.ConexionDB.ConexionDB;
import com.proyecto2.app.backend.Backend.Exception.daoException;
import com.proyecto2.app.backend.Backend.Model.Estudiante;
import java.sql.Connection;
import java.sql.Date;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.sql.Types;
import java.util.ArrayList;
import java.util.Collection;

/**
 *
 * @author wilian
 */
public class EstudianteDao {

    private ConexionDB conexionDB;

    private static final String CREAR_TABLA = """
            CREATE TABLE IF NOT EXISTS ESTUDIANTE (
                id_estudiante INT PRIMARY KEY AUTO_INCREMENT,
                nombre VARCHAR(100) NOT NULL,
                apellido VARCHAR(100) NOT NULL,
                fecha_nacimiento DATE NOT NULL,
                direccion VARCHAR(255),
                telefono VARCHAR(30),
                correo VARCHAR(150),
                informacion_medica VARCHAR(500),
                datos_encargados VARCHAR(500),
                estado VARCHAR(20) NOT NULL,
                id_usuario INT UNIQUE,

                CONSTRAINT fk_estudiante_usuario
                    FOREIGN KEY (id_usuario)
                    REFERENCES USUARIO(id_usuario)
            )
            """;

    private static final String INSERTAR
            = "INSERT INTO ESTUDIANTE (nombre, apellido, fecha_nacimiento, direccion, telefono, correo, informacion_medica, datos_encargados, estado, id_usuario) "
            + "VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?)";
    private static final String CONSULTAR
            = "SELECT id_estudiante, nombre, apellido, fecha_nacimiento, direccion, telefono, correo, informacion_medica, datos_encargados, estado, id_usuario "
            + "FROM ESTUDIANTE ORDER BY apellido, nombre";
    private static final String ACTUALIZAR
            = "UPDATE ESTUDIANTE SET nombre = ?, apellido = ?, fecha_nacimiento = ?, direccion = ?, telefono = ?, correo = ?, informacion_medica = ?, datos_encargados = ?, id_usuario = ? "
            + "WHERE id_estudiante = ?";
    private static final String CAMBIAR_ESTADO = "UPDATE ESTUDIANTE SET estado = ? WHERE id_estudiante = ?";
    private static final String CONSULTAR_USUARIO = "SELECT id_usuario FROM USUARIO WHERE id_usuario = ?";

    public EstudianteDao() {
        this.conexionDB = ConexionDB.getInstance();
    }

    public void crearTabla() {
        try (Connection conexion = conexionDB.getConnection()) {
            crearTabla(conexion);
        } catch (SQLException e) {
            daoException.manejarError(e);
        }
    }

    public void crearTabla(Connection conexion) {
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

    public boolean insertar(Estudiante estudiante) {
        try (Connection conexion = conexionDB.getConnection()) {
            return insertar(estudiante, conexion);
        } catch (SQLException e) {
            daoException.manejarError(e);
            return false;
        }
    }

    public boolean insertar(Estudiante estudiante, Connection conexion) {

        if (estudiante == null) {
            return false;
        }

        if (estudiante.getNombre() == null || estudiante.getNombre().trim().isEmpty()) {
            return false;
        }

        if (estudiante.getApellido() == null || estudiante.getApellido().trim().isEmpty()) {
            return false;
        }

        if (estudiante.getFechaNacimiento() == null) {
            return false;
        }

        if (!estadoValido(estudiante.getEstado())) {
            return false;
        }

        if (estudiante.getIdUsuario() > 0 && !existeUsuario(estudiante.getIdUsuario(), conexion)) {
            return false;
        }

        PreparedStatement ps = null;

        try {
            ps = conexion.prepareStatement(INSERTAR);
            ps.setString(1, estudiante.getNombre().trim());
            ps.setString(2, estudiante.getApellido().trim());
            ps.setDate(3, Date.valueOf(estudiante.getFechaNacimiento()));
            ps.setString(4, estudiante.getDireccion());
            ps.setString(5, estudiante.getTelefono());
            ps.setString(6, estudiante.getCorreo());
            ps.setString(7, estudiante.getInformacionMedica());
            ps.setString(8, estudiante.getDatosEncargados());
            ps.setString(9, estudiante.getEstado().toUpperCase());

            if (estudiante.getIdUsuario() > 0) {
                ps.setInt(10, estudiante.getIdUsuario());
            } else {
                ps.setNull(10, Types.INTEGER);
            }

            return ps.executeUpdate() > 0;
        } catch (SQLException e) {
            daoException.manejarError(e);
            return false;
        } finally {
            cerrar(ps);
        }
    }

    public Collection<Estudiante> consultar() {
        try (Connection conexion = conexionDB.getConnection()) {
            return consultar(conexion);
        } catch (SQLException e) {
            daoException.manejarError(e);
            return new ArrayList<>();
        }
    }

    public Collection<Estudiante> consultar(Connection conexion) {

        Collection<Estudiante> estudiantes = new ArrayList<>();
        PreparedStatement ps = null;
        ResultSet rs = null;

        try {
            ps = conexion.prepareStatement(CONSULTAR);
            rs = ps.executeQuery();

            while (rs.next()) {
                estudiantes.add(construirEstudiante(rs));
            }
        } catch (SQLException e) {
            daoException.manejarError(e);
        } finally {
            cerrar(rs);
            cerrar(ps);
        }

        return estudiantes;
    }

    public boolean actualizar(Estudiante estudiante) {
        try (Connection conexion = conexionDB.getConnection()) {
            return actualizar(estudiante, conexion);
        } catch (SQLException e) {
            daoException.manejarError(e);
            return false;
        }
    }

    public boolean actualizar(Estudiante estudiante, Connection conexion) {

        if (estudiante == null) {
            return false;
        }

        if (estudiante.getIdEstudiante() <= 0) {
            return false;
        }

        if (estudiante.getNombre() == null || estudiante.getNombre().trim().isEmpty()) {
            return false;
        }

        if (estudiante.getApellido() == null || estudiante.getApellido().trim().isEmpty()) {
            return false;
        }

        if (estudiante.getFechaNacimiento() == null) {
            return false;
        }

        if (estudiante.getIdUsuario() > 0 && !existeUsuario(estudiante.getIdUsuario(), conexion)) {
            return false;
        }

        PreparedStatement ps = null;

        try {
            ps = conexion.prepareStatement(ACTUALIZAR);
            ps.setString(1, estudiante.getNombre().trim());
            ps.setString(2, estudiante.getApellido().trim());
            ps.setDate(3, Date.valueOf(estudiante.getFechaNacimiento()));
            ps.setString(4, estudiante.getDireccion());
            ps.setString(5, estudiante.getTelefono());
            ps.setString(6, estudiante.getCorreo());
            ps.setString(7, estudiante.getInformacionMedica());
            ps.setString(8, estudiante.getDatosEncargados());

            if (estudiante.getIdUsuario() > 0) {
                ps.setInt(9, estudiante.getIdUsuario());
            } else {
                ps.setNull(9, Types.INTEGER);
            }

            ps.setInt(10, estudiante.getIdEstudiante());

            return ps.executeUpdate() > 0;
        } catch (SQLException e) {
            daoException.manejarError(e);
            return false;
        } finally {
            cerrar(ps);
        }
    }

    public boolean cambiarEstado(int idEstudiante, String estado) {
        try (Connection conexion = conexionDB.getConnection()) {
            return cambiarEstado(idEstudiante, estado, conexion);
        } catch (SQLException e) {
            daoException.manejarError(e);
            return false;
        }
    }

    public boolean cambiarEstado(int idEstudiante, String estado, Connection conexion) {

        if (idEstudiante <= 0) {
            return false;
        }

        if (!estadoValido(estado)) {
            return false;
        }

        PreparedStatement ps = null;

        try {
            ps = conexion.prepareStatement(CAMBIAR_ESTADO);
            ps.setString(1, estado.toUpperCase());
            ps.setInt(2, idEstudiante);

            return ps.executeUpdate() > 0;
        } catch (SQLException e) {
            daoException.manejarError(e);
            return false;
        } finally {
            cerrar(ps);
        }
    }

    private boolean existeUsuario(int idUsuario) {
        try (Connection conexion = conexionDB.getConnection()) {
            return existeUsuario(idUsuario, conexion);
        } catch (SQLException e) {
            daoException.manejarError(e);
            return false;
        }
    }

    private boolean existeUsuario(int idUsuario, Connection conexion) {

        PreparedStatement ps = null;
        ResultSet rs = null;

        try {
            ps = conexion.prepareStatement(CONSULTAR_USUARIO);
            ps.setInt(1, idUsuario);
            rs = ps.executeQuery();
            return rs.next();
        } catch (SQLException e) {
            daoException.manejarError(e);
            return false;
        } finally {
            cerrar(rs);
            cerrar(ps);
        }
    }

    private boolean estadoValido(String estado) {
        if (estado == null) {
            return false;
        }
        return estado.equalsIgnoreCase("ACTIVO") || estado.equalsIgnoreCase("INACTIVO") || estado.equalsIgnoreCase("GRADUADO");
    }

    private Estudiante construirEstudiante(ResultSet rs) throws SQLException {
        int idEstudiante = rs.getInt("id_estudiante");
        String nombre = rs.getString("nombre");
        String apellido = rs.getString("apellido");
        Date fechaNacimiento = rs.getDate("fecha_nacimiento");
        String direccion = rs.getString("direccion");
        String telefono = rs.getString("telefono");
        String correo = rs.getString("correo");
        String informacionMedica = rs.getString("informacion_medica");
        String datosEncargados = rs.getString("datos_encargados");
        String estado = rs.getString("estado");
        int idUsuario = rs.getInt("id_usuario");

        if (rs.wasNull()) {
            idUsuario = 0;
        }

        return new Estudiante(idEstudiante, nombre, apellido, fechaNacimiento.toLocalDate(), direccion, telefono, correo, informacionMedica, datosEncargados, estado, idUsuario);
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
