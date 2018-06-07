import java.io.BufferedReader;
import java.io.DataOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.ServerSocket;
import java.net.Socket;

//Sumanjali Tirunagaru
public class TirunagaruP1Receiver {
	static ServerSocket receiver = null;
	public static void main(String[] args) 
	{
		int receiverport = Integer.parseInt(args[0]);
		//17740; //Integer.parseInt(args[0]); /* Receivers port number*/
		try
		{
			receiver = new ServerSocket(receiverport);
			System.out.println("Receiver is on Service");
			while(true)
			{
				Socket acceptSocket = receiver.accept();  /* accepting the connection*/
				System.out.println("RECIEVER: Connection accepted");
				new ReceiverHelper(acceptSocket);
				
			}
		}
		catch(IOException e)
			     {  
					if (receiver != null && !receiver.isClosed()) 
			    	 { 
			    	 	try{ receiver.close();} catch (IOException e1) { e1.printStackTrace(System.err);}
			    	 }
			     }  			
	}// main
}//class Receiver	
	class ReceiverHelper extends Thread implements TirunagaruP1Interface
	{
		static DataOutputStream outToRelayServer ; /*For data flow from Receiver to RelayServer*/
		static BufferedReader inFromRelayServer; /*For data flow To Receiver From RelayServer*/
	    static Socket helperSocket;
	    String receiverMessage  = null;
	    String dataFromRelay = null;
	    String strings[] = null;
	   //constructor
		public ReceiverHelper(Socket helperSocket)
		{
			try
			{
			outToRelayServer = new DataOutputStream(helperSocket.getOutputStream()); /* data  to RelayServer*/
			inFromRelayServer = new BufferedReader(new InputStreamReader(helperSocket.getInputStream()));  /* data from RelayServer*/
			this.start(); //starts a thread
			} //try
			catch(IOException ioe)
			{
				System.out.println("RECEIVER HELPER CONSTRUCTOR");
				try 
	    		{
					outToRelayServer.close();
					inFromRelayServer.close();
				} catch (IOException e1) 
	    		{
					System.out.println("closing  Buffers ERRORS");
				}
			} // catch	
		} //constructor	
		
	public void run()
	{
		try {		
	//	outToRelayServer.writeBytes("A from Receiever"+'\n');
    		receiverMessage = inFromRelayServer.readLine(); /* Data from RelayServer */
    		do	
			{
				System.out.println("RELAYSERVER MESSAGE: "+ receiverMessage.substring(4));
				switch(receiverMessage.charAt(0))
				{
				case 'A' : System.out.println("Connection Established Successfully with receiver");
						   outToRelayServer.writeBytes("A Success"+'\n');  /* sending data to relayServer*/
						   break;
						   
				case 'B':  strings = receiverMessage.substring(4).toLowerCase().split(" ");
						   String finalSub = CommonSubstring(strings);  /* code for finding substring */
						    if(finalSub.isEmpty())
						  	{
						  		outToRelayServer.writeBytes("O empty"+'\n'); /* sending data to relayServer*/
						  	}
							else
							{
								outToRelayServer.writeBytes("O Substring is:"+ finalSub +" and its length is: "+finalSub.length()+'\n'); /* sending data to relayServer*/
							}
						 break;
						   
			    case 'C':  outToRelayServer.writeBytes("X Disconnecting from receiver "+'\n'); /* sending data to relayServer*/
			    			try 
			   	    		{ /*closing socket and all buffers*/
			    				TirunagaruP1Receiver.receiver.close();
			    				
			    			  outToRelayServer.close();
			   				  inFromRelayServer.close();
			   				//helperSocket.close();
			   				  System.out.println("Disconnected with Receiver-Relay Server ");
			   				} catch (IOException e) {System.out.println("Could not Disconnect with Receiver-Relay Server");}
			    			break; 
			   				 
			    case 'Q' : outToRelayServer.writeBytes("X Disconnection from receiver "+'\n');	
			 			   try 
			 	    		{/*closing socket and all buffers*/
			 				  TirunagaruP1Receiver.receiver.close();
			 					
			 				   outToRelayServer.close();
			 				   inFromRelayServer.close();
			 	//			   helperSocket.close();
			 				} catch (IOException e) {System.out.println("Could not Disconnect with Receiver-Relay Server");}
			 			   break;
			   
			    default:
			    		System.out.println("Something is wrong check in switch statement");
			   			break;		
			   				
				} //switch
			}while((receiverMessage=inFromRelayServer.readLine())!= null); //while
		} catch (IOException e) { }
		
		finally{ /*closing socket and all buffers*/
			outToRelayServer = null;
			inFromRelayServer = null;
			helperSocket = null;
			
					}
	} //run
	
	public String CommonSubstring(String senderData[])
	 { /*program for finding longest substring in a given strings  */
	     int length = senderData.length;
	     for (int s = 0; s < senderData.length; s++) {senderData[s]  = senderData[s].trim();}
	   
	     String reference = senderData[0]; // first word from array as reference
	     int reflen = reference.length();

	     String finalSubString = "";

	     for (int i = 0; i < reflen; i++) 
	     {
	         for (int j = i + 1; j <= reflen; j++) 
	         {
	        	 String subString = reference.substring(i, j); // generating all possible substrings of our reference subString[0] 
	             int k = 1;
	             for (k = 1; k < length; k++) 
	             { // Checking the generated stem is common to all words
	                 if (!senderData[k].contains(subString))
	                     break;
	             }
	             // If current substring is present in all strings and its length is greater than current result
	             if (k == length && finalSubString.length() < subString.length())
	            	 finalSubString = subString;
	         }
	     }
	    if(finalSubString.isEmpty())
		  	{	System.out.println("Empty");	}
		else
			{ 	System.out.println(finalSubString);  }
	    return finalSubString;
	 } // Common Substring
} // Class ReceiverHelper
	
		

	
	
	

