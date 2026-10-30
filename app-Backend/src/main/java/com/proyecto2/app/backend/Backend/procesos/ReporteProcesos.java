/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package com.proyecto2.app.backend.Backend.procesos;

import com.proyecto2.app.backend.Backend.Dao.AsignacionDao;
import com.proyecto2.app.backend.Backend.Dao.CursoDao;
import com.proyecto2.app.backend.Backend.Dao.EstudianteDao;
import com.proyecto2.app.backend.Backend.Dao.EmpleadoDao;
import com.proyecto2.app.backend.Backend.Model.Empleado;
import com.proyecto2.app.backend.Backend.Dao.GradoDao;
import com.proyecto2.app.backend.Backend.Dao.InscripcionDao;
import com.proyecto2.app.backend.Backend.Dao.LibroDao;
import com.proyecto2.app.backend.Backend.Dao.NotaDao;
import com.proyecto2.app.backend.Backend.Dao.PagoDao;
import com.proyecto2.app.backend.Backend.Dao.PrestamoDao;
import com.proyecto2.app.backend.Backend.Dao.SeccionDao;
import com.proyecto2.app.backend.Backend.Dao.ZonaDao;
import com.proyecto2.app.backend.Backend.Exception.ProcesosException;
import com.proyecto2.app.backend.Backend.Exception.daoException;
import com.proyecto2.app.backend.Backend.Model.Asignacion;
import com.proyecto2.app.backend.Backend.Model.Curso;
import com.proyecto2.app.backend.Backend.Model.Estudiante;
import com.proyecto2.app.backend.Backend.Model.Grado;
import com.proyecto2.app.backend.Backend.Model.Inscripcion;
import com.proyecto2.app.backend.Backend.Model.Libro;
import com.proyecto2.app.backend.Backend.Model.Nota;
import com.proyecto2.app.backend.Backend.Model.Pago;
import com.proyecto2.app.backend.Backend.Model.Prestamo;
import com.proyecto2.app.backend.Backend.Model.ReporteGradoSeccion;
import com.proyecto2.app.backend.Backend.Model.ReporteHistorialPago;
import com.proyecto2.app.backend.Backend.Model.ReporteIngresoConcepto;
import com.proyecto2.app.backend.Backend.Model.ReporteIngresos;
import com.proyecto2.app.backend.Backend.Model.ReportePrestamoMora;
import com.proyecto2.app.backend.Backend.Model.ReportePromedioCurso;
import com.proyecto2.app.backend.Backend.Model.ReportePromedioEstudiante;
import com.proyecto2.app.backend.Backend.Model.ReporteRendimiento;
import com.proyecto2.app.backend.Backend.Model.ReporteSolventeMoroso;
import com.proyecto2.app.backend.Backend.Model.Seccion;
import com.proyecto2.app.backend.Backend.Model.Zona;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.HashMap;

/**
 *
 * @author wilian
 */
public class ReporteProcesos {

   private EstudianteDao estudianteDAO;
    private InscripcionDao inscripcionDAO;
    private GradoDao gradoDAO;
    private SeccionDao seccionDAO;
    private CursoDao cursoDAO;
    private AsignacionDao asignacionDAO;
    private ZonaDao zonaDAO;
    private NotaDao notaDAO;
    private PagoDao pagoDAO;
    private LibroDao libroDAO;
    private PagoProcesos pagoProcesos;
    private BibliotecaProcesos bibliotecaProcesos;

    public ReporteProcesos() {
        estudianteDAO = new EstudianteDao();
        inscripcionDAO = new InscripcionDao();
        gradoDAO = new GradoDao();
        seccionDAO = new SeccionDao();
        cursoDAO = new CursoDao();
        asignacionDAO = new AsignacionDao();
        zonaDAO = new ZonaDao();
        notaDAO = new NotaDao();
        pagoDAO = new PagoDao();
        libroDAO = new LibroDao();
        pagoProcesos = new PagoProcesos();
        bibliotecaProcesos = new BibliotecaProcesos();
    }

