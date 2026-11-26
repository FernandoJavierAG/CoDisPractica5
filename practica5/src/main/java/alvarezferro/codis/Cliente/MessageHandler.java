package alvarezferro.codis.Cliente;

import java.rmi.RemoteException;
import java.rmi.server.UnicastRemoteObject;

import javax.swing.JFrame;

import alvarezferro.codis.API.MessageHandlerInterface;
import alvarezferro.codis.Cliente.UI.VentanaChat;

public class MessageHandler extends UnicastRemoteObject implements MessageHandlerInterface {

    private String interlocutor;

    public MessageHandler(String interlocutor) throws RemoteException{
        this.interlocutor = interlocutor;
    }

    public String getInterlocutor() {
        return this.interlocutor;
    }

    public void enviarMensaje(String mensaje) {

        Cliente.recibirMensaje(interlocutor, mensaje);

    }
    
}
