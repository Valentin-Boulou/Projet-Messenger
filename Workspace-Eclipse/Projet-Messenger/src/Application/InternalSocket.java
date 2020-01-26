package Application;

import java.util.ArrayList;
import java.util.Iterator;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.HttpURLConnection;
import java.net.InetAddress;
import java.net.NetworkInterface;
import java.net.ServerSocket;
import java.net.Socket;
import java.net.SocketException;
import java.net.SocketTimeoutException;
import java.net.URL;
import java.net.http.HttpClient;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.io.Serializable;
import java.io.StringReader;
import java.lang.Thread;
import java.sql.Timestamp;
import java.util.Enumeration;
import java.util.List;
import java.util.Objects;
import Common.Tools;

import Common.Address;


/* MUST BE SYNCHRONIZED, EACH TIME YOU CALL an INTERNALSOCKET object
 * Peut etre source de pb -> deadlock : Essayer de conserver un unique synchronized*/
public class InternalSocket implements NetworkSocketInterface {
	ArrayList<Address> connectedUserList = new ArrayList<Address>(); // Need to be synchronized
	/*protected static final int UDP_PORT_RCV = 6666;
	protected static final int UDP_PORT_SEND = 6667;
	protected static final int TCP_PORT_RCV= 6668;
	protected static final int TCP_PORT_SEND = 6669;
	
	protected static final String  CONNECTED =  "Connected";
	protected static final String  DISCONNECTED =  "Disconnected";
	protected static final String  MESSAGE =  "Message";
	protected static final String NEW_PSEUDO = "New_Pseudo";
	protected static final String CON_ACK = "Con_Ack";
	protected static final String END_MESSAGE = "ef399b2d446bb37b7c32ad2cc1b6045b";*/
	protected final Account UsernameLogged;
	protected static final int MAX_CHAR = 256;
	DatagramSocket UDP_SEND_Socket;
	UDPThreadReceiver UDP_RCV_Thread;
	TCPThreadReceiver TCP_RCV_Thread;
	protected DBLocale db;
	protected UserInterface UI;
	
	
	
	public InternalSocket(Account UsernameLoggedAccount_, UserInterface _UI){
		
		this.UsernameLogged = UsernameLoggedAccount_;
		this.connectedUserList = new ArrayList<Address>();
		this.db = new DBLocale();
		this.UI = _UI;
		try {
			this.UDP_SEND_Socket = new DatagramSocket(Tools.Ports.UDP_SEND.getValue());
		} catch (SocketException e) {
			System.out.println("Internal Socket: Error init UDP socket in constructor");
			e.printStackTrace();
		}
		this.startReceiverThread();
		this.sendConnected(UsernameLogged);
	}
	
	List<InetAddress> listAllBroadcastAddresses() throws SocketException {
	    List<InetAddress> broadcastList = new ArrayList<>();
	    Enumeration<NetworkInterface> interfaces 
	      = NetworkInterface.getNetworkInterfaces();
	    while (interfaces.hasMoreElements()) {
	        NetworkInterface networkInterface = interfaces.nextElement();
	 
	        if (networkInterface.isLoopback() || !networkInterface.isUp()) {
	            continue;
	        }
	 
	        networkInterface.getInterfaceAddresses().stream() 
	          .map(a -> a.getBroadcast())
	          .filter(Objects::nonNull)
	          .forEach(broadcastList::add);
	    }
	    return broadcastList;
	}

	@Override
	public void sendConnected(Account loggedAccount) {
		while (!UDP_RCV_Thread.isAlive() ) {
			
		}
		try {
			this.UDP_SEND_Socket.setBroadcast(true);
			
		} catch (SocketException e) {
			System.out.println("InternalSocket: Error sendConnected");
			e.printStackTrace();
		}
		System.out.println("InternalSocket: BROADCAST SENT");
		String message = Tools.Msg_Code.Connected.toString() + "\n" + loggedAccount.getPseudo() + "\n" + loggedAccount.getUsername() + "\n" + (new Timestamp(System.currentTimeMillis())).toString();
		try {
			DatagramPacket outPacket = new DatagramPacket(message.getBytes(),message.length(),listAllBroadcastAddresses().get(0), Tools.Ports.UDP_RCV.getValue());
			this.UDP_SEND_Socket.send(outPacket);
		} catch ( IOException e) {
			System.out.println("InternalSocket: Error dans sendConnected");
			e.printStackTrace();
		}
	}
	
