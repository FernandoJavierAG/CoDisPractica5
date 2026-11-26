package alvarezferro.codis.API;

import java.io.Serializable;
import java.rmi.RemoteException;

public class Usuario implements Serializable {
    private String userName;
    private CallbackHandlerInterface referencia;

    public Usuario(String userName, CallbackHandlerInterface referencia) {
        this.userName = userName;
        this.referencia = referencia;
    }

    public String getUserName() {
        return userName;
    }

    public CallbackHandlerInterface getReferencia() {
        return referencia;
    }

    public void notificarInicioSesion(Usuario usuario) throws RemoteException {    
        referencia.notificarInicioSesion(usuario);
    }

    public void notificarCierreSesion(String usuario) throws RemoteException {    
        referencia.notificarCierreSesion(usuario);
    }

    public void notificarSolicitudAmistad(String solicitante) throws RemoteException {
        referencia.notificarSolicitudAmistad(solicitante);
    }

    public void notificarSolicitudAceptada(String destinatario) throws RemoteException {
        referencia.notificarSolicitudAceptada(destinatario);
    }
}
