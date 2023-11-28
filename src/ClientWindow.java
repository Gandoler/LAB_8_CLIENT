import javax.accessibility.AccessibleContext;
import javax.swing.*;
import java.awt.*;
import java.awt.event.*;
import java.io.*;
import java.net.Socket;
import java.util.Scanner;
 
public class ClientWindow extends JFrame {
  // адрес сервера
  private static String SERVER_HOST = "localhost";
  // порт
  private static final int SERVER_PORT = 3443;
  // клиентский сокет
  private Socket clientSocket;
  // входящее сообщение
  private Scanner inMessage;
  // исходящее сообщение
  private PrintWriter outMessage;
  // следующие поля отвечают за элементы формы
  private JTextField jtfMessage;
  private JTextField jtfName;
  private JTextArea jtaTextAreaMessage;
  // имя клиента
  private String clientName = "";
  // получаем имя клиента
  public String getClientName() {
    return this.clientName;
  }
 
  // конструктор
  public ClientWindow() {
	  SERVER_HOST = JOptionPane.showInputDialog(null, new String[] {"Введите IP-адрес сервера",
	  "Формат: xxx.xxx.xxx.xxx"}, 
  "Подключение",
  JOptionPane.QUESTION_MESSAGE);
	  while(clientName.equals(""))
		  clientName = JOptionPane.showInputDialog(null, new String[] {"Введите свой ник:"}, 
		  "Подключение",   
		  JOptionPane.QUESTION_MESSAGE);
    try {
      clientSocket = new Socket(SERVER_HOST, SERVER_PORT);
      inMessage = new Scanner(clientSocket.getInputStream());
      outMessage = new PrintWriter(clientSocket.getOutputStream());
    } catch (IOException e) {
      e.printStackTrace();
    }
    // Задаём настройки элементов на форме
    setBounds(600, 300, 600, 500);
    setTitle("Client");
    setDefaultCloseOperation(WindowConstants.EXIT_ON_CLOSE);
    jtaTextAreaMessage = new JTextArea();
    jtaTextAreaMessage.setEditable(false);
    jtaTextAreaMessage.setLineWrap(true);
    JScrollPane jsp = new JScrollPane(jtaTextAreaMessage);
    add(jsp, BorderLayout.CENTER);
    // label, который будет отражать количество клиентов в чате
    JLabel jlNumberOfClients = new JLabel("Количество клиентов в чате: ");
    add(jlNumberOfClients, BorderLayout.NORTH);
    JPanel bottomPanel = new JPanel(new BorderLayout());
    add(bottomPanel, BorderLayout.SOUTH);
    JButton jbSendMessage = new JButton("Отправить");
    
    bottomPanel.add(jbSendMessage, BorderLayout.EAST);
   
    jtfMessage = new JTextField("Введите ваше сообщение: ");
    bottomPanel.add(jtfMessage, BorderLayout.CENTER);
    jtfName = new JTextField(clientName);
    jtfName.setEditable(false);    
    bottomPanel.add(jtfName, BorderLayout.WEST);
    // обработчик события нажатия кнопки отправки сообщения
    jbSendMessage.addActionListener(new ActionListener() {
      @Override
      public void actionPerformed(ActionEvent e) {
        // если имя клиента, и сообщение непустые, то отправляем сообщение
        if (!jtfMessage.getText().trim().isEmpty()) {
          //clientName = jtfName.getText();
          sendMsg();
          // фокус на текстовое поле с сообщением
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
                     // фокус на текстовое поле с сообщением
                     jtfMessage.grabFocus();
                   }            	 
        	}                 
    });
    // при фокусе поле сообщения очищается
    jtfMessage.addFocusListener(new FocusAdapter() {
      @Override
      public void focusGained(FocusEvent e) {
        jtfMessage.setText("");
      }
    });
    jtfMessage.grabFocus();
    // в отдельном потоке начинаем работу с сервером
    new Thread(new Runnable() {
      @Override
      public void run() {
        try {
          // бесконечный цикл
          while (true) {
            // если есть входящее сообщение
            if (inMessage.hasNext()) {
              // считываем его
              String inMes = inMessage.nextLine();
              String clientsInChat = "Клиентов в чате = ";
              if (inMes.indexOf(clientsInChat) == 0) {
                jlNumberOfClients.setText(inMes);
              } else {
                // выводим сообщение
                jtaTextAreaMessage.append(inMes);
                // добавляем строку перехода
                jtaTextAreaMessage.append("\r\n");
              }
            }
          }
        } catch (Exception e) {
          }
      }
    }).start();
    // добавляем обработчик события закрытия окна клиентского приложения
    addWindowListener(new WindowAdapter() {
      @Override
      public void windowClosing(WindowEvent e) {
        super.windowClosing(e);
        try {         
            outMessage.println(clientName + " вышел из чата!");         
          
          // отправляем служебное сообщение, которое является признаком того, что клиент вышел из чата
          outMessage.println("##session##end##");
          outMessage.flush();
          outMessage.close();
          inMessage.close();
          clientSocket.close();
        } catch (IOException exc) { 
        }
      }
    });
    // отображаем форму
    setVisible(true);
    jtfMessage.grabFocus();
  }
 
  // отправка сообщения
  public void sendMsg() {
    // формируем сообщение для отправки на сервер
    String messageStr = jtfName.getText() + ": " + jtfMessage.getText();
    // отправляем сообщение
    outMessage.println(messageStr);
    outMessage.flush();
    jtfMessage.setText("");
  }
}