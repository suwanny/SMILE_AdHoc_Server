/*===================================================================================
  Developed by Sunmi Seol
  Modified by Chi-Hou Vong
  File Name: MainPanel2.java
  Version: 2.1
  Modified Time: 08.03.2012
======================================================================================*/
package edu.stanford;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.Graphics2D;
import java.awt.Image;
import java.awt.Insets;
import java.awt.Rectangle;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.image.BufferedImage;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;

import java.net.DatagramPacket;
import java.net.InetAddress;
import java.net.MulticastSocket;
import java.net.NetworkInterface;
import java.net.SocketException;
import java.net.URL;
import java.net.UnknownHostException;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.Formatter;
import java.util.Locale;
import java.util.Properties;
import java.util.ResourceBundle;
import java.util.Vector;

import javax.swing.BorderFactory;
import javax.swing.BoxLayout;
import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JComboBox;
import javax.swing.JEditorPane;
import javax.swing.JFileChooser;
import javax.swing.JLabel;
import javax.swing.JLayeredPane;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JSeparator;
import javax.swing.JTable;
import javax.swing.JTextArea;
import javax.swing.JTextField;
import javax.swing.ListSelectionModel;
import javax.swing.RowSorter;
import javax.swing.SortOrder;
import javax.swing.SwingConstants;
import javax.swing.border.Border;
import javax.swing.border.EtchedBorder;
import javax.swing.border.TitledBorder;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;
import javax.swing.table.JTableHeader;
import javax.swing.table.TableColumn;
import javax.swing.table.TableModel;
import javax.swing.table.TableRowSorter;
import javax.swing.text.Document;

@SuppressWarnings("serial")
public class MainPanel2 extends JPanel {

	//definition of variables
	Locale 			cur_locale;
	ResourceBundle 	rb;
	Properties conf;
	
	String currentIP = "128.0.0.1";       // dummy. Not used
	String current_language = "English";  // Default
	
	JEditorPane sc_pane;
	
	int X_1   = 260;		// width of LEFT  (control)
	int X_2   = 400;		// size of MIDDLE (student)	
	int X_3   = 400;		// size of RIGHT  (question)
	int Y_ALL = 700;	    // height of all

	// FOR MIDDLE PANE
	int Y_A = 220;		// height of Student Table
	
	public boolean multicastingStart = false;
	MulticastingThread multicastingThread;
	JLabel labB;
	JButton multicasting;
	
	mainWindow main;
	StudentStatus data_source; 
	
	JTextField 	apacheDirectory = null;
	JLabel 		remainingTime;
	JTextField 	timeLimit;
	JComboBox 	serverIP;
	JCheckBox 	chkReuse;
	JCheckBox 	chkTime;
	JTextField 	questionDirectory;
	JButton 	browseDir2;
	
	JComboBox spinner1;
	
	JButton btnConnect;
	JButton btnMakeQuestion;
	JButton btnStartSolve;
	JButton btnShowResult;
	JTextArea msgHelp;
	JButton browseDir;
	
	JLabel labNumStudent;
	JLabel labNumAnswer;
	JLabel labNumQuestion;
	
	JButton btnRefresh; // not used
	JButton btnRetakeQuiz;
	JButton btnStartSession;
	
	ControlPanel  control_panel;
	StudentPanel  student_panel;
	QuestionPanel question_panel;
	
	JTextArea msgHighRank;
	JFileChooser fc;

	JTable tableQuestion; 
	JTable tableStudent; 
	Color bgColor, btnBgColor, headerColor, btnFgColor;
	Font newButtonFont, zoomBtnFont;
	
	JPanel jp_question;
	JPanel zoomPanel;
	JEditorPane question_pane;	
	JLayeredPane q_layeredPane;
	
	//help message
	JPanel helppane;
	Border lowerededched3;
	TitledBorder title3;
	//Font titleFont;
	
	public MainPanel2(mainWindow _main, StudentStatus _d) {
		
		super();
		main        = _main;	
		data_source = _d;
			    		
		conf = new Properties();
		try {
		  URL url = getClass().getClassLoader().getResource("resources/conf.properties");
		  FileInputStream fis = new FileInputStream(url.getPath());
		  conf.load(fis);
		  fis.close();
		} catch(IOException e) {
		  e.printStackTrace();
		}
		
		set_default_locale();   //set English
		initialize_mainPanel();	// create all the panels
		String chosen_language = conf.getProperty("SEL_LANG");
		set_locale(chosen_language);		
		setDefaultApacheDirectory(conf.getProperty("APA_DIR"));
	}
	
	void set_default_locale(){
		cur_locale = new Locale(""); //English
		rb = ResourceBundle.getBundle("languages/MessageBundle", cur_locale);
	}
	
	ResourceBundle getRB() {return rb;}
	
	//create panel (main function)
	void initialize_mainPanel() {
		
		int y_bot = 100;
		int size_X = X_1 + X_2 + X_3 + 40;
		int size_Y = Y_ALL + y_bot;
		
		JPanel contents = new JPanel();
		contents.setLayout(null);  // Use Absolute layout. (Is it a good idea?)
		contents.setSize(size_X, size_Y);  
		contents.setPreferredSize(new Dimension(size_X, size_Y));
		contents.setMinimumSize(new Dimension(size_X, size_Y));
		contents.setMaximumSize(new Dimension(size_X, size_Y));
		bgColor = new Color(0xE7D19A);  // 0xE7D19A
		btnFgColor = new Color(0xFFFFFF);
		btnBgColor = new Color(0x990000);  // 0x990000
		headerColor = new Color(0xD0A760);  // 0xD0A760
		contents.setBackground(bgColor);
		
		JPanel help_panel = create_help_panel(size_X, y_bot);
		contents.add(help_panel);
		help_panel.setBounds(0, Y_ALL, size_X, y_bot);		
		
		control_panel = new ControlPanel(X_1, Y_ALL);
		contents.add(control_panel);
		control_panel.setBounds(0,0, X_1, Y_ALL);
		
		// added 2/8/13
		zoomPanel = new JPanel();
		zoomPanel.setLayout(null);  // Use Absolute layout.
		zoomPanel.setBounds(0, 0, X_2 + X_3 + 20, Y_ALL);	
		zoomPanel.setBackground(bgColor);
		// added
		
		student_panel = new StudentPanel();
		//contents.add(student_panel);		
		//student_panel.setBounds(X_1+20, 0, X_2, Y_ALL);
		zoomPanel.add(student_panel);
		student_panel.setBounds(0, 0, X_2, Y_ALL);
		
		question_panel = new QuestionPanel(X_3, Y_ALL);
		//contents.add(question_panel);
		zoomPanel.add(question_panel);
		question_panel.setBounds(X_2 + 20, 0, X_3, Y_ALL);		
        
		create_question_panel(X_2 + X_3 + 20, Y_ALL-20);
		jp_question.setBounds(0, 10, X_2 + X_3 + 20, Y_ALL-20);
		
		// added 2/8/13
	    q_layeredPane = new JLayeredPane();
		q_layeredPane.add(zoomPanel, new Integer(10));
		q_layeredPane.add(jp_question, new Integer(0));
		q_layeredPane.setBounds(X_1+20, 0, X_2 + X_3 + 20, Y_ALL);
		// added
		
		contents.add(q_layeredPane);
		//---------------------------------------------
		// Add a scroll pane around the contents
		//---------------------------------------------
		JScrollPane scrollPane = new JScrollPane(contents);
		scrollPane.setPreferredSize(new Dimension(X_1 + X_2 + X_3 + 40 + 40, size_Y));
			
		this.setLayout(new BorderLayout());
		this.add(scrollPane, BorderLayout.CENTER);
		this.setPreferredSize(new Dimension(X_1 + X_2 + X_3 + 40 + 40, size_Y));
		this.setMaximumSize(new Dimension(X_1 + X_2 + X_3 + 40 + 50, size_Y));
	   // titleFont = new Font(title3.getTitleFont().getName(), Font.BOLD, 14);
	}
		
	//=============================
	// Interface with mainWindow
	//=============================
	void setDefaultApacheDirectory(String s) {
		this.apacheDirectory.setText(s);
	}
	void addIPList(String s) {
		serverIP.addItem(s);
		if((s.equals(conf.getProperty("SERVER_IP"))))
			serverIP.setSelectedIndex(serverIP.getItemCount()-1); 
	}
	 
