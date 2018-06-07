import java.io.BufferedReader;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.Socket;
import java.util.Scanner;

//Sumanjali Tirunagaru
public class TirunagaruP3Sender {
	
	
	public static void main(String args[]) throws Exception
	    {
		 String userName = null;
		 
		 
		 //IP-Address and port
		 String ipAddress = args[0];//"LOCALHOST"; //args[0]
		 int port =  Integer.parseInt(args[1]); //17739; //args[1]
		 
		 if(args.length<2)
		  {  System.out.println("Please provide <hostId> <portNumber>"); System.exit(0);}
		  
		 
		// String secretKey = args[2]; //"TirunagaruP3@1739";
		// TirunagaruP3Tea tea = new TirunagaruP3Tea(secretKey.getBytes());
		 Socket relayServerSocket = null;
		 DataInputStream dataFromRelayServer=null;
		 DataOutputStream dataToRelayServer = null;
		 BufferedReader fromConsole = null;
		 
		 Scanner sc = new Scanner(System.in);
		 try
		 {
		 fromConsole = new BufferedReader(new InputStreamReader(System.in)); /* used for taking data inFromUser */
		 relayServerSocket = new Socket(ipAddress, port); /* specifying the Relayserver port and IP address */
		 
		 dataToRelayServer = new DataOutputStream(relayServerSocket.getOutputStream());  /* used for sending data To Server */
		 dataFromRelayServer = new DataInputStream(relayServerSocket.getInputStream()); /* used for hold data from Server */
			
		 byte[] key = new byte[20];
		 dataFromRelayServer.read((key));
		 TirunagaruP3Tea tea = new TirunagaruP3Tea(key);
		 System.out.println("Recieved Shared Secret Key from Relay Server: "+ new String(key));

		 byte[] ack = "Recieved Shared Key".getBytes();
		 dataToRelayServer.write(ack);
		 
	     boolean yes  = true;
		 while(yes)
		 {
			 byte[] message = new byte[100];
			 String send = null;
			
			 if(dataFromRelayServer.read((message)) >= 0 )
			 { 
			 byte[] decrypt_bytes =  tea.decrypt(message);
			 String msgFrmRS = new String(decrypt_bytes);
			 switch(msgFrmRS.charAt(0))
			 {
				 case '0':System.out.println("Connection with Relay Server is Successful");
				 		  System.out.println("Please Enter your Username: ");
				 		  userName =fromConsole.readLine();
				 		  
				 		  send = "A "+userName+'\n' ;
				 		  byte[] send_0 = send.getBytes();
				 		  byte[] encrytp_0 = tea.encrypt(send_0);
				 		  
				 		  dataToRelayServer.write(encrytp_0);   /* sending data to server*/
				 		  break;
				 
			
				 case 'A':System.out.println("Congrats. Username Exists.");
					 	  System.out.println("Please Enter Your Password for "+userName+ ":");
					 	  String password =fromConsole.readLine();
					 	 
					 	  send = "B "+password+'\n';
				 		  byte[] send_A = send.getBytes();
				 		  byte[] encrytp_A = tea.encrypt(send_A);
				 		  dataToRelayServer.write(encrytp_A);	 /* sending data to server*/
					 	  break;
					 
				 case 'B':System.out.println("Username Does Not Exists with Relay Server");
					 	  System.out.println("Please Enter your Correct Username: ");
					 	  userName =fromConsole.readLine();
					 	 
					 	  send = "A "+userName+'\n' ;
				 		  byte[] send_B = send.getBytes();
				 		  byte[] encrytp_B = tea.encrypt(send_B);
				 		  
				 		  dataToRelayServer.write(encrytp_B);   /* sending data to server*/
				 		  break;
						 
				 case 'C':System.out.println("User is Authenticated");
					 	  System.out.println("Please Enter the Reciever Name you need to Connect With: ");
					 	  String receiverName =fromConsole.readLine();
					 	  
					 	  send = "C "+receiverName+'\n';
				 		  byte[] send_C = send.getBytes();
				 		  byte[] encrytp_C = tea.encrypt(send_C);
				 		  
				 		  dataToRelayServer.write(encrytp_C);    /* sending data to server*/
				 		  break;
					 	  
				 case 'D':System.out.println("Password You Entered for "+userName+" is Incorrect");
						  System.out.println("Please Enter your Correct password: ");
						  password =fromConsole.readLine();
						  
						  send = "B " +password+ '\n';
				 		  byte[] send_D = send.getBytes();
				 		  byte[] encrytp_D = tea.encrypt(send_D);
				 		  
				 		  dataToRelayServer.write(encrytp_D);    /* sending data to server*/
						  break;
					
				 case 'E':System.out.println("Receiver Name is correct");
					 	  System.out.println("Establishing Connection");
					 	  break;
				 
				 case 'F':System.out.println("Receiver Name Incorrect");
				  		  System.out.println("Please Enter Correct Receiver Name to connect with :");
				  		  receiverName = fromConsole.readLine();
				  		  
				  		  send = "C "+receiverName+'\n';
				 		  byte[] send_F = send.getBytes();
				 		  byte[] encrytp_F = tea.encrypt(send_F);
				 		  
				 		  dataToRelayServer.write(encrytp_F);  	 /* sending data to server*/
				  		  break;
				 	  
				 case 'G':System.out.println("Server and Reciever connection established");
				 		  String receiverString = "D";
					      System.out.println("Please Enter the No of Strings for Finding Substring");
					      int no = sc.nextInt();
						  for (int i = 0; i < no; i++) 
						  {
							  System.out.print("Enter String "+(i+1)+":");
							  String word = sc.next();
							  receiverString = receiverString +" "+word;
						  }
						
						  receiverString = receiverString + '\n';
						  byte[] send_G = receiverString.getBytes();
				 		  byte[] encrytp_G = tea.encrypt(send_G);
				 		 
						  dataToRelayServer.write(encrytp_G); /* sending data to server*/
						  break;
				 
				 case 'H':System.out.println(msgFrmRS.substring(2));
					 	  break;
				
				 case 'O':System.out.println("Output From the RelayServer:");
					 	  String subString = msgFrmRS.substring(2);
					 	  if(subString.contains("empty"))
					 	  {
							 System.out.println("No Substrings are Present for the given Sequence of Strings");
					 	  }
					 	  else{
							 System.out.println(subString);
					 	  }
					 	  System.out.println("1. To Exit (X) or Quit (Q) ");
					 	  System.out.println("2. Strings to Process ");
					 	  System.out.println("Select any option: ");
					 	  String option = sc.next();
					 	  if(option.equals("1")||option.equals("X")||option.equals("Q")||option.equals("x")||option.equals("q"))
					 	  {
					 		  System.out.println("Thank You. Closing your Connections.");
					 		 
					 		  send = "Q " +option+ '\n';
					 		  byte[] send_O = send.getBytes();
					 		  byte[] encrytp_O = tea.encrypt(send_O);
					 		  
					 		  dataToRelayServer.write(encrytp_O); /* sending data to server*/
					 	  }
					 	  else
					 	  {
					 		  System.out.println("Enter the no of Strings.");
					 		  int n= sc.nextInt();
					 		  String receiverString2 = "R";
					 		  for(int i= 0;i< n;i++)
					 		  {
					 			  System.out.println("Enter String "+(i+1)+":");
					 			  String rword =sc.next();
					 			  receiverString2 = receiverString2 +" "+rword;
					 		  }
					 		  receiverString2 = receiverString2 + '\n';
							  byte[] send_O_2 = receiverString2.getBytes();
					 		  byte[] encrytp_O_2 = tea.encrypt(send_O_2);
					 		 
							  dataToRelayServer.write(encrytp_O_2);  /* sending data to server*/					
					 	  }
					 	  break;
				 case 'X': try 
		   	    			{
						 	dataFromRelayServer.close();
						 	dataToRelayServer.close();
		   					relayServerSocket.close();
		   					System.out.println("Connection Disconnected with Server");
		   	    			} 
				 			catch (IOException e) 
		   	    			{
				 				System.out.println("Errors in disconnecting Out/in-put Streams and Socket");
		   	    			}
			   				break;
				 default :System.out.println("Something is Wrong/ Message is Corrupted");
			  			  relayServerSocket.close();	
			  			  break;
				 }//switch	
				 
			 } //if 
			 
			 
		 	} //while
		 } //try
		 catch(IOException e) { System.out.println("sender Closed Connection");}
		 finally /* closing all buffers and socket */
		 { 
			 try 
	    		{
				 	sc.close();
				 	dataFromRelayServer.close();
				 	dataToRelayServer.close();
					relayServerSocket.close();
				} catch (IOException e) 
	    			{ System.out.println("Sender: FINALLY"); }
		 } //FINALLY
	 }// main of Sender	  
} //class sender

