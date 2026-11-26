package alvarezferro.codis.API.Excepciones;

public class ForbiddenActionException extends Exception {
    public ForbiddenActionException(String message) {
        super(message);        
    }
    
}
