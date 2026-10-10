/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package com.proyecto2.app.backend.Backend.servicio;

import com.proyecto2.app.backend.Backend.ConexionDB.ConexionDB;
import com.proyecto2.app.backend.Backend.Dao.AsignacionDao;
import com.proyecto2.app.backend.Backend.Dao.AsistenciaDao;
import com.proyecto2.app.backend.Backend.Dao.CarreraDao;
import com.proyecto2.app.backend.Backend.Dao.CurriculoCursoDao;
import com.proyecto2.app.backend.Backend.Dao.CursoDao;
import com.proyecto2.app.backend.Backend.Dao.EmpleadoDao;
import com.proyecto2.app.backend.Backend.Dao.EstudianteDao;
import com.proyecto2.app.backend.Backend.Dao.GradoDao;
import com.proyecto2.app.backend.Backend.Dao.InscripcionDao;
import com.proyecto2.app.backend.Backend.Dao.NivelDao;
import com.proyecto2.app.backend.Backend.Dao.NotaDao;
import com.proyecto2.app.backend.Backend.Dao.SeccionDao;
import com.proyecto2.app.backend.Backend.Dao.ZonaDao;
import com.proyecto2.app.backend.Backend.Dao.cicloDao;
import com.proyecto2.app.backend.Backend.Dao.curriculoDao;
import com.proyecto2.app.backend.Backend.Exception.ServicioException;
import com.proyecto2.app.backend.Backend.Exception.daoException;
import com.proyecto2.app.backend.Backend.Model.Asignacion;
import com.proyecto2.app.backend.Backend.Model.Asistencia;
import com.proyecto2.app.backend.Backend.Model.Carrera;
import com.proyecto2.app.backend.Backend.Model.Ciclo;
import com.proyecto2.app.backend.Backend.Model.Curriculo;
import com.proyecto2.app.backend.Backend.Model.Curso;
import com.proyecto2.app.backend.Backend.Model.Empleado;
import com.proyecto2.app.backend.Backend.Model.Estudiante;
import com.proyecto2.app.backend.Backend.Model.Grado;
import com.proyecto2.app.backend.Backend.Model.Inscripcion;
import com.proyecto2.app.backend.Backend.Model.Nivel;
import com.proyecto2.app.backend.Backend.Model.Nota;
import com.proyecto2.app.backend.Backend.Model.Seccion;
import com.proyecto2.app.backend.Backend.Model.Zona;
import java.sql.Connection;
import java.sql.SQLException;
import java.util.Collection;

/**
 *
 * @author wilian
 */
public class AcademicoServicio {
    
     private  AsignacionDao asignacionDAO;
    private  AsistenciaDao asistenciaDAO;
    private  CarreraDao carreraDAO;
    private cicloDao cicloDAO;
    private curriculoDao curriculoDAO;
    private  CurriculoCursoDao curriculoCursoDAO;
    private  CursoDao cursoDAO;
    private  EmpleadoDao empleadoDAO;
    private  EstudianteDao estudianteDAO;
    private  GradoDao gradoDAO;
    private  InscripcionDao inscripcionDAO;
    private  NivelDao nivelDAO;
    private  NotaDao notaDAO;
    private  SeccionDao seccionDAO;
    private  ZonaDao zonaDAO;

    public AcademicoServicio() {
        asignacionDAO = new AsignacionDao();
        asistenciaDAO = new AsistenciaDao();
        carreraDAO = new CarreraDao();
        cicloDAO = new cicloDao();
        curriculoDAO = new curriculoDao();
        curriculoCursoDAO = new CurriculoCursoDao();
        cursoDAO = new CursoDao();
        empleadoDAO = new EmpleadoDao();
        estudianteDAO = new EstudianteDao();
        gradoDAO = new GradoDao();
        inscripcionDAO = new InscripcionDao();
        nivelDAO = new NivelDao();
        notaDAO = new NotaDao();
        seccionDAO = new SeccionDao();
        zonaDAO = new ZonaDao();
    }

