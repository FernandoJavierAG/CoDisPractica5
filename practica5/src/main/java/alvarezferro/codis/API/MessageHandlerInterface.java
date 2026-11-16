package alvarezferro.codis.API;

import java.rmi.Remote;
import java.rmi.RemoteException;

public interface MessageHandlerInterface extends Remote {

    public void enviarMensaje(String mensaje) throws RemoteException;

}
