/*
 * @(#) jqIRC	0.4	08/12/2001
 *
 * Copyright (c), 2013 by Tom4u.
 *
 * License: We grant you this piece of source code to play with
 * as you wish provided that 1) you buy us a drink when we meet
 * somewhere someday. 2) Incase you don't want to fullfill the
 * first condition, you just buy something for one of your
 * beloved friends.
 */

import javax.swing.*;
import java.awt.*;

import java.awt.event.*;
import javax.swing.event.*;

import java.io.*;
import java.net.*;

import java.util.*;

import javax.swing.border.*;

public class tIRC extends JFrame implements Runnable
{
	String host;
	int port;
	JTabbedPane tabbedPane;

        // nickname is used for distinguishing a user on a sever
	// realname is not used anywhere actually. If you are a
	// dumb you might set it to your real username on your
	// ISP/server or whatever tht might be.
	String nickname;
	String realname;
	
        // Menu bar and menu items
	private JMenuBar menuBar = new JMenuBar();
	private JMenuItem newChannelPanelItem,
	                  newUserPanelItem;

	private Action newChannelAction, newUserAction;

        // Action to join a Channel
	private Action joinChannelAction;
	private Action changeNickAction;
	private Action whoisAction;
	private Action aboutAction;

	private JDialog dialog;
	JTextField nickJTextField1;
	JTextField nickJTextField2;
        JTextField nickJTextField3;

	String nickname1, nickname2, nickname3;
	JList list;
	private Socket chatSocket;
	BufferedReader fromServer;
	PrintWriter toServer;

	// For debugging purpose only
	PrintWriter debugOut;
	public tIRC(String host, int port)
	{
		this.host = host;
		this.port = port;

                // set's the title of the jqIRC window
		setTitle("tIRC");
		setJMenuBar(menuBar);

		JMenu connectionMenu = new JMenu("Connection");
		connectionMenu.setMnemonic('C');

		JMenuItem connectionMenuItem = new JMenuItem("New Connection");
		connectionMenu.add(connectionMenuItem);

		connectionMenuItem.addActionListener(new ActionListener()
		   {
		      public void actionPerformed(ActionEvent e)
		      {
		         System.out.println(" New Connection MenuItem chosen ");
			 System.out.println(" Now yet implemented dude ");
		      }
		   });
		
		JMenu actionMenu = new JMenu("Actions");
		actionMenu.setMnemonic('A');

		JMenu helpMenu = new JMenu("Help");
		helpMenu.setMnemonic('H');
		
		newUserAction = new NewUserAction("Talk Private",
	                            KeyStroke.getKeyStroke('P', Event.CTRL_MASK));
		joinChannelAction = new JoinChannelAction("Join a Channel",
	                            KeyStroke.getKeyStroke('J', Event.CTRL_MASK));
                changeNickAction = new ChangeNickAction("Change nick",
	                            KeyStroke.getKeyStroke('N', Event.CTRL_MASK));
                whoisAction = new WhoisAction("Whois",
	                            KeyStroke.getKeyStroke('W', Event.CTRL_MASK));
                aboutAction = new AboutAction("About tIRC");

		addMenuItem(actionMenu, newUserAction);
		addMenuItem(actionMenu, joinChannelAction);
		addMenuItem(actionMenu, changeNickAction);
		addMenuItem(actionMenu, whoisAction);
		addMenuItem(helpMenu, aboutAction);
		
		tabbedPane = createTabbedPane();
		
                // Let's create a ToolBar
		JToolBar toolBar = new JToolBar();
		toolBar.add(newUserAction);
		toolBar.add(joinChannelAction);
		toolBar.add(changeNickAction);
		toolBar.add(whoisAction);

		toolBar.setBorder(BorderFactory.createCompoundBorder(
                                  BorderFactory.createRaisedBevelBorder(),
                                  BorderFactory.createEmptyBorder(5,5,5,5)));
		
		getContentPane().add("North", toolBar);
		getContentPane().add("Center", tabbedPane);
		
		// Menus
		menuBar.add(actionMenu);
		menuBar.add(connectionMenu);
		menuBar.add(helpMenu);
		
                // mandatory things to do!
		//setBounds(50, 50, 600, 400);
                Toolkit theKit = java.awt.Toolkit.getDefaultToolkit();
		Dimension dm = theKit.getScreenSize();
		setBounds(dm.width/6, dm.height/6,
		          (dm.width*5)/8, // width
		          (dm.height*2)/3 // height
		         );

		setDefaultCloseOperation(EXIT_ON_CLOSE);
		setVisible(true);

		// Let's see How can get connection info
		makeConnectionInfo();

		connectStartRegister();
	}

	public void connectStartRegister()
	{
	   connect();
	   start();
	   register();
	}

	private void makeConnectionInfo()
	{
	   dialog = new JDialog(this, " Connection Info ", true);
           Border etched = BorderFactory.createEtchedBorder();

	   //String s = JOptionPane.showInputDialog(" Show message ");
	   Container dialogContentPane = dialog.getContentPane();

	   // Ok, let's make the UserInfo
	   JPanel userInfo = new JPanel();
	   userInfo.setBorder(makeBorder(" User Info "));
	   JLabel label1 = new JLabel("Nickname1: ");
	   JLabel label2 = new JLabel("Nickname2: ");
	   JLabel label3 = new JLabel("Nickname3: ");

	   nickJTextField1 = new JTextField(25);
	   nickJTextField2 = new JTextField(25);
           nickJTextField3 = new JTextField(25);

	   Box box1 = Box.createHorizontalBox();
	   box1.add(label1);
	   box1.add(nickJTextField1);

	   Box box2 = Box.createHorizontalBox();
	   box2.add(label2);
	   box2.add(nickJTextField2);

           Box box3 = Box.createHorizontalBox();
           box3.add(label3);
           box3.add(nickJTextField3);

           Box box4 = Box.createVerticalBox();
           box4.add(box1);
           box4.add(box2);
           box4.add(box3);

           userInfo.add("Center", box4);

           // Let's make the Server area
	   JPanel serverInfo = new JPanel();
	   serverInfo.setBorder(makeBorder(" Server Info "));
	   String[] servers = {
				"irc.dal.net",
	                        "irc.snowirc.us.to", "irc.freenode.net"
	                      };
	   list = new JList(servers);
	   //list.setVisibleRowCount(5);
	   JScrollPane scrollPane = new JScrollPane(list);
	   scrollPane.setBorder(BorderFactory.createLoweredBevelBorder());
	   serverInfo.add("Center", scrollPane);

           JPanel buttonPanel = new JPanel();
           buttonPanel.setBorder(BorderFactory.createCompoundBorder(
	                               BorderFactory.createLineBorder(Color.black, 1),
                                       BorderFactory.createRaisedBevelBorder()
                               ));

           JButton connectButton = new JButton("Connect");
           JButton cancelButton = new JButton("Cancel");
           JButton editButton = new JButton("Edit");
           JButton removeButton = new JButton("Remove");

           connectButton.setBorder(makeButtonBorder());
           cancelButton.setBorder(makeButtonBorder());
           editButton.setBorder(makeButtonBorder());
           removeButton.setBorder(makeButtonBorder());

           // Action Listener
           connectButton.addActionListener(new ActionListener()
	      {
	         public void actionPerformed(ActionEvent e)
		 {
		    //System.out.println("Nickname1: " + nickJTextField1.getText());
		    //System.out.println("Nickname2: " + nickJTextField2.getText());
		    //System.out.println("Nickname3: " + nickJTextField3.getText());

		    // OK, let's set the nickname if user has mentioned it
		    if (nickJTextField1.getText() != null)
		    {
		       setNickname(nickJTextField1.getText());
		    }

		    if (nickJTextField2.getText() != null)
		    {
		       nickname2 = nickJTextField2.getText();
		    }

		    if (nickJTextField3.getText() != null)
		    {
		       nickname3 = nickJTextField3.getText();
		    }

		    // Setting the hostname(the IRC server)
                    if (list.getSelectedValue() != null)
		    {
		       host = (String)list.getSelectedValue();
		       System.out.println("Connecting to: " + list.getSelectedValue());
		    }

		    dialog.dispose();
		    dialog = null;
		 }
	      });

           //cancelButton.addActionListener(Test.this);
           //editButton.addActionListener(Test.this);
           //removeButton.addActionListener(Test.this);

           buttonPanel.add(connectButton);
           buttonPanel.add(cancelButton);
	   buttonPanel.add(removeButton);
	   buttonPanel.add(editButton);

           dialogContentPane.add("North", userInfo);
           dialogContentPane.add("Center", serverInfo);
           dialogContentPane.add("South", buttonPanel);

	   dialog.pack();
	   dialog.setLocationRelativeTo(this);
	   dialog.show();
	}

