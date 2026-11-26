package alvarezferro.codis.Cliente;

import java.io.FileWriter;
import java.io.IOException;
import java.rmi.NotBoundException;
import java.rmi.Remote;
import java.rmi.RemoteException;
import java.rmi.registry.LocateRegistry;
import java.rmi.registry.Registry;
import java.util.Collection;
import java.util.HashMap;
import java.util.Scanner;
import java.util.Set;

import javax.swing.JOptionPane;

import alvarezferro.codis.API.CallbackHandlerInterface;
import alvarezferro.codis.API.MessageHandlerInterface;
import alvarezferro.codis.API.Red;
import alvarezferro.codis.API.SignUpHandlerInterface;
import alvarezferro.codis.API.Usuario;
import alvarezferro.codis.API.Excepciones.AuthException;
import alvarezferro.codis.API.Excepciones.ForbiddenActionException;
import alvarezferro.codis.API.Excepciones.ServerException;
import alvarezferro.codis.Cliente.UI.InicioSesion;
import alvarezferro.codis.Cliente.UI.VentanaPrincipal;

public class Cliente {

    private static HashMap<String, CallbackHandlerInterface> amigos;

    private static VentanaPrincipal ventana;
    private static CallbackHandler nuestraReferencia;
    
    private static FileWriter logWriter;

    public static void main(String[] args) {

        try {
            logWriter = new FileWriter("log.txt");
        } catch(IOException err) {
            JOptionPane.showMessageDialog(null, 
                "Error al acceder al archivo log.txt\n"+
                "\t"+err.getMessage()
            );
        }

        Registry registroRMI;
        try {
            registroRMI = LocateRegistry.getRegistry(Red.IP_SERVIDOR, Red.PUERTO_REGISTRO);

        } catch(RemoteException err) {
            JOptionPane.showMessageDialog(null, 
                "No se pudo conectar con el registroRMI"+
                "\t"+err.getMessage()
            );
            return;
        }

        SignUpHandlerInterface servidor;
        try {
            Object ref = registroRMI.lookup("rmi://"+Red.IP_SERVIDOR+":"+Red.PUERTO_REGISTRO+"/"+Red.NAME_SERVIDOR_OBJ);

            if(! (ref instanceof SignUpHandlerInterface) ) {
                throw new IllegalArgumentException("Objeto servidor es de la clase "+ref.getClass());
            }

            servidor = (SignUpHandlerInterface) ref;

        } catch (RemoteException | NotBoundException | IllegalArgumentException err) {
            JOptionPane.showMessageDialog(null,
                "Error al obtener el objeto servidor"+
                        "\n\t"+err.getMessage());
            return;
        }

        amigos = new HashMap<>();

        Scanner entrada = new Scanner(System.in);

        System.out.print("Usuario:");String username= entrada.nextLine();
        System.out.print("Contraseña:");String password= entrada.nextLine();
        try {
            nuestraReferencia = new CallbackHandler();
        } catch(RemoteException err) {
            System.out.println("Error al crear nuestra referencia");
        }


        try {
            servidor.conectarCliente(username, password, nuestraReferencia);
        } catch (RemoteException | ServerException err) {
            System.out.println("Error al conectar cliente:\n\t"+err.getMessage());
            return;
        } catch (AuthException err) {
            System.out.println("Error de autenticación:\n\t"+err.getMessage());
        }

        logMessage("Sesion iniciada.\n");

        System.out.println("Amigos: ");
        for(String amigo : amigos.keySet()) {
            System.out.print(amigo +", ");
        }
        System.out.println();

        System.out.println("Presione enter para enviar un mensaje.");
        entrada.nextLine();

        Collection<String> test = amigos.keySet();

        String amigo1 = test.iterator().next();
        
        MessageHandlerInterface chatAmigo1;
        try {
            chatAmigo1 = servidor.iniciarChat(username, password, amigo1);
            if(chatAmigo1 == null)
                throw new RemoteException("Amigo es nulo!");
        } catch (RemoteException | ServerException err) {
            System.out.println("Error al solicitar un chat con "+amigo1+".\n\t"+err.getMessage());
            return;
        } catch (AuthException err) {
            System.out.println("Error de autenticacion:\n\t"+err.getMessage());
            return;
        } catch (ForbiddenActionException err) {
            System.out.println("Accion no permitida:\n\t"+err.getMessage());
            return;
        }

        try {
            System.out.println("Chat iniciado con "+amigo1);
            chatAmigo1.enviarMensaje("Hola amigo!!");
        } catch(RemoteException err) {
            System.out.println("Error al enviar mensaje.\n\t"+err.getMessage());
        }


        
        //new InicioSesion(servidor);
    }

    public static void addAmigo(Usuario amigo) {
        logMessage("Añadiendo amigo!");
        amigos.put(amigo.getUserName(), amigo.getReferencia());
    }

    public static void removeAmigo(String amigo) {
        amigos.remove(amigo);
    }

    public static void logMessage(String message) {
        if(logWriter == null)
            return;

        try {
            logWriter.write(message+"\n");
            logWriter.flush();
        } catch(IOException err) {} // Ignoramos.
    }

    public static void recibirMensaje(String interlocutor, String mensaje) {
        logMessage(interlocutor+" dice: "+mensaje);
    }

}
