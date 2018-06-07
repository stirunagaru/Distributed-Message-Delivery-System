import java.io.*;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.HashMap;
import java.util.Map;

//Sumanjali Tirunagaru
public class TirunagaruP1RelayServer {
	
	static String actualString = null;/*  only one object and one reference should be */
    static final public Object sharedLock = new Object(); 
	static Map<String, String> userListFromFile; /*   for storing user name and associated password */
	static Map<String,Object> receiverListFromFile; /*   for storing user Receiver name and associated ip ,port address */
	
    public static void main(String args[]) throws Exception
	    { //main
		  int portNum = Integer.parseInt(args[0]); 
		 // 17739; //args[0]
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
					 new SenderRelayServer(senderSocket); /* accept() gives back new socket details with client stories */
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
	BufferedReader inFromSender; /* used for data flow From Sender */
	DataOutputStream outToSender; /* used for data flow To Sender */
	
	String senderTestMsg = null;
	String senderUsername= null;
	String senderPassword=null;
	String senderMessage = null;
	String receiverName = null;
	String senderDataMessage = null;
	
	ReceiverRelayServer rrs = null;
	String[] actualMessage = null;
	
	//constructor
	public SenderRelayServer(Socket senderSocket) {
		try {
			
		this.crsSocket = senderSocket;
		inFromSender = new BufferedReader(new InputStreamReader(crsSocket.getInputStream()));
	 	outToSender = new DataOutputStream(crsSocket.getOutputStream());
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
			outToSender.writeBytes("0 Success"+'\n');
			senderMessage = inFromSender.readLine();
		do{
	
			System.out.println("SENDER MESSAGE:"+senderMessage.substring(2));
			/*first letter for finding specific message */
			/*second word is actual data i.e, Username */
			
			switch(senderMessage.charAt(0))
			{
			case 'A':
					actualMessage = senderMessage.split(" ");
					senderUsername=actualMessage[1];
					if(TirunagaruP1RelayServer.userListFromFile.containsKey(senderUsername))
					{
						outToSender.writeBytes("A.Username Exists"+'\n'); /* sending data to sender*/
					}
					else
					{
						outToSender.writeBytes("B. Username Not Exists"+'\n');  /* sending data to sender*/
					}
					break;
				
			case 'B':
					actualMessage = senderMessage.split(" ");
					senderPassword = actualMessage[1];
					String pairValue = (String) TirunagaruP1RelayServer.userListFromFile.get(senderUsername);
					
					if(pairValue.equals(senderPassword))
					{
						outToSender.writeBytes("C.Matched"+'\n');  /* sending data to sender*/
					}
					else{ outToSender.writeBytes("D.not matched"+'\n'); }  /* sending data to sender*/
					break;
			
			case 'C':
					actualMessage = senderMessage.split(" ");
					receiverName = actualMessage[1].trim();
					System.out.println("Receivername mentioned:"+receiverName);
					if(TirunagaruP1RelayServer.receiverListFromFile.containsKey(receiverName)) /* looking for receiver in text file*/
					{
						System.out.println("Establishing connection with Receiver Specified");
						Network nd = (Network)TirunagaruP1RelayServer.receiverListFromFile.get(receiverName);
						
						String receiverIpAdress =nd.getipAddress();
						int receiverPort =Integer.parseInt(nd.getportNumber());
						
						System.out.println("SENDER REQUESTED FOR RECEIVER: IP:"+receiverIpAdress+"port:"+receiverPort);
						 
						rrsSocket=new Socket(receiverIpAdress,receiverPort); /*  Connecting to Receiver*/
						outToSender.writeBytes("E Receiver Exists and connection is Being Made"+ '\n');  /* sending data to sender*/
						rrs = new ReceiverRelayServer(rrsSocket, outToSender, inFromSender);
					}
					else
					{
						outToSender.writeBytes("F Receiver Doesnot Exists"+'\n');  /* sending data to sender*/
					}
					break;
					
			case 'D': 
					senderDataMessage = senderMessage;
					TirunagaruP1RelayServer.actualString = senderDataMessage;
					synchronized (TirunagaruP1RelayServer.sharedLock){
						TirunagaruP1RelayServer.sharedLock.notifyAll();
					} 
					break;
			
			case 'Q':
					ReceiverRelayServer.outToReceiver.writeBytes("Q    Client closed the connection"+'\n');  /* sending data to Receiver*/
				    break;
				
			case 'R':
					ReceiverRelayServer.outToReceiver.writeBytes("B "+senderMessage+'\n');  /* sending data to Receiver*/
					break;
					  
			default: System.out.println("Something is wrong: With CLIENT MESSAGE");
					 break;	
			
			}
		}while((senderMessage = inFromSender.readLine())!= null);
	} //try
	catch(IOException ioe){ System.out.println("SENDER - RELAY SERVER: CONNECTION FAILED: MAIN TRY-CATCH");}
		
	
	}
}

 
 ////---------------------------------------------------------------------------------------------------------------------------------------------/////----
 
class ReceiverRelayServer extends Thread {

