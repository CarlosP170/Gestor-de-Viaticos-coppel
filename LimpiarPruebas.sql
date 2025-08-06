USE GestorViaticosCoppel;
GO

DELETE F FROM Facturas F 
INNER JOIN Viajes V ON F.ID_Viaje = V.ID_Viaje 
WHERE V.Estado IN ('Finalizado', 'Rechazado');

DELETE FROM Viajes WHERE Estado IN ('Finalizado', 'Rechazado');

DELETE FROM Solicitudes_Viaticos WHERE Estado IN ('Rechazada', 'Autorizada');

DELETE FROM Notificaciones WHERE DATEDIFF(hour, Fecha_Creacion, GETDATE()) > 1;

DELETE FROM Viajes WHERE Estado = 'En_Curso' AND DATEDIFF(day, Fecha_Inicio, GETDATE()) > 2;

DELETE FROM Viajes WHERE Estado = 'Enviado_A_Revision' AND DATEDIFF(day, Fecha_Envio_Revision, GETDATE()) > 1;

DELETE FROM Solicitudes_Viaticos WHERE Estado = 'Pendiente' AND DATEDIFF(day, Fecha_Solicitud, GETDATE()) > 2;

UPDATE Solicitudes_Viaticos SET Motivo_Rechazo = NULL WHERE Motivo_Rechazo IS NOT NULL;

UPDATE Viajes SET Motivo_Rechazo = NULL WHERE Motivo_Rechazo IS NOT NULL;

PRINT 'Datos de prueba limpiados exitosamente';
