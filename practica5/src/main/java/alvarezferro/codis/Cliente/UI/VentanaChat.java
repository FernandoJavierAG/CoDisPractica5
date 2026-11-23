package alvarezferro.codis.Cliente.UI;

import alvarezferro.codis.API.CallbackHandlerInterface;
import alvarezferro.codis.API.MessageHandlerInterface;

public class VentanaChat {
    MessageHandlerInterface interlocutor;

    public VentanaChat(CallbackHandlerInterface interlocutor) {

        this.interlocutor = interlocutor.iniciarChat();

    }


    
}
