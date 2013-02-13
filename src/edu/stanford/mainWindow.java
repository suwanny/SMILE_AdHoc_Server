/*===================================================================================
  Developed by Sunmi Seol
  File Name: MainWindow.java
  Version: 2.1
  Modified Time: 08.03.2012
======================================================================================*/


package edu.stanford;

import java.awt.Dimension;
import java.awt.Toolkit;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.io.BufferedInputStream;
import java.io.File;
import java.io.FilenameFilter;
import java.net.InetAddress;
import java.net.NetworkInterface;
import java.net.SocketException;
import java.util.Enumeration;
import java.util.Locale;
import java.util.ResourceBundle;

import javax.swing.ImageIcon;
import javax.swing.JFileChooser;
import javax.swing.JFrame;
import javax.swing.JOptionPane;
import javax.swing.JLabel;
import org.json.JSONArray;

//---------------------------------------------------------------
// STATUS
//   INIT   : before start the game
//   WAIT   : wait students to join
//   MAKE   : wait students to make a question
//   SOLVE  : wait students to solve questions
//   FINISHED : wait students to review the results
//---------------------------------------------------------------

public class mainWindow extends WindowAdapter implements ActionListener {

	//private String Default_IP = "192.168.2.3";  // XP netbook
	// private String Default_IP = "192.168.2.4";  // windows7 laptop
    // 1. program
	//String directory = "C:/SMILE/current";
    // 2. actual server (Hard coding, it should be fixed)
	//String directory = "C:/server/apache/htdocs/SMILE/current"; // for Windows7 
	//String directory = "D:/Apache/htdocs/SMILE/current";        // for XP netbook
	String default_apache_top = "C:/Server/apache/htdocs";
	String directory = default_apache_top + subdir_name;
	String save_directory = default_apache_top + dir_name2save; 
		
	public final static int STATE_INIT     = 0;
	public final static int STATE_WAIT     = 1;
	public final static int STATE_MAKE     = 2;
	public final static int STATE_SOLVE    = 3;
	public final static int STATE_RESULT   = 4;
	public final static int STATE_FINISHED = 5;
	
	public final static String export_file_name       = "smile_export.csv";
	public final static String export_file_oldname    = "jq_export.txt";
	public final static String total_result_file_name = "total_result.html";
	public final static String subdir_name            = "/SMILE/current";
	public final static String dir_name2save          = "/SMILE";
	public final static String pushmsg                = "/pushmsg.php";
	
	int curr_state = STATE_INIT;
	boolean questions_modified = true;
	boolean isQuestionModified() {return questions_modified;}
	void setQuestionModified(boolean b) {questions_modified = b;}
	
	final static float REFRESH_TIME = 2.00f;		// unit is seconds, for initial connect stages
	final static float CHECK_TIME   = 2.00f;		// unit is seconds, for later stages
	
	StudentStatus myStudentStatus = null;  
    MainPanel2    mp = null;
    int      	  retake_quiz;
    
    HttpMsgForTeacher communicator = null;
    
    static ResourceBundle cur_rb;
    static Locale		  cur_locale;
    
	// for local debug.
	// if test mode, no communication is done. But dummy function calls are made
	// to emulate the user inputs.

	boolean is_test_mode = false;
	void setTestMode(boolean b) {
		is_test_mode = b;
	}
	
	boolean isTestMode() {return is_test_mode;}
		
    public static void main(String[] args) {
    	mainWindow main = new mainWindow();
		
    	set_default();
    	main.createWindow();
    }

    public String getDirectory() {return directory;} // returns full named directory (apache dir + /SMILE/Current)
    public String getSaveDirectory() {return save_directory;} // returns full named directory (apache dir + /SMILE)
    public JFrame getFrame() {return frame;} 
  
    public void setDirectory(String apache_top) {  // called from mainPanel when 'freezing' the setting
    	
    	default_apache_top = apache_top;
    	directory = default_apache_top + subdir_name;
    	System.out.println("New Top Directory:"+directory);
    	
    	if (myStudentStatus != null) {
        	myStudentStatus.setDirectory(directory);
    	}
    	if (communicator != null) {
    		communicator.setDirectory(directory);
    	}
    }
    
