package alvarezferro.codis.Servidor;

import java.io.FileWriter;
import java.io.IOException;
import java.rmi.NotBoundException;
import java.rmi.RemoteException;
import java.rmi.registry.LocateRegistry;
import java.rmi.registry.Registry;
import java.rmi.server.UnicastRemoteObject;
import java.sql.SQLException;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Scanner;
import java.util.Set;

import javax.swing.JOptionPane;

import alvarezferro.codis.Cliente.Cliente;
import alvarezferro.codis.Servidor.BBDD.FachadaBaseDatos;
import alvarezferro.codis.API.CallbackHandlerInterface;
import alvarezferro.codis.API.MessageHandlerInterface;
import alvarezferro.codis.API.Red;
import alvarezferro.codis.API.SignUpHandlerInterface;
import alvarezferro.codis.API.Usuario;
import alvarezferro.codis.API.Excepciones.AuthException;
import alvarezferro.codis.API.Excepciones.ForbiddenActionException;
import alvarezferro.codis.API.Excepciones.ServerException;

public class Servidor {

    private static final String LOG_FILE="serverLog.txt";

    static FileWriter logWriter;

    static FachadaBaseDatos fbdd;
    static Registry registroRMI;
    static SignUpHandler objetoServidor;

    static Map<String, Usuario> Usuarios;

    public static void main(String[] args) {

        logWriter = startLogging();
        
        if(logWriter == null) {
            System.out.println("No se pudo acceder al archivo de registro. Logging deshabilitado.");
        }

        Usuarios = new HashMap<>();
        
        fbdd = connectToDatabase();

        registroRMI = startRegistry();

        objetoServidor = publishServerObject();

        if(Usuarios == null || registroRMI == null || fbdd == null || objetoServidor == null) {
            System.out.println("Error crítico en la apertura del servidor. Saliendo...");
            freeResources();
            return;
        }

        Scanner entrada = new Scanner(System.in);
        System.out.println("Servidor listo y emitiendo. Introduzca \"exit\" para salir.");
        logMessage("Servidor iniciado.\n");

        while(true) 
            if(entrada.nextLine().equals("exit")) {
                freeResources();
                break;
            }
        entrada.close();

        freeResources();
    }

    // FUNCIONES PROPRIAS ----------------------------------

    private static FileWriter startLogging() {
        FileWriter logWriter;
        try {
            logWriter = new FileWriter(LOG_FILE);
        } catch(IOException err) {
            System.out.println( "Error al acceder al archivo "+LOG_FILE+"\n\t"+err.getMessage());
            return null;
        }
        return logWriter;
    }

    // Basicamente o mesmo proposito que a do exemplo do CV
    private static Registry startRegistry() {
        Registry registroRMI;

        try {
        registroRMI = LocateRegistry.createRegistry(Red.PUERTO_REGISTRO);
        } catch (RemoteException errCre) {
            System.out.println("Non se puido crear o rexistro RMI.");
            return null;
        }
        System.out.println("Rexistro RMI creado con exito.");
        return registroRMI;
    }

    private static FachadaBaseDatos connectToDatabase() {
        FachadaBaseDatos fbdd;
        try {
            fbdd = new FachadaBaseDatos();
        } catch(SQLException err) {
            System.out.println("Error en la conexion con la base de datos:\n\t"+err.getMessage());
            freeResources();
            return null;
        }
        return fbdd;
    }

    private static SignUpHandler publishServerObject() {
        SignUpHandler objetoServidor;
        try {
            objetoServidor = new SignUpHandler(fbdd);
        } catch(RemoteException err) {
            System.out.println("Error al crear nuestra referencia remota:\n\t"+err.getMessage());
            return null;
        }

        try {
            registroRMI.rebind("rmi://"+Red.IP_SERVIDOR+":"+Red.PUERTO_REGISTRO+"/"+Red.NAME_SERVIDOR_OBJ, objetoServidor);
        } catch(RemoteException err ) {
            System.out.println("Error al publicar el objeto servidor.\n\t"+err.getMessage());
            return null;
        }
        return objetoServidor;
    }

    // SETUP END

    protected static void notificarInicioSesion(Usuario usuario) {

       logMessage("Notificando el inicio de sesion de "+usuario.getUserName()+"...");
        
        Set<String> amigos;
        try {
            amigos = objetoServidor.getAmigos(usuario.getUserName());
        } catch(ServerException err) {
            logMessage("Error de servidor al notificar el inicio de sesion de "+usuario.getUserName()+".");
            return;
        }

        for(String amigo : amigos) {
            try {
                Usuario amigoUsr;
                if((amigoUsr =getUsuario(amigo)) != null) {
                    amigoUsr.notificarInicioSesion(usuario);
                   logMessage("\t Notificacion de inicio de sesion enviada a "+amigo);
                }
            } catch(RemoteException err) {
               logMessage("Usuario "+amigo+" no accesible a la hora de notificar inicio de sesion.");
            }
        }
    }

    protected static void notificarCierreSesion(String usuario) {

       logMessage("Notificando el cierre de sesion de "+usuario+"...");

       Set<String> amigos;
        try {
            amigos = objetoServidor.getAmigos(usuario);
        } catch(ServerException err) {
            logMessage("Error de servidor al notificar el inicio de sesion de "+usuario+".");
            return;
        }

        for(String amigo : amigos) {
            try {
                Usuario amigoUsr;
                if((amigoUsr =getUsuario(amigo)) != null) {
                    amigoUsr.notificarCierreSesion(usuario);
                   logMessage("\t Notificacion de cierre de sesion enviada a "+amigo);
                }
            } catch(RemoteException err) {
               logMessage("Usuario "+amigo+" no accesible a la hora de notificar cierre de sesion.");
            }
        }

       removeUsuario(usuario);
    }

    protected static void addUsuario(Usuario usuario) {
        Usuarios.put(usuario.getUserName(), usuario);

        logMessage("Usuario "+usuario.getUserName()+" en linea.");
    }

    protected static void removeUsuario(String usuario) {
        Usuarios.remove(usuario);
    }

    protected static Usuario getUsuario(String userName) {
        return Usuarios.get(userName);
    }

    protected static Set<Usuario> getUsuarios(Set<String> userNames) {
        Set<Usuario> resultado = new HashSet<>();

        for(String username : userNames) {
            Usuario usr;
            if((usr=Usuarios.get(username)) != null)
                resultado.add(usr);
        }
        return resultado;
    }

    private static void freeResources() {
        if(registroRMI == null)
            return;

        try {
            registroRMI.unbind("rmi://"+Red.IP_SERVIDOR+":"+Red.PUERTO_REGISTRO+"/"+Red.NAME_SERVIDOR_OBJ);
        } catch (RemoteException | NotBoundException err) {}

        try {
        if(objetoServidor != null)
                UnicastRemoteObject.unexportObject(objetoServidor, false);
        } catch(RemoteException err) {}

        try {
            UnicastRemoteObject.unexportObject(registroRMI, false);
        } catch(RemoteException err) {}

        if(fbdd != null)
            fbdd.cerrarConexion();
        
    }

    public static void logMessage(String message) {
        if(logWriter == null)
            return;

        try {
            logWriter.write(message+"\n");
            logWriter.flush();
        } catch(IOException err) {} // Ignoramos.
    }
}