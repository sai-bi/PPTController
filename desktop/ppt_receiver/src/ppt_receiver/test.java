package assign7;


import java.awt.BasicStroke;
import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Point;
import java.awt.RenderingHints;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.awt.event.MouseMotionListener;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.ArrayList;

import javax.swing.*;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;

public class Painter {
	private JFrame frame;
	private DrawPanel draw;
	private JPanel pick;
	private Color selectColor;
    private JColorChooser colorPick;
    private Point start;
    private Point end;
    private JScrollPane scrollPan;
    private ArrayList<Line> image;
    private Line line;
    private JMenuItem clear;
    private JMenuItem save;
    private JMenuItem exit;
    private JMenuItem load;
    private JMenuItem undo;
    private JButton zoomIn;
    private JButton zoomOut;
    private double width;
    private double height;
    private double scale;
    private ArrayList<ObjectOutputStream> clientOutputStreams;
    private boolean isClient;
    //server side 
    public Painter(int port){
    	isClient = false;
    	try {
			Thread t = new Thread(new serverThread(port));
			t.start();
		} catch (Exception e) {
			JOptionPane.showMessageDialog(frame,
    			    "Unable to listen to port " + Integer.toString(port) + "!",
    			    "Fail to start",
    			    JOptionPane.ERROR_MESSAGE);
    		System.exit(0);
		}
    	setupUI();
    }
    
    
    //client side
    public Painter(String host, int port){
    	isClient = true;
    	try {
			Thread t = new Thread(new clientThread(host,port));
			t.start();
		} catch (Exception e) {
			JOptionPane.showMessageDialog(frame,
    			    "Unable to connect to host!",
    			    "Fail to start",
    			    JOptionPane.ERROR_MESSAGE);
    		System.exit(0);
		}
    	setupUI();
    }
    
    class serverThread implements Runnable{
    	private ServerSocket s;
		public serverThread(int port) throws Exception {
	    	s = new ServerSocket(port);
	    	clientOutputStreams = new ArrayList<ObjectOutputStream>();	    
		}
		
		public void run(){
			// TODO Auto-generated method stub
			int id = 0;
			try{
	    		while(true){
	    			System.out.println("listening");
	    			Socket client = s.accept();
	    			System.out.println("Got a connection");
	    			ObjectOutputStream w = new ObjectOutputStream(client.getOutputStream());
	    			clientOutputStreams.add(w);
	    			Thread t = new Thread(new ClientHandler(client,id));
	    			id = id + 1;
	    			t.start();
	    		}
			}catch(Exception e){
				//System.out.println("Error 3");
			}
		}
    	
    }
    
    class clientThread implements Runnable{
    	private Socket s;
    	public clientThread(String address, int port) throws Exception{
	    	s = new Socket(address,port);
	    	clientOutputStreams = new ArrayList<ObjectOutputStream>();
	    	ObjectOutputStream w = new ObjectOutputStream(s.getOutputStream());
			clientOutputStreams.add(w);
    	}

		@Override
		public void run() {
			// TODO Auto-generated method stub
			Thread t = new Thread(new ClientHandler(s,-1));
			t.start();
		}
    	
    }
    
    
    class ClientHandler implements Runnable {
    	private BufferedReader reader;
    	private ObjectInputStream objInput;
    	Socket s;
    	//private int clientID;
    	public ClientHandler(Socket client, int id){
    		try{
    			s = client;
    			//clientID = id;
    			//InputStreamReader is = new InputStreamReader(s.getInputStream());
    			//reader = new BufferedReader(is);
    			objInput = new ObjectInputStream(s.getInputStream());
    		}catch(Exception e){
    			JOptionPane.showMessageDialog(frame,
        			    "Network error!",
        			    "Error",
        			    JOptionPane.ERROR_MESSAGE);
        		System.exit(0);
    		}
    	}
    	public void run(){
    		Line temp;
    		try {
				while(true){
					//server draws the line
					temp = (Line) objInput.readObject();
					if(temp == null)
						continue;
		
					if(temp.getOperation() == 2){
						//clear
						image.clear();
						draw.repaint();
					}
					else if(temp.getOperation() == 3){
						//undo
						if(image.size() == 0)
							continue;
						else{
							image.remove(image.size()-1);
							draw.repaint();
						}
					}
					else if(temp.getOperation() == 4){
						JOptionPane.showMessageDialog(frame,
		        			    "Host is gone!",
		        			    "Connection dropped",
		        			    JOptionPane.ERROR_MESSAGE);
		        		System.exit(0);
					}
					else{
						//normal drawing
						image.add(temp);
						draw.repaint();
					}
					//image.add(temp);
					//draw.repaint();
					//tell every clients to draw the line
					//if(clientID != -1)
					if(!isClient){
						tellAllClients(temp);
						//System.out.println("Tell all clients");
					}
				}
			} catch(Exception e){
				//e.printStackTrace();
				//System.out.println("Error 2");
			}
    	}    
    }
    