    public boolean asignarMaestroCurso(Asignacion asignacion) {

        if (asignacion == null || asignacion.getIdEmpleado() <= 0 || asignacion.getIdCurso() <= 0 || asignacion.getIdSeccion() <= 0 || asignacion.getIdCiclo() <= 0) {
            throw new ServicioException("DATOS_INVALIDOS", "Los datos de la asignación no son válidos");
        }

        Connection conexion = null;
        boolean autoCommitAnterior = true;

        try {
            conexion = ConexionDB.getInstance().getConnection();
            autoCommitAnterior = conexion.getAutoCommit();
            conexion.setAutoCommit(false);

            Empleado maestro = null;
            Collection<Empleado> empleados = empleadoDAO.consultar(conexion);

            for (Empleado empleado : empleados) {
                if (empleado.getIdEmpleado() == asignacion.getIdEmpleado()) {
                    maestro = empleado;
                    break;
                }
            }

            if (maestro == null) {
                throw new ServicioException("NO_ENCONTRADO", "El empleado no existe");
            }

            if (!"MAESTRO".equalsIgnoreCase(maestro.getPuesto()) || !"ACTIVO".equalsIgnoreCase(maestro.getEstado())) {
                throw new ServicioException("CONFLICTO", "El empleado debe ser un maestro activo");
            }

            Curso cursoEncontrado = null;
            Collection<Curso> cursos = cursoDAO.consultar(conexion);

            for (Curso curso : cursos) {
                if (curso.getIdCurso() == asignacion.getIdCurso()) {
                    cursoEncontrado = curso;
                    break;
                }
            }

            if (cursoEncontrado == null) {
                throw new ServicioException("NO_ENCONTRADO", "El curso no existe");
            }

            Seccion seccionEncontrada = null;
            Collection<Seccion> secciones = seccionDAO.consultar(conexion);

            for (Seccion seccion : secciones) {
                if (seccion.getIdSeccion() == asignacion.getIdSeccion()) {
                    seccionEncontrada = seccion;
                    break;
                }
            }

            if (seccionEncontrada == null) {
                throw new ServicioException("NO_ENCONTRADO", "La sección no existe");
            }

            boolean cicloExiste = false;
            Collection<Ciclo> ciclos = cicloDAO.consultar(conexion);

            for (Ciclo ciclo : ciclos) {
                if (ciclo.getIdCiclo() == asignacion.getIdCiclo()) {
                    cicloExiste = true;
                    break;
                }
            }

            if (!cicloExiste) {
                throw new ServicioException("NO_ENCONTRADO", "El ciclo no existe");
            }

            Curriculo curriculoEncontrado = null;
            Collection<Curriculo> curriculos = curriculoDAO.consultar(conexion);

            for (Curriculo curriculo : curriculos) {
                if (curriculo.getIdGrado() == seccionEncontrada.getIdGrado() && curriculo.getIdCiclo() == asignacion.getIdCiclo()) {
                    curriculoEncontrado = curriculo;
                    break;
                }
            }

            if (curriculoEncontrado == null) {
                throw new ServicioException("CONFLICTO", "El grado de la sección no tiene currículo para el ciclo indicado");
            }

            boolean cursoPerteneceCurriculo = false;
            Collection<Curso> cursosCurriculo = curriculoCursoDAO.consultarCursos(curriculoEncontrado.getIdCurriculo(), conexion);

            for (Curso curso : cursosCurriculo) {
                if (curso.getIdCurso() == asignacion.getIdCurso()) {
                    cursoPerteneceCurriculo = true;
                    break;
                }
            }

            if (!cursoPerteneceCurriculo) {
                throw new ServicioException("CONFLICTO", "El curso no pertenece al currículo del grado para ese ciclo");
            }

            boolean creado = asignacionDAO.insertar(asignacion, conexion);

            if (!creado) {
                throw new ServicioException("OPERACION_ERROR", "No fue posible asignar el maestro al curso");
            }

            conexion.commit();
            return true;

        } catch (ServicioException e) {
            rollback(conexion, e);
            throw e;

        } catch (daoException e) {
            rollback(conexion, e);
            throw new ServicioException("BD_ERROR", "Error de base de datos al asignar el maestro", e);

        } catch (SQLException e) {
            rollback(conexion, e);
            throw new ServicioException("TRANSACCION_ERROR", "Error en la transacción al asignar el maestro", e);

        } finally {
            restaurarAutoCommit(conexion, autoCommitAnterior);
            cerrarConexion(conexion);
        }
    }

    public boolean crearCarrera(Carrera carrera) {

        if (carrera == null || carrera.getNombre() == null || carrera.getNombre().trim().isEmpty() || carrera.getIdNivel() <= 0) {
            throw new ServicioException("DATOS_INVALIDOS", "Los datos de la carrera no son válidos");
        }

        Connection conexion = null;
        boolean autoCommitAnterior = true;

        try {
            conexion = ConexionDB.getInstance().getConnection();
            autoCommitAnterior = conexion.getAutoCommit();
            conexion.setAutoCommit(false);

            Nivel nivelEncontrado = null;
            Collection<Nivel> niveles = nivelDAO.consultar(conexion);

            for (Nivel nivel : niveles) {
                if (nivel.getIdNivel() == carrera.getIdNivel()) {
                    nivelEncontrado = nivel;
                    break;
                }
            }

            if (nivelEncontrado == null) {
                throw new ServicioException("NO_ENCONTRADO", "El nivel no existe");
            }

            if (!"DIVERSIFICADO".equalsIgnoreCase(nivelEncontrado.getNombre())) {
                throw new ServicioException("CONFLICTO", "Las carreras únicamente pueden pertenecer al nivel Diversificado");
            }

            carrera.setEstado("ACTIVO");

            boolean creado = carreraDAO.insertar(carrera, conexion);

            if (!creado) {
                throw new ServicioException("OPERACION_ERROR", "No fue posible crear la carrera");
            }

            conexion.commit();
            return true;

        } catch (ServicioException e) {
            rollback(conexion, e);
            throw e;

        } catch (daoException e) {
            rollback(conexion, e);
            throw new ServicioException("BD_ERROR", "Error de base de datos al crear la carrera", e);

        } catch (SQLException e) {
            rollback(conexion, e);
            throw new ServicioException("TRANSACCION_ERROR", "Error en la transacción al crear la carrera", e);

        } finally {
            restaurarAutoCommit(conexion, autoCommitAnterior);
            cerrarConexion(conexion);
        }
    }