    public Collection<ReporteGradoSeccion> generarReporteGradoSeccion(int idGrado, int idSeccion, int idCiclo) {
        if (idGrado < 0 || idSeccion < 0 || idCiclo < 0) {
            throw new ProcesosException("DATOS_INVALIDOS", "Los filtros del reporte no son válidos");
        }

        try {
            Collection<ReporteGradoSeccion> reporte = new ArrayList<>();
            Collection<Estudiante> estudiantes = estudianteDAO.consultar();
            Collection<Grado> grados = gradoDAO.consultar();
            Collection<Seccion> secciones = seccionDAO.consultar();

            for (Inscripcion inscripcion : inscripcionDAO.consultar()) {
                if (idGrado > 0 && inscripcion.getIdGrado() != idGrado) {
                    continue;
                }

                if (idSeccion > 0 && inscripcion.getIdSeccion() != idSeccion) {
                    continue;
                }

                if (idCiclo > 0 && inscripcion.getIdCiclo() != idCiclo) {
                    continue;
                }

                Estudiante estudiante = buscarEstudiante(inscripcion.getIdEstudiante(), estudiantes);
                Grado grado = buscarGrado(inscripcion.getIdGrado(), grados);
                Seccion seccion = buscarSeccion(inscripcion.getIdSeccion(), secciones);

                if (estudiante != null) {
                    reporte.add(new ReporteGradoSeccion(estudiante.getIdEstudiante(), estudiante.getNombre() + " " + estudiante.getApellido(), inscripcion.getIdGrado(), grado != null ? grado.getNombre() : "", inscripcion.getIdSeccion(), seccion != null ? seccion.getNombre() : "", inscripcion.getIdCiclo()));
                }
            }

            return reporte;
        } catch (daoException e) {
            throw new ProcesosException("BD_ERROR", "No se pudo generar el reporte por grado y sección", e);
        }
    }

    public Collection<ReporteSolventeMoroso> generarReporteSolventesMorosos(int idGrado, int idSeccion, int idCiclo, int mensualidadesRequeridas) {
        if (mensualidadesRequeridas < 0) {
            throw new ProcesosException("DATOS_INVALIDOS", "La cantidad de mensualidades requeridas no es válida");
        }

        Collection<ReporteSolventeMoroso> reporte = new ArrayList<>();

        for (ReporteGradoSeccion estudiante : generarReporteGradoSeccion(idGrado, idSeccion, idCiclo)) {
            double saldoPendiente = pagoProcesos.consultarEstadoCuentaPorCiclo(estudiante.getIdEstudiante(), estudiante.getIdCiclo());
            double saldoVencido = pagoProcesos.consultarSaldoVencido(estudiante.getIdEstudiante(), estudiante.getIdCiclo());
            String estadoCuenta = saldoVencido > 0.01 ? "MOROSO" : saldoPendiente > 0.01 ? "PENDIENTE" : "SOLVENTE";
            reporte.add(new ReporteSolventeMoroso(estudiante.getIdEstudiante(), estudiante.getEstudiante(), estudiante.getIdGrado(), estudiante.getGrado(), estudiante.getIdSeccion(), estudiante.getSeccion(), estudiante.getIdCiclo(), redondear(saldoPendiente), estadoCuenta));
        }

        return reporte;
    }

    public ReportePromedioCurso generarReportePromedioCurso(int idCurso, int idSeccion, int idCiclo) {
        if (idCurso <= 0 || idSeccion <= 0 || idCiclo <= 0) {
            throw new ProcesosException("DATOS_INVALIDOS", "El curso, la sección y el ciclo deben ser válidos");
        }

        try {
            Collection<Curso> cursos = cursoDAO.consultar();
            Collection<Asignacion> asignaciones = asignacionDAO.consultar();
            Collection<Zona> zonas = zonaDAO.consultar();
            Collection<Nota> notas = notaDAO.consultar();
            Collection<Inscripcion> inscripciones = inscripcionDAO.consultar();
            Curso curso = buscarCurso(idCurso, cursos);

            if (curso == null) {
                throw new ProcesosException("NO_ENCONTRADO", "No se encontró el curso");
            }

            if (!existeAsignacionCurso(idCurso, idSeccion, idCiclo, asignaciones)) {
                throw new ProcesosException("NO_ENCONTRADO", "El curso no está asignado a la sección y ciclo indicados");
            }

            return calcularPromedioCurso(idCurso, idSeccion, idCiclo, curso.getNombre(), asignaciones, zonas, notas, inscripciones);
        } catch (daoException e) {
            throw new ProcesosException("BD_ERROR", "No se pudo generar el promedio del curso", e);
        }
    }

