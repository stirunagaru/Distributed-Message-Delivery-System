import java.io.*;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.HashMap;
import java.util.Map;

//Sumanjali Tirunagaru
public class TirunagaruP3RelayServer {
	
	static String actualString = null;/*  only one object and one reference should be */
    static final public Object sharedLock = new Object(); 
	static Map<String, String> userListFromFile; /*   for storing user name and associated password */
	static Map<String,Object> receiverListFromFile; /*   for storing user Receiver name and associated ip ,port address */
	
    public static void main(String args[]) throws Exception
	    { //main
		  int portNum = Integer.parseInt(args[0]);//17739; //args[0]
		  String specifiedSharedKey = args[1];//"TirunagaruP3@1739"; //args[1];
		  
		  if(args.length<2)
		  {  System.out.println("Please provide <portNumber> <secretKey> "); System.exit(0);}
		  
		 
		  if (specifiedSharedKey.getBytes().length < 16 || specifiedSharedKey.getBytes() == null)
		  {
			  
			  System.out.println("Invalid Length for the Secret Key [was less than 16 Bytes]");
			  System.exit(0);
		  }
		    
		  ServerSocket listenSocket = null;	 
		  
				/* loading details of users&passwords into hash map */
				  BufferedReader users = null;
				  String fromTextFile = null;
				  try {
						users = new BufferedReader(new FileReader("userList.txt"));
						userListFromFile = new HashMap<String, String>(); //System.out.println("-- in UserNameFromFile ---");
						while((fromTextFile = users.readLine())!=null)
						 {
								/* getting data from file UN, passwords */
							String[] tempData = fromTextFile.split(" ");	
							//System.out.println(tempData[0]+"-"+ tempData[1]);
							userListFromFile.put(tempData[0], tempData[1]);
						 }
					   } 
					 catch (FileNotFoundException e) { System.out.println(" from UserNameFile() Method:"+e.getMessage());}
					 catch (IOException e) {System.out.println(" from UserNameFile() Method- HashMap:"+e.getMessage());}
				  //loading Network Details into hash map
					 BufferedReader receivers = null;
					 String receiverData = null;
					 String[] networkData = null;
					 try{
						 receivers = new BufferedReader(new FileReader("receiverList.txt")); /* reading details from text file*/
					   	 receiverListFromFile = new HashMap<String,Object>();
				   		 while((receiverData = receivers.readLine())!=null)
							{
								networkData = receiverData.split(" ");
								String recieverName = networkData[0];
								String 	recieverIP = networkData[1];
								String recieverPort = networkData[2];
								receiverListFromFile.put(recieverName, new Network(recieverIP,recieverPort));
							} //while
						 }//try
						 catch (FileNotFoundException e) { System.out.println(" from ReceiverListFromFile() Method:"+e.getMessage());} 
						 catch (IOException e) {System.out.println(" from ReceiverListFromFile() Method- HashMap:"+e.getMessage());}
			 
			try 
			  { //try
				 listenSocket = new ServerSocket(portNum);
				 System.out.println("Relay Server: Started Successfully");
				 while(true)
			   	  {	  
					 Socket senderSocket = listenSocket.accept(); 
					 new SenderRelayServer(senderSocket, specifiedSharedKey); /* accept() gives back new socket details with client stories */
			   	  } //while				 
			 } //try
		   catch(Exception e) {System.out.println("Error in creation of Server socket:\n "+e.getMessage()); }
		} //main	 
} // relay server