    public boolean actualizarCarrera(Carrera carrera) {

        if (carrera == null || carrera.getIdCarrera() <= 0 || carrera.getNombre() == null || carrera.getNombre().trim().isEmpty() || carrera.getIdNivel() <= 0) {
            throw new ServicioException("DATOS_INVALIDOS", "Los datos de la carrera no son válidos");
        }

        Connection conexion = null;
        boolean autoCommitAnterior = true;

        try {
            conexion = ConexionDB.getInstance().getConnection();
            autoCommitAnterior = conexion.getAutoCommit();
            conexion.setAutoCommit(false);

            Carrera carreraActual = null;
            Collection<Carrera> carreras = carreraDAO.consultar(conexion);

            for (Carrera actual : carreras) {
                if (actual.getIdCarrera() == carrera.getIdCarrera()) {
                    carreraActual = actual;
                    break;
                }
            }

            if (carreraActual == null) {
                throw new ServicioException("NO_ENCONTRADO", "La carrera no existe");
            }

            Nivel nivelEncontrado = null;
            Collection<Nivel> niveles = nivelDAO.consultar(conexion);

            for (Nivel nivel : niveles) {
                if (nivel.getIdNivel() == carrera.getIdNivel()) {
                    nivelEncontrado = nivel;
                    break;
                }
            }

            if (nivelEncontrado == null || !"DIVERSIFICADO".equalsIgnoreCase(nivelEncontrado.getNombre())) {
                throw new ServicioException("CONFLICTO", "La carrera debe pertenecer al nivel Diversificado");
            }

            carrera.setEstado(carreraActual.getEstado());

            boolean actualizado = carreraDAO.actualizar(carrera, conexion);

            if (!actualizado) {
                throw new ServicioException("OPERACION_ERROR", "No fue posible actualizar la carrera");
            }

            conexion.commit();
            return true;

        } catch (ServicioException e) {
            rollback(conexion, e);
            throw e;

        } catch (daoException e) {
            rollback(conexion, e);
            throw new ServicioException("BD_ERROR", "Error de base de datos al actualizar la carrera", e);

        } catch (SQLException e) {
            rollback(conexion, e);
            throw new ServicioException("TRANSACCION_ERROR", "Error en la transacción al actualizar la carrera", e);

        } finally {
            restaurarAutoCommit(conexion, autoCommitAnterior);
            cerrarConexion(conexion);
        }
    }

    public boolean activarCarrera(int idCarrera) {

        if (idCarrera <= 0) {
            throw new ServicioException("DATOS_INVALIDOS", "El id de la carrera no es válido");
        }

        Connection conexion = null;
        boolean autoCommitAnterior = true;

        try {
            conexion = ConexionDB.getInstance().getConnection();
            autoCommitAnterior = conexion.getAutoCommit();
            conexion.setAutoCommit(false);

            Carrera carreraEncontrada = null;
            Collection<Carrera> carreras = carreraDAO.consultar(conexion);

            for (Carrera carrera : carreras) {
                if (carrera.getIdCarrera() == idCarrera) {
                    carreraEncontrada = carrera;
                    break;
                }
            }

            if (carreraEncontrada == null) {
                throw new ServicioException("NO_ENCONTRADO", "La carrera no existe");
            }

            if ("ACTIVO".equalsIgnoreCase(carreraEncontrada.getEstado())) {
                throw new ServicioException("CONFLICTO", "La carrera ya se encuentra activa");
            }

            boolean activada = carreraDAO.activar(idCarrera, conexion);

            if (!activada) {
                throw new ServicioException("OPERACION_ERROR", "No fue posible activar la carrera");
            }

            conexion.commit();
            return true;

        } catch (ServicioException e) {
            rollback(conexion, e);
            throw e;

        } catch (daoException e) {
            rollback(conexion, e);
            throw new ServicioException("BD_ERROR", "Error de base de datos al activar la carrera", e);

        } catch (SQLException e) {
            rollback(conexion, e);
            throw new ServicioException("TRANSACCION_ERROR", "Error en la transacción al activar la carrera", e);

        } finally {
            restaurarAutoCommit(conexion, autoCommitAnterior);
            cerrarConexion(conexion);
        }
    }

    public boolean desactivarCarrera(int idCarrera) {

        if (idCarrera <= 0) {
            throw new ServicioException("DATOS_INVALIDOS", "El id de la carrera no es válido");
        }

        Connection conexion = null;
        boolean autoCommitAnterior = true;

        try {
            conexion = ConexionDB.getInstance().getConnection();
            autoCommitAnterior = conexion.getAutoCommit();
            conexion.setAutoCommit(false);

            Carrera carreraEncontrada = null;
            Collection<Carrera> carreras = carreraDAO.consultar(conexion);

            for (Carrera carrera : carreras) {
                if (carrera.getIdCarrera() == idCarrera) {
                    carreraEncontrada = carrera;
                    break;
                }
            }

            if (carreraEncontrada == null) {
                throw new ServicioException("NO_ENCONTRADO", "La carrera no existe");
            }

            if ("INACTIVO".equalsIgnoreCase(carreraEncontrada.getEstado())) {
                throw new ServicioException("CONFLICTO", "La carrera ya se encuentra inactiva");
            }

            boolean desactivada = carreraDAO.desactivar(idCarrera, conexion);

            if (!desactivada) {
                throw new ServicioException("OPERACION_ERROR", "No fue posible desactivar la carrera");
            }

            conexion.commit();
            return true;

        } catch (ServicioException e) {
            rollback(conexion, e);
            throw e;

        } catch (daoException e) {
            rollback(conexion, e);
            throw new ServicioException("BD_ERROR", "Error de base de datos al desactivar la carrera", e);

        } catch (SQLException e) {
            rollback(conexion, e);
            throw new ServicioException("TRANSACCION_ERROR", "Error en la transacción al desactivar la carrera", e);

        } finally {
            restaurarAutoCommit(conexion, autoCommitAnterior);
            cerrarConexion(conexion);
        }
    }

