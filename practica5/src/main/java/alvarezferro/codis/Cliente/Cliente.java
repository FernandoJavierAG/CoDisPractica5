package alvarezferro.codis.Cliente;

import java.io.FileWriter;
import java.io.IOException;
import java.rmi.NotBoundException;
import java.rmi.RemoteException;
import java.rmi.registry.LocateRegistry;
import java.rmi.registry.Registry;
import java.util.HashMap;

import javax.swing.JOptionPane;

import alvarezferro.codis.API.Red;
import alvarezferro.codis.API.SignUpHandlerInterface;
import alvarezferro.codis.API.Usuario;
import alvarezferro.codis.Cliente.UI.InicioSesion;
import alvarezferro.codis.Cliente.UI.VentanaPrincipal;

public class Cliente {

    private static HashMap<String, Usuario> amigos;

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
            Object ref = registroRMI.lookup(Red.NAME_SERVIDOR_OBJ);

            if(! (ref instanceof SignUpHandlerInterface) ) {
                throw new IllegalArgumentException("Objeto servidor es de la clase "+ref.getClass());
            }

            servidor = (SignUpHandlerInterface) ref;

        } catch (RemoteException | NotBoundException | IllegalArgumentException err) {
            JOptionPane.showMessageDialog(null,
                "Error al obtener el objeto servidor"+
                        "\t"+err.getMessage());
            return;
        }

        amigos = new HashMap<>();
        
        new InicioSesion(servidor);
    }

    public static void addAmigo(Usuario amigo) {
        amigos.put(amigo.getUserName(), amigo);
    }

    public static void removeAmigo(String amigo) {
        amigos.remove(amigo);
    }

    public static void logMessage(String message) {
        if(logWriter == null)
            return;

        try {
            logWriter.write(message);
        } catch(IOException err) {} // Ignoramos.
    }

}