//------------------------------------------------------------///----------------------------------------------------------------///-----------

 class SenderRelayServer extends Thread
 {
	 
	Socket crsSocket;  
	Socket rrsSocket;
	//BufferedReader inFromSender; /* used for data flow From Sender */
	DataOutputStream outToSender; /* used for data flow To Sender */
	DataInputStream inFromSender;
	
	String senderTestMsg = null;
	String senderUsername= null;
	String senderPassword=null;
	
	String receiverName = null;
	String senderDataMessage = null;
	
	ReceiverRelayServer rrs = null;
	String[] actualMessage = null;
	TirunagaruP3Tea tea = null;
	String sharedKey =   null; //"TirunagaruP3@1739" ;
	
	
	
	//constructor
	public SenderRelayServer(Socket senderSocket, String key) {
		try {
			
		this.crsSocket = senderSocket;
		this.sharedKey = key;
		tea = new TirunagaruP3Tea(sharedKey.getBytes());
		
		//inFromSender = new BufferedReader(new InputStreamReader(crsSocket.getInputStream()));
		inFromSender = new DataInputStream(crsSocket.getInputStream());
		outToSender = new DataOutputStream(crsSocket.getOutputStream());
		
		System.out.println("Sending the Secret Shared Key to Sender");
		outToSender.write(sharedKey.getBytes());
		
		 byte[] ms = new byte[100];
		 inFromSender.read(ms); 
		
		this.start();
		} //try
		catch (IOException e) {
			if(crsSocket!=null && !crsSocket.isClosed())
			{
				 try {crsSocket.close(); } 
	    	     catch (IOException e1) {  System.out.println("from ClientRelayServer Constructor:"+e1.getMessage());  }
			}
			System.out.println("from Constructor-ClientRelayServer"+e.getMessage());
		}//catch
	} //constructor
	
	public synchronized void run()
	{
	try
	{
		
			System.out.println("Relay Server - Sender established completly, Sending Succcess mesg to Sender");
			
		      String send0 = "0 Success"+'\n' ;
	 		  byte[] send_0 = send0.getBytes();
	 		  byte[] encrytp_0 = tea.encrypt(send_0);
	 		  outToSender.write(encrytp_0);
			
			
			 byte[] message = new byte[100];
			 String send = null;
			 while (inFromSender.read(message) >= 0  ) /*  data from server*/
			 {
				 byte[] decrypt_bytes =  tea.decrypt(message);
				 String msgFrmSender = new String(decrypt_bytes);
				 
				 System.out.println("SENDER MESSAGE:"+msgFrmSender.substring(2).trim());
			/*first letter for finding specific message */
			/*second word is actual data i.e, Username */
			switch(msgFrmSender.charAt(0))
			{
			case 'A':
					actualMessage = msgFrmSender.split(" ");
					senderUsername=actualMessage[1].trim();
					System.out.println("Entered Username: "+senderUsername);
					
					if(TirunagaruP3RelayServer.userListFromFile.containsKey(senderUsername)) // trim her once
					{
						
						  send = "A Username Exists"+'\n' ;
				 		  byte[] send_A = send.getBytes();
				 		  byte[] encrytp_A = tea.encrypt(send_A);
				 		  
				 		  outToSender.write(encrytp_A); /* sending data to sender*/
					}
					else
					{
						
						  send ="B Username Not Exists"+'\n' ;
				 		  byte[] send_A = send.getBytes();
				 		  byte[] encrytp_A = tea.encrypt(send_A);
						
				 		  outToSender.write(encrytp_A);  /* sending data to sender*/
					}
					break;
			case 'B':
					actualMessage = msgFrmSender.split(" ");
					senderPassword = actualMessage[1].trim();
					String pairValue = (String) TirunagaruP3RelayServer.userListFromFile.get(senderUsername);
					
					if(pairValue.equals(senderPassword))
					{
						send = "C Matched"+'\n' ;
				 	    byte[] send_B = send.getBytes();
				 	    byte[] encrytp_B = tea.encrypt(send_B);
				 		  
				 		outToSender.write(encrytp_B);   /* sending data to sender*/
					}
					else{ 
						send = "D NotMatched"+'\n' ;
				 	    byte[] send_B = send.getBytes();
				 	    byte[] encrytp_B = tea.encrypt(send_B);
				 		  
				 		outToSender.write(encrytp_B);   /* sending data to sender*/
					}
					break;
		
		case 'C':
				actualMessage = msgFrmSender.split(" ");
				receiverName = actualMessage[1].trim();
				System.out.println("Receivername mentioned:"+receiverName);
				if(TirunagaruP3RelayServer.receiverListFromFile.containsKey(receiverName)) /* looking for receiver in text file*/
				{
					System.out.println("Establishing connection with Receiver Specified");
					Network nd = (Network)TirunagaruP3RelayServer.receiverListFromFile.get(receiverName);
					
					String receiverIpAdress =nd.getipAddress();
					int receiverPort =Integer.parseInt(nd.getportNumber());
					
					System.out.println("SENDER REQUESTED FOR RECEIVER: IP:"+receiverIpAdress+"port:"+receiverPort);
					 
					rrsSocket=new Socket(receiverIpAdress,receiverPort); /*  Connecting to Receiver*/
					
					send = "E Receiver Exists and connection is Being Made"+ '\n';
			 	    byte[] send_C = send.getBytes();
			 	    byte[] encrytp_C = tea.encrypt(send_C);
			 		  
			 		outToSender.write(encrytp_C);  /* sending data to sender*/
				
			 		rrs = new ReceiverRelayServer(rrsSocket, outToSender, inFromSender, sharedKey);
				}
				else
				{
					send = "F Receiver Doesnot Exists"+'\n';
			 	    byte[] send_C = send.getBytes();
			 	    byte[] encrytp_C = tea.encrypt(send_C);
			 		  
			 		outToSender.write(encrytp_C);  /* sending data to sender*/
				}
				break;
				
		case 'D': 
				senderDataMessage = msgFrmSender;
				TirunagaruP3RelayServer.actualString = senderDataMessage;
				synchronized (TirunagaruP3RelayServer.sharedLock){
					TirunagaruP3RelayServer.sharedLock.notifyAll();
				} 
				break;
		
		case 'Q':
				send = "Q    Client closed the connection"+'\n';
				byte[] send_Q = send.getBytes();
				byte[] encrytp_Q = tea.encrypt(send_Q);
	 		  
			
				ReceiverRelayServer.outToReceiver.write(encrytp_Q);  /* sending data to Receiver*/
			    break;
			
		case 'R':
				send = "B "+msgFrmSender+'\n';
				byte[] send_R = send.getBytes();
				byte[] encrytp_R = tea.encrypt(send_R);
	 		  
				ReceiverRelayServer.outToReceiver.write(encrytp_R);  /* sending data to Receiver*/
				break;
				  
		default: System.out.println("Something is wrong/ Corrupted With CLIENT MESSAGE");
				 break;	
		
			
			
			}
		}
	} //try
	catch(IOException ioe){ System.out.println("SENDER - RELAY SERVER: CONNECTION FAILED: MAIN TRY-CATCH");}
		
	
	}
}

 
 ////---------------------------------------------------------------------------------------------------------------------------------------------/////----
 
