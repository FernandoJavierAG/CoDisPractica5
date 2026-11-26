package alvarezferro.codis.Servidor;

import java.rmi.RemoteException;
import java.rmi.server.UnicastRemoteObject;
import java.util.Set;

import alvarezferro.codis.API.CallbackHandlerInterface;
import alvarezferro.codis.API.MessageHandlerInterface;
import alvarezferro.codis.API.SignUpHandlerInterface;
import alvarezferro.codis.API.Usuario;
import alvarezferro.codis.API.Excepciones.AuthException;
import alvarezferro.codis.API.Excepciones.ForbiddenActionException;
import alvarezferro.codis.API.Excepciones.ServerException;
import alvarezferro.codis.Servidor.BBDD.FachadaBaseDatos;

public class SignUpHandler extends UnicastRemoteObject implements SignUpHandlerInterface {
    private FachadaBaseDatos fbdd;

    public SignUpHandler(FachadaBaseDatos fbdd) throws RemoteException {
        this.fbdd = fbdd;
    }

    protected void autenticarUsuario(String userName, String password) 
        throws ServerException, AuthException 
    {
        Servidor.logMessage("Autenticando usuario "+ userName +" con contraseña "+password);

        fbdd.autenticarUsuario(userName, password);
    }

    protected Set<String> getAmigos(String usuario) 
        throws ServerException 
    {
        return fbdd.getAmigos(usuario);
    }
    
    public void inscribirUsuario(String userName, String password) 
        throws ServerException, ForbiddenActionException
    {
        Servidor.logMessage("Registrando usuario "+ userName +" con contraseña "+password);

        fbdd.inscribirUsuario(userName, password);
    }

    public void modificarContrasena(String userName, String oldPassword, String newPassword)
        throws ServerException, AuthException
    {
        autenticarUsuario(userName, newPassword);

        Servidor.logMessage("Actualizando la contraseña del usuario "+ userName +" a "+newPassword);

        fbdd.modificarContrasena(userName, newPassword);
    }

    public void conectarCliente(String userName, String password, CallbackHandlerInterface cliente) 
        throws ServerException, AuthException
    {
        autenticarUsuario(userName, password);

        Usuario nuevoUsuario = new Usuario(userName, cliente);
        Servidor.addUsuario(nuevoUsuario);
        Servidor.notificarInicioSesion(nuevoUsuario);        

        Set<String> solicitudesPendientes = fbdd.getSolicitudesPendientes(userName);;
        for(String solicitante : solicitudesPendientes) {
            try {
                nuevoUsuario.notificarSolicitudAmistad(solicitante);
            } catch(RemoteException err) {}
        }

        Set<Usuario> amigos = Servidor.getUsuarios(getAmigos(userName));

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
    }

    public void cerrarSesion(String userName, String password) 
        throws ServerException, AuthException
    {
        autenticarUsuario(userName, password);

        Servidor.notificarCierreSesion(userName);

        Servidor.removeUsuario(userName);
    }

    

    public void enviarSolicitudAmistad(String userName, String password, String destinatario) 
        throws ServerException, AuthException, ForbiddenActionException
    {
        autenticarUsuario(userName, password);

        Servidor.logMessage("Registrando solicitud de amistad de "+userName+" a "+destinatario);

        Usuario usrDestinatario = Servidor.getUsuario(destinatario);

        if(usrDestinatario != null) { // Esta en linea en este momento.
            try {
                usrDestinatario.notificarSolicitudAmistad(userName);
            } catch(RemoteException err) {
                Servidor.logMessage("\tNo se ha podido notificar la solicitud:\n\t\t"+err.getMessage());
            } // La ignoramos. Ya sera notificado la proxima vez que inicie sesión.
        }         

        fbdd.enviarSolicitudAmistad(userName, destinatario);
    }

    public void aceptarSolicitudAmistad(String userName, String password, String solicitante) 
        throws ServerException, AuthException, ForbiddenActionException
    {
        autenticarUsuario(userName, password);

        Servidor.logMessage("Registrando aceptacion de amistad de "+solicitante+" a "+userName);
        
        fbdd.aceptarSolicitudAmistad(userName, solicitante);

        Usuario usrSolicitante = Servidor.getUsuario(solicitante);
        Usuario usrDestinatario = Servidor.getUsuario(userName);
        if(usrSolicitante != null) {
            try {
                usrSolicitante.notificarSolicitudAceptada(userName);
            } catch(RemoteException err) {
                Servidor.logMessage("\tNo se ha podido notificar la aceptacion:\n\t\t"+err.getMessage());
            }
        }

        try {
        if(usrSolicitante != null)
            usrSolicitante.notificarInicioSesion(usrDestinatario);
        if(usrDestinatario != null)
            usrDestinatario.notificarInicioSesion(usrSolicitante);
        } catch(RemoteException err) {}        
    }

    public MessageHandlerInterface iniciarChat(String userName, String password, String interlocutor) 
        throws ServerException, AuthException, ForbiddenActionException
    {
        autenticarUsuario(userName, password);

        if(!getAmigos(userName).contains(interlocutor))
            throw new ForbiddenActionException("El usuario con el que se desea iniciar un chat no es amigo del solicitante");

        Servidor.logMessage("Inciando chat entre "+ userName +" con contraseña "+ interlocutor);
        

        Usuario usrInterlocutor = Servidor.getUsuario(interlocutor);
        if(usrInterlocutor == null)
            return null;

        try {
            return usrInterlocutor.getReferencia().iniciarChat(userName);
        } catch(RemoteException err) {
            return null;
        }
    }

}
