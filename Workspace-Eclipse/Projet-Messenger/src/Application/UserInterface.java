package Application;

import javax.swing.*;
import java.awt.*;
import java.awt.event.*;
//import java.util.EventObject;
import java.util.ArrayList;

class UserInterface extends JFrame{
	//juste pour test
	ArrayList<Address> connectedUserList = new ArrayList<Address>();
	//
	
	Controller co;
	int nb_uc=0;
	boolean connecte = false;
	connexionPage connexionpage;
	creationcomptePage creationcomptepage;
	utilisateursconnectesPage utilisateursconnectespage;
	JScrollPane scrollbar_uc;
	conversationPage conversationpage;
	JScrollPane scrollbar_conv;
	MenuBar menubar;
	
	
	public UserInterface(/*ArrayList<Address> connectedUserList/*pour test*/) {
		/*super("Messenger");
		SwingUtilities.invokeLater(new Runnable() {
			public void run() {
				initGUI();
			}
		});*/
		
		super("Messenger");
		//this.connectedUserList = connectedUserList; //pour test
		initGUI();
		
	}
	
	
	
	void initGUI() {
		this.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		this.setSize(new Dimension(500,500));
		this.connexionpage = new connexionPage();
		this.creationcomptepage = new creationcomptePage();
		this.utilisateursconnectespage = new utilisateursconnectesPage();
		this.scrollbar_uc = new JScrollPane(this.utilisateursconnectespage);
		this.scrollbar_uc.setVerticalScrollBarPolicy(JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED);
		//this.scrollbar_uc.setBounds(50, 30, 300, 50);
		this.conversationpage = new conversationPage();
		this.scrollbar_conv = new JScrollPane(this.conversationpage);
		this.scrollbar_conv.setVerticalScrollBarPolicy(JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED);
		this.scrollbar_conv.setHorizontalScrollBarPolicy(ScrollPaneConstants.HORIZONTAL_SCROLLBAR_NEVER);
		//this.scrollbar_conv.setBounds(50, 30, 300, 50);
		this.menubar = new MenuBar();
		this.getContentPane().add(this.menubar, BorderLayout.PAGE_START);
		setConnexionPage();
		
		
	}

/////////méthodes de changement de page//////
	void setConnexionPage() {
		this.setVisible(false);
		this.getContentPane().add(this.connexionpage, BorderLayout.CENTER);
		this.getContentPane().remove(this.creationcomptepage);
		this.getContentPane().remove(this.scrollbar_uc);
		this.getContentPane().remove(this.scrollbar_conv);
		this.setVisible(true);	
	}
	
	void setCreationcomptePage() {
		this.setVisible(false);
		this.getContentPane().add(this.creationcomptepage, BorderLayout.CENTER);
		this.getContentPane().remove(this.connexionpage);
		this.getContentPane().remove(this.scrollbar_uc);
		this.getContentPane().remove(this.scrollbar_conv);
		this.setVisible(true);	
	}
	
	void setUtilisateursconnectesPage_same_frame() {
		this.setVisible(false);
		//on recrée la page pour màj
		this.getContentPane().remove(this.scrollbar_uc);
		this.utilisateursconnectespage = new utilisateursconnectesPage();
		this.scrollbar_uc = new JScrollPane(this.utilisateursconnectespage);
		this.scrollbar_uc.setVerticalScrollBarPolicy(JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED);
		this.getContentPane().add(this.scrollbar_uc, BorderLayout.CENTER);
		//
		this.getContentPane().remove(this.connexionpage);
		this.getContentPane().remove(this.creationcomptepage);
		this.getContentPane().remove(this.scrollbar_conv);
		this.setVisible(true);
	}
	
	void setConversationPage() {
		this.setVisible(false);		
		//on recrée la page pour màj
		this.getContentPane().remove(this.scrollbar_conv);
		this.conversationpage = new conversationPage();
		this.scrollbar_conv = new JScrollPane(this.conversationpage);
		this.scrollbar_conv.setVerticalScrollBarPolicy(JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED);
		this.scrollbar_conv.setHorizontalScrollBarPolicy(ScrollPaneConstants.HORIZONTAL_SCROLLBAR_NEVER);
		//
		this.getContentPane().add(this.scrollbar_conv, BorderLayout.CENTER);
		this.getContentPane().remove(this.connexionpage);
		this.getContentPane().remove(this.creationcomptepage);
		this.getContentPane().remove(this.scrollbar_uc);
		this.setVisible(true);
	}
////////////////////////////////////////////	
	
	
/////////////////////////////////////////PAGE DE CONNEXION (JPanel)///////////////////////////////////////////	
	
	class connexionPage extends JPanel{
		private JTextField username;
		private JTextField password;
		private JButton connexion;
		private JButton creation;
		private connexionHandler coH = new connexionHandler();
		private pagecreationcompteHandler crH = new pagecreationcompteHandler();
		private JLabel erreur;
		
