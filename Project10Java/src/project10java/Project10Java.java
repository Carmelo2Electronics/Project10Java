


/*
 * To run the program, disconnect and connect arduino from the computer's (USB) port. 
 * Then run the program
 */



package project10java;

import java.awt.Color;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.nio.charset.StandardCharsets;

import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;

import com.fazecast.jSerialComm.SerialPort;
import com.fazecast.jSerialComm.SerialPortEvent;
import com.fazecast.jSerialComm.SerialPortPacketListener;


//55555555555555555555555555555555555555555555555555555555555555999999999999fffffffffffffffffffff
public class Project10Java {

	public static void main(String[] args) {
        SerialPort serialPort=new LookingPortsConfigure().getSelectedPort();
		Frame frame=new Frame(serialPort);
		frame.setVisible(true);
	}
}
//55555555555555555555555555555555555555555555555555555555555555999999999999fffffffffffffffffffff

//hhhhhhhhhhhhhhhhhhhhhhhhhhhhhhhhhhhhhhhhhhhhhhhhhhhhhhhhhhhhhhhhhhhhhhhhhhhhhhhhhhhhhhhhhhhh
class LookingPortsConfigure{
	
	private SerialPort[] serialPortArray;	
	private String[] dataPort;	
	private Object selectedPortObject;	
	private SerialPort serialPort;
	private String selection;

	public LookingPortsConfigure() {	
		serialPortArray= SerialPort.getCommPorts();			
		dataPort = new String[serialPortArray.length];
				
		if(serialPortArray.length==0) {
			JOptionPane.showMessageDialog(null, "No busy comm port", "ERROR MESSAGE", JOptionPane.ERROR_MESSAGE);
			System.exit(0);
		}
		
		for (int i = 0; i < serialPortArray.length; ++i) {
			dataPort[i]=i + "  "+
			serialPortArray[i].getSystemPortName()+ "  " +
			serialPortArray[i].getDescriptivePortName()+ "  " +
			serialPortArray[i].getPortDescription();
		}
		
		selectedPortObject = JOptionPane.showInputDialog(null,"Choose port", "PORTS", JOptionPane.QUESTION_MESSAGE, null, dataPort,"Seleccione");
				
		if(selectedPortObject==null){
			JOptionPane.showMessageDialog(null, "You did not select port", "ERROR MESSAGE", JOptionPane.ERROR_MESSAGE);
			System.exit(0);
		}else {
			selection=selectedPortObject.toString().substring(0,1);			
			serialPort=SerialPort.getCommPort(serialPortArray[Integer.parseInt(selection)].getSystemPortName());		
			serialPort.setComPortParameters(9600, 8, 1, 0);		//port configuration
			serialPort.openPort();	
		}
	}	
	
	public SerialPort getSelectedPort() {
		return serialPort;
	}
}
//hhhhhhhhhhhhhhhhhhhhhhhhhhhhhhhhhhhhhhhhhhhhhhhhhhhhhhhhhhhhhhhhhhhhhhhhhhhhhhhhhhhhhhhhhhhh

//+++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++
class Frame extends JFrame{
	private static final long serialVersionUID = 1L;
	
	private SendString  sendString ;
	
	public Frame(SerialPort serialPort) {	
		setDefaultCloseOperation(JFrame.DO_NOTHING_ON_CLOSE);		
		setBounds(20, 50, 500, 100);
		setTitle("Carmelo2Elecronics");
		sendString =new SendString (serialPort);
		
		addWindowListener(new WindowAdapter() {							
	        public void windowClosing(WindowEvent event) {
	    		int opcion=JOptionPane.showConfirmDialog(null, "You want to close the entire program", "CONFIRMATION", JOptionPane.OK_CANCEL_OPTION);
	    		if(opcion==0) {
	    			sendString.stringSend("Led_0_OFF"+"\n");		//Turn off the leds when we close the program
	    			sendString.stringSend("Led_1_OFF"+"\n");
	    			sendString.stringSend("Led_2_OFF"+"\n");
	    			serialPort.closePort();
	    			System.exit(0);	    			
	    		}
	        }
	    });		
		add(new Panel(serialPort));		
	}
}
//+++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++

