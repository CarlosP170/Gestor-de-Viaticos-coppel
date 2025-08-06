USE master;
GO

ALTER LOGIN androiduser WITH PASSWORD = 'Android123!';
GO

ALTER LOGIN androiduser ENABLE;
GO

USE GestorViaticosCoppel;
GO

ALTER ROLE db_owner ADD MEMBER androiduser;
GO

GRANT CONNECT TO androiduser;
GO

SELECT 
    dp.name AS usuario,
    r.name AS rol
FROM sys.database_role_members rm
JOIN sys.database_principals dp ON rm.member_principal_id = dp.principal_id
JOIN sys.database_principals r ON rm.role_principal_id = r.principal_id
WHERE dp.name = 'androiduser';

PRINT 'Usuario androiduser reparado exitosamente';