CREATE DATABASE GestorViaticosCoppel;
GO

USE GestorViaticosCoppel;
GO

CREATE TABLE Usuarios (
    ID_Usuario NVARCHAR(20) PRIMARY KEY,
    Email NVARCHAR(100) NOT NULL UNIQUE,
    Password_Hash NVARCHAR(255) NOT NULL,
    Rol NVARCHAR(20) NOT NULL CHECK (Rol IN ('Colaborador', 'Gerente', 'RH')),
    Nombre NVARCHAR(100),
    Fecha_Creacion DATETIME DEFAULT GETDATE(),
    Activo BIT DEFAULT 1
);

CREATE TABLE Solicitudes_Viaticos (
    ID_Solicitud INT IDENTITY(1,1) PRIMARY KEY,
    Folio_Viaje NVARCHAR(6) NOT NULL,
    ID_Solicitante NVARCHAR(20) NOT NULL,
    ID_Beneficiario NVARCHAR(20) NOT NULL,
    Monto_Hotel DECIMAL(10,2) DEFAULT 0,
    Monto_Comida DECIMAL(10,2) DEFAULT 0,
    Monto_Transporte DECIMAL(10,2) DEFAULT 0,
    Monto_Gasolina DECIMAL(10,2) DEFAULT 0,
    Estado NVARCHAR(20) DEFAULT 'Pendiente' CHECK (Estado IN ('Pendiente', 'Autorizada', 'Rechazada')),
    Fecha_Solicitud DATETIME DEFAULT GETDATE(),
    Fecha_Revision DATETIME NULL,
    ID_Revisor NVARCHAR(20) NULL,
    Motivo_Rechazo NVARCHAR(500) NULL,
    
    FOREIGN KEY (ID_Solicitante) REFERENCES Usuarios(ID_Usuario),
    FOREIGN KEY (ID_Beneficiario) REFERENCES Usuarios(ID_Usuario),
    FOREIGN KEY (ID_Revisor) REFERENCES Usuarios(ID_Usuario)
);

CREATE TABLE Viajes (
    ID_Viaje INT IDENTITY(1,1) PRIMARY KEY,
    ID_Solicitud INT NOT NULL,
    ID_Usuario NVARCHAR(20) NOT NULL,
    Folio_Viaje NVARCHAR(6) NOT NULL,
    
    Monto_Hotel_Autorizado DECIMAL(10,2) DEFAULT 0,
    Monto_Comida_Autorizado DECIMAL(10,2) DEFAULT 0,
    Monto_Transporte_Autorizado DECIMAL(10,2) DEFAULT 0,
    Monto_Gasolina_Autorizado DECIMAL(10,2) DEFAULT 0,
    
    Monto_Hotel_Gastado DECIMAL(10,2) DEFAULT 0,
    Monto_Comida_Gastado DECIMAL(10,2) DEFAULT 0,
    Monto_Transporte_Gastado DECIMAL(10,2) DEFAULT 0,
    Monto_Gasolina_Gastado DECIMAL(10,2) DEFAULT 0,
    Monto_Otros_Gastado DECIMAL(10,2) DEFAULT 0,
    
    Estado NVARCHAR(20) DEFAULT 'En_Curso' CHECK (Estado IN ('En_Curso', 'Enviado_A_Revision', 'Finalizado', 'Rechazado')),
    
    Fecha_Inicio DATETIME DEFAULT GETDATE(),
    Fecha_Envio_Revision DATETIME NULL,
    Fecha_Finalizacion DATETIME NULL,
    
    Folio_Ticket_Sobrante NVARCHAR(50) NULL,
    ID_Revisor_Final NVARCHAR(20) NULL,
    Motivo_Rechazo NVARCHAR(500) NULL,
    
    FOREIGN KEY (ID_Solicitud) REFERENCES Solicitudes_Viaticos(ID_Solicitud),
    FOREIGN KEY (ID_Usuario) REFERENCES Usuarios(ID_Usuario),
    FOREIGN KEY (ID_Revisor_Final) REFERENCES Usuarios(ID_Usuario)
);

CREATE TABLE Facturas (
    ID_Factura INT IDENTITY(1,1) PRIMARY KEY,
    ID_Viaje INT NOT NULL,
    Categoria NVARCHAR(20) NOT NULL CHECK (Categoria IN ('Hotel', 'Comida', 'Transporte', 'Gasolina', 'Otros')),
    Monto DECIMAL(10,2) NOT NULL,
    Archivo_Nombre NVARCHAR(255) NOT NULL,
    Archivo_Ruta NVARCHAR(500) NULL,
    Archivo_Tipo NVARCHAR(10) NOT NULL,
    Archivo_Contenido_Base64 NVARCHAR(MAX) NULL,
    Fecha_Subida DATETIME DEFAULT GETDATE(),
    
    FOREIGN KEY (ID_Viaje) REFERENCES Viajes(ID_Viaje)
);

INSERT INTO Usuarios (ID_Usuario, Email, Password_Hash, Rol, Nombre) VALUES
('90126701', 'colaborador@coppel.com', '12345678', 'Colaborador', 'Carlos Padilla'),
('90313132', 'gerente@coppel.com', '12345678', 'Gerente', 'Eduardo Gayosso'),
('98875256', 'rh@coppel.com', '12345678', 'RH', 'Natalia Santillan');

CREATE TABLE Notificaciones (
    ID_Notificacion INT IDENTITY(1,1) PRIMARY KEY,
    ID_Usuario NVARCHAR(20) NOT NULL,
    Tipo NVARCHAR(50) NOT NULL,
    Titulo NVARCHAR(200) NOT NULL,
    Mensaje NVARCHAR(500) NOT NULL,
    Fecha_Creacion DATETIME DEFAULT GETDATE(),
    Leida BIT DEFAULT 0,
    Datos_Relacionados NVARCHAR(100) NULL,
    
    FOREIGN KEY (ID_Usuario) REFERENCES Usuarios(ID_Usuario)
);

CREATE INDEX IX_Notificaciones_Usuario_Fecha ON Notificaciones (ID_Usuario, Fecha_Creacion DESC);
CREATE INDEX IX_Notificaciones_Usuario_Leida ON Notificaciones (ID_Usuario, Leida);