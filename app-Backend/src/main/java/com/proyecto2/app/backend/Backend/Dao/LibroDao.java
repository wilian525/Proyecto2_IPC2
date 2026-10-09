/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package com.proyecto2.app.backend.Backend.Dao;

import com.proyecto2.app.backend.Backend.ConexionDB.ConexionDB;
import com.proyecto2.app.backend.Backend.Exception.daoException;
import com.proyecto2.app.backend.Backend.Model.Libro;
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
public class LibroDao {
    
    private static final String CREAR_TABLA = """
            CREATE TABLE IF NOT EXISTS LIBRO (
                id_libro INT PRIMARY KEY AUTO_INCREMENT,
                titulo VARCHAR(150) NOT NULL,
                autor VARCHAR(150) NOT NULL,
                editorial VARCHAR(150),
                cantidad_disponible INT NOT NULL,
                estado VARCHAR(20) NOT NULL
            )
            """;

    private static final String INSERTAR ="INSERT INTO LIBRO (titulo, autor, editorial, cantidad_disponible, estado) VALUES (?, ?, ?, ?, ?)";
    private static final String CONSULTAR ="SELECT id_libro, titulo, autor, editorial, cantidad_disponible, estado FROM LIBRO ORDER BY titulo";
    private static final String ACTUALIZAR ="UPDATE LIBRO SET titulo = ?, autor = ?, editorial = ?, cantidad_disponible = ? WHERE id_libro = ?";
    private static final String ACTIVAR ="UPDATE LIBRO SET estado = 'ACTIVO' WHERE id_libro = ?";
    private static final String DESACTIVAR ="UPDATE LIBRO SET estado = 'INACTIVO' WHERE id_libro = ?";
    private static final String ELIMINAR ="DELETE FROM LIBRO WHERE id_libro = ?";

    public void crearTabla() {

        Connection conexion = null;
        Statement statement = null;

        try {

            conexion = ConexionDB.obtenerConexion();

            statement = conexion.createStatement();
            statement.execute(CREAR_TABLA);

        } catch (SQLException e) {
            daoException.manejarError(e);
        } finally {
            cerrar(statement);
            cerrar(conexion);
        }
    }

    public boolean insertar(Libro libro) {

        if (libro == null) {
            return false;
        }

        if (libro.getTitulo() == null || libro.getTitulo().trim().isEmpty()) {
            return false;
        }

        if (libro.getAutor() == null|| libro.getAutor().trim().isEmpty()) {
            return false;
        }

        if (libro.getCantidadDisponible() < 0) {
            return false;
        }

        Connection conexion = null;
        PreparedStatement ps = null;

        try {

            conexion = ConexionDB.obtenerConexion();

            ps = conexion.prepareStatement(INSERTAR);

            ps.setString(1, libro.getTitulo().trim());
            ps.setString(2, libro.getAutor().trim());
            ps.setString(3, libro.getEditorial());
            ps.setInt(4, libro.getCantidadDisponible());
            ps.setString(5, libro.getEstado());

            return ps.executeUpdate() > 0;

        } catch (SQLException e) {
            daoException.manejarError(e);
            return false;
        } finally {
            cerrar(ps);
            cerrar(conexion);
        }
    }

    public Collection<Libro> consultar() {

        Collection<Libro> libros = new ArrayList<>();

        Connection conexion = null;
        PreparedStatement ps = null;
        ResultSet rs = null;

        try {

            conexion = ConexionDB.obtenerConexion();

            ps = conexion.prepareStatement(CONSULTAR);
            rs = ps.executeQuery();

            while (rs.next()) {
                libros.add(construirLibro(rs));
            }

        } catch (SQLException e) {
            daoException.manejarError(e);
        } finally {
            cerrar(rs);
            cerrar(ps);
            cerrar(conexion);
        }

        return libros;
    }

    public boolean actualizar(Libro libro) {

        if (libro == null) {
            return false;
        }

        if (libro.getIdLibro() <= 0) {
            return false;
        }

        if (libro.getTitulo() == null
                || libro.getTitulo().trim().isEmpty()) {
            return false;
        }

        if (libro.getAutor() == null
                || libro.getAutor().trim().isEmpty()) {
            return false;
        }

        if (libro.getCantidadDisponible() < 0) {
            return false;
        }

        Connection conexion = null;
        PreparedStatement ps = null;

        try {

            conexion = ConexionDB.obtenerConexion();

            ps = conexion.prepareStatement(ACTUALIZAR);

            ps.setString(1, libro.getTitulo().trim());
            ps.setString(2, libro.getAutor().trim());
            ps.setString(3, libro.getEditorial());
            ps.setInt(4, libro.getCantidadDisponible());
            ps.setInt(5, libro.getIdLibro());

            return ps.executeUpdate() > 0;

        } catch (SQLException e) {
            daoException.manejarError(e);
            return false;
        } finally {
            cerrar(ps);
            cerrar(conexion);
        }
    }

    public boolean activar(int idLibro) {

        if (idLibro <= 0) {
            return false;
        }

        Connection conexion = null;
        PreparedStatement ps = null;

        try {

            conexion = ConexionDB.obtenerConexion();

            ps = conexion.prepareStatement(ACTIVAR);
            ps.setInt(1, idLibro);

            return ps.executeUpdate() > 0;

        } catch (SQLException e) {
            daoException.manejarError(e);
            return false;
        } finally {
            cerrar(ps);
            cerrar(conexion);
        }
    }

    public boolean desactivar(int idLibro) {

        if (idLibro <= 0) {
            return false;
        }

        Connection conexion = null;
        PreparedStatement ps = null;

        try {

            conexion = ConexionDB.obtenerConexion();

            ps = conexion.prepareStatement(DESACTIVAR);
            ps.setInt(1, idLibro);

            return ps.executeUpdate() > 0;

        } catch (SQLException e) {
            daoException.manejarError(e);
            return false;
        } finally {
            cerrar(ps);
            cerrar(conexion);
        }
    }

    public boolean eliminar(int idLibro) {

        if (idLibro <= 0) {
            return false;
        }

        Connection conexion = null;
        PreparedStatement ps = null;

        try {

            conexion = ConexionDB.obtenerConexion();

            ps = conexion.prepareStatement(ELIMINAR);
            ps.setInt(1, idLibro);

            return ps.executeUpdate() > 0;

        } catch (SQLException e) {
            daoException.manejarError(e);
            return false;
        } finally {
            cerrar(ps);
            cerrar(conexion);
        }
    }

    private Libro construirLibro(ResultSet rs)
            throws SQLException {

        return new Libro(
                rs.getInt("id_libro"),
                rs.getString("titulo"),
                rs.getString("autor"),
                rs.getString("editorial"),
                rs.getInt("cantidad_disponible"),
                rs.getString("estado")
        );
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

    private void cerrar(Connection conexion) {

        if (conexion != null) {
            try {
                conexion.close();
            } catch (SQLException e) {
                daoException.manejarError(e);
            }
        }
    }
}