        private Border makeButtonBorder()
        {
           return BorderFactory.createCompoundBorder(
                     BorderFactory.createRaisedBevelBorder(),
                     BorderFactory.createEmptyBorder(2,4,2,4)
                     );
        }


        private Border makeBorder(String title)
        {
           Border etched = BorderFactory.createEtchedBorder();
	   Border border = BorderFactory.createTitledBorder
	                          (
	                           etched, title, TitledBorder.CENTER, 
				   TitledBorder.TOP
				  );
           return border;
	}

        /**
	 * Connect tries to establish a connection to host in port
	 */
	private void connect()
	{
		try
		{
			chatSocket = new Socket(host, port);
			fromServer = new BufferedReader(new InputStreamReader(
				                                    chatSocket.getInputStream()));
                        toServer = new PrintWriter(new OutputStreamWriter(
				                                    chatSocket.getOutputStream()));
                        //debugOut = new PrintWriter(new FileWriter(new File("Debug.txt")));
		}
		catch(IOException e)
		{
			System.err.println("Connection refused to: " + host + ":" + port);
		}
	}

	private void register()
	{
		Random random = new Random();

                if (nickname == null)
		{
		   nickname = "tIRC" + random.nextInt(9999);
                }
		realname = nickname;

                parseSendToCommand("PASS tIRC");
                parseSendToCommand("NICK " + nickname);
                parseSendToCommand("USER " + nickname + " 0 * :" + realname);
	}

        // sends back your current nickname
	public String getNickname()
	{
	   return nickname;
	}

	// setNickname sets your current nickname
	public void setNickname(String nickname)
	{
	   this.nickname = nickname;
	}

	public void changeNickname(String oldNickname, String newNickname)
	{
	   int totalTabs = tabbedPane.getTabCount(); 

	   for (int i = 0; i < totalTabs; i++)
	   {
	      Component aComponent = tabbedPane.getComponentAt(i);

	      if (aComponent instanceof ChannelPanel)
	      {
	         // Do channel processing
		 // At first remove the old nickname from the userNameBox
		 ((ChannelPanel)aComponent).updateUserArea(oldNickname, "remove");
		 ((ChannelPanel)aComponent).updateUserArea(newNickname, "add");
		 ((ChannelPanel)aComponent).updateUserAreaWithNames();

		 // Now let's post a sensible comment on the channel Panel
		 ((ChannelPanel)aComponent).updateTextArea(oldNickname + 
		                                           " changes nickname to " + 
							   newNickname);
	      }
	      else if (aComponent instanceof UserPanel)
	      {
	      }
	   }
	}
	public String formatNickname(String nickname)
	{
	   int formatlen = 12;
	   String blank = "";
	   int len = nickname.length();
	   if (len >= formatlen)
	   {
	      return nickname;
	   }
	   else
	   {
	      for (int i = 0; i < (formatlen - len); i++)
	      {
	         blank = blank + " ";
	      }
	   }

	   return blank + nickname;
	}

	public String getRealname()
	{
	   return realname;
	}

