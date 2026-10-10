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
public class CurriculoCursoDao {

    private ConexionDB conexionDB;

    private static final String CREAR_TABLA = """
            CREATE TABLE IF NOT EXISTS CURRICULO_CURSO (
                id_curriculo INT NOT NULL,
                id_curso INT NOT NULL,

                PRIMARY KEY (id_curriculo, id_curso),

                CONSTRAINT fk_curriculo_curso_curriculo
                    FOREIGN KEY (id_curriculo)
                    REFERENCES CURRICULO(id_curriculo),

                CONSTRAINT fk_curriculo_curso_curso
                    FOREIGN KEY (id_curso)
                    REFERENCES CURSO(id_curso)
            )
            """;

    private static final String AGREGAR_CURSO = "INSERT INTO CURRICULO_CURSO (id_curriculo, id_curso) VALUES (?, ?)";
    private static final String ELIMINAR_CURSO = "DELETE FROM CURRICULO_CURSO WHERE id_curriculo = ? AND id_curso = ?";
    private static final String CONSULTAR_CURSOS = """
            SELECT c.id_curso, c.nombre, c.descripcion
            FROM CURRICULO_CURSO cc
            INNER JOIN CURSO c
                ON cc.id_curso = c.id_curso
            WHERE cc.id_curriculo = ?
            ORDER BY c.nombre
            """;

    private static final String EXISTE_CURRICULO = "SELECT COUNT(*) FROM CURRICULO WHERE id_curriculo = ?";
    private static final String EXISTE_CURSO = "SELECT COUNT(*) FROM CURSO WHERE id_curso = ?";
    private static final String EXISTE_RELACION = "SELECT COUNT(*) FROM CURRICULO_CURSO WHERE id_curriculo = ? AND id_curso = ?";

    public CurriculoCursoDao() {
        this.conexionDB = ConexionDB.getInstance();
    }

    public void crearTabla() {
        try (Connection conexion = conexionDB.getConnection(); Statement statement = conexion.createStatement()) {
            statement.execute(CREAR_TABLA);
        } catch (SQLException e) {
            daoException.manejarError(e);
        }
    }

    public boolean agregarCurso(int idCurriculo, int idCurso) {
        try (Connection conexion = conexionDB.getConnection()) {
            return agregarCurso(idCurriculo, idCurso, conexion);
        } catch (SQLException e) {
            daoException.manejarError(e);
            return false;
        }
    }

    public boolean agregarCurso(int idCurriculo, int idCurso, Connection conexion) {
        if (idCurriculo <= 0 || idCurso <= 0) {
            return false;
        }

        if (!existeCurriculo(idCurriculo, conexion)) {
            return false;
        }

        if (!existeCurso(idCurso, conexion)) {
            return false;
        }

        if (existeRelacion(idCurriculo, idCurso, conexion)) {
            return false;
        }

        PreparedStatement ps = null;

        try {
            ps = conexion.prepareStatement(AGREGAR_CURSO);
            ps.setInt(1, idCurriculo);
            ps.setInt(2, idCurso);
            return ps.executeUpdate() > 0;
        } catch (SQLException e) {
            daoException.manejarError(e);
            return false;
        } finally {
            cerrar(ps);
        }
    }

    public boolean eliminarCurso(int idCurriculo, int idCurso) {
        try (Connection conexion = conexionDB.getConnection()) {
            return eliminarCurso(idCurriculo, idCurso, conexion);
        } catch (SQLException e) {
            daoException.manejarError(e);
            return false;
        }
    }

    public boolean eliminarCurso(int idCurriculo, int idCurso, Connection conexion) {
        if (idCurriculo <= 0 || idCurso <= 0) {
            return false;
        }

        if (!existeRelacion(idCurriculo, idCurso, conexion)) {
            return false;
        }

        PreparedStatement ps = null;

        try {
            ps = conexion.prepareStatement(ELIMINAR_CURSO);
            ps.setInt(1, idCurriculo);
            ps.setInt(2, idCurso);
            return ps.executeUpdate() > 0;
        } catch (SQLException e) {
            daoException.manejarError(e);
            return false;
        } finally {
            cerrar(ps);
        }
    }

    public Collection<Curso> consultarCursos(int idCurriculo) {
        try (Connection conexion = conexionDB.getConnection()) {
            return consultarCursos(idCurriculo, conexion);
        } catch (SQLException e) {
            daoException.manejarError(e);
            return new ArrayList<>();
        }
    }

    public Collection<Curso> consultarCursos(int idCurriculo, Connection conexion) {
        Collection<Curso> cursos = new ArrayList<>();

        if (idCurriculo <= 0) {
            return cursos;
        }

        PreparedStatement ps = null;
        ResultSet rs = null;

        try {
            ps = conexion.prepareStatement(CONSULTAR_CURSOS);
            ps.setInt(1, idCurriculo);
            rs = ps.executeQuery();

            while (rs.next()) {
                Curso curso = new Curso(rs.getInt("id_curso"), rs.getString("nombre"), rs.getString("descripcion"));
                cursos.add(curso);
            }
        } catch (SQLException e) {
            daoException.manejarError(e);
        } finally {
            cerrar(rs);
            cerrar(ps);
        }

        return cursos;
    }

    private boolean existeCurriculo(int idCurriculo) {
        try (Connection conexion = conexionDB.getConnection()) {
            return existeCurriculo(idCurriculo, conexion);
        } catch (SQLException e) {
            daoException.manejarError(e);
            return false;
        }
    }

    private boolean existeCurriculo(int idCurriculo, Connection conexion) {
        PreparedStatement ps = null;
        ResultSet rs = null;

        try {
            ps = conexion.prepareStatement(EXISTE_CURRICULO);
            ps.setInt(1, idCurriculo);
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

    private boolean existeRelacion(int idCurriculo, int idCurso) {
        try (Connection conexion = conexionDB.getConnection()) {
            return existeRelacion(idCurriculo, idCurso, conexion);
        } catch (SQLException e) {
            daoException.manejarError(e);
            return false;
        }
    }

    private boolean existeRelacion(int idCurriculo, int idCurso, Connection conexion) {
        PreparedStatement ps = null;
        ResultSet rs = null;

        try {
            ps = conexion.prepareStatement(EXISTE_RELACION);
            ps.setInt(1, idCurriculo);
            ps.setInt(2, idCurso);
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
