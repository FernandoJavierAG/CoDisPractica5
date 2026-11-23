package alvarezferro.codis.Servidor;

import java.rmi.RemoteException;
import java.rmi.server.UnicastRemoteObject;
import java.util.Set;

import alvarezferro.codis.API.CallbackHandlerInterface;
import alvarezferro.codis.API.MessageHandlerInterface;
import alvarezferro.codis.API.SignUpHandlerInterface;
import alvarezferro.codis.API.Usuario;

public class SignUpHandler extends UnicastRemoteObject implements SignUpHandlerInterface {
    
    public int conectarCliente(String userName, String password, CallbackHandlerInterface cliente) {
        int codError;
        codError = Servidor.autenticarUsuario(userName, password);

        if(codError != 0) 
            return codError;

        Usuario nuevoUsuario = new Usuario(userName, cliente);
        Servidor.addUsuario(nuevoUsuario);
        Servidor.notificarInicioSesion(nuevoUsuario);

        Set<Usuario> amigos = Servidor.getAmigosEnLinea(nuevoUsuario);

        for(int i = 1; i <= 3; i++) try {
            cliente.conectarAmigos(amigos);
            break;
        } catch(RemoteException err) {
            if(i != 3)
                System.out.println("Error al notificar amistades a "+userName+". Reintentando...");
            else
                System.out.println("No se pudo comunicar amistades a "+userName);
        }

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

    public MessageHandlerInterface iniciarChat(String destinatario, String userNameRemitente, String passwordRemitente) {
        int codError;
        codError = Servidor.autenticarUsuario(userNameRemitente, passwordRemitente);

        if(codError != 0)
            return null;

        return Servidor.iniciarChat(destinatario, userNameRemitente);
    }

}