	private Thread listener;
	public void start()
	{
		listener = new Thread(this);
		listener.setDaemon(true);
		listener.start();
	}
	public void run()
	{
		try
		{
			while(!Thread.interrupted())
			{
				String line = fromServer.readLine();
				if (line != null)
				{
					parseFromServer(line);
				}
				else
				{
				   System.out.println("READ a null line");
				   System.out.println("That means server has closed the connection");
				   System.out.println("Or something wrong happened in the network");

				   closeAll();
				   
				   try
				   {
				      listener.sleep(5000);
				   }
				   catch(InterruptedException e)
				   {
				   }

				   listener = null;
				   connectStartRegister();
				}
			}
		}
		
		catch(IOException e)
		{
		   System.out.println("Read Exception from Server. Exception is something like:");
		   System.out.println(e);
		}
	}
	public void closeAll()
	{
	   try
	   {
	      chatSocket.close();
	      fromServer.close();
	      toServer.close();
	   }
	   catch(IOException e)
	   {
	      System.out.println("chatSocket.close() thrown an IOException");
	      System.out.println("fromServer.close() thrown an IOException");
	      System.out.println("toServer.close() thrown an IOException");
	   }

	   chatSocket = null;
	   fromServer = null;
	   toServer = null;
	}
	public void parseFromServer(String lineFromServer)
	{
		myParser parser = new myParser(lineFromServer);
		String command = parser.getCommand();
		if (command.equals("PING"))
		{
		   parseSendToCommand("PONG :" + parser.getTrailing());
		}
                else if (command.equals("JOIN"))
                {
                   if (getNickname().equals(parser.getNick()) || 1 == 1)
		   {
		      String channelName = parser.getTrailing();
		      if (channelName.startsWith("#"))
		      {
		         channelName = channelName.substring(1);
		         int indexOfChannel = findTab(tabbedPane, "#" + channelName);
			 if (indexOfChannel == -1)
			 {
		            ChannelPanel channel = addChannel(tabbedPane, channelName);
			    channel.updateTextArea(parser.getNick() + " (" + parser.getUser() + "@" + 
			                 parser.getHost() +  ") has joined channel " + parser.getTrailing());
                            tabbedPane.setBackgroundAt(findTab(tabbedPane, "#" + channelName), Color.red);
			    tabbedPane.revalidate();
		            channel.updateUserArea(parser.getNick(), "add");
		            channel.updateUserAreaWithNames();
			 }
			 else // channelName tab exists!
			 {
		            ChannelPanel channel = (ChannelPanel)tabbedPane.getComponentAt(indexOfChannel);
			    channel.updateTextArea(parser.getNick() + " (" + parser.getUser() + "@" + 
			                 parser.getHost() +  ") has joined channel " + parser.getTrailing());
                            tabbedPane.setBackgroundAt(findTab(tabbedPane, "#" + channelName), Color.red);
			    tabbedPane.revalidate();

			    //System.out.println(" someone joined ");
		            channel.updateUserArea(parser.getNick(), "add");
		            channel.updateUserAreaWithNames();
			 }
		      }
		   }
		   else // some user joined channel
		   {
		      String channelName = parser.getTrailing();
		      if (channelName.startsWith("#"))
		      {
		         channelName = channelName.substring(1);
		         int indexOfChannel = findTab(tabbedPane, "#" + channelName);
			 if (indexOfChannel != -1) // Channel Tab exist
			 {
		            Component aComponent = tabbedPane.getComponentAt(indexOfChannel);
			    if (aComponent instanceof ChannelPanel)
			    {
			       ((ChannelPanel)aComponent).updateTextArea(parser.getNick() + " (" 
			              + parser.getUser() + "@"
			              + parser.getHost() +  ") has joined channel " 
				      + parser.getTrailing());
                               tabbedPane.setBackgroundAt(indexOfChannel, Color.red);
			       tabbedPane.revalidate();
			    }
			 }
		      }
		   }
                }
		else if (command.equals("PRIVMSG"))
		{
		   //System.out.println("Got a PRIVMSG: " + lineFromServer);
		   // handle PRIVMSG
		   String destination = parser.getMiddle();
		   if (destination.startsWith("#"))
		   {
		      // it's a channel!
		      String channelName = destination.substring(1);

                      // How to extract required fields from the lineFromServer
		      // ------------------------------------------------------
		      // System.out.println("Message from: " + parser.getNick());
		      // System.out.println("Channel name: " + channelName);
		      // System.out.println("Channel mesg: " + parser.getTrailing());

		      int indexOfChannel = findTab(tabbedPane, destination);
		      if (indexOfChannel != -1)  // A connection/tab already exists!
		      {
		         Component aComponent = tabbedPane.getComponentAt(indexOfChannel);
			 if (aComponent instanceof ChannelPanel)
			 {
			    ((ChannelPanel)aComponent).updateTextArea(
			               formatNickname("<" + parser.getNick() + "> ") + parser.getTrailing());
                            tabbedPane.setBackgroundAt(indexOfChannel, Color.red);
			    tabbedPane.revalidate();
			 }
		      }
		      else // A connection/tab doesn't exists, so create one!
		      {
		         ChannelPanel channel = addChannel(tabbedPane, channelName);
			 channel.updateTextArea(
			         formatNickname("<" + parser.getNick() + "> ") + "> " + parser.getTrailing());
                         tabbedPane.setBackgroundAt(findTab(tabbedPane, "#" + channelName), Color.red);
			 tabbedPane.revalidate();
		      }

		      //System.out.println("<" + parser.getNick() + ">" + " " + parser.getTrailing());

		   } //if (destination.startsWith("#"))
		   else //if (!destination.equalsIgnoreCase(getNickname()))
		   {
		      // it's from an user
		      // How to extract import fields from lineFromServer
		      // ------------------------------------------------
		      // String userName = destination;
		      // System.out.println("Message from: " + parser.getNick());
		      // System.out.println("Private mesg: " + parser.getTrailing());

		      int indexOfUser = findTab(tabbedPane, parser.getNick());
		      if (indexOfUser != -1)  // A user connection/tab already exists!
		      {
		         Component aComponent = tabbedPane.getComponentAt(indexOfUser);
			 if (aComponent instanceof UserPanel)
			 {
			    ((UserPanel)aComponent).updateTextArea(
			               formatNickname("<" + parser.getNick() + "> ") + parser.getTrailing());
                            tabbedPane.setBackgroundAt(indexOfUser, Color.red);
			    tabbedPane.revalidate();
			 }
		      }
		      else  // An user connection/tab doesn't exists, so create one
		      {
		         UserPanel userPanel = addUser(tabbedPane, parser.getNick());
			 userPanel.updateTextArea(
			           formatNickname("<" + parser.getNick() + "> ") + parser.getTrailing());
			 // Add the userName over here! Actually, this is responsible for
			 // showing the real username@hostname of a IRC client
			 userPanel.setTitleArea(parser.getUser() + "@" + parser.getHost());

			 // Now update the tabbedPane's tab
                         tabbedPane.setBackgroundAt(findTab(tabbedPane, parser.getNick()), Color.red);
			 tabbedPane.revalidate();
		      }
		      //System.out.println("<" + parser.getNick() + ">" + " " + parser.getTrailing());

		      // Let's see what we got from the PRIVMSG
		      //System.out.println("====================== PRIVMSG =======================");
		      //System.out.println("   getPrefix: " + parser.getPrefix());
		      //System.out.println("   getParams: " + parser.getParams());
		      //System.out.println("   getServer: " + parser.getServer());
		      //System.out.println("     getNick: " + parser.getNick());
		      //System.out.println("     getUser: " + parser.getUser());
		      //System.out.println("     getHost: " + parser.getHost());
		      //System.out.println(" getTrailing: " + parser.getTrailing());
		      //System.out.println("   getMiddle: " + parser.getMiddle());
		      //System.out.println("====================== PRIVMSG =======================");
		   }
		}
		else if (command.equals("JOIN"))
		{
		   // handle JOIN
                   //System.out.println(parser.getNick() + " joined in " + parser.getTrailing());

                   // tabUpdate(tabTitle, message)
		   tabUpdate(parser.getTrailing(), parser.getNick() + " joined in " + parser.getTrailing());
		}
		else if (command.equals("PART"))
		{
		   //System.out.println("Line From Server => " + lineFromServer);
		   // handle PART
                   //System.out.println(parser.getNick() + " leaving channel " + parser.getParams());
                   //System.out.println(" SOMETHING WRONG WITH THE ABOVE LINE ");

		   //System.out.println("PART:   getPrefix: " + parser.getPrefix());
		   //System.out.println("PART:   getServer: " + parser.getServer());
		   //System.out.println("PART:     getNick: " + parser.getNick());
		   //System.out.println("PART:     getUser: " + parser.getUser());
		   //System.out.println("PART:     getHost: " + parser.getHost());
		   //System.out.println("PART: getTrailing: " + parser.getTrailing());
		   //System.out.println("PART: getMiddle: " + parser.getTrailing());

                   // Let's grab the channel name
                   String channelName = parser.getParams();
		   int index = channelName.indexOf("#");
		   int index2 = channelName.indexOf(":");
		   if (index2 != -1)
		   {
		      channelName = channelName.substring(index, index2 - 1);
		   }
		   else
		   {
		      channelName = channelName.substring(index);
		   }

		   if (channelName.startsWith("#"))
		   {
		      channelName = channelName.substring(1);

		      //System.out.println("Trying to find: >" + "#" + channelName + "<");
		      int indexOfChannel = findTab(tabbedPane, "#" + channelName);
		      if (indexOfChannel == -1) // Tab doesn't exist. Do nothing
		      {
		         //System.out.println("Couldn't find channel: " + "#" + channelName);
		      }
		      else // Tab exist
		      {
		         Component aComponent = tabbedPane.getComponentAt(indexOfChannel);
			 if (aComponent instanceof ChannelPanel)
			 {
			    ((ChannelPanel)aComponent).updateTextArea(parser.getNick() + 
			    " leaving channel #" + channelName + " " + parser.getTrailing());

			    ((ChannelPanel)aComponent).updateUserArea(parser.getNick(), "remove");
			    ((ChannelPanel)aComponent).updateUserAreaWithNames();
			 }

			 // Ok, let's remove the tab if the user who left the channel is you
			 if (parser.getNick().equals(getNickname()))
			 {
			    tabbedPane.removeTabAt(indexOfChannel);
			 }
		      }
		   }

		   //System.out.println(parser.getNick() + " leaving channel #" + 
		   //                                        channelName + " " + parser.getTrailing());
		}
		else if (command.equals("QUIT"))
		{
		   // handle QUIT
		   System.out.println(parser.getNick() + " has quit " + parser.getTrailing());
		}
		else if (command.equals("NICK"))
		{
		   // handle NICK --- When a user changes its nickname
		   //System.out.println("Line From Server: >" + lineFromServer + "<");
                   //System.out.println(parser.getNick() + " change nick to " + parser.getTrailing());

		   int index = findTab(tabbedPane, parser.getNick());
		   if (index != -1)
		   {
		      // setting the user tab title to changed nickname
		      tabbedPane.setTitleAt(index, parser.getTrailing());

		      // Let's change the name of the UserPanel alsow
		      UserPanel temp = (UserPanel)tabbedPane.getComponentAt(index);
		      temp.setName(parser.getTrailing());
		   }

		   if (parser.getNick().equalsIgnoreCase(getNickname()))
		   {
		      //System.out.println("You changed your namename");
		      setNickname(parser.getTrailing());
		   }

		   // When a user changes his/her name, his name in every
		   // UserPanel or ChannelPanel should be changed too.
		   // So, we are doing it now --- here
		   String oldNickname = parser.getNick();
		   String newNickname = parser.getTrailing();

		   changeNickname(oldNickname, newNickname);
		}
		else if (command.equals("NOTICE"))
		{
		   // handle NOTICE
		   System.out.println(parser.getTrailing());
		}
		else if (command.equals("KICK"))
		{
                   // handle KICK
                   System.out.println(parser.getNick() + " got kicked from " + 
                                          parser.getMiddle() + " for " + parser.getTrailing());
		}
		else if (command.equals("001") ||  // RPL_WELCOME
		         command.equals("002") ||  // RPL_YOURHOST
                         command.equals("003") ||  // RPL_CREATED
                         command.equals("004"))    // RPL_MYINFO
                {
		   // successful registration
		   //System.out.println(parser.getTrailing());
		   tabUpdate("Init Window", parser.getTrailing());
		}
		else if (command.equals("005"))
		{
		   // RPL_BOUNCE
		   //System.out.println(" *** Alternative server suggested *** ");
		   System.out.println(parser.getTrailing());
		}
		else if (command.equals("251") ||
		         command.equals("252") ||
                         command.equals("253") ||
                         command.equals("254") ||
                         command.equals("255") ||
                         command.equals("256") ||
                         command.equals("257") ||
                         command.equals("258") ||
                         command.equals("259") 
                        )
		{
		   System.out.println(parser.getTrailing());
		   tabUpdate("Init Window", parser.getTrailing());
		}
		else if (command.equals("371") ||
		         command.equals("372") ||
                         command.equals("374") ||
                         command.equals("375") ||
                         command.equals("376")
			)
		{
                   System.out.println(parser.getTrailing());
		   tabUpdate("Init Window", parser.getTrailing());
		}
		else if (command.equals("311") // RPL_WHOISUSER
		        )
		{
		   //System.out.println("lineFromServer: " + lineFromServer);
		   //System.out.println("WHOIS:   getPrefix: " + parser.getPrefix());
		   //System.out.println("WHOIS:   getServer: " + parser.getServer());
		   //System.out.println("WHOIS:     getNick: " + parser.getNick());
		   //System.out.println("WHOIS:     getUser: " + parser.getUser());
		   //System.out.println("WHOIS:     getHost: " + parser.getHost());
		   //System.out.println("WHOIS: getTrailing: " + parser.getTrailing());
		   //System.out.println("WHOIS:   getMiddle: " + parser.getTrailing());
		   //System.out.println("WHOIS:   getParams: " + parser.getParams());

		   // Ok, let's see how we can process PROCESS
		   StringTokenizer st = new StringTokenizer(parser.getParams(), " \r\n");
		   String whoNick = "",
		          whoUser = "",
			  whoHost = "",
			  whoMode = "",
			  whoRealName = "";

                   for (int i = 0; st.hasMoreTokens();)
		   {
		      String aToken = st.nextToken();
		      if (i == 1)
		      {
		         whoNick = aToken;
		      }
		      else if (i == 2)
		      {
		         whoUser = aToken;
		      }
		      else if (i == 3)
		      {
		         whoHost = aToken;
		      }
		      else if (i == 4)
		      {
		         whoMode = aToken;
		      }
		      else if (i == 5)
		      {
		         whoRealName = aToken.substring(1);
		      }

		      i++;
		   }

		   //System.out.println("  nickname: " + whoNick);
		   //System.out.println("  username: " + whoUser);
		   //System.out.println("  hostname: " + whoHost);
		   //System.out.println("  realname: " + whoRealName);

		   // Let's show the messages in the Init Window
		   tabUpdate("Init Window", "  nickname: " + whoNick);
		   tabUpdate("Init Window", "  username: " + whoUser);
		   tabUpdate("Init Window", "  hostname: " + whoHost);
		   tabUpdate("Init Window", "  realname: " + whoRealName);
		}
		else if (command.equals("312"))
		{
		   String whoNick = "",
		          whoServer = "",
			  whoServerInfo = "";

		   StringTokenizer st = new StringTokenizer(parser.getParams(), " \r\n");
                   for (int i = 0; st.hasMoreTokens();)
		   {
		      String aToken = st.nextToken();
		      if (i == 1)
		      {
		         whoNick = aToken;
		      }
		      else if (i == 2)
		      {
		         whoServer = aToken;
		      }
		      else if (i == 3)
		      {
		         whoServerInfo = aToken.substring(1);
		      }

		      i++;
		   }
		   //System.out.println("    Server: " + whoServer);
		   //System.out.println("ServerInfo: " + whoServerInfo);

		   // Let's update the Init Window
		   tabUpdate("Init Window", "    Server: " + whoServer);
		   tabUpdate("Init Window", "ServerInfo: " + whoServerInfo);
		}
		else if (command.equals("313"))
		{
		   //System.out.println("  Operator: " + lineFromServer);
		   tabUpdate("Init Window", "  Operator: " + lineFromServer);
		}
		else if (command.equals("317"))
		{
		   String seconds = "",
		          trailing = "";
                   trailing = parser.getTrailing();
		   StringTokenizer st = new StringTokenizer(parser.getParams(), " \r\n");
                   for (int i = 0; st.hasMoreTokens();)
		   {
		      String aToken = st.nextToken();
		      if (i == 2)
		      {
		         seconds = aToken;
			 break;
		      }

		      i++;
		   }
		   //System.out.println(" Idle time: " + seconds + " " + trailing);
		   tabUpdate("Init Window", " Idle time: " + seconds + " " + trailing);
		}
		else if (command.equals("318"))
		{
		   //System.out.println(parser.getTrailing());

		   tabUpdate("Init Window", parser.getTrailing());
		}
		else if (command.equals("319"))
		{
		   //System.out.println("  Channels: " + parser.getTrailing());

		   tabUpdate("Init Window", "  Channels: " + parser.getTrailing());
		}
		else if (command.equals("301")) // RPL_AWAY
                {
		   //System.out.println("301: " + lineFromServer);

		   StringTokenizer st = new StringTokenizer(parser.getParams(), " \r\n");
		   String awayNickname = "",
		          awayReason = "";
                   for (int i = 0; st.hasMoreTokens();)
		   {
		      String aToken = st.nextToken();
		      if (i == 1)
		      {
		         awayNickname = aToken;
			 break;
		      }

		      i++;
		   }
		   tabUpdate("Init Window", awayNickname + " is away for \"" + parser.getTrailing() + "\"");
		}
		else if (command.equals("305")) // RPL_UNAWAY
                {
		   System.out.println("305: " + lineFromServer);
		}
		else if (command.equals("332")  // RPL_TOPIC
		        )
		{
		   String topicChannelName = "",
		          topicTopic = "";

		   StringTokenizer st = new StringTokenizer(parser.getParams(), " \r\n");
		   for (int i = 0; st.hasMoreTokens();)
		   {
		      String aToken = st.nextToken();
		      if (i == 1)
		      {
		         topicChannelName = aToken;
			 break;
		      }

		      i++;
		   }

		   System.out.println("Topic of channel " + topicChannelName + 
		                      " is: " + parser.getTrailing());

                   int indexOfChannel = findTab(tabbedPane, topicChannelName);
		   if (indexOfChannel != -1)
		   {
		      Component aComponent = tabbedPane.getComponentAt(indexOfChannel);
		      if (aComponent instanceof ChannelPanel)
		      {
		         ((ChannelPanel)aComponent).setTitleArea(parser.getTrailing());
		      }
		   }
		   else  // No channel yet, so creat one
		   {
		      // Sometimes this makes two channel Panels
		      //ChannelPanel channelPanel = addChannel(tabbedPane, topicChannelName);
		      //channelPanel.setTitleArea(parser.getTrailing());
		   }

		}
		else if (command.equals("TOPIC") 
		        )
		{
		   String topicChannelName = "";

                   StringTokenizer st = new StringTokenizer(parser.getParams(), " \r\n");
		   if (st.hasMoreTokens())
		      topicChannelName = st.nextToken();
		   System.out.println(parser.getNick() + " has changed the topic of " + 
		                      topicChannelName + " to: " + parser.getTrailing());

                   int indexOfChannel = findTab(tabbedPane, topicChannelName);
		   if (indexOfChannel != -1)
		   {
		      Component aComponent = tabbedPane.getComponentAt(indexOfChannel);
		      if (aComponent instanceof ChannelPanel)
		      {
		         ((ChannelPanel)aComponent).setTitleArea(parser.getTrailing());
		      }
		   }
		}
		else if (command.equals("353") //
		        )
                {
		   //System.out.println("Line From Server: >" + lineFromServer + "<");
		   // channel name
		   String channelName = parser.getMiddle();
		   int index = channelName.lastIndexOf("#");
		   channelName = channelName.substring(index);

                   // Find the tab of channelName
		   int indexOfChannel = findTab(tabbedPane, channelName);

		   // If the channelName tab exists!
		   if (indexOfChannel != -1)
		   {
		      Component aComponent = tabbedPane.getComponentAt(indexOfChannel);
		      if (aComponent instanceof ChannelPanel)
		      {
		         // All the usernames are in trailing!
		         String trailing = parser.getTrailing();

			 // let's split those usernames with StringTokenizer
		         StringTokenizer st = new StringTokenizer(trailing, " \r\n");

			 // How many users are there?
		         int totalTokens = st.countTokens();

		         for (int i = 0; i < totalTokens; i++)
		         {
			    // a username/nickname
			    String tempnickname = st.nextToken();

			    // A debugging message
		            //System.out.println("353: user: " + tempnickname + " in " + channelName);

			    // Add that username to channelPanel's usernameBox
		            ((ChannelPanel)aComponent).updateUserArea(tempnickname, "add");
		         }
		         ((ChannelPanel)aComponent).updateUserAreaWithNames();
		      }
		   }
		   else  // channelName tab doesn't exist!
		   {
		   }
		}
		else if (command.equals("433"))
		{
		   System.out.println("ERR_NICKNAMEINUSE: " + lineFromServer);
		   System.out.println("trying to connect again.... " + host + ":" + port);

		   tabUpdate("Init Window", nickname + " already in use " + parser.getTrailing());
		   nickname = nickname2;
		   tabUpdate("Init Window", " Trying to connect again with nickname: " + nickname);

		   nickname2 = nickname3;
		   nickname3 = null;
		   register();
		}
		else
		{
		   // Just print the message from server
		   // to the console --- rugged idea
		   //System.out.println(lineFromServer);
		   tabUpdate("Init Window", parser.getTrailing());
		}
	}

