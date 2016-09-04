import java.io.BufferedReader;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.InetAddress;
import java.net.ServerSocket;
import java.net.Socket;
import java.net.UnknownHostException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.StringTokenizer;


public class FTPClient {
	/* Line separator */
	public static final String EOL = System.getProperty("line.separator");
	/*CRLF String*/
	public static final String CRLF = "\r\n";
	/* Array containing all correct command names */
	public static final ArrayList<String> COMMANDS = new ArrayList<String>(
			Arrays.asList("CONNECT","GET","QUIT"));
	/* Array containing all incorrect command names ending without required CRLF */
	public static final ArrayList<String> ERRCOMMANDS = new ArrayList<String>(
			Arrays.asList("QUIT\r\n","QUIT\r","QUIT\n"));
	private static int fileNum = 1;
	/* type of error */
	public static String errType = "";
	/* successful message*/
	public static String sucType = "";
	/* Validity info of input string */
	static Boolean validInput = false;
	/* Order Array*/
	public static ArrayList<String> orderArr = new ArrayList<String>();
	/*Keep track of number of commands*/
	private static boolean CONNECT = true;
	/*spaces at end of each input line*/
	public static String endSpace = "";
	/*server host for connect*/
	private static String serverHost = "";
	/*server port for connect*/
	private static String serverPort = "";
	/*server port number */
	private static int serverPortNum = 0;
	/*path name for get */
	private static String pathName = "";
	/*connection port number*/
	private static int portNum = 8000;
	/*host IP address*/
	private static String myIP = "";
	/*host address object*/
	private static InetAddress myInet;
	/*path name*/
	private static String fullPath;
	/*input from server*/
	private static String modifiedSentence = "";
	/*client socket*/
	private static Socket clientSocket;
	/*client output*/
	private static DataOutputStream outToServer;
	/*client input*/
	private static DataInputStream inFromServerData;
	/*client input stream*/
	private static BufferedReader inFromServer;
	/*client welcoming socket*/
	private static ServerSocket clientWelcomingSocket;
	/*client data socket*/
	private static Socket dataSocket;
	/*read in data*/
	private static String fileData;
	/*reply code*/
	private static int replyCode = 0;
	/*reply text*/
	private static String replyText = "";
	/*reply to Server*/
	private static String replyMessage = "";
	/*reply to Server*/
	private static String dataSentence = "";
	/*quit Array*/
	private static ArrayList<String> quitArr = new ArrayList<String>();

	public static void main(String[] args) throws IOException {
		
		if(args.length != 1) {
			System.out.println("Invalid args!");
			System.exit(0);
		}
		
		try {
			 portNum = Integer.parseInt(args[0]);
		} catch(Exception e) {
			System.out.println("args have to be int type!");
			System.exit(0);
		}
		
		if(portNum < 0 || portNum > 65535) {
			System.out.println("Invalid port number range!");
			System.exit(0);
		}
		
		BufferedReader reader = new BufferedReader(new InputStreamReader(
				System.in));
		String inputLine = "";
		while ((inputLine = reader.readLine()) != null) {
			char[] checkSpace = inputLine.toCharArray();
			int count = checkSpace.length - 1;
			while(count> 0) {
				 char letter = checkSpace[count--];
				if(letter == ' ') endSpace += " ";
				else break;
			}
			if(inputLine.equals("")) break;
			processInput(inputLine,orderArr);
			endSpace = "";
		}
	}

