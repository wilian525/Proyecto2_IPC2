/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package com.proyecto2.app.backend.Backend.procesos;

import com.proyecto2.app.backend.Backend.Dao.EstudianteDao;
import com.proyecto2.app.backend.Backend.Dao.LibroDao;
import com.proyecto2.app.backend.Backend.Dao.PrestamoDao;
import com.proyecto2.app.backend.Backend.Dao.UsuarioDao;
import com.proyecto2.app.backend.Backend.Exception.ProcesosException;
import com.proyecto2.app.backend.Backend.Exception.daoException;
import com.proyecto2.app.backend.Backend.Model.Estudiante;
import com.proyecto2.app.backend.Backend.Model.Libro;
import com.proyecto2.app.backend.Backend.Model.LectorBiblioteca;
import com.proyecto2.app.backend.Backend.Model.Usuario;
import com.proyecto2.app.backend.Backend.Model.Prestamo;
import com.proyecto2.app.backend.Backend.servicio.BibliotecaServicio;
import java.time.LocalDate;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.Collection;

/**
 *
 * @author wilian
 */
public class BibliotecaProcesos {

    private LibroDao libroDAO;
    private PrestamoDao prestamoDAO;
    private EstudianteDao estudianteDAO;

    public BibliotecaProcesos() {
        libroDAO = new LibroDao();
        prestamoDAO = new PrestamoDao();
        estudianteDAO = new EstudianteDao();
    }

    public Collection<LectorBiblioteca> consultarLectores() {
        Collection<LectorBiblioteca> lectores = new ArrayList<>();
        try {
            for (Usuario usuario : new UsuarioDao().consultar()) {
                if ("ACTIVO".equalsIgnoreCase(usuario.getEstado())
                        && ("ESTUDIANTE".equalsIgnoreCase(usuario.getRol()) || "MAESTRO".equalsIgnoreCase(usuario.getRol()))) {
                    lectores.add(new LectorBiblioteca(usuario.getIdUsuario(), usuario.getNombreUsuario(), usuario.getRol()));
                }
            }
            return lectores;
        } catch (daoException e) {
            throw new ProcesosException("BD_ERROR", "No se pudieron consultar los lectores", e);
        }
    }

    public Libro consultarLibro(int idLibro) {
        if (idLibro <= 0) {
            throw new ProcesosException("DATOS_INVALIDOS", "El id del libro no es válido");
        }

        try {
            for (Libro libro : libroDAO.consultar()) {
                if (libro.getIdLibro() == idLibro) {
                    return libro;
                }
            }

            throw new ProcesosException("NO_ENCONTRADO", "No se encontró el libro");
        } catch (daoException e) {
            throw new ProcesosException("BD_ERROR", "No se pudo consultar el libro", e);
        }
    }

    public Collection<Libro> consultarCatalogo() {
        try {
            Collection<Libro> catalogo = new ArrayList<>();

            for (Libro libro : libroDAO.consultar()) {
                if ("ACTIVO".equalsIgnoreCase(libro.getEstado()) && libro.getCantidadDisponible() > 0) {
                    catalogo.add(libro);
                }
            }

            return catalogo;
        } catch (daoException e) {
            throw new ProcesosException("BD_ERROR", "No se pudo consultar el catálogo de libros", e);
        }
    }

    public Collection<Prestamo> consultarAtrasos() {
        try {
            Collection<Prestamo> prestamos = prestamoDAO.consultarAtrasos();

            for (Prestamo prestamo : prestamos) {
                controlarAtraso(prestamo);
            }

            return prestamos;
        } catch (daoException e) {
            throw new ProcesosException("BD_ERROR", "No se pudieron consultar los préstamos atrasados", e);
        }
    }

    public Prestamo controlarAtraso(Prestamo prestamo) {
        if (prestamo == null || prestamo.getFechaDevolucionEsperada() == null) {
            throw new ProcesosException("DATOS_INVALIDOS", "Los datos del préstamo no son válidos");
        }

        if (prestamo.getFechaDevolucion() != null) {
            return prestamo;
        }

        LocalDate fechaActual = LocalDate.now();
        int diasAtraso = 0;

        if (fechaActual.isAfter(prestamo.getFechaDevolucionEsperada())) {
            diasAtraso = (int) ChronoUnit.DAYS.between(prestamo.getFechaDevolucionEsperada(), fechaActual);
        }

        prestamo.setDiasAtraso(diasAtraso);
        prestamo.setMontoMulta(diasAtraso * BibliotecaServicio.PRECIO_MULTA);

        return prestamo;
    }