   private boolean saveCurrentQuestionsToDirectory()
    {
    	JFileChooser fc = new JFileChooser();
    	fc.setFileSelectionMode(JFileChooser.DIRECTORIES_ONLY);
    	fc.setCurrentDirectory(new File(getSaveDirectory()));
		
    	int returnVal = fc.showSaveDialog(frame);
		if (returnVal == JFileChooser.APPROVE_OPTION) {
			// check if over-writing
			
			File dir = fc.getSelectedFile();
			System.out.println("Selected = " + dir);
           	
			File f2 = new File(dir.getAbsolutePath() + "/" + export_file_name);
			if (f2.exists()) {
				int yn = JOptionPane.showConfirmDialog(
                        frame, 
                        cur_rb.getString("WARN_NOTICE"),
                        cur_rb.getString("WARN"),
                        JOptionPane.YES_NO_OPTION);
				
				if ((yn != JOptionPane.YES_OPTION) &&
					(yn != JOptionPane.OK_OPTION))
					return false; // don't save
			}
			
			//Fetching information from the current directory (current)
			// call export_data() to save questions as csv format
			if (!myStudentStatus.export_data(getDirectory(),dir.getAbsolutePath(), export_file_name))
			{
				mp.setHelpMsg(cur_rb.getString("ERROR_BROWSE_PREV"));
				return false;
			}
			
			myStudentStatus.save_total_result(
				getDirectory(),
				dir.getAbsolutePath(),
				total_result_file_name);
			
			return true;
		}
		else 
			return false;
				
    }
    
    void saveCurrentQuestions()
    {
    	if (this.isQuestionModified() && (myStudentStatus.getNumQuestions() != 0))
    	{
    		// Make a window to ask if want to save questions
    		int n = JOptionPane.showConfirmDialog(
    			    frame,
    			    cur_rb.getString("Q_SAVE"),
    			    cur_rb.getString("SQ_TITLE"),
    			    JOptionPane.YES_NO_OPTION);
    		
    		if (n==JOptionPane.YES_OPTION) {
    			saveCurrentQuestionsToDirectory();
    		} 
    	}	   
    }
    
    public void windowClosing(WindowEvent e)
    {
    	boolean close_application = true;
    	// check problem should be saved
    	if (this.isQuestionModified() && (myStudentStatus.getNumQuestions() != 0))
    	{
    		// Make a window to ask if want to save questions
    		int n = JOptionPane.showOptionDialog(frame,
    				cur_rb.getString("Q_SAVE"),
    				cur_rb.getString("SQ_TITLE"),
    				JOptionPane.YES_NO_CANCEL_OPTION,
    				JOptionPane.QUESTION_MESSAGE,
    				null,null, null);
    		
    		if (n==2) {
    			// user canceled. Cancel exit 
    			close_application = false;
    		} else if (n==1) {
    			// user said no. Close Application
    			close_application = true;
    			
    		} else {
    			// find a directory to choose
    			boolean save_success = saveCurrentQuestionsToDirectory();
    			if (save_success) {
    				close_application = true;
    			} else {
        			// user canceled. Cancel exit
    				close_application = false; 			
    			}
    		}
    	} 
    	
    	if (close_application) {
    		// no question added. Just exit
    		communicator.clean_up();
    		mp.saveProperty();
    		frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
    	} else {
			frame.setDefaultCloseOperation(JFrame.DO_NOTHING_ON_CLOSE);
    	}
    }
   
   static void set_default () {
	   cur_locale = new Locale(""); //English
	   cur_rb = ResourceBundle.getBundle("languages/MessageBundle", cur_locale);
   }
    
   void set_locale (String re_locale) {
	   cur_locale = new Locale (re_locale);
	   cur_rb = ResourceBundle.getBundle("languages/MessageBundle", cur_locale);
   }
    
   public void re_write_label_mw() {
    	if(mp != null && mp.conf != null)
    	   frame.setTitle(cur_rb.getString("SMILE_TITLE") + " - " + mp.conf.getProperty("VERSION")); 
        else frame.setTitle( cur_rb.getString("SMILE_TITLE"));
    }
   