    public ReporteRendimiento generarReporteRendimientoMaestro(int idGrado, int idSeccion, int idCiclo, int idEmpleado) {
        if (idEmpleado <= 0) {
            throw new ProcesosException("NO_AUTORIZADO", "No se identificó al maestro");
        }
        return generarReporteRendimientoFiltrado(idGrado, idSeccion, idCiclo, idEmpleado);
    }

    public ReporteRendimiento generarReporteRendimiento(int idGrado, int idSeccion, int idCiclo) {
        return generarReporteRendimientoFiltrado(idGrado, idSeccion, idCiclo, 0);
    }

    private ReporteRendimiento generarReporteRendimientoFiltrado(int idGrado, int idSeccion, int idCiclo, int idEmpleadoFiltro) {
        if (idGrado <= 0 || idSeccion <= 0 || idCiclo <= 0) {
            throw new ProcesosException("DATOS_INVALIDOS", "El grado, la sección y el ciclo deben ser válidos");
        }

        try {
            Collection<Estudiante> estudiantes = estudianteDAO.consultar();
            Collection<Inscripcion> inscripciones = inscripcionDAO.consultar();
            Collection<Grado> grados = gradoDAO.consultar();
            Collection<Seccion> secciones = seccionDAO.consultar();
            Collection<Curso> cursos = cursoDAO.consultar();
            Collection<Asignacion> asignaciones = new ArrayList<>(asignacionDAO.consultar());
            if (idEmpleadoFiltro > 0) {
                asignaciones.removeIf(asignacion -> asignacion.getIdEmpleado() != idEmpleadoFiltro);
            }
            Collection<Zona> zonas = zonaDAO.consultar();
            Collection<Nota> notas = notaDAO.consultar();
            Collection<Integer> cursosProcesados = new ArrayList<>();
            Collection<ReportePromedioCurso> promediosCursos = new ArrayList<>();
            ArrayList<ReportePromedioEstudiante> mejoresPromedios = new ArrayList<>();
            ReportePromedioCurso cursoMayorDificultad = null;
            Grado grado = buscarGrado(idGrado, grados);
            Seccion seccion = buscarSeccion(idSeccion, secciones);

            if (grado == null || seccion == null) {
                throw new ProcesosException("NO_ENCONTRADO", "No se encontró el grado o la sección");
            }

            if (seccion.getIdGrado() != idGrado) {
                throw new ProcesosException("DATOS_INVALIDOS", "La sección no pertenece al grado indicado");
            }

            for (Asignacion asignacion : asignaciones) {
                if (asignacion.getIdSeccion() != idSeccion || asignacion.getIdCiclo() != idCiclo || cursosProcesados.contains(asignacion.getIdCurso())) {
                    continue;
                }

                Curso curso = buscarCurso(asignacion.getIdCurso(), cursos);

                if (curso != null) {
                    ReportePromedioCurso promedioCurso = calcularPromedioCurso(curso.getIdCurso(), idSeccion, idCiclo, curso.getNombre(), asignaciones, zonas, notas, inscripciones);
                    promediosCursos.add(promedioCurso);
                    cursosProcesados.add(curso.getIdCurso());

                    if (promedioCurso.getEstudiantesEvaluados() > 0 && (cursoMayorDificultad == null || promedioCurso.getPromedio() < cursoMayorDificultad.getPromedio())) {
                        cursoMayorDificultad = promedioCurso;
                    }
                }
            }

            for (Inscripcion inscripcion : inscripciones) {
                if (inscripcion.getIdGrado() != idGrado || inscripcion.getIdSeccion() != idSeccion || inscripcion.getIdCiclo() != idCiclo) {
                    continue;
                }

                Estudiante estudiante = buscarEstudiante(inscripcion.getIdEstudiante(), estudiantes);

                if (estudiante != null) {
                    double sumaPromedios = 0;
                    int cursosEvaluados = 0;

                    for (Integer idCurso : cursosProcesados) {
                        Double notaCurso = calcularNotaCursoEstudiante(idCurso, inscripcion.getIdInscripcion(), idSeccion, idCiclo, asignaciones, zonas, notas);

                        if (notaCurso != null) {
                            sumaPromedios += notaCurso;
                            cursosEvaluados++;
                        }
                    }

                    if (cursosEvaluados > 0) {
                        mejoresPromedios.add(new ReportePromedioEstudiante(estudiante.getIdEstudiante(), estudiante.getNombre() + " " + estudiante.getApellido(), redondear(sumaPromedios / cursosEvaluados)));
                    }
                }
            }

            ordenarMejoresPromedios(mejoresPromedios);
            return new ReporteRendimiento(idGrado, grado.getNombre(), idSeccion, seccion.getNombre(), idCiclo, promediosCursos, cursoMayorDificultad, mejoresPromedios);
        } catch (daoException e) {
            throw new ProcesosException("BD_ERROR", "No se pudo generar el reporte de rendimiento académico", e);
        }
    }

