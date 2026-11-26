package alvarezferro.codis.API;

import java.rmi.Remote;
import java.rmi.RemoteException;

// Objeto remoto del cliente, empleado para la comunicación P2P entre clientes.
public interface MessageHandlerInterface extends Remote {

    // Funcion que devuelve el nombre de usuario del Interlocutor, esto es, 
    // el cliente que creó esta referencia.
    public String getInterlocutor() throws RemoteException;

    // Funcion que llama otro cliente para enviarle un mensaje al interlocutor.
    public void enviarMensaje(String mensaje) throws RemoteException;

}
