/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package com.proyecto2.app.backend.Backend.Dao;

import com.proyecto2.app.backend.Backend.Exception.daoException;
import com.proyecto2.app.backend.Backend.Model.Nota;
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
public class NotaDao {
    
    private DBConnectionSingleton conexionDB;

    private static final String CREAR_TABLA = """
            CREATE TABLE IF NOT EXISTS NOTA (
                id_nota INT PRIMARY KEY AUTO_INCREMENT,
                id_inscripcion INT NOT NULL,
                id_zona INT NOT NULL,
                valor DECIMAL(5,2) NOT NULL,

                CONSTRAINT fk_nota_inscripcion
                    FOREIGN KEY (id_inscripcion)
                    REFERENCES INSCRIPCION(id_inscripcion),

                CONSTRAINT fk_nota_zona
                    FOREIGN KEY (id_zona)
                    REFERENCES ZONA(id_zona)
            )
            """;

    private static final String INSERTAR = "INSERT INTO NOTA (id_inscripcion, id_zona, valor) VALUES (?, ?, ?)";
    private static final String CONSULTAR = "SELECT id_nota, id_inscripcion, id_zona, valor FROM NOTA ORDER BY id_nota";
    private static final String ACTUALIZAR = "UPDATE NOTA SET id_inscripcion = ?, id_zona = ?, valor = ? WHERE id_nota = ?";

    public NotaDao() {
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

    public boolean insertar(Nota nota) {

        if (nota == null) {
            return false;
        }
        if (nota.getIdInscripcion() <= 0 || nota.getIdZona() <= 0) {
            return false;
        }
        if (nota.getValor() < 0) {
            return false;
        }

        Connection conexion = conexionDB.getConnection();
        PreparedStatement ps = null;

        try {
            ps = conexion.prepareStatement(INSERTAR);
            ps.setInt(1, nota.getIdInscripcion());
            ps.setInt(2, nota.getIdZona());
            ps.setDouble(3, nota.getValor());

            return ps.executeUpdate() > 0;

        } catch (SQLException e) {
            daoException.manejarError(e);
            return false;
        } finally {
            cerrar(ps);
        }
    }

    public Collection<Nota> consultar() {
        Collection<Nota> notas = new ArrayList<>();

        Connection conexion = conexionDB.getConnection();
        PreparedStatement ps = null;
        ResultSet rs = null;

        try {
            ps = conexion.prepareStatement(CONSULTAR);
            rs = ps.executeQuery();

            while (rs.next()) {
                notas.add(construirNota(rs));
            }

        } catch (SQLException e) {
            daoException.manejarError(e);
        } finally {
            cerrar(rs);
            cerrar(ps);
        }
        return notas;
    }

    public boolean actualizar(Nota nota) {
        if (nota == null) {
            return false;
        }
        if (nota.getIdNota() <= 0 || nota.getIdInscripcion() <= 0 || nota.getIdZona() <= 0) {
            return false;
        }
        if (nota.getValor() < 0) {
            return false;
        }

        Connection conexion = conexionDB.getConnection();
        PreparedStatement ps = null;

        try {
            ps = conexion.prepareStatement(ACTUALIZAR);
            ps.setInt(1, nota.getIdInscripcion());
            ps.setInt(2, nota.getIdZona());
            ps.setDouble(3, nota.getValor());
            ps.setInt(4, nota.getIdNota());

            return ps.executeUpdate() > 0;

        } catch (SQLException e) {
            daoException.manejarError(e);
            return false;
        } finally {
            cerrar(ps);
        }
    }

    private Nota construirNota(ResultSet rs) throws SQLException {
        return new Nota( rs.getInt("id_nota"), rs.getInt("id_inscripcion"), rs.getInt("id_zona"), rs.getDouble("valor"));
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
