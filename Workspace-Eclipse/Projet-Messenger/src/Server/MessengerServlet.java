package Server;

import java.io.IOException;
import java.io.PrintWriter;
import java.net.InetAddress;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.Iterator;

import Common.Address;
import Common.Disco;
import Common.Tools;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import java.sql.Timestamp;

public class MessengerServlet extends HttpServlet{
	 /**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	static ArrayList<Address> coUsers = new ArrayList<Address>();
	 static ArrayList<Disco> discoUsers = new ArrayList<Disco>();
	 static boolean init = false;
	
	@Override
	protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
		/*if(MessengerServlet.init == false) {
			MessengerServlet.init=true;
			coUsers.add(new Address("Alice Pseudo", "Alice Username"));
			coUsers.add(new Address("Bob Pseudo", "Bob Username"));
			coUsers.add(new Address("Eve Pseudo", "Eve Username"));
		}*/
		
		
		Enumeration<String> parameterNames = req.getParameterNames();
		String type ="";
		Timestamp ts = null;
		while (parameterNames.hasMoreElements()) {
			 String key = parameterNames.nextElement();
			    if(key.equals("type")) {
			    	type = req.getParameter(key);
			    }else {
			    	 ts = new Timestamp(Long.parseLong(req.getParameter(key)));
			    }
		}
		
		
		resp.setContentType("text/html");
		resp.setCharacterEncoding( "UTF-8" );
		PrintWriter out = resp.getWriter();
		Iterator<Address> it = coUsers.iterator();
		Address temp;
		
		
		if (type.equals(Tools.Msg_Code.CoSpecificList.toString())) {
			out.println(Tools.Msg_Code.CoSpecificList.toString());
			while (it.hasNext()) {
				temp = it.next();
				Timestamp tempTS = temp.getTs();
				if(tempTS.after(ts)) {
					out.println(temp.getPseudo());
					out.println(temp.getUsername());
					out.println(temp.getIP());
				}
			}
			out.println("--rm--");
			Iterator<Disco> iter = discoUsers.iterator();
			Disco tempora;
			while (iter.hasNext()) {
				tempora = iter.next();
				
				if(ts.before(tempora.ts)) {
					out.println(tempora.Username);
				}
			}
		}else {
			out.println(Tools.Msg_Code.CoList.toString());
			while (it.hasNext()) {
				temp = it.next();
				out.println(temp.getPseudo());
				out.println(temp.getUsername());
				out.println(temp.getIP());
			}
			
			
		}
		
		
		
	}
	
	@Override
	protected void doPost(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
		Enumeration<String> parameterNames = req.getParameterNames();
		byte[] addr = new byte[4];
		String username = "";
		String pseudo = "";
		boolean ajout = true;
		while (parameterNames.hasMoreElements()) {
		    String key = parameterNames.nextElement();
		    if(key.equals("username")) {
		    	username = req.getParameter(key);
		    }else if(key.equals("pseudo")) {
			    pseudo = req.getParameter(key);
			}else if (key.equals("add")) {
				String temp = req.getParameter(key);
				if (temp.equals("1")) {
					ajout = true;
				}else {
					ajout = false;
				}
				
			}else if(key.equals("addr1")) {
				addr[0] = (byte)Integer.parseInt(req.getParameter(key));
			}else if(key.equals("addr2")) {
				addr[1] = (byte)Integer.parseInt(req.getParameter(key));
			}else if(key.equals("addr3")) {
				addr[2] = (byte)Integer.parseInt(req.getParameter(key));
			}else {
				addr[3] = (byte)Integer.parseInt(req.getParameter(key));
			}
		} 
		
		if (!ajout) {
			Boolean fin = false;
			Iterator<Address> iter = coUsers.iterator();
			Address tempor;
;			while (!fin && iter.hasNext()) {
				tempor = iter.next();
				if(tempor.getUsername().equals(username)) {
					discoUsers.add(new Disco(username, new Timestamp(System.currentTimeMillis())));
					coUsers.remove(tempor);
					fin = true;
				}
			}
		}else {
			coUsers.add(new Address(InetAddress.getByAddress(addr), pseudo, username));
		}
		
	}
}
	