    public boolean crearCurriculo(Curriculo curriculo) {

        if (curriculo == null || curriculo.getIdGrado() <= 0 || curriculo.getIdCiclo() <= 0) {
            throw new ServicioException("DATOS_INVALIDOS", "Los datos del currículo no son válidos");
        }

        Connection conexion = null;
        boolean autoCommitAnterior = true;

        try {
            conexion = ConexionDB.getInstance().getConnection();
            autoCommitAnterior = conexion.getAutoCommit();
            conexion.setAutoCommit(false);

            boolean gradoExiste = false;
            Collection<Grado> grados = gradoDAO.consultar(conexion);

            for (Grado grado : grados) {
                if (grado.getIdGrado() == curriculo.getIdGrado()) {
                    gradoExiste = true;
                    break;
                }
            }

            if (!gradoExiste) {
                throw new ServicioException("NO_ENCONTRADO", "El grado no existe");
            }

            boolean cicloExiste = false;
            Collection<Ciclo> ciclos = cicloDAO.consultar(conexion);

            for (Ciclo ciclo : ciclos) {
                if (ciclo.getIdCiclo() == curriculo.getIdCiclo()) {
                    cicloExiste = true;
                    break;
                }
            }

            if (!cicloExiste) {
                throw new ServicioException("NO_ENCONTRADO", "El ciclo no existe");
            }

            Collection<Curriculo> curriculos = curriculoDAO.consultar(conexion);

            for (Curriculo actual : curriculos) {
                if (actual.getIdGrado() == curriculo.getIdGrado() && actual.getIdCiclo() == curriculo.getIdCiclo()) {
                    throw new ServicioException("CONFLICTO", "El grado ya posee un currículo para ese ciclo");
                }
            }

            boolean creado = curriculoDAO.insertar(curriculo, conexion);

            if (!creado) {
                throw new ServicioException("OPERACION_ERROR", "No fue posible crear el currículo");
            }

            conexion.commit();
            return true;

        } catch (ServicioException e) {
            rollback(conexion, e);
            throw e;

        } catch (daoException e) {
            rollback(conexion, e);
            throw new ServicioException("BD_ERROR", "Error de base de datos al crear el currículo", e);

        } catch (SQLException e) {
            rollback(conexion, e);
            throw new ServicioException("TRANSACCION_ERROR", "Error en la transacción al crear el currículo", e);

        } finally {
            restaurarAutoCommit(conexion, autoCommitAnterior);
            cerrarConexion(conexion);
        }
    }

    public boolean establecerCurriculo(int idCurriculo, int[] idCursos) {

        if (idCurriculo <= 0 || idCursos == null || idCursos.length == 0) {
            throw new ServicioException("DATOS_INVALIDOS", "El currículo debe contener al menos un curso");
        }

        Connection conexion = null;
        boolean autoCommitAnterior = true;

        try {
            conexion = ConexionDB.getInstance().getConnection();
            autoCommitAnterior = conexion.getAutoCommit();
            conexion.setAutoCommit(false);

            boolean curriculoExiste = false;
            Collection<Curriculo> curriculos = curriculoDAO.consultar(conexion);

            for (Curriculo curriculo : curriculos) {
                if (curriculo.getIdCurriculo() == idCurriculo) {
                    curriculoExiste = true;
                    break;
                }
            }

            if (!curriculoExiste) {
                throw new ServicioException("NO_ENCONTRADO", "El currículo no existe");
            }

            Collection<Curso> cursos = cursoDAO.consultar(conexion);

            for (int idCurso : idCursos) {

                if (idCurso <= 0) {
                    throw new ServicioException("DATOS_INVALIDOS", "Uno de los cursos no es válido");
                }

                boolean cursoExiste = false;

                for (Curso curso : cursos) {
                    if (curso.getIdCurso() == idCurso) {
                        cursoExiste = true;
                        break;
                    }
                }

                if (!cursoExiste) {
                    throw new ServicioException("NO_ENCONTRADO", "Uno de los cursos indicados no existe");
                }
            }

            for (int i = 0; i < idCursos.length; i++) {
                for (int j = i + 1; j < idCursos.length; j++) {
                    if (idCursos[i] == idCursos[j]) {
                        throw new ServicioException("CONFLICTO", "No se puede repetir un curso dentro del currículo");
                    }
                }
            }

            Collection<Curso> cursosActuales = curriculoCursoDAO.consultarCursos(idCurriculo, conexion);

            for (Curso curso : cursosActuales) {
                boolean eliminado = curriculoCursoDAO.eliminarCurso(idCurriculo, curso.getIdCurso(), conexion);

                if (!eliminado) {
                    throw new ServicioException("OPERACION_ERROR", "No fue posible actualizar los cursos del currículo");
                }
            }

            for (int idCurso : idCursos) {
                boolean agregado = curriculoCursoDAO.agregarCurso(idCurriculo, idCurso, conexion);

                if (!agregado) {
                    throw new ServicioException("OPERACION_ERROR", "No fue posible agregar un curso al currículo");
                }
            }

            conexion.commit();
            return true;

        } catch (ServicioException e) {
            rollback(conexion, e);
            throw e;

        } catch (daoException e) {
            rollback(conexion, e);
            throw new ServicioException("BD_ERROR", "Error de base de datos al establecer el currículo", e);

        } catch (SQLException e) {
            rollback(conexion, e);
            throw new ServicioException("TRANSACCION_ERROR", "Error en la transacción al establecer el currículo", e);

        } finally {
            restaurarAutoCommit(conexion, autoCommitAnterior);
            cerrarConexion(conexion);
        }
    }