    public ReporteHistorialPago generarReporteHistorialPagos(int idEstudiante, LocalDate fechaInicio, LocalDate fechaFin, int mensualidadesRequeridas) {
        if (idEstudiante <= 0 || mensualidadesRequeridas < 0) {
            throw new ProcesosException("DATOS_INVALIDOS", "Los datos del reporte de pagos no son válidos");
        }

        validarRangoFechas(fechaInicio, fechaFin);

        try {
            Estudiante estudiante = buscarEstudiante(idEstudiante, estudianteDAO.consultar());

            if (estudiante == null) {
                throw new ProcesosException("NO_ENCONTRADO", "No se encontró el estudiante");
            }

            Collection<Pago> historial = new ArrayList<>();
            Collection<Empleado> personal = new EmpleadoDao().consultar();
            double totalPagado = 0;

            for (Pago pago : pagoProcesos.consultarHistorialPagos(idEstudiante)) {
                if (!fechaDentroRango(pago.getFecha(), fechaInicio, fechaFin)) {
                    continue;
                }

                for(Empleado empleado:personal) {
                    if(empleado.getIdEmpleado()==pago.getIdEmpleado()) {
                        pago.setNombreEmpleado(empleado.getNombre()+" "+empleado.getApellido());break;
                    }
                }
                if(pago.getNombreEmpleado()==null) pago.setNombreEmpleado("Personal #"+pago.getIdEmpleado());
                historial.add(pago);
                totalPagado += pago.getMonto();
            }

            double saldoPendiente = pagoProcesos.consultarEstadoCuenta(idEstudiante, mensualidadesRequeridas);
            double saldoVencido = pagoProcesos.consultarSaldoVencido(idEstudiante);
            String estadoCuenta = saldoVencido > 0.01 ? "MOROSO" : saldoPendiente > 0.01 ? "PENDIENTE" : "SOLVENTE";
            return new ReporteHistorialPago(estudiante.getIdEstudiante(), estudiante.getNombre() + " " + estudiante.getApellido(), fechaInicio, fechaFin, historial, redondear(totalPagado), redondear(saldoPendiente), estadoCuenta);
        } catch (daoException e) {
            throw new ProcesosException("BD_ERROR", "No se pudo generar el historial de pagos", e);
        }
    }

