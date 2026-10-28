/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package com.proyecto2.app.backend.Backend.controller;

import com.proyecto2.app.backend.Backend.Exception.ProcesosException;
import com.proyecto2.app.backend.Backend.Exception.ServicioException;
import com.proyecto2.app.backend.Backend.Model.*;
import com.proyecto2.app.backend.Backend.procesos.AcademicoProcesos;
import com.proyecto2.app.backend.Backend.servicio.AcademicoServicio;
import jakarta.ws.rs.*;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;
import java.util.Collection;
import java.util.Map;
import com.proyecto2.app.backend.Backend.procesos.EstudianteProcesos;
import jakarta.annotation.security.RolesAllowed;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.ws.rs.core.Context;


@Path("academico")
@Consumes(MediaType.APPLICATION_JSON)
@Produces(MediaType.APPLICATION_JSON)
public class AcademicoController {
    
    @GET
    @Path("cursos")
    @RolesAllowed("ADMIN")
    public Response consultarCursos() {
        try {
            return Response.ok(new AcademicoProcesos().consultarCursos()).build();
        } catch (ProcesosException e) {
            return error(e.getCodigo(), e.getMessage());
        }
    }

    @POST
    @Path("cursos")
    @RolesAllowed("ADMIN")
    public Response crearCurso(Curso curso) {
        try {
            new AcademicoServicio().crearCurso(curso);
            return Response.status(Response.Status.CREATED).entity(Map.of("mensaje", "Curso creado correctamente")).build();
        } catch (ServicioException e) {
            return error(e.getCodigo(), e.getMessage());
        }
    }

    @PUT
    @Path("cursos/{idCurso}")
    @RolesAllowed("ADMIN")
    public Response modificarCurso(@PathParam("idCurso") int idCurso, Curso curso) {
        curso.setIdCurso(idCurso);
        try {
            new AcademicoServicio().modificarCurso(curso);
            return Response.ok(Map.of("mensaje", "Curso modificado correctamente")).build();
        } catch (ServicioException e) {
            return error(e.getCodigo(), e.getMessage());
        }
    }

    @GET
    @Path("secciones")
    @RolesAllowed({"ADMIN", "SECRETARIA"})
    public Response consultarSecciones() {
        try {
            return Response.ok(new AcademicoProcesos().consultarSecciones()).build();
        } catch (ProcesosException e) {
            return error(e.getCodigo(), e.getMessage());
        }
    }

    @POST
    @Path("secciones")
    @RolesAllowed("ADMIN")
    public Response crearSeccion(Seccion seccion) {
        try {
            new AcademicoServicio().crearSeccion(seccion);
            return Response.status(Response.Status.CREATED).entity(Map.of("mensaje", "Sección creada correctamente")).build();
        } catch (ServicioException e) {
            return error(e.getCodigo(), e.getMessage());
        }
    }

    @PUT
    @Path("secciones/{idSeccion}")
    @RolesAllowed("ADMIN")
    public Response modificarSeccion(@PathParam("idSeccion") int idSeccion, Seccion seccion) {
        seccion.setIdSeccion(idSeccion);
        try {
            new AcademicoServicio().modificarSeccion(seccion);
            return Response.ok(Map.of("mensaje", "Sección modificada correctamente")).build();
        } catch (ServicioException e) {
            return error(e.getCodigo(), e.getMessage());
        }
    }

    @POST
    @Path("asignaciones")
    @RolesAllowed("ADMIN")
    public Response asignarMaestro(Asignacion asignacion) {
        try {
            new AcademicoServicio().asignarMaestroCurso(asignacion);
            return Response.status(Response.Status.CREATED).entity(Map.of("mensaje", "Maestro asignado correctamente")).build();
        } catch (ServicioException e) {
            return error(e.getCodigo(), e.getMessage());
        }
    }

    @GET
    @Path("maestros/{idEmpleado}/asignaciones")
    @RolesAllowed("ADMIN")
    public Response consultarAsignaciones(@PathParam("idEmpleado") int idEmpleado) {
        try {
            return Response.ok(new AcademicoProcesos().consultarAsignacionesMaestro(idEmpleado)).build();
        } catch (ProcesosException e) {
            return error(e.getCodigo(), e.getMessage());
        }
    }