	public void sendNewPseudo(String New_Pseudo, String oldPseudo) {
		try {
			this.UDP_SEND_Socket.setBroadcast(true);
			String message = Tools.Msg_Code.New_Pseudo.toString() + "\n" + New_Pseudo + "\n" + this.UsernameLogged.getUsername() + "\n" + oldPseudo + "\n" + (new Timestamp(System.currentTimeMillis())).toString();
			DatagramPacket outPacket = new DatagramPacket(message.getBytes(),message.length(),listAllBroadcastAddresses().get(0), Tools.Ports.UDP_RCV.getValue());
			this.UDP_SEND_Socket.send(outPacket);
		} catch (IOException e) {
			System.out.println("InternalSocket: Error dans sendNewPseudo");
			e.printStackTrace();
		}
		
	}
	
	public void termine() {
		this.sendDisconnected(UsernameLogged);
		this.TCP_RCV_Thread.setStop();
		this.UDP_RCV_Thread.setStop();
	}

	@Override
	public void isServerUp() {
		// TODO Auto-generated method stub
		
	}

	@Override
	public ArrayList<Address> getUserList() {
		// TODO Auto-generated method stub
		return connectedUserList;
	}
	@Override
	public synchronized void  setUserList(ArrayList<Address> ul) {
		// TODO Auto-generated method stub
		this.connectedUserList = ul;
	}

	@Override
	public void sendMessage(Message msg, String Username) {
		// TODO Auto-generated method stub
		Address res = null;
		Boolean fin = false;
		synchronized (connectedUserList) {
			Iterator<Address> iter = connectedUserList.iterator();

			while (iter.hasNext() && !fin) {
			 res = iter.next();
			 if(res.getUsername().equals(Username)) {
				 fin = true;
			 }
			}
		}
		System.out.println("InternalSocket: addr find " + res.getIP());
		String message = Tools.Msg_Code.Message.toString() + "\n" +  UsernameLogged.getUsername() + "\n" + Username + "\n" + msg.getTimestamp().toString() + "\n" + msg.getMsg();
		System.out.println("InternalSocket: msg : " + message);
		InetAddress addrRcv = res.getIP();
		try {
			Socket TCP_SEND_Socket = new Socket(addrRcv, Tools.Ports.TCP_RCV.getValue());
			PrintWriter out = new PrintWriter(TCP_SEND_Socket.getOutputStream(),true);
			out.println(message);
			TCP_SEND_Socket.close();
		} catch (IOException e) {
			System.out.println("InternalSocket: Error Send message création Socket");
			e.printStackTrace();
		}
		
	}

	@Override
	public void startReceiverThread() {
		// TODO Auto-generated method stub
		System.out.println("InternalSocket: starting RECEIVER UDP AND TCP THREADS . . .");
		this.TCP_RCV_Thread = new TCPThreadReceiver(this.db, UsernameLogged.getUsername(),this.connectedUserList, this.UI);
		this.UDP_RCV_Thread = new UDPThreadReceiver(this.connectedUserList, this.db, this.UDP_SEND_Socket, UsernameLogged);
	}

	@Override
	public ArrayList<Conversation> getHistorique() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public void sendHistorique(ArrayList<Conversation> lh) {
		// TODO Auto-generated method stub

	}
	
	

