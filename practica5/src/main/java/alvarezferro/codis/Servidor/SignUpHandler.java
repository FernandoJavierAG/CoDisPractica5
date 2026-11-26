package alvarezferro.codis.Servidor;

import java.rmi.RemoteException;
import java.rmi.server.UnicastRemoteObject;
import java.util.Set;

import alvarezferro.codis.API.CallbackHandlerInterface;
import alvarezferro.codis.API.MessageHandlerInterface;
import alvarezferro.codis.API.SignUpHandlerInterface;
import alvarezferro.codis.API.Usuario;

public class SignUpHandler extends UnicastRemoteObject implements SignUpHandlerInterface {

    public SignUpHandler() throws RemoteException {
        super();
    }
    
    public int conectarCliente(String userName, String password, CallbackHandlerInterface cliente) {
        int codError;
        codError = Servidor.autenticarUsuario(userName, password);

        if(codError != 0) 
            return codError;

        Usuario nuevoUsuario = new Usuario(userName, cliente);
        Servidor.addUsuario(nuevoUsuario);
        Servidor.notificarInicioSesion(nuevoUsuario);

        Set<Usuario> amigos = Servidor.getAmigosEnLinea(nuevoUsuario.getUserName());

        for(int i = 1; i <= 3; i++) try {
            Servidor.logMessage("Conectando con los amigos de "+userName+"...");
            cliente.conectarAmigos(amigos);
            Servidor.logMessage("Conexion exitosa con los amigos de "+userName+"...");
            break;
        } catch(RemoteException err) {
            if(i != 3)
                Servidor.logMessage("Error al notificar amistades a "+userName+"\n\t"+err.getMessage()+". Reintentando...");
            else
                Servidor.logMessage("No se pudo comunicar amistades a "+userName);
        }

        return codError;
    }

    public int inscribirCliente(String userName, String password) {
        return Servidor.inscribirUsuario(userName, password);
    }

    public int modificarContrasena(String userName, String oldPassword, String newPassword) {
        int codError;
        codError = Servidor.autenticarUsuario(userName, newPassword);

        if(codError != 0)
            return codError;

        return Servidor.modificarContrasena(userName, newPassword);
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

    public MessageHandlerInterface iniciarChat(String userName, String password, String interlocutor) {
        int codError;
        codError = Servidor.autenticarUsuario(userName, password);

        if(codError != 0)
            return null;

        return Servidor.iniciarChat(userName, interlocutor);
    }

}