    public ReporteIngresos generarReporteIngresos(LocalDate fechaInicio, LocalDate fechaFin) {
        validarRangoFechas(fechaInicio, fechaFin);

        try {
            Collection<ReporteIngresoConcepto> detalle = new ArrayList<>();
            double totalIngresos = 0;

            for (Pago pago : pagoDAO.consultar()) {
                if (!fechaDentroRango(pago.getFecha(), fechaInicio, fechaFin)) {
                    continue;
                }

                String concepto = pago.getConcepto() == null ? "SIN_CONCEPTO" : pago.getConcepto().trim().toUpperCase();
                ReporteIngresoConcepto ingreso = buscarIngresoConcepto(concepto, detalle);

                if (ingreso == null) {
                    detalle.add(new ReporteIngresoConcepto(concepto, redondear(pago.getMonto())));
                } else {
                    ingreso.setTotal(redondear(ingreso.getTotal() + pago.getMonto()));
                }

                totalIngresos += pago.getMonto();
            }

            return new ReporteIngresos(fechaInicio, fechaFin, detalle, redondear(totalIngresos));
        } catch (daoException e) {
            throw new ProcesosException("BD_ERROR", "No se pudo generar el reporte de ingresos", e);
        }
    }

    public Collection<ReportePrestamoMora> generarReportePrestamosMora() {
        try {
            Collection<ReportePrestamoMora> reporte = new ArrayList<>();
            Collection<Estudiante> estudiantes = estudianteDAO.consultar();
            Collection<Libro> libros = libroDAO.consultar();

            for (Prestamo prestamo : bibliotecaProcesos.consultarAtrasos()) {
                Estudiante estudiante = buscarEstudiantePorUsuario(prestamo.getIdUsuario(), estudiantes);

                if (estudiante == null) {
                    continue;
                }

                Libro libro = buscarLibro(prestamo.getIdLibro(), libros);
                String nombreLibro = libro != null ? libro.getTitulo() : "";
                reporte.add(new ReportePrestamoMora(prestamo.getIdPrestamo(), estudiante.getIdEstudiante(), estudiante.getNombre() + " " + estudiante.getApellido(), prestamo.getIdLibro(), nombreLibro, prestamo.getFechaPrestamo(), prestamo.getFechaDevolucionEsperada(), prestamo.getDiasAtraso(), redondear(prestamo.getMontoMulta())));
            }

            return reporte;
        } catch (daoException e) {
            throw new ProcesosException("BD_ERROR", "No se pudo generar el reporte de préstamos en mora", e);
        }
    }

    // Estas consultas son solo de lectura. Los PDF usan los metodos completos de arriba.
    public Map<String, Object> paginaSolventes(int grado, int seccion, int ciclo, int mensualidades,
            int pagina, int limite) {
        validarPagina(pagina, limite);
        if (grado < 0 || seccion < 0 || ciclo < 0 || mensualidades < 0) {
            throw new ProcesosException("DATOS_INVALIDOS", "Filtros de reporte no válidos");
        }
        try {
            Collection<ReporteSolventeMoroso> filas = new ArrayList<>();
            Collection<Estudiante> estudiantes = estudianteDAO.consultar();
            Collection<Grado> grados = gradoDAO.consultar();
            Collection<Seccion> secciones = seccionDAO.consultar();
            for (Inscripcion i : inscripcionDAO.consultarReporte(grado, seccion, ciclo, pagina, limite)) {
                Estudiante e = buscarEstudiante(i.getIdEstudiante(), estudiantes);
                if (e == null) continue;
                Grado g = buscarGrado(i.getIdGrado(), grados);
                Seccion sec = buscarSeccion(i.getIdSeccion(), secciones);
                double pendiente = pagoProcesos.consultarEstadoCuentaPorCiclo(e.getIdEstudiante(), i.getIdCiclo());
                double vencido = pagoProcesos.consultarSaldoVencido(e.getIdEstudiante(), i.getIdCiclo());
                String estado = vencido > 0.01 ? "MOROSO" : pendiente > 0.01 ? "PENDIENTE" : "SOLVENTE";
                filas.add(new ReporteSolventeMoroso(e.getIdEstudiante(), e.getNombre() + " " + e.getApellido(),
                        i.getIdGrado(), g == null ? "" : g.getNombre(), i.getIdSeccion(),
                        sec == null ? "" : sec.getNombre(), i.getIdCiclo(), redondear(pendiente), estado));
            }
            return respuestaPagina(filas, inscripcionDAO.contarReporte(grado, seccion, ciclo), pagina, limite);
        } catch (daoException e) {
            throw new ProcesosException("BD_ERROR", "No se pudo consultar el reporte paginado", e);
        }
    }