	@Override
	public void sendDisconnected(Account loggedAccount) {
		String message = Tools.Msg_Code.Disconnected.toString() + "\n" + loggedAccount.getUsername() + "\n" + loggedAccount.getPseudo() + "\n" + (new Timestamp(System.currentTimeMillis())).toString();;
		try {
			DatagramPacket outPacket = new DatagramPacket(message.getBytes(),message.length(),listAllBroadcastAddresses().get(0), Tools.Ports.UDP_RCV.getValue());
			this.UDP_SEND_Socket.send(outPacket);
			this.UDP_SEND_Socket.close();
		} catch ( IOException e) {
			System.out.println("InternalSocket: Error dans sendConnected");
			e.printStackTrace();
		}
		

}

class UDPThreadReceiver extends Thread {
	DatagramSocket receiver;
	DatagramSocket sender;
	ArrayList<Address> connectedUserList;
	DBLocale db;
	boolean termine = false;
	Account userLogged;
	
	public UDPThreadReceiver(ArrayList<Address> _connectedUserList, DBLocale _db, DatagramSocket _DS, Account _UN) {
		super();
		this.sender = _DS;
		this.userLogged = _UN;
		this.db = _db;
		this.connectedUserList = _connectedUserList;
		System.out.println("ThreadReceiver: starting . . .");
		try {
			receiver = new DatagramSocket(Tools.Ports.UDP_RCV.getValue());
		} catch (SocketException e) {
			System.out.println("UDPThreadReceiver: Error creatino socket");
			e.printStackTrace();
		}
		this.start();
		
	}
	public void setStop() {
		this.termine = true;
	}
	
