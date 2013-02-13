/*===================================================================================
  Developed by Sunmi Seol
  Modified by Chi-Hou Vong
  File Name: HttpMsgForTeacher.java
  Version: 2.1
  Modified Time: 08.03.2012
======================================================================================*/


package edu.stanford;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.FileWriter;
import java.io.FilenameFilter;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.util.*;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.json.JSONTokener;

public class HttpMsgForTeacher extends Thread {
	
	int           _msec;  // interval
	boolean       finished;
	String        dir;
	File          msg_dir;
	mainWindow    main;
	StudentStatus s_status;
	
	public HttpMsgForTeacher(mainWindow _main) {
		
		main     = _main;
		finished = false;
		dir      = main.directory +"/MSG";
		msg_dir  = new File(dir);
		_msec    = 250; // per msec
		
	}
	
	synchronized public void setDirectory(String directory) {
		dir = directory + "/MSG";
		msg_dir = new File(dir);
	}
	
	public void setInterval(int msec)             {_msec = msec;}
	public void setStudentStatus(StudentStatus s) {s_status = s;}
	
	public void run() {
		
		while(!finished) {
			process_message_from_files();
			try {
				sleep(_msec);
			} catch (InterruptedException e) {}
		}
	}
	
	synchronized void process_message_from_files() {
		// get all files in directory 
		File msgfiles[] = msg_dir.listFiles(
			new FilenameFilter() {
				public boolean accept(File dir, String name)
				{
					if (name.startsWith("MSG"))
						return true;
					return false;
				}
			}
			
		);
		
		for(int i=0;i<msgfiles.length;i++)
		{
			if (finished) return;
			
			JSONObject msg = readMessageFromFile(msgfiles[i]);
			
			if (msg!=null) {
				receivedMessage(msg);
				/* For Debugging
				try {
					copyFile(msgfiles[i], File.createTempFile("tttt",".txt", msg_dir));

				} catch (IOException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
				*/
				msgfiles[i].delete();
			}
						
		}
	}
	
	void copyFile(File src, File dest)
	{
		try {
			InputStream in = new FileInputStream(src);
			OutputStream out = new FileOutputStream(dest);
			byte[] buf = new byte[1024];
			int len;
			while ((len = in.read(buf))>0) {
				out.write(buf, 0, len);
			}
			in.close();
			out.close();
		} catch (FileNotFoundException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}

		
	}
	
	// Revised in 7/13/2012
	JSONObject readMessageFromFile(File f) {
		
		BufferedReader r = null;
		if (f.isDirectory()) return null;
		System.out.println("Reading : " + f.toString());
	/*	RandomAccessFile raf=null;
		
	    try {	    	
	       raf = new RandomAccessFile(f, "rw");
	       FileLock fl = raf.getChannel().tryLock();	
	       if(fl != null) {
	          fl.release();
	       }
	       else {
	    	   System.out.println("MSG file is being written. Sleep and wait.");
	    	   sleep(_msec);
	       }
	       raf.close();
	    } catch (Exception e) {
	    	if (raf!=null) {
				try {raf.close(); } catch(Exception e2) {}}	
		   e.printStackTrace();
	    }*/
	    
		try {

			/* for debug
			String s;
			r = new BufferedReader (new InputStreamReader( new FileInputStream(f),"UTF-8"));
			while ( (s = r.readLine()) != null) {
				System.out.println("UTF8:" + s);
			}
			r.close();		
			*/
			
			r = new BufferedReader (new InputStreamReader( new FileInputStream(f),"UTF-8"));
			JSONObject o =  new JSONObject( new JSONTokener( r ));
			r.close();

			return o;
		
		} catch (JSONException e) {
			
			if (r!=null) {
				try {r.close(); } catch(Exception e2) {}}
			e.printStackTrace();	
		} catch (Exception e1) {
			// TODO Auto-generated catch block
			e1.printStackTrace();
		}
		return null;
	}
	