    public void tellAllClients(Line l){
		for(int i = 0;i < clientOutputStreams.size();i++){
			//if(i == id)
				//continue;
			ObjectOutputStream w = clientOutputStreams.get(i);
			try {
				w.writeObject(l);
				w.flush();
			} catch (IOException e) {
				//System.out.println("Error 1");
			}
		}
		
	}
    
    
    
	public void setupUI(){
		
		image = new ArrayList<Line>();
		selectColor = Color.black;
		line = new Line(selectColor,scale);
		scale = 1;
		
        frame = new JFrame("Painter");
		frame.setSize(1200,700);
		frame.addWindowListener(new WindowAdapter(){
			public void windowClosing(WindowEvent e){
				if(!isClient){
					Line temp = new Line();
					temp.setOperation(4);
					tellAllClients(temp);
				}
				System.exit(0);
				
            }
		});
		frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
	    frame.setResizable(false);
        //add menu    
        JMenu menu = new JMenu("Option");
        save = new JMenuItem("Save");
        save.addActionListener(new OptionListener());
        exit = new JMenuItem("Exit");
        exit.addActionListener(new OptionListener());
        if(!isClient){
        	clear = new JMenuItem("Clear");
            clear.addActionListener(new OptionListener());
            load = new JMenuItem("Load");
            load.addActionListener(new OptionListener());
            undo = new JMenuItem("Undo");
            undo.addActionListener(new OptionListener());
            menu.add(clear);
            menu.add(undo);
            menu.add(load);
        }
        menu.add(save);        
        menu.add(exit);
        
        JMenuBar mb = new JMenuBar();
        mb.add(menu);
        frame.setJMenuBar(mb);
        frame.setLayout(new BorderLayout());

        draw = new DrawPanel();
        width = 2500;
        height = 2500;
        draw.setPreferredSize(new Dimension(2500,2500));
        draw.setBackground(Color.WHITE);
        
        scrollPan = new JScrollPane(draw);
        scrollPan.setPreferredSize(new Dimension(700,700));
        
        colorPick = new JColorChooser();
        zoomIn = new JButton("+");
        zoomOut = new JButton("-");
        
        zoomIn.setPreferredSize(new Dimension(250,50));
        zoomOut.setPreferredSize(new Dimension(250,50));
        colorPick.setPreferredSize(new Dimension(500,600));
        
        pick = new JPanel();
        pick.setLayout(new BorderLayout());
        pick.add(colorPick,BorderLayout.NORTH);
        pick.add(zoomOut,BorderLayout.WEST);
        pick.add(zoomIn,BorderLayout.EAST);        
       
        colorPick.getSelectionModel().addChangeListener(new colorChangeListener());
        draw.addMouseListener(new mouseClick());
        draw.addMouseMotionListener(new mouseMove());
        zoomIn.addActionListener(new zoomInListener());
        zoomOut.addActionListener(new zoomOutListener());
         
        
        frame.getContentPane().add(scrollPan,BorderLayout.WEST);
        frame.getContentPane().add(pick,BorderLayout.EAST);
        frame.setVisible(true);
	}
	


	class colorChangeListener implements ChangeListener{
		
		@Override
		public void stateChanged(ChangeEvent arg0) {
			// TODO Auto-generated method stub
			
			selectColor = colorPick.getColor();
            //draw.setBackground(selectColor); 
		}
		
	}
	
	class DrawPanel extends JPanel {
		public void paintComponent(Graphics g) {
			super.paintComponent(g);
			Graphics2D g2 = (Graphics2D) g;
	        g2.setStroke(new BasicStroke(10));
	        g2.setRenderingHint(
	        	    RenderingHints.KEY_ANTIALIASING,
	        	    RenderingHints.VALUE_ANTIALIAS_OFF);
	        g2.setRenderingHint(
	        	    RenderingHints.KEY_TEXT_ANTIALIASING,
	        	    RenderingHints.VALUE_TEXT_ANTIALIAS_OFF);

	        for(int i = 0;i < image.size();i++){
	        	ArrayList<Point> temp;
	        	temp = image.get(i).getLine();
	        	g2.setColor(image.get(i).getColor());
	        	double lineScale = image.get(i).getScale();

	        	for(int j = 0;j < temp.size()-1;j++){
	        		g2.drawLine((int)(temp.get(j).x / lineScale * scale), (int)(temp.get(j).y / lineScale * scale), 
	        					(int)(temp.get(j+1).x / lineScale* scale), (int)(temp.get(j+1).y / lineScale* scale));
	        	}
	        }
	        g2.setColor(line.getColor());
	        ArrayList<Point> temp = line.getLine();
	        for(int j = 0;j < temp.size()-1;j++){
	        	g2.drawLine((int)(temp.get(j).x ), (int)(temp.get(j).y ), (int)(temp.get(j+1).x ), (int)(temp.get(j+1).y ));
        	}

        	start = end;
		}
	}
	
