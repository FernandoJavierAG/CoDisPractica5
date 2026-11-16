package alvarezferro.codis.API;

import java.rmi.Remote;
import java.rmi.RemoteException;
import java.rmi.server.RemoteObject;

public interface CallbackHandlerInterface extends Remote {
    

    public void notificarInicioSesion(String usuario) throws RemoteException;

    public void notificarCierreSesion(String usuario) throws RemoteException;

    public void notificarSolicitudAmistad(String usuario) throws RemoteException;

    public void notificarSolicitudAceptada(String destinatario) throws RemoteException;

}