	/*
	 * Parse and check an input string matches FTP client command format and print
	 * message
	 * 
	 * @param inputLine --- command strings to be checked
	 */
	private static void processInput(String inputLine, ArrayList<String> orderArr) throws UnknownHostException, IOException {
		/*
		 * If input command first character is space, update error type to
		 * command and print message
		 */
		/* else process the string and print message */
		if (inputLine.charAt(0) == ' ') {
			errType = "request";
			validInput = false;
		} else {
			if (validateInput(inputLine,orderArr))
				validInput = true;
			else 
				validInput = false;
		}
		System.out.println(inputLine);
		if (validInput) {
			if(sucType.equals("CONNECT")) {
				if(quitArr.size() > 0)  {
					outToServer.writeBytes("QUITSP\r\n");
//					System.out.println("passClient");
					int value = 0;
					char token;
					modifiedSentence = "";
					while(!inFromServer.ready()) {
						
					}
					try {
						Thread.sleep(1200);
					} catch (InterruptedException e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
					}
					while (inFromServer.ready()) {
						value = inFromServer.read();
						token = (char) value;
						modifiedSentence += token;
						if (token == '\r') {
							token = (char) inFromServer.read();
							if (token == '\n') {
								modifiedSentence += '\n';
								replyMessage = processInput(modifiedSentence);
							} else if (token == '\r') {
								replyMessage = processInput(modifiedSentence);
								
								replyMessage = processInput("" + token);
								modifiedSentence = "";
							} else {
								replyMessage = processInput(modifiedSentence);
								modifiedSentence = "" + token;
							}
						} else if (token == '\n') {
							replyMessage = processInput(modifiedSentence);
							modifiedSentence = "";
						} else {

						}
					}
					
					if(modifiedSentence.equals("221 Goodbye2\r\n")) {
						
						orderArr.clear();
						quitArr.clear();
						
					} 
//					else {
//						System.out.print(replyMessage);
//					}
				}
				if(clientSocket != null) {
					clientSocket.close();
				}
				try {
					clientSocket = new Socket(serverHost,serverPortNum);
				}  catch (Exception e) {
					System.out.print("CONNECT failed" + EOL);
					orderArr.clear();
					return;
				}
//				System.out.println("pass");
				outToServer = new DataOutputStream(clientSocket.getOutputStream());
				inFromServerData = new DataInputStream(clientSocket.getInputStream());
				inFromServer = new BufferedReader(new InputStreamReader(clientSocket.getInputStream()));
				int attempts = 0;
		        while(inFromServerData.available() == 0 && attempts < 10)
		        {
		            attempts++;
//		            System.out.println("pass2");
		            try {
						Thread.sleep(10);
					} catch (InterruptedException e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
					}
		        }
		        int value = 0;
				char token ;
				modifiedSentence = "";
				while (inFromServer.ready()) {
					value = inFromServer.read();
					token = (char) value;
					modifiedSentence += token;
					if (token == '\r') {
						token = (char) inFromServer.read();
						if (token == '\n') {
							modifiedSentence += '\n';
							replyMessage = processInput(modifiedSentence);
						} else if (token == '\r') {
							replyMessage = processInput(modifiedSentence);
							
							replyMessage = processInput("" + token);
							modifiedSentence = "";
						} else {
							replyMessage = processInput(modifiedSentence);
							modifiedSentence = "" + token;
						}
					} else if (token == '\n') {
						replyMessage = processInput(modifiedSentence);
						modifiedSentence = "";
					} else {

					}
				}
				if(modifiedSentence != "") {
//					System.out.println("pass2");
					if(modifiedSentence.equals("220 COMP 431 FTP server ready.\r\n")) {
						System.out.print("CONNECT accepted for FTP server at host " + serverHost +  " and port " + serverPort + EOL);
						System.out.print(replyMessage);
						System.out.print("USER anonymous\r\n");
						outToServer.writeBytes("USER anonymous\r\n");
						outToServer.flush();
						quitArr.add("QUIT");
						
						 attempts = 0;
				        while(inFromServerData.available() == 0 && attempts < 10)
				        {
				            attempts++;
//				            System.out.println("pass2");
				            try {
								Thread.sleep(1000);
							} catch (InterruptedException e) {
								// TODO Auto-generated catch block
								e.printStackTrace();
							}
				        }
//				        inFromServer = new BufferedReader(new InputStreamReader(clientSocket.getInputStream()));
				        value = 0;
						modifiedSentence = "";
						while (inFromServer.ready()) {
							value = inFromServer.read();
							token = (char) value;
							modifiedSentence += token;
							if (token == '\r') {
								token = (char) inFromServer.read();
								if (token == '\n') {
									modifiedSentence += '\n';
									replyMessage = processInput(modifiedSentence);
								} else if (token == '\r') {
									replyMessage = processInput(modifiedSentence);
									
									replyMessage = processInput("" + token);
									modifiedSentence = "";
								} else {
									replyMessage = processInput(modifiedSentence);
									modifiedSentence = "" + token;
								}
							} else if (token == '\n') {
								replyMessage = processInput(modifiedSentence);
								modifiedSentence = "";
							} else {

							}
						}
						if(modifiedSentence != "") {
//							System.out.println("guest");
							if(modifiedSentence.equals("331 Guest access OK, send password.\r\n")) {
								System.out.print(replyMessage);
								System.out.print("PASS guest@\r\n");
//								outToServer.flush();
								outToServer.writeBytes("PASS guest@\r\n");
								while (!inFromServer.ready()) {
									
								}
								try {
									Thread.sleep(1200);
								} catch (InterruptedException e) {
									// TODO Auto-generated catch block
									e.printStackTrace();
								}
								value = 0;
								modifiedSentence = "";
								while (inFromServer.ready()) {
									value = inFromServer.read();
									token = (char) value;
									modifiedSentence += token;
									if (token == '\r') {
										token = (char) inFromServer.read();
										if (token == '\n') {
											modifiedSentence += '\n';
											replyMessage = processInput(modifiedSentence);
										} else if (token == '\r') {
											replyMessage = processInput(modifiedSentence);
											
											replyMessage = processInput("" + token);
											modifiedSentence = "";
										} else {
											replyMessage = processInput(modifiedSentence);
											modifiedSentence = "" + token;
										}
									} else if (token == '\n') {
										replyMessage = processInput(modifiedSentence);
										modifiedSentence = "";
									} else {

									}
								}
								if(modifiedSentence != "") {
									if(modifiedSentence.equals("230 Guest login OK.\r\n")) {
										System.out.print(replyMessage);
										System.out.print("SYST\r\n");
										outToServer.writeBytes("SYST\r\n");
										while (!inFromServer.ready()) {
											
										}
										try {
											Thread.sleep(1200);
										} catch (InterruptedException e) {
											// TODO Auto-generated catch block
											e.printStackTrace();
										}
										
										value = 0;
										modifiedSentence = "";
										while (inFromServer.ready()) {
											value = inFromServer.read();
											token = (char) value;
											modifiedSentence += token;
											if (token == '\r') {
												token = (char) inFromServer.read();
												if (token == '\n') {
													modifiedSentence += '\n';
													replyMessage = processInput(modifiedSentence);
//													System.out.print(replyMessage + 1);
												} else if (token == '\r') {
													replyMessage = processInput(modifiedSentence);
													
													replyMessage = processInput("" + token);
													modifiedSentence = "";
												} else {
													replyMessage = processInput(modifiedSentence);
													modifiedSentence = "" + token;
												}
											} else if (token == '\n') {
												replyMessage = processInput(modifiedSentence);
												modifiedSentence = "";
											} else {

											}
										}
										System.out.print(replyMessage);
										
										
										System.out.print("TYPE I\r\n");
										outToServer.writeBytes("TYPE I\r\n");
										while (!inFromServer.ready()) {
											
										}	
										try {
											Thread.sleep(1200);
										} catch (InterruptedException e) {
											// TODO Auto-generated catch block
											e.printStackTrace();
										}
//										while (!inFromServer.ready()) {
//											
//										}
										value = 0;
										modifiedSentence = "";
										while (inFromServer.ready()) {
											value = inFromServer.read();
											token = (char) value;
											modifiedSentence += token;
											if (token == '\r') {
												token = (char) inFromServer.read();
												if (token == '\n') {
													modifiedSentence += '\n';
													replyMessage = processInput(modifiedSentence);
												} else if (token == '\r') {
													replyMessage = processInput(modifiedSentence);
													
													replyMessage = processInput("" + token);
													modifiedSentence = "";
												} else {
													replyMessage = processInput(modifiedSentence);
													modifiedSentence = "" + token;
												}
											} else if (token == '\n') {
												replyMessage = processInput(modifiedSentence);
												modifiedSentence = "";
											} else {

											}
										}
										if(modifiedSentence != "") {
											System.out.print(replyMessage);
										}
									} else {
										System.out.print(replyMessage);
									}
								}
								
							} else {
								System.out.print(replyMessage);
							}
						
						} 	
					}
				} else {
					System.out.print("CONNECT failed" + EOL);
				}
				
//				outToServer.writeBytes();
//				System.out.print("CONNECT accepted for FTP server at host " + serverHost +  " and port " + serverPort + "\r\n");
//				System.out.printf("USER anonymous%s",CRLF);
//				System.out.printf("PASS guest@%s",CRLF); 
//				System.out.printf("SYST%s",CRLF);
//				System.out.printf("TYPE I%s",CRLF);  
			} else if (sucType.equals("GET")) {
				System.out.print("GET accepted for " + fullPath + EOL);
//				outToServer.writeBytes("GET accepted for " + fullPath + "\n");
				try {
					myInet = InetAddress.getLocalHost();
				} catch (UnknownHostException e) {
					
				}
				/* get ip address and parse it into port number.*/
				myIP = myInet.getHostAddress();
				myIP = myIP.replace('.', ',');
				int firstNum = portNum / 256;
				int secondNum = portNum % 256;
				
				if(firstNum < 0 || firstNum > 255) {
					System.out.println("ERROR -- PORT NUMBER");
					return;
				}
				
				if(secondNum < 0 || secondNum > 255) {
					System.out.println("ERROR -- PORT NUMBER");
					return;
				}
				if(clientWelcomingSocket != null) clientWelcomingSocket = null;  //changed
				try {
					clientWelcomingSocket = new ServerSocket(portNum);
				} catch(Exception e) {
					System.out.print("GET failed, FTP-data port not allocated." + EOL);
				}
				
				if(clientWelcomingSocket != null) {
					
					
//					System.out.print(inFromServer.readLine());
					outToServer.writeBytes("PORT " + myIP + "," + firstNum + "," + secondNum + CRLF);
					System.out.print("PORT " + myIP + "," + firstNum + "," + secondNum + CRLF);
//					outToServer.flush();
					portNum++;
					while(!inFromServer.ready()) {
						
					}
					try {
						Thread.sleep(1200);
					} catch (InterruptedException e) {	
						// TODO Auto-generated catch block
						e.printStackTrace();
					}
					while(!inFromServer.ready()) {
						
					}
					int value = 0;
					char token;
					modifiedSentence = "";
					while (inFromServer.ready()) {
						value = inFromServer.read();
						token = (char) value;
						modifiedSentence += token;
//						System.out.println(token);
						if (token == '\r') {
							token = (char) inFromServer.read();
							if (token == '\n') {
								modifiedSentence += '\n';
								replyMessage = processInput(modifiedSentence);
							} else if (token == '\r') {
								replyMessage = processInput(modifiedSentence);
								
								replyMessage = processInput("" + token);
								modifiedSentence = "";
							} else {
								replyMessage = processInput(modifiedSentence);
								modifiedSentence = "" + token;
							}
						} else if (token == '\n') {
							replyMessage = processInput(modifiedSentence);
							modifiedSentence = "";
						} else {

						}
					}
					
					myIP = myIP.replace(',', '.');
					if(modifiedSentence.equals("200 Port command successful " + "(" + myIP + "," + (portNum - 1) + ")." + CRLF)) {
						System.out.print(replyMessage);
						outToServer.flush();
						
						outToServer.writeBytes("RETR "+ fullPath + CRLF);
						System.out.print("RETR "+ fullPath + CRLF);
						clientWelcomingSocket.setSoTimeout(6000);
						try {
							dataSocket = clientWelcomingSocket.accept();
						} catch(Exception e) {
						}
						
						if(dataSocket != null) {
//							if((modifiedSentence = inFromServer.readLine()) != null) 
//								System.out.print(modifiedSentence);
//							System.out.print("1");
//							System.out.println("passed");
							if(!dataSocket.isClosed()) {
								BufferedReader dataInFromServer = new BufferedReader(new InputStreamReader(dataSocket.getInputStream()));
								
								
								value = 0;
								dataSentence = "";
								while (dataInFromServer.ready()) {
									value = dataInFromServer.read();
									token = (char) value;
									fileData += token;
								}
								
								
								File dest = new File("retr_files/file" + fileNum);
								FileOutputStream fout = new FileOutputStream(dest);
//								System.out.print("2");
								byte[] contentInBytes = fileData.getBytes();

								fout.write(contentInBytes);
								fout.flush();
								fout.close();
								fileNum++;
								dataSocket.close();
							}
							
//							while ((modifiedSentence = inFromServer.readLine()) != null) 
//								System.out.print(modifiedSentence);
						} 
//						outToServer.flush();
//						System.out.print("retr sent!");
						while(!inFromServer.ready()) {
							
						}
						try {
							Thread.sleep(1200);
						} catch (InterruptedException e) {
							// TODO Auto-generated catch block
							e.printStackTrace();
						}
						
						value = 0;
						modifiedSentence = "";
						String totalSentence = "";
						replyMessage = "";
						while (inFromServer.ready()) {
							value = inFromServer.read();
							token = (char) value;
							modifiedSentence += token;
							if (token == '\r') {
								token = (char) inFromServer.read();
								if (token == '\n') {
									modifiedSentence += '\n';
									replyMessage += processInput(modifiedSentence);
									totalSentence = modifiedSentence;
									modifiedSentence = "";
								} else if (token == '\r') {
									replyMessage = processInput(modifiedSentence);
									
									replyMessage = processInput("" + token);
									modifiedSentence = "";
								} else {
									replyMessage = processInput(modifiedSentence);
									modifiedSentence = "" + token;
								}
							} else if (token == '\n') {
								replyMessage = processInput(modifiedSentence);
								modifiedSentence = "";
							} else {

							}
						}
						System.out.print(replyMessage);
					} else {
						System.out.print(replyMessage);
					}
					
				} 
				
//				System.out.printf("PORT " + myIP + "," + firstNum + "," + secondNum + "%s",CRLF);
//		        System.out.printf("RETR "+ fullPath + "%s",CRLF);
			} else if(sucType.equals("QUIT")) {
				System.out.print("QUIT accepted, terminating FTP client" + EOL);
				System.out.printf("QUIT%s",CRLF);
				outToServer.writeBytes("QUIT\r\n");
				int value = 0;
				char token;
				modifiedSentence = "";
				while(!inFromServer.ready()) {
					
				}
				try {
					Thread.sleep(1200);
				} catch (InterruptedException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
				while (inFromServer.ready()) {
					value = inFromServer.read();
					token = (char) value;
					modifiedSentence += token;
					if (token == '\r') {
						token = (char) inFromServer.read();
						if (token == '\n') {
							modifiedSentence += '\n';
							replyMessage = processInput(modifiedSentence);
						} else if (token == '\r') {
							replyMessage = processInput(modifiedSentence);
							
							replyMessage = processInput("" + token);
							modifiedSentence = "";
						} else {
							replyMessage = processInput(modifiedSentence);
							modifiedSentence = "" + token;
						}
					} else if (token == '\n') {
						replyMessage = processInput(modifiedSentence);
						modifiedSentence = "";
					} else {

					}
				}
				
				if(modifiedSentence.equals("221 Goodbye\r\n")) {
					System.out.printf("QUIT%s",CRLF);
					System.out.print(replyMessage);
					orderArr.clear();
					quitArr.clear();
					if(clientSocket != null) clientSocket.close();
					System.exit(0);
				} else {
					System.out.print(replyMessage);
				}
			}
		}	else {
			System.out.print("ERROR -- " + errType + EOL);
		}
			
	}

	/*
	 * break string into tokens and specially check the validity of command token
	 * @param inputLine --- command strings to be checked
	 * @return boolean for validity of input line
	 */
	private static boolean validateInput(String inputLine, ArrayList<String> orderArr) {
		String commandToken = "";
		/*
		 * if command token is valid go ahead to check the rest of input line
		 * else set error type to command
		 */
		if(inputLine.length() > 3) {
			String getSpecial = inputLine.substring(0, 3);
			if(getSpecial.toUpperCase().equals("GET")) {
				int fullPathNum = 3;
				while(fullPathNum < inputLine.length() && inputLine.charAt(fullPathNum) == ' ' )
					fullPathNum++;
				fullPath = inputLine.substring(fullPathNum);
			}
		}
		StringTokenizer tokenizedLine = new StringTokenizer(inputLine, " ");
		if (tokenizedLine.hasMoreElements()) {
			commandToken = tokenizedLine.nextToken();
			if (ERRCOMMANDS.contains(commandToken.toUpperCase())) {
				errType = "request";
				return false;
			}
			if (COMMANDS.contains(commandToken.toUpperCase())) {
				if (validateParameter(tokenizedLine, commandToken, orderArr)) {
					return true;
				} else {
					return false;
				}
			} else {
				errType = "request";
				return false;
			}
		} else 
			errType = "request";
		return false;

	}

	/*
	 * parse and validate parameter 
	 * @param tokenizedLine rest of input line commandToken command token
	 * 
	 * @return boolean value for validity of parameter
	 */
	private static boolean validateParameter(
			StringTokenizer tokenizedLine, String commandToken, ArrayList<String> orderArr) {

		String sumToken = "";
		String hostToken = "";
		int sumCount = 0;
		/* if command token is CONNECT check rest of string */

		if (commandToken.toUpperCase().equals("CONNECT")) {
			if(tokenizedLine.hasMoreTokens()) {
				serverHost = tokenizedLine.nextToken();
				if(serverHost.charAt(0) != '.' && !Character.isDigit(serverHost.charAt(0)) && serverHost.charAt(serverHost.length() - 1) != '.') {
//					if(!serverHost.contains(".")) {
//						errType = "server-host";
//						return false;
//					}
					
					for(int i = 0; i < serverHost.length(); i++) {
						char element = serverHost.charAt(i);
						if(i == serverHost.length() - 1) {
							if(hostToken.length() < 2) {
								errType = "server-host";
								return false;
							}
						}
						if(element == '.') {
							if(hostToken.length() < 2) {
								errType = "server-host";
								return false;
							}
							hostToken = "";
							if(serverHost.charAt(i + 1) == '.') {
								errType = "server-host";
								return false;
							}
							continue;
						}
						if(!Character.isLetter(element) && !Character.isDigit(element)) {
							errType = "server-host";
							return false;
						} 
						hostToken += element;
						
					}
					
				} else {
					errType = "server-host";
					return false;
				}
			} else {
				errType = "request";
				return false;
			}
			
			if(tokenizedLine.hasMoreTokens()) {
				serverPort = tokenizedLine.nextToken();
				if(isNumeric(serverPort)) {
					serverPortNum = Integer.parseInt(serverPort);
					if(serverPortNum >= 0 && serverPortNum <= 65535) {
					} else {
						errType = "server-port";
						return false;
					}
				} else {
					errType = "server-port";
					return false;
				}
			} else {
				errType = "server-host";
				return false;
			}
			if(tokenizedLine.hasMoreTokens() || endSpace.length() != 0) {
				errType = "server-port";
				return false;
			} else {
				if(orderArr.size() == 0) {
					orderArr.add("CONNECT");
				}
					
				sucType = "CONNECT";
				return true;
			}
			
			
			/* command name is GET case */
		} else if (commandToken.toUpperCase().equals("GET")) {
			while (tokenizedLine.hasMoreTokens()) {
				sumToken += tokenizedLine.nextToken();
				sumCount++;
			}
			if(sumCount == 0) {
				errType = "pathname";
				return false;
			}
			for (int i = 0; i < sumToken.length(); i++) {
				int ascVal =  sumToken.charAt(i);
				if(ascVal < 0 || ascVal > 127) {
					errType = "pathname";
					return false;
				}
			}
			if(orderArr.size() == 0) {
				errType = "expecting CONNECT";
				return false;
			}
			pathName = sumToken;
			sucType = "GET";
			return true;
			/* command name is QUIT case */
		} else if (commandToken.toUpperCase().equals("QUIT")) {
			while (tokenizedLine.hasMoreTokens()) {
				sumToken += tokenizedLine.nextToken();
			}
			if(endSpace.length() != 0) {
				errType = "request";
				return false;
			}
			if (sumToken.length() == 0) {
				if(orderArr.size() == 0) {
					errType = "expecting CONNECT";
					return false;
				}
				sucType = "QUIT";
				return true;
			} else {
				errType = "request";
				return false;
			}
		} else {
			errType = "request";
			return false;
		}
		

	}
	
	/* Check if input string is numeric
	 * 
	 * @param inputData input to be checked
	 */
	public static boolean isNumeric(String inputData) {
		char[] ch = inputData.toCharArray();
		for(char element: ch) {
			if(!Character.isDigit(element)) {
				return false;
			}
		}
		return true;
	}
	
	
	private static String processInput(String inputLine) {
		/*
		 * If input command first character is space, update error type to
		 * command and print message
		 */
		/* else process the string and print message */
		
		if (inputLine.charAt(0) == ' ') {
			errType = "reply-code";
			validInput = false;
		} else {
			if (validateInput(inputLine))
				validInput = true;
			else
				validInput = false;
		}
		
		if (validInput)
			return "FTP reply " + replyCode + " accepted. Text is: " + replyText + EOL;
		else
			return "ERROR -- " + errType + EOL;
	}

	/*
	 * break string into tokens and specially check the validity of command
	 * token
	 * 
	 * @param inputLine --- command strings to be checked
	 * 
	 * @return boolean for validity of input line
	 */
	private static boolean validateInput(String inputLine) {

		//if inputLine length is smaller than 7 investigate error types.
		if(inputLine.length() < 7) {
			if(inputLine.length() <= 4) {
				errType = "reply-code";
				return false;
			} else {
				if(isNumeric(inputLine.substring(0, 3))) {
					int replyCodeNum = Integer.parseInt(inputLine.substring(0, 3));
					if(replyCodeNum >= 100 && replyCodeNum <= 599) {
						if(inputLine.charAt(3) != ' ') {
							errType = "reply-code";
							return false;
						} else {
							errType = "reply-text";
							return false;
						}
					} else {
						errType = "reply-code";
						return false;
					}
				} else {
					errType = "reply-code";
					return false;
				}
			}
			//if inputLine length is greater than or equal to 7 pares reply message.
		} else {
			if(isNumeric(inputLine.substring(0, 3))) {
				int replyCodeNum = Integer.parseInt(inputLine.substring(0, 3));
				if(replyCodeNum >= 100 && replyCodeNum <= 599) {
					if(inputLine.charAt(3) != ' ') {
						errType = "reply-code";
						return false;
					} else {
						replyCode = replyCodeNum;
						String replyTextAndCRLF = inputLine.substring(4);
						String checkCRLF = replyTextAndCRLF.substring(replyTextAndCRLF.length() - 2);
						String checkReplyText = replyTextAndCRLF.substring(0,replyTextAndCRLF.length() - 2);
						for (int i = 0; i < checkReplyText.length(); i++) {
							int ascVal =  checkReplyText.charAt(i);
							if(ascVal < 0 || ascVal > 127) {
								errType = "reply-text";
								return false;
							}		
						}
						if(checkCRLF.matches("\\r\\n")) {
							replyText = checkReplyText;
							return true;
						} else {
							errType = "<CRLF>";
							return false;
						}	
					}
				} else {
					errType = "reply-code";
					return false;
				}
			} else {
				errType = "reply-code";
				return false;
			}
		}
		

	}
	
	
}