	class mouseClick implements MouseListener{
		@Override
		public void mouseEntered(MouseEvent e) {
		}

		@Override
		public void mouseExited(MouseEvent e) {
		}

		@Override
		public void mousePressed(MouseEvent e) {
			
			start = e.getPoint();
			//start.x = (int)(start.x * scale);
			//start.y = (int)(start.y * scale);
			line = new Line(selectColor,scale);
			line.addPoint(start);
			draw.repaint();
		}

		@Override
		public void mouseReleased(MouseEvent e) {
			tellAllClients(line);
			image.add(line);
			line = new Line(selectColor,scale);
		}

		@Override
		public void mouseClicked(MouseEvent e) {
			// do nothing			
		}
		
	}

	
	class mouseMove implements MouseMotionListener{

		@Override
		public void mouseDragged(MouseEvent e) {
			// TODO Auto-generated method stub
			end = e.getPoint();
			//end.x = (int)(end.x * scale);
			//end.y = (int)(end.y * scale);
			line.addPoint(end);
			draw.repaint();
		}

		@Override
		public void mouseMoved(MouseEvent arg0) {
			//do nothing
		}
	}

	
	
	class OptionListener implements ActionListener{

		@Override
		public void actionPerformed(ActionEvent e) {
			// TODO Auto-generated method stub
			if(e.getSource() == exit){
				if(!isClient){
					Line temp = new Line();
					temp.setOperation(4);
					tellAllClients(temp);
				}
				System.exit(0);
			}
			else if(e.getSource() == load){
				JFileChooser jf = new JFileChooser();
				int status = jf.showOpenDialog(frame);
				if(status == JFileChooser.APPROVE_OPTION){
					File file = jf.getSelectedFile();
					try {
						FileInputStream input = new FileInputStream(file);
						ObjectInputStream objectIn = new ObjectInputStream(input);
						Line temp = new Line(selectColor,scale);
						image.clear();
						
						temp.setOperation(2);
						tellAllClients(temp);
						
						while((temp = (Line) (objectIn.readObject())) != null){
							image.add(temp);
							tellAllClients(temp);
						}
						objectIn.close();
					}catch (Exception e1) {
					}finally{
						draw.repaint();
					}
				}
			}
			else if(e.getSource() == save){
				JFileChooser jf = new JFileChooser();
				int status = jf.showSaveDialog(frame);
				if(status == JFileChooser.APPROVE_OPTION){
					File file = jf.getSelectedFile();
					try {
						FileOutputStream output = new FileOutputStream(file);
						ObjectOutputStream objectOut = new ObjectOutputStream(output);
						for(int i = 0;i < image.size();i++){
							objectOut.writeObject(image.get(i));
						}
						objectOut.close();
					} catch (IOException e1) {
						e1.printStackTrace();
						JOptionPane.showMessageDialog(frame,
							    "Cannot write to the selected file",
							    "IO Error",
							    JOptionPane.ERROR_MESSAGE);
					}
				}
			}
			else if(e.getSource() == clear){
				Line temp = new Line();
				temp.setOperation(2);
				tellAllClients(temp);
				image.clear();
				draw.repaint();
			}
			else if(e.getSource() == undo){
				Line temp = new Line();
				temp.setOperation(3);
				tellAllClients(temp);
				
				if(image.size() == 0)
					return;
				else{
					image.remove(image.size()-1);
					draw.repaint();
				}
			}
		}
		
	}
	
	class zoomInListener implements ActionListener{

		@Override
		public void actionPerformed(ActionEvent e) {
			// TODO Auto-generated method stub
			scale  = scale * 1.2;
			height  = height * 1.2;
			width  = width * 1.2;
			draw.setPreferredSize(new Dimension((int)width,(int)height));
			draw.revalidate();
			draw.repaint();
		}
		
	}
	class zoomOutListener implements ActionListener{

		@Override
		public void actionPerformed(ActionEvent e) {
			// TODO Auto-generated method stub
			scale  = scale / 1.2;
			height  = height /1.2;
			width  = width / 1.2;
			draw.setPreferredSize(new Dimension((int)width,(int)height));
			draw.revalidate();
			draw.repaint();
		}
		
	}
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	

	
	
	
	
	
	
	
	
	
	
	
	
	

}

