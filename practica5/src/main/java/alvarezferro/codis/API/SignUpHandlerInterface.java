package alvarezferro.codis.API;

import java.rmi.Remote;
import java.rmi.RemoteException;

// Objeto remoto del Servidor, empleado en la comunicación con clientes.
// Todas las funciones deben solicitar autenticación.

// Los valores de retorno son codigos de error, con valores enumerados en la clase CodigosError de este paquete.
public interface SignUpHandlerInterface extends Remote {

    // Función que llama un cliente para crear una cuenta en el servidor.
    public int inscribirCliente(String userName, String password) throws RemoteException;

    // Funcion que llama un cliente para modificar su contraseña.
    public int modificarContrasena(String userName, String oldPassword, String newPassword) throws RemoteException;

    // Funcion que llama un cliente para iniciar sesion en el servidor y pasar a estar en linea.
    public int conectarCliente(String userName, String password, CallbackHandlerInterface cliente) throws RemoteException;
    
    // Funcion que llama un cliente para enviar una solicitud de amistad al usuario con nombre 'destinatario'.
    public int enviarSolicitudAmistad(String userName, String password, String destinatario) throws RemoteException;

    // Funcion que llama un cliente para aceptar una solicitud de amistad que le envió previamente el usuario 'solicitante'.
    public int aceptarSolicitudAmistad(String userName, String password, String solicitante) throws RemoteException;

    public MessageHandlerInterface iniciarChat(String userName, String password, String interlocutor) throws RemoteException;

}
