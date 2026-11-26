package alvarezferro.codis.Cliente.UI;

import java.rmi.RemoteException;

import javax.swing.JDialog;
import javax.swing.JFrame;
import javax.swing.JOptionPane;

import alvarezferro.codis.API.CallbackHandlerInterface;
import alvarezferro.codis.API.SignUpHandlerInterface;
import alvarezferro.codis.Cliente.CallbackHandler;

public class InicioSesion extends JDialog {
    SignUpHandlerInterface servidor;


    public InicioSesion(SignUpHandlerInterface servidor) {
        super((JFrame) null, true);

        this.servidor = servidor;
    }

    private int conectarUsuario(String userName, String password) {
        CallbackHandler cliente;

        try {
            cliente = new CallbackHandler();
        } catch(RemoteException err) {
            JOptionPane.showMessageDialog(null, 
                "No se pudo crear nuestra referencia remota"+
                "\t"+err.getMessage()
            );
            return -2;
        }
        /* 
        try {
            servidor.conectarCliente(userName, password, cliente);            
            cliente.setVentanaPrincipal(new VentanaPrincipal(servidor, cliente));
            this.dispose();
        } catch(RemoteException err) {
            JOptionPane.showMessageDialog(null, 
                "No se pudo conectar con el servidor"+
                "\t"+err.getMessage()
            );
            return -1;
        }
        */
        return 0;
        
    }

}
