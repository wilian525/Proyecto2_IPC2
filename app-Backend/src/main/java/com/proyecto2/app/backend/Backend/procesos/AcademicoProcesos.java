/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package com.proyecto2.app.backend.Backend.procesos;

import com.proyecto2.app.backend.Backend.Dao.AsignacionDao;
import com.proyecto2.app.backend.Backend.Dao.AsistenciaDao;
import com.proyecto2.app.backend.Backend.Dao.CarreraDao;
import com.proyecto2.app.backend.Backend.Dao.CurriculoCursoDao;
import com.proyecto2.app.backend.Backend.Dao.CursoDao;
import com.proyecto2.app.backend.Backend.Dao.EstudianteDao;
import com.proyecto2.app.backend.Backend.Dao.InscripcionDao;
import com.proyecto2.app.backend.Backend.Dao.NotaDao;
import com.proyecto2.app.backend.Backend.Dao.SeccionDao;
import com.proyecto2.app.backend.Backend.Dao.ZonaDao;
import com.proyecto2.app.backend.Backend.Dao.curriculoDao;
import com.proyecto2.app.backend.Backend.Exception.ProcesosException;
import com.proyecto2.app.backend.Backend.Exception.daoException;
import com.proyecto2.app.backend.Backend.Model.Asignacion;
import com.proyecto2.app.backend.Backend.Model.AsignacionDetalle;
import com.proyecto2.app.backend.Backend.Model.AlumnoNomina;
import com.proyecto2.app.backend.Backend.Model.ReporteRendimiento;
import com.proyecto2.app.backend.Backend.Model.Asistencia;
import com.proyecto2.app.backend.Backend.Model.Carrera;
import com.proyecto2.app.backend.Backend.Model.Curriculo;
import com.proyecto2.app.backend.Backend.Model.Curso;
import com.proyecto2.app.backend.Backend.Model.Estudiante;
import com.proyecto2.app.backend.Backend.Model.Inscripcion;
import com.proyecto2.app.backend.Backend.Model.Nota;
import com.proyecto2.app.backend.Backend.Model.NotaHistorial;
import com.proyecto2.app.backend.Backend.Model.Seccion;
import com.proyecto2.app.backend.Backend.Model.Zona;
import java.util.ArrayList;
import java.util.Collection;

/**
 *
 * @author wilian
 */
public class AcademicoProcesos {

    private AsignacionDao asignacionDAO;
    private AsistenciaDao asistenciaDAO;
    private CarreraDao carreraDAO;
    private CurriculoCursoDao curriculoCursoDAO;
    private CursoDao cursoDAO;
    private EstudianteDao estudianteDAO;
    private InscripcionDao inscripcionDAO;
    private NotaDao notaDAO;
    private ZonaDao zonaDAO;
    private curriculoDao curriculoDAO;
    private SeccionDao seccionDAO;

    public AcademicoProcesos() {
        asignacionDAO = new AsignacionDao();
        asistenciaDAO = new AsistenciaDao();
        carreraDAO = new CarreraDao();
        curriculoCursoDAO = new CurriculoCursoDao();
        cursoDAO = new CursoDao();
        estudianteDAO = new EstudianteDao();
        inscripcionDAO = new InscripcionDao();
        notaDAO = new NotaDao();
        zonaDAO = new ZonaDao();
        curriculoDAO = new curriculoDao();
        seccionDAO = new SeccionDao();
    }

    public Collection<Curso> consultarCursos() {
        try {
            return cursoDAO.consultar();
        } catch (daoException e) {
            throw new ProcesosException("BD_ERROR", "No se pudieron consultar los cursos", e);
        }
    }

    public Collection<AsignacionDetalle> consultarMisCursos(int idEmpleado) {
        if (idEmpleado <= 0) {
            throw new ProcesosException("NO_AUTORIZADO", "No se identificó al maestro autenticado");
        }
        try {
            return asignacionDAO.consultarDetallesMaestro(idEmpleado);
        } catch (daoException e) {
            throw new ProcesosException("BD_ERROR", "No se pudieron consultar los cursos asignados", e);
        }
    }