		public connexionPage() {
			super(new GridLayout(0,1));
			
			this.username = new JTextField("username");
			this.password = new JTextField("password");
			this.connexion = new JButton("connexion");
			this.creation = new JButton("création de compte");
			this.erreur = new JLabel("Entrez username/password");
			
			this.add(this.erreur);
			this.add(this.username);
			this.add(this.password);
			this.add(this.connexion);
			this.add(this.creation);
			
			this.connexion.addActionListener(this.coH);
			this.creation.addActionListener(this.crH);
			
			this.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));
			
		}
		
		
	}
////////////////////////////////////////////////////////////////////////////////////////
	
//////////////////////////////////PAGE DE CREATION COMPTE (JPanel)////////////////////////////////////////////////////
	class creationcomptePage extends JPanel{
		public JTextField username;
		private JTextField password;
		private JTextField pseudo;
		private JButton creation;
		private creationcompteHandler crH = new creationcompteHandler();
		
		public creationcomptePage() {
			super(new GridLayout(0,1));
			
			this.username = new JTextField("username");
			this.password = new JTextField("password");
			this.pseudo = new JTextField("pseudo");
			this.creation = new JButton("création du compte");
			
			this.add(this.username);
			this.add(this.password);
			this.add(this.pseudo);
			this.add(this.creation);
			
			this.creation.addActionListener(this.crH);
			
			this.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));
			
		}
		
		
		
	}
///////////////////////////////////////////////////////////////////////////////////////////////////
	
/////////////////////////////////////////PAGE UTILISATEURS CONNECTES (JPanel)///////////////////////////////////////////	
	
class utilisateursconnectesPage extends JPanel{
	private JButton[] utilisateurs;
	private JLabel erreur;
	private afficherconversationHandler acH = new afficherconversationHandler();

	
	public utilisateursconnectesPage() {
		super(new GridLayout(0,1));
		
		//if (co.getSocket().getUserList().isEmpty()) {
		if(connectedUserList.isEmpty()) { //pour test
			this.erreur = new JLabel("Erreur, pas d'utilisateurs connectés");
			this.add(this.erreur);
		}
		else {
			String[] psdo = pseudo_uc();
			this.utilisateurs = new JButton[nb_uc];
			for (int i=0;i<nb_uc;i++) {
				this.utilisateurs[i]= new JButton(psdo[i]);
				//this.utilisateurs[i].setMinimumSize(new Dimension(4000,4000));
				this.add(this.utilisateurs[i]);
				this.utilisateurs[i].addActionListener(this.acH);
			}
		}
	
		this.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));
	
	}
	
	String[] pseudo_uc() { //recup une liste de pseudo des uc
		/*String[] uc;
		nb_uc = co.getSocket().getUserList().size();
		uc = new String[nb_uc];
		for (int i=0;i<nb_uc;i++) {
			uc[i] = co.getSocket().getUserList().get(i).getPseudo();
		}
		
		return uc;*/
		
		//pour test
		String[] uc;
		nb_uc = connectedUserList.size();
		uc = new String[nb_uc];
		for (int i=0;i<nb_uc;i++) {
			uc[i] = connectedUserList.get(i).getPseudo();
		}
		
		return uc;
	}

}
////////////////////////////////////////////////////////////////////////////////////////

///////////////PAGE DE CONVERSATION (JPanel)////////////////////////////////////////////
class conversationPage extends JPanel{
	private JTextArea[] discussion;
	private JTextField message;
	private JButton envoi_message;
	private envoyermessageHandler emH = new envoyermessageHandler();
	
	public conversationPage() {
		super(new GridLayout(0,1));
		try {
			if (co.getConversation().isEmpty()) {
				this.discussion = new JTextArea[1];
				this.discussion[0] = new JTextArea("pas de conversation");
				this.discussion[0].setForeground(Color.RED);
			}
			else {
				this.discussion = new JTextArea[co.getConversation().getConvSize()];
				Message[] m = co.getConversation().getAllMessages();
				for (int i=0;i<co.getConversation().getConvSize();i++) {
					this.discussion[i] = new JTextArea(m[i].getTimestamp() + " : " + m[i].getMsg());
					if (m[i].getIsEnvoyeur()) {
						this.discussion[i].setForeground(Color.BLUE);
						//this.discussion[i].setHorizontalAlignment(SwingConstants.RIGHT);
					}
					else {
						this.discussion[i].setForeground(Color.DARK_GRAY);
						//this.discussion[i].setHorizontalAlignment(SwingConstants.LEFT);
					}
					this.add(this.discussion[i]);
				}
			} 
		} catch (NullPointerException e) {
			this.discussion = new JTextArea[1];
			this.discussion[0] = new JTextArea("pas de conversation");
			this.discussion[0].setForeground(Color.RED);
		}
		

		this.message = new JTextField();
		try {
		this.envoi_message = new JButton("envoyer le message à "+ co.getConversation().getDestinataire().getPseudo());
		} catch (NullPointerException e) {
			this.envoi_message = new JButton("envoyer le message");
		}
		this.add(this.message);
		this.add(this.envoi_message);
		
		this.envoi_message.addActionListener(this.emH);
		
		this.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));
		
	}
	
	
	
}
////////////////////////////////////////////////////////////////////////////////////////
	
