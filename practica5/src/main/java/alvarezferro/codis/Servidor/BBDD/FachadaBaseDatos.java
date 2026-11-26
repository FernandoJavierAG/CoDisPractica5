//El "main" pero de la base de datos

package alvarezferro.codis.Servidor.BBDD;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.HashSet;
import java.util.Properties;
import java.util.Set;


public class FachadaBaseDatos {
    private java.sql.Connection conexion;

    // Inicializacion:
    public FachadaBaseDatos () throws java.sql.SQLException {
            Properties usuario = new Properties();

            usuario.setProperty("user", "aplicacion");
            usuario.setProperty("password", "");
            
            String url = 
                    "jdbc:"+"postgresql"+"://"+
                    "172.18.21.101:"+
                    "5432/"+
                    "p2p"
                ;

            System.out.println(url);
            System.out.println(usuario);

            this.conexion=java.sql.DriverManager.getConnection(url, usuario);  
            this.conexion.setAutoCommit(false);
    }



    // Funciones:
    public int inscribirUsuario(String userName, String password) {
        int codError = 0;
        PreparedStatement stmComprobacion=null, stmInscripcion=null;
        ResultSet rsComprobacion;

        try{
            stmComprobacion=conexion.prepareStatement("select * from usuario where nombre = ?");
            stmComprobacion.setString(1, userName);
            rsComprobacion=stmComprobacion.executeQuery();

            if(rsComprobacion.next())
                codError=2;
            else {
                stmInscripcion=conexion.prepareStatement("insert into usuario (nombre, password) values (?, ?)");
                stmInscripcion.setString(1, userName);
                stmInscripcion.setString(2, password);
                stmInscripcion.executeUpdate();
            }

            conexion.commit();
            
        }catch(SQLException err){
            System.out.println("Error al inscribir un usuario:\n\t"+err.getMessage());
            codError = 1;
            tryRollback();

        }finally{
            try {
                if(stmComprobacion!= null) stmComprobacion.close(); 
                if(stmInscripcion!= null) stmInscripcion.close();
            } catch (SQLException err){System.out.println("Imposible cerrar cursores");}
        }

        return codError;
    }

    public int modificarContrasena(String userName, String newPassword) {
        int codError = 0;
        PreparedStatement stmActualizacion = null;

        try{
            stmActualizacion=conexion.prepareStatement("update usuario set password = ? where nombre = ?");
            stmActualizacion.setString(1, newPassword);
            stmActualizacion.setString(2, userName);

            conexion.commit();
            
        }catch(SQLException err){
            System.out.println("Error al modificar la contraseña de un usuario:\n\t"+err.getMessage());
            codError = 1;
            tryRollback();

        }finally{
            try {
                if(stmActualizacion!= null) stmActualizacion.close(); 
            } catch (SQLException err){System.out.println("Imposible cerrar cursores");}
        }

        return codError;
    }

    public int autenticarUsuario(String userName, String password) {
        int codError = 0;
        PreparedStatement stmUsuario=null;
        ResultSet rsUsuario;

        try {
            stmUsuario = conexion.prepareStatement("select nombre, password from usuario where nombre = ? ");
            stmUsuario.setString(1, userName);
            rsUsuario = stmUsuario.executeQuery();

            if(!rsUsuario.next())
                codError = 2;
            else if(!rsUsuario.getString("password").equals(password))
                codError = 3;

        } catch(SQLException err){
            System.out.println("Error al acceder a la base de datos de usuarios:\n\t"+err.getMessage());
            codError = 1;

        } finally{
            try {stmUsuario.close();} catch (SQLException err){System.out.println("Imposible cerrar cursores");}
        }

        return codError;
    }

    public Set<String> getAmigos(String usuario) {
        PreparedStatement stmAmigos=null;
        ResultSet rsAmigos;
        Set<String> resultado= new HashSet<String>();
        
        try{
            stmAmigos=conexion.prepareStatement("select amigo2 from amistad where amigo1 = ?");
            stmAmigos.setString(1, usuario);
            rsAmigos=stmAmigos.executeQuery();
            
            while(rsAmigos.next()){
                resultado.add(rsAmigos.getString("amigo2"));
            }
            
        }catch(SQLException err){
            System.out.println("Error al obtener amigos de la base de datos:\n\t"+err.getMessage());
            resultado = null;

        }finally{
            try {stmAmigos.close();} catch (SQLException err){System.out.println("Imposible cerrar cursores");}
        }
        
        return resultado;
        
    }