	private int findTab(final JTabbedPane tabbedPane, String title)
	{
	   int totalTabs = tabbedPane.getTabCount(); 

	   for (int i = 0; i < totalTabs; i++)
	   {
	      String tabTitle = tabbedPane.getTitleAt(i);

	      // Below lines are just for debugging purpose
	      // ------------------------------------------
	      // System.out.println(" tabTitle: " + tabTitle + " len: " + tabTitle.length());
	      // System.out.println("    title: " + title    + " len: " + title.length());
              
	      // Let's see whether tabbedPane title and title matches!
	      if (tabTitle.equalsIgnoreCase(title))
	      {
	         return i;
	      }
	   }
           
	   // Not found anything? Return -1
	   return -1;
	}

	private int tabUpdate(String tabTitle, String message)
	{
	   int indexOfTab = findTab(tabbedPane, tabTitle);
           
	   if (indexOfTab != -1)
	   {
	      Component aComponent = tabbedPane.getComponentAt(indexOfTab);
	      if (aComponent instanceof ChannelPanel)
	      {
	         ((ChannelPanel)aComponent).updateTextArea(message);
                 tabbedPane.setBackgroundAt(indexOfTab, Color.red);
		 tabbedPane.revalidate();
	      }
	      else if (aComponent instanceof UserPanel)
	      {
	         ((UserPanel)aComponent).updateTextArea(message);
                 tabbedPane.setBackgroundAt(indexOfTab, Color.red);
		 tabbedPane.revalidate();
	      }
	      else if (aComponent instanceof InitPanel)
	      {
	         ((InitPanel)aComponent).updateTextArea(message);
                 tabbedPane.setBackgroundAt(indexOfTab, Color.red);
		 tabbedPane.revalidate();
	      }
	   }

	   return indexOfTab;
	}

