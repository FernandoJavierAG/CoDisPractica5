package alvarezferro.codis.Cliente;

import java.rmi.RemoteException;
import java.rmi.server.UnicastRemoteObject;
import java.util.Set;

import alvarezferro.codis.API.CallbackHandlerInterface;
import alvarezferro.codis.API.MessageHandlerInterface;
import alvarezferro.codis.API.Usuario;
import alvarezferro.codis.Cliente.UI.VentanaPrincipal;

public class CallbackHandler extends UnicastRemoteObject implements CallbackHandlerInterface {
    VentanaPrincipal ventanaPrincipal;

    protected void setVentanaPrincipal(VentanaPrincipal ventanaPrincipal) {
        this.ventanaPrincipal = ventanaPrincipal;
    }

    public CallbackHandler() throws RemoteException {
        
    }

    public void notificarInicioSesion(Usuario usuario) {
        Cliente.addAmigo(usuario);
        
        ventanaPrincipal.notificarInicioSesion(usuario.getUserName());   
    }

    public void notificarCierreSesion(String usuario) {

    }

    public void notificarSolicitudAmistad(String usuario) {

    }

    public void notificarSolicitudAceptada(String destinatario) {

    }

    public void conectarAmigos(Usuario amigos) {

    }

    public void conectarAmigos(Set<Usuario> amigos) {

    }


    public MessageHandlerInterface iniciarChat(String interlocutor) throws RemoteException {
        return new MessageHandler(interlocutor);
    }

}
