package Application;
import java.util.ArrayList;

import Common.Address;

public interface NetworkSocketInterface {
	
 public void sendConnected(Account loggedAccount);
 
 public void sendDisconnected(Account loggedAccount);
 
 public void isServerUp();
 
 public ArrayList<Address> getUserList();
 
 public void sendMessage(Message msg, String Username);
 
 public void startReceiverThread();
 
 public ArrayList<Conversation> getHistorique();
 
 public void sendHistorique(ArrayList<Conversation> lh);
 
 public void sendNewPseudo(String New_Pseudo, String oldPseudo);

 
 void setUserList(ArrayList<Address> ul);
 
 


}