	void saveProperty()
	{
		try {		
			URL url = getClass().getClassLoader().getResource("resources/conf.properties");
			FileOutputStream fos = new FileOutputStream(url.getPath());
			// Setting a key=value pair
			/*String version = conf.getProperty("VERSION");
			String support_language = conf.getProperty("LANG");
            String dir = conf.getProperty("APA_DIR");
            String ip = conf.getProperty("SERVER_IP");
            String lang = conf.getProperty("SEL_LANG");*/
            
			String str="";
			str = (String)apacheDirectory.getText();			
			if(str != null && !str.equals(""))
			{
				String smileDir= str + mainWindow.dir_name2save;  // .../apacheDir/SMILE
				File f = new File (smileDir);
				if (f.exists()) {
					conf.setProperty("APA_DIR", str);
				}				
			}
			
			str = (String)serverIP.getSelectedItem();
			if(str != null && !str.equals(""))
				conf.setProperty("SERVER_IP", str);
			
			str = (String)spinner1.getSelectedItem();
			if(str != null && !str.equals(""))
				conf.setProperty("SEL_LANG", str);
			
			//conf.setProperty("LANG", support_language);
			//conf.setProperty("VERSION", version);
			conf.store(fos, "SMILE configuration data");
			fos.close();
		} catch(IOException e) {
		    e.printStackTrace();
		}
	}
	void setConnectEnabled(boolean b)  {btnConnect.setEnabled(b);}
	void setMakeEnabled(boolean b)     {btnMakeQuestion.setEnabled(b);}
	void setSolveEnabled(boolean b)    {btnStartSolve.setEnabled(b);}
	void setShowEnabled(boolean b)     {btnShowResult.setEnabled(b);}
	void setHelpMsg(String s)          {msgHelp.setText(s);}
	void setRefreshEnabled(boolean b)  {btnRefresh.setEnabled(b);}
	void setRetakeQuizEnabled(boolean b)  {btnRetakeQuiz.setEnabled(b);}
	void setStartSeesionEnabled(boolean b)  {btnStartSession.setEnabled(b);}
	boolean isReusePreviousQuestions() {return chkReuse.isSelected();}
	String getServerIP() { return (String)serverIP.getSelectedItem();}
	
	public void startSession()
	{
		create_not_available();
    	setTotalChanged();
    	msgHighRank.setText("");
    	chkReuse.setSelected(false);
    	questionDirectory.setText("");
    	qv_pane.setText("");
    	sc_pane.setText("");
    	setImportEnabled(true);
	}
	// set locales
	void set_locale(String chosen_language) {
		
		if (chosen_language.equals("Arabic")){
			cur_locale = new Locale("ar_AR");
			main.set_locale("ar_AR");
			data_source.set_locale("ar_AR");
			spinner1.setSelectedIndex(0); 
		} else if(chosen_language.equals("English")) {
			cur_locale = new Locale("");
			main.set_locale("");
			data_source.set_locale("");
			spinner1.setSelectedIndex(1); 
		} else if (chosen_language.equals("Portuguese")){
			cur_locale = new Locale("pt_PT");
			main.set_locale("pt_PT");
			data_source.set_locale("pt_PT");
			spinner1.setSelectedIndex(2); 
		} else if (chosen_language.equals("Spanish")){
			cur_locale = new Locale("es_ES");
			main.set_locale("es_ES");
			data_source.set_locale("es_ES");
			spinner1.setSelectedIndex(3); 
		} else if (chosen_language.equals("Swahili")){
			cur_locale = new Locale("sw_SW");
			main.set_locale("sw_SW");
			data_source.set_locale("sw_SW");
			spinner1.setSelectedIndex(4); 
		} else if (chosen_language.equals("Thai")){
			cur_locale = new Locale("th_TH");
			main.set_locale("th_TH");
			data_source.set_locale("th_TH");
			spinner1.setSelectedIndex(5); 
		} 
		
		data_source.set_lang(chosen_language);
		rb = ResourceBundle.getBundle("languages/MessageBundle", cur_locale);
		
		main.re_write_label_mw();
		data_source.re_write_label_ss();
				
		control_panel.re_write_labels(); 
		re_write_help_message();
		student_panel.re_write_label_sp();
		question_panel.re_write_label_qt();
		rewrite_btn_panel();	
		create_not_available();
	}
	
	void displayTopScore() {
	
		String s;
		
		s = rb.getString("H_SCORE"); s += ": ";
		s += data_source.getHighScore() + "\n";
		s += rb.getString("TOP_SCORER"); s += ": ";
		Vector<String> names = data_source.getTopScorers();
		
		for(int i=0;i<names.size(); i++) {
			s += names.elementAt(i);
			if (i!= (names.size()-1)) s+= ", ";
		}
		s += "\n";
		
        Formatter f = new Formatter();
        String rt = f.format(new String("%4.2f"), data_source.getHighRating()).toString();
		s += rb.getString("H_RATE"); s += ": ";
		s += rt + "\n";
		s += rb.getString("Q_OWNER"); s += ": ";
		names = data_source.getTopRankers();
		for(int i=0;i<names.size(); i++) {
			s += names.elementAt(i);
			if (i!= (names.size()-1)) s+= ", ";
		}
		s += "\n";
		
		msgHighRank.setText(s);
	}
	
	void setTotalChanged() {
		
		labNumStudent.setText(""+data_source.getNumStudents());
		labNumQuestion.setText(""+data_source.getNumQuestions());
		labNumAnswer.setText(""+data_source.getNumAnswers());
	}
   
	private boolean check_settings() {
			
		String apache = this.apacheDirectory.getText();
		File f = new File(apache);
		
		if (!f.isDirectory()) {			
			System.out.println("Invalid apache directory");
			setHelpMsg(rb.getString("ERROR_APACH_TOP"));
			JOptionPane.showMessageDialog(main.getFrame(), rb.getString("ERROR_APACH_TOP"));
			return false;		// not a valid directory
		}

		if (apache.endsWith("/"))
			apache = apache.substring(0, apache.length() -1);  // drop final '/'
		if (apache.endsWith("\\"))
			apache = apache.substring(0, apache.length() -1);  // drop final '\'
		
		String smileDir= apache + mainWindow.dir_name2save;  // .../apacheDir/SMILE
		f = new File (smileDir);
		if (!f.exists()) {
			setHelpMsg("The selected Apache directory does not contain the SMILE diectory. Please check if the setting for the Apache Directory is correct.");
			JOptionPane.showMessageDialog(main.getFrame(), smileDir + " directory does not exist. Please check if the setting for the SMILE Directory is correct.");
			return false;	
		}
		
		String pushMsg= smileDir + mainWindow.pushmsg;  // .../apacheDir/SMILE/pushmsg.php
		f = new File (pushMsg);
		if (!f.exists()) {
			setHelpMsg("The PUSH MSG script cannot be found. Please check if the SMILE Directory is correct.");
			JOptionPane.showMessageDialog(main.getFrame(), pushMsg + " file does not exist. Please check if the setting for the SMILE Directory is correct.");
			return false;	
		}
		
		String s = apache + mainWindow.subdir_name;   // ..../apacheDir/SMILE/current
		f = new File (s);
		if (!f.exists()) {
			setHelpMsg(s + " directory does not exist.");
			JOptionPane.showMessageDialog(main.getFrame(), s + " directory does not exist.");
			return false;
		 	/*try {
				f.mkdirs();
			} catch (Exception e) {
				System.out.println("Cannot create execution directory");
				setHelpMsg(rb.getString("ERROR_CREATE_JUNTION_TOP"));
			}*/
		}
		
		// set directory to main
		main.setDirectory(apache);
		
		if (!isReusePreviousQuestions()) data_source.clear();
				
		String s2 = s + "/MSG";
		f = new File (s2);
		if (!f.exists()) {
			setHelpMsg(s2 + " directory does not exist.");
			JOptionPane.showMessageDialog(main.getFrame(), s2 + " directory does not exist.");
			return false;
			/*try {
				f.mkdirs();
			} catch (Exception e) {
				System.out.println("Cannot create MSG directory");
				setHelpMsg(rb.getString("ERROR_CREATE_JUNTION_TOP"));
			}*/
		}
		
		// temp
		//String body = "No question available";
		create_not_available();
		/*
		String body = rb.getString("Q_NOT_AVAILABLE");
		f = new File (s + "/not_avail.html");
		
		try {
			FileWriter f0 = new FileWriter (f);
			f0.write(body);
			f0.close();
		} catch (IOException e) {
			
			e.printStackTrace();
		}*/
						
		return true;
	}
	
	public void create_not_available()
	{
		String body = rb.getString("Q_NOT_AVAILABLE");
		String apache = this.apacheDirectory.getText();
		String s = apache + mainWindow.subdir_name;
		File f = new File (s + "/not_avail.html");
		
		try {
			FileWriter f0 = new FileWriter (f);
			f0.write(body);
			f0.close();
		} catch (IOException e) {
			
			e.printStackTrace();
		}
	}
	
