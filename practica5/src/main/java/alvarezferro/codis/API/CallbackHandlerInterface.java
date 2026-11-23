package alvarezferro.codis.API;

import java.rmi.Remote;
import java.rmi.RemoteException;
import java.util.Set;

public interface CallbackHandlerInterface extends Remote {
    
    public void notificarInicioSesion(Usuario usuario) throws RemoteException;

    public void notificarCierreSesion(String usuario) throws RemoteException;

    public void notificarSolicitudAmistad(String usuario) throws RemoteException;

    public void notificarSolicitudAceptada(String destinatario) throws RemoteException;

    public void conectarAmigos(Usuario amigos) throws RemoteException;

    public void conectarAmigos(Set<Usuario> amigos) throws RemoteException;

    public MessageHandlerInterface iniciarChat(String interlocutor) throws RemoteException;

}
