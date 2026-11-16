package alvarezferro.codis.API;

import java.rmi.Remote;
import java.rmi.RemoteException;

public interface SignUpHandlerInterface extends Remote {

    public int conectarCliente(String userName, String password, CallbackHandlerInterface cliente) throws RemoteException;

    public int inscribirCliente(String userName, String password) throws RemoteException;
    
    public int enviarSolicitudAmistad(String userName, String password, String destinatario);

    public int aceptarSolicitudAmistad(String userName, String password, String solicitante);

}
