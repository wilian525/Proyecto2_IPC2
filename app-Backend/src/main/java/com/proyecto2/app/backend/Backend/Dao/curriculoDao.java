/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package com.proyecto2.app.backend.Backend.Dao;

import com.proyecto2.app.backend.Backend.ConexionDB.ConexionDB;
import com.proyecto2.app.backend.Backend.Exception.daoException;
import com.proyecto2.app.backend.Backend.Model.Curriculo;
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
public class curriculoDao {

    private ConexionDB conexionDB;

    private static final String CREAR_TABLA = """
            CREATE TABLE IF NOT EXISTS CURRICULO (
                id_curriculo INT PRIMARY KEY AUTO_INCREMENT,
                id_grado INT NOT NULL,
                id_ciclo INT NOT NULL,

                CONSTRAINT fk_curriculo_grado
                    FOREIGN KEY (id_grado)
                    REFERENCES GRADO(id_grado),

                CONSTRAINT fk_curriculo_ciclo
                    FOREIGN KEY (id_ciclo)
                    REFERENCES CICLO(id_ciclo)
            )
            """;

    private static final String INSERTAR = "INSERT INTO CURRICULO (id_grado, id_ciclo) VALUES (?, ?)";
    private static final String CONSULTAR = "SELECT id_curriculo, id_grado, id_ciclo FROM CURRICULO ORDER BY id_curriculo";
    private static final String ACTUALIZAR = "UPDATE CURRICULO SET id_grado = ?, id_ciclo = ? WHERE id_curriculo = ?";
    private static final String EXISTE_GRADO = "SELECT COUNT(*) FROM GRADO WHERE id_grado = ?";
    private static final String EXISTE_CICLO  = "SELECT COUNT(*) FROM CICLO WHERE id_ciclo = ?";
    private static final String EXISTE_CURRICULO = "SELECT COUNT(*) FROM CURRICULO WHERE id_curriculo = ?";

    public curriculoDao() {
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

    public boolean insertar(Curriculo curriculo) {
        if (curriculo == null) {
            return false;
        }

        if (curriculo.getIdGrado() <= 0 || curriculo.getIdCiclo() <= 0) {
            return false;
        }

        if (!existeGrado(curriculo.getIdGrado())) {
            return false;
        }

        if (!existeCiclo(curriculo.getIdCiclo())) {
            return false;
        }

        Connection conexion = conexionDB.getConnection();
        PreparedStatement ps = null;

        try {
            ps = conexion.prepareStatement(INSERTAR);

            ps.setInt(1, curriculo.getIdGrado());
            ps.setInt(2, curriculo.getIdCiclo());

            return ps.executeUpdate() > 0;

        } catch (SQLException e) {
            daoException.manejarError(e);
            return false;
        } finally {
            cerrar(ps);
        }
    }

    public Collection<Curriculo> consultar() {
        Collection<Curriculo> curriculos = new ArrayList<>();

        Connection conexion = conexionDB.getConnection();
        PreparedStatement ps = null;
        ResultSet rs = null;

        try {
            ps = conexion.prepareStatement(CONSULTAR);
            rs = ps.executeQuery();

            while (rs.next()) {
                curriculos.add(construirCurriculo(rs));
            }

        } catch (SQLException e) {
            daoException.manejarError(e);
        } finally {
            cerrar(rs);
            cerrar(ps);
        }
        return curriculos;
    }

    public boolean actualizar(Curriculo curriculo) {
        if (curriculo == null) {
            return false;
        }

        if (curriculo.getIdCurriculo() <= 0|| curriculo.getIdGrado() <= 0|| curriculo.getIdCiclo() <= 0) {
            return false;
        }

        if (!existeCurriculo(curriculo.getIdCurriculo())) {
            return false;
        }

        if (!existeGrado(curriculo.getIdGrado())) {
            return false;
        }

        if (!existeCiclo(curriculo.getIdCiclo())) {
            return false;
        }

        Connection conexion = conexionDB.getConnection();
        PreparedStatement ps = null;

        try {
            ps = conexion.prepareStatement(ACTUALIZAR);

            ps.setInt(1, curriculo.getIdGrado());
            ps.setInt(2, curriculo.getIdCiclo());
            ps.setInt(3, curriculo.getIdCurriculo());

            return ps.executeUpdate() > 0;

        } catch (SQLException e) {
            daoException.manejarError(e);
            return false;
        } finally {
            cerrar(ps);
        }
    }

    private boolean existeGrado(int idGrado) {
        Connection conexion = conexionDB.getConnection();
        PreparedStatement ps = null;
        ResultSet rs = null;

        try {
            ps = conexion.prepareStatement(EXISTE_GRADO);
            ps.setInt(1, idGrado);

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

    private boolean existeCiclo(int idCiclo) {
        Connection conexion = conexionDB.getConnection();
        PreparedStatement ps = null;
        ResultSet rs = null;

        try {
            ps = conexion.prepareStatement(EXISTE_CICLO);
            ps.setInt(1, idCiclo);

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

    private boolean existeCurriculo(int idCurriculo) {
        Connection conexion = conexionDB.getConnection();
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

    private Curriculo construirCurriculo(ResultSet rs) throws SQLException {
        int idCurriculo = rs.getInt("id_curriculo");
        int idGrado = rs.getInt("id_grado");
        int idCiclo = rs.getInt("id_ciclo");

        return new Curriculo(idCurriculo, idGrado, idCiclo );
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
