import java.io.BufferedReader;
import java.io.DataOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.Socket;
import java.util.Scanner;

//Sumanjali Tirunagaru
public class TirunagaruP1Sender {
	public static void main(String args[]) throws Exception
	    {
		 String msgFromRelayServer = null; //to send data to Relay Server
		 String userName = null;
		 
		 //IP-Address and port
		 String ipAddress = args[0]; // "LOCALHOST"; //args[0]
		 int port = Integer.parseInt(args[1]); //17739; //args[1]

		 Socket relayServerSocket = null;
		 BufferedReader dataFromRelayServer = null;
		 DataOutputStream dataToRelayServer = null;
		 BufferedReader fromConsole = null;
		 Scanner sc = new Scanner(System.in);
		 try
		 {
		 fromConsole = new BufferedReader(new InputStreamReader(System.in)); /* used for taking data inFromUser */
		 relayServerSocket = new Socket(ipAddress, port); /* specifying the Relayserver port and IP address */
		 dataToRelayServer = new DataOutputStream(relayServerSocket.getOutputStream());  /* used for sending data To Server */
		 dataFromRelayServer = new BufferedReader(new InputStreamReader(relayServerSocket.getInputStream())); /* used for hold data from Server */
		
		 while ((msgFromRelayServer = dataFromRelayServer.readLine())!=null ) /*  data from server*/
		 {
			 switch(msgFromRelayServer.charAt(0))
			 {
				 case '0':System.out.println("Connection with Relay Server is Successful");
				 		  System.out.println("Please Enter your Username: ");
				 		  userName =fromConsole.readLine();
				 		  dataToRelayServer.writeBytes("A "+userName+'\n'); /* sending data to server*/
				 		  break;
				
				 case 'A':System.out.println("Congrats. Username Exists.");
					 	  System.out.println("Please Enter Your Password for "+userName+ ":");
					 	  String password =fromConsole.readLine();
					 	  dataToRelayServer.writeBytes("B "+password+'\n');	 /* sending data to server*/
					 	  break;
					 
				 case 'B':System.out.println("Username Does Not Exists with Relay Server");
					 	  System.out.println("Please Enter your Correct Username: ");
					 	  userName =fromConsole.readLine();
  						  dataToRelayServer.writeBytes("A "+userName+'\n'); /* sending data to server*/
						  break;
						 
				 case 'C':System.out.println("User is Authenticated");
					 	  System.out.println("Please Enter the Reciever Name you need to Connect With: ");
					 	  String receiverName =fromConsole.readLine();
					 	  dataToRelayServer.writeBytes("C "+receiverName+'\n'); /* sending data to server*/
					 	  break;
				 
				 case 'D':System.out.println("Password You Entered for "+userName+" is Incorrect");
						  System.out.println("Please Enter your Correct password: ");
						  password =fromConsole.readLine();
						  dataToRelayServer.writeBytes("B " +password+ '\n'); /* sending data to server*/
						  break;
					
				 case 'E':System.out.println("Receiver Name is correct");
					 	  System.out.println("Establishing Connection");
					 	  break;
				 
				 case 'F':System.out.println("Receiver Name Incorrect");
				  		  System.out.println("Please Enter Correct Receiver Name to connect with :");
				  		  receiverName = fromConsole.readLine();
				  		  dataToRelayServer.writeBytes("C " +receiverName+ '\n');	 /* sending data to server*/
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
						  dataToRelayServer.writeBytes(receiverString+ '\n'); /* sending data to server*/
						  break;
				 
				 case 'H':System.out.println(msgFromRelayServer);
					 	  break;
				
				 case 'O':System.out.println("Output From the RelayServer:");
					 	  String subString = msgFromRelayServer.substring(2);
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
					 		  dataToRelayServer.writeBytes("Q " +option+ '\n'); /* sending data to server*/
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
		   				  dataToRelayServer.writeBytes(receiverString2+ '\n'); /* sending data to server*/
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
				 default :System.out.println("Something is Wrong");
			  			  relayServerSocket.close();	
			  			  break;
				 }//switch					 
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

