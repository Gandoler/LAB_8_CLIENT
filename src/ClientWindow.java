import javax.accessibility.AccessibleContext;
import javax.swing.*;
import java.awt.*;
import java.awt.event.*;
import java.io.*;
import java.net.Socket;
import java.util.Scanner;
 
public class ClientWindow extends JFrame {
  // ����� �������
  private static String SERVER_HOST = "localhost";
  // ����
  private static final int SERVER_PORT = 3443;
  // ���������� �����
  private Socket clientSocket;
  // �������� ���������
  private Scanner inMessage;
  // ��������� ���������
  private PrintWriter outMessage;
  // ��������� ���� �������� �� �������� �����
  private JTextField jtfMessage;
  private JTextField jtfName;
  private JTextArea jtaTextAreaMessage;
  // ��� �������
  private String clientName = "";
  // �������� ��� �������
  public String getClientName() {
    return this.clientName;
  }
 
  // �����������
  public ClientWindow() {
	  SERVER_HOST = JOptionPane.showInputDialog(null, new String[] {"������� IP-����� �������",
	  "������: xxx.xxx.xxx.xxx"}, 
  "�����������",
  JOptionPane.QUESTION_MESSAGE);
	  while(clientName.equals(""))
		  clientName = JOptionPane.showInputDialog(null, new String[] {"������� ���� ���:"}, 
		  "�����������",   
		  JOptionPane.QUESTION_MESSAGE);
    try {
      clientSocket = new Socket(SERVER_HOST, SERVER_PORT);
      inMessage = new Scanner(clientSocket.getInputStream());
      outMessage = new PrintWriter(clientSocket.getOutputStream());
    } catch (IOException e) {
      e.printStackTrace();
    }
    // ����� ��������� ��������� �� �����
    setBounds(600, 300, 600, 500);
    setTitle("Client");
    setDefaultCloseOperation(WindowConstants.EXIT_ON_CLOSE);
    jtaTextAreaMessage = new JTextArea();
    jtaTextAreaMessage.setEditable(false);
    jtaTextAreaMessage.setLineWrap(true);
    JScrollPane jsp = new JScrollPane(jtaTextAreaMessage);
    add(jsp, BorderLayout.CENTER);
    // label, ������� ����� �������� ���������� �������� � ����
    JLabel jlNumberOfClients = new JLabel("���������� �������� � ����: ");
    add(jlNumberOfClients, BorderLayout.NORTH);
    JPanel bottomPanel = new JPanel(new BorderLayout());
    add(bottomPanel, BorderLayout.SOUTH);
    JButton jbSendMessage = new JButton("���������");
    
    bottomPanel.add(jbSendMessage, BorderLayout.EAST);
   
    jtfMessage = new JTextField("������� ���� ���������: ");
    bottomPanel.add(jtfMessage, BorderLayout.CENTER);
    jtfName = new JTextField(clientName);
    jtfName.setEditable(false);    
    bottomPanel.add(jtfName, BorderLayout.WEST);
    // ���������� ������� ������� ������ �������� ���������
    jbSendMessage.addActionListener(new ActionListener() {
      @Override
      public void actionPerformed(ActionEvent e) {
        // ���� ��� �������, � ��������� ��������, �� ���������� ���������
        if (!jtfMessage.getText().trim().isEmpty()) {
          //clientName = jtfName.getText();
          sendMsg();
          // ����� �� ��������� ���� � ����������
          jtfMessage.grabFocus();
        }
      }
    });
    jtfMessage.addKeyListener(new KeyAdapter() {
        
        public void keyPressed(KeyEvent e) {
             if(e.getKeyCode() == KeyEvent.VK_ENTER)
            	 if (!jtfMessage.getText().trim().isEmpty()) {
                     //clientName = jtfName.getText();
                     sendMsg();
                     // ����� �� ��������� ���� � ����������
                     jtfMessage.grabFocus();
                   }            	 
        	}                 
    });
    // ��� ������ ���� ��������� ���������
    jtfMessage.addFocusListener(new FocusAdapter() {
      @Override
      public void focusGained(FocusEvent e) {
        jtfMessage.setText("");
      }
    });
    jtfMessage.grabFocus();
    // � ��������� ������ �������� ������ � ��������
    new Thread(new Runnable() {
      @Override
      public void run() {
        try {
          // ����������� ����
          while (true) {
            // ���� ���� �������� ���������
            if (inMessage.hasNext()) {
              // ��������� ���
              String inMes = inMessage.nextLine();
              String clientsInChat = "�������� � ���� = ";
              if (inMes.indexOf(clientsInChat) == 0) {
                jlNumberOfClients.setText(inMes);
              } else {
                // ������� ���������
                jtaTextAreaMessage.append(inMes);
                // ��������� ������ ��������
                jtaTextAreaMessage.append("\r\n");
              }
            }
          }
        } catch (Exception e) {
          }
      }
    }).start();
    // ��������� ���������� ������� �������� ���� ����������� ����������
    addWindowListener(new WindowAdapter() {
      @Override
      public void windowClosing(WindowEvent e) {
        super.windowClosing(e);
        try {         
            outMessage.println(clientName + " ����� �� ����!");         
          
          // ���������� ��������� ���������, ������� �������� ��������� ����, ��� ������ ����� �� ����
          outMessage.println("##session##end##");
          outMessage.flush();
          outMessage.close();
          inMessage.close();
          clientSocket.close();
        } catch (IOException exc) { 
        }
      }
    });
    // ���������� �����
    setVisible(true);
    jtfMessage.grabFocus();
  }
 
  // �������� ���������
  public void sendMsg() {
    // ��������� ��������� ��� �������� �� ������
    String messageStr = jtfName.getText() + ": " + jtfMessage.getText();
    // ���������� ���������
    outMessage.println(messageStr);
    outMessage.flush();
    jtfMessage.setText("");
  }
}