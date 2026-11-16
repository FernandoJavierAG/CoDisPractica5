package alvarezferro.codis.Servidor;

import java.rmi.NotBoundException;
import java.rmi.RemoteException;
import java.rmi.registry.LocateRegistry;
import java.rmi.registry.Registry;
import java.rmi.server.UnicastRemoteObject;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Scanner;
import java.util.Set;

import alvarezferro.codis.Cliente.Cliente;
import alvarezferro.codis.API.Red;

public class Servidor {

    static Registry registroRMI;
    static SignUpHandler nuestraReferencia;
    static Map<String, Usuario> Usuarios;

    public static void main(String[] args) {

        Usuarios = new HashMap<>();

        registroRMI = startRegistry();

        nuestraReferencia = new SignUpHandler();

        try {
            registroRMI.rebind(Red.NAME_SERVIDOR_OBJ, nuestraReferencia);
        } catch(RemoteException err ) {
            System.out.println("Error ó publicar o obxecto servidor.");
            freeResources();
            return;
        }


        Scanner entrada = new Scanner(System.in);
        System.out.println("Servidor listo y emitiendo. Introduzca \"exit\" para salir.");
        
        while(true) 
            if(entrada.nextLine().equals("exit")) {
                freeResources();
                break;
            }
        entrada.close();

        freeResources();
    }

    // Basicamente o mesmo proposito que a do exemplo do CV
    private static Registry startRegistry() {
        Registry registroRMI;
        
        try {
            registroRMI = LocateRegistry.getRegistry(Red.PUERTO_REGISTRO);
            System.out.println("Conexion establecida co rexistro RMI");
        } catch (RemoteException errLoc) {
            try {
            System.out.println("Non se atopou un rexistro RMI. Creando...");
            registroRMI = LocateRegistry.createRegistry(Red.PUERTO_REGISTRO);
            } catch (RemoteException errCre) {
                System.out.println("Non se puido crear o rexistro RMI.");
                return null;
            }
            System.out.println("Rexistro RMI creado con exito.");
        }
        return registroRMI;
    }

    protected static void addUsuario(Usuario usuario) {
        Usuarios.put(usuario.getUserName(), usuario);
    }

    protected static Set<Usuario> getAmigos(Usuario usuario) {
        Set<Usuario> amigos = new HashSet<>();
        
        // Interaccion con BDD - Obtener amigos

        return amigos;
    }

    protected static void notificarInicioSesion(Usuario usuario) {
        for(Usuario amigo : getAmigos(usuario)) {
            try {            
                amigo.notificarInicioSesion(usuario.getUserName());
            } catch(RemoteException err) {
                Cliente.logMessage("Usuario "+amigo.getUserName()+" no accesible a la hora de notificar inicio de sesion.\n");
            }
        }
    }

    protected static void notificarCierreSesion(Usuario usuario) {
        for(Usuario amigo : getAmigos(usuario)) {
            try {            
                amigo.notificarCierreSesion(usuario.getUserName());
            } catch(RemoteException err) {
                Cliente.logMessage("Usuario "+amigo.getUserName()+" no accesible a la hora de notificar cierre de sesion.\n");
            }
        }
    }

    protected static int enviarSolicitudAmistad(String solicitante, String destinatario) {
        Usuario usrDestinatario = Usuarios.get(destinatario);

        if(usrDestinatario != null) { // Esta en linea en este momento.
            try {
                usrDestinatario.notificarSolicitudAmistad(solicitante);
                return 0;
            } catch(RemoteException err) {} // La ignoramos. Ya sera notificado la proxima vez que inicie sesión.
        } 

        // Interaccion BDD - Registrar solicitud pendiente

        // Si el usuario no existe retornar != 0

        return 0;
    }

    protected static int aceptarSolicitudAmistad(String destinatario, String solicitante) {
        // Interaccion BDD - Registrar solicitud aceptada.

        Usuario usrSolicitante = Usuarios.get(solicitante);
        if(usrSolicitante != null) {
            try {
                usrSolicitante.notificarSolicitudAceptada(destinatario);
            } catch(RemoteException err) {}
        }

        return 0;
    }

    protected static int autenticarUsuario(String userName, String password) {

        // Interaccion con BDD - Verificar que userName y password coinciden.

        return 0;

    }

    protected static int inscribirUsuario(String userName, String password) {

        // Interaccion con BDD - Añadir usuario

        return 0;
    }

    private static void freeResources() {
        if(registroRMI == null)
            return;

        try {
            registroRMI.unbind(Red.NAME_SERVIDOR_OBJ);

            if(nuestraReferencia != null)
                UnicastRemoteObject.unexportObject(nuestraReferencia, false);
            UnicastRemoteObject.unexportObject(registroRMI, false);
        } catch (RemoteException | NotBoundException err) {}
        
    }
}