    public boolean actualizarCurriculo(Curriculo curriculo, int[] idCursos) {

        if (curriculo == null || curriculo.getIdCurriculo() <= 0 || curriculo.getIdGrado() <= 0 || curriculo.getIdCiclo() <= 0 || idCursos == null || idCursos.length == 0) {
            throw new ServicioException("DATOS_INVALIDOS", "Los datos del currículo no son válidos");
        }

        Connection conexion = null;
        boolean autoCommitAnterior = true;

        try {
            conexion = ConexionDB.getInstance().getConnection();
            autoCommitAnterior = conexion.getAutoCommit();
            conexion.setAutoCommit(false);

            Collection<Curriculo> curriculos = curriculoDAO.consultar(conexion);
            boolean curriculoExiste = false;

            for (Curriculo actual : curriculos) {

                if (actual.getIdCurriculo() == curriculo.getIdCurriculo()) {
                    curriculoExiste = true;
                }

                if (actual.getIdCurriculo() != curriculo.getIdCurriculo() && actual.getIdGrado() == curriculo.getIdGrado() && actual.getIdCiclo() == curriculo.getIdCiclo()) {
                    throw new ServicioException("CONFLICTO", "Ya existe otro currículo para ese grado y ciclo");
                }
            }

            if (!curriculoExiste) {
                throw new ServicioException("NO_ENCONTRADO", "El currículo no existe");
            }

            boolean gradoExiste = false;
            Collection<Grado> grados = gradoDAO.consultar(conexion);

            for (Grado grado : grados) {
                if (grado.getIdGrado() == curriculo.getIdGrado()) {
                    gradoExiste = true;
                    break;
                }
            }

            if (!gradoExiste) {
                throw new ServicioException("NO_ENCONTRADO", "El grado no existe");
            }

            boolean cicloExiste = false;
            Collection<Ciclo> ciclos = cicloDAO.consultar(conexion);

            for (Ciclo ciclo : ciclos) {
                if (ciclo.getIdCiclo() == curriculo.getIdCiclo()) {
                    cicloExiste = true;
                    break;
                }
            }

            if (!cicloExiste) {
                throw new ServicioException("NO_ENCONTRADO", "El ciclo no existe");
            }

            Collection<Curso> cursos = cursoDAO.consultar(conexion);

            for (int idCurso : idCursos) {

                if (idCurso <= 0) {
                    throw new ServicioException("DATOS_INVALIDOS", "Uno de los cursos no es válido");
                }

                boolean cursoExiste = false;

                for (Curso curso : cursos) {
                    if (curso.getIdCurso() == idCurso) {
                        cursoExiste = true;
                        break;
                    }
                }

                if (!cursoExiste) {
                    throw new ServicioException("NO_ENCONTRADO", "Uno de los cursos indicados no existe");
                }
            }

            boolean actualizado = curriculoDAO.actualizar(curriculo, conexion);

            if (!actualizado) {
                throw new ServicioException("OPERACION_ERROR", "No fue posible actualizar el currículo");
            }

            Collection<Curso> cursosActuales = curriculoCursoDAO.consultarCursos(curriculo.getIdCurriculo(), conexion);

            for (Curso curso : cursosActuales) {
                boolean eliminado = curriculoCursoDAO.eliminarCurso(curriculo.getIdCurriculo(), curso.getIdCurso(), conexion);

                if (!eliminado) {
                    throw new ServicioException("OPERACION_ERROR", "No fue posible actualizar los cursos del currículo");
                }
            }

            for (int idCurso : idCursos) {
                boolean agregado = curriculoCursoDAO.agregarCurso(curriculo.getIdCurriculo(), idCurso, conexion);

                if (!agregado) {
                    throw new ServicioException("OPERACION_ERROR", "No fue posible agregar un curso al currículo");
                }
            }

            conexion.commit();
            return true;

        } catch (ServicioException e) {
            rollback(conexion, e);
            throw e;

        } catch (daoException e) {
            rollback(conexion, e);
            throw new ServicioException("BD_ERROR", "Error de base de datos al actualizar el currículo", e);

        } catch (SQLException e) {
            rollback(conexion, e);
            throw new ServicioException("TRANSACCION_ERROR", "Error en la transacción al actualizar el currículo", e);

        } finally {
            restaurarAutoCommit(conexion, autoCommitAnterior);
            cerrarConexion(conexion);
        }
    }