	void sendMessage(JSONObject obj) {
		File f = new File(dir+"/smsg.txt");
		try {
			BufferedWriter o = new BufferedWriter(new FileWriter(f));
			o.write(obj.toString());
			o.close();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
	
	void exportStatus(JSONObject obj, String IP)
	{
		File f = new File(dir+"/"+IP+".txt");
		try {
			BufferedWriter o = new BufferedWriter(new FileWriter(f));
			o.write(obj.toString());
			o.close();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
	public void setStudentRegistered(String name, String IP)
	{
		try {
			JSONObject o = new JSONObject();
			o.put("NAME",name);
			o.put("MADE","N");
			o.put("SOLVED","N");
			exportStatus(o, IP);
		}catch (Exception e){
			e.printStackTrace();
		}
	}
	public void setStudentQuestioned(String name, String IP)
	{
		try {
			JSONObject o = new JSONObject();
			o.put("NAME",name);
			o.put("MADE","Y");
			o.put("SOLVED", "N");
			exportStatus(o, IP);
		}catch (Exception e){
			e.printStackTrace();
		}
	}
	public void setStudentAnswered(String name, String IP, JSONArray saved_answers)
	{
		try {
			JSONObject o = new JSONObject();
			o.put("NAME",name);
			o.put("MADE","Y");
			o.put("SOLVED", "Y");
			o.put("NUMQ", new Integer(s_status.getNumQuestions()));
			o.put("YOUR_ANSWERS", saved_answers);
			exportStatus(o, IP);
		}catch (Exception e){
			e.printStackTrace();
		}
	}
	
	
	//----------------------------------------------------------------------
	// old methods from Junction Teacher
	//----------------------------------------------------------------------
	synchronized public void clean_up() {
		finished = true;
		
		File msgfiles[] = msg_dir.listFiles();
		if(msgfiles != null) {  // added on 8/6/2012
		for(int i=0;i<msgfiles.length;i++)
		{
			msgfiles[i].delete();
			//System.out.println("deleting " + msgfiles[i].toString());
		}
		}
		//sent_init_message(); 
	}
	
	public void begin()
	{
		this.start();		// start fetching messages
		sent_init_message();
	}
	
	public void sent_init_message()
	{
		JSONObject msg = new JSONObject();
		try {
			msg.put("TYPE", "WAIT_CONNECT");
		} catch (JSONException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		sendMessage(msg);
	}
	
	public void send_make_question(String t)
	{
		
		JSONObject msg1 = new JSONObject();
		try {
			msg1.put("TYPE", "START_MAKE");
		
			sendMessage(msg1);
			System.out.println("Sending START_MAKE to all");
			
		} catch (Exception e) {
			System.out.println("Exception during sending make question: "  + e);
		}
	} 
	
	public void send_solve_question(int retake)
	{
		
		JSONObject sendmsg = new JSONObject();
		int numofq = s_status.getNumQuestions();
		JSONArray ranswers;
		ranswers = new JSONArray();
		
		ranswers = s_status.getAnswers(); 
				
		try {
			System.out.println("RESending START SOLVE");
			
			if(retake > 0)
			{			   
			   System.out.println("RE_TAKE");    
			   Vector<String> student_names = s_status.getStudents();
			   Vector<String> IPs = s_status.getIPs();
			   int num = student_names.size();
			   
				for(int i=0;i < num; i ++) 
				{				
					System.out.println("setStudentQuestioned: " + student_names.get(i));   
					setStudentQuestioned(student_names.get(i), IPs.get(i));							    			
				}
			    sendmsg.put("TYPE", "RE_TAKE" + retake);
			}
			else {
				System.out.println("START_SOLVE");
				sendmsg.put("TYPE", "START_SOLVE");
			}
			sendmsg.put("TIME_LIMIT", new Integer(10));
			sendmsg.put("NUMQ", new Integer(numofq));
			sendmsg.put("RANSWER", ranswers);
			
			sendMessage(sendmsg);
			System.out.println("START_SOLVE SENT");
			
		} catch (Exception e) {
			System.out.println("Send solve question error"  + e);
		}
		
	}
	
	public void start_a_new_session()
	{
	    Vector<String> student_names = s_status.getStudents();
		Vector<String> IPs = s_status.getIPs();
		int num = student_names.size();
		
	    for(int i=0;i < num; i ++) 
	    {				
		   System.out.println("setStudentRegistered: " + student_names.get(i));   
		   setStudentRegistered(student_names.get(i), IPs.get(i));							    			
	    }
	    
	    JSONObject msg = new JSONObject();
		try {
			msg.put("TYPE", "RE_START");
		} catch (JSONException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		sendMessage(msg);
	}
	public void send_show_results()
	{
		JSONObject result   = new JSONObject();
		
		int numofq = s_status.getNumQuestions();
		JSONArray winscore;
		JSONArray winrating;		
		winscore  = s_status.getWinScoreJSON();
		winrating = s_status.getWinRatingJSON();
		JSONArray ranswers     = s_status.getAnswers();
		JSONArray avg_ratings  = s_status.get_final_avg_ratings();
		JSONArray ranswer_percents  = s_status.get_r_answer_percents();

		int   send_high_score  = s_status.getHighScore();
		float send_high_rating = s_status.getHighRating();
				
		try {
			
			System.out.println("Sending START_SHOW");
			result.put("TYPE", "START_SHOW");
			result.put("WINSCORE", winscore);
			result.put("WINRATING", winrating);
			result.put("HIGHSCORE", send_high_score);
			result.put("HIGHRATING", send_high_rating);
			
			result.put("NUMQ", new Integer(numofq));
			result.put("RANSWER", ranswers);
			result.put("AVG_RATINGS", avg_ratings);             // added in 6/22
			result.put("RPERCENT", ranswer_percents);           // added in 6/24
			sendMessage(result);
			
		} catch (Exception e) {
			System.out.println("Send show results error"  + e);
		}
	}	
	
	void receivedMessage(JSONObject arg1) {
						
		try {
			String type = arg1.getString("TYPE");
			String IP   = arg1.getString("IP");
			
			if (type.equals("HAIL"))
			{
				String name = arg1.getString("NAME");
				main.student_hail(IP, name);		// new student or re-entered student
			}
	
			else if (type.equals("QUESTION")) {
				
				String name = arg1.getString("NAME");
				
				String q = arg1.getString("Q");
				
				String o1 = arg1.getString("O1");				
				String o2 = arg1.getString("O2");
				String o3 = arg1.getString("O3");		
				String o4 = arg1.getString("O4");	
				String a = arg1.getString("A");
								
				main.SaveQuestion(IP, name, q, o1, o2, o3, o4, a, null);
				setStudentQuestioned(name,IP);
			}
			
			else if (type.equals("QUESTION_PIC"))
			{
				
				String name = arg1.getString("NAME");
				String q    = arg1.getString("Q");
				String o1   = arg1.getString("O1");				
				String o2   = arg1.getString("O2");
				String o3   = arg1.getString("O3");		
				String o4   = arg1.getString("O4");	
				String a    = arg1.getString("A");
				String pic  = arg1.getString("PIC");
				
				byte[] jpeg = Base64.decode(pic);		
				System.out.println("received_pic:"+q);
				if (jpeg == null)
					System.out.println("null picture");
				
				main.SaveQuestion(IP, name, q, o1, o2, o3, o4, a, jpeg);		
				setStudentQuestioned(name,IP);
			}
			// related to answer submit debugging is going on
			else if (type.equals("ANSWER")) {
				
				JSONArray received_answer = new JSONArray();
				JSONArray received_rating = new JSONArray();
				
				String name = arg1.getString("NAME");
				received_answer = arg1.getJSONArray("MYANSWER");
				received_rating = arg1.getJSONArray("MYRATING");
				
				main.SaveAnswer(IP, name, received_answer, received_rating);
				setStudentAnswered(name,IP,received_answer);
			}
			
		} catch (Exception e) {
			System.out.println("Error in MSG "+ e);
		
		}

	}


}