    JFrame frame;
    private  void createWindow() {
    	
    	frame = new JFrame(cur_rb.getString("SMILE_TITLE"));
    	frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
    	frame.addWindowListener(this);
    	
    	//frame.setUndecorated(true);
        //AWTUtilities.setWindowOpaque(frame, false); 
    	/*ImageIcon logo;
    	JLabel label1;
        java.net.URL imgURL = getClass().getClassLoader().getResource("resources/mainback.png");
        
    	    if (imgURL != null) {
    	        logo = new ImageIcon(imgURL, "LOGO");
    	         label1= new JLabel(logo);
    	    } else {
    	    	 label1= new JLabel("LOGO");
    	    }
    	
    	    frame.getContentPane().add(label1);
    	    
    	ImageIcon img = new ImageIcon(getClass().getClassLoader().getResource("resources/icon.png"));
    	frame.setIconImage(img.getImage());
    	frame.pack();
    	// center the jframe on screen
        frame.setLocationRelativeTo(null);
    	frame.setVisible(true);
    	
    	try{
    		  //do what you want to do before sleeping
    		  Thread.sleep(1500);  //sleep for 2000 ms
    		  //do what you want to do after sleeptig
    		}
    		catch(InterruptedException ie){
    		//If this thread was intrrupted by nother thread 
    		}*/
    		
    	myStudentStatus = new StudentStatus();
    	mp = new MainPanel2(this, myStudentStatus);
    	//mp.setDefaultApacheDirectory(default_apache_top);
    	
    	//frame.getContentPane().removeAll();    	
    	//frame.getContentPane().invalidate();
    	    	
    	//frame.setVisible(false);
    	//frame.getContentPane().remove(label1);
    	frame.getContentPane().add(mp);
    	frame.getContentPane().setPreferredSize(mp.getPreferredSize());
    	frame.getContentPane().setMaximumSize(mp.getMaximumSize());
    	frame.setMaximumSize(frame.getContentPane().getMaximumSize()); // max size does not work.
    	frame.setTitle(cur_rb.getString("SMILE_TITLE") + " - " + mp.conf.getProperty("VERSION"));  
    	
    	//Display the window.
    	frame.pack();
    	
    	Toolkit toolkit =  Toolkit.getDefaultToolkit ();
    	Dimension screen = toolkit.getScreenSize();
    	Dimension psize = mp.getPreferredSize();
    	//Dimension msize = mp.getMaximumSize();
    	
    	if (psize.height > screen.height)
    	{
    		psize.height = screen.height - 30;    	
    	    frame.setSize(psize);
    	}
    	
    	frame.setLocationRelativeTo(null);
    	frame.setVisible(true);
        
    	set_available_IP();
    	do_state_init();
    	
    	communicator = new HttpMsgForTeacher(this);
    	communicator.setStudentStatus(myStudentStatus);
    	retake_quiz = 0;
    	
    	//-------------------------------------------
    	// for test
    	//-------------------------------------------
    	//setTestMode(true);
    }
    
