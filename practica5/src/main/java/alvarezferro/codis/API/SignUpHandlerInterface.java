package alvarezferro.codis.API;

import java.rmi.Remote;
import java.rmi.RemoteException;
import java.sql.SQLException;

import alvarezferro.codis.API.Excepciones.AuthException;
import alvarezferro.codis.API.Excepciones.ForbiddenActionException;
import alvarezferro.codis.API.Excepciones.ServerException;

// Objeto remoto del Servidor, empleado en la comunicación con clientes.
// Todas las funciones deben solicitar autenticación.

// Los valores de retorno son codigos de error, con valores enumerados en la clase CodigosError de este paquete.
public interface SignUpHandlerInterface extends Remote {

    // Función que llama un cliente para crear una cuenta en el servidor.
    public void inscribirUsuario(String userName, String password) 
        throws RemoteException, ServerException, ForbiddenActionException;

    // Funcion que llama un cliente para modificar su contraseña.
    public void modificarContrasena(String userName, String oldPassword, String newPassword) 
        throws RemoteException, ServerException, AuthException;

    // Funcion que llama un cliente para iniciar sesion en el servidor y pasar a estar en linea.
    public void conectarCliente(String userName, String password, CallbackHandlerInterface cliente) 
        throws RemoteException, ServerException, AuthException;

    // Funcion que llama un cliente para cerrar sesion en el servidor.
    public void cerrarSesion(String userName, String password) 
        throws RemoteException, ServerException, AuthException;
    
    // Funcion que llama un cliente para enviar una solicitud de amistad al usuario con nombre 'destinatario'.
    public void enviarSolicitudAmistad(String userName, String password, String destinatario) 
        throws RemoteException, ServerException, AuthException, ForbiddenActionException;

    // Funcion que llama un cliente para aceptar una solicitud de amistad que le envió previamente el usuario 'solicitante'.
    public void aceptarSolicitudAmistad(String userName, String password, String solicitante) 
        throws RemoteException, ServerException, AuthException, ForbiddenActionException;

    // Funcion que llama un cliente para solicitar el inicio de un chat con 'interlocutor'
    public MessageHandlerInterface iniciarChat(String userName, String password, String interlocutor) 
        throws RemoteException, ServerException, AuthException, ForbiddenActionException;

}