    public boolean inscribirEstudiante(Inscripcion inscripcion) {

        if (inscripcion == null || inscripcion.getIdEstudiante() <= 0 || inscripcion.getIdCiclo() <= 0 || inscripcion.getIdGrado() <= 0 || inscripcion.getIdSeccion() <= 0) {
            throw new ServicioException("DATOS_INVALIDOS", "Los datos de la inscripción no son válidos");
        }

        Connection conexion = null;
        boolean autoCommitAnterior = true;

        try {
            conexion = ConexionDB.getInstance().getConnection();
            autoCommitAnterior = conexion.getAutoCommit();
            conexion.setAutoCommit(false);

            Estudiante estudianteEncontrado = null;
            Collection<Estudiante> estudiantes = estudianteDAO.consultar(conexion);

            for (Estudiante estudiante : estudiantes) {
                if (estudiante.getIdEstudiante() == inscripcion.getIdEstudiante()) {
                    estudianteEncontrado = estudiante;
                    break;
                }
            }

            if (estudianteEncontrado == null) {
                throw new ServicioException("NO_ENCONTRADO", "El estudiante no existe");
            }

            if (!"ACTIVO".equalsIgnoreCase(estudianteEncontrado.getEstado())) {
                throw new ServicioException("CONFLICTO", "Solo un estudiante activo puede ser inscrito");
            }

            boolean gradoExiste = false;
            Collection<Grado> grados = gradoDAO.consultar(conexion);

            for (Grado grado : grados) {
                if (grado.getIdGrado() == inscripcion.getIdGrado()) {
                    gradoExiste = true;
                    break;
                }
            }

            if (!gradoExiste) {
                throw new ServicioException("NO_ENCONTRADO", "El grado no existe");
            }

            Seccion seccionEncontrada = null;
            Collection<Seccion> secciones = seccionDAO.consultar(conexion);

            for (Seccion seccion : secciones) {
                if (seccion.getIdSeccion() == inscripcion.getIdSeccion()) {
                    seccionEncontrada = seccion;
                    break;
                }
            }

            if (seccionEncontrada == null) {
                throw new ServicioException("NO_ENCONTRADO", "La sección no existe");
            }

            if (seccionEncontrada.getIdGrado() != inscripcion.getIdGrado()) {
                throw new ServicioException("CONFLICTO", "La sección no pertenece al grado indicado");
            }

            boolean cicloExiste = false;
            Collection<Ciclo> ciclos = cicloDAO.consultar(conexion);

            for (Ciclo ciclo : ciclos) {
                if (ciclo.getIdCiclo() == inscripcion.getIdCiclo()) {
                    cicloExiste = true;
                    break;
                }
            }

            if (!cicloExiste) {
                throw new ServicioException("NO_ENCONTRADO", "El ciclo no existe");
            }

            boolean tieneCurriculo = false;
            Collection<Curriculo> curriculos = curriculoDAO.consultar(conexion);

            for (Curriculo curriculo : curriculos) {
                if (curriculo.getIdGrado() == inscripcion.getIdGrado() && curriculo.getIdCiclo() == inscripcion.getIdCiclo()) {
                    tieneCurriculo = true;
                    break;
                }
            }

            if (!tieneCurriculo) {
                throw new ServicioException("CONFLICTO", "El grado no tiene currículo establecido para ese ciclo");
            }

            Collection<Inscripcion> inscripciones = inscripcionDAO.consultar(conexion);

            for (Inscripcion actual : inscripciones) {
                if (actual.getIdEstudiante() == inscripcion.getIdEstudiante() && actual.getIdCiclo() == inscripcion.getIdCiclo()) {
                    throw new ServicioException("CONFLICTO", "El estudiante ya se encuentra inscrito en ese ciclo");
                }
            }

            boolean creado = inscripcionDAO.insertar(inscripcion, conexion);

            if (!creado) {
                throw new ServicioException("OPERACION_ERROR", "No fue posible inscribir al estudiante");
            }

            conexion.commit();
            return true;

        } catch (ServicioException e) {
            rollback(conexion, e);
            throw e;

        } catch (daoException e) {
            rollback(conexion, e);
            throw new ServicioException("BD_ERROR", "Error de base de datos al inscribir al estudiante", e);

        } catch (SQLException e) {
            rollback(conexion, e);
            throw new ServicioException("TRANSACCION_ERROR", "Error en la transacción al inscribir al estudiante", e);

        } finally {
            restaurarAutoCommit(conexion, autoCommitAnterior);
            cerrarConexion(conexion);
        }
    }