    private void set_available_IP() {
    	// Find available IPs. 
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
					
					mp.addIPList(ipa);
					System.out.println(ipa+",");
				}
			}
		
    	} catch (SocketException e) {
			
			e.printStackTrace();
		}
    	
    }
        
    // called from message
    public void SaveQuestion(String ip, String n, String q, String o1, String o2, String o3, String o4, 
    		String answer, byte[] img)
    {
      	boolean has_img = false;
      	
      	//int student_id = myStudentStatus.findName(n); When using user name
      	int student_id = myStudentStatus.findIP(ip);
      	if (student_id == -1) {
      		//System.out.println("Invalid_name:" + n);
      		System.out.println("Invalid_ip:" + ip);
      		return;
      	}
      	
      	if (this.curr_state != STATE_MAKE )
      	{
      		// ignore this question.
      		return;
      	}
      	
      	if (img != null) {
    		has_img = true;
      	}
      	
    	System.out.println("Saving Question");
      	int qid = myStudentStatus.SaveQuestion(ip, n, q, o1, o2, o3, o4, answer, has_img, false);
      	
      	if (has_img) {
           if (qid != -1) {  // added on 7/26/2012 to fix duplicate questions problem
         	  System.out.println("Saving image");
         	  myStudentStatus.SaveImage(qid, img);
           }
      	}
      	
      	setQuestionModified(true);
       	mp.setTotalChanged();
      	//mp.setSaveEnabled(true);  // as soon as one question is available, question can be saved.
      	
    }
    
    // called from communicator
    public void SaveAnswer(String ip, String n, JSONArray _answer, JSONArray _rating) {
    	if (this.curr_state != STATE_SOLVE )
      		return;
    	
    	myStudentStatus.SaveAnswer(ip, n, _answer, _rating);
    	mp.setTotalChanged();
    }
    
    public void doRefresh() {
    	System.out.println("refresh");
    	mp.refreshTable();
    }
    
    public void retakeQuiz() {
    	System.out.println("Retake the Quiz from mainWindow");   
    	retake_quiz++;
    	myStudentStatus.retakeQuiz();
    	delete_question_files();
    	myStudentStatus.create_question_files();
    	mp.setTotalChanged();
    	mp.msgHighRank.setText("");
    	mp.qv_pane.setText("");
    	do_state_solve_question(retake_quiz);
    }
    
    public void startSession() {
    	System.out.println("Start a new session from mainWindow"); 
    	boolean restart=true;
    	
    	//saveCurrentQuestions();
    	
    	if (this.isQuestionModified() && (myStudentStatus.getNumQuestions() != 0))
    	{
	    	int n = JOptionPane.showOptionDialog(frame,
					cur_rb.getString("Q_SAVE"),
					cur_rb.getString("SQ_TITLE"),
					JOptionPane.YES_NO_CANCEL_OPTION,
					JOptionPane.QUESTION_MESSAGE,
					null,null, null);
			
			if (n==2) {
				// user canceled. Cancel exit 
				restart = false;
			} else if (n==1) {
				// user said no. Close Application
				restart = true;
				
			} else {
				// find a directory to choose
				boolean save_success = saveCurrentQuestionsToDirectory();
				if (save_success) {
					restart = true;
				} else {
	    			// user canceled. Cancel exit
					restart = false; 			
				}
			}   	
    	}
    	
		if(restart)
		{
	    	retake_quiz=0;
	    	myStudentStatus.startSession();
	    	delete_student_files();
	    	mp.startSession();
	    	setQuestionModified(false);
	    	do_state_wait(true);
		}
    }
    
    void delete_student_files()
    {
        File current_dir = new File(getDirectory());
        File student_files[] = current_dir.listFiles(
    			new FilenameFilter() {
    				public boolean accept(File dir, String name)
    				{
    				   if (name.endsWith("html"))    				  
    						  return true;
    				   return false;
    				}
    			}
    			
    		);
    		
    	for(int i=0;i<student_files.length;i++)
    	{
    		student_files[i].delete();  						
    	}    	
    }

    void delete_question_files()
    {
        File current_dir = new File(getDirectory());
        File score_files[] = current_dir.listFiles(
    			new FilenameFilter() {
    				public boolean accept(File dir, String name)
    				{
    				   if (name.startsWith("score"))    				  
    						  return true;
    				   return false;
    				}
    			}
    			
    		);
    		
    	for(int i=0;i<score_files.length;i++)
    	{
    		score_files[i].delete();  						
    	}
    	
/*    	File result_files[] = current_dir.listFiles(
    			new FilenameFilter() {
    				public boolean accept(File dir, String name)
    				{
    				   if (name.endsWith("result.html"))    				  
    						  return true;
    				   return false;
    				}
    			}
    			
    		);
    		
    	for(int i=0;i<result_files.length;i++)
    	{
    		result_files[i].delete();  						
    	}*/
    }
    
    public boolean import_questions(String from_dir, String filename) {
    	//myStudentStatus.clear();
    	myStudentStatus.startSession();
    	delete_student_files();
    	mp.create_not_available();
    	
    	boolean ret;
    	
    	if(filename.endsWith("csv")){
    	    ret = myStudentStatus.import_data(directory, from_dir, filename);
        }else{
        	ret = myStudentStatus.import_questions(directory, from_dir, filename);
        }
    	
    	if (ret) {
    		System.out.println ("Total=" + myStudentStatus.getNumQuestions());
    		setQuestionModified(true);
    	}
    	
		mp.setTotalChanged();
		doRefresh();
      	return ret;
    }

    void do_state_init()
    {
    	curr_state = STATE_INIT;
    	mp.setConnectEnabled(true);
    	mp.setMakeEnabled(false);
    	mp.setSolveEnabled(false);
    	mp.setHelpMsg(cur_rb.getString("HELP_FIRST"));
     }
    
    void do_state_wait(boolean new_session)
    {
    	curr_state = STATE_WAIT;
    	mp.setConnectEnabled(false);
    	
    	/*if (mp.isReusePreviousQuestions()) {
    		mp.setMakeEnabled(false);
    		mp.setSolveEnabled(true);  		
     	} else {
    		mp.setMakeEnabled(true);
    		mp.setSolveEnabled(false);
    	}*/
    	
    	mp.setImportEnabled(true);  // added on 08/21/2012
    	mp.setMakeEnabled(true);
		mp.setSolveEnabled(false);
    	mp.setShowEnabled(false);
    	mp.freezeSettings();
    	mp.setRefreshEnabled(true);
    	mp.setRetakeQuizEnabled(false); 
    	mp.setStartSeesionEnabled(false);  
    	mp.setHelpMsg(cur_rb.getString("WAIT_NOTICE"));

        if(new_session)
           communicator.start_a_new_session();
        
    	if (isTestMode()) {  // test mode add dummy persons
    		do_test(STATE_WAIT);
     	}    	
    
    }
    
    // called when timer expired
    
    public void actionPerformed(ActionEvent evt) {
    	/*
        if ((curr_state != STATE_INIT) && (curr_state != STATE_FINISHED)) {
        	communicator.do_query();
        	if (curr_state == STATE_WAIT)
        		start_timer(REFRESH_TIME);
        	//else 
        		//start_timer(CHECK_TIME);
        }
        */
    }
         
    public void do_state_make_question()
    {
    	if (myStudentStatus.getNumStudents() == 0) {
    		JOptionPane.showMessageDialog(frame, cur_rb.getString("WAIT_NOTICE"));
    		return;
    	}
    	
    	curr_state = STATE_MAKE; 
    	mp.setImportEnabled(false);  // added on 8/21/2012
    	mp.setConnectEnabled(false);
    	mp.setMakeEnabled(false);
    	mp.setSolveEnabled(true);
    	mp.setShowEnabled(false);
 	   	mp.setHelpMsg(cur_rb.getString("MK_Q_NOTICE"));
 	    mp.setRetakeQuizEnabled(false); 
   	    mp.setStartSeesionEnabled(true); 
 	    communicator.send_make_question(null);  // NULL means to all students. 
 	   
 	  	if (isTestMode()) {  // test mode add dummy persons
 	  		do_test(STATE_MAKE);
 	  	}
     }
    
    public void do_state_solve_question(int retake)
    {
    	if (myStudentStatus.getNumQuestions() == 0) {
    		mp.setHelpMsg(cur_rb.getString("SV_Q_WARNING"));
    		return;
    	}
    	
    	curr_state = STATE_SOLVE; 
    	mp.setConnectEnabled(false);
    	mp.setMakeEnabled(false);
    	mp.setSolveEnabled(false);
    	mp.setShowEnabled(true);
    	if (retake > 0)
    		mp.setHelpMsg(cur_rb.getString("RETAKE_QUIZ_NOTICE")+" ("+retake+")");
    	else
 	   	    mp.setHelpMsg(cur_rb.getString("SV_Q_NOTICE"));
 	    mp.setRetakeQuizEnabled(true); 
   	    mp.setStartSeesionEnabled(true); 
 	   communicator.send_solve_question(retake);
 	   doRefresh();
 	   
 	   if (this.isTestMode()) {
 		   do_test(curr_state);
 	   }
    }
    
    public void do_state_show_results() {
    	curr_state = STATE_RESULT;
    	mp.setConnectEnabled(false);
    	mp.setMakeEnabled(false);
    	mp.setSolveEnabled(false);
    	mp.setShowEnabled(false);
 	   	mp.setHelpMsg(cur_rb.getString("SR_NOTICE"));
 	    mp.setRetakeQuizEnabled(true); 
   	    mp.setStartSeesionEnabled(true); 
 	   	myStudentStatus.finalize_results();
 	   	communicator.send_show_results();
 	    mp.displayTopScore();
 	   doRefresh();
 	   	
  	   if (this.isTestMode()) {
 		   do_test(STATE_RESULT);
 	   } 	   	
    }
    
    
    //---------------------------------------------------------------------
    // Functions called by message received
    //---------------------------------------------------------------------

    // msg received, when a new client is attached to the network
    //public void student_reply(MessageTarget ID, String name)
