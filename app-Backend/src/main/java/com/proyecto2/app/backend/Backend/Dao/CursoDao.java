/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package com.proyecto2.app.backend.Backend.Dao;

import com.proyecto2.app.backend.Backend.ConexionDB.ConexionDB;
import com.proyecto2.app.backend.Backend.Exception.daoException;
import com.proyecto2.app.backend.Backend.Model.Curso;
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
public class CursoDao {

    private ConexionDB conexionDB;

    private static final String CREAR_TABLA = """
            CREATE TABLE IF NOT EXISTS CURSO (
                id_curso INT PRIMARY KEY AUTO_INCREMENT,
                nombre VARCHAR(100) NOT NULL,
                descripcion VARCHAR(255)
            )
            """;

    private static final String INSERTAR = "INSERT INTO CURSO (nombre, descripcion) VALUES (?, ?)";
    private static final String CONSULTAR = "SELECT id_curso, nombre, descripcion FROM CURSO ORDER BY nombre";
    private static final String ACTUALIZAR = "UPDATE CURSO SET nombre = ?, descripcion = ? WHERE id_curso = ?";
    private static final String EXISTE_CURSO = "SELECT COUNT(*) FROM CURSO WHERE id_curso = ?";

    public CursoDao() {
        this.conexionDB = ConexionDB.getInstance();
    }

    public void crearTabla() {
        try (Connection conexion = conexionDB.getConnection(); Statement statement = conexion.createStatement()) {
            statement.execute(CREAR_TABLA);
        } catch (SQLException e) {
            daoException.manejarError(e);
        }
    }

    public boolean insertar(Curso curso) {
        try (Connection conexion = conexionDB.getConnection()) {
            return insertar(curso, conexion);
        } catch (SQLException e) {
            daoException.manejarError(e);
            return false;
        }
    }

    public boolean insertar(Curso curso, Connection conexion) {
        if (curso == null) {
            return false;
        }

        if (curso.getNombre() == null || curso.getNombre().trim().isEmpty()) {
            return false;
        }

        if (curso.getNombre().trim().length() > 100) {
            return false;
        }

        if (curso.getDescripcion() != null && curso.getDescripcion().length() > 255) {
            return false;
        }

        PreparedStatement ps = null;

        try {
            ps = conexion.prepareStatement(INSERTAR);
            ps.setString(1, curso.getNombre().trim());
            ps.setString(2, curso.getDescripcion());
            return ps.executeUpdate() > 0;
        } catch (SQLException e) {
            daoException.manejarError(e);
            return false;
        } finally {
            cerrar(ps);
        }
    }

    public Collection<Curso> consultar() {
        try (Connection conexion = conexionDB.getConnection()) {
            return consultar(conexion);
        } catch (SQLException e) {
            daoException.manejarError(e);
            return new ArrayList<>();
        }
    }

    public Collection<Curso> consultar(Connection conexion) {
        Collection<Curso> cursos = new ArrayList<>();
        PreparedStatement ps = null;
        ResultSet rs = null;

        try {
            ps = conexion.prepareStatement(CONSULTAR);
            rs = ps.executeQuery();

            while (rs.next()) {
                cursos.add(construirCurso(rs));
            }
        } catch (SQLException e) {
            daoException.manejarError(e);
        } finally {
            cerrar(rs);
            cerrar(ps);
        }

        return cursos;
    }

    public boolean actualizar(Curso curso) {
        try (Connection conexion = conexionDB.getConnection()) {
            return actualizar(curso, conexion);
        } catch (SQLException e) {
            daoException.manejarError(e);
            return false;
        }
    }

    public boolean actualizar(Curso curso, Connection conexion) {
        if (curso == null) {
            return false;
        }

        if (curso.getIdCurso() <= 0) {
            return false;
        }

        if (curso.getNombre() == null || curso.getNombre().trim().isEmpty()) {
            return false;
        }

        if (curso.getNombre().trim().length() > 100) {
            return false;
        }

        if (curso.getDescripcion() != null && curso.getDescripcion().length() > 255) {
            return false;
        }

        if (!existeCurso(curso.getIdCurso(), conexion)) {
            return false;
        }

        PreparedStatement ps = null;

        try {
            ps = conexion.prepareStatement(ACTUALIZAR);
            ps.setString(1, curso.getNombre().trim());
            ps.setString(2, curso.getDescripcion());
            ps.setInt(3, curso.getIdCurso());
            return ps.executeUpdate() > 0;
        } catch (SQLException e) {
            daoException.manejarError(e);
            return false;
        } finally {
            cerrar(ps);
        }
    }

    private boolean existeCurso(int idCurso) {
        try (Connection conexion = conexionDB.getConnection()) {
            return existeCurso(idCurso, conexion);
        } catch (SQLException e) {
            daoException.manejarError(e);
            return false;
        }
    }

    private boolean existeCurso(int idCurso, Connection conexion) {
        PreparedStatement ps = null;
        ResultSet rs = null;

        try {
            ps = conexion.prepareStatement(EXISTE_CURSO);
            ps.setInt(1, idCurso);
            rs = ps.executeQuery();

            if (rs.next()) {
                return rs.getInt(1) > 0;
            }
        } catch (SQLException e) {
            daoException.manejarError(e);
        } finally {
            cerrar(rs);
            cerrar(ps);
        }

        return false;
    }

    private Curso construirCurso(ResultSet rs) throws SQLException {
        int idCurso = rs.getInt("id_curso");
        String nombre = rs.getString("nombre");
        String descripcion = rs.getString("descripcion");

        return new Curso(idCurso, nombre, descripcion);
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
