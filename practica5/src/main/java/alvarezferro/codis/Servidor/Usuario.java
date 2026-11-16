package alvarezferro.codis.Servidor;

import java.rmi.RemoteException;

import alvarezferro.codis.API.CallbackHandlerInterface;

public class Usuario {
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

    public void notificarInicioSesion(String usuario) throws RemoteException {    
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