	private void sendSpecificConnected(InetAddress addr, String Pseudo, String Username) {
		
		String message = Tools.Msg_Code.Con_Ack + "\n" + userLogged.getPseudo() + "\n" + userLogged.getUsername() + "\n" + (new Timestamp(System.currentTimeMillis())).toString();
		System.out.println("UDPThreadReceiver: sendSpecificConnected " + message + "\n\n" + addr.getAddress()[0] + + addr.getAddress()[1] + + addr.getAddress()[2] + addr.getAddress()[3]);
		try {
			DatagramPacket outPacket = new DatagramPacket(message.getBytes(),message.length(),addr, Tools.Ports.UDP_RCV.getValue());
			this.sender.send(outPacket);
		} catch ( IOException e) {
			System.out.println("UDPThreadReceiver: Error dans sendConnected");
			e.printStackTrace();
		}
	}
	@Override
	public void run(){
		System.out.println("UDPThreadReceiver: running . . .");
		try {
			receiver.setSoTimeout(1000);
		} catch (SocketException e1) {
			System.out.println("UDPThreadReceiver: Error setSO");
			e1.printStackTrace();
		}
		while(!termine) {
			byte[] buffer = new byte[InternalSocket.MAX_CHAR];
			DatagramPacket inPacket = new DatagramPacket(buffer,buffer.length);
			try {
				
				receiver.receive(inPacket);
				if (inPacket != null) {
					System.out.println("UUDPThreadReceiver: msg recu");
					InetAddress clientAddress = inPacket.getAddress();
					String message = new String (inPacket.getData(), 0, inPacket.getLength());
					BufferedReader reader = new BufferedReader(new StringReader(message));
					String line = reader.readLine();
					if (line.contains(Tools.Msg_Code.Connected.toString())) {
						System.out.println("UDPThreadReceiver: Connected received: " + message +" from " + clientAddress.getAddress()[0] + "." + clientAddress.getAddress()[1] + "." + clientAddress.getAddress()[2] + "." + clientAddress.getAddress()[3]);
						String Pseudo = reader.readLine();
						String Username = reader.readLine();
						if (!Username.equals(userLogged.getUsername())) {
							synchronized(this.connectedUserList) {
								
								this.connectedUserList.add(new Address(clientAddress,Pseudo,Username ));
								
								this.sendSpecificConnected(clientAddress, Pseudo, Username);
							}
						}
						
	
					}else if (line.contains(Tools.Msg_Code.Disconnected.toString())) {
						System.out.println("UDPThreadReceiver: Disconnected received: " + message);
						synchronized(this.connectedUserList) {
							String Username = reader.readLine();
							Boolean fin = false;
							Iterator<Address> iter = this.connectedUserList.iterator();
							Address tempor;
	;						while (!fin && iter.hasNext()) {
								tempor = iter.next();
								if(tempor.getUsername().equals(Username)) {
									this.connectedUserList.remove(tempor);
									fin = true;
								}
							}
							
						}
					}else if (line.contains(Tools.Msg_Code.New_Pseudo.toString())){
						System.out.println("UDPThreadReceiver: New_Pseudo received: " + message);
						synchronized(this.connectedUserList) {
							String new_pseudo = reader.readLine();
							String username = reader.readLine();
							String old_pseudo = reader.readLine();
							if (!username.equals(userLogged.getUsername())) {
								this.connectedUserList.add(new Address(InetAddress.getByAddress(clientAddress.getAddress()),new_pseudo, username));
								Boolean fin = false;
								Iterator<Address> iter = this.connectedUserList.iterator();
								Address tempor;
		;						while (!fin && iter.hasNext()) {
									tempor = iter.next();
									if(tempor.getPseudo().equals(old_pseudo)) {
										this.connectedUserList.remove(tempor);
										fin = true;
									}
								}
								this.db.updatePseudo(new_pseudo, old_pseudo, username, UsernameLogged.getUsername());
							}
							
						}
						
					}else if(line.contains(Tools.Msg_Code.Con_Ack.toString())) {
						System.out.println("UDPThreadReceiver: Connected_ACK received: " + message);
						synchronized(this.connectedUserList) {
							String Pseudo = reader.readLine();
							String Username = reader.readLine();
							this.connectedUserList.add(new Address(clientAddress,Pseudo,Username ));
						}
					}else {
						System.out.println("UDPThreadReceiver: Unknown message received: " + message);
					}
				}
				
				
				
			} catch(SocketTimeoutException a) {
				
			}catch (IOException e) {
				System.out.println("UDPThreadReceiver: Error thread");
				e.printStackTrace();
			}
		}
		this.receiver.close();
		System.out.println("UDPThreadReceiver: Closing . . .");
		
		
	}
}
	
class TCPThreadReceiver extends Thread {
		ServerSocket receiver;
		DBLocale db;
		ArrayList<Address> coUsers;
		int n;
		String UsernameLogged;
		UserInterface UI;
		boolean termine = false;
		public TCPThreadReceiver(DBLocale db_, String _UsernameLogged, ArrayList<Address> _coUsers, UserInterface _UI) {
			super();
			this.coUsers = _coUsers;
			this.UsernameLogged = _UsernameLogged;
			this.db = db_;
			this.UI = _UI;
			try {
				receiver = new ServerSocket(Tools.Ports.TCP_RCV.getValue());
			} catch (IOException e) {
				System.out.println("TCPThreadReceiver: Error constructor ServSocket");
				e.printStackTrace();
			}
			this.n = 0;
			System.out.println("TCPThreadreceiver: Creation ServSocket");
			this.start();
			
		}
		
		public void setStop() {
			this.termine = true;
		}
		
		public void run() {
			try {
				receiver.setSoTimeout(1000);
			} catch (SocketException e1) {
				System.out.println("TCPThreadReceiver: Error setTO");
				e1.printStackTrace();
			}
			while(!termine) {
				
					
					
					Socket clientSocket;
					try {
						clientSocket = receiver.accept();
						if (clientSocket != null) {
							n++;
							System.out.println("TCPThreadReceiver: Creation Socket fils en cours . . .");
							ThreadSocketFils temp = new ThreadSocketFils(clientSocket, n, db,UsernameLogged, this.coUsers, this.UI);
						}
					} catch(SocketTimeoutException a) {
						
					}catch (IOException e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
					}
					
					
				
			}
			try {
				this.receiver.close();
				System.out.println("TCPThreadReceiver: Closing . . .");
			} catch (IOException e) {
				System.out.println("TCPThreadReceiver: Error close");
				e.printStackTrace();
			}
			
		}
		
	}
	
	
class ThreadSocketFils extends Thread{
		Socket son;
		int n;
		DBLocale db;
		ArrayList<Address> coUsers;
		String UsernameLogged;
		UserInterface UI;
		ThreadSocketFils(Socket chassot, int a, DBLocale db_, String _UsernameLogged, ArrayList<Address> _coUsers, UserInterface _UI){
			this.UsernameLogged = _UsernameLogged;
			//this.db= db_;
			this.db = new DBLocale();
			this.coUsers = _coUsers;
			son = chassot;
			n =a;
			this.UI = _UI;
			System.out.println("ThreadSocketFils" + n + ": creation ThreadSocketfils . . .");
			this.start();
		}
		
