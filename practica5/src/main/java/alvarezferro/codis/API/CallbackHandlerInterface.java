package alvarezferro.codis.API;

import java.rmi.Remote;
import java.rmi.RemoteException;
import java.util.Set;

// Objeto remoto del cliente, empleado para la interacción con el Servidor.
public interface CallbackHandlerInterface extends Remote {

    // Funcion llamada por el Servidor cada vez que un amigo pasa a estar en linea.
    public void notificarInicioSesion(Usuario usuario) throws RemoteException;

    // Funcion llamada por el Servidor cada vez que un amigo pasa a estar offline.
    public void notificarCierreSesion(String usuario) throws RemoteException;

    // Funcion llamada por el Servidor cuando 'usuario' le envia una solicitud de amistad.
    public void notificarSolicitudAmistad(String usuario) throws RemoteException;

    // Funcion llamada por el Servidor cuando 'destinatario' acepta una solicitud de amistade que le envió previamente.
    public void notificarSolicitudAceptada(String destinatario) throws RemoteException;


    public void conectarAmigos(Usuario amigos) throws RemoteException;

    public void conectarAmigos(Set<Usuario> amigos) throws RemoteException;

    // Funcion llamada por el Servidor cuando 'interlocutor' solicita iniciar un chat.
    public MessageHandlerInterface iniciarChat(String interlocutor) throws RemoteException;

}