    @GET
    @Path("mis-asignaciones")
    @RolesAllowed("MAESTRO")
    public Response consultarMisAsignaciones(@Context HttpServletRequest request) {
        try {
            return Response.ok(new AcademicoProcesos().consultarMisCursos(obtenerId(request, "idEmpleado"))).build();
        } catch (ProcesosException e) {
            return error(e.getCodigo(), e.getMessage());
        }
    }

    @GET
    @Path("asignaciones/{idAsignacion}/nomina")
    @RolesAllowed("MAESTRO")
    public Response consultarNomina(@PathParam("idAsignacion") int idAsignacion, @Context HttpServletRequest request) {
        try {
            return Response.ok(new AcademicoProcesos().consultarNominaMaestro(idAsignacion, obtenerId(request, "idEmpleado"))).build();
        } catch (ProcesosException e) {
            return error(e.getCodigo(), e.getMessage());
        }
    }

    @GET
    @Path("inscripciones")
    @RolesAllowed("SECRETARIA")
    public Response consultarInscripciones(@DefaultValue("1") @QueryParam("pagina") int pagina, @DefaultValue("20") @QueryParam("limite") int limite) {
        try {
            AcademicoProcesos procesos = new AcademicoProcesos();
            Collection<Inscripcion> datos = procesos.consultarInscripciones(pagina, limite);
            return Response.ok(Map.of("datos", datos, "pagina", pagina, "limite", limite, "total", procesos.contarInscripciones())).build();
        } catch (ProcesosException e) {
            return error(e.getCodigo(), e.getMessage());
        }
    }

    @POST
    @Path("inscripciones")
    @RolesAllowed("SECRETARIA")
    public Response inscribirEstudiante(Inscripcion inscripcion) {
        try {
            new AcademicoServicio().inscribirEstudiante(inscripcion);
            return Response.status(Response.Status.CREATED).entity(Map.of("mensaje", "Estudiante inscrito correctamente")).build();
        } catch (ServicioException e) {
            return error(e.getCodigo(), e.getMessage());
        }
    }

    @GET
    @Path("asignaciones/{idAsignacion}/asistencias")
    @RolesAllowed("MAESTRO")
    public Response consultarAsistencias(@PathParam("idAsignacion") int idAsignacion, @Context HttpServletRequest request) {
        try {
            return Response.ok(new AcademicoProcesos().consultarAsistencia(idAsignacion, obtenerId(request, "idEmpleado"))).build();
        } catch (ProcesosException e) {
            return error(e.getCodigo(), e.getMessage());
        }
    }

    @POST
    @Path("asignaciones/{idAsignacion}/asistencias")
    @RolesAllowed("MAESTRO")
    public Response registrarAsistencia(@PathParam("idAsignacion") int idAsignacion, Asistencia asistencia, @Context HttpServletRequest request) {
        asistencia.setIdAsignacion(idAsignacion);
        try {
            new AcademicoServicio().registrarAsistencia(asistencia, obtenerId(request, "idEmpleado"));
            return Response.status(Response.Status.CREATED).entity(Map.of("mensaje", "Asistencia registrada correctamente")).build();
        } catch (ProcesosException e) {
            return error(e.getCodigo(), e.getMessage());
        } catch (ServicioException e) {
            return error(e.getCodigo(), e.getMessage());
        }
    }

    @GET
    @Path("asignaciones/{idAsignacion}/zonas")
    @RolesAllowed("MAESTRO")
    public Response consultarZonas(@PathParam("idAsignacion") int idAsignacion, @Context HttpServletRequest request) {
        try {
            return Response.ok(new AcademicoProcesos().consultarZonas(idAsignacion, obtenerId(request, "idEmpleado"))).build();
        } catch (ProcesosException e) {
            return error(e.getCodigo(), e.getMessage());
        }
    }