    public Map<String, Object> paginaPrestamosMora(int pagina, int limite) {
        validarPagina(pagina, limite);
        try {
            PrestamoDao dao = new PrestamoDao();
            Collection<ReportePrestamoMora> filas = new ArrayList<>();
            Collection<Estudiante> estudiantes = estudianteDAO.consultar();
            Collection<Libro> libros = libroDAO.consultar();
            for (Prestamo prestamo : dao.consultarMoraEstudiantes(pagina, limite)) {
                bibliotecaProcesos.controlarAtraso(prestamo);
                Estudiante estudiante = buscarEstudiantePorUsuario(prestamo.getIdUsuario(), estudiantes);
                if (estudiante == null) continue;
                Libro libro = buscarLibro(prestamo.getIdLibro(), libros);
                filas.add(new ReportePrestamoMora(prestamo.getIdPrestamo(), estudiante.getIdEstudiante(),
                        estudiante.getNombre() + " " + estudiante.getApellido(), prestamo.getIdLibro(),
                        libro == null ? "" : libro.getTitulo(), prestamo.getFechaPrestamo(),
                        prestamo.getFechaDevolucionEsperada(), prestamo.getDiasAtraso(), redondear(prestamo.getMontoMulta())));
            }
            return respuestaPagina(filas, dao.contarMoraEstudiantes(), pagina, limite);
        } catch (daoException e) {
            throw new ProcesosException("BD_ERROR", "No se pudo consultar la mora paginada", e);
        }
    }

    public Map<String, Object> paginaHistorialPagos(int idEstudiante, LocalDate inicio, LocalDate fin,
            int mensualidades, int pagina, int limite) {
        validarPagina(pagina, limite);
        ReporteHistorialPago completo = generarReporteHistorialPagos(idEstudiante, inicio, fin, mensualidades);
        try {
            Collection<Pago> pagos = pagoDAO.consultarReporte(idEstudiante, inicio, fin, pagina, limite);
            Collection<Empleado> empleados = new EmpleadoDao().consultar();
            for (Pago pago : pagos) {
                for (Empleado empleado : empleados) {
                    if (empleado.getIdEmpleado() == pago.getIdEmpleado()) {
                        pago.setNombreEmpleado(empleado.getNombre() + " " + empleado.getApellido());
                        break;
                    }
                }
            }
            Map<String, Object> respuesta = new HashMap<>();
            respuesta.put("estudiante", completo.getEstudiante());
            respuesta.put("idEstudiante", completo.getIdEstudiante());
            respuesta.put("totalPagado", completo.getTotalPagado());
            respuesta.put("saldoPendiente", completo.getSaldoPendiente());
            respuesta.put("estadoCuenta", completo.getEstadoCuenta());
            respuesta.put("pagos", pagos);
            respuesta.put("total", pagoDAO.contarReporte(idEstudiante, inicio, fin));
            respuesta.put("pagina", pagina);
            respuesta.put("limite", limite);
            return respuesta;
        } catch (daoException e) {
            throw new ProcesosException("BD_ERROR", "No se pudo consultar el historial paginado", e);
        }
    }

    public Map<String, Object> paginaIngresos(LocalDate inicio, LocalDate fin, int pagina, int limite) {
        validarPagina(pagina, limite);
        ReporteIngresos completo = generarReporteIngresos(inicio, fin);
        Map<String, Object> respuesta = new HashMap<>();
        respuesta.put("totalIngresos", completo.getTotalIngresos());
        respuesta.put("detalle", cortarPagina(completo.getDetalle(), pagina, limite));
        respuesta.put("total", completo.getDetalle().size());
        respuesta.put("pagina", pagina);
        respuesta.put("limite", limite);
        return respuesta;
    }

