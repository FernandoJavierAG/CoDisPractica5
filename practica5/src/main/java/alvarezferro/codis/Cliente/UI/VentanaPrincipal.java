package alvarezferro.codis.Cliente.UI;

import alvarezferro.codis.API.CallbackHandlerInterface;
import alvarezferro.codis.API.SignUpHandlerInterface;

public class VentanaPrincipal {
    SignUpHandlerInterface servidor;
    CallbackHandlerInterface cliente;


    public VentanaPrincipal(SignUpHandlerInterface servidor, CallbackHandlerInterface cliente) {
        this.servidor = servidor;
        this.cliente = cliente;
    }


    public void mostrarMensaje(String remitente, String mensaje) {
        System.out.println("["+remitente+"] : "+mensaje);
    }
}
