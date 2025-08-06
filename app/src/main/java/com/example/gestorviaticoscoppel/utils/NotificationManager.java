package com.example.gestorviaticoscoppel.utils;

import com.example.gestorviaticoscoppel.dao.NotificacionDAO;
import com.example.gestorviaticoscoppel.dao.UserDAO;
import com.example.gestorviaticoscoppel.models.Notificacion;
import com.example.gestorviaticoscoppel.models.User;
import java.util.List;

public class NotificationManager {

    private static boolean verificandoNotificacionesRH = false;

    public static void crearNotificacionSolicitudRechazada(String idUsuario, String folioViaje) {
        Notificacion notificacion = new Notificacion(
                idUsuario,
                Notificacion.TIPO_SOLICITUD_RECHAZADA,
                "Solicitud Rechazada",
                "Su solicitud de viáticos para el folio " + folioViaje + " ha sido rechazada por RH. Favor de validar la situacion.",
                folioViaje
        );

        NotificacionDAO.crearNotificacion(notificacion, new NotificacionDAO.NotificacionCallback() {
            @Override
            public void onSuccess(String message) {
            }

            @Override
            public void onError(String error) {
            }
        });
    }

    public static void crearNotificacionViaticosAutorizados(String idUsuario, String folioViaje) {
        Notificacion notificacion = new Notificacion(
                idUsuario,
                Notificacion.TIPO_VIATICOS_AUTORIZADOS,
                "Viáticos Autorizados",
                "Sus viáticos para el folio " + folioViaje + " han sido autorizados. Ya están disponibles para su uso.",
                folioViaje
        );

        NotificacionDAO.crearNotificacion(notificacion, new NotificacionDAO.NotificacionCallback() {
            @Override
            public void onSuccess(String message) {
            }

            @Override
            public void onError(String error) {
            }
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
            public void onSuccess(String message) {
            }

            @Override
            public void onError(String error) {
            }
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
            public void onSuccess(String message) {
            }

            @Override
            public void onError(String error) {
            }
        });
    }

    public static void verificarYCrearNotificacionesParaTodosRH() {
        if (verificandoNotificacionesRH) {
            return;
        }

        verificandoNotificacionesRH = true;

        UserDAO.obtenerUsuariosPorRol("RH", new UserDAO.UsuarioListCallback() {
            @Override
            public void onSuccess(List<User> usuariosRH) {
                if (usuariosRH != null && !usuariosRH.isEmpty()) {
                    for (User userRH : usuariosRH) {
                        verificarYCrearNotificacionesRH(userRH.getIdUsuario());
                    }
                }
                verificandoNotificacionesRH = false;
            }

            @Override
            public void onError(String error) {
                verificandoNotificacionesRH = false;
            }
        });
    }

    public static void verificarYCrearNotificacionesRH(String idUsuarioRH) {
        NotificacionDAO.verificarNotificacionExistente(idUsuarioRH, Notificacion.TIPO_SOLICITUDES_PENDIENTES,
                new NotificacionDAO.NotificacionCountCallback() {
                    @Override
                    public void onSuccess(boolean yaExisteSolicitudes) {
                        NotificacionDAO.verificarNotificacionExistente(idUsuarioRH, Notificacion.TIPO_VIAJES_PENDIENTES,
                                new NotificacionDAO.NotificacionCountCallback() {
                                    @Override
                                    public void onSuccess(boolean yaExisteViajes) {
                                        if (!yaExisteSolicitudes) {
                                            verificarSolicitudesPendientesRH(idUsuarioRH);
                                        }

                                        if (!yaExisteViajes) {
                                            verificarViajesPendientesRH(idUsuarioRH);
                                        }
                                    }

                                    @Override
                                    public void onError(String error) {
                                    }
                                });
                    }

                    @Override
                    public void onError(String error) {
                    }
                });
    }

    private static void verificarSolicitudesPendientesRH(String idUsuarioRH) {
        NotificacionDAO.verificarSolicitudesPendientes(new NotificacionDAO.NotificacionCountCallback() {
            @Override
            public void onSuccess(boolean tienePendientes) {
                if (tienePendientes) {
                    crearNotificacionSolicitudesPendientesRH(idUsuarioRH);
                }
            }

            @Override
            public void onError(String error) {
            }
        });
    }

    private static void verificarViajesPendientesRH(String idUsuarioRH) {
        NotificacionDAO.verificarViajesPendientes(new NotificacionDAO.NotificacionCountCallback() {
            @Override
            public void onSuccess(boolean tienePendientes) {
                if (tienePendientes) {
                    crearNotificacionViajesPendientesRH(idUsuarioRH);
                }
            }

            @Override
            public void onError(String error) {
            }
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
            public void onSuccess(String message) {
            }

            @Override
            public void onError(String error) {
            }
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
            public void onSuccess(String message) {
            }

            @Override
            public void onError(String error) {
            }
        });
    }

    public static void crearNotificacionSolicitudRechazadaConMotivo(String idUsuario, String folioViaje, String motivo) {
        Notificacion notificacion = new Notificacion(
                idUsuario,
                Notificacion.TIPO_SOLICITUD_RECHAZADA,
                "Solicitud Rechazada",
                "Su solicitud de viáticos para el folio " + folioViaje + " ha sido rechazada por RH.\n\nMotivo: " + motivo + "\n\nFavor de validar la situación.",
                folioViaje
        );

        NotificacionDAO.crearNotificacion(notificacion, new NotificacionDAO.NotificacionCallback() {
            @Override
            public void onSuccess(String message) {
            }

            @Override
            public void onError(String error) {
            }
        });
    }

    public static void crearNotificacionViajeRechazadoConMotivo(String idUsuario, String folioViaje, String motivo) {
        Notificacion notificacion = new Notificacion(
                idUsuario,
                Notificacion.TIPO_VIAJE_RECHAZADO,
                "Viaje Rechazado",
                "Su viaje con folio " + folioViaje + " ha sido rechazado en la revisión.\n\nMotivo: " + motivo + "\n\nFavor de contactar mesa de ayuda.",
                folioViaje
        );

        NotificacionDAO.crearNotificacion(notificacion, new NotificacionDAO.NotificacionCallback() {
            @Override
            public void onSuccess(String message) {
            }

            @Override
            public void onError(String error) {
            }
        });
    }
}