	void freezeSettings() {
		
		browseDir.setEnabled(false);
		apacheDirectory.setEnabled(false);
		serverIP.setEnabled(false);
		spinner1.setEnabled(false);
		
		/*chkReuse.setEnabled(false);
		browseDir2.setEnabled(false);
		questionDirectory.setEnabled(false);*/
				
	}
	
	public void setImportEnabled(boolean b)
	{
	   if(b)
	   {
			chkReuse.setEnabled(true);			
	   }
	   else
	   {
			chkReuse.setEnabled(false);			 		   
	   }
	}
	public void refreshTable() {
		
		Document doc = qv_pane.getDocument(); 
		doc.putProperty(Document.StreamDescriptionProperty, null); 
		tableQuestion.getSelectionModel().clearSelection();
		tableQuestion.invalidate();
		doc = sc_pane.getDocument(); 
		doc.putProperty(Document.StreamDescriptionProperty, null); 

		tableStudent.getSelectionModel().clearSelection();
		
	}

	//=========================================
	// Nested Class #1: Control Panel (LEFT)
	//=========================================
	
	class ControlPanel extends JPanel {
		
		// Border Variables
		JPanel 			subpane;
		JPanel			outpane;
		JPanel			setpane;
		
		// setting
		Border 			lowerededched1;
		TitledBorder 	title1;
		
		// activity flow
		Border 			lowerededched2;
		TitledBorder 	title2;
		
		JLabel 			lab0;
		JLabel 			lab1;
		
		// For LEFT PANE
		int Y_1 = 370;		// height of set
		int Y_2 = 300;		// height of flow
		//int Y_3 = 220;	// height of help
		
		JLabel lab2, lab3, lab4;
		int grid_init_X, grid_init_Y;
		int grid_unit_X, grid_unit_Y;
		
		public ControlPanel (int size_X, int size_Y) {
			
			super();
			this.setLayout(null);
			this.setSize(size_X, size_Y);
			this.setBackground(bgColor);
			
			int x_margin = 10;
			JPanel set_panel = create_set_panel(size_X - x_margin, Y_1);
			this.add(set_panel);
			
			JPanel flow_panel = create_flow_panel(size_X - x_margin, Y_2);
			this.add(flow_panel);
			
			int last_y = 0;
			set_panel. setBounds(new Rectangle(10, last_y + 10,  size_X - x_margin, Y_1)); last_y = last_y + 10 + Y_1;
			flow_panel.setBounds(new Rectangle(10, last_y + 10,  size_X - x_margin, Y_2)); last_y = last_y + 30 + Y_2; 
						
		}
		
		private void setGridInfo(int init_X, int init_Y, int unit_X, int unit_Y) {
			grid_init_X = init_X;
			grid_init_Y = init_Y;
			grid_unit_X = unit_X;
			grid_unit_Y = unit_Y;
		}
		
		private int gridX(double X) {return (int) (grid_init_X + grid_unit_X * X);}
		private int gridY(double Y) {return (int) (grid_init_Y + grid_unit_Y * Y);}
		
		public void re_write_labels() {
			
			title1 = BorderFactory.createTitledBorder(lowerededched1, rb.getString("SETTING"));			
			//title1.setTitleFont(titleFont);
			setpane.setBorder(title1);
			
			lab0.setText(rb.getString("SERVER_IP"));
			labB.setText(rb.getString("BROADCASTING_IP"));         
		    multicasting.setText(rb.getString("START"));
			lab1.setText(rb.getString("APA_DIR"));
			browseDir.setText(rb.getString("BROWSE"));
			
			chkReuse.setText(rb.getString("USED_Q_NOTICE"));
			lab4.setText(rb.getString("Q_DIR"));
			browseDir2.setText(rb.getString("BROWSE"));
			
			lab2.setText(rb.getString("SEL_LANG"));
			
			title2 = BorderFactory.createTitledBorder(lowerededched2, rb.getString("ACTIVITY_FLOW"));
			//title2.setTitleFont(titleFont);
			outpane.setBorder(title2);
			
			btnConnect.setText(rb.getString("CONNECT"));		        
			btnMakeQuestion.setText(rb.getString("START_MK_Q"));	
			btnStartSolve.setText(rb.getString("START_SV_Q"));			
			btnShowResult.setText(rb.getString("SR"));					
			
		}
		
		void stopMulticasting()
		{
			multicastingStart = false;
			multicasting.setText(rb.getString("START"));			
		}
		
