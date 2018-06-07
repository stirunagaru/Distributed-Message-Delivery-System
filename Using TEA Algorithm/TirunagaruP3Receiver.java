import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;

//Sumanjali Tirunagaru
public class TirunagaruP3Receiver {
	static ServerSocket receiver = null;
	public static void main(String[] args) 
	{
		int receiverport = Integer.parseInt(args[0]); /* Receivers port number*/
	//	String secretKey = args[1];//"TirunagaruP3@1739"; //args[1];
		
		if(args.length<1) //3
		  {  System.out.println("Please provide <portNumber> "); System.exit(0);}
		  
		
		
		try
		{
			receiver = new ServerSocket(receiverport);
			System.out.println("Receiver is on Service");
			while(true)
			{
				Socket acceptSocket = receiver.accept();  /* accepting the connection*/
				System.out.println("RECIEVER: Connection accepted");
				new ReceiverHelper(acceptSocket);
		//		new ReceiverHelper(acceptSocket, secretKey);

				
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
	class ReceiverHelper extends Thread implements TirunagaruP3Interface
	{
		static DataOutputStream outToRelayServer ; /*For data flow from Receiver to RelayServer*/
		static DataInputStream inFromRelayServer; /*For data flow To Receiver From RelayServer*/
	    TirunagaruP3Tea tea = null;
			
	    static Socket helperSocket;
	    String secretKey;
	    
	    String receiverMessage  = null;
	    String dataFromRelay = null;
	    String strings[] = null;
	   //constructor
		//public ReceiverHelper(Socket helperSocket, String secretKey)
		public ReceiverHelper(Socket helperSocket)
		{
			try
			{
	
			//	tea  = new TirunagaruP3Tea(secretKey.getBytes());
			outToRelayServer = new DataOutputStream(helperSocket.getOutputStream()); /* data  to RelayServer*/
			inFromRelayServer =new DataInputStream(helperSocket.getInputStream());  /* data from RelayServer*/
			
			byte[] key = new byte[20];
			inFromRelayServer.read(key);
			tea  = new TirunagaruP3Tea(key);
			System.out.println("Recieved Shared Secret Key from Relay Server: "+ new String(key));			
			
			byte[] ack = "Recieved Shared Key".getBytes();
			outToRelayServer.write(ack);
			
			
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
	
			byte[] message = new byte[100];
			 String send = null;
			 while (inFromRelayServer.read(message) >= 0  ) /*  data from server*/
			 {
				 byte[] decrypt_bytes =  tea.decrypt(message);
				 receiverMessage = new String(decrypt_bytes);
				 
    		
				System.out.println("RELAYSERVER MESSAGE: "+ receiverMessage.substring(4));
				switch(receiverMessage.charAt(0))
				{
				case 'A' : System.out.println("Connection Established Successfully with receiver");
						   
							 send = "A Success"+'\n' ;
					 		 byte[] send_A = send.getBytes();
					 		 byte[] encrytp_A = tea.encrypt(send_A);
					 		  
				    	outToRelayServer.write(encrytp_A);  /* sending data to relayServer*/
						   break;
						   
				case 'B':  strings = receiverMessage.substring(4).toLowerCase().split(" ");
						   String finalSub = CommonSubstring(strings);  /* code for finding substring */
						    if(finalSub.isEmpty())
						  	{
						    	send = "O empty"+'\n';
						 	    byte[] send_B = send.getBytes();
						 	    byte[] encrytp_B = tea.encrypt(send_B);
						    	
						    	outToRelayServer.write(encrytp_B); /* sending data to relayServer*/
						  	}
							else
							{
								send = "O Substring is:"+ finalSub +" and its length is: "+finalSub.length()+'\n';
						 	    byte[] send_B = send.getBytes();
						 	    byte[] encrytp_B = tea.encrypt(send_B);
						    	
						    	outToRelayServer.write(encrytp_B); /* sending data to relayServer*/
						  }
						 break;
						   
			    case 'C':  
			    	
					    	send = "X Disconnecting from receiver "+'\n';
					 	    byte[] send_C = send.getBytes();
					 	    byte[] encrytp_C = tea.encrypt(send_C);
					    	
					    	outToRelayServer.write(encrytp_C); /* sending data to relayServer*/
			    			try 
			   	    		{ /*closing socket and all buffers*/
			    				TirunagaruP3Receiver.receiver.close();
			    				
			    			  outToRelayServer.close();
			   				  inFromRelayServer.close();
			   				//helperSocket.close();
			   				  System.out.println("Disconnected with Receiver-Relay Server ");
			   				} catch (IOException e) {System.out.println("Could not Disconnect with Receiver-Relay Server");}
			    			break; 
			   				 
			    case 'Q' : 
					    	send = "X Disconnection from receiver "+'\n';
					 	    byte[] send_Q = send.getBytes();
					 	    byte[] encrytp_Q = tea.encrypt(send_Q);
					    	
					    	outToRelayServer.write(encrytp_Q); /* sending data to relayServer*/
					    	try 
			 	    		{/*closing socket and all buffers*/
			 				  TirunagaruP3Receiver.receiver.close();
			 					
			 				   outToRelayServer.close();
			 				   inFromRelayServer.close();
			 	//			   helperSocket.close();
			 				} catch (IOException e) {System.out.println("Could not Disconnect with Receiver-Relay Server");}
			 			   break;
			   
			    default:
			    		System.out.println("Something is wrong / Message Is Corrupted");
			   			break;		
			   				
				} //switch
			}//while
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
			{ 	System.out.println("Processed and Substring is : "+finalSubString);  }
	    return finalSubString;
	 } // Common Substring
} // Class ReceiverHelper
	
		

	
	
	