		@Override
		public void run(){
			try {
				System.out.println("ThreadSocketFils" + n + ": Succesfully created");
				BufferedReader in = new BufferedReader(new InputStreamReader(son.getInputStream()));
				Boolean fin = false;
				String message = "";
				String sender = "";
				String rcv = "";
				Timestamp ts = null;
				String temp = in.readLine();
				while(temp !=null) {
					if(temp.equals(Tools.Msg_Code.Message.toString())){	
						sender = in.readLine();
						rcv = in.readLine();
						ts = Timestamp.valueOf(in.readLine());
					}else {
		                message += temp+ "\n";
					}
					temp = in.readLine();
				}
				System.out.println("ThreadSocketFils" + n + ": msg received: " + sender + ";\n"+ rcv + ";\n"+ message);
				System.out.println("ThreadSocketFils" + n +":" + UsernameLogged +";");
				if( UsernameLogged.equals(rcv)) {
					Address temporary = this.db.getSpecificKnownUser(UsernameLogged, sender);
					if (temporary == null) {
						System.out.println("LOOOOOOOOOOLLLL2");
						synchronized(this.coUsers) {
							boolean fin2 = false;
							Iterator<Address> lol = this.coUsers.iterator();
							Address tempor;
							while(!fin2 && lol.hasNext()) {
								tempor = lol.next();
								System.out.println("lollolol" + tempor.getUsername() + ";" + sender);
								
								if (tempor.getUsername().equals(sender)) {
									System.out.println("LOOOOOOOOOOLLLL");
									fin2 = true;
									temporary = new Address(tempor.getIP(),tempor.getPseudo(),tempor.getUsername());
									this.db.setKnownUser(temporary, UsernameLogged);
									
								}
							}
						}
						
					}
					Message ephemere = new Message(false,message,true,ts);
					this.db.setMessage(ephemere,sender,rcv);
					this.UI.recevoirmessageUI(ephemere,temporary);
				}else {
					System.out.println("ThreadSocketFils" + n +": Msg rejeté car mauvais destinataire");
				}
				
				System.out.println("ThreadSocketFils" + n +": Closing . . .");
				son.close();
				
			}catch(IOException e) {
					System.out.println("ThreadSocketFils" + n + ": Error accept");
					e.printStackTrace();
			}
		}
			
		}
	
class HttpServer{
	private static final String GET_URL = "http://localhost:8080/test/toto";
	
	
	private void sendGET() throws IOException {
		URL obj = new URL(GET_URL);
		HttpURLConnection con = (HttpURLConnection) obj.openConnection();
		con.setRequestMethod("GET");
		con.setRequestProperty("testKEY", "testVALUE");
		int responseCode = con.getResponseCode();
		System.out.println("GET Response Code :: " + responseCode);
		if (responseCode == HttpURLConnection.HTTP_OK) { // success
			BufferedReader in = new BufferedReader(new InputStreamReader(
					con.getInputStream()));
			String inputLine;
			StringBuffer response = new StringBuffer();

			while ((inputLine = in.readLine()) != null) {
				response.append(inputLine);
			}
			in.close();

			// print result
			System.out.println(response.toString());
		} else {
			System.out.println("GET request not worked");
		}

	}
	}
	
}
	



	