//gggggggggggggggggggggggggggggggggggggggggggggggggggggggggggggggggggggggggggg
class Panel extends JPanel implements ActionListener{
	private static final long serialVersionUID = 1L;
	
	private JButton Button[]= {new JButton("Led_0_OFF"),new JButton("Led_1_OFF"),new JButton("Led_2_OFF")};	
	private boolean flags[]= {false, false,false};	
	private SendString  sendString ;
	private JLabel label;
	
	public Panel(SerialPort puerto) {	
		for(int i=0;i<3;i++) {
			add(Button[i]);
			Button[i].addActionListener(this);
		}
		label=new JLabel("--START--");
		add(label);
		sendString =new SendString (puerto);		
		new ReceiveString(puerto,label);
	}

	public void actionPerformed(ActionEvent e) {		
		if(e.getSource()==Button[0]) {			
			executeEvent(0);			
		}		
		else if(e.getSource()==Button[1]) {
			executeEvent(1);			
		}
		else if(e.getSource()==Button[2]) {
			executeEvent(2);			
		}
	}
	
	public void executeEvent(int a) {		
		if(flags[a]==false) {
			Button[a].setText("Led_"+a+"_OFF");
			Button[a].setBackground(Color.RED);
			sendString.stringSend("Led_"+a+"_OFF"+"\n");		//This is the string you send to Arduino, it is important to send the escape sequence "\n" as part of the string
			flags[a]=!flags[a];
			
		}else {
			Button[a].setText("Led_"+a+"_ON ");
			Button[a].setBackground(Color.GREEN);
			sendString.stringSend("Led_"+a+"__ON"+"\n");
			flags[a]=!flags[a];
		}			
	}
}
//gggggggggggggggggggggggggggggggggggggggggggggggggggggggggggggggggggggggggggg

//hhhhhhhhhhhhhhhhhhhhhhhhhhhhhhhhhhhhhhhhhhhhhhhhhhhhhhhhhhhhhhhhhh
class ReceiveString implements SerialPortPacketListener{

	private StringBuilder stringBuilder;
	private String stringReceived;	
	private JLabel label;
	
	public ReceiveString(SerialPort serialPort, JLabel label) {		
		this.label=label;	
		serialPort.addDataListener(this);
	}
	
	public int getListeningEvents() {
		return SerialPort.LISTENING_EVENT_DATA_RECEIVED;
	}	
	
	public void serialEvent(SerialPortEvent event) {
		stringBuilder = new StringBuilder();
		byte[] newData = event.getReceivedData();

		for (int i = 0; i < newData.length; ++i) {
			if((char)newData[i]!='\n') {
				stringBuilder.append((char)newData[i]);
			}else {
				break;
			}
		}	
		stringReceived=stringBuilder.toString();
		stringReceived.trim();
		label.setText(stringReceived);			//Put the string received from arduino on the label		
		stringBuilder=null;
	}
	
	public int getPacketSize() {
		return 9;		//Number of characters you receive from Arduino. Use a fixed number of characters always.
	}						
}
//hhhhhhhhhhhhhhhhhhhhhhhhhhhhhhhhhhhhhhhhhhhhhhhhhhhhhhhhhhhhhhhhhh

//pppppppppppppppppppppppppppppppppppppppppppppppppppppppppppppppppppppppppppppppppppp
class SendString {
	
	private SerialPort serialPort;
	private int stringLegth;
	private byte[] newData;

	public SendString (SerialPort serialPort) {
		this.serialPort=serialPort;
	}	
	
	public void stringSend(String stringToSend)  {		
		stringLegth=stringToSend.length();		
		newData = new byte[stringLegth];	
		newData=stringToSend.getBytes(StandardCharsets.ISO_8859_1);
		serialPort.writeBytes(newData, stringLegth);
	}
}
//pppppppppppppppppppppppppppppppppppppppppppppppppppppppppppppppppppppppppppppppppppp


