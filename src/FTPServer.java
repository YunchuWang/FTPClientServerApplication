import java.io.*;
import java.net.*;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.StringTokenizer;
public class FTPServer {
	/* Line separator */
	public static final String EOL = System.getProperty("line.separator");
	/*port Number used to set up TCP connections*/
	private static int acceptPortNum = 0;
    /*data from client*/
	private static String clientData = "";
	/*CRLF String*/
	public static final String CRLF = "\r\n";
	/* Array containing all correct command names */
	public static final ArrayList<String> COMMANDS = new ArrayList<String>(
			Arrays.asList("USER", "PASS", "TYPE", "PORT", "RETR", "SYST\r\n",
					"NOOP\r\n", "QUIT\r\n"));
	/* Array containing all incorrect command names ending without required CRLF */
	public static final ArrayList<String> ERRCOMMANDS = new ArrayList<String>(
			Arrays.asList("SYST","NOOP","QUIT","SYST\r","NOOP\r","QUIT\r","SYST\n","NOOP\n","QUIT\n"));
	/* correct number of characters for parameter and CRLF of TYPE command */
	public static final int CORRECTNUMOFPARAMANDCRLF = 3;
	/* incorrect number of characters for parameter and CRLF for other commands */
	public static final int INCORRNUMPARAMANDCRLF = 2;
	/* type of error */
	public static String errType = "";
	/* successful message*/
	public static String sucType = "";
	/* Validity info of input string */
	static Boolean validInput = false;
	/* File number*/
	static int fileNum = 1;
	/* Order Array*/
	public static ArrayList<String> orderArr = new ArrayList<String>();
	/*Server welcoming socket*/
	private static ServerSocket welcomingSocket;
	/*Server connection socket*/
	private static Socket connectionSocket;
	/*Server output*/
	private static DataOutputStream outToClient;
	/*client input stream*/
	private static BufferedReader inFromClient;
	/*client input stream data*/
	private static DataInputStream inFromClientData;
	/* reply message*/
	private static String replyMessage = "";
	/* an array storing port number */
	private static String[] portNumArr = {"0","0","0","0","0","0"};
	/* FTP data connection*/
	private static Socket dataConnection;
	/*server host for connect*/
	private static String serverHost = "";
	/*server port for connect*/
	private static String serverPort = "";
	/*server port number */
	private static int serverPortNum = 0;
	/*quit array */
	public static ArrayList<String> quitArr = new ArrayList<String>();
		
