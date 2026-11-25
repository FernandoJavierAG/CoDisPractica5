package alvarezferro.codis.Servidor;

import java.rmi.NotBoundException;
import java.rmi.RemoteException;
import java.rmi.registry.LocateRegistry;
import java.rmi.registry.Registry;
import java.rmi.server.UnicastRemoteObject;
import java.sql.SQLException;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Scanner;
import java.util.Set;

import alvarezferro.codis.Cliente.Cliente;
import alvarezferro.codis.Servidor.BBDD.FachadaBaseDatos;
import alvarezferro.codis.API.MessageHandlerInterface;
import alvarezferro.codis.API.Red;
import alvarezferro.codis.API.SignUpHandlerInterface;
import alvarezferro.codis.API.Usuario;

public class Servidor {
    private static FachadaBaseDatos fbdd; 

    static Registry registroRMI;
    static SignUpHandlerInterface nuestraReferencia;
    static Map<String, Usuario> Usuarios;

    public static void main(String[] args) {

        Usuarios = new HashMap<>();

        registroRMI = startRegistry();
        
        try {
            nuestraReferencia = new SignUpHandler();
        } catch(RemoteException err) {
            System.out.println("Error al crear nuestra referencia remota:\n\t"+err.getMessage());
            freeResources();
            return;
        }

        try {
            registroRMI.rebind("rmi://"+Red.IP_SERVIDOR+":"+Red.PUERTO_REGISTRO+"/"+Red.NAME_SERVIDOR_OBJ, nuestraReferencia);
        } catch(RemoteException err ) {
            System.out.println("Error al publicar el objeto servidor.\n\t"+err.getMessage());
            freeResources();
            return;
        }

        try {
        fbdd = new FachadaBaseDatos();
        } catch(SQLException err) {
            System.out.println("Error en la conexion con la base de datos:\n\t"+err.getMessage());
            freeResources();
            return;
        }

        Scanner entrada = new Scanner(System.in);
        System.out.println("Servidor listo y emitiendo. Introduzca \"exit\" para salir.");
        

        // BATERIA DE PRUEBAS
        int codError; Set<String> res;
        fbdd.inscribirUsuario("Joselito", "ABCD");
        fbdd.inscribirUsuario("Manolita", "1234");

        codError = fbdd.autenticarUsuario("Joselito", "ABCD");
        codError = fbdd.autenticarUsuario("Joselite", "ABCD");
        codError = fbdd.autenticarUsuario("Joselito", "SiYoSoyJoselito");

        codError = fbdd.enviarSolicitudAmistad("Manolita", "Joselito");
        res = fbdd.getAmigos("Manolita");
        res = fbdd.getAmigos("Joselito");

        codError = fbdd.aceptarSolicitudAmistad("Joselito", "Manolita");
        res = fbdd.getAmigos("Manolita");
        res = fbdd.getAmigos("Joselito");

        // FIN BATERIA DE PRUEBAS

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
        registroRMI = LocateRegistry.createRegistry(Red.PUERTO_REGISTRO);
        } catch (RemoteException errCre) {
            System.out.println("Non se puido crear o rexistro RMI.");
            return null;
        }
        System.out.println("Rexistro RMI creado con exito.");
        return registroRMI;
    }

    protected static void addUsuario(Usuario usuario) {
        Usuarios.put(usuario.getUserName(), usuario);
    }

    protected static Set<String> getAmigos(Usuario usuario) {
        return fbdd.getAmigos(usuario.getUserName());
    }

    protected static Set<Usuario> getAmigosEnLinea(Usuario usuario) {
        Set<Usuario> amigosEnLinea = new HashSet<>();
        Set<String> amigos = fbdd.getAmigos(usuario.getUserName());

        for(String amigo : amigos)
            if(Usuarios.containsKey(amigo))
                amigosEnLinea.add(Usuarios.get(amigo));        

        return amigosEnLinea;
    }

    protected static void notificarInicioSesion(Usuario usuario) {
        for(String amigo : getAmigos(usuario)) {
            try {
                if(Usuarios.containsKey(amigo))            
                    Usuarios.get(amigo).notificarInicioSesion(usuario);
            } catch(RemoteException err) {
                Cliente.logMessage("Usuario "+amigo+" no accesible a la hora de notificar inicio de sesion.\n");
            }
        }
    }

    protected static void notificarCierreSesion(Usuario usuario) {
        for(String amigo : getAmigos(usuario)) {
            try {
                if(Usuarios.containsKey(amigo))            
                Usuarios.get(amigo).notificarCierreSesion(usuario.getUserName());
            } catch(RemoteException err) {
                Cliente.logMessage("Usuario "+amigo+" no accesible a la hora de notificar cierre de sesion.\n");
            }
        }

        Usuarios.remove(usuario.getUserName());
    }

    protected static int enviarSolicitudAmistad(String solicitante, String destinatario) {
        Usuario usrDestinatario = Usuarios.get(destinatario);

        if(usrDestinatario != null) { // Esta en linea en este momento.
            try {
                usrDestinatario.notificarSolicitudAmistad(solicitante);
                return 0;
            } catch(RemoteException err) {} // La ignoramos. Ya sera notificado la proxima vez que inicie sesión.
        }         

        return fbdd.enviarSolicitudAmistad(solicitante, destinatario);
    }

    protected static int aceptarSolicitudAmistad(String destinatario, String solicitante) {
        int codError = fbdd.aceptarSolicitudAmistad(destinatario, solicitante);

        if(codError != 0)
            return codError;


        Usuario usrSolicitante = Usuarios.get(solicitante);
        Usuario usrDestinatario = Usuarios.get(destinatario);
        if(usrSolicitante != null) {
            try {
                usrSolicitante.notificarSolicitudAceptada(destinatario);
            } catch(RemoteException err) {}
        }

        try {
        if(usrSolicitante != null)
            usrSolicitante.notificarInicioSesion(usrDestinatario);
        if(usrDestinatario != null)
            usrDestinatario.notificarInicioSesion(usrSolicitante);
        } catch(RemoteException err) {}

        return 0;
    }

    protected static int autenticarUsuario(String userName, String password) {

        return fbdd.autenticarUsuario(userName, password);

    }

    protected static int inscribirUsuario(String userName, String password) {

        return inscribirUsuario(userName, password);
    }

    protected static MessageHandlerInterface iniciarChat(String destinatario, String remitente) {
        Usuario usrDestinatario = Usuarios.get(destinatario);
        if(usrDestinatario == null)
            return null;

        try {
            return usrDestinatario.getReferencia().iniciarChat(remitente);
        } catch(RemoteException err) {
            return null;
        }
    }

    private static void freeResources() {
        if(registroRMI == null)
            return;

        try {
            registroRMI.unbind("rmi://"+Red.IP_SERVIDOR+":"+Red.PUERTO_REGISTRO+"/"+Red.NAME_SERVIDOR_OBJ);
        } catch (RemoteException | NotBoundException err) {}

        try {
        if(nuestraReferencia != null)
                UnicastRemoteObject.unexportObject(nuestraReferencia, false);
        } catch(RemoteException err) {}

        try {
            UnicastRemoteObject.unexportObject(registroRMI, false);
        } catch(RemoteException err) {}
    }
}