/*    
    public void student_reply(String ID, String name, Integer seq)
    {
    	if (curr_state == STATE_WAIT) 
    	{
    		if (myStudentStatus.AddStudent(name)) {  // if new student 
    			mp.setTotalChanged();
    		}
    	}
    	
    	myStudentStatus.ReceivedPongNumber(name, seq);
    }
  */
    
    // new student. Or re-enter student
    public void student_hail(String IP, String name) 
    {
		if (myStudentStatus.AddStudent(name, IP)) {  // if new student (same IP regardless of name)
			mp.setTotalChanged();
			communicator.setStudentRegistered(name, IP);
		} 
    }

    // called when connect button is pressed
    public void try_connect(String ip_addr) {
    	//boolean success = communicator.create_connection(ip_addr);
    	//if (success) {
    	//	
    	//}

    	String str = mp.getServerIP();
		if(str == null || str.equals(""))
		{
			JOptionPane.showMessageDialog(frame, cur_rb.getString("NETWORK_WARNING"));
		}
		else {
    	   do_state_wait(false); // freeze directory    	
    	   communicator.begin();
		}
    }
    
    void do_test(int state) {
    	
    	if (state == STATE_WAIT) {
        // 	reply_name("Thomas");
    	//	reply_name("Percy");
    	//reply_name("James");
    	}
    	if (state == STATE_MAKE) {
	  		
 	  		byte buf[] = new byte[1024*1024*1];  // file size is smaller than 16MB
 	  		byte buf2[] = null;
 	  		boolean ok = true;
 	  		
 	  		System.out.println("111"); //System.out.flush();
 	  		try {
 	  			BufferedInputStream input = new BufferedInputStream(
 	  				mainWindow.class.getResourceAsStream("test_img1.jpg"));
 	  		
 	  			System.out.println("222"); //System.out.flush();
 	  			int sz = input.read(buf, 0, 1*1024*1024);
 	  			buf2 = new byte[sz];
 	  			for(int i = 0; i < sz; i++)
 	  				buf2[i] = buf[i];
 	  			
 	  			System.out.println("333"); //System.out.flush();
 	  		} catch(Exception e ) {
 	  			ok = false;
 	  		}
 	  		
 	  		System.out.println("444"); //System.out.flush();
 	  	
 	  		SaveQuestion("192.168.2.3","Thomas", 
 	  						"What is the number of Thomas", 
 	  						"No. 1", "No. 3", "NO. 5", "No. 99", "1",
 	  						//null);
 	  						(ok? buf2 : null));
 	  		
 	  		SaveQuestion("192.168.2.5","James", 
						"What colour is James", 
						"Blue", "Green", "Red", "Black", "3", null);	  				   
    	}
    	
    	if (state == STATE_SOLVE ) {
   		  JSONArray received_answer = new JSONArray();
 		  JSONArray received_rating = new JSONArray();
 		  received_answer.put(new String("1"));
 		  received_answer.put(new String("2"));
 		  
 		  received_rating.put(new String("5"));
 		  received_rating.put(new String("2"));
 		  SaveAnswer("", "Thomas", received_answer, received_rating);
 		  
 		  received_answer = new JSONArray();
 		  received_rating = new JSONArray();
 		  received_answer.put(new String("3"));
 		  received_answer.put(new String("3"));
 		  
 		  received_rating.put(new String("5"));
 		  received_rating.put(new String("4"));
 		  SaveAnswer("", "James", received_answer, received_rating);		  
 		  
 		  received_answer = new JSONArray();
 		  received_rating = new JSONArray();
 		  received_answer.put(new String("1"));
 		  received_answer.put(new String("4"));
 		  
 		  received_rating.put(new String("1"));
 		  received_rating.put(new String("3"));
 		  SaveAnswer("", "Percy", received_answer, received_rating);
    		
    	
    	}
    }
    
 }