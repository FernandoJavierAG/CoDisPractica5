//El "main" pero de la base de datos

package alvarezferro.codis.Servidor.BBDD;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.SQLIntegrityConstraintViolationException;
import java.sql.SQLNonTransientException;
import java.util.HashSet;
import java.util.Properties;
import java.util.Set;

import alvarezferro.codis.API.Excepciones.AuthException;
import alvarezferro.codis.API.Excepciones.ForbiddenActionException;
import alvarezferro.codis.API.Excepciones.ServerException;
import alvarezferro.codis.Servidor.Servidor;


public class FachadaBaseDatos {
    private java.sql.Connection conexion;

    // Inicializacion:
    public FachadaBaseDatos () throws java.sql.SQLException {
            Properties usuario = new Properties();

            usuario.setProperty("user", "aplicacion");
            usuario.setProperty("password", "");
            
            String url = 
                    "jdbc:"+"postgresql"+"://"+
                    "192.168.56.101:"+
                    "5432/"+
                    "p2p"
                ;

            System.out.println("Conectando con la base de datos:");
            System.out.println("\tURL:"+url);
            System.out.println("\tUsuario:"+usuario);

            this.conexion=java.sql.DriverManager.getConnection(url, usuario);  
            this.conexion.setAutoCommit(false);
    }

    public void cerrarConexion() {
        try {
            this.conexion.close();
        } catch(SQLException err) {
            Servidor.logMessage("Error al cerrar la conexion con las base de datos.");
        }
        conexion=null;
    }


    // Funciones:
    public void inscribirUsuario(String userName, String password) throws ServerException, ForbiddenActionException {
        PreparedStatement stmInscripcion=null;

        try{
            stmInscripcion=conexion.prepareStatement("insert into usuario (nombre, password) values (?, ?)");
            stmInscripcion.setString(1, userName);
            stmInscripcion.setString(2, password);
            stmInscripcion.executeUpdate();

            conexion.commit();
            
        } catch(SQLException err){
            Servidor.logMessage("Error al inscribir un usuario:\n\t"+err.getMessage());
            tryRollback();

            if(err instanceof SQLIntegrityConstraintViolationException)
                throw new ForbiddenActionException("El nombre de usuario indicado ya está en uso");
            else
                throw new ServerException("No se pudo realizar el registro. Vuelva a intentarlo más tarde.");

        }finally{
            try {
                if(stmInscripcion!= null) stmInscripcion.close();
            } catch (SQLException err){Servidor.logMessage("Imposible cerrar cursores");}
        }
    }

    public void modificarContrasena(String userName, String newPassword) throws ServerException {
        PreparedStatement stmActualizacion = null;

        try{
            stmActualizacion=conexion.prepareStatement("update usuario set password = ? where nombre = ?");
            stmActualizacion.setString(1, newPassword);
            stmActualizacion.setString(2, userName);

            conexion.commit();
            
        }catch(SQLException err){
            Servidor.logMessage("Error al modificar la contraseña del usuario "+userName+" a "+newPassword+":\n\t"+err.getMessage());
            tryRollback();

            throw new ServerException("No se pudo realizar el registro. Vuelva a intentarlo más tarde.");

        }finally{
            try {
                if(stmActualizacion!= null) stmActualizacion.close(); 
            } catch (SQLException err){Servidor.logMessage("Imposible cerrar cursores");}
        }
    }

    public void autenticarUsuario(String userName, String password) throws ServerException, AuthException {
        PreparedStatement stmUsuario=null;
        ResultSet rsUsuario;

        try {
            stmUsuario = conexion.prepareStatement("select nombre, password from usuario where nombre = ? ");
            stmUsuario.setString(1, userName);
            rsUsuario = stmUsuario.executeQuery();

            if(!rsUsuario.next() || !rsUsuario.getString("password").equals(password))
                throw new AuthException("El usuario o contraseña indicados son incorrectos.");

        } catch(SQLException err){
            Servidor.logMessage("Error al acceder al autenticar al usuario"+userName+"con contraseña"+password+":\n\t"+err.getMessage());

            throw new ServerException("No se pudo acceder al registro. Vuelva a intentarlo más tarde.");

        } finally{
            try {
                if(stmUsuario!= null) stmUsuario.close();
            } catch (SQLException err){Servidor.logMessage("Imposible cerrar cursores");}
        }
    }