    public Collection<Prestamo> consultarMultas() {
        try {
            Collection<Prestamo> multas = new ArrayList<>();

            for (Prestamo prestamo : prestamoDAO.consultar()) {
                controlarAtraso(prestamo);

                if (prestamo.getMontoMulta() > 0) {
                    multas.add(prestamo);
                }
            }

            return multas;
        } catch (daoException e) {
            throw new ProcesosException("BD_ERROR", "No se pudieron consultar las multas", e);
        }
    }

    public Collection<Prestamo> consultarLibrosPendientes(int idUsuario) {
        if (idUsuario <= 0) {
            throw new ProcesosException("DATOS_INVALIDOS", "El id del usuario no es válido");
        }

        try {
            Collection<Prestamo> pendientes = new ArrayList<>();

            for (Prestamo prestamo : prestamoDAO.consultar()) {
                if (prestamo.getIdUsuario() == idUsuario && prestamo.getFechaDevolucion() == null) {
                    controlarAtraso(prestamo);
                    pendientes.add(prestamo);
                }
            }

            return pendientes;
        } catch (daoException e) {
            throw new ProcesosException("BD_ERROR", "No se pudieron consultar los libros pendientes", e);
        }
    }

    public Collection<Prestamo> consultarMultasEstudiante(int idEstudiante) {
        if (idEstudiante <= 0) {
            throw new ProcesosException("DATOS_INVALIDOS", "El id del estudiante no es válido");
        }

        try {
            Estudiante estudiante = buscarEstudiante(idEstudiante);

            if (estudiante == null) {
                throw new ProcesosException("NO_ENCONTRADO", "No se encontró el estudiante");
            }

            Collection<Prestamo> multas = new ArrayList<>();

            for (Prestamo prestamo : prestamoDAO.consultar()) {
                if (prestamo.getIdUsuario() == estudiante.getIdUsuario()) {
                    controlarAtraso(prestamo);

                    if (prestamo.getMontoMulta() > 0) {
                        multas.add(prestamo);
                    }
                }
            }

            return multas;
        } catch (daoException e) {
            throw new ProcesosException("BD_ERROR", "No se pudieron consultar las multas del estudiante", e);
        }
    }

    private Estudiante buscarEstudiante(int idEstudiante) {
        for (Estudiante estudiante : estudianteDAO.consultar()) {
            if (estudiante.getIdEstudiante() == idEstudiante) {
                return estudiante;
            }
        }

        return null;
    }
    
    public Collection<Libro> consultarLibros(int pagina, int limite) {
    if (pagina <= 0 || limite <= 0) {
        throw new ProcesosException("DATOS_INVALIDOS", "La página y el límite deben ser mayores que cero");
    }

    try {
        return libroDAO.consultar(pagina, limite);
    } catch (daoException e) {
        throw new ProcesosException("BD_ERROR", "No se pudieron consultar los libros", e);
    }
}

public int contarLibros() {
    try {
        return libroDAO.contar();
    } catch (daoException e) {
        throw new ProcesosException("BD_ERROR", "No se pudo obtener el total de libros", e);
    }
}

public Collection<Prestamo> consultarPrestamos(int pagina, int limite) {
    if (pagina <= 0 || limite <= 0) {
        throw new ProcesosException("DATOS_INVALIDOS", "La página y el límite deben ser mayores que cero");
    }

    try {
        return prestamoDAO.consultar(pagina, limite);
    } catch (daoException e) {
        throw new ProcesosException("BD_ERROR", "No se pudieron consultar los préstamos", e);
    }
}

public int contarPrestamos() {
    try {
        return prestamoDAO.contar();
    } catch (daoException e) {
        throw new ProcesosException("BD_ERROR", "No se pudo obtener el total de préstamos", e);
    }
}

public Collection<Prestamo> consultarAtrasos(int pagina, int limite) {
    if (pagina <= 0 || limite <= 0) {
        throw new ProcesosException("DATOS_INVALIDOS", "La página y el límite deben ser mayores que cero");
    }

    try {
        Collection<Prestamo> prestamos = prestamoDAO.consultarAtrasos(pagina, limite);

        for (Prestamo prestamo : prestamos) {
            controlarAtraso(prestamo);
        }

        return prestamos;

    } catch (daoException e) {
        throw new ProcesosException("BD_ERROR", "No se pudieron consultar los préstamos atrasados", e);
    }
}

public int contarAtrasos() {
    try {
        return prestamoDAO.contarAtrasos();
    } catch (daoException e) {
        throw new ProcesosException("BD_ERROR", "No se pudo obtener el total de préstamos atrasados", e);
    }
}
}
