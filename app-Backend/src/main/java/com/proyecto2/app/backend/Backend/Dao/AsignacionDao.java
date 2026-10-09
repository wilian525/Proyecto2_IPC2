/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package com.proyecto2.app.backend.Backend.Dao;

import com.proyecto2.app.backend.Backend.ConexionDB.ConexionDB;
import com.proyecto2.app.backend.Backend.Exception.daoException;
import com.proyecto2.app.backend.Backend.Model.Asignacion;
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
public class AsignacionDao {
    
     private ConexionDB conexionDB;

    private static final String CREAR_TABLA = """
            CREATE TABLE IF NOT EXISTS ASIGNACION (
                id_asignacion INT PRIMARY KEY AUTO_INCREMENT,
                id_empleado INT NOT NULL,
                id_curso INT NOT NULL,
                id_seccion INT NOT NULL,
                id_ciclo INT NOT NULL,

                CONSTRAINT fk_asignacion_empleado
                    FOREIGN KEY (id_empleado)
                    REFERENCES EMPLEADO(id_empleado),

                CONSTRAINT fk_asignacion_curso
                    FOREIGN KEY (id_curso)
                    REFERENCES CURSO(id_curso),

                CONSTRAINT fk_asignacion_seccion
                    FOREIGN KEY (id_seccion)
                    REFERENCES SECCION(id_seccion),

                CONSTRAINT fk_asignacion_ciclo
                    FOREIGN KEY (id_ciclo)
                    REFERENCES CICLO(id_ciclo)
            )
            """;

    private static final String INSERTAR ="INSERT INTO ASIGNACION (id_empleado, id_curso, id_seccion, id_ciclo) VALUES (?, ?, ?, ?)";
    private static final String CONSULTAR ="SELECT id_asignacion, id_empleado, id_curso, id_seccion, id_ciclo FROM ASIGNACION ORDER BY id_asignacion";
    private static final String CONSULTAR_EMPLEADO ="SELECT puesto, estado FROM EMPLEADO WHERE id_empleado = ?";
    private static final String CONSULTAR_CICLO ="SELECT estado FROM CICLO WHERE id_ciclo = ?";
    private static final String CONSULTAR_SECCION ="SELECT id_grado FROM SECCION WHERE id_seccion = ?";
    private static final String CONSULTAR_CURRICULO ="SELECT id_curriculo FROM CURRICULO WHERE id_grado = ? AND id_ciclo = ?";
    private static final String CONSULTAR_CURSO_CURRICULO ="SELECT id_curso FROM CURRICULO_CURSO WHERE id_curriculo = ? AND id_curso = ?";
    private static final String CONSULTAR_ASIGNACION_EXISTENTE ="SELECT id_asignacion FROM ASIGNACION WHERE id_empleado = ? AND id_curso = ? AND id_seccion = ? AND id_ciclo = ?";