 	static DataOutputStream outToReceiver;
 	static BufferedReader inFromReceiver;
 	static DataOutputStream outToSender;
 	static BufferedReader inFromSender;
 	static Socket srrsSocket = null; 
 	
     static String rrsMsg = null;
     static String processedString = null;
 	
 	/* connection with relay server and receiver with the name sender specified */
 	/* constructor */
 	public ReceiverRelayServer(Socket srrsSocket, DataOutputStream fromSRS, BufferedReader fromSRSBR)
 	{	
 		try {
 		/* connection with receiver and Relay Server from Receiver - Relay Server */
 		/*through socket created before calling constructor */
 		/* buffers  made here		*/
 			outToReceiver = new DataOutputStream(srrsSocket.getOutputStream());
 			inFromReceiver = new BufferedReader(new InputStreamReader(srrsSocket.getInputStream()));
 			
 			/* connection with sender from sender - relay server */
 			outToSender =fromSRS;
 			inFromSender =fromSRSBR;
 			/* now thread will run to connect to receiver specified and for communication. */
 			this.start();
 		} catch (IOException e) { e.printStackTrace();}
 	} //end constructor
 	
 	//thread run Method
 	public synchronized void run()
 	{
 		try {
 			System.out.println("-----In ReceiverRelayServer : Run Method ------");
 			outToReceiver.writeBytes("A    Connection from Relay Relay Server"+'\n');
 		
 			while((rrsMsg = inFromReceiver.readLine())!= null) /*  data from Receiver*/
 			{
 				switch(rrsMsg.charAt(0))
 				{
 				case 'A':
 						 System.out.println("Connection established");
 						 outToSender.writeBytes("G Connection is successfull with Receiver "+ '\n'); /* sending data to Sender*/
 						
 						 synchronized(TirunagaruP1RelayServer.sharedLock)
	 						 {
	 							try { TirunagaruP1RelayServer.sharedLock.wait();} 
	 							catch (InterruptedException e) { System.out.println("RRS- CASE A ; SYNC BLOCK");}
	 							outToReceiver.writeBytes("B "+TirunagaruP1RelayServer.actualString+'\n'); /* sending data to Receiver*/
	 						 }
	 					 break;
 				case 'B': 
	 					System.out.println("Client's String is Processed");
	 					outToSender.writeBytes("H Output from server"+'\n'); /* sending data to Sender*/
	 					
 				case 'C': 
	 	    		     outToSender.writeBytes("X. Diconnecting : Server Closed Connection"+'\n'); /* sending data to Sender*/
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
			 			   System.out.println("RECEIVER MESSAGE: "+ rrsMsg.substring(2));
			 			   outToSender.writeBytes(rrsMsg +'\n');
			 		   	   break;
			 		   			
 				case 'X' :  
 						outToSender.writeBytes("X. Disconnecting from Reciever - RelaySever"+'\n'); /* sending data to Sender*/
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
 				
 				default:System.out.println("RRS - Switch: No Desired message");
 						System.out.println(rrsMsg);
 						outToReceiver.writeBytes("C.Quit"+'\n');
 						break;
 				} //switch
 			}//while
 		}//try
 		catch (IOException e) {}		
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