    public Collection<AlumnoNomina> consultarNominaMaestro(int idAsignacion, int idEmpleado) {
        try {
            Asignacion asignacion = validarAsignacionMaestro(idAsignacion, idEmpleado);
            return inscripcionDAO.consultarNominaAsignacion(asignacion.getIdSeccion(), asignacion.getIdCiclo());
        } catch (ProcesosException e) {
            throw e;
        } catch (daoException e) {
            throw new ProcesosException("BD_ERROR", "No se pudo consultar la nómina", e);
        }
    }

    public ReporteRendimiento consultarRendimientoMaestro(int idAsignacion, int idEmpleado) {
        Asignacion asignacion = validarAsignacionMaestro(idAsignacion, idEmpleado);
        for (Seccion seccion : seccionDAO.consultar()) {
            if (seccion.getIdSeccion() == asignacion.getIdSeccion()) {
                return new ReporteProcesos().generarReporteRendimientoMaestro(
                        seccion.getIdGrado(), asignacion.getIdSeccion(), asignacion.getIdCiclo(), idEmpleado);
            }
        }
        throw new ProcesosException("NO_ENCONTRADO", "No se encontró la sección asignada");
    }

    public Collection<Estudiante> consultarNomina(int idAsignacion, int idEmpleado) {
        try {
            Asignacion asignacion = validarAsignacionMaestro(idAsignacion, idEmpleado);
            Collection<Estudiante> resultado = new ArrayList<>();
            Collection<Estudiante> estudiantes = estudianteDAO.consultar();

            for (Inscripcion inscripcion : inscripcionDAO.consultar()) {
                if (inscripcion.getIdSeccion() == asignacion.getIdSeccion() && inscripcion.getIdCiclo() == asignacion.getIdCiclo()) {
                    Estudiante estudiante = buscarEstudiante(inscripcion.getIdEstudiante(), estudiantes);
                    if (estudiante != null) {
                        resultado.add(estudiante);
                    }
                }
            }

            return resultado;
        } catch (ProcesosException e) {
            throw e;
        } catch (daoException e) {
            throw new ProcesosException("BD_ERROR", "No se pudo consultar la nómina", e);
        }
    }

    public Collection<Asistencia> consultarAsistencia(int idAsignacion, int idEmpleado) {
        try {
            validarAsignacionMaestro(idAsignacion, idEmpleado);
            Collection<Asistencia> resultado = new ArrayList<>();

            for (Asistencia asistencia : asistenciaDAO.consultar()) {
                if (asistencia.getIdAsignacion() == idAsignacion) {
                    resultado.add(asistencia);
                }
            }

            return resultado;
        } catch (ProcesosException e) {
            throw e;
        } catch (daoException e) {
            throw new ProcesosException("BD_ERROR", "No se pudo consultar la asistencia", e);
        }
    }

    public Collection<Nota> consultarNotas(int idAsignacion, int idEmpleado) {
        try {
            validarAsignacionMaestro(idAsignacion, idEmpleado);
            Collection<Nota> resultado = new ArrayList<>();
            Collection<Zona> zonas = zonaDAO.consultar();

            for (Nota nota : notaDAO.consultar()) {
                if (zonaPerteneceAsignacion(nota.getIdZona(), idAsignacion, zonas)) {
                    resultado.add(nota);
                }
            }

            return resultado;
        } catch (ProcesosException e) {
            throw e;
        } catch (daoException e) {
            throw new ProcesosException("BD_ERROR", "No se pudieron consultar las notas", e);
        }
    }

    public Collection<NotaHistorial> consultarHistorialNotas(int idEstudiante) {
        if (idEstudiante <= 0) {
            throw new ProcesosException("DATOS_INVALIDOS", "El estudiante no es válido");
        }
        try {
            return notaDAO.consultarHistorialEstudiante(idEstudiante);
        } catch (daoException e) {
            throw new ProcesosException("BD_ERROR", "No se pudo consultar el historial de notas", e);
        }
    }

    public Collection<Curriculo> consultarCurriculosRegistrados() {
        try {
            return curriculoDAO.consultar();
        } catch (daoException e) {
            throw new ProcesosException("BD_ERROR", "No se pudieron consultar los currículos", e);
        }
    }