    public Map<String, Object> paginaRendimiento(int grado, int seccion, int ciclo,
            int paginaCursos, int paginaEstudiantes, int limite) {
        validarPagina(paginaCursos, limite);
        validarPagina(paginaEstudiantes, limite);
        ReporteRendimiento completo = generarReporteRendimiento(grado, seccion, ciclo);
        Map<String, Object> respuesta = new HashMap<>();
        respuesta.put("grado", completo.getGrado());
        respuesta.put("seccion", completo.getSeccion());
        respuesta.put("idGrado", completo.getIdGrado());
        respuesta.put("idSeccion", completo.getIdSeccion());
        respuesta.put("idCiclo", completo.getIdCiclo());
        respuesta.put("cursoMayorDificultad", completo.getCursoMayorDificultad());
        respuesta.put("promediosCursos", cortarPagina(completo.getPromediosCursos(), paginaCursos, limite));
        respuesta.put("mejoresPromedios", cortarPagina(completo.getMejoresPromedios(), paginaEstudiantes, limite));
        respuesta.put("totalCursos", completo.getPromediosCursos().size());
        respuesta.put("totalEstudiantes", completo.getMejoresPromedios().size());
        respuesta.put("paginaCursos", paginaCursos);
        respuesta.put("paginaEstudiantes", paginaEstudiantes);
        respuesta.put("limite", limite);
        return respuesta;
    }

    private void validarPagina(int pagina, int limite) {
        if (pagina < 1 || limite < 1 || limite > 100 || (long)pagina * limite > Integer.MAX_VALUE) {
            throw new ProcesosException("DATOS_INVALIDOS", "La página o el límite no son válidos");
        }
    }

    private <T> Map<String, Object> respuestaPagina(Collection<T> filas, int total, int pagina, int limite) {
        return Map.of("datos", filas, "total", total, "pagina", pagina, "limite", limite);
    }

    private <T> Collection<T> cortarPagina(Collection<T> datos, int pagina, int limite) {
        List<T> lista = new ArrayList<>(datos);
        int inicio = Math.min(lista.size(), (pagina - 1) * limite);
        int fin = Math.min(lista.size(), inicio + limite);
        return new ArrayList<>(lista.subList(inicio, fin));
    }

    private ReportePromedioCurso calcularPromedioCurso(int idCurso, int idSeccion, int idCiclo, String nombreCurso, Collection<Asignacion> asignaciones, Collection<Zona> zonas, Collection<Nota> notas, Collection<Inscripcion> inscripciones) {
        double suma = 0;
        int estudiantesEvaluados = 0;

        for (Inscripcion inscripcion : inscripciones) {
            if (inscripcion.getIdSeccion() != idSeccion || inscripcion.getIdCiclo() != idCiclo) {
                continue;
            }

            Double notaCurso = calcularNotaCursoEstudiante(idCurso, inscripcion.getIdInscripcion(), idSeccion, idCiclo, asignaciones, zonas, notas);

            if (notaCurso != null) {
                suma += notaCurso;
                estudiantesEvaluados++;
            }
        }

        double promedio = estudiantesEvaluados > 0 ? redondear(suma / estudiantesEvaluados) : 0;
        return new ReportePromedioCurso(idCurso, nombreCurso, idSeccion, idCiclo, promedio, estudiantesEvaluados);
    }

    private Double calcularNotaCursoEstudiante(int idCurso, int idInscripcion, int idSeccion, int idCiclo, Collection<Asignacion> asignaciones, Collection<Zona> zonas, Collection<Nota> notas) {
        double notaFinal = 0;
        boolean tieneNotas = false;

        for (Asignacion asignacion : asignaciones) {
            if (asignacion.getIdCurso() != idCurso || asignacion.getIdSeccion() != idSeccion || asignacion.getIdCiclo() != idCiclo) {
                continue;
            }

            for (Zona zona : zonas) {
                if (zona.getIdAsignacion() != asignacion.getIdAsignacion()) {
                    continue;
                }

                for (Nota nota : notas) {
                    if (nota.getIdInscripcion() == idInscripcion && nota.getIdZona() == zona.getIdZona()) {
                        notaFinal += nota.getValor() * zona.getPorcentaje() / 100.0;
                        tieneNotas = true;
                    }
                }
            }
        }

        return tieneNotas ? redondear(notaFinal) : null;
    }