    public boolean registrarAsistencia(Asistencia asistencia, int idEmpleado) {

        if (asistencia == null || idEmpleado <= 0 || asistencia.getIdAsignacion() <= 0 || asistencia.getIdInscripcion() <= 0 || asistencia.getFecha() == null || asistencia.getEstado() == null || asistencia.getEstado().trim().isEmpty()) {
            throw new ServicioException("DATOS_INVALIDOS", "Los datos de la asistencia no son válidos");
        }

        Connection conexion = null;
        boolean autoCommitAnterior = true;

        try {
            conexion = ConexionDB.getInstance().getConnection();
            autoCommitAnterior = conexion.getAutoCommit();
            conexion.setAutoCommit(false);

            Asignacion asignacionEncontrada = null;
            Collection<Asignacion> asignaciones = asignacionDAO.consultar(conexion);

            for (Asignacion asignacion : asignaciones) {
                if (asignacion.getIdAsignacion() == asistencia.getIdAsignacion()) {
                    asignacionEncontrada = asignacion;
                    break;
                }
            }

            if (asignacionEncontrada == null) {
                throw new ServicioException("NO_ENCONTRADO", "La asignación no existe");
            }

            if (asignacionEncontrada.getIdEmpleado() != idEmpleado) {
                throw new ServicioException("NO_AUTORIZADO", "El maestro no puede registrar asistencia en una asignación que no le pertenece");
            }

            Inscripcion inscripcionEncontrada = null;
            Collection<Inscripcion> inscripciones = inscripcionDAO.consultar(conexion);

            for (Inscripcion inscripcion : inscripciones) {
                if (inscripcion.getIdInscripcion() == asistencia.getIdInscripcion()) {
                    inscripcionEncontrada = inscripcion;
                    break;
                }
            }

            if (inscripcionEncontrada == null) {
                throw new ServicioException("NO_ENCONTRADO", "La inscripción no existe");
            }

            if (inscripcionEncontrada.getIdSeccion() != asignacionEncontrada.getIdSeccion() || inscripcionEncontrada.getIdCiclo() != asignacionEncontrada.getIdCiclo()) {
                throw new ServicioException("CONFLICTO", "El estudiante no pertenece a la sección y ciclo de la asignación");
            }

            Collection<Ciclo> ciclos = cicloDAO.consultar(conexion);
            boolean cicloActivo = false;

            for (Ciclo ciclo : ciclos) {
                if (ciclo.getIdCiclo() == asignacionEncontrada.getIdCiclo() && "ACTIVO".equalsIgnoreCase(ciclo.getEstado())) {
                    cicloActivo = true;
                    break;
                }
            }

            if (!cicloActivo) {
                throw new ServicioException("CONFLICTO", "La asistencia solo puede registrarse durante el ciclo activo");
            }

            Collection<Asistencia> asistencias = asistenciaDAO.consultar(conexion);

            for (Asistencia actual : asistencias) {
                if (actual.getIdAsignacion() == asistencia.getIdAsignacion() && actual.getIdInscripcion() == asistencia.getIdInscripcion() && actual.getFecha().equals(asistencia.getFecha())) {
                    throw new ServicioException("CONFLICTO", "La asistencia de este estudiante ya fue registrada para esa fecha");
                }
            }

            boolean creado = asistenciaDAO.insertar(asistencia, conexion);

            if (!creado) {
                throw new ServicioException("OPERACION_ERROR", "No fue posible registrar la asistencia");
            }

            conexion.commit();
            return true;

        } catch (ServicioException e) {
            rollback(conexion, e);
            throw e;

        } catch (daoException e) {
            rollback(conexion, e);
            throw new ServicioException("BD_ERROR", "Error de base de datos al registrar la asistencia", e);

        } catch (SQLException e) {
            rollback(conexion, e);
            throw new ServicioException("TRANSACCION_ERROR", "Error en la transacción al registrar la asistencia", e);

        } finally {
            restaurarAutoCommit(conexion, autoCommitAnterior);
            cerrarConexion(conexion);
        }
    }

    public boolean definirZona(Zona zona, int idEmpleado) {

        if (zona == null || idEmpleado <= 0 || zona.getNombre() == null || zona.getNombre().trim().isEmpty() || zona.getPorcentaje() <= 0 || zona.getPorcentaje() > 100 || zona.getIdAsignacion() <= 0) {
            throw new ServicioException("DATOS_INVALIDOS", "Los datos de la zona no son válidos");
        }

        Connection conexion = null;
        boolean autoCommitAnterior = true;

        try {
            conexion = ConexionDB.getInstance().getConnection();
            autoCommitAnterior = conexion.getAutoCommit();
            conexion.setAutoCommit(false);

            Asignacion asignacionEncontrada = null;
            Collection<Asignacion> asignaciones = asignacionDAO.consultar(conexion);

            for (Asignacion asignacion : asignaciones) {
                if (asignacion.getIdAsignacion() == zona.getIdAsignacion()) {
                    asignacionEncontrada = asignacion;
                    break;
                }
            }

            if (asignacionEncontrada == null) {
                throw new ServicioException("NO_ENCONTRADO", "La asignación no existe");
            }

            if (asignacionEncontrada.getIdEmpleado() != idEmpleado) {
                throw new ServicioException("NO_AUTORIZADO", "El maestro no puede definir zona para una asignación que no le pertenece");
            }

            Collection<Ciclo> ciclos = cicloDAO.consultar(conexion);
            boolean cicloActivo = false;

            for (Ciclo ciclo : ciclos) {
                if (ciclo.getIdCiclo() == asignacionEncontrada.getIdCiclo() && "ACTIVO".equalsIgnoreCase(ciclo.getEstado())) {
                    cicloActivo = true;
                    break;
                }
            }

            if (!cicloActivo) {
                throw new ServicioException("CONFLICTO", "La zona solo puede definirse durante el ciclo activo");
            }

            double porcentajeTotal = 0;
            Collection<Zona> zonas = zonaDAO.consultar(conexion);

            for (Zona actual : zonas) {
                if (actual.getIdAsignacion() == zona.getIdAsignacion()) {
                    porcentajeTotal += actual.getPorcentaje();
                }
            }

            if (porcentajeTotal + zona.getPorcentaje() > 100) {
                throw new ServicioException("CONFLICTO", "La suma de los porcentajes de zona no puede superar 100%");
            }

            boolean creado = zonaDAO.insertar(zona, conexion);

            if (!creado) {
                throw new ServicioException("OPERACION_ERROR", "No fue posible definir la zona");
            }

            conexion.commit();
            return true;

        } catch (ServicioException e) {
            rollback(conexion, e);
            throw e;

        } catch (daoException e) {
            rollback(conexion, e);
            throw new ServicioException("BD_ERROR", "Error de base de datos al definir la zona", e);

        } catch (SQLException e) {
            rollback(conexion, e);
            throw new ServicioException("TRANSACCION_ERROR", "Error en la transacción al definir la zona", e);

        } finally {
            restaurarAutoCommit(conexion, autoCommitAnterior);
            cerrarConexion(conexion);
        }
    }

