package alvarezferro.codis.Cliente.UI;

import alvarezferro.codis.API.CallbackHandlerInterface;
import alvarezferro.codis.API.SignUpHandlerInterface;
import alvarezferro.codis.API.Usuario;

public class VentanaPrincipal {
    SignUpHandlerInterface servidor;
    CallbackHandlerInterface cliente;


    public VentanaPrincipal(SignUpHandlerInterface servidor, CallbackHandlerInterface cliente) {
        this.servidor = servidor;
        this.cliente = cliente;
    }

    public void notificarInicioSesion(String amigo) {
        System.out.println(amigo+" en linea.");
    }

    public void notificarCierreSesion(String amigo) {
        System.out.println(amigo+"ha cerrado sesión.");
    }

    public void mostrarMensaje(String remitente, String mensaje) {
        System.out.println("["+remitente+"] : "+mensaje);
    }
}