    public AsignacionDao() {
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

    public boolean insertar(Asignacion asignacion) {

        if (asignacion == null) {
            return false;
        }

        if (asignacion.getIdEmpleado() <= 0 || asignacion.getIdCurso() <= 0 || asignacion.getIdSeccion() <= 0 || asignacion.getIdCiclo() <= 0) {
            return false;
        }

        if (!empleadoEsMaestroActivo(asignacion.getIdEmpleado())) {
            return false;
        }

        if (!cicloActivo(asignacion.getIdCiclo())) {
            return false;
        }

        if (!cursoPerteneceCurriculo(
                asignacion.getIdCurso(),
                asignacion.getIdSeccion(),
                asignacion.getIdCiclo())) {
            return false;
        }

        if (asignacionExiste(
                asignacion.getIdEmpleado(),
                asignacion.getIdCurso(),
                asignacion.getIdSeccion(),
                asignacion.getIdCiclo())) {
            return false;
        }

        Connection conexion = conexionDB.getConnection();
        PreparedStatement ps = null;

        try {

            ps = conexion.prepareStatement(INSERTAR);

            ps.setInt(1, asignacion.getIdEmpleado());
            ps.setInt(2, asignacion.getIdCurso());
            ps.setInt(3, asignacion.getIdSeccion());
            ps.setInt(4, asignacion.getIdCiclo());

            return ps.executeUpdate() > 0;

        } catch (SQLException e) {
            daoException.manejarError(e);
            return false;
        } finally {
            cerrar(ps);
        }
    }

    public Collection<Asignacion> consultar() {
        Collection<Asignacion> asignaciones = new ArrayList<>();

        Connection conexion = conexionDB.getConnection();
        PreparedStatement ps = null;
        ResultSet rs = null;

        try {

            ps = conexion.prepareStatement(CONSULTAR);
            rs = ps.executeQuery();

            while (rs.next()) {
                asignaciones.add(construirAsignacion(rs));
            }

        } catch (SQLException e) {
            daoException.manejarError(e);
        } finally {
            cerrar(rs);
            cerrar(ps);
        }

        return asignaciones;
    }

    private boolean empleadoEsMaestroActivo(int idEmpleado) {

        Connection conexion = conexionDB.getConnection();
        PreparedStatement ps = null;
        ResultSet rs = null;

        try {

            ps = conexion.prepareStatement(CONSULTAR_EMPLEADO);
            ps.setInt(1, idEmpleado);

            rs = ps.executeQuery();

            if (rs.next()) {

                String puesto = rs.getString("puesto");
                String estado = rs.getString("estado");

                return puesto != null && puesto.equalsIgnoreCase("MAESTRO")  && estado != null && estado.equalsIgnoreCase("ACTIVO");
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

    private boolean cursoPerteneceCurriculo(  int idCurso, int idSeccion, int idCiclo) {

        Connection conexion = conexionDB.getConnection();

        PreparedStatement psSeccion = null;
        PreparedStatement psCurriculo = null;
        PreparedStatement psCurso = null;

        ResultSet rsSeccion = null;
        ResultSet rsCurriculo = null;
        ResultSet rsCurso = null;

        try {
            psSeccion = conexion.prepareStatement(CONSULTAR_SECCION);
            psSeccion.setInt(1, idSeccion);

            rsSeccion = psSeccion.executeQuery();

            if (!rsSeccion.next()) {
                return false;
            }

            int idGrado = rsSeccion.getInt("id_grado");

            psCurriculo = conexion.prepareStatement(CONSULTAR_CURRICULO);
            psCurriculo.setInt(1, idGrado);
            psCurriculo.setInt(2, idCiclo);

            rsCurriculo = psCurriculo.executeQuery();

            if (!rsCurriculo.next()) {
                return false;
            }

            int idCurriculo = rsCurriculo.getInt("id_curriculo");
            psCurso = conexion.prepareStatement(CONSULTAR_CURSO_CURRICULO);
            psCurso.setInt(1, idCurriculo);
            psCurso.setInt(2, idCurso);

            rsCurso = psCurso.executeQuery();

            return rsCurso.next();

        } catch (SQLException e) {
            daoException.manejarError(e);
        } finally {

            cerrar(rsCurso);
            cerrar(psCurso);
            cerrar(rsCurriculo);
            cerrar(psCurriculo);
            cerrar(rsSeccion);
            cerrar(psSeccion);
        }
        return false;
    }

    private boolean asignacionExiste(int idEmpleado, int idCurso, int idSeccion,int idCiclo) {
        Connection conexion = conexionDB.getConnection();
        PreparedStatement ps = null;
        ResultSet rs = null;

        try {
            ps = conexion.prepareStatement(CONSULTAR_ASIGNACION_EXISTENTE);

            ps.setInt(1, idEmpleado);
            ps.setInt(2, idCurso);
            ps.setInt(3, idSeccion);
            ps.setInt(4, idCiclo);

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

    private Asignacion construirAsignacion(ResultSet rs)throws SQLException {
        int idAsignacion = rs.getInt("id_asignacion");
        int idEmpleado = rs.getInt("id_empleado");
        int idCurso = rs.getInt("id_curso");
        int idSeccion = rs.getInt("id_seccion");
        int idCiclo = rs.getInt("id_ciclo");

        return new Asignacion(idAsignacion, idEmpleado,  idCurso, idSeccion, idCiclo);
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
