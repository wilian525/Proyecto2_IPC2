/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package com.proyecto2.app.backend.Backend.Dao;

import com.proyecto2.app.backend.Backend.ConexionDB.ConexionDB;
import com.proyecto2.app.backend.Backend.Exception.daoException;
import com.proyecto2.app.backend.Backend.Model.Usuario;
import java.sql.Connection;
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
public class UsuarioDao {
    
       private ConexionDB conexionDB;
       
    public static final String CREAR_TABLA = """
           id_usuario INT PRIMARY KEY AUTO_INCREMENT,
            nombre_usuario VARCHAR(100) NOT NULL UNIQUE,
           contraseña VARCHAR(255) NOT NULL,
            rol VARCHAR(30) NOT NULL,
           estado VARCHAR(20) NOT NULL,
             id_empleado INT,
                                             
             CONSTRAINT fk_usuario_empleado
             FOREIGN KEY (id_empleado)
              REFERENCES EMPLEADO(id_empleado)
                                             );                                   
                                             """ ;
    
      private static final String INSERTAR =
            "INSERT INTO USUARIO "
            + "(nombre_usuario, contraseña, rol, estado, id_empleado) "
            + "VALUES (?, ?, ?, ?, ?)";

    private static final String CONSULTAR =
            "SELECT id_usuario, nombre_usuario, contraseña, rol, "
            + "estado, id_empleado "
            + "FROM USUARIO ORDER BY nombre_usuario";

    private static final String ACTUALIZAR =
            "UPDATE USUARIO SET nombre_usuario = ?, rol = ?, "
            + "id_empleado = ? WHERE id_usuario = ?";

    private static final String DESACTIVAR =
            "UPDATE USUARIO SET estado = 'INACTIVO' "
            + "WHERE id_usuario = ?";

    private static final String CAMBIAR_CONTRASEÑA =
            "UPDATE USUARIO SET contraseña = ? "
            + "WHERE id_usuario = ?";

    public UsuarioDao() {
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

    public boolean insertar(Usuario usuario) {

        if (usuario == null) {
            return false;
        }

        if (usuario.getNombreUsuario() == null
                || usuario.getNombreUsuario().trim().isEmpty()) {
            return false;
        }

        if (usuario.getContrasena() == null
                || usuario.getContrasena().trim().isEmpty()) {
            return false;
        }

        if (usuario.getRol() == null
                || usuario.getRol().trim().isEmpty()) {
            return false;
        }

        if (usuario.getEstado() == null
                || usuario.getEstado().trim().isEmpty()) {
            return false;
        }

        Connection conexion = conexionDB.getConnection();
        PreparedStatement ps = null;

        try {

            ps = conexion.prepareStatement(INSERTAR);

            ps.setString(1, usuario.getNombreUsuario().trim());
            ps.setString(2, usuario.getContrasena());
            ps.setString(3, usuario.getRol().trim());
            ps.setString(4, usuario.getEstado().trim());

            if (usuario.getIdEmpleado() > 0) {
                ps.setInt(5, usuario.getIdEmpleado());
            } else {
                ps.setNull(5, Types.INTEGER);
            }

            return ps.executeUpdate() > 0;

        } catch (SQLException e) {
            daoException.manejarError(e);
            return false;
        } finally {
            cerrar(ps);
        }
    }

    public Collection<Usuario> consultar() {

        Collection<Usuario> usuarios = new ArrayList<>();

        Connection conexion = conexionDB.getConnection();
        PreparedStatement ps = null;
        ResultSet rs = null;

        try {

            ps = conexion.prepareStatement(CONSULTAR);
            rs = ps.executeQuery();

            while (rs.next()) {
                usuarios.add(construirUsuario(rs));
            }

        } catch (SQLException e) {
            daoException.manejarError(e);
        } finally {
            cerrar(rs);
            cerrar(ps);
        }

        return usuarios;
    }

    public boolean actualizar(Usuario usuario) {

        if (usuario == null) {
            return false;
        }

        if (usuario.getIdUsuario() <= 0) {
            return false;
        }

        if (usuario.getNombreUsuario() == null
                || usuario.getNombreUsuario().trim().isEmpty()) {
            return false;
        }

        if (usuario.getRol() == null
                || usuario.getRol().trim().isEmpty()) {
            return false;
        }

        Connection conexion = conexionDB.getConnection();
        PreparedStatement ps = null;

        try {

            ps = conexion.prepareStatement(ACTUALIZAR);

            ps.setString(1, usuario.getNombreUsuario().trim());
            ps.setString(2, usuario.getRol().trim());

            if (usuario.getIdEmpleado() > 0) {
                ps.setInt(3, usuario.getIdEmpleado());
            } else {
                ps.setNull(3, Types.INTEGER);
            }

            ps.setInt(4, usuario.getIdUsuario());

            return ps.executeUpdate() > 0;

        } catch (SQLException e) {
            daoException.manejarError(e);
            return false;
        } finally {
            cerrar(ps);
        }
    }

    public boolean desactivar(int idUsuario) {

        if (idUsuario <= 0) {
            return false;
        }

        Connection conexion = conexionDB.getConnection();
        PreparedStatement ps = null;

        try {

            ps = conexion.prepareStatement(DESACTIVAR);
            ps.setInt(1, idUsuario);

            return ps.executeUpdate() > 0;

        } catch (SQLException e) {
            daoException.manejarError(e);
            return false;
        } finally {
            cerrar(ps);
        }
    }

    public boolean cambiarContraseña(
            int idUsuario, String nuevaContraseña) {

        if (idUsuario <= 0) {
            return false;
        }

        if (nuevaContraseña == null
                || nuevaContraseña.trim().isEmpty()) {
            return false;
        }

        Connection conexion = conexionDB.getConnection();
        PreparedStatement ps = null;

        try {

            ps = conexion.prepareStatement(CAMBIAR_CONTRASEÑA);

            ps.setString(1, nuevaContraseña);
            ps.setInt(2, idUsuario);

            return ps.executeUpdate() > 0;

        } catch (SQLException e) {
            daoException.manejarError(e);
            return false;
        } finally {
            cerrar(ps);
        }
    }

    private Usuario construirUsuario(ResultSet rs)
            throws SQLException {

        int idUsuario = rs.getInt("id_usuario");
        String nombreUsuario = rs.getString("nombre_usuario");
        String contrasena = rs.getString("contraseña");
        String rol = rs.getString("rol");
        String estado = rs.getString("estado");

        int idEmpleado = rs.getInt("id_empleado");

        if (rs.wasNull()) {
            idEmpleado = 0;
        }

        return new Usuario(
                idUsuario,
                nombreUsuario,
                contrasena,
                rol,
                estado,
                idEmpleado
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
}
