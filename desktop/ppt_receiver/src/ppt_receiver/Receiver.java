package ppt_receiver;

import java.awt.AWTException;
import java.awt.Robot;
import java.awt.event.KeyEvent;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.OutputStream;
import java.io.PrintWriter;
import java.net.ServerSocket;
import java.net.Socket;

import javax.swing.JOptionPane;



public class Receiver {
	private ServerSocket serverSock;
	private int serverPort;
	public Receiver(int port) {
		// TODO Auto-generated constructor stub
        startServer(port);
	}
    
    private void startServer(int port){
    	try {
    		serverSock = new ServerSocket(port);
    		while(true){
    			System.out.println("listening");
    			Socket client = serverSock.accept();
    			System.out.println("Got a connection");
    			
    			Thread t = new Thread(new ClientHandler(client));
    			t.start();
    		}
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
       

    }   
    
    class ClientHandler implements Runnable {
    	private Socket client;
    	private int contentLength;
    	private OutputStream out;
    	public ClientHandler(Socket s){
    		client = s;
    		try {
				out = s.getOutputStream();
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}  
    	}
		@Override
		public void run() {
			// TODO Auto-generated method stub
			InputStream input;
			try {
				input = client.getInputStream();
				BufferedReader reader = new BufferedReader(new InputStreamReader(input));
				
				String line = reader.readLine();  
		        while (line != null) {  
		            System.out.println(line);  
		            line = reader.readLine();  
		            
		            if ("".equals(line)) {  
		                break;  
		            } else if (line.indexOf("Content-Length") != -1) {  
		                this.contentLength = Integer.parseInt(line.substring(line.indexOf("Content-Length") + 16));  
		                System.out.println("contentLength: " + this.contentLength);  
		            }
		        }  
		        //contentLength = 1;
		        if (this.contentLength != 0) {  
		            char[] buf = new char[this.contentLength];  
		            int totalRead = 0;  
		            int size = 0;  
		            while (totalRead < this.contentLength) {  
		            	System.out.println("hello world");
		                size = reader.read(buf,totalRead, contentLength-totalRead);  
		                totalRead += size;  
		            }  
		            System.out.println("hello world2");
		            String dataString = new String(buf, 0, totalRead);  
		            process(dataString);
		            
		            System.out.println("the data user posted:\n" + dataString);
		        }
		        String response = "";  
		        response += "HTTP/1.1 200 OK\n";  
		        response += "Server: Sunpache 1.0\n";  
		        response += "Content-Type: text/html\n";  
		        response += "Last-Modified: Mon, 11 Jan 1998 13:23:42 GMT\n";  
		        response += "Access-Control-Allow-Origin: http://localhost:5000\n";
		        response +=	"Access-Control-Allow-Credentials: true\n";
		        response += "Access-Control-Expose-Headers: FooBar\n";
		        	
		        response += "Accept-ranges: bytes";  
		        response += "\n";  
		        out.write(response.getBytes());
		        out.write("<html><head><title>test server</title></head><body><p>Post is ok</p></body></html>".getBytes());  
		        out.flush();  
		        reader.close();  
		        System.out.println("request complete."); 
		        
				
			}catch(Exception e){
				e.printStackTrace();
			}
		}
		
		private void process(String data){
			//data = data.substring(2);
			// previous
			if(data.length() == 0)
				return;
			
			try {
				Robot robot = new Robot();
				if(data.charAt(0) == '0'){
					// previous
					robot.keyPress(KeyEvent.VK_UP);
					
				}
				else if(data.charAt(0) == '1'){
					// next
					robot.keyPress(KeyEvent.VK_DOWN);
				}
			} catch (AWTException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
		
		
		
		
		
		
		
		
		
		
		
		
		
		
		
		
		
		
		
		
		
		
		
		
		
		
		
		
		
		
		
		
		
		
		
		
		
    
    }
	

}
