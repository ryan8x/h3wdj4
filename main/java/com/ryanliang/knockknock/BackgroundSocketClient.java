package com.ryanliang.knockknock;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.PrintWriter;
import java.net.InetAddress;
import java.net.ServerSocket;
import java.net.Socket;
import java.net.SocketTimeoutException;
import java.net.UnknownHostException;
import java.util.List;
import java.util.concurrent.ExecutionException;

import javax.swing.JLabel;
import javax.swing.JTextArea;
import javax.swing.SwingWorker;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

/**
 * BackgroundSocketClient is a SwingWorker subclass utilized by the knock knock client app for processing socket network communication with the knock knock server. 
 * @author Ryan L.
 * @version $Revision$
 * @since 1.7
 */
public class BackgroundSocketClient extends SwingWorker<Void, String> {
    private static Logger logger;
    static{
    	System.setProperty("logFileName", "client.log");
    	logger = LogManager.getLogger();
    }
    	
	private String userInput = null;
    private JTextArea chatTextArea;
    private JLabel connectionStatusLabel;
    
	private Socket kkSocket = null;
	
	private String kkServerHost;
	private int kkServerPort;
	
	private String exceptionErrorMessage = "";
	
	private ObjectOutputStream out = null;
	private ObjectInputStream in = null;
    
	/**
	 * This is the only constructor defined for this class.
	 * @param kkServerHost Specifies server host name or ip address
	 * @param kkServerPort Specifies server port number
	 * @param connectionStatusLabel A reference JLabel for updating connection status
	 * @param chatTextArea A reference JTextArea for updating chat text
	 */
    public BackgroundSocketClient(String kkServerHost, int kkServerPort, JLabel connectionStatusLabel, JTextArea chatTextArea){
    	this.kkServerHost = kkServerHost;
    	this.kkServerPort = kkServerPort;
    	this.connectionStatusLabel = connectionStatusLabel;
    	this.chatTextArea = chatTextArea;
    }
    
	/**
	 * This method performs a task in the background in a SwingWorker thread.
	 * @return null 
	 */
	@Override
	public Void doInBackground(){
		logger.trace("doInBackground() is called");
		
		connectToServer();
		
		return null;
	}
	
	/**
	 * This method updates specific Swing components (UI) when doInBackground() is completed. 
	 */
	@Override
	protected void done(){
		logger.trace("done() is called");
		
		if (exceptionErrorMessage.length() > 1){
			connectionStatusLabel.setText("<html>Connection status: <font color='red'>Fail</font></html>");
			chatTextArea.append(exceptionErrorMessage + "\n");
		}
	}
	
	/**
	 * This method updates specific Swing components (UI) while doInBackground() is in progress. 
	 */
    @Override
    protected void process(List<String> chunks) {

    	chatTextArea.append(chunks.get(0) + "\n");
	}
	
	/**
	 * This method is for establishing connection to the server. 
	 */
	private void connectToServer() {
		logger.trace("connectToServer() is called");
		
		try {
			kkSocket = new Socket(InetAddress.getByName(kkServerHost), kkServerPort);
			out = new ObjectOutputStream(kkSocket.getOutputStream());
			in = new ObjectInputStream(kkSocket.getInputStream());
			
			String fromServer;

			while ((fromServer = (String) in.readObject()) != null) {

				publish(fromServer);

				if (fromServer.equals("Bye")){
					break;
				}

				//wait for user input
				while (userInput == null)
				{
					try {
						Thread.sleep(200);
					} catch (InterruptedException e) {
						//e.printStackTrace();
						System.err.println("thread sleep method of client SwingWorker is being interrupted.");
						logger.error("thread sleep method of client SwingWorker is being interrupted.");
					}
				}

				if (userInput != null) {
					out.writeObject(userInput);
					out.flush();
					userInput = null;
				}
			}
		} catch (ClassNotFoundException e) {
			System.err.println("Client side socket network communication error is encountered for some reason.");
			logger.error("Client side socket network communication error is encountered for some reason.");
		} catch (UnknownHostException e) {
			exceptionErrorMessage = "Don't know about host " + kkServerHost;
			System.err.println(exceptionErrorMessage);
			logger.warn(exceptionErrorMessage);
		} catch (IOException e) {
			exceptionErrorMessage = "Couldn't get I/O for the connection to " + kkServerHost + ":" + kkServerPort;
			System.err.println(exceptionErrorMessage);
			logger.warn(exceptionErrorMessage);
		} 
		finally{
			stopServer();
		}
	}
	
	/**
	 * This method is for freeing up resources. 
	 */
	public void stopServer() {
		logger.trace("stopServer() is called");
		
		try {
			if (kkSocket != null){
				if (out != null){
					out.close();
					out = null;
				}
				if (in != null){
					in.close();
					in = null;
				}
				kkSocket.close();
				kkSocket = null;
				userInput = "exit";
			}
		} catch (IOException e) {
			//e.printStackTrace();
			System.err.println("Client socket is being closed.");
			logger.warn("Client socket is being closed.");
		}

	}
	
	/**
	 * This method is for accepting user text input.
	 */
	public void processUserInput(String userInput) {
		logger.trace("processUserInput(String userInput) is called");
		
		this.userInput = userInput;
	}
}