	private void parseSendToCommand(String lineToServer)
	{
	        //System.out.println("Sending to server: " + lineToServer);
		// At first let's see whether the connection is still alive or
		// not.
		toServer.print(lineToServer + "\r\n");
		toServer.flush();
	}

	private JMenuItem addMenuItem(JMenu menu, Action action)
	{
		JMenuItem item = menu.add(action);
		
		KeyStroke keystroke = (KeyStroke)action.getValue(action.ACCELERATOR_KEY);
		if (keystroke != null)
		   item.setAccelerator(keystroke);
		
		return item;
	}
	
	private JTabbedPane createTabbedPane()
	{
		JTabbedPane tabbedPane = new JTabbedPane();
		

                // Adding the init window
		tabbedPane.add(new InitPanel(), "Init Window");

		// addInitTab(tabbedPane);
		// Adding a sample channel tab
		// tabbedPane.add(new ChannelPanel("#bangladesh"), "#bangladesh");
		
		tabbedPane.addChangeListener(new ChangeListener()
		  {
				public void stateChanged(ChangeEvent e)
				{
					setSelectedTab();
				}
			});
		
		return tabbedPane;
	}
	
			
	private void setSelectedTab()
	{
		// to return a selected tab to it's original color
		if (tabbedPane.getModel().isSelected())
		{
			int index = tabbedPane.getSelectedIndex();
			
			// setting a background color to null makes
			// a tab's background to it's default background color
			tabbedPane.setBackgroundAt(index, null);
			
			tabbedPane.revalidate();
		}
	}
	
	public class NewUserAction extends AbstractAction
	{
		NewUserAction(String name)
		{
			super(name);
			putValue(Action.SHORT_DESCRIPTION, " Private msg to a nickname ");
		}
		
		NewUserAction(String name, KeyStroke keystroke)
		{
			this(name);
			if (keystroke != null)
			{
				putValue(ACCELERATOR_KEY, keystroke);
			}
		}
		
		public void actionPerformed(ActionEvent e)
		{
			String userName;
			
			userName = JOptionPane.showInputDialog(" Input a nickname to talk privately ");

			if (userName != null)
			{
			   addUser(tabbedPane, userName);
			}
			else
			{
			   System.out.println("You can do /msg nickname your message");
			   System.out.println("to send a private message to nickname");
			}
		}
	}

	public class JoinChannelAction extends AbstractAction
	{
	   JoinChannelAction(String name)
	   {
	      super(name);
	      putValue(Action.SHORT_DESCRIPTION, " Join a channel ");
	   }

	   JoinChannelAction(String name, KeyStroke keystroke)
	   {
	      this(name);
	      if (keystroke != null)
	      {
	         putValue(ACCELERATOR_KEY, keystroke);
	      }
	   }

	   public void actionPerformed(ActionEvent e)
	   {
	      //System.out.println("JoinChannelAction chosen");
	      //System.out.println("Gotta Send some message");
	      String channelName;

	      channelName = JOptionPane.showInputDialog(" Please Enter a Channel Name ");

              if (channelName != null)
	      {
	         if (channelName.startsWith("#"))
	         {
	            System.out.println("Trying to join: " + channelName);
		    if (toServer != null)
		    {
		       parseSendToCommand("JOIN " + channelName);
		    }
	         }
	         else
	         {
	            System.out.println("Trying to join: #" + channelName);
		    if (toServer != null)
		    {
		       parseSendToCommand("JOIN #" + channelName);
		    }
	         }
	      }
	      else
	      {
	         System.out.println("No channel name specified.");
		 System.out.println("You can also join a channel: ");
		 System.out.println("/join channel_name");
	      }
	   }
	}

	public class ChangeNickAction extends AbstractAction
	{
	   ChangeNickAction(String name)
	   {
	      super(name);
	      putValue(Action.SHORT_DESCRIPTION, "Change your current nickname");
	   }

	   ChangeNickAction(String name, KeyStroke keystroke)
	   {
	      this(name);
	      if (keystroke != null)
	      {
	         putValue(ACCELERATOR_KEY, keystroke);
	      }
	   }

	   public void actionPerformed(ActionEvent e)
	   {
	      String newNickname;

	      newNickname = JOptionPane.showInputDialog(" Change current nickname: " + getNickname() + " to: ");

              if (newNickname != null)
	      {
		 if (toServer != null)
		 {
	            parseSendToCommand("NICK " + newNickname);
		 }
	      }
	      else
	      {
	         System.out.println("You can also change your nickname like: ");
		 System.out.println("/nick newnickname");
	      }
	   }
	}

	public class WhoisAction extends AbstractAction
	{
	   WhoisAction(String name)
	   {
	      super(name);
	      putValue(Action.SHORT_DESCRIPTION, "Check nickname Info");
	   }

	   WhoisAction(String name, KeyStroke keystroke)
	   {
	      this(name);
	      if (keystroke != null)
	      {
	         putValue(ACCELERATOR_KEY, keystroke);
	      }
	   }

	   public void actionPerformed(ActionEvent e)
	   {
	      String whoisNickname;

	      whoisNickname = JOptionPane.showInputDialog(" Check Info of(Input nickname): ");

              if (whoisNickname != null)
	      {
		 if (toServer != null)
		 {
	            parseSendToCommand("WHOIS " + whoisNickname);
		 }
	      }
	      else
	      {
	         System.out.println("You can also check nickname Info:");
		 System.out.println("/whois nickname");
	      }
	   }
        }
	
	public class AboutAction extends AbstractAction
	{
	   AboutAction(String name)
	   {
	      super(name);
	      putValue(Action.SHORT_DESCRIPTION, "About us: j & q");
	   }

	   AboutAction(String name, KeyStroke keystroke)
	   {
	      this(name);
	      if (keystroke != null)
	      {
	         putValue(ACCELERATOR_KEY, keystroke);
	      }
	   }

	   public void actionPerformed(ActionEvent e)
	   {
	      //JOptionPane.showMessageDialog(jqIRC.this, "We are j & q");
	      JOptionPane.showMessageDialog(tIRC.this,                       // parent
	                                    "  tIRC 0.1\n" + 
					    "  (c) Tom4u 2012-2013 \n\n" +
					    "  Tom Hill\n" +
					    "  www.tom4u.co.uk ",
					    "About tIRC",                    // title
					    JOptionPane.INFORMATION_MESSAGE
	                                   );
	   }
	}

	private UserPanel addUser(final JTabbedPane tabbedPane, String name)
	{
	        UserPanel userPanel = new UserPanel(name);
		tabbedPane.add(userPanel, name);
		tabbedPane.revalidate();
		
		return userPanel;
	}	
	
	private ChannelPanel addChannel(final JTabbedPane tabbedPane, String name)
	{
	        ChannelPanel channel;

	        if (name.startsWith("#"))
		{
	           channel = new ChannelPanel(name);
		   tabbedPane.add(channel, name);
		}
		else
		{
	           channel = new ChannelPanel("#" + name);
		   tabbedPane.add(channel, "#" + name);
		}
		tabbedPane.revalidate();
		
		return channel;
	}	
	
	public JTabbedPane closeChannel(final JTabbedPane tabbedPane, String name)
	{
		int index;
		
		index = tabbedPane.indexOfTab(name);
		
		if (index != -1 && index >= 0)
		{
			tabbedPane.removeTabAt(index);
		}
		
		tabbedPane.revalidate();
		return tabbedPane;
	}
	
	public class ConnectionAction extends AbstractAction
	{
		ConnectionAction(String name)
		{
			super(name);
		}
		
		ConnectionAction(String name, KeyStroke keystroke)
		{
			this(name);
			if (keystroke != null)
			{
				putValue(ACCELERATOR_KEY, keystroke);
			}
		}
		
		public void actionPerformed(ActionEvent e)
		{
			Object source = e.getSource();
		}
	}

	public class InitPanel extends JPanel implements ActionListener
	{
		final String name = "Init Window";
		final JTextArea textArea = new JTextArea();
		final JTextArea userArea = new JTextArea();
		final JTextField textField = new JTextField();
		
		JButton closeButton = new JButton("X");
		JButton sendToUser = new JButton("toUser");
		
		public InitPanel()
		{
			makePanel();
		}
		
		private void makePanel()
		{
			setLayout(new BorderLayout());
			
			// let's add actionListener
                        textArea.setBorder(BorderFactory.createEmptyBorder(5,5,5,5));
                        textArea.setLineWrap(true);
                        textArea.setWrapStyleWord(true);
			textField.addActionListener(this);
			
                        JScrollPane sp1 = new JScrollPane(textArea);
                        JScrollPane sp2 = new JScrollPane(userArea);
			
			sp1.setVerticalScrollBarPolicy(JScrollPane.VERTICAL_SCROLLBAR_ALWAYS);
			sp2.setVerticalScrollBarPolicy(JScrollPane.VERTICAL_SCROLLBAR_ALWAYS);
			
                        JSplitPane splitPane = new JSplitPane(JSplitPane.HORIZONTAL_SPLIT,
                                                              sp1,
                                                              sp2);
                        splitPane.setOneTouchExpandable(true);
                        splitPane.setDividerLocation(400);
                        add(splitPane, BorderLayout.CENTER);
                        add(textField, BorderLayout.SOUTH);
			add(makeNorth(), BorderLayout.NORTH);
		}
		
		private Box makeNorth()
		{
			Box northBox = Box.createHorizontalBox();
			
			JButton linkButton = new JButton("V");
			JButton delinkButton = new JButton("^");
			JTextField titleArea  = new JTextField("Title goes over here");
			
			// let's add actions
			closeButton.addActionListener(this);
			sendToUser.addActionListener(this);
			
			northBox.add(closeButton);
			northBox.add(linkButton);
			northBox.add(delinkButton);
			northBox.add(titleArea);
			northBox.add(sendToUser);
			
			return northBox;
		}
		
		public void actionPerformed(ActionEvent e)
		{
			Object source = e.getSource();
			
			// let's take care of textField
			if (source == textField)
			{
				String message = textField.getText();

                                if (message.startsWith("/"))
			        {
				   // commands that start with "/"
				   message = message.substring(1);
				   textArea.append(message + "\n");
				   textField.setText("");
				
				   parseSendToCommand(message);
				}
			        else
				{
				   // update textArea
				   textArea.append(message + "\n");
				   textField.setText("");
				
				   parseSendToCommand(message);
				}
			}
		}
		
		public void resetTextField()
		{
			textField.setText("");
		}
		
		public void updateTextArea(String message)
		{
		        int oldCaretPosition = textArea.getCaretPosition();
			textArea.append(message + "\n");

			int newCaretPosition = textArea.getCaretPosition();
			if (newCaretPosition == oldCaretPosition)
			{
			   textArea.setCaretPosition(oldCaretPosition + (message + "\n").length());
			}

			//textArea.revalidate();
		}
	}

	private void analyzeCommand(String message)
	{
	   CommandInterpreter cmdI = new CommandInterpreter(message);

	   if (cmdI.getCommand().equals("JOIN"))
	   {
	      parseSendToCommand(cmdI.getCommand() + " " + 
	                         cmdI.getParam1()  + " " +
				 cmdI.getMessage());
	   }
	   else if (cmdI.getCommand().equals("PART") ||
	            cmdI.getCommand().equals("LEAVE"))
           {
	      parseSendToCommand("PART " +
	                         cmdI.getParam1() + " :" +
				 cmdI.getMessage());
	   }
	   else if (cmdI.getCommand().equals("QUIT"))
	   {
	      parseSendToCommand("QUIT :" +
	                         cmdI.getParam1() + " " +
				 cmdI.getMessage());
	   }
	   else if (cmdI.getCommand().equals("WHOIS"))
	   {
	      parseSendToCommand("WHOIS " +
	                         cmdI.getParam1() + " " +
				 cmdI.getMessage());
	   }
	   else if (cmdI.getCommand().equals("MSG"))
	   {
	      parseSendToCommand("PRIVMSG " +
	                         cmdI.getParam1() + " :" +
				 cmdI.getMessage());
	   }
	   else if (cmdI.getCommand().equals("NICK"))
	   {
	      parseSendToCommand("NICK " + cmdI.getParam1());
	   }
	   else if (cmdI.getCommand().equals("TOPIC"))
	   {
	      //System.out.println("Got a message: " + message);
	      //System.out.println("  getParam1(): " + cmdI.getParam1());
	      //System.out.println("  getMessage(): " + cmdI.getMessage());
              if (cmdI.getMessage().equals(""))
	      {
	         //System.out.println("Sending a message: " + cmdI.getCommand() + " " + cmdI.getParam1());
	         parseSendToCommand(cmdI.getCommand() + " " +
	                            cmdI.getParam1() + " :");
	      }
	      else
	      {
	         System.out.println("Sending a message: " + cmdI.getCommand() + " " + 
		                     cmdI.getParam1() + " :" + cmdI.getMessage());
	         parseSendToCommand(cmdI.getCommand() + " " +
	                            cmdI.getParam1() + " :" +
				    cmdI.getMessage());
	      }
	   }
	   else
	   {
	      //System.out.println("Got a message: " + message);
	      //System.out.println("Sending the message as it is");
	      parseSendToCommand(cmdI.getCommand() + " :" +
	                         cmdI.getParam1()  + " " +
				 cmdI.getMessage());
	   }
	}

	public class ChannelPanel extends JPanel implements ActionListener
	{
		final String name;
		final JTextArea textArea = new JTextArea();
		final JTextArea userArea = new JTextArea();
		final JTextField textField = new JTextField();
		
	        JTextField titleArea  = new JTextField();

		JButton closeButton = new JButton("X");
		JButton sendToUser = new JButton("toUser");

                // For saving users name is a sorted list
		Set usernameBox = new TreeSet();
		
		public ChannelPanel(String name)
		{
			this.name = name;
			
			makePanel();
		}
		
		private void makePanel()
		{
			setLayout(new BorderLayout());
			
			// let's add actionListener
                        textArea.setBorder(BorderFactory.createEmptyBorder(5,5,5,5));
                        textArea.setLineWrap(true);
                        textArea.setWrapStyleWord(true);
			textField.addActionListener(this);

                        // let's add actionListener 
                        userArea.setBorder(BorderFactory.createEmptyBorder(5,5,5,5));
                        userArea.setLineWrap(true);
                        userArea.setWrapStyleWord(true);
			
                        JScrollPane sp1 = new JScrollPane(textArea);
                        JScrollPane sp2 = new JScrollPane(userArea);
			
			sp1.setVerticalScrollBarPolicy(JScrollPane.VERTICAL_SCROLLBAR_ALWAYS);
			sp2.setVerticalScrollBarPolicy(JScrollPane.VERTICAL_SCROLLBAR_ALWAYS);
			
                        JSplitPane splitPane = new JSplitPane(JSplitPane.HORIZONTAL_SPLIT,
                                                              sp1,
                                                              sp2);
                        splitPane.setOneTouchExpandable(true);
                        splitPane.setDividerLocation(400);
                        add(splitPane, BorderLayout.CENTER);
                        add(textField, BorderLayout.SOUTH);
			add(makeNorth(), BorderLayout.NORTH);
		}
		
		private Box makeNorth()
		{
			Box northBox = Box.createHorizontalBox();
			
			JButton linkButton = new JButton("V");
			JButton delinkButton = new JButton("^");

			//JTextField titleArea  = new JTextField("Title goes over here");
			
			// let's add actions
			closeButton.addActionListener(this);
			sendToUser.addActionListener(this);
			
			northBox.add(closeButton);
			northBox.add(linkButton);
			northBox.add(delinkButton);
			northBox.add(titleArea);
			northBox.add(sendToUser);
			
			return northBox;
		}
		
		public void actionPerformed(ActionEvent e)
		{
			Object source = e.getSource();
			
			// let's take care of textField
			if (source == textField)
			{
				String message = textField.getText();
				
				//System.out.println("from textField: " + textField.getText());
				
				// let's send to server
				if (message.startsWith("/"))
				{
				   // commands that start with "/"
				   // message = message.substring(1);
				   analyzeCommand(message);
				   textField.setText("");
				}
				else
				{
				   // A private message to channel!
				   parseSendToCommand("PRIVMSG " + name + " :" + message);

				   // let's update textArea
				   // textArea.append(textField.getText() + "\n");
				   updateTextArea(formatNickname("<" + getNickname() + "> ") + 
				                  textField.getText());
				   resetTextField();
				}
			}
			else if (source == closeButton)
			{
				//System.out.println("Close Button Chosen" + name);
				closeChannel(tabbedPane, name);

				// Let's send a sensible message
				parseSendToCommand("PART " + name);
			}
			else if (source == sendToUser)
			{
				System.out.println("sendToUser was selected");
				System.out.println("But not yet implemented");
			}
		}
		
		public void resetTextField()
		{
			textField.setText("");
		}

		public void setTitleArea(String title) // Actually, it's user@host
		{
		   titleArea.setText("");
		   titleArea.setText(title);
		}
		
		public void updateTextArea(String message)
		{
		        int oldCaretPosition = textArea.getCaretPosition();
			textArea.append(message + "\n");

			int newCaretPosition = textArea.getCaretPosition();
			if (newCaretPosition == oldCaretPosition)
			{
			   textArea.setCaretPosition(oldCaretPosition + (message + "\n").length());
			}

			//textArea.revalidate();
		}

		public void updateUserAreaWithNames()
		{
		   int oldCaretPosition;
		   int newCaretPosition;

		   userArea.selectAll();
		   userArea.setText("");

                   oldCaretPosition = userArea.getCaretPosition();
		   for (Iterator i = usernameBox.iterator(); i.hasNext();)
		   {
		      // username
		      String username = (String)i.next();

		      // append the username to userPanel
		      userArea.append(username + "\n");

		      //System.out.println("Added: >" + username + "<");

                      // new Caret Position
		      newCaretPosition = userArea.getCaretPosition();

		      /*******
		      if (newCaretPosition == oldCaretPosition)
		      {
		         userArea.setCaretPosition(oldCaretPosition + (username + "\n").length());
		      }
		      else
		      {
		         oldCaretPosition = newCaretPosition;
		      }
		      *******/
		   }
		}

		public void updateUserArea(String username)
		{
		        int oldCaretPosition = userArea.getCaretPosition();
			userArea.append(username + "\n");

			int newCaretPosition = userArea.getCaretPosition();
			if (newCaretPosition == oldCaretPosition)
			{
			   userArea.setCaretPosition(oldCaretPosition + (username + "\n").length());
			}
		}

		public void updateUserArea(String username, String command)
		{
		        if (command.equals("add"))
			{
			   // add a username
			   usernameBox.add(username);
			}
			else if (command.equals("remove"))
			{
			   // remove a username
			   // System.out.println("removing a name from nambox");
			   usernameBox.remove(username);
			}
			// ((ChannelPanel)aComponent).updateUserArea(parser.getNick(), "remove");
		}
	}

	public class UserPanel extends JPanel implements ActionListener
	{
		String name;
		final JTextArea textArea = new JTextArea();
		final JTextArea userArea = new JTextArea();
		final JTextField textField = new JTextField();
		JTextField titleArea  = new JTextField("Title goes over here");
		
		JButton closeButton = new JButton("X");
		
		public UserPanel(String name)
		{
			this.name = name;
			
			makePanel();
		}

		public void setName(String name)
		{
		   this.name = name;
		}

		public void setTitleArea(String title) // Actually, it's user@host
		{
		   titleArea.setText("");
		   titleArea.setText(title);
		}
		
		private void makePanel()
		{
			setLayout(new BorderLayout());
			
			// let's add actionListener
                        textArea.setBorder(BorderFactory.createEmptyBorder(5,5,5,5));
                        textArea.setLineWrap(true);
                        textArea.setWrapStyleWord(true);
			textField.addActionListener(this);
			
                        JScrollPane sp1 = new JScrollPane(textArea);
                        JScrollPane sp2 = new JScrollPane(userArea);
			
			sp1.setVerticalScrollBarPolicy(JScrollPane.VERTICAL_SCROLLBAR_ALWAYS);
			sp2.setVerticalScrollBarPolicy(JScrollPane.VERTICAL_SCROLLBAR_ALWAYS);
			
                        JSplitPane splitPane = new JSplitPane(JSplitPane.HORIZONTAL_SPLIT,
                                                              sp1,
                                                              sp2);
                        splitPane.setOneTouchExpandable(true);
                        splitPane.setDividerLocation(400);
                        add(splitPane, BorderLayout.CENTER);
                        add(textField, BorderLayout.SOUTH);
			add(makeNorth(), BorderLayout.NORTH);
		}
		
		private Box makeNorth()
		{
			Box northBox = Box.createHorizontalBox();
			
			JButton linkButton = new JButton("V");
			JButton delinkButton = new JButton("^");
			//JTextField titleArea  = new JTextField("Title goes over here");
			
			// let's add actions
			closeButton.addActionListener(this);
			
			northBox.add(closeButton);
			northBox.add(linkButton);
			northBox.add(delinkButton);
			northBox.add(titleArea);
			
			return northBox;
		}
		
		public void actionPerformed(ActionEvent e)
		{
			Object source = e.getSource();
			
			// let's take care of textField
			if (source == textField)
			{
				String message = textField.getText();
				
				// let's send to the server
				if (message.startsWith("/"))
				{
				   // A command
				   analyzeCommand(message);
				   textField.setText("");
				}
				else
				{
				   // A normal private message to send to the user
				   parseSendToCommand("PRIVMSG " + name + " :" + message);

				   // let's update textArea
				   // textArea.append(textField.getText() + "\n");
				   updateTextArea(formatNickname("<" + getNickname() + "> ") + 
				                  textField.getText());
				   resetTextField();
				}
			}
			else if (source == closeButton)
			{
				//System.out.println("Close Button Chosen" + name);
				closeChannel(tabbedPane, name);
			}
		}
		
		public void resetTextField()
		{
			textField.setText("");
		}
		
		public void updateTextArea(String message)
		{
		        int oldCaretPosition = textArea.getCaretPosition();
			textArea.append(message + "\n");

			int newCaretPosition = textArea.getCaretPosition();
			if (newCaretPosition == oldCaretPosition)
			{
			   textArea.setCaretPosition(oldCaretPosition + (message + "\n").length());
			}
		}
	}

	
	public static void main(String[] args)
	{
		String host = "localhost";
		int port = 6667;
		
		if (args.length == 2)
		{
			host = args[0];
			try
			{
				port = Integer.parseInt(args[1]);
			}
			catch(NumberFormatException e)
			{
				System.out.println("port isn't an integer");
				System.out.println("using default port: " + port);
			}
		}
    tIRC theApp = new tIRC(host, port);
    }
}