		JPanel create_set_panel(int size_X, int size_Y) {
			
			subpane = new JPanel();
			setpane = new JPanel();
			setpane.setLayout(null);
			setpane.setBackground(bgColor);
			
			// Create Border	       
			lowerededched1 = BorderFactory.createEtchedBorder(EtchedBorder.LOWERED);
			title1 		  = BorderFactory.createTitledBorder(lowerededched1, rb.getString("SETTING"));
			setpane.setBorder(title1);

			// Setting Display with absolute view
			subpane.setLayout(null);
			subpane.setBackground(Color.WHITE);
			setpane.add(subpane);
			
			//-------------------------------------------------------------------------------------
			// create components
			//-------------------------------------------------------------------------------------
			//   server IP           [      ]
			//   working directory   <Browse>
			//   [                          ]
			//   ---------------------------
			//   [] use previous question 
			//   Directory           <Browse>
			//   ---------------------------
			//   Select a Target Language (Spinner)
			//   ---------------------------
			//   English                  Y
			//   ---------------------------
			
			lab0 = new JLabel(rb.getString("SERVER_IP"));				subpane.add(lab0);
			serverIP  = new JComboBox();								subpane.add(serverIP);
		    labB = new JLabel(rb.getString("BROADCASTING_IP"));         subpane.add(labB);
		    multicasting = new JButton(rb.getString("START"));  		subpane.add(multicasting);
			lab1 = new JLabel(rb.getString("APA_DIR"));					subpane.add(lab1);
			browseDir = new JButton(rb.getString("BROWSE"));			subpane.add(browseDir);
			apacheDirectory = new JTextField(  );						subpane.add(apacheDirectory);
			apacheDirectory.setEditable(false);
			
			multicasting.addActionListener(new ActionListener() {
				public void actionPerformed(ActionEvent arg0) {
										
					if (multicastingStart) {
						multicastingThread.stopMulticasting();
					}
					else 
					{
						multicastingStart = true;
						multicasting.setText(rb.getString("STOP"));
						try {
							multicastingThread = new MulticastingThread(control_panel);
							multicastingThread.start();
					    }   
					    catch (Exception e){
					        System.err.println("Couldn't start broadcasting.");
					        e.printStackTrace();
					    }
						
					}
				}
			});
			
			//Line1
			JPanel line1 = new JPanel(); 								subpane.add(line1);
			line1.setLayout(new BoxLayout(line1, BoxLayout.PAGE_AXIS));
			line1.add(new JSeparator(SwingConstants.HORIZONTAL));
			line1.setBackground(Color.WHITE);
			
			chkReuse = new JCheckBox(rb.getString("USED_Q_NOTICE"));	subpane.add(chkReuse);
			chkReuse.setBackground(Color.WHITE);
			lab4 = new JLabel(rb.getString("Q_DIR"));		    		subpane.add(lab4);
			questionDirectory = new JTextField("");						subpane.add(questionDirectory);			
			questionDirectory.setEnabled(false);
			questionDirectory.setEditable(false);
			browseDir2 = new JButton(rb.getString("BROWSE"));			subpane.add(browseDir2);
			chkReuse.setSelected(false);
			lab4.setEnabled(false);
			questionDirectory.setEnabled(false);
			browseDir2.setEnabled(false);

			//Line2
			JPanel line2 = new JPanel();								subpane.add(line2);
			line2.setLayout(new BoxLayout(line2, BoxLayout.PAGE_AXIS));
			line2.add(new JSeparator(SwingConstants.HORIZONTAL));
			line2.setBackground(Color.WHITE);
			
			lab2 = new JLabel(rb.getString("SEL_LANG"));				subpane.add(lab2);
			
			String lang = conf.getProperty("LANG");			
			String language_list[] = lang.split(",");			
			spinner1  = new JComboBox(language_list);	
			spinner1.setSelectedIndex(1); // English as default language
			subpane.add(spinner1);
			
			fc = new JFileChooser();
			fc.setFileSelectionMode(JFileChooser.DIRECTORIES_ONLY);
			
			browseDir.addActionListener(new ActionListener() {
				public void actionPerformed(ActionEvent arg0) {
					
					fc.setCurrentDirectory(new File(apacheDirectory.getText()));
					
					int returnVal = fc.showOpenDialog(MainPanel2.this);
					if (returnVal == JFileChooser.APPROVE_OPTION) {
			            File file = fc.getSelectedFile();
			            apacheDirectory.setText(file.getAbsoluteFile().toString());
					}
				}
			});
			
			browseDir2.addActionListener(new ActionListener() {
				public void actionPerformed(ActionEvent arg0) {
					
					fc.setCurrentDirectory(new File(main.getSaveDirectory()));
					boolean file_exists = true;
					
					int returnVal = fc.showOpenDialog(MainPanel2.this);
					if (returnVal == JFileChooser.APPROVE_OPTION) {
			            File dir = fc.getSelectedFile();
			            String filename = mainWindow.export_file_name;
			            
			            // check if exists
			            File f2 = new File(dir.getAbsolutePath() + "/" + filename);
			            if (!f2.exists()) 
			    		{ 	
			            	filename = mainWindow.export_file_oldname;
			    			f2 = new File(dir.getAbsolutePath() + "/" + filename);
			    			if (!f2.exists()) 
			    				file_exists = false;
			    		}
			            
						if (file_exists) {
							check_settings();
							questionDirectory.setText(dir.getAbsoluteFile().toString());
							boolean ret = main.import_questions(dir.getAbsoluteFile().toString(), filename);
							if(ret)
							{
								setImportEnabled(false); 
								browseDir2.setEnabled(false);
								questionDirectory.setEnabled(false);
								lab4.setEnabled(false);
								setSolveEnabled(true); 
								setStartSeesionEnabled(true); 
							}
							else 
							{
								setImportEnabled(true); 
								setMakeEnabled(false);				    		
							}
						}
						else {
							questionDirectory.setText("");
							setHelpMsg(rb.getString("ERROR_BROWSE_PREV"));
						} 
					}
				}
			});					
			
			serverIP.setEditable(true);
			serverIP.addActionListener(new ActionListener() {
				public void actionPerformed(ActionEvent a) {
					currentIP =  (String)serverIP.getSelectedItem();
					System.out.println("CurrentIP:" + currentIP + "\n" );
		
				}
			});		
			
			chkReuse.addActionListener(new ActionListener() {
				public void actionPerformed(ActionEvent a) {
					boolean b = chkReuse.isSelected();
					browseDir2.setEnabled(b);
					questionDirectory.setEnabled(b);
					lab4.setEnabled(b);
					if (!b) 
					{ 
						 setMakeEnabled(true);
						 //btnMakeQuestion.setText(rb.getString("START_MK_Q"));
					}
					else {
						setMakeEnabled(false);
						//btnMakeQuestion.setText(rb.getString("START_MK_Q_SKIP"));
					}
				}
			});
					
			spinner1.addActionListener(new ActionListener() {
				public void actionPerformed(ActionEvent a) {
					current_language = (String) spinner1.getSelectedItem();
					System.out.println("Current_language:" + current_language + "\n" );
					
					set_locale(current_language);					
				}
				
			});
			
			int x_gap_init = 5; // 10
			int y_gap_init = 10; // 20
			int x_gap = 5;
			int y_gap = 10;
			int x_size = 110; int grid_x_size = x_gap + x_size;
			int y_size = 25;
			setGridInfo(x_gap_init, y_gap_init, grid_x_size, y_size + y_gap);  // size of grid
			lab0.setBounds(gridX(0), gridY(0), x_size, y_size);  
			serverIP.setBounds(gridX(1), gridY(0), x_size, y_size);
			labB.setBounds(gridX(0), gridY(1), x_size, y_size); 
			multicasting.setBounds(gridX(1), gridY(1), x_size, y_size);
			lab1.setBounds( gridX(0), gridY(2), x_size, y_size); 
			browseDir.setBounds(gridX(1), gridY(2), x_size, y_size );
			apacheDirectory.setBounds( gridX(0), gridY(3), 2*x_size + x_gap, y_size);
			
			line1.setBounds(gridX(0), gridY(4), 2*x_size + x_gap, (int) (y_size*0.2));
			chkReuse.setBounds(gridX(0), gridY(4.3), 2*x_size + x_gap, y_size);
			lab4.setBounds(gridX(0), gridY(5.3), x_size, y_size); 
			browseDir2.setBounds(gridX(1), gridY(5.3), x_size, y_size);
			questionDirectory.setBounds( gridX(0), gridY(6.3), 2*x_size + x_gap, y_size);
			
			
			line2.setBounds(gridX(0), gridY(7.3), 2*x_size + x_gap, (int) (y_size*0.2));
			
			lab2.setBounds(gridX(0), gridY(7.6), 2*x_size + x_gap, y_size);
			spinner1.setBounds(gridX(0), gridY(8.6), x_size+30, y_size); // 
			setImportEnabled(false);
			
			// locate subpane inside outpane
			Insets i = title1.getBorderInsets(setpane); 
			subpane.setBounds(i.left, i.top, size_X - (i.left + i.right), size_Y - (i.top + i.bottom));
			
			return setpane;
		}
		
		JPanel create_flow_panel(int size_X, int size_Y) {
			
			// Create Border
			lowerededched2 = BorderFactory.createEtchedBorder(EtchedBorder.LOWERED);
			title2 = BorderFactory.createTitledBorder(lowerededched2, rb.getString("ACTIVITY_FLOW"));
   
			outpane = new JPanel();
			outpane.setLayout(null);
			outpane.setBorder(title2);
			outpane.setBackground(bgColor);
			
			JPanel subpane = new JPanel();
			subpane.setLayout(null);			
			subpane.setBackground(Color.WHITE);
			outpane.add(subpane);

			// arrows
			// arrow.png must be in the same directory as bin/edu/stanford
			ImageIcon icon = new ImageIcon(MainPanel2.class.getResource("arrow.png"));  
			 			 
			// Create Buttons
			btnConnect      = new JButton(rb.getString("CONNECT"));	  
			newButtonFont=new Font(btnConnect.getFont().getName(), Font.BOLD, btnConnect.getFont().getSize()); 			
			zoomBtnFont=new Font(btnConnect.getFont().getName(), Font.BOLD, btnConnect.getFont().getSize()); 
			btnConnect.setForeground(btnFgColor);
			btnConnect.setFont(newButtonFont);
			btnConnect.setBackground(btnBgColor);
			btnConnect.setBorderPainted(false);
			btnConnect.setOpaque(true);
			subpane.add(btnConnect);
			
			btnMakeQuestion = new JButton(rb.getString("START_MK_Q"));		
			btnMakeQuestion.setForeground(btnFgColor);
			btnMakeQuestion.setFont(newButtonFont);
			btnMakeQuestion.setBackground(btnBgColor);
			btnMakeQuestion.setBorderPainted(false);
			btnMakeQuestion.setOpaque(true);
			subpane.add(btnMakeQuestion);
			
			btnStartSolve   = new JButton(rb.getString("START_SV_Q"));			
			btnStartSolve.setForeground(btnFgColor);
			btnStartSolve.setFont(newButtonFont);
			btnStartSolve.setBackground(btnBgColor);
			btnStartSolve.setBorderPainted(false);
			btnStartSolve.setOpaque(true);
			subpane.add(btnStartSolve);
			
			btnShowResult   = new JButton(rb.getString("SR"));					
			btnShowResult.setForeground(btnFgColor);
			btnShowResult.setFont(newButtonFont);
			btnShowResult.setBackground(btnBgColor);
			btnShowResult.setBorderPainted(false);
			btnShowResult.setOpaque(true);
			subpane.add(btnShowResult);
			
			btnConnect.setEnabled(false);
			btnMakeQuestion.setEnabled(false);
			btnStartSolve.setEnabled(false);
			btnShowResult.setEnabled(false);
	
			btnConnect.addActionListener(new java.awt.event.ActionListener() {
				public void actionPerformed(java.awt.event.ActionEvent e) {
					if (check_settings())
					   main.try_connect(currentIP);
				}
			});
			
			btnMakeQuestion.addActionListener(new java.awt.event.ActionListener() {
				public void actionPerformed(java.awt.event.ActionEvent e) {
					main.do_state_make_question();
				}
			});
			
			btnStartSolve.addActionListener(new java.awt.event.ActionListener() {
				public void actionPerformed(java.awt.event.ActionEvent e) {
					main.do_state_solve_question(0);
				}
			});
			
			btnShowResult.addActionListener(new java.awt.event.ActionListener() {
				public void actionPerformed(java.awt.event.ActionEvent e) {
					main.do_state_show_results();
				}
			});
			
			JLabel arrow1 = new JLabel(icon);  subpane.add(arrow1);
			JLabel arrow2 = new JLabel(icon);  subpane.add(arrow2);
			JLabel arrow3 = new JLabel(icon);  subpane.add(arrow3);
			
			int x_gap_init = 15;
			int y_gap_init = 20;
			int x_gap = 0;
			int y_gap = 0;
			int x_size = 200; int grid_x_size = x_gap + x_size;
			int y_size = 35;
			setGridInfo(x_gap_init, y_gap_init, grid_x_size, y_size + y_gap);  // size of grid		
			btnConnect.setBounds(gridX(0), gridY(0), x_size, y_size);
			arrow1.setBounds(gridX(0.5) - 15, gridY(1), 30, y_size);
			btnMakeQuestion.setBounds(gridX(0), gridY(2), x_size, y_size);
			arrow2.setBounds(gridX(0.5) - 15, gridY(3), 30, y_size);
			btnStartSolve.setBounds(gridX(0), gridY(4), x_size, y_size);
			arrow3.setBounds(gridX(0.5) - 15, gridY(5), 30, y_size);
			btnShowResult.setBounds(gridX(0), gridY(6), x_size, y_size);

			// locate subpane inside outpane
			Insets i = title2.getBorderInsets(outpane); 
			subpane.setBounds(i.left, i.top, size_X - (i.left + i.right), size_Y - (i.top + i.bottom));
			return outpane;
		}
		
	}  // end of control panel