class ReceiverRelayServer extends Thread {
	
	TirunagaruP3Tea tea = null;

 	static DataOutputStream outToReceiver;
 	static DataInputStream inFromReceiver;
 	
 	static DataOutputStream outToSender;
 	static DataInputStream inFromSender;
 	static Socket srrsSocket = null; 
 	
     static String rrsMsg = null;
     static String processedString = null;
 	
 	/* connection with relay server and receiver with the name sender specified */
 	/* constructor */
 	public ReceiverRelayServer(Socket srrsSocket, DataOutputStream fromSRS, DataInputStream fromSRSBR, String sharedKey)
 	{	
 		try {
 		/* connection with receiver and Relay Server from Receiver - Relay Server */
 		/*through socket created before calling constructor */
 		/* buffers  made here		*/
 			outToReceiver = new DataOutputStream(srrsSocket.getOutputStream());
 			inFromReceiver = new DataInputStream(srrsSocket.getInputStream());
 			
 			/* connection with sender from sender - relay server */
 			outToSender =fromSRS;
 			inFromSender =fromSRSBR;
 			/* now thread will run to connect to receiver specified and for communication. */
 			this.tea = new TirunagaruP3Tea(sharedKey.getBytes());
 			
 			System.out.println("Sending the Secret Shared Key to Sender Specified - Reciever ");
 			outToReceiver.write(sharedKey.getBytes());
 			
 			byte[] ack = new byte[20];
 			inFromReceiver.read(ack);
 			
 			this.start();
 		} catch (IOException e) { e.printStackTrace();}
 	} //end constructor
 	
