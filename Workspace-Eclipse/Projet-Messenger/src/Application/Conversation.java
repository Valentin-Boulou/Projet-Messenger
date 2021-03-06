package Application;
import java.util.ArrayList;

import Common.Address;

public class Conversation {
	private ArrayList<Message> conv = new ArrayList<Message>();
	private Address destinataire;

	protected Conversation(Address destinataire) {
		this.destinataire = destinataire;
		this.conv = new ArrayList<Message>() ;
	}
	
	protected void addMessage(Message msg) {
		this.conv.add(msg);
	}
	
	protected Message getMessage(int index) {
		return this.conv.get(index);
	}
	
	protected int getConvSize() {
		return this.conv.size();
	}
	 
	protected Message[] getAllMessages() {
		return this.conv.toArray(new Message[this.conv.size()]);
	}
	
	protected Address getDestinataire() {
		return this.destinataire;
	}
	
	protected void setDestinataire(Address dest) {
		this.destinataire = dest;
	}
	
	protected boolean isEmpty() {
		return this.conv.isEmpty();
	}

}
