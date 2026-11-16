package alvarezferro.codis.Servidor;

import java.rmi.server.UnicastRemoteObject;

import alvarezferro.codis.API.CallbackHandlerInterface;
import alvarezferro.codis.API.SignUpHandlerInterface;

public class SignUpHandler extends UnicastRemoteObject implements SignUpHandlerInterface {
    
    public int conectarCliente(String userName, String password, CallbackHandlerInterface cliente) {
        int codError;
        codError = Servidor.autenticarUsuario(userName, password);

        if(codError != 0) 
            return codError;

        Usuario nuevoUsuario = new Usuario(userName, cliente);
        Servidor.addUsuario(nuevoUsuario);
        Servidor.notificarInicioSesion(nuevoUsuario);

        return 0;
    }

    public int inscribirCliente(String userName, String password) {
        return Servidor.inscribirUsuario(userName, password);
    }

    public int enviarSolicitudAmistad(String userName, String password, String destinatario) {
        int codError;
        codError = Servidor.autenticarUsuario(userName, password);

        if(codError != 0)
            return codError;

        return Servidor.enviarSolicitudAmistad(userName, destinatario);
    }

    public int aceptarSolicitudAmistad(String userName, String password, String solicitante) {
        int codError;
        codError = Servidor.autenticarUsuario(userName, password);

        if(codError != 0)
            return codError;

        return Servidor.aceptarSolicitudAmistad(userName, solicitante);
    }

}