    @POST
    @Path("asignaciones/{idAsignacion}/zonas")
    @RolesAllowed("MAESTRO")
    public Response definirZona(@PathParam("idAsignacion") int idAsignacion, Zona zona, @Context HttpServletRequest request) {
        zona.setIdAsignacion(idAsignacion);
        try {
            new AcademicoServicio().definirZona(zona, obtenerId(request, "idEmpleado"));
            return Response.status(Response.Status.CREATED).entity(Map.of("mensaje", "Zona definida correctamente")).build();
        } catch (ProcesosException e) {
            return error(e.getCodigo(), e.getMessage());
        } catch (ServicioException e) {
            return error(e.getCodigo(), e.getMessage());
        }
    }

    @PUT
    @Path("asignaciones/{idAsignacion}/zonas/{idZona}")
    @RolesAllowed("MAESTRO")
    public Response actualizarZona(@PathParam("idAsignacion") int idAsignacion,
            @PathParam("idZona") int idZona, Zona zona, @Context HttpServletRequest request) {
        if (zona == null) return error("DATOS_INVALIDOS", "Debes ingresar los datos de la zona");
        zona.setIdZona(idZona);
        zona.setIdAsignacion(idAsignacion);
        try {
            new AcademicoServicio().actualizarZona(zona, obtenerId(request, "idEmpleado"));
            return Response.ok(Map.of("mensaje", "Zona actualizada correctamente")).build();
        } catch (ProcesosException e) {
            return error(e.getCodigo(), e.getMessage());
        } catch (ServicioException e) {
            return error(e.getCodigo(), e.getMessage());
        }
    }

    @GET
    @Path("asignaciones/{idAsignacion}/notas")
    @RolesAllowed("MAESTRO")
    public Response consultarNotas(@PathParam("idAsignacion") int idAsignacion, @Context HttpServletRequest request) {
        try {
            return Response.ok(new AcademicoProcesos().consultarNotas(idAsignacion, obtenerId(request, "idEmpleado"))).build();
        } catch (ProcesosException e) {
            return error(e.getCodigo(), e.getMessage());
        }
    }

    @PUT
    @Path("notas")
    @RolesAllowed("MAESTRO")
    public Response modificarNota(Nota nota, @Context HttpServletRequest request) {
        try {
            new AcademicoServicio().modificarNota(nota, obtenerId(request, "idEmpleado"));
            return Response.ok(Map.of("mensaje", "Nota registrada o modificada correctamente")).build();
        } catch (ProcesosException e) {
            return error(e.getCodigo(), e.getMessage());
        } catch (ServicioException e) {
            return error(e.getCodigo(), e.getMessage());
        }
    }

    @GET
    @Path("mis-notas")
    @RolesAllowed("ESTUDIANTE")
    public Response consultarMisNotas(@Context HttpServletRequest request) {
        try {
            int idUsuario = obtenerId(request, "idUsuario");
            Estudiante estudiante = new EstudianteProcesos().consultarPerfil(idUsuario);
            return Response.ok(new AcademicoProcesos().consultarHistorialNotas(estudiante.getIdEstudiante())).build();
        } catch (ProcesosException e) {
            return error(e.getCodigo(), e.getMessage());
        }
    }

    @GET
    @Path("carreras")
    @RolesAllowed("ADMIN")
    public Response consultarCarreras() {
        try {
            return Response.ok(new AcademicoProcesos().consultarCarreras()).build();
        } catch (ProcesosException e) {
            return error(e.getCodigo(), e.getMessage());
        }
    }

    // Reutiliza la activación validada que ya existe en AcademicoServicio.
    @PUT
    @Path("carreras/{idCarrera}/activar")
    @RolesAllowed("ADMIN")
    public Response activarCarrera(@PathParam("idCarrera") int idCarrera) {
        try {
            new AcademicoServicio().activarCarrera(idCarrera);
            return Response.ok(Map.of("mensaje", "Carrera activada correctamente")).build();
        } catch (ServicioException e) {
            return error(e.getCodigo(), e.getMessage());
        }
    }