	JPanel create_help_panel(int size_X, int size_Y) {
		
		helppane = new JPanel();
		helppane.setLayout(null);
		helppane.setBackground(bgColor);
		
		// Create Border
		lowerededched3 = BorderFactory.createEtchedBorder(EtchedBorder.LOWERED); //BorderFactory.createLoweredBevelBorder();
		title3 = BorderFactory.createTitledBorder(lowerededched3, rb.getString("HELP_MSG_TITLE"));	
		helppane.setBorder(title3);
		
		msgHelp = new JTextArea(rb.getString("WELCOME_MSG"));
		msgHelp.setEditable(false);
		msgHelp.setLineWrap(true);
		msgHelp.setFont(msgHelp.getFont().deriveFont(13.0f));
		
		JScrollPane jsp = new JScrollPane();
		jsp.setViewportView(msgHelp);
		
		Insets i = title3.getBorderInsets(helppane);
		int y = size_Y - (i.top + i.bottom);
		int x = size_X - (i.left + i.right);
					
		ImageIcon icon = new ImageIcon(getClass().getClassLoader().getResource("resources/icon.png"));
		//JLabel lbl = new JLabel(scale(icon.getImage(), y-1, y-1)); 
		JButton lbl = new JButton(scale(icon.getImage(), y-1, y-1));
		lbl.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent arg0) {
				String os = System.getProperty("os.name").toLowerCase();
				System.out.println(os);
				if (os.indexOf("win") >= 0) 				// windows
				   showStatusOnWindows();
				else showStatusOnUnix();
			}
		});
		jsp.setBounds(i.left, i.top, x-y-15, y);	
		helppane.add(jsp);
		lbl.setBounds(x-y-10, i.top-1, y, y);
		helppane.add(lbl);
		
		return helppane;
	}	
	
	private String get_available_IP() {
    	// Find available IPs. 
		String str="";
		InetAddress ip;
		String host="not connected";
		
		try {
 
			ip = InetAddress.getLocalHost();
			host = ip.getHostAddress();
			System.out.println("Current IP address : " + host);
 
		} catch (UnknownHostException e) {
			e.printStackTrace(); 
		}
		
    	try {
			Enumeration<NetworkInterface> e = NetworkInterface.getNetworkInterfaces();
			while (e.hasMoreElements()) {
				NetworkInterface ni = e.nextElement();				
				Enumeration<InetAddress> ips = ni.getInetAddresses();
				while (ips.hasMoreElements()) {
					String ipa =  ips.nextElement().toString();
					
					if (ipa.startsWith("/"))
						ipa = ipa.substring(1);
					
					if (ipa.indexOf(':') >= 0) {  // IPv6. Ignore
						continue;
					}
					
					if (ipa.equals("127.0.0.1")) {
						continue;		// loopback IP. Not meaningful for out purpose
					}
					
					if (ipa.equals(host)) {
					   str += ipa + " (connected) \n";
					}
					else str += ipa + "\n";
				}
			}
		
    	} catch (SocketException e) {
			
			e.printStackTrace();
		}
    	if (str.isEmpty())
    		str="network is not available.\n";
    	return str;
    }
	
	private String checkApacheDir()
	{
	   String apache = this.apacheDirectory.getText();
	   File f = new File(apache);
	
	   if (!f.isDirectory()) {		
			return apache + " is not a valid apache directory.";		// not a valid directory
	   }
	   
	   return "Apache Directory: " + apache;
	}
	
	public void showStatusDialog(String msg) {
		// create and configure a text area - fill it with exception text.
	    JTextArea textArea = new JTextArea();
		textArea.setFont(new Font("Sans-Serif", Font.PLAIN, 12));
		textArea.setEditable(false);
		textArea.setText(msg);
		
		// stuff it in a scrollpane with a controlled size.
		JScrollPane scrollPane = new JScrollPane(textArea);		
		scrollPane.setPreferredSize(new Dimension(450, 250));
		
		// pass the scrollpane to the joptionpane.				
		JOptionPane.showMessageDialog(this, scrollPane);
	}
	
	private String showPortOnWindows()
	{
		String line="";
		String port="";
		String command="netstat -o -n -a";
		String pid="";
		String pids="";
		try {
		      //Process p = Runtime.getRuntime().exec(new String[] {"cmd", "-c", "netstat -o -n -a | findstr 0.0:80"});	
			Process proc;
			BufferedReader in;	
			String line1="";
		      Process p = Runtime.getRuntime().exec(command);
		      BufferedReader input =new BufferedReader(new InputStreamReader(p.getInputStream()));
		      while ((line = input.readLine()) != null) {
		    	  if(line.indexOf("0.0:80") >-1) 
		    	  {
		              port += line + "\n";
		              pid = line.substring(line.lastIndexOf(" "));
		              proc = Runtime.getRuntime().exec("tasklist /FI \"PID eq " + pid + "\"");
				      in =new BufferedReader(new InputStreamReader(proc.getInputStream()));

				      while ((line1 = in.readLine()) != null) {		    	  
				              pids += line1 + "\n";
				      }
				      in.close();
		    	  }
		      }
		      input.close();
		      		      
		} catch (IOException ioe) {
		    ioe.printStackTrace();
		}
		port = "Processes that use the Port 80:\n" + port + "\n" + pids;
		return port;
	}
	private void showStatusOnWindows()
	{
		String msg = "Network inofrmation:\n" + get_available_IP() + "\n";
		String apache="";
		String line="";
		try {
		    Process proc = Runtime.getRuntime().exec("wmic.exe");
		    BufferedReader input = new BufferedReader(new InputStreamReader(proc.getInputStream()));
		    OutputStreamWriter oStream = new OutputStreamWriter(proc.getOutputStream());
		    oStream .write("process where name='httpd.exe'");
		    oStream .flush();
		    oStream .close();
		    while ((line = input.readLine()) != null) {
		    	line = line.trim();
			    if(line.length() >0){
			    	if(line.length()>80)
			    	   apache += line.substring(0, 80) + "\n";
			    	else apache += line + "\n";
		    	}
		    }
		    input.close();
		} catch (IOException ioe) {
		    ioe.printStackTrace();
		}
		if(apache.isEmpty())
			apache = "Apache is not running.";
		msg = msg + "Apache information:\n" + checkApacheDir() + "\n\n" + apache + "\n\n" + showPortOnWindows() + "\n\n";
		showStatusDialog(msg);  
	}
	
	private void showStatusOnUnix()
	{
		//String command="ps -ef | grep httpd";
		String msg = "Network inofrmation:\n" + get_available_IP() + "\n";
		String command="pgrep httpd";
		String apache="Apache is not running.";
		try {
		      String line;
		      Process p = Runtime.getRuntime().exec(command);
		      BufferedReader input =new BufferedReader(new InputStreamReader(p.getInputStream()));
		      while ((line = input.readLine()) != null) {
		        apache = "Apache is running and the pid is " + line;
		      }
		      input.close();
		    }catch (Exception err) {
		      err.printStackTrace();
		}		
		msg = msg + "Apache information:\n" + checkApacheDir() + "\n\n" + apache + "\n\n";
		showStatusDialog(msg);   
	}
	
	private ImageIcon scale(Image img, int w, int h) {       
        int type = BufferedImage.TYPE_INT_RGB;
        BufferedImage dst = new BufferedImage(w, h, type);
        Graphics2D g2 = dst.createGraphics();
        g2.drawImage(img, 0, 0, w, h, null);
        g2.dispose();
        return new ImageIcon(dst);
    }
	
	public void re_write_help_message() {
		
		title3 = BorderFactory.createTitledBorder(lowerededched3, rb.getString("HELP_MSG_TITLE"));	
		//title3.setTitleFont(titleFont);
		helppane.setBorder(title3);
		
		msgHelp.setText(rb.getString("LANG_CHANGE_NOTICE"));
		msgHelp.setText(rb.getString("HELP_FIRST"));
		
	}
	
	
	//=====================================================================================
	// Nested Class #2: Score View Panel
	//=====================================================================================
	//JEditorPane sc_pane;
	
	class StudentPanel extends JPanel {
	
		int Y_SCB = 300;
		JPanel jp_st;
		JScrollPane jsp;
		Insets i_set;
		
		Border lowerededched4;
		TitledBorder title4;
		
		Border lowerededched5;
		TitledBorder title5;
		
		ListSelectionModel lsm;
		
		JPanel j_ss, s_outpane;
				
		JLabel l1_s;
		JLabel l2_q;
		JLabel l3_a;
		
		JPanel jp_sv;
		Border lowerededched6;
		TitledBorder title6;
		
		JScrollPane jsp_sv;
		Insets i_sv; 
		
		JPanel jp_h_sv;
		Border lowerededched7;
		TitledBorder title7;
		
		JScrollPane jsp_h_sv;
		Insets i_h_sv; 
		
		
		public StudentPanel () {
			
			super();
			this.setLayout(null);
			this.setSize(X_2, Y_ALL);
			this.setBackground(bgColor);
			
			JPanel table_panel = create_student_table_panel();
			this.add(table_panel);			
			
			JPanel stat_panel = create_stat_panel();
			stat_panel.setBackground(bgColor);
			this.add(stat_panel);
			
			JPanel score_paenl = create_score_view_panel();
			score_paenl.setBackground(bgColor);
			this.add(score_paenl);
			
			int Y_HIGH = 100;
			JPanel high_panel = create_high_score_view_panel(Y_HIGH);
			high_panel.setBackground(bgColor);
			this.add(high_panel);
		
			int last_y = 0;
			table_panel. setBounds(new Rectangle(10, last_y + 10,  X_2 - 10, Y_A)); 
			last_y = last_y + 10 + Y_A;
			
			int Y_stat = 50;
			
			stat_panel. setBounds(new Rectangle(10, last_y ,  X_2 - 10, Y_stat)); 
			last_y = last_y + Y_stat;
			
			score_paenl. setBounds(new Rectangle(10, last_y + 5 ,  X_2 - 10, Y_SCB)); 
			last_y = last_y + Y_SCB + 5;
			
			high_panel. setBounds(new Rectangle(10, last_y + 5 ,  X_2 - 10, Y_HIGH)); 
			last_y = last_y + Y_HIGH + 5;			
		
		}
		
		public void re_write_label_sp() {
			
			title4 = BorderFactory.createTitledBorder(lowerededched4, rb.getString("SS_TITLE"));
			//title4.setTitleFont(titleFont);
			jp_st.setBorder(title4);
			
			title5 = BorderFactory.createTitledBorder(lowerededched5, rb.getString("TOTAL"));
			//title5.setTitleFont(titleFont);
			s_outpane.setBorder(title5);	
			
			l1_s.setText(rb.getString("STUDENT"));
			l2_q.setText(rb.getString("QUESTION"));
			l3_a.setText(rb.getString("ANSWER"));
			
			title6 = BorderFactory.createTitledBorder(lowerededched6, rb.getString("SCORE_BOARD"));
			//title6.setTitleFont(titleFont);
			jp_sv.setBorder(title6);
			
			title7 = BorderFactory.createTitledBorder(lowerededched7, rb.getString("TOP_SCORER"));
			//title7.setTitleFont(titleFont);
			jp_h_sv.setBorder(title7);
			
		}
		
		// student table panel
		JPanel create_student_table_panel() {
			
			jp_st = new JPanel();
			jp_st.setLayout(null);
			lowerededched4 = BorderFactory.createEtchedBorder(EtchedBorder.LOWERED);
			title4 = BorderFactory.createTitledBorder(lowerededched4, rb.getString("SS_TITLE"));
			jp_st.setBorder(title4);
			jp_st.setBackground(bgColor);
			
			// added on 8/10/2012
			TableModel student_model = data_source.getTableModelForStudent();
			tableStudent = new JTable(student_model);
			JTableHeader header = tableStudent.getTableHeader();
		    header.setBackground(headerColor);
		    //header.setForeground(Color.WHITE);
		      
	        RowSorter<TableModel> sorter1 = new TableRowSorter<TableModel>(student_model);
	        tableStudent.setRowSorter(sorter1);
	        
			for (int i = 0; i < tableStudent.getColumnCount(); i++) {
				TableColumn column = tableStudent.getColumnModel().getColumn(i);
				
			    if (i == 0) {
			    	column.setPreferredWidth(100); // Name			    	
			    }			    
			    else {
			        column.setPreferredWidth(50); // Other
			    }
			}
						
			lsm = tableStudent.getSelectionModel();
			lsm.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
			lsm.addListSelectionListener(new ListSelectionListener() {
				public void valueChanged(ListSelectionEvent e) {

					int i = tableStudent.getSelectionModel().getMaxSelectionIndex();
					//studentTableSelected(i);
				}
			});
				
			tableStudent.addMouseListener(new java.awt.event.MouseAdapter() {
				public void mouseClicked(java.awt.event.MouseEvent e) {
					int row = tableStudent.rowAtPoint(e.getPoint());
				    int i = tableStudent.getSelectedRow();

				    if(row ==i)
				    	studentTableSelected(row);
				}				
			});
			
			jsp = new JScrollPane();
			jsp.setViewportView(tableStudent);
			i_set = title4.getBorderInsets(jp_st); 
			jsp.setBounds(i_set.left-0,i_set.top, X_2-24, Y_A-26); // X_2-18,
			jsp.setBackground(Color.WHITE);
			jsp.setOpaque(true);
			jsp.getViewport().setBackground(Color.WHITE);
			
			jp_st.add(jsp, null);
			return jp_st;
		}
		
		JPanel create_stat_panel() {
			s_outpane = new JPanel();
			s_outpane.setLayout(null);
						
			j_ss = new JPanel();
			j_ss.setLayout(null);
			j_ss.setBackground(Color.WHITE);
			
			lowerededched5 = BorderFactory.createEtchedBorder(EtchedBorder.LOWERED); // BorderFactory.createLoweredBevelBorder();
			title5 = BorderFactory.createTitledBorder(lowerededched5, rb.getString("TOTAL"));
			//j_ss.setBorder(title5);					
			s_outpane.setBorder(title5);
			s_outpane.setBackground(bgColor);
			s_outpane.add(j_ss);
			
			l1_s = new JLabel(rb.getString("STUDENT"));
			l2_q = new JLabel(rb.getString("QUESTION"));
			l3_a = new JLabel(rb.getString("ANSWER"));
			
			labNumStudent  = new JLabel("0");
			labNumQuestion = new JLabel("0");
			labNumAnswer   = new JLabel("0");
			
			j_ss.add(l1_s); 
			j_ss.add(l2_q);
			j_ss.add(l3_a);
			j_ss.add(labNumStudent);
			j_ss.add(labNumQuestion);
			j_ss.add(labNumAnswer);
			
			int x_begin = 15;
			int x_space = 40; int x_size_lab = 70; int x_size_num = 20; int Y = 10;
			
			l1_s.setBounds           (x_begin, 5, x_size_lab, Y); x_begin+= x_size_lab +2;
			labNumStudent.setBounds(x_begin, 5, x_size_num, Y); x_begin+= x_size_num + x_space; 
			l2_q.setBounds           (x_begin, 5, x_size_lab, Y); x_begin+= x_size_lab +2;
			labNumQuestion.setBounds(x_begin, 5, x_size_num, Y); x_begin+= x_size_num + x_space; 
			l3_a.setBounds           (x_begin, 5, x_size_lab, Y); x_begin+= x_size_lab +2;
			labNumAnswer.setBounds (x_begin, 5, x_size_num, Y); x_begin+= x_size_num + x_space; 
			
			// locate subpane inside outpane
			Insets i = title5.getBorderInsets(s_outpane); 
			j_ss.setBounds(i.left + 1, i.top, X_2 - 13 - (i.left + i.right), 48 - (i.top + i.bottom));
			return s_outpane;			
		}
		
		JPanel create_score_view_panel() {
			
			jp_sv = new JPanel();
			jp_sv.setLayout(null);
			
			lowerededched6 = BorderFactory.createEtchedBorder(EtchedBorder.LOWERED);
			title6 = BorderFactory.createTitledBorder(lowerededched6, rb.getString("SCORE_BOARD"));
			jp_sv.setBorder(title6);

			sc_pane = new JEditorPane();
			sc_pane.setEditable(false);
			
			// add scroll panel
			jsp_sv = new JScrollPane();
			jsp_sv.setViewportView(sc_pane);
			i_sv = title6.getBorderInsets(jp_sv); 
			jsp_sv.setBounds(i_sv.left,i_sv.top, X_3-12 - i_sv.left - i_sv.right , Y_SCB - i_sv.top - i_sv.bottom );
			jp_sv.add(jsp_sv, null);
			
			return jp_sv;				
		}
		
		JPanel create_high_score_view_panel(int height) {
			
			jp_h_sv = new JPanel();
			jp_h_sv.setLayout(null);
			lowerededched7 = BorderFactory.createEtchedBorder(EtchedBorder.LOWERED);
			title7 = BorderFactory.createTitledBorder(lowerededched7, rb.getString("TOP_SCORER"));
			jp_h_sv.setBorder(title7);	
			
			msgHighRank = new JTextArea();
			msgHighRank.setEditable(false);
			msgHighRank.setLineWrap(true);
			msgHighRank.setFont(msgHelp.getFont().deriveFont(13.0f));
			//msgHighRank.setBackground(jp_h_sv.getBackground());
			msgHighRank.setBackground(Color.WHITE);
			
			jsp_h_sv = new JScrollPane();
			jsp_h_sv.setViewportView(msgHighRank);
			i_h_sv = title7.getBorderInsets(jp_h_sv); 
			jsp_h_sv.setBounds(i_h_sv.left,i_h_sv.top, X_3-12 - i_h_sv.left - i_h_sv.right , height - i_h_sv.top - i_h_sv.bottom );
			jp_h_sv.add(jsp_h_sv, null);			
			
			return jp_h_sv;
		}
		
	}

	JEditorPane qv_pane;	
	//=====================================
	// Nested Class #3: Question View Panel
	//=====================================
	class QuestionPanel extends JPanel {
		
		// definition of variables
		int Y_qv;
		int Y_btn;
		int last_y;
		
		JPanel table_panel;
		JPanel qview_panel;
		JPanel btn_panel;
		
		JPanel jp_qt;
		Border lowerededched8;
		TitledBorder title8;
		
		JScrollPane jsp_qt;
		Insets i_qt; 
		
		JPanel jp_qv;
		Border lowerededched9;
		TitledBorder title9;
		
		JScrollPane jsp_qv;
		Insets i_qv; 
		
		public QuestionPanel (int size_X, int size_Y) {
			
			super();
			this.setLayout(null);
			this.setSize(size_X, size_Y);
			this.setBackground(bgColor);
			
			Y_qv = 400;
			Y_btn = 50;
			
			table_panel = create_question_table_panel(size_X-10, Y_A);
			this.add(table_panel);
			qview_panel = create_question_view_panel(size_X-10, Y_qv);
			this.add(qview_panel);
			btn_panel = create_btn_panel(size_X-10, Y_btn);
			this.add(btn_panel);	
		
			last_y = 0;
			table_panel. setBounds(new Rectangle(10, last_y + 10,  size_X - 10, Y_A)); 
			last_y = last_y + 10 + Y_A;

			qview_panel. setBounds(new Rectangle(10, last_y + 10,  size_X - 10, Y_qv)); 
			last_y = last_y + 10 + Y_qv;
			
			btn_panel. setBounds(new Rectangle(10, last_y + 10,  size_X - 10, Y_btn)); 
			last_y = last_y + 10 + Y_btn;

		}
		
		public void re_write_label_qt() {
			
			title8 = BorderFactory.createTitledBorder(lowerededched8, rb.getString("Q_STAT"));
			//title8.setTitleFont(titleFont);
			jp_qt.setBorder(title8);
			
			title9 = BorderFactory.createTitledBorder(lowerededched9, rb.getString("Q"));
			//title9.setTitleFont(titleFont);
			jp_qv.setBorder(title9);
			
		}
		
		JPanel create_question_table_panel(int sz_X, int sz_Y) {
			
			jp_qt = new JPanel();
			jp_qt.setLayout(null);
			lowerededched8 = BorderFactory.createEtchedBorder(EtchedBorder.LOWERED);
			title8 = BorderFactory.createTitledBorder(lowerededched8, rb.getString("Q_STAT"));
			jp_qt.setBorder(title8);
			jp_qt.setBackground(bgColor);
			
			// added on 8/10/2012
			TableModel model = data_source.getTableModelForQuestion();
			tableQuestion = new JTable(model);
			tableQuestion.setOpaque(true);
			tableQuestion.setBackground(Color.WHITE);
			
			JTableHeader header = tableQuestion.getTableHeader();
		    header.setBackground(headerColor);
	        RowSorter<TableModel> sorter = new TableRowSorter<TableModel>(model);
	        tableQuestion.setRowSorter(sorter);
	        
	        // set default sorted column on "Rating"
	        //sorter.toggleSortOrder(3);sorter.toggleSortOrder(3);
	        ArrayList<RowSorter.SortKey> sortKeys = new ArrayList<RowSorter.SortKey>();	        
	        //    sortKeys.add(new RowSorter.SortKey(iColumnIndex, SortOrder.ASCENDING));	        
	        sortKeys.add(new RowSorter.SortKey(3, SortOrder.DESCENDING));
	        sorter.setSortKeys(sortKeys);
	        
			for (int i = 0; i < 4; i++) {
				TableColumn column = tableQuestion.getColumnModel().getColumn(i);
			    if (i == 1) {
			        column.setPreferredWidth(100); // Name
			    } else {
			        column.setPreferredWidth(50); // Other
			    }
			}
						
			final ListSelectionModel lsm = tableQuestion.getSelectionModel();
			lsm.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
			lsm.clearSelection();
			
			lsm.addListSelectionListener(new ListSelectionListener() {
				public void valueChanged(ListSelectionEvent e) {
					int i = lsm.getMaxSelectionIndex();
					//questionTableSelected(i);
				}
			});
			
			tableQuestion.addMouseListener(new java.awt.event.MouseAdapter() {
				public void mouseClicked(java.awt.event.MouseEvent e) {
					int row = tableQuestion.rowAtPoint(e.getPoint());
				    int i = tableQuestion.getSelectedRow();
				    //showStatusDialog("selected: "+i+", clicked: "+row);
				    if(row ==i)
				       questionTableSelected(row, qv_pane);
				}				
			});
			
			jsp_qt = new JScrollPane();
			jsp_qt.setViewportView(tableQuestion);
			i_qt = title8.getBorderInsets(jp_qt); 
			jsp_qt.setBounds(i_qt.left-0,i_qt.top, sz_X-18, sz_Y-26);
			jsp_qt.setBackground(Color.WHITE);
			jsp_qt.setOpaque(true);
			jsp_qt.getViewport().setBackground(Color.WHITE);
			
			jp_qt.add(jsp_qt, null);
			return jp_qt;
			
		}
		
		JPanel create_question_view_panel(int sz_X, int sz_Y) {
			
			jp_qv = new JPanel();
			jp_qv.setLayout(null);
			jp_qv.setBackground(bgColor);
			
			lowerededched9 = BorderFactory.createEtchedBorder(EtchedBorder.LOWERED);
			title9 = BorderFactory.createTitledBorder(lowerededched9, rb.getString("Q"));
			jp_qv.setBorder(title9);
            
			qv_pane = new JEditorPane();
			qv_pane.setEditable(false);
						
			jsp_qv = new JScrollPane();
			jsp_qv.setViewportView(qv_pane);
			i_qv = title9.getBorderInsets(jp_qv); 
			//jsp_qv.setBounds(i_qv.left,i_qv.top, sz_X-12 - i_qv.left - i_qv.right + 6, sz_Y - i_qv.top - i_qv.bottom );
						
			// added 2/8/13
			jsp_qv.setBounds(0,0, sz_X-12 - i_qv.left - i_qv.right + 6, sz_Y - i_qv.top - i_qv.bottom );
			Font f = new Font("Dialog", Font.BOLD, 10);
			JButton zoom = new JButton("+"); 
			zoom.setToolTipText("Maximize");
			zoom.setFont(f);
			zoom.setForeground(btnFgColor);
			zoom.setBackground(btnBgColor);
			//zoom.setBorderPainted(false);  // for Mac only
			zoom.setOpaque(true);
			zoom.setMargin(new Insets(0, 0, 0, 0));			
			zoom.setBounds(i_qv.left+(sz_X-12 - i_qv.left - i_qv.right -21),0, 20, 20 );
			zoom.addActionListener(new ActionListener() {
				public void actionPerformed(ActionEvent arg0) {
					int i = tableQuestion.getSelectedRow();
					if(i!=-1)
					  questionTableSelected(i, question_pane);
					q_layeredPane.setLayer(jp_question, new Integer(20));	
				}
			});
			
			JLayeredPane layeredPane = new JLayeredPane();
			layeredPane.add(jsp_qv, new Integer(10));
			layeredPane.add(zoom, new Integer(20));
			layeredPane.setBounds(i_qv.left,i_qv.top, sz_X-12 - i_qv.left - i_qv.right + 6, sz_Y - i_qv.top - i_qv.bottom );
			jp_qv.add(layeredPane, null);
			//// added
			
			//jp_qv.add(jsp_qv, null);
			
			return jp_qv;			
		}
		
		JPanel create_btn_panel(int sz_X, int sz_Y)
		{
			JPanel jp = new JPanel();
			//jp.setLayout(null);
			jp.setBackground(bgColor);
			
			btnRetakeQuiz = new JButton(rb.getString("RETAKE_QUIZ"));  
			btnRetakeQuiz.setForeground(btnFgColor);
			btnRetakeQuiz.setFont(newButtonFont);
			btnRetakeQuiz.setBackground(btnBgColor);
			btnRetakeQuiz.setBorderPainted(false);
			btnRetakeQuiz.setOpaque(true);
			jp.add(btnRetakeQuiz);
			btnRetakeQuiz.addActionListener(new ActionListener() {
				public void actionPerformed(ActionEvent arg0) {
					main.retakeQuiz();	
				}
			});
						
		    btnStartSession = new JButton(rb.getString("START_NEW_SESSION")); 
		    btnStartSession.setForeground(btnFgColor);
		    btnStartSession.setFont(newButtonFont);
		    btnStartSession.setBackground(btnBgColor);
		    btnStartSession.setBorderPainted(false);
		    btnStartSession.setOpaque(true);
			jp.add(btnStartSession);
		    btnStartSession.addActionListener(new ActionListener() {
				public void actionPerformed(ActionEvent arg0) {
					main.startSession();	
				}
			});
		    
		    btnRetakeQuiz.setEnabled(false);
			btnStartSession.setEnabled(false);
			
			btnRefresh = new JButton("Refresh"); //jp.add(btnRefresh);
			btnRefresh.setEnabled(false);
			
			btnRefresh.addActionListener(new ActionListener() {
				public void actionPerformed(ActionEvent arg0) {
					main.doRefresh();	
				}
			});
			
			int btn_sz = 140;
			//int btn_gap = 40;
			int btn_y = 30;
			int gap_y = 5;
			btnRefresh.setBounds(30 ,gap_y, btn_sz, btn_y);
			//btnSave.setBounds(30 + btn_sz + btn_gap,gap_y, 150, btn_y);	
			return jp;
			
		}
		
	}	
	
	public void rewrite_btn_panel()
	{
		btnRetakeQuiz.setText(rb.getString("RETAKE_QUIZ"));
		btnStartSession.setText(rb.getString("START_NEW_SESSION"));
	}
	// Called when user selected question i
	void questionTableSelected(int i, JEditorPane editorPane) {
		
		if (i == -1) {
			editorPane.setText(rb.getString("Q_NOT_AVAILABLE"));
			editorPane.repaint();
			return;
		}

		try {
			// To force a document reload it is necessary to clear the stream description property of the document. 			
			Document doc = editorPane.getDocument();
			doc.putProperty(Document.StreamDescriptionProperty, null);
			   
			if (tableQuestion.getModel().getRowCount() == 0) { // 08/07/2012
				String path = main.getDirectory() + "/not_avail.html";
				File f = new File(path);
					URL u = f.toURI().toURL();					
					editorPane.setPage(u);					
					editorPane.repaint();
			}
			
			// convert coordinates from JTable to that of the underlying model after sorting
			int selection = tableQuestion.convertRowIndexToModel(i);
			             
			String path = main.getDirectory() + "/"+selection+"_result.html";
            File f = new File(path);
			URL u = f.toURI().toURL();
			editorPane.setPage(u);				    
			editorPane.repaint();
							
		} catch (Exception e) {
			System.out.println("No page 2");
			editorPane.setText(rb.getString("Q_NOT_AVAILABLE"));
		}		
	}
	
	// called when user selects student i
	void studentTableSelected(int i) {
		
		if (i<0) {
			sc_pane.setText(rb.getString("S_NOT_AVAILABLE"));
			return;
		}
		
		if (!data_source.isAnswerFinished(i)) {
			sc_pane.setText(rb.getString("S_NOT_AVAILABLE"));
			return;
		}
		
		int selection = tableStudent.convertRowIndexToModel(i);  
        
		try {
			// To force a document reload it is necessary to clear the stream description property of the document. 			
			Document doc = sc_pane.getDocument();
			doc.putProperty(Document.StreamDescriptionProperty, null);
			
			String path = main.getDirectory() + "/score_"+selection+".html";
			File f = new File(path);
			URL u = f.toURI().toURL();
			
			sc_pane.setPage(u);
			
		} catch (Exception e) {
			sc_pane.setText(rb.getString("S_NOT_AVAILABLE"));
		}

		
	}
	
	JPanel create_question_panel(int sz_X, int sz_Y) {
		
	    jp_question = new JPanel();
		jp_question.setLayout(null);
		jp_question.setBackground(bgColor);
		
		Border border = BorderFactory.createEtchedBorder(EtchedBorder.LOWERED);
		TitledBorder title = BorderFactory.createTitledBorder(border, rb.getString("Q"));
		jp_question.setBorder(title);
        
		question_pane = new JEditorPane();
		Font f = new Font("Dialog", Font.PLAIN, 16);
		question_pane.setFont(f);
		question_pane.setEditable(false);
					
		JScrollPane sp = new JScrollPane();
		sp.setViewportView(question_pane);
		Insets insets = title.getBorderInsets(jp_question); 

		sp.setBounds(0,0, sz_X-12 - insets.left - insets.right + 6, sz_Y - insets.top - insets.bottom );
		JButton back = new JButton("_"); 
		back.setToolTipText("Minimize");
		Font font = new Font("Dialog", Font.BOLD, 10);
		back.setFont(font);
		back.setForeground(btnFgColor);
		back.setBackground(btnBgColor);
		//zoom.setBorderPainted(false);
		back.setOpaque(true);
		back.setMargin(new Insets(0, 0, 0, 0));	
		back.setBounds(insets.left+(sz_X-12 - insets.left - insets.right -21),0, 20, 20 );
		back.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent arg0) {
				q_layeredPane.setLayer(jp_question, new Integer(0));	
			}
		});
		
		JLayeredPane layeredPane = new JLayeredPane();
		layeredPane.add(sp, new Integer(10));
		layeredPane.add(back, new Integer(20));
		layeredPane.setBounds(insets.left,insets.top, sz_X-12 - insets.left - insets.right + 6, sz_Y - insets.top - insets.bottom );
		jp_question.add(layeredPane, null);
		
		return jp_question;			
	}
	
	public class MulticastingThread extends Thread {
		 
		private long TWO_SECONDS = 2000;
		private long broadcasting_time = 2000*30*5;  // FIVE_MINUTES
		boolean start = false;
		protected MulticastSocket socket = null;
	    InetAddress group;
	    DatagramPacket packet;  
	    ControlPanel cp;
	    
	    public MulticastingThread(ControlPanel panel) throws IOException {
	        super("MulticastingThread");
	        cp = panel;
	        start = true;
	        socket = new MulticastSocket(4445);
	        group = InetAddress.getByName("224.0.0.251");  // IPv4 multicast address
	        socket.joinGroup(group);
	    }
	 
	    public void run() {
	    	System.out.println("Multicasting start");
	        while (start) {
	            try {
	                byte[] buf = new byte[256];
	 
	                String dString = (String) serverIP.getSelectedItem();	                
	                buf = dString.getBytes();
	 
	                // send it
	                packet = new DatagramPacket(buf, buf.length, group, 4445);
	                System.out.println("Multicasting for IP " + dString);
	                socket.send(packet);
	 
	                // sleep for a while
	                try {
	                    sleep(TWO_SECONDS);	                    
	                } catch (InterruptedException e) { }
	                
	                broadcasting_time -= TWO_SECONDS; 
	                if (broadcasting_time <=0)
	                	start = false;
	                
	            } catch (IOException e) {
	                e.printStackTrace();
	                start = false;
	            }	            
	        }	        
	        socket.close();
	        cp.stopMulticasting();
	        System.out.println("Multicasting end");
	    }
	    
	    public void stopMulticasting(){ 
	      start = false;
	    }
	 	 
	}

}
