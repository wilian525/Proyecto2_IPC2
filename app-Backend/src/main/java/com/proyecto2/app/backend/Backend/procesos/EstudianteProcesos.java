/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package com.proyecto2.app.backend.Backend.procesos;

import com.proyecto2.app.backend.Backend.Dao.EstudianteDao;
import com.proyecto2.app.backend.Backend.Dao.InscripcionDao;
import com.proyecto2.app.backend.Backend.Exception.ProcesosException;
import com.proyecto2.app.backend.Backend.Exception.daoException;
import com.proyecto2.app.backend.Backend.Model.Estudiante;
import com.proyecto2.app.backend.Backend.Model.Inscripcion;
import java.util.ArrayList;
import java.util.Collection;

/**
 *
 * @author wilian
 */
public class EstudianteProcesos {

    private EstudianteDao estudianteDAO;
    private InscripcionDao inscripcionDAO;

    public EstudianteProcesos() {
        this.estudianteDAO = new EstudianteDao();
        this.inscripcionDAO = new InscripcionDao();
    }

    public Estudiante consultarEstudiante(int idEstudiante) {
        if (idEstudiante <= 0) {
            throw new ProcesosException("DATOS_INVALIDOS", "El id del estudiante no es válido");
        }

        Estudiante estudiante = buscarEstudiantePorId(idEstudiante);

        if (estudiante == null) {
            throw new ProcesosException("NO_ENCONTRADO", "No se encontró el estudiante");
        }

        return estudiante;
    }

    public Collection<Inscripcion> consultarExpediente(int idEstudiante) {
        if (idEstudiante <= 0) {
            throw new ProcesosException("DATOS_INVALIDOS", "El id del estudiante no es válido");
        }

        if (buscarEstudiantePorId(idEstudiante) == null) {
            throw new ProcesosException("NO_ENCONTRADO", "No se encontró el estudiante");
        }

        Collection<Inscripcion> expediente = new ArrayList<>();

        try {
            for (Inscripcion inscripcion : inscripcionDAO.consultar()) {
                if (inscripcion.getIdEstudiante() == idEstudiante) {
                    expediente.add(inscripcion);
                }
            }

            return expediente;
        } catch (daoException e) {
            throw new ProcesosException("BD_ERROR", "No se pudo consultar el expediente del estudiante", e);
        }
    }

    public Estudiante consultarPerfil(int idUsuario) {
        if (idUsuario <= 0) {
            throw new ProcesosException("DATOS_INVALIDOS", "El id del usuario no es válido");
        }

        Estudiante estudiante = buscarEstudiantePorUsuario(idUsuario);

        if (estudiante == null) {
            throw new ProcesosException("NO_ENCONTRADO", "No se encontró el perfil del estudiante");
        }

        return estudiante;
    }

    private Estudiante buscarEstudiantePorId(int idEstudiante) {
        try {
            for (Estudiante estudiante : estudianteDAO.consultar()) {
                if (estudiante.getIdEstudiante() == idEstudiante) {
                    return estudiante;
                }
            }

            return null;
        } catch (daoException e) {
            throw new ProcesosException("BD_ERROR", "No se pudo consultar el estudiante", e);
        }
    }

    private Estudiante buscarEstudiantePorUsuario(int idUsuario) {
        try {
            for (Estudiante estudiante : estudianteDAO.consultar()) {
                if (estudiante.getIdUsuario() == idUsuario) {
                    return estudiante;
                }
            }

            return null;
        } catch (daoException e) {
            throw new ProcesosException("BD_ERROR", "No se pudo consultar el perfil del estudiante", e);
        }
    }
    
    public Collection<Estudiante> consultarEstudiantes(int pagina, int limite) {
    if (pagina <= 0 || limite <= 0) {
        throw new ProcesosException("DATOS_INVALIDOS", "La página y el límite deben ser mayores que cero");
    }

    try {
        return estudianteDAO.consultar(pagina, limite);
    } catch (daoException e) {
        throw new ProcesosException("BD_ERROR", "No se pudieron consultar los estudiantes", e);
    }
}

public int contarEstudiantes() {
    try {
        return estudianteDAO.contar();
    } catch (daoException e) {
        throw new ProcesosException("BD_ERROR", "No se pudo obtener el total de estudiantes", e);
    }
}
}