    private boolean existeAsignacionCurso(int idCurso, int idSeccion, int idCiclo, Collection<Asignacion> asignaciones) {
        for (Asignacion asignacion : asignaciones) {
            if (asignacion.getIdCurso() == idCurso && asignacion.getIdSeccion() == idSeccion && asignacion.getIdCiclo() == idCiclo) {
                return true;
            }
        }

        return false;
    }

    private Estudiante buscarEstudiante(int idEstudiante, Collection<Estudiante> estudiantes) {
        for (Estudiante estudiante : estudiantes) {
            if (estudiante.getIdEstudiante() == idEstudiante) {
                return estudiante;
            }
        }

        return null;
    }

    private Estudiante buscarEstudiantePorUsuario(int idUsuario, Collection<Estudiante> estudiantes) {
        for (Estudiante estudiante : estudiantes) {
            if (estudiante.getIdUsuario() == idUsuario) {
                return estudiante;
            }
        }

        return null;
    }

    private Grado buscarGrado(int idGrado, Collection<Grado> grados) {
        for (Grado grado : grados) {
            if (grado.getIdGrado() == idGrado) {
                return grado;
            }
        }

        return null;
    }

    private Seccion buscarSeccion(int idSeccion, Collection<Seccion> secciones) {
        for (Seccion seccion : secciones) {
            if (seccion.getIdSeccion() == idSeccion) {
                return seccion;
            }
        }

        return null;
    }

    private Curso buscarCurso(int idCurso, Collection<Curso> cursos) {
        for (Curso curso : cursos) {
            if (curso.getIdCurso() == idCurso) {
                return curso;
            }
        }

        return null;
    }

    private Libro buscarLibro(int idLibro, Collection<Libro> libros) {
        for (Libro libro : libros) {
            if (libro.getIdLibro() == idLibro) {
                return libro;
            }
        }

        return null;
    }

    private ReporteIngresoConcepto buscarIngresoConcepto(String concepto, Collection<ReporteIngresoConcepto> ingresos) {
        for (ReporteIngresoConcepto ingreso : ingresos) {
            if (ingreso.getConcepto().equalsIgnoreCase(concepto)) {
                return ingreso;
            }
        }

        return null;
    }

    private void ordenarMejoresPromedios(ArrayList<ReportePromedioEstudiante> estudiantes) {
        for (int i = 0; i < estudiantes.size() - 1; i++) {
            for (int j = i + 1; j < estudiantes.size(); j++) {
                if (estudiantes.get(j).getPromedioGeneral() > estudiantes.get(i).getPromedioGeneral()) {
                    ReportePromedioEstudiante temporal = estudiantes.get(i);
                    estudiantes.set(i, estudiantes.get(j));
                    estudiantes.set(j, temporal);
                }
            }
        }
    }

    private void validarRangoFechas(LocalDate fechaInicio, LocalDate fechaFin) {
        if (fechaInicio != null && fechaFin != null && fechaInicio.isAfter(fechaFin)) {
            throw new ProcesosException("DATOS_INVALIDOS", "La fecha inicial no puede ser posterior a la fecha final");
        }
    }

    private boolean fechaDentroRango(LocalDate fecha, LocalDate fechaInicio, LocalDate fechaFin) {
        if (fecha == null) {
            return false;
        }

        if (fechaInicio != null && fecha.isBefore(fechaInicio)) {
            return false;
        }

        if (fechaFin != null && fecha.isAfter(fechaFin)) {
            return false;
        }

        return true;
    }

    private double redondear(double valor) {
        return Math.round(valor * 100.0) / 100.0;
    }
}