    @POST
    @Path("carreras")
    @RolesAllowed("ADMIN")
    public Response crearCarrera(Carrera carrera) {
        try {
            new AcademicoServicio().crearCarrera(carrera);
            return Response.status(Response.Status.CREATED).entity(Map.of("mensaje", "Carrera creada correctamente")).build();
        } catch (ServicioException e) {
            return error(e.getCodigo(), e.getMessage());
        }
    }

    @PUT
    @Path("carreras/{idCarrera}")
    @RolesAllowed("ADMIN")
    public Response modificarCarrera(@PathParam("idCarrera") int idCarrera, Carrera carrera) {
        carrera.setIdCarrera(idCarrera);
        try {
            new AcademicoServicio().actualizarCarrera(carrera);
            return Response.ok(Map.of("mensaje", "Carrera modificada correctamente")).build();
        } catch (ServicioException e) {
            return error(e.getCodigo(), e.getMessage());
        }
    }

    @DELETE
    @Path("carreras/{idCarrera}")
    @RolesAllowed("ADMIN")
    public Response desactivarCarrera(@PathParam("idCarrera") int idCarrera) {
        try {
            new AcademicoServicio().desactivarCarrera(idCarrera);
            return Response.ok(Map.of("mensaje", "Carrera desactivada correctamente")).build();
        } catch (ServicioException e) {
            return error(e.getCodigo(), e.getMessage());
        }
    }

    @POST
    @Path("curriculos")
    @RolesAllowed("ADMIN")
    public Response crearCurriculo(Curriculo curriculo) {
        try {
            new AcademicoServicio().crearCurriculo(curriculo);
            return Response.status(Response.Status.CREATED).entity(Map.of("mensaje", "Currículo creado correctamente")).build();
        } catch (ServicioException e) {
            return error(e.getCodigo(), e.getMessage());
        }
    }

    @PUT
    @Path("curriculos/{idCurriculo}/cursos")
    @RolesAllowed("ADMIN")
    public Response establecerCurriculo(@PathParam("idCurriculo") int idCurriculo, int[] idCursos) {
        try {
            new AcademicoServicio().establecerCurriculo(idCurriculo, idCursos);
            return Response.ok(Map.of("mensaje", "Cursos del currículo actualizados correctamente")).build();
        } catch (ServicioException e) {
            return error(e.getCodigo(), e.getMessage());
        }
    }

    // Listado para la selección de currículos en Angular; usa el DAO existente.
    @GET
    @Path("curriculos/registrados")
    @RolesAllowed("ADMIN")
    public Response listarCurriculos() {
        try {
            return Response.ok(new AcademicoProcesos().consultarCurriculosRegistrados()).build();
        } catch (ProcesosException e) {
            return error(e.getCodigo(), e.getMessage());
        }
    }

    @GET
    @Path("curriculos")
    @RolesAllowed("ADMIN")
    public Response consultarCurriculo(@QueryParam("grado") int idGrado, @QueryParam("ciclo") int idCiclo) {
        try {
            return Response.ok(new AcademicoProcesos().consultarCurriculo(idGrado, idCiclo)).build();
        } catch (ProcesosException e) {
            return error(e.getCodigo(), e.getMessage());
        }
    }

    private int obtenerId(HttpServletRequest request, String atributo) {
        Object valor = request.getAttribute(atributo);
        if (!(valor instanceof Integer) || (Integer) valor <= 0) throw new ProcesosException("NO_AUTORIZADO", "No se pudo identificar al usuario autenticado");
        return (Integer) valor;
    }

    private Response error(String codigo, String mensaje) {
        Response.Status estado = "DATOS_INVALIDOS".equals(codigo) ? Response.Status.BAD_REQUEST
                : "NO_ENCONTRADO".equals(codigo) ? Response.Status.NOT_FOUND
                : "CONFLICTO".equals(codigo) ? Response.Status.CONFLICT
                : "NO_AUTORIZADO".equals(codigo) ? Response.Status.FORBIDDEN
                : Response.Status.INTERNAL_SERVER_ERROR;
        return Response.status(estado).entity(Map.of("codigo", codigo, "mensaje", mensaje)).build();
    }
}