////////////////////////////////MENU BAR (JMenuBar)/////////////////////////////////////////////////////////////////
	class MenuBar extends JMenuBar{
		
		private JMenu msysteme;
		private JMenu mconversation;
		private JMenuItem fermerapp;
		private JMenuItem deconnexion;
		private JMenuItem changercompte;
		private JMenuItem changerpseudo;
		private JMenuItem creercompte;
		private JMenuItem changerconversation;
		private JMenuItem userco;
		
		
		public MenuBar() {
			super();
			
			this.msysteme = new JMenu("Système");
			this.mconversation = new JMenu("Conversations");
			this.fermerapp = new JMenuItem("fermer l'application");
			this.deconnexion = new JMenuItem("se déconnecter");
			this.changercompte = new JMenuItem("changer de compte");
			this.changerpseudo = new JMenuItem("modisfier le pseudo");
			this.creercompte = new JMenuItem("créer un compte");
			this.changerconversation = new JMenuItem("changer de conversation");
			this.userco = new JMenuItem("utilisateurs connectés");
			
			this.add(this.msysteme);
			this.add(this.mconversation);
			
			this.msysteme.add(this.deconnexion);
			this.deconnexion.addActionListener(new deconnexionHandler());
			this.msysteme.add(this.changercompte);
			this.changercompte.addActionListener(new deconnexionHandler());
			this.msysteme.add(this.changerpseudo);
			this.changerpseudo.addActionListener(new changerpseudoHandler());
			this.msysteme.add(this.creercompte);
			this.creercompte.addActionListener(new pagecreationcompteHandler());
			this.msysteme.add(this.fermerapp);
			this.fermerapp.addActionListener(new fermerappHandler());
			
			this.mconversation.add(this.changerconversation);
			this.changerconversation.addActionListener(new changerconversationHandler());
			this.mconversation.add(this.userco);
			this.userco.addActionListener(new utilisateursconnectesHandler());
			
		}
		
		
		
	}
////////////////////////////////////////////////////////////////////////////////////////////////////////
	
	
///////////////////////////////LES ACTIONS LISTENER///////////////////////////////////////////////////////
	private class pagecreationcompteHandler implements ActionListener {

		@Override
		public void actionPerformed(ActionEvent e) {
			setCreationcomptePage();
			
		}
		
		
	}
	
	private class creationcompteHandler implements ActionListener {

		@Override
		public void actionPerformed(ActionEvent e) {
			// TODO Auto-generated method stub
			
		}
		
	}
	
	private class connexionHandler implements ActionListener {
		

		@Override
		public void actionPerformed(ActionEvent e) {
			// TODO Auto-generated method stub
			String username_ = connexionpage.username.getText();
			String password_ = connexionpage.password.getText();
			
			if ( username_.equals("admin") && password_.equals("admin")) { //par exemple pour l'instant
				//(...)//
				connexionpage.erreur.setText("Entrez username/password");
				/*utilisateursconnectespage = new utilisateursconnectesPage();
				scrollbar_uc = new JScrollPane(utilisateursconnectespage);
				scrollbar_uc.setVerticalScrollBarPolicy(JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED);*/
				//scrollbar_uc.setBounds(50, 30, 300, 50);
				setUtilisateursconnectesPage_same_frame();
			}
			else {
				connexionpage.erreur.setText("erreur de connexion");
			}
			
		}
		
		
	}
	
	private class deconnexionHandler implements ActionListener {

		@Override
		public void actionPerformed(ActionEvent e) {
			// TODO Auto-generated method stub
			
			//(...)//
			connexionpage.erreur.setText("Entrez username/password");
			setConnexionPage();
		}
		
		
	}
	
	private class changerpseudoHandler implements ActionListener {

		@Override
		public void actionPerformed(ActionEvent e) {
			// TODO Auto-generated method stub
			
		}
		
		
	}
	
	private class fermerappHandler implements ActionListener {

		@Override
		public void actionPerformed(ActionEvent e) {
			// TODO Auto-generated method stub
			
			//(...) deconnexion//
			
			System.exit(0);
		}
		
		
	}
	
	private class changerconversationHandler implements ActionListener {

		@Override
		public void actionPerformed(ActionEvent e) {
			// TODO Auto-generated method stub
			
		}
		
		
	}
	
	private class utilisateursconnectesHandler implements ActionListener {

		@Override
		public void actionPerformed(ActionEvent e) {
			// TODO Auto-generated method stub
			
		}
		
		
	}
	
	private class afficherconversationHandler implements ActionListener {

		@Override
		public void actionPerformed(ActionEvent e) {
			// TODO Auto-generated method stub
			
			//(...) màj co.conversation//
			
			setConversationPage();
			
		}
		
		
	}
	
	private class envoyermessageHandler implements ActionListener {

		@Override
		public void actionPerformed(ActionEvent e) {
			// TODO Auto-generated method stub
			
			co.getConversation().addMessage(new Message(true,conversationpage.message.getText()));
			
			setConversationPage();
			
		}
		
		
	}
	
////////////////////////////////////////////////////////////////////////////////////////////////////////////
}