    public boolean modificarNota(Nota nota, int idEmpleado) {

        if (nota == null || idEmpleado <= 0 || nota.getIdInscripcion() <= 0 || nota.getIdZona() <= 0 || nota.getValor() < 0) {
            throw new ServicioException("DATOS_INVALIDOS", "Los datos de la nota no son válidos");
        }

        Connection conexion = null;
        boolean autoCommitAnterior = true;

        try {
            conexion = ConexionDB.getInstance().getConnection();
            autoCommitAnterior = conexion.getAutoCommit();
            conexion.setAutoCommit(false);

            Zona zonaEncontrada = null;
            Collection<Zona> zonas = zonaDAO.consultar(conexion);

            for (Zona zona : zonas) {
                if (zona.getIdZona() == nota.getIdZona()) {
                    zonaEncontrada = zona;
                    break;
                }
            }

            if (zonaEncontrada == null) {
                throw new ServicioException("NO_ENCONTRADO", "La zona no existe");
            }

            Asignacion asignacionEncontrada = null;
            Collection<Asignacion> asignaciones = asignacionDAO.consultar(conexion);

            for (Asignacion asignacion : asignaciones) {
                if (asignacion.getIdAsignacion() == zonaEncontrada.getIdAsignacion()) {
                    asignacionEncontrada = asignacion;
                    break;
                }
            }

            if (asignacionEncontrada == null) {
                throw new ServicioException("NO_ENCONTRADO", "La asignación relacionada con la zona no existe");
            }

            if (asignacionEncontrada.getIdEmpleado() != idEmpleado) {
                throw new ServicioException("NO_AUTORIZADO", "El maestro no puede modificar notas de una asignación que no le pertenece");
            }

            boolean cicloActivo = false;
            Collection<Ciclo> ciclos = cicloDAO.consultar(conexion);

            for (Ciclo ciclo : ciclos) {
                if (ciclo.getIdCiclo() == asignacionEncontrada.getIdCiclo() && "ACTIVO".equalsIgnoreCase(ciclo.getEstado())) {
                    cicloActivo = true;
                    break;
                }
            }

            if (!cicloActivo) {
                throw new ServicioException("CONFLICTO", "Las notas únicamente pueden ingresarse o modificarse durante el ciclo activo");
            }

            Inscripcion inscripcionEncontrada = null;
            Collection<Inscripcion> inscripciones = inscripcionDAO.consultar(conexion);

            for (Inscripcion inscripcion : inscripciones) {
                if (inscripcion.getIdInscripcion() == nota.getIdInscripcion()) {
                    inscripcionEncontrada = inscripcion;
                    break;
                }
            }

            if (inscripcionEncontrada == null) {
                throw new ServicioException("NO_ENCONTRADO", "La inscripción no existe");
            }

            if (inscripcionEncontrada.getIdSeccion() != asignacionEncontrada.getIdSeccion() || inscripcionEncontrada.getIdCiclo() != asignacionEncontrada.getIdCiclo()) {
                throw new ServicioException("CONFLICTO", "El estudiante no pertenece a la sección y ciclo de la asignación");
            }

            Nota notaExistente = null;
            Collection<Nota> notas = notaDAO.consultar(conexion);

            for (Nota actual : notas) {
                if (actual.getIdInscripcion() == nota.getIdInscripcion() && actual.getIdZona() == nota.getIdZona()) {
                    notaExistente = actual;
                    break;
                }
            }

            boolean resultado;

            if (notaExistente == null) {
                resultado = notaDAO.insertar(nota, conexion);
            } else {
                nota.setIdNota(notaExistente.getIdNota());
                resultado = notaDAO.actualizar(nota, conexion);
            }

            if (!resultado) {
                throw new ServicioException("OPERACION_ERROR", "No fue posible registrar o modificar la nota");
            }

            conexion.commit();
            return true;

        } catch (ServicioException e) {
            rollback(conexion, e);
            throw e;

        } catch (daoException e) {
            rollback(conexion, e);
            throw new ServicioException("BD_ERROR", "Error de base de datos al modificar la nota", e);

        } catch (SQLException e) {
            rollback(conexion, e);
            throw new ServicioException("TRANSACCION_ERROR", "Error en la transacción al modificar la nota", e);

        } finally {
            restaurarAutoCommit(conexion, autoCommitAnterior);
            cerrarConexion(conexion);
        }
    }

    private void rollback(Connection conexion, Exception causa) {
        if (conexion != null) {
            try {
                conexion.rollback();
            } catch (SQLException e) {
                causa.addSuppressed(e);
            }
        }
    }

    private void restaurarAutoCommit(Connection conexion, boolean autoCommitAnterior) {
        if (conexion != null) {
            try {
                conexion.setAutoCommit(autoCommitAnterior);
            } catch (SQLException e) {
                System.err.println("No fue posible restaurar el autoCommit: " + e.getMessage());
            }
        }
    }

    private void cerrarConexion(Connection conexion) {
        if (conexion != null) {
            try {
                conexion.close();
            } catch (SQLException e) {
                System.err.println("No fue posible cerrar la conexión: " + e.getMessage());
            }
        }
    }
}