	public static void main(String[] args) {
		if(args.length != 1) {
			System.out.println("Invalid args!");
			System.exit(0);
		}
		
		try {
			 acceptPortNum = Integer.parseInt(args[0]);
		} catch(Exception e) {
			System.out.println("args have to be int type!");
			System.exit(0);
		}
		
		if(acceptPortNum < 0 || acceptPortNum > 65535) {
			System.out.println("Invalid port number range!");
			System.exit(0);
		}
		
		while(true) {
			try {
				welcomingSocket = new ServerSocket(acceptPortNum);
			} catch (IOException e) {
				System.out.println("Port number unavaliable!");
				System.exit(0);
			}
			try {
				connectionSocket = welcomingSocket.accept();
				
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
//			System.out.println("accepted!");
			if(connectionSocket != null) {
				try {
					inFromClient = new BufferedReader(new InputStreamReader(connectionSocket.getInputStream()));
					inFromClientData = new DataInputStream(connectionSocket.getInputStream());
				} catch (IOException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
				try {
					outToClient = new DataOutputStream(connectionSocket.getOutputStream());
				} catch (IOException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
				try {
					outToClient.writeBytes("220 COMP 431 FTP server ready.\r\n");
					connectionSocket.getOutputStream().flush();
				} catch (IOException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
				System.out.print("220 COMP 431 FTP server ready." + "\r\n");
				try {
					while(!inFromClient.ready()) {
					}
					int attempts = 0;
			        while(attempts < 10)
			        {
			            attempts++;
//			            System.out.println("pass2");
			            try {
							Thread.sleep(30);
						} catch (InterruptedException e) {
							// TODO Auto-generated catch block
							e.printStackTrace();
						}
			        }
					int value = 0;
					char inputToken;
					clientData = "";
					while (inFromClient.ready()) {
						value = inFromClient.read();
						inputToken = (char) value;
//						System.out.println(inputToken);
						clientData += inputToken;
						if (inputToken == '\r') {
							inputToken = (char) inFromClient.read();
							if (inputToken == '\n') {
								clientData += '\n';
								replyMessage = processInput(clientData,orderArr);
//								System.out.println("replied" + replyMessage);
//								value = inFromClient.read();
//								System.out.println(value);
//								clientData = "";
							} else if (inputToken == '\r') {
								replyMessage = processInput(clientData,orderArr);
								outToClient.writeBytes(replyMessage);
								outToClient.flush();
								replyMessage = processInput("" + inputToken,orderArr);
								clientData = "";
							} else {
								replyMessage = processInput(clientData,orderArr);
								clientData = "" + inputToken;
							}
						} else if (inputToken == '\n') {
							replyMessage = processInput(clientData,orderArr);
							clientData = "";
						} else {

						}
					}
					//
					outToClient.writeBytes(replyMessage);
//					System.out.println("sent!");
					connectionSocket.getOutputStream().flush();
//					System.out.println("replied" + replyMessage);
					while(!inFromClient.ready()) {
						
					}
					attempts = 0;
					while(attempts++ < 10) {
						try {
							Thread.sleep(120);
//							System.out.println("hi");
						} catch (InterruptedException e) {
							// TODO Auto-generated catch block
							e.printStackTrace();
						}
					}
					
					value = 0;
					clientData = "";
					replyMessage = "";
					while (inFromClient.ready()) {
						value = inFromClient.read();
						inputToken = (char) value;
						clientData += inputToken;
//						System.out.println(inputToken);
//						System.out.println(inFromClient.ready());
						if (inputToken == '\r') {
							inputToken = (char) inFromClient.read();
							if (inputToken == '\n') {
								clientData += '\n';
								replyMessage = processInput(clientData,orderArr);
							} else if (inputToken == '\r') {
								replyMessage = processInput(clientData,orderArr);
								outToClient.writeBytes(replyMessage);
								replyMessage = processInput("" + inputToken,orderArr);
								clientData = "";
							} else {
								replyMessage = processInput(clientData,orderArr);
								clientData = "" + inputToken;
							}
						} else if (inputToken == '\n') {
							replyMessage = processInput(clientData,orderArr);
							clientData = "";
						} else {

						}
					}
					
//					System.out.println("pass " + replyMessage);
					if(clientData != "") {
//						System.out.println("1");
//						System.out.print(replyMessage);
						if(replyMessage.equals("230 Guest login OK.\r\n")) {
							outToClient.writeBytes(replyMessage);
							outToClient.flush();
							while(true) {
								value = 0;
								clientData = "";
								replyMessage = "";
								attempts = 0;
								while(!inFromClient.ready()) {
									
								}
								while(attempts++ < 10) {
									try {
										Thread.sleep(120);
//										System.out.println("hi");
									} catch (InterruptedException e) {
										// TODO Auto-generated catch block
										e.printStackTrace();
									}
								}
								
								while (inFromClient.ready()) {
									
									value = inFromClient.read();
									inputToken = (char) value;
									clientData += inputToken;
									if (inputToken == '\r') {
										inputToken = (char) inFromClient.read();
										if (inputToken == '\n') {
											clientData += '\n';
											if(clientData.equals("QUITSP\r\n")) {
												replyMessage = "221 Goodbye2.\r\n";
												orderArr.clear();
												quitArr.clear();
//												System.out.println("pass");
												break;
											} else {
												replyMessage = processInput(clientData,orderArr);
												clientData = "";
											}
										} else if (inputToken == '\r') {
											replyMessage = processInput(clientData,orderArr);
											outToClient.writeBytes(replyMessage);
											replyMessage = processInput("" + inputToken,orderArr);
											clientData = "";
										} else {
											replyMessage = processInput(clientData,orderArr);
											clientData = "" + inputToken;
										}
									} else if (inputToken == '\n') {
										replyMessage = processInput(clientData,orderArr);
										clientData = "";
									} else {

									}
								}
								outToClient.writeBytes(replyMessage);
								outToClient.flush();
								try {
									Thread.sleep(1200);
//									System.out.println("hi");
								} catch (InterruptedException e) {
									// TODO Auto-generated catch block
									e.printStackTrace();
								}
								if(dataConnection != null) dataConnection.close();
								if(replyMessage.equals("221 Goodbye.\r\n") || replyMessage.equals("221 Goodbye2.\r\n")) {
									orderArr.clear();
									if(connectionSocket != null) {
										try {
											connectionSocket.close();
										} catch (IOException e) {
											// TODO Auto-generated catch block
											e.printStackTrace();
										}
									}
									break;
								}
									
								
							}	
						} else {
							outToClient.writeBytes(replyMessage);
							outToClient.flush();
						}
					}
					
				} catch (IOException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
				try {
					if(welcomingSocket != null) welcomingSocket.close();
				} catch (IOException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
			} else {
				System.out.println("Error");
			}
			
			
		}
	}
	
	/*
	 * Parse and check an input string matches FTP command format and print
	 * message
	 	* 
	 * @param inputLine --- command strings to be checked
	 */
	private static String processInput(String inputLine, ArrayList<String> orderArr) {
		/*
		 * If input command first character is space, update error type to
		 * command and print message
		 */
		/* else process the string and print message */
		
		if (inputLine.charAt(0) == ' ') {
			errType = "500 Syntax error, command unrecognized.";
			validInput = false;
		} else {
			if (validateInput(inputLine,orderArr))
				validInput = true;
			else
				validInput = false;
		}
		System.out.print(inputLine);
		if (validInput) {
			System.out.print(sucType + CRLF);
			return sucType + CRLF;
		} else {
			System.out.print(errType + CRLF);
			return errType + CRLF;
		}
			
	}

	/*
	 * break string into tokens and specially check the validity of command
	 * token
	 * 
	 * @param inputLine --- command strings to be checked
	 * 
	 * @return boolean for validity of input line
	 */
	private static boolean validateInput(String inputLine, ArrayList<String> orderArr) {

		String token = "";
		String commandToken = "";
		String parameterToken = "";

		/*
		 * if command token is valid go ahead to check the rest of input line
		 * else set error type to command
		 */
		StringTokenizer tokenizedLine = new StringTokenizer(inputLine, " ");
		if (tokenizedLine.hasMoreElements()) {
			commandToken = tokenizedLine.nextToken();
			if (ERRCOMMANDS.contains(commandToken.toUpperCase())) {
				errType = "501 Syntax error in parameter.";
				return false;
			}
			if (COMMANDS.contains(commandToken.toUpperCase())) {
				if (validateParameterAndCRLF(tokenizedLine, commandToken, orderArr)) {
					return true;
				} else {
					return false;
				}
			} else {
				errType = "500 Syntax error, command unrecognized.";
				return false;
			}
		}
		return false;

	}

	/*
	 * parse and validate parameter and CRLF components
	 * 
	 * @param tokenizedLine rest of input line commandToken command token
	 * 
	 * @return boolean value for validity of parameter and crlf
	 */
	private static boolean validateParameterAndCRLF(
			StringTokenizer tokenizedLine, String commandToken, ArrayList<String> orderArr) {

		String parameterToken = "";
		String sumToken = "";
		int sumTokenCount = 0;
		String crlfToken = "";
		int length = 0;

		/* if command token is TYPE check rest of string */

		if (commandToken.toUpperCase().equals("TYPE")) {
			while (tokenizedLine.hasMoreTokens()) {
				sumToken += tokenizedLine.nextToken();
				sumTokenCount++;
			}
			/*
			 * precondition: if space between parameter and \r\n token count
			 * should be greater than one
			 */
			if (sumTokenCount > 1) {
				errType = "501 Syntax error in parameter.";
				return false;
			}
			/* token number correct condition */
			if (sumToken.length() == CORRECTNUMOFPARAMANDCRLF) {
				if (sumToken.charAt(0) == 'A' || sumToken.charAt(0) == 'I') {
					crlfToken = sumToken.substring(1);
					if (crlfToken.matches("\\r\\n")) {
						if(orderArr.size() == 0) {
							errType = "530 Not logged in.";
							return false;
						} else if (orderArr.size() == 1) {
							errType = "503 Bad sequence of commands.";
							return false;
						} else {
							sucType = "200 Type set to " + sumToken.charAt(0) + ".";
							return true;
						}
					} else {
						errType = "501 Syntax error in parameter.";
						return false;
					}
				} else {
					errType = "501 Syntax error in parameter.";
					return false;
				}
				/* too few number condition */
			} else if (sumToken.length() <= 1) {
				errType = "501 Syntax error in parameter.";
				return false;
				/* too few number condition */
			} else if (sumToken.length() == INCORRNUMPARAMANDCRLF) {
				errType = "501 Syntax error in parameter.";
				return false;
			}
			/* too many number condition */
			else {
				crlfToken = sumToken.substring(sumToken.length()
						- INCORRNUMPARAMANDCRLF);
				errType = "501 Syntax error in parameter.";
				return false;
			}
			/* command name is USER or PASS case */
		} else if (commandToken.toUpperCase().equals("USER")) {
			while (tokenizedLine.hasMoreTokens()) {
				sumToken += tokenizedLine.nextToken();
			}
			if (sumToken.length() > INCORRNUMPARAMANDCRLF) {
				parameterToken = sumToken.substring(0, sumToken.length()
						- INCORRNUMPARAMANDCRLF);
				crlfToken = sumToken.substring(sumToken.length()
						- INCORRNUMPARAMANDCRLF);
				int charCount = parameterToken.length();
				boolean flag = true;
				for (int i = 0; i < charCount; i++) {
					int ascVal = parameterToken.charAt(i);
					if (ascVal < 0 || ascVal > 127)
						flag = false;
				}
				if (flag) {
					if (crlfToken.matches("\\r\\n")) {
						if(orderArr.size() == 0) {
							orderArr.add("USER");
							sucType = "331 Guest access OK, send password.";
							if(quitArr.size() == 0) quitArr.add("QUIT");
							return true;
						} else {
							errType = "503 Bad sequence of commands.";
							return false;
						}
					} else {
						errType = "501 Syntax error in parameter.";
						return false;
					}
				} else {
					errType = "501 Syntax error in parameter.";
					return false;
				}
			} else {
				errType = "501 Syntax error in parameter.";
				return false;
			}
		} else if (commandToken.toUpperCase().equals("PASS")) {
				while (tokenizedLine.hasMoreTokens()) {
					sumToken += tokenizedLine.nextToken();
				}
				if (sumToken.length() > INCORRNUMPARAMANDCRLF) {
					parameterToken = sumToken.substring(0, sumToken.length()
							- INCORRNUMPARAMANDCRLF);
					crlfToken = sumToken.substring(sumToken.length()
							- INCORRNUMPARAMANDCRLF);
					int charCount = parameterToken.length();
					boolean flag = true;
					for (int i = 0; i < charCount; i++) {
						int ascVal = parameterToken.charAt(i);
						if (ascVal < 0 || ascVal > 127)
							flag = false;
					}
					if (flag) {
						if (crlfToken.matches("\\r\\n")) {
							if(orderArr.size() == 1) {
								orderArr.add("PASS");
								sucType = "230 Guest login OK.";
								return true;
							} else if(orderArr.isEmpty()) {
								errType = "530 Not logged in.";
								return false;
							} else {
								errType = "503 Bad sequence of commands.";
								return false;
							}
						} else {
							errType = "501 Syntax error in parameter.";
							return false;
						}
					} else {
						errType = "501 Syntax error in parameter.";
						return false;
					}
				} else {
					errType = "501 Syntax error in parameter.";
					return false;
				}

		} else if (commandToken.toUpperCase().equals("PORT")) {
			while (tokenizedLine.hasMoreTokens()) {
				sumToken += tokenizedLine.nextToken();
				sumTokenCount++;
			}
			if(sumTokenCount != 1) {
				errType = "501 Syntax error in parameter.";
				return false;
			}
			StringTokenizer portline = new StringTokenizer(sumToken,",");
			int portNumCount = 0;
			portNumArr = new String[6];
			String portToken = "";
			while(portline.hasMoreTokens()) {
				 portToken = portline.nextToken();
				 if(portNumCount > 5) {
					 errType = "501 Syntax error in parameter.";
					 return false;
				 }
				 if(portNumCount == 5) {
					 if(portToken.length() < 3 || portToken.length() > 5) {
						 errType = "501 Syntax error in parameter.";
						 return false;
					 } else {
						 String crlf = portToken.substring(portToken.length() - 2);
						 if(crlf.matches("\\r\\n")) {
							 String lastNum = portToken.substring(0, portToken.length() - 2);
							 if(isNumeric(lastNum)) {
								 portNumArr[portNumCount++] = lastNum;
								 continue;
							 } else {
								 errType = "501 Syntax error in parameter.";
								 return false;
							 }
						 } else {
							 errType = "501 Syntax error in parameter.";
							 return false;
						 }
					 }
				 }
				 if(isNumeric(portToken)) {
					 if(Integer.parseInt(portToken) >= 0 && Integer.parseInt(portToken) <= 255) {
						 portNumArr[portNumCount++] = portToken;
					 } else {
						 errType = "501 Syntax error in parameter.";
						 return false;
					 }
				 } else {
					 errType = "501 Syntax error in parameter.";
					 return false;
				 }
			}
			
			if(portNumCount == 6) {
				if(orderArr.size() == 2) {
					orderArr.add("PORT");
					sucType = "200 Port command successful (" + portNumArr[0] + "." + portNumArr[1] + "." + portNumArr[2] + "." + portNumArr[3] + "," + ((256 * Integer.parseInt(portNumArr[4])) + Integer.parseInt(portNumArr[5])) + ").";
					return true;
				} else if(orderArr.isEmpty()) {
					errType = "530 Not logged in.";
					return false;
				} else if(orderArr.size() == 3) {
					sucType = "200 Port command successful (" + portNumArr[0] + "." + portNumArr[1] + "." + portNumArr[2] + "." + portNumArr[3] + "," + ((256 * Integer.parseInt(portNumArr[4])) + Integer.parseInt(portNumArr[5])) + ").";
					return true;
				} else if(orderArr.size() == 4) {
					sucType = "200 Port command successful (" + portNumArr[0] + "." + portNumArr[1] + "." + portNumArr[2] + "." + portNumArr[3] + "," + ((256 * Integer.parseInt(portNumArr[4])) + Integer.parseInt(portNumArr[5])) + ").";
					orderArr.remove("RETR");
					return true;
				} else {
					errType = "503 Bad sequence of commands.";
					return false;
				}
			} else {
				 errType = "501 Syntax error in parameter.";
				 return false;
			}
			
		} else if (commandToken.toUpperCase().equals("RETR")) {
			while (tokenizedLine.hasMoreTokens()) {
				sumToken += tokenizedLine.nextToken();
			}
			while(sumToken.charAt(0) == '\\' || sumToken.charAt(0) == '/')
				sumToken = sumToken.substring(1);
			if(sumToken.length() < 3) {
				errType = "501 Syntax error in parameter.";
				return false;
			}
			String paramToken = sumToken.substring(0, sumToken.length() - 2);
			String crlf = sumToken.substring(sumToken.length() - 2);
			if(!crlf.matches("\\r\\n")) {
				errType = "501 Syntax error in parameter.";
				return false; 
			}
			boolean flag2 = true;
			for (int i = 0; i < paramToken.length(); i++) {
				int ascVal = paramToken.charAt(i);
				if (ascVal < 0 || ascVal > 127)
					flag2 = false;

			}
			if(!flag2) {
				errType = "501 Syntax error in parameter.";
				return false; 
			}
			
			if(orderArr.isEmpty()) {
				errType = "530 Not logged in.";
				return false;
			} else if(orderArr.size() == 1) {
				errType = "503 Bad sequence of commands.";
				return false;
			} else if(orderArr.size() == 3) {
				orderArr.add("RETR");
			} else if(orderArr.size() == 2 || orderArr.size() == 4) {
				errType = "503 Bad sequence of commands.";
				return false;
			} else {
				return false;
			}
			InputStream input = null;
			try{
				File source = new File(paramToken);
				input = new FileInputStream(source);
			    serverHost = portNumArr[0] + "." + portNumArr[1] + "." + portNumArr[2] + "." + portNumArr[3];
			    serverPortNum = ((256 * Integer.parseInt(portNumArr[4])) + Integer.parseInt(portNumArr[5]));
//			            output = new FileOutputStream(dest);
			            
			} catch(Exception e) {
				errType = "550 File not found or access denied.";
				orderArr.remove("RETR");
				return false;
			} 
			try {
//				System.out.println(serverPortNum);
		    	dataConnection = new Socket(serverHost,serverPortNum);
		    	if(dataConnection != null) {
			    	DataOutputStream outToClientData = new DataOutputStream(dataConnection.getOutputStream());
			       	byte[] buf = new byte[1024];
		            int bytesRead;
		            while ((bytesRead = input.read(buf)) > 0) {
		            	outToClientData.write(buf, 0, bytesRead);
				    }
		            
	            }
//		    	dataConnection.close();
		    } catch(IOException e) {
		    	sucType = "150 File status okay.\r\n425 Can not open data connection.";
				orderArr.remove("RETR");
				return true;
			} finally {
	        	if(input != null) {
	        		try {
						input.close();
					} catch (IOException e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
					}
	        	}		
	        }
		    
			sucType = "150 File status okay.\r\n250 Requested file action completed.";
			fileNum++;
			return true;
		} else if (commandToken.toUpperCase().equals("QUIT\r\n")) {
			while (tokenizedLine.hasMoreTokens()) {
				sumToken += tokenizedLine.nextToken();
			}
			if (sumToken.length() == 0) {
				sucType = "221 Goodbye.";
//				System.out.printf("QUIT%s",CRLF);
//				System.out.print(sucType + CRLF);
				orderArr.clear();
				quitArr.clear();
				fileNum = 1;
				
//				System.exit(0);
				
				return true;
			} else {
				errType = "501 Syntax error in parameter.";
				return false;
			}
		} else if (commandToken.toUpperCase().equals("SYST\r\n")) {
			while (tokenizedLine.hasMoreTokens()) {
				sumToken += tokenizedLine.nextToken();
			}
			if (sumToken.length() == 0) {
				if(orderArr.isEmpty()) {
					errType = "530 Not logged in.";
					return false;
				} else if(orderArr.size() == 1) {
					errType = "503 Bad sequence of commands.";
					return false;
				} else {
					sucType = "215 UNIX Type: L8.";
					return true;
				}
			} else {
				errType = "501 Syntax error in parameter.";
				return false;
			}
		} else {
			while (tokenizedLine.hasMoreTokens()) {
				sumToken += tokenizedLine.nextToken();
			}
			if (sumToken.length() == 0) {
				if(orderArr.isEmpty()) {
					errType = "530 Not logged in.";
					return false;
				} else if(orderArr.size() == 1) {
					errType = "503 Bad sequence of commands.";
					return false;
				} else {
					sucType = "200 Command OK.";
				}
				return true;
			} else {
				errType = "501 Syntax error in parameter.";
				return false;
			}
		}

	}
	public static boolean isNumeric(String inputData) {
		char[] ch = inputData.toCharArray();
		for(char element: ch) {
			if(!Character.isDigit(element)) {
				return false;
			}
		}
		return true;
	}
	
}