    public int enviarSolicitudAmistad(String solicitante, String destinatario) {
        int codError = 0;
        PreparedStatement stmSolicitud=null;
        
        try{
            stmSolicitud=conexion.prepareStatement("insert into solicitud_amistad(solicitante, destinatario) values (?, ?)");
            stmSolicitud.setString(1, solicitante);
            stmSolicitud.setString(2, destinatario);
            stmSolicitud.executeUpdate();
           
            conexion.commit();
            
        }catch(SQLException err){
            System.out.println("Error al añadir una solicitud de amistad:\n\t"+err.getMessage());
            codError = 1;
            tryRollback();
                
        }finally{
            try {stmSolicitud.close();} catch (SQLException err){System.out.println("Imposible cerrar cursores");}
        }

        return codError;
    }

    public int aceptarSolicitudAmistad(String destinatario, String solicitante) {
        int codError = 0;
        PreparedStatement stmExisteSolicitud=null, stmEliminarSolicitud=null, stmCrearAmistad=null;
        ResultSet rsExisteSolicitud;
        
        // Comprobamos, en primer lugar, que exista la supuesta solicitud de amistad. El solcitante podría haberla retirado.
        try{
            stmExisteSolicitud=conexion.prepareStatement("select * from solicitud_amistad where solicitante = ? and destinatario = ?");
            stmExisteSolicitud.setString(1, solicitante);
            stmExisteSolicitud.setString(2, destinatario);
            rsExisteSolicitud = stmExisteSolicitud.executeQuery();
            
            if(!rsExisteSolicitud.next())
                codError = 2;
            
        }catch(SQLException err){
            System.out.println("Error al obtener amigos de la base de datos:\n\t"+err.getMessage());
        }finally{
            try {stmExisteSolicitud.close();} catch (SQLException err){System.out.println("Imposible cerrar cursores");}
        }

        // La solicitud existe. Lo convertimos en amistad.
        if(codError == 0) try {

            // Pendiente: Comprobar si funciona bien sin esta linea, tras añadir la regla a la BDD
            stmEliminarSolicitud=conexion.prepareStatement("delete from solicitud_amistad where solicitante = ? and destinatario = ?");
            stmEliminarSolicitud.setString(1, solicitante);
            stmEliminarSolicitud.setString(2, destinatario);
            stmEliminarSolicitud.executeUpdate();

            stmCrearAmistad=conexion.prepareStatement("insert into amistad(amigo1, amigo2) values (?, ?), (?, ?)");
            stmCrearAmistad.setString(1, solicitante); stmCrearAmistad.setString(2, destinatario);
            stmCrearAmistad.setString(3, destinatario); stmCrearAmistad.setString(4, solicitante);
            stmCrearAmistad.executeUpdate();

            conexion.commit();

        }catch(SQLException err){
            System.out.println("Error al añadir una relacion de amistad a la base de datos:\n\t"+err.getMessage());
            codError = 1;
            tryRollback();
                
        }finally{
            try {
                if(stmCrearAmistad!= null)
                    stmCrearAmistad.close(); 
                if(stmEliminarSolicitud!= null)
                    stmEliminarSolicitud.close(); 
            } catch (SQLException err){System.out.println("Imposible cerrar cursores");}
        }

        return codError;
    }

    public Set<String> getSolicitudesPendientes(String destinatario) {
        PreparedStatement stmSolicitudes=null;
        ResultSet rsSolicitudes;
        Set<String> resultado= new HashSet<String>();
        
        try{
            stmSolicitudes=conexion.prepareStatement("select solicitante from solicitud_amistad where destinatario = ?");
            stmSolicitudes.setString(1, destinatario);
            rsSolicitudes=stmSolicitudes.executeQuery();
            
            while(rsSolicitudes.next()){
                resultado.add(rsSolicitudes.getString("solicitante"));
            }
            
        }catch(SQLException err){
            System.out.println("Error al obtener solicitudes de amistad de la base de datos:\n\t"+err.getMessage());
            resultado = null;

        }finally{
            try {stmSolicitudes.close();} catch (SQLException err){System.out.println("Imposible cerrar cursores");}
        }
        
        return resultado;
        
    }

    private void tryRollback() {
        for(int i = 1; i <= 3; i++) try {
            conexion.rollback();
            break;
        } catch (SQLException error) {
            if(i == 3)
                System.out.println("Error grave. No se ha podido hacer rollback. Es posible que la BDD este en un estado inconsistente.");
            else
                System.out.println("Error al hacer rollback. Reintentando...");
        }
}
    
}
    
