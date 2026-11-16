package alvarezferro.codis.Cliente;

import java.rmi.RemoteException;
import java.rmi.server.UnicastRemoteObject;
import alvarezferro.codis.API.MessageHandlerInterface;

public class MessageHandler extends UnicastRemoteObject implements MessageHandlerInterface {

    private String interlocutor;

    public MessageHandler(String interlocutor) throws RemoteException{
        this.interlocutor = interlocutor;
    }

    public void enviarMensaje(String mensaje) {

        Cliente.recibirMensaje(interlocutor, mensaje);

    }
    
}
