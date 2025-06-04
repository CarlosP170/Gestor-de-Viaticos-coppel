package com.example.gestorviaticoscoppel.utils;

import com.example.gestorviaticoscoppel.dao.NotificacionDAO;
import com.example.gestorviaticoscoppel.models.Notificacion;

public class NotificationManager {

    public static void crearNotificacionSolicitudRechazada(String idUsuario, String folioViaje) {
        Notificacion notificacion = new Notificacion(
                idUsuario,
                Notificacion.TIPO_SOLICITUD_RECHAZADA,
                "Solicitud Rechazada",
                "Su solicitud de viáticos para el folio " + folioViaje + " ha sido rechazada por RH. Favor de notificarle a Gerente.",
                folioViaje
        );

        NotificacionDAO.crearNotificacion(notificacion, new NotificacionDAO.NotificacionCallback() {
            @Override
            public void onSuccess(String message) {}

            @Override
            public void onError(String error) {}
        });
    }

    public static void crearNotificacionViaticosAutorizados(String idUsuario, String folioViaje) {
        Notificacion notificacion = new Notificacion(
                idUsuario,
                Notificacion.TIPO_VIATICOS_AUTORIZADOS,
                "Viáticos Autorizados",
                "Sus viáticos para el folio " + folioViaje + " han sido autorizados. Ya están disponibles en su cuenta.",
                folioViaje
        );

        NotificacionDAO.crearNotificacion(notificacion, new NotificacionDAO.NotificacionCallback() {
            @Override
            public void onSuccess(String message) {}

            @Override
            public void onError(String error) {}
        });
    }

    public static void crearNotificacionViajeFinalizado(String idUsuario, String folioViaje) {
        Notificacion notificacion = new Notificacion(
                idUsuario,
                Notificacion.TIPO_VIAJE_FINALIZADO,
                "Viaje Finalizado",
                "Su viaje con folio " + folioViaje + " ha sido finalizado exitosamente por RH.",
                folioViaje
        );

        NotificacionDAO.crearNotificacion(notificacion, new NotificacionDAO.NotificacionCallback() {
            @Override
            public void onSuccess(String message) {}

            @Override
            public void onError(String error) {}
        });
    }

    public static void crearNotificacionViajeRechazado(String idUsuario, String folioViaje) {
        Notificacion notificacion = new Notificacion(
                idUsuario,
                Notificacion.TIPO_VIAJE_RECHAZADO,
                "Viaje Rechazado",
                "Su viaje con folio " + folioViaje + " ha sido rechazado en la revisión. Favor de contactar mesa de ayuda.",
                folioViaje
        );

        NotificacionDAO.crearNotificacion(notificacion, new NotificacionDAO.NotificacionCallback() {
            @Override
            public void onSuccess(String message) {}

            @Override
            public void onError(String error) {}
        });
    }

    public static void verificarYCrearNotificacionRH(String idUsuarioRH) {
        NotificacionDAO.verificarSolicitudesPendientes(new NotificacionDAO.NotificacionCountCallback() {
            @Override
            public void onSuccess(boolean tienePendientes) {
                if (tienePendientes) {
                    verificarSiYaExisteNotificacionSolicitudes(idUsuarioRH);
                }
            }

            @Override
            public void onError(String error) {}
        });

        NotificacionDAO.verificarViajesPendientes(new NotificacionDAO.NotificacionCountCallback() {
            @Override
            public void onSuccess(boolean tienePendientes) {
                if (tienePendientes) {
                    verificarSiYaExisteNotificacionViajes(idUsuarioRH);
                }
            }

            @Override
            public void onError(String error) {}
        });
    }

    private static void verificarSiYaExisteNotificacionSolicitudes(String idUsuarioRH) {
        NotificacionDAO.obtenerNotificacionesPorUsuario(idUsuarioRH, new NotificacionDAO.NotificacionListCallback() {
            @Override
            public void onSuccess(java.util.List<Notificacion> notificaciones) {
                boolean yaExisteSolicitudes = false;
                for (Notificacion n : notificaciones) {
                    if (Notificacion.TIPO_SOLICITUDES_PENDIENTES.equals(n.getTipo()) && !n.isLeida()) {
                        yaExisteSolicitudes = true;
                        break;
                    }
                }
                if (!yaExisteSolicitudes) {
                    crearNotificacionSolicitudesPendientesRH(idUsuarioRH);
                }
            }

            @Override
            public void onError(String error) {}
        });
    }

    private static void verificarSiYaExisteNotificacionViajes(String idUsuarioRH) {
        NotificacionDAO.obtenerNotificacionesPorUsuario(idUsuarioRH, new NotificacionDAO.NotificacionListCallback() {
            @Override
            public void onSuccess(java.util.List<Notificacion> notificaciones) {
                boolean yaExisteViajes = false;
                for (Notificacion n : notificaciones) {
                    if (Notificacion.TIPO_VIAJES_PENDIENTES.equals(n.getTipo()) && !n.isLeida()) {
                        yaExisteViajes = true;
                        break;
                    }
                }
                if (!yaExisteViajes) {
                    crearNotificacionViajesPendientesRH(idUsuarioRH);
                }
            }

            @Override
            public void onError(String error) {}
        });
    }

    private static void crearNotificacionSolicitudesPendientesRH(String idUsuarioRH) {
        Notificacion notificacion = new Notificacion(
                idUsuarioRH,
                Notificacion.TIPO_SOLICITUDES_PENDIENTES,
                "Solicitudes Pendientes",
                "Tiene solicitudes de viáticos pendientes de revisar en el sistema.",
                "RH_SOLICITUDES"
        );

        NotificacionDAO.crearNotificacion(notificacion, new NotificacionDAO.NotificacionCallback() {
            @Override
            public void onSuccess(String message) {}

            @Override
            public void onError(String error) {}
        });
    }

    private static void crearNotificacionViajesPendientesRH(String idUsuarioRH) {
        Notificacion notificacion = new Notificacion(
                idUsuarioRH,
                Notificacion.TIPO_VIAJES_PENDIENTES,
                "Viajes en Revisión",
                "Tiene viajes pendientes de supervisión y finalización en el sistema.",
                "RH_VIAJES"
        );

        NotificacionDAO.crearNotificacion(notificacion, new NotificacionDAO.NotificacionCallback() {
            @Override
            public void onSuccess(String message) {}

            @Override
            public void onError(String error) {}
        });
    }
}