 	//thread run Method
 	public synchronized void run()
 	{
 		try {
 			System.out.println("-----In ReceiverRelayServer : Run Method ------");
 			
 			String send0 = "A    Connection from Relay Relay Server";
	 		  byte[] send_0 = send0.getBytes();
	 		  byte[] encrytp_0 = tea.encrypt(send_0);
	 		  outToReceiver.write(encrytp_0);
			
			
			 byte[] message = new byte[100];
			 String send = null;
			 while (inFromReceiver.read(message) >= 0  ) /*  data from server*/
			 {
				 byte[] decrypt_bytes =  tea.decrypt(message);
				 String rrsMsg = new String(decrypt_bytes);
			
 			
 				switch(rrsMsg.charAt(0))
 				{
 				case 'A':
 						 System.out.println("Connection established");
 						
 						 send = "G Connection is successfull with Receiver "+ '\n';
 				 		 byte[] send_A = send.getBytes();
 				 	     byte[] encrytp_A = tea.encrypt(send_A);
 				 		  
 				 	     outToSender.write(encrytp_A);  /* sending data to Sender*/
 						
 						 synchronized(TirunagaruP3RelayServer.sharedLock)
	 						 {
	 							try { TirunagaruP3RelayServer.sharedLock.wait();} 
	 							catch (InterruptedException e) { System.out.println("RRS- CASE A ; SYNC BLOCK");}
	 							
	 							 send = "B "+TirunagaruP3RelayServer.actualString+'\n';
	 	 				 		 byte[] send_A_2 = send.getBytes();
	 	 				 	     byte[] encrytp_A_2 = tea.encrypt(send_A_2);
	 							
	 							outToReceiver.write(encrytp_A_2); /* sending data to Receiver*/
	 						 }
	 					 break;
 				case 'B': 
	 					System.out.println("Client's String is Processed");
	 					 send = "H Output from server"+'\n';
	 				 	 byte[] send_B = send.getBytes();
	 				     byte[] encrytp_B = tea.encrypt(send_B);
						
	 					outToSender.write(encrytp_B); /* sending data to Sender*/
	 					
 				case 'C': 
	 					send = "X. Diconnecting : Server Closed Connection"+'\n';
					 	 byte[] send_C = send.getBytes();
					     byte[] encrytp_C = tea.encrypt(send_C);
						
						outToSender.write(encrytp_C); /* sending data to Sender*/
						  try 
	 		   	    		{ /* Closing all the data flow and socket*/
	 		            	    outToReceiver.close();
	 		   					inFromReceiver.close();
	 		   					srrsSocket.close();
	 		   					System.out.println("Disconnected with sender- RelayServer");
	 		   				} catch (IOException e) 
	 		   	    		{
	 		   					System.out.println("Unable to close the Buffers which are used for communicating");
	 		   				}
	 	   				 break;
 	   				 
 				case 'O' : 
		 					 send = rrsMsg+'\n';
		 					 System.out.println("RECEIVER MESSAGE: "+ rrsMsg.substring(2));
				 			 byte[] send_O = send.getBytes();
						     byte[] encrytp_O = tea.encrypt(send_O);
						     System.out.println("Sending to Sender");
				 			 
							 outToSender.write(encrytp_O); /* sending data to Sender*/
							 
				 		break;
			 		   			
 				case 'X' :  
	 					 send = "X. Disconnecting from Reciever - RelaySever"+'\n';
					 	 byte[] send_X = send.getBytes();
					     byte[] encrytp_X = tea.encrypt(send_X);
						
 						outToSender.write(encrytp_X); /* sending data to Sender*/
 						try 
 		   	    		{/* Closing all the data flow and socket*/
 		            	    outToReceiver.close();
 		   					inFromReceiver.close();
 		   	//				srrsSocket.close();
 		   					System.out.println("Connection Disconnected with Receiver");
 		   				} 
 						catch (IOException e) 
 		   	    		{ System.out.println("Unable to close Buffers with Receivers");	}
 		              	break;
 				
 				default:System.out.println("Something is wrong/Data Corrupted");
 						System.out.println(rrsMsg);
 						
 						send = "C.Quit"+'\n';
					 	byte[] send_Def = send.getBytes();
					    byte[] encrytp_Def = tea.encrypt(send_Def);
						
 						outToReceiver.write(encrytp_Def);
 						outToReceiver.close();
		   				inFromReceiver.close();
 						break;
 				} //switch
 			}//while
 		}//try
 		catch (IOException e) {System.out.println(e.getMessage());}		
 	} //run method
 } // class ReceiverRelayServer

//-------------------------------------------------------------------------------------------------------------------------------------------------//

/* for holding the Network IP and Port address as an Object associated with Receiver names */
class Network{

    private String ipAddress;
    private String portNumber;

    public Network(String ipAddress, String portNumber){
        this.ipAddress = ipAddress;
        this.portNumber = portNumber;
    }

	public String getipAddress() {
	    return ipAddress;
	}

	public void setipAddress(String ipAddress) {
	    this.ipAddress = ipAddress;
	}

	public String getportNumber() {
	    return portNumber;
	}
	
	public void setportNumber(String portNumber) {
	    this.portNumber = portNumber;
	}
}


//------------------------------------------------------------------------------------------------------------------------------------------------//