    public Set<String> getAmigos(String usuario) throws ServerException {
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
            Servidor.logMessage("No se pudieron obtener obtener los amigos de "+usuario+" de la base de datos:\n\t"+err.getMessage());

            throw new ServerException("No se pudo acceder al registro. Vuelva a intentarlo más tarde.");

        }finally{
            try {
                if(stmAmigos!=null) stmAmigos.close();
            } catch (SQLException err){Servidor.logMessage("Imposible cerrar cursores");}
        }
        
        return resultado;
        
    }

    public void enviarSolicitudAmistad(String solicitante, String destinatario) throws ServerException, ForbiddenActionException {
        PreparedStatement stmSolicitud=null;
        
        try{
            stmSolicitud=conexion.prepareStatement("insert into solicitud_amistad(solicitante, destinatario) values (?, ?)");
            stmSolicitud.setString(1, solicitante);
            stmSolicitud.setString(2, destinatario);
            stmSolicitud.executeUpdate();
           
            conexion.commit();
            
        }catch(SQLException err) {
            Servidor.logMessage("Error al añadir una solicitud de amistad de "+solicitante+" a "+destinatario+":\n\t"+err.getMessage());
            tryRollback();

            if(err instanceof SQLNonTransientException)
                throw new ForbiddenActionException("Los usuarios indicados ya tienen una relación de amistad");

            throw new ServerException("No se pudo realizar el registro. Vuelva a intentarlo más tarde.");
                
        }finally{
            try {
                if(stmSolicitud != null) stmSolicitud.close();
            } catch (SQLException err){Servidor.logMessage("Imposible cerrar cursores");}
        }
    }

    public void aceptarSolicitudAmistad(String destinatario, String solicitante) throws ServerException, ForbiddenActionException {
        PreparedStatement stmCrearAmistad=null;

        try {
            stmCrearAmistad=conexion.prepareStatement("insert into amistad(amigo1, amigo2) values (?, ?), (?, ?)");
            stmCrearAmistad.setString(1, solicitante); stmCrearAmistad.setString(2, destinatario);
            stmCrearAmistad.setString(3, destinatario); stmCrearAmistad.setString(4, solicitante);
            stmCrearAmistad.executeUpdate();

            conexion.commit();

        }catch(SQLException err){
            Servidor.logMessage("Error al añadir una relacion de amistad a la base de datos:\n\t"+err.getMessage());
            tryRollback();

            if(err instanceof SQLNonTransientException)
                throw new ForbiddenActionException("No existe una solicitud de amistad de "+solicitante+" hacia "+destinatario+".");

            throw new ServerException("Error al registrar la amistad. Vuelva a intentarlo más tarde.");
                
        }finally{
            try {
                if(stmCrearAmistad!= null) stmCrearAmistad.close();
            } catch (SQLException err){Servidor.logMessage("Imposible cerrar cursores");}
        }
    }

    public Set<String> getSolicitudesPendientes(String destinatario) throws ServerException {
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
            Servidor.logMessage("Error al obtener solicitudes de amistad de la base de datos:\n\t"+err.getMessage());
            
            throw new ServerException("No se pudo acceder al registro. Vuelva a intentarlo más tarde.");

        }finally{
            try {
                if(stmSolicitudes != null) stmSolicitudes.close();
            } catch (SQLException err){Servidor.logMessage("Imposible cerrar cursores");}
        }
        
        return resultado;
        
    }

    private void tryRollback() {
        for(int i = 1; i <= 3; i++) try {
            conexion.rollback();
            break;
        } catch (SQLException error) {
            if(i == 3)
                Servidor.logMessage("Error grave. No se ha podido hacer rollback. Es posible que la BDD este en un estado inconsistente.");
            else
                Servidor.logMessage("Error al hacer rollback. Reintentando...");
        }
    }
    
}
    