    public Collection<Curso> consultarCurriculo(int idGrado, int idCiclo) {
        if (idGrado <= 0 || idCiclo <= 0) {
            throw new ProcesosException("DATOS_INVALIDOS", "El grado y el ciclo deben ser válidos");
        }

        try {
            for (Curriculo curriculo : curriculoDAO.consultar()) {
                if (curriculo.getIdGrado() == idGrado && curriculo.getIdCiclo() == idCiclo) {
                    return curriculoCursoDAO.consultarCursos(curriculo.getIdCurriculo());
                }
            }

            throw new ProcesosException("NO_ENCONTRADO", "No se encontró un currículo para el grado y ciclo indicados");
        } catch (daoException e) {
            throw new ProcesosException("BD_ERROR", "No se pudo consultar el currículo", e);
        }
    }

    public Collection<Carrera> consultarCarreras() {
        try {
            return carreraDAO.consultar();
        } catch (daoException e) {
            throw new ProcesosException("BD_ERROR", "No se pudieron consultar las carreras", e);
        }
    }

    private Asignacion buscarAsignacion(int idAsignacion) {
        for (Asignacion asignacion : asignacionDAO.consultar()) {
            if (asignacion.getIdAsignacion() == idAsignacion) {
                return asignacion;
            }
        }

        return null;
    }

    private Estudiante buscarEstudiante(int idEstudiante, Collection<Estudiante> estudiantes) {
        for (Estudiante estudiante : estudiantes) {
            if (estudiante.getIdEstudiante() == idEstudiante) {
                return estudiante;
            }
        }

        return null;
    }

    private boolean zonaPerteneceAsignacion(int idZona, int idAsignacion, Collection<Zona> zonas) {
        for (Zona zona : zonas) {
            if (zona.getIdZona() == idZona && zona.getIdAsignacion() == idAsignacion) {
                return true;
            }
        }

        return false;
    }

    public Collection<Seccion> consultarSecciones() {
        try {
            return seccionDAO.consultar();
        } catch (daoException e) {
            throw new ProcesosException("BD_ERROR", "No se pudieron consultar las secciones", e);
        }
    }

    public Collection<Asignacion> consultarAsignacionesMaestro(int idEmpleado) {
        if (idEmpleado <= 0) {
            throw new ProcesosException("DATOS_INVALIDOS", "El id del empleado no es válido");
        }

        try {
            Collection<Asignacion> resultado = new ArrayList<>();

            for (Asignacion asignacion : asignacionDAO.consultar()) {
                if (asignacion.getIdEmpleado() == idEmpleado) {
                    resultado.add(asignacion);
                }
            }
            return resultado;
        } catch (daoException e) {
            throw new ProcesosException("BD_ERROR", "No se pudieron consultar las asignaciones", e);
        }
    }

    public Collection<Zona> consultarZonas(int idAsignacion, int idEmpleado) {
        try {
            validarAsignacionMaestro(idAsignacion, idEmpleado);
            Collection<Zona> resultado = new ArrayList<>();

            for (Zona zona : zonaDAO.consultar()) {
                if (zona.getIdAsignacion() == idAsignacion) {
                    resultado.add(zona);
                }
            }

            return resultado;
        } catch (ProcesosException e) {
            throw e;
        } catch (daoException e) {
            throw new ProcesosException("BD_ERROR", "No se pudieron consultar las zonas", e);
        }
    }

    public Collection<Inscripcion> consultarInscripciones(int pagina, int limite) {
        if (pagina <= 0 || limite <= 0) {
            throw new ProcesosException("DATOS_INVALIDOS", "La página y el límite deben ser mayores que cero");
        }

        try {
            return inscripcionDAO.consultar(pagina, limite);
        } catch (daoException e) {
            throw new ProcesosException("BD_ERROR", "No se pudieron consultar las inscripciones", e);
        }
    }

    public int contarInscripciones() {
        try {
            return inscripcionDAO.contar();
        } catch (daoException e) {
            throw new ProcesosException("BD_ERROR", "No se pudo obtener el total de inscripciones", e);
        }
    }

    private Asignacion validarAsignacionMaestro(int idAsignacion, int idEmpleado) {
        if (idAsignacion <= 0 || idEmpleado <= 0) {
            throw new ProcesosException("DATOS_INVALIDOS", "La asignación y el empleado deben ser válidos");
        }

        Asignacion asignacion = buscarAsignacion(idAsignacion);

        if (asignacion == null) {
            throw new ProcesosException("NO_ENCONTRADO", "No se encontró la asignación");
        }
        if (asignacion.getIdEmpleado() != idEmpleado) {
            throw new ProcesosException("NO_AUTORIZADO", "La asignación no pertenece al maestro autenticado");
        }

        return asignacion;
    }
}
