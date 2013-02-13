/*===================================================================================
  Developed by Sunmi Seol
  Modified by Chi-Hou Vong
  File Name: StudnetStatus.java
  Version: 2.1
  Modified Time: 08.03.2012
======================================================================================*/

package edu.stanford;

import java.io.BufferedOutputStream;
import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;
import java.io.Writer;
import java.nio.MappedByteBuffer;
import java.nio.channels.FileChannel;
import java.util.Formatter;
import java.util.Locale;
import java.util.ResourceBundle;
import java.util.StringTokenizer;
import java.util.Vector;

import javax.swing.event.TableModelEvent;
import javax.swing.table.AbstractTableModel;
import org.json.JSONArray;
import org.json.JSONException;

import com.csvreader.CsvReader;
import com.csvreader.CsvWriter;

public class StudentStatus { 
	
	String APP_TAG = "Student_status"; 		
	int numQuestion_submitted;
	int numAnswer_submitted;
	
	boolean update_name = false;
	boolean import_questions_index = false;
	
	// Information about Student
	Vector<String> IPs;
	Vector<String> student_names;
	Vector<Boolean> submit_question;
	Vector<Boolean> submit_answer;
	Vector<Integer> student_score;
	
	// to check 
	Vector<Integer> pong_number;
	int   ping_number;
	
	Vector< Vector<String> > received_answers;
	Vector< Vector<String> > received_ratings;
	
	Vector<String> final_avg_ratings; // added in 6/22
	Vector<String> r_answer_percents; // added in 6/24
	
	// Information per questions
	Vector<String> question;
	Vector<String> choice1;
	Vector<String> choice2;
	Vector<String> choice3;
	Vector<String> choice4;
	Vector<Boolean> has_image;
	Vector<String> answers;
	Vector<String> owner_name;
	Vector<String> owner_IP;
	
	// Information per question 
	Vector<Integer> num_correct_students;
	Vector<Integer>   total_ratings;
	Vector<Integer>   valid_ratings;
	
	// for winners (only used temporary)
	Vector<String>  name_high_scores;		// student names having high scores
	Vector<String>  name_high_ratings;		// student names that have highest rating
	
	int   send_high_score;
	float send_high_rating;
	
	tableModel_student  tm_student;	
	tableModel_question tm_question;	
	
	static ResourceBundle cur_rb;
    static Locale		  cur_locale;
    String cur_lang;
    
    static void set_default () {
 	   cur_locale = new Locale(""); //English
 	   cur_rb = ResourceBundle.getBundle("languages/MessageBundle", cur_locale);
    }
     
    void set_locale (String re_locale) {
 	   cur_locale = new Locale (re_locale);
 	   cur_rb = ResourceBundle.getBundle("languages/MessageBundle", cur_locale);
    }
    
    void set_lang (String lang) {
   	   cur_lang = lang;
   	   System.out.println("cur lang:"+cur_lang);
     }
    
    //refreshed by selecting the language
    public void re_write_label_ss() {
    
    	tm_student.fireTableStructureChanged();
    	tm_question.fireTableStructureChanged();
       	       	
    }
	
	public tableModel_student getTableModelForStudent()   { return tm_student;}
	public tableModel_question getTableModelForQuestion() { return tm_question;}
	
	//Main part
	public StudentStatus() {
		
		super();
		set_default();
		tm_student  = new tableModel_student();
		tm_question = new tableModel_question();
		clear();
	
	}

	public void clear() {
		
		numQuestion_submitted = 0;
		numAnswer_submitted   = 0;
		
		//---------------------------------------
		// Information per each student
		//---------------------------------------
		student_names   = new Vector<String> ();
		submit_question = new Vector<Boolean> ();
		submit_answer   = new Vector<Boolean> ();
		IPs             = new Vector<String> ();
		
		received_answers = new Vector<Vector<String> > ();
		received_ratings = new Vector<Vector<String> > ();
		
		//---------------------------------------
		// Information per each question
		//---------------------------------------
		question  = new Vector<String>();
		choice1   = new Vector<String>();
		choice2   = new Vector<String>();
		choice3   = new Vector<String>();
		choice4   = new Vector<String>();
		has_image = new Vector<Boolean>();
		answers   = new Vector<String>();
		owner_name = new Vector<String>();
		owner_IP = new Vector<String>();
		final_avg_ratings = new Vector<String>(); // added in 6/22
		r_answer_percents = new Vector<String>(); // added in 6/24 
			
		//--------------------------------------
		// for result
		//--------------------------------------
		student_score        = new Vector<Integer>();		// per student
		num_correct_students = new Vector<Integer>();		// per question
		
		//average_rating       = new Vector<Float>();	// Per Question
		total_ratings        = new Vector<Integer>();	// Per Question: Sum of ratings
		valid_ratings		 = new Vector<Integer>();	// Per Question: Count of valid(non-zero) ratings
		
		//-----------------------------------------
		// Final Statistics
		//-----------------------------------------
		name_high_scores = new Vector<String>();	// 
		name_high_ratings = new Vector<String>();	// 
		send_high_score= 0;
		send_high_rating = 0.0f;
				
		//--------------------------------------
		// for status check
		//--------------------------------------
		ping_number = 0;
		pong_number = new Vector<Integer>() ;
	}
	
	synchronized public void startSession() {
    	System.out.println("Start a new session from Student Status");  
    	import_questions_index = false;
    	numQuestion_submitted = 0;
		numAnswer_submitted   = 0;
		int num = getNumStudents();
		
		for(int i=0;i < num; i ++) 
		{					
		    //---------------------------------------
		    // Information per each student
		    //---------------------------------------
			submit_question.setElementAt(new Boolean(false), i);		
			submit_answer.setElementAt(new Boolean(false), i);			
		    received_answers.setElementAt(new Vector<String> (), i);
		    received_ratings.setElementAt(new Vector<String> (), i);
		    student_score.setElementAt(new Integer(0), i);					
		}
		
		//---------------------------------------
		// Information per each question
		//---------------------------------------        
		//average_rating       = new Vector<Float>();	// Per Question
		total_ratings        = new Vector<Integer>();	// Per Question: Sum of ratings
		valid_ratings		 = new Vector<Integer>();
		num_correct_students = new Vector<Integer>();		// per question		
		
		question  = new Vector<String>();
		choice1   = new Vector<String>();
		choice2   = new Vector<String>();
		choice3   = new Vector<String>();
		choice4   = new Vector<String>();
		has_image = new Vector<Boolean>();
		answers   = new Vector<String>();
		owner_name = new Vector<String>();
		owner_IP = new Vector<String>();

		//-----------------------------------------
		// Final Statistics
		//-----------------------------------------		
		final_avg_ratings = new Vector<String>(); 
		r_answer_percents = new Vector<String>();
		name_high_scores = new Vector<String>();	
		name_high_ratings = new Vector<String>();	
		send_high_score= 0;
		send_high_rating = 0.0f;
		
		tm_student.dataChanged();
		tm_question.dataChanged();
	}
	
	synchronized public void retakeQuiz() {
    	System.out.println("Retake the Quiz from Student Status");   		
		numAnswer_submitted   = 0;
		int num = getNumStudents();
		
		for(int i=0;i < num; i ++) 
		{					
		    //---------------------------------------
		    // Information per each student
		    //---------------------------------------
			submit_answer.setElementAt(new Boolean(false), i);			
		    received_answers.setElementAt(new Vector<String> (), i);
		    received_ratings.setElementAt(new Vector<String> (), i);
		    student_score.setElementAt(new Integer(0), i);					
		}
				
		for(int i=0;i < numQuestion_submitted; i ++) 
		{
	  	   //---------------------------------------
		   // Information per each question
		   //---------------------------------------
			
		   //average_rating       = new Vector<Float>();	// Per Question
		   total_ratings.setElementAt(new Integer(0), i);  // Per Question: Sum of ratings
		   valid_ratings.setElementAt(new Integer(0), i);	// Count of valid(non-zero) ratings
		   num_correct_students.setElementAt(new Integer(0), i);		// per question
		}
		
		//-----------------------------------------
		// Final Statistics
		//-----------------------------------------
		final_avg_ratings = new Vector<String>(); 
		r_answer_percents = new Vector<String>();
		name_high_scores = new Vector<String>();	
		name_high_ratings = new Vector<String>();	
		send_high_score= 0;
		send_high_rating = 0.0f;
		
		tm_student.dataChanged();
		tm_question.dataChanged();
	}

	// return true if new student (This should be changed)
	synchronized public boolean AddStudent(String name, String IP) {
		
		String [] data = new String[3];
		data[0] = name;
		data[1] = "N";
		data[2] = "N";	
		
		//if ((findName(name)== -1) && (findIP(IP) == -1)) {// add only if this is a new name
		if (findIP(IP) == -1){ // add if this is a new IP
			
			// add Student Information
			student_names.add(name);
			submit_question.add(new Boolean(false));
			submit_answer.add(new Boolean(false));
			student_score.add(new Integer(0));
			pong_number.add(new Integer(0));
			//IPs.add(new String("0.0.0.0"));
			IPs.add(IP);
			
			received_answers.add(new Vector<String> ());  
			received_ratings.add(new Vector<String> ());  
			
			// update display
			System.out.println("Adding new Name : " + name+ " Adding new IP:" + IP );
			tm_student.dataChanged();
			
			return true;
					
		} else if ((findIP(IP) != -1) && (findName(name) == -1)) {	//When the user logs in with other name and same IP
			System.out.println("Logging in again!!!");
			return false;
			
		} else {
			System.out.println("Logging in again!!!");
			return false;
		}
	}
	
	synchronized public void changeName(int order, String name) {
		
		for(int i=0;i < getNumStudents(); i ++) {
			student_names.setElementAt(name, order);
		}
		
	}
	
	synchronized public String findOrigName (int order) {
		
		String temp_name = student_names.get(order);
		return temp_name;
	
	}

	synchronized public int findName(String name) {
		// returns row number of corresponding name
		for(int i=0;i < getNumStudents(); i ++) {
			
			if (name.equals(student_names.elementAt(i))) {
				return i;
			}
		}
		return -1;
	}
	
	synchronized public int findIP(String IP) {
		
		for (int i = 0; i < getNumStudents(); i++) {
			if(IP.equals(IPs.elementAt(i))) { 
				//System.out.println("(!!!!) Adding new IP:" + IP );
				return i; 
			}
		}
		
		return -1;
		
	}
	
	synchronized public boolean isQuestionFinished(int idx) {
		if (idx == -1) return false;
		else { 
			return submit_question.elementAt(idx).booleanValue();
		}
	}
	
	//synchronized public void SetQuestionFinished(String name) {// when using user name
	synchronized public void SetQuestionFinished(String ip) {// whsn using IP
		// find corresponding name
		int row_id = findIP(ip); 
		if ((row_id != -1)&& (!isQuestionFinished(row_id))) { // if found name
			submit_question.setElementAt(new Boolean(true), row_id);
			tm_student.dataChanged();

		}
	}
	
	synchronized public boolean isAnswerFinished(int idx) {
		if (idx == -1) return false;
		else { //String s = (String) getValueAt(idx,1); 
			return submit_answer.elementAt(idx).booleanValue();
		}
	}
	
	//synchronized public void SetAnswerFinished(String name) { // when using user name
	synchronized public void SetAnswerFinished(String ip) { // when using ip
		// find corresponding name
		int row_id = findIP(ip);
		if ((row_id != -1) && (!isAnswerFinished(row_id))) { // if found name
			
			if(submit_answer.get(row_id) == false) {
				
				numAnswer_submitted ++; 
				submit_answer.setElementAt(new Boolean(true), row_id);
			
				tm_student.dataChanged();
				tm_question.dataChanged(); //03162012 changed
			}
		}
	}	
	
	public void ReceivedPongNumber(String name, Integer new_val)
	{
		int row_id = findName(name);
		if (row_id != -1) { // if found name
			Integer old_val = pong_number.elementAt(row_id);
			if (old_val.intValue() < new_val.intValue())	
				pong_number.setElementAt(new_val, row_id);
		}
	}
	public void SetPingNumber(int val)
	{
		ping_number = val;
		tm_student.dataChanged();
	}
	
	//03162012 by SM
	public void SetResult()
	{
		tm_question.dataChanged();
	}
	
	public void SaveImage(int q_id, byte[] jpg )
	{
		//int id = findName(name);
		//if (id == -1) return;		
		
		String filename = directory + "/" + q_id +".jpg";
		// write to the file
		
		FileOutputStream out = null;
        try {
            out = new FileOutputStream(filename);
            BufferedOutputStream bout = new BufferedOutputStream(out, 128*1024); // buffered write makes it faster to save
            int i;
            //System.out.println("out begin"+jpg.length);
            for(i=0;i<jpg.length;i++) {
            	System.out.print(" "+i);
            	bout.write(jpg[i]);  //
            }
            
            System.out.println("out done");
            bout.close();
        
        } catch(IOException e) {
        	System.out.println("IOException:"+e);
        }
	}
	
	// returns question id
	synchronized public int SaveQuestion(		
			String IP, String name, String q, String c1, String c2, String c3, String c4,
			String answer, 
			boolean img,
			boolean from_saved_file)
	{	
		String orig_name = "";
		
		if (!from_saved_file) {
			
			int id;
			if(import_questions_index == true) {
				id = findName(name);
			} else { 
				id = findIP(IP);
				orig_name = findOrigName(id);
			}
			
			if (id == -1) return -1;
		}
		
		if (question.contains(q))  // added on 7/26/2012 to fix duplicate questions problem		
		{ 			               
		    for(int i=0;i < numQuestion_submitted; i ++) 
			{
		  	   if(q.equals(question.get(i)))
		  	   {
		  		 if(IP.equals(owner_IP.get(i)))
				 {
				    System.out.println("Received a duplicate post from " + name);
				    return -1;	
				 }
		  	   }
			}		                   		
		}
		
		System.out.println("adding question");
		
		int q_id = question.size();
		question.add(q);
		choice1.add(c1);
		choice2.add(c2);
		choice3.add(c3);
		choice4.add(c4);
		answers.add(answer);
		has_image.add(new Boolean(img));
		
		if(import_questions_index == true) {
			owner_name.add(name);
		} else {
			owner_name.add(orig_name);
		}
		
		owner_IP.add(IP);
		total_ratings.add(new Integer(0));
		valid_ratings.add(new Integer(0));
		num_correct_students.add(new Integer(0));
		
		// create html
		if(import_questions_index == true) { 
			create_html_file(q_id, name);
			create_html_file_answer(q_id, name);  // need this by who?
		} else {
			create_html_file(q_id, orig_name);
			create_html_file_answer(q_id, orig_name); 
		}
		
		if (!from_saved_file)
			SetQuestionFinished(IP);
		
		numQuestion_submitted ++;
		tm_question.dataChanged();
		
		return q_id;
	}
	
    public void create_question_files()
	{		
		for(int i=0;i < numQuestion_submitted; i ++) 
		{
			create_html_file(i, owner_name.get(i));
			create_html_file_answer(i, owner_name.get(i)); 
		}
	}

	synchronized public void SaveAnswer(String ip, String name, JSONArray answer, JSONArray rating) {
		
		// check if this name exist in the list
		int id = findIP(ip);
		if(id == -1) return;
		
		//create text file
		// calculate score and rating
		cal_score_and_rating(id, answer, rating);
				
		String temp_name = "";
		temp_name = findOrigName(id);
		if(findOrigName(id).equals(name)) {
			create_scoreborad_file(id, name, answer, rating);
		} else {
			create_scoreborad_file(id, temp_name, answer, rating);
		}
		
		SetAnswerFinished(ip); // when using ip
	}
	
	
	public void cal_score_and_rating(int id, JSONArray _answer, JSONArray _rating) 
	{
		int score = 0;
		
		Vector<String> his_answers = received_answers.elementAt(id);
		Vector<String> his_ratings = received_ratings.elementAt(id);
		his_answers.clear();
		his_ratings.clear();
		try {
			// count correct answers
			for(int i=0;i<_answer.length();i++)
			{
				Integer user_answer = Integer.valueOf(_answer.get(i).toString());
				Integer correct_answer = Integer.valueOf(answers.elementAt(i));
				if (user_answer.equals(correct_answer)) {
					score++;
					
					int old_val = num_correct_students.get(i).intValue();
					num_correct_students.set(i, new Integer(old_val+1));
				}
				his_answers.add(_answer.get(i).toString());
				
				// update rating
				Integer user_rating = Integer.valueOf(_rating.get(i).toString());
				int rate = user_rating.intValue();
				if (rate > 0) {
					// if 0 ignore (not rated)
					int rating_sum = total_ratings.get(i).intValue() + rate;
					total_ratings.set(i, new Integer(rating_sum));
					
					int valid_rating = valid_ratings.get(i).intValue() + 1;
					valid_ratings.set(i, new Integer(valid_rating));
				}
				his_ratings.add(_rating.get(i).toString());
			}
			
			this.student_score.set(id, new Integer(score));
		}
		catch (Exception e) {
			System.out.println("JSON ERROR" + e);
		}
	}
	
	/*
	public JSONArray score_answer(JSONArray _answer, int id) {
		
		JSONArray score_result = new JSONArray();
		int count_rightanswer = 0;
		int my_answer         = 0;
		int right_answer      = 0;
		
		
		for(int i = 0 ; i < answers.size(); i++) {
			
			try {
				my_answer    = (Integer) _answer.get(i);
				right_answer = Integer.parseInt(answers.get(i));
				
				System.out.println("Rightanswer: "+ right_answer);
				System.out.println("Myanswer: "+ my_answer);
				
				if(my_answer == right_answer) {// right answer
					try {
						System.out.println("RIGHT!!!");
						score_result.put(i, 1);
						count_rightanswer++;
						int temp = num_student_question.get(i);
						temp = temp + 1;
						System.out.println("num_student_question: "+temp);
						num_student_question.set(i,temp);
						
					} catch (JSONException e) {
						e.printStackTrace();
					}
				} else {
					score_result.put(i, 0);
				}
				
			} catch (JSONException e) {
				e.printStackTrace();
			} 
		}
		
		student_score.set(id, count_rightanswer);
		System.out.println("("+id+")"+"Rightnum: "+count_rightanswer);
		return score_result;
	}
	*/
	
	// Why do we need this?
	/*
	public void create_result_file(int id, String name, JSONArray answer, JSONArray rating) {
		
		String filename = directory + "/" + id +".txt";
		JSONArray score = new JSONArray();
		
		score = score_answer(answer, id);
				
		// create File
		try {
			
			BufferedWriter out = new BufferedWriter(new FileWriter(filename));
			for (int i = 0 ; i < score.length() ; i++) {
				out.write(answer.get(i)+" "+score.get(i)+ " "+ rating.get(i));
				out.newLine();
			}
			
			out.close();
		
		} catch (Exception e) {
			System.out.println("FILE Error:"+e);
		}
	}
	*/
	public void create_scoreborad_file(int sid, String name, JSONArray u_answer, JSONArray rating)
	{
	 
		String filename = directory + "/" + "score_" + sid +".html";
		if(cur_lang.equals("Arabic"))
			   set_locale("ar_HTML");
		else if(cur_lang.equals("Thai"))
			   set_locale("th_HTML");
		
		try {
			//BufferedWriter out = new BufferedWriter(new FileWriter(filename));
			Writer out = new BufferedWriter(new OutputStreamWriter(
					new FileOutputStream(filename), "UTF8"));
			
			out.write("<html><head>" + getHTMLTagForUTF8() + "</head><body><P></P>\n");
			out.write("<center>"+cur_rb.getString("SCORE_TITLE")+ " " + name+"<br>\n");
			out.write(cur_rb.getString("SCORE")+": " + student_score.elementAt(sid) + " /" + getNumQuestions() +"\n");

			out.write("<P><table border=\"1\">");
			out.write("<tr><td><div align=\"center\"> "+ cur_rb.getString("Q_NUM")+" </div></td>" +
					   "<td><div align=\"center\"> "+ cur_rb.getString("C_ANSWER")+" </div></td>" +
					   "<td><div align=\"center\"> "+ cur_rb.getString("CH_ANSWER")+ " </div></td>" +
					   "<td><div align=\"center\"> "+ cur_rb.getString("G_RATE")+ " </div></td>" +
					   "</tr>\n");
			
			for(int i = 0 ; i < getNumQuestions(); i++) {
				out.write("<tr><td><div align=\"center\">");
				out.write(""+(i+1));
				out.write("</div></td>");
				
				out.write("<td><div align=\"center\">");
				out.write(answers.elementAt(i));
				out.write("</div></td>");

				out.write("<td><div align=\"center\">");
				out.write(""+u_answer.get(i));
				out.write("</div></td>");

				out.write("<td><div align=\"center\">");
				out.write(""+rating.get(i));
				out.write("</div></td></tr>\n");
			}
			out.write("</table></p></center></body></html>\n");
			out.close();
			
		} catch (Exception e) {
			System.out.println("ERROR in file:" + e);
		}
		if(cur_lang.equals("Arabic"))
			   set_locale("ar_AR");
		else if(cur_lang.equals("Thai"))
			   set_locale("th_TH");
	}
		
	private String directory;  //  @jve:decl-index=0:
	void setDirectory(String s) {directory = s;}
	private String getHTMLTagForUTF8() {
		/*String str="\n <meta http-equiv=\"Content-Type\" content=\"text/html; charset=UTF-8\"\n ";
		str+="\n <meta name='viewport' content='width=device-width; initial-scale=1.0; maximum-scale=1.0; user-scalable=0;' />\n";		
		str+="<style>";
		str+="@media only screen and (min-device-width : 320px) and (max-device-width : 480px) {img { max-width: 200px; height: 180px;}}";
		str+="@media only screen and (min-device-width : 768px) and (max-device-width : 1024px) {img { max-width: 100%; height: auto;}}";
		str+="img  {max-width: expression(this.width > 200 ? 200: true);}";
		str+="</style>";
		
		return str;*/
		return new String("\n <meta http-equiv=\"Content-Type\" content=\"text/html; charset=UTF-8\"\n ");
	}
	
	public void create_html_file(int id, String sname)
	{
		String filename = directory + "/" + id +".html";
		if(cur_lang.equals("Arabic"))
			   set_locale("ar_HTML");
		else if(cur_lang.equals("Thai"))
			   set_locale("th_HTML");
		
		// create FILE
		try {
		    //BufferedWriter out = new BufferedWriter(new FileWriter(filename));
		    Writer out = new BufferedWriter(new OutputStreamWriter(
					new FileOutputStream(filename), "UTF8"));
		    //out.write("<!DOCTYPE html PUBLIC '-//WAPFORUM//DTD XHTML Mobile 1.2//EN' 'http://www.openmobilealliance.org/tech/DTD/xhtml-mobile12.dtd'>");
		    out.write("<html>\n<head>"+cur_rb.getString("Q_N") + " " + (id+1) + getHTMLTagForUTF8() + "</head>\n<body>\n");
		    out.write("<p>("+cur_rb.getString("Q_OWNER") + " " + sname + ")</p>\n");
		    out.write("<P>"+cur_rb.getString("Q")+":\n");
		    out.write(question.get(id));
		    out.write("\n</P>\n");
		    
		    if (has_image.get(id).booleanValue()) {
		    	//out.write("<a href='"+id+".jpg'>\n");
		    	//out.write("<img class=\"main\" src=\"" + id + ".jpg\" width=\"200\" height=\"180\"/>\n");
		    	out.write("<img class=\"main\" src=\"" + id + ".jpg\" width=\"280\" height=\"210\"/>\n");
		    	//out.write("</a>\n");
		    }

		    out.write("<P>\n");
		    out.write("(1) " +  choice1.get(id) + "<br>\n");
		    out.write("(2) " +  choice2.get(id) + "<br>\n");
		    out.write("(3) " +  choice3.get(id) + "<br>\n");
		    out.write("(4) " +  choice4.get(id) + "<br>\n");

		    out.write("</P>\n</body></html>\n");		    

		    out.close();
		
		} catch (IOException e) {
			System.out.println("FILE Error:"+e);
		}
		if(cur_lang.equals("Arabic"))
			   set_locale("ar_AR");
		else if(cur_lang.equals("Thai"))
			   set_locale("th_TH");
	}

	float get_average_rate(int q_id) {
        
		float avg_rate = 0;
        int valid = valid_ratings.get(q_id).intValue();
        int rate = total_ratings.get(q_id).intValue();
        if (valid > 0) 
        {
        	avg_rate = rate / (float) valid;
        }
        
        return avg_rate;
	}
	
	// It should be fixed
	float get_correct_percent_correct_people(int q_id) {
		
		float pct;
		
		int i = getNumStudents();
		if (i==0)return 0;
		
		pct = ((float) num_correct_students.get(q_id).intValue()) / i;
		
		return  (pct * 100);
		
	}
	
    private float get_percent(int num) {
		
		float pct;
		
		int i = getNumStudents();
		if (i==0)return 0;
		
		pct = ((float) num) / i;
		
		return  (pct * 100);		
	}

    private float getStudentRating(int id)
	{
		int len, num, rating;
		Vector<String> user_ratings;
		int user_rating=0;
        float avg_rating=0;
				
	    user_ratings = received_ratings.elementAt(id);	
	    len = user_ratings.size();
	    
		if(len>0)
		{			
			num=0;
			for(int i=0; i<len; i++)
			{				
				rating = Integer.valueOf(user_ratings.get(i).toString());
				if(rating > 0)
				{
					user_rating += rating;
					num++;
				}
			}
			
			avg_rating = (float)user_rating/(float)num;
		}

		return avg_rating;
	}
    
	private Vector<Integer> getNumResponse(int questionNum)
	{
		Vector<Integer> numResponse = new Vector<Integer>(4);  // 4 answers per question
		int len = received_answers.size();  // num of students
		Vector<String> user_answers;
		int user_answer;
		int old_val;
		
		for(int i=0; i<4; i++)
		    numResponse.add(new Integer(0));
		
		for(int i=0; i<len; i++)
		{
			user_answers = received_answers.elementAt(i);	
			if(user_answers.size()>0)
			{
			    user_answer = Integer.valueOf(user_answers.get(questionNum).toString());
				old_val = numResponse.get(user_answer-1).intValue();
				numResponse.set(user_answer-1, new Integer(old_val+1));
			}
		}
		
		return numResponse;
	}
	
	// Add Final Statistics to the HTML files (for clients)
	public void append_tohtml() {
		if(cur_lang.equals("Arabic"))
			   set_locale("ar_HTML");
		else if(cur_lang.equals("Thai"))
			   set_locale("th_HTML");
		
		for (int i = 0 ; i < getNumQuestions() ; i++) {
			String out_filename = directory + "/" + i +"_result"+".html";
			Vector<Integer> numResponse = getNumResponse(i);
			
			try {
				
				//FileWriter fstream = new FileWriter(out_filename,true);
		        //BufferedWriter out = new BufferedWriter(fstream);
		        Writer out = new BufferedWriter(new OutputStreamWriter(
						new FileOutputStream(out_filename, true), "UTF8"));
		        
		        /* out.write("<P> "+cur_rb.getString("C_N_PPL")+": " + num_correct_students.get(i)+" / " + 
		        		getNumAnswers() + "<br>\n");*/
		        float pct;				
				pct = ((float) num_correct_students.get(i)) /getNumStudents();				
				pct = Math.round(pct*100);	
		        out.write("<P> "+cur_rb.getString("C_N_PPL")+": " + num_correct_students.get(i)+" / " + 
		        		getNumStudents() + " ("+((int)pct)+"%)<br>\n");  
		        
		        Formatter f = new Formatter();
		        String avg = f.format(new String("%4.2f"), get_average_rate(i)).toString();
		        // Save average question
		        final_avg_ratings.add(i, avg);
		        
		        String s1="";
		        String s2="";
		        String s3="";
		        String s4="";
		        int r1 = Math.round(get_percent(numResponse.get(0)));
		        int r2 = Math.round(get_percent(numResponse.get(1)));
		        int r3 = Math.round(get_percent(numResponse.get(2)));
		        int r4 = Math.round(get_percent(numResponse.get(3)));
		        if (r1 > 0)  // red
		        	s1 = "style='background-color:#ED1C24;width:" + r1 + "%;'";
		        if (r2 > 0)  // orange
		        	s2 = "style='background-color:#F99C1C;width:" + r2 + "%;'";
		        if (r3 > 0)  // green
		        	s3 = "style='background-color:#97B546;width:" + r3 + "%;'";
		        if (r4 > 0)  // blue
		        	s4 = "style='background-color:#00AEEF;width:" + r4 + "%;'";
		        
		        Formatter f1 = new Formatter();
				String percent = f1.format(new String("%3.0f"), get_correct_percent_correct_people(i)).toString();
				r_answer_percents.add(i, percent); 
		        
		        out.write(cur_rb.getString("AVG_RATE")+": " + avg +"<br>\n");
			    out.write("</P><br>\n");
			    
			    out.write("<table cellpadding='1' width='95%'>\n");
			    out.write("<tr><th align='center' colspan='2'>"+ cur_rb.getString("CH_ANSWER") +" %</th></tr>\n");
			    out.write("<tr><td width='30%'>(1) " + r1 + "%</td><td><div "+s1+">&nbsp;</div></td></tr>\n");
			    out.write("<tr><td>(2) " + r2 + "%</td><td><div "+s2+">&nbsp;</div></td></tr>\n");
			    out.write("<tr><td>(3) " + r3 + "%</td><td><div "+s3+">&nbsp;</div></td></tr>\n");
			    out.write("<tr><td>(4) " + r4 + "%</td><td><div "+s4+">&nbsp;</div></td></tr>\n");
			    out.write("</table>\n</body></html>\n");
			    out.close();
			    
			} catch (IOException e) {
				System.out.println("FILE Append Error:"+e);
			}
			
		}
		if(cur_lang.equals("Arabic"))
			   set_locale("ar_AR");
		else if(cur_lang.equals("Thai"))
			   set_locale("th_TH");
	}
	
	public void create_final_result(){
		String out_filename = directory + "/" + "final_result"+".html";		
		if(cur_lang.equals("Arabic"))
			   set_locale("ar_HTML");
		else if(cur_lang.equals("Thai"))
			   set_locale("th_HTML");
		
		try {
			
		    //BufferedWriter out = new BufferedWriter(new FileWriter(out_filename));
		    Writer out = new BufferedWriter(new OutputStreamWriter(
					new FileOutputStream(out_filename), "UTF8"));
		    
		    out.write("<html>\n<head>"+ getHTMLTagForUTF8() + "</head>\n<body>\n");
		    out.write("<center><Strong>"+cur_rb.getString("WINNER")+"</Strong></center><br>\n");
		    out.write("<P></P>\n");
		    String s = cur_rb.getString("H_SCORE"); s += ": ";
			s += getHighScore() + "<br />\n";
			s += cur_rb.getString("TOP_SCORER"); s += ": ";
			Vector<String> names = getTopScorers();
			
			for(int i=0;i<names.size(); i++) {
				s += names.elementAt(i);
				if (i!= (names.size()-1)) s+= ", ";
			}
			s += "<br /><br />\n";
			
	        Formatter f = new Formatter();
	        String rt = f.format(new String("%4.2f"), getHighRating()).toString();
			s += cur_rb.getString("H_RATE"); s += ": ";
			s += rt + "<br />\n";
			s += cur_rb.getString("Q_OWNER"); s += ": ";
			names = getTopRankers();
			for(int i=0;i<names.size(); i++) {
				s += names.elementAt(i);
				if (i!= (names.size()-1)) s+= ", ";
			}
			s += "<br />\n";
		    out.write(s);
		    out.write("\n</body>\n");
		    out.close();
		
		} catch (IOException e) {
			System.out.println("FILE Error:"+e);
		}
		if(cur_lang.equals("Arabic"))
			   set_locale("ar_AR");
		else if(cur_lang.equals("Thai"))
			   set_locale("th_TH");
	}
	
	public void create_html_file_answer(int id, String sname) {
		
		String out_filename = directory + "/" + id +"_result"+".html";
		if(cur_lang.equals("Arabic"))
		   set_locale("ar_HTML");
		else if(cur_lang.equals("Thai"))
			   set_locale("th_HTML");
		
		try {
			
		    //BufferedWriter out = new BufferedWriter(new FileWriter(out_filename));
		    Writer out = new BufferedWriter(new OutputStreamWriter(
					new FileOutputStream(out_filename), "UTF8"));
		    
		    out.write("<html>\n<head>"+ getHTMLTagForUTF8() + "</head>\n<body>\n");
		    out.write(cur_rb.getString("Q_N") + " " + (id+1)  + "\n");
		    out.write("<p>("+cur_rb.getString("Q_OWNER")+ " " + sname + ")</p>\n");
		    out.write("<P> "+cur_rb.getString("Q")+ ":\n");
		    out.write(question.get(id));
		    out.write("\n</P>\n");
		    
		    if (has_image.get(id).booleanValue()) {
		    	//out.write("<img class=\"main\" src=\"" + id + ".jpg\" width=\"200\" height=\"180\"/>\n");
		    	out.write("<img class=\"main\" src=\"" + id + ".jpg\" width=\"280\" height=\"210\"/>\n");
		    }

		    out.write("<P>\n");
		    if(answers.get(id).equals("1")) 
		    	out.write("(1) " +  choice1.get(id) + "<font color = red>&#10004;</font>" + " (" + cur_rb.getString("C_ANSWER") + ")<br>\n");
		    else                       out.write("(1) " +  choice1.get(id) + "<br>\n");
		    
		    if(answers.get(id).equals("2")) 
		    	out.write("(2) " +  choice2.get(id) + "<font color = red>&#10004;</font>" + " (" + cur_rb.getString("C_ANSWER") + ")<br>\n");
		    else                       out.write("(2) " +  choice2.get(id) + "<br>\n");
		    
		    if(answers.get(id).equals("3")) 
		    	out.write("(3) " +  choice3.get(id) + "<font color = red>&#10004;</font>" + " (" + cur_rb.getString("C_ANSWER") + ")<br>\n");
		    else                       out.write("(3) " +  choice3.get(id) + "<br>\n");
		    
		    if(answers.get(id).equals("4")) 
		    	out.write("(4) " +  choice4.get(id) + "<font color = red>&#10004;</font>" + " (" + cur_rb.getString("C_ANSWER") + ")<br>\n");
		    else 					   out.write("(4) " +  choice4.get(id) + "<br>\n");

		    out.write("</P>\n");		    
		    //out.write("<P>\n");
		    //out.write(cur_rb.getString("C_ANSWER")+": " + answers.get(id)+"<br>\n");
		    
		    out.close();
		
		} catch (IOException e) {
			System.out.println("FILE Error:"+e);
		}
		if(cur_lang.equals("Arabic"))
		   set_locale("ar_AR");
		else if(cur_lang.equals("Thai"))
			   set_locale("th_TH");
	}
	
	// "Correct" Answers
	public JSONArray getAnswers() { 
	
		JSONArray send_answers;
		send_answers = new JSONArray();
		int sizeofarray = answers.size();
				
		for(int i = 0 ; i< sizeofarray ; i++) {
			try {
				if(answers.get(i) == "") send_answers.put(i, "null");
				send_answers.put(i,answers.get(i));
								
			} catch (JSONException e) {
				System.out.println("Converting Error:"+e);
				e.printStackTrace();
			}
		}
		
		return send_answers;
	}
	
	// for average rating in 6/22
	public JSONArray get_final_avg_ratings() { 
		
		JSONArray send_avg_ratings;
		send_avg_ratings = new JSONArray();
		int sizeofarray = final_avg_ratings.size();
				
		for(int i = 0 ; i< sizeofarray ; i++) {
			try {
				if(final_avg_ratings.get(i) == "") send_avg_ratings.put(i, "null");
				send_avg_ratings.put(i,final_avg_ratings.get(i));
				
								
			} catch (JSONException e) {
				System.out.println("Converting Error:"+e);
				e.printStackTrace();
			}
		}
		
		return send_avg_ratings;
	}
	
	// for average rating in 6/22
	public JSONArray get_r_answer_percents() { 
		
		JSONArray send_r_answer_percents;
		send_r_answer_percents = new JSONArray();
		int sizeofarray = r_answer_percents.size();
				
		for(int i = 0 ; i< sizeofarray ; i++) {
			try {
				if(r_answer_percents.get(i) == "") send_r_answer_percents.put(i, "null");
				send_r_answer_percents.put(i,r_answer_percents.get(i));
				
								
			} catch (JSONException e) {
				System.out.println("Converting Error:"+e);
				e.printStackTrace();
			}
		}
		
		return send_r_answer_percents;
	}
	
	// Submitted Answers from name
	public JSONArray get_saved_answers(String name) {
		JSONArray a = new JSONArray();
		int id = this.findName(name);
		if ((id == -1) || (this.isAnswerFinished(id)==false)) {
			for(int i = 0; i < this.numQuestion_submitted; i++) {
				a.put("-1");
			}
		} else {
			Vector<String> saved = received_answers.elementAt(id);	
			for(int i = 0; i < saved.size(); i++)
				a.put(saved.elementAt(i));
		}
		 
		return a;
	}

	
	// Finalize Results
	public void finalize_results()
	{
		caclHighScore();		// calculate high score, and names of high scorers
		calcHighRating();		// calculate high ratings, and names of high ratings
		append_tohtml();        // add final rating to the result
		create_final_result();
	}
		
	
	// calculate high score
	public void caclHighScore() {
		
		send_high_score = 0;
		
		// 1. get high score
		for (int i = 0 ; i < student_score.size(); i++) { // for all students
			
			int cur_score = student_score.get(i).intValue(); 
			if(cur_score > send_high_score) {
				send_high_score = cur_score;
			}
		}
		
		// 2. Add names of high scorers to the vector
		name_high_scores = new Vector<String>();	// 
		
		for(int i = 0 ; i < student_score.size(); i++ ) {
			int cur_score = student_score.get(i).intValue();
			if (cur_score == send_high_score) {
				String name = student_names.elementAt(i);
				name_high_scores.add(name);
			}
		}
		System.out.println("high_score: "+send_high_score);
		return;
	}
	
	// return name of Best Scorers
	public void calcHighRating() {
		
		send_high_rating = 0;
		// 1. get high score
		for (int i = 0 ; i < question.size(); i++) {  // for all questions
			
			float curr_rank = this.get_average_rate(i);
			if(curr_rank > send_high_rating) {
				send_high_rating = curr_rank;
			}
		}
		
		// 2. Add names of high scorers to the vector
		name_high_ratings = new Vector<String>();
		
		for(int i = 0 ; i < question.size(); i++ ) {
			float curr_rank = this.get_average_rate(i);
			if(curr_rank >= send_high_rating) {
				String name = owner_name.elementAt(i);
				name_high_ratings.add(name);
			}
		}
		
		System.out.println("high_rating: "+ send_high_rating);
		return;	
	}
	
	public JSONArray convert_json(Vector<String> _arr) {
		
		JSONArray newjson = new JSONArray();
				
		for(int i = 0 ; i< _arr.size(); i++) {
			try {
				newjson.put(i, _arr.get(i));
			} catch (JSONException e) {
				e.printStackTrace();
			}
		}
		
		return newjson;
	}
	
	public JSONArray getWinScoreJSON() {
		JSONArray new_json = convert_json(name_high_scores);
		return new_json;
	}
	
	public JSONArray getWinRatingJSON() {
		JSONArray new_json = convert_json(name_high_ratings);
		return new_json;
	}
	
	public int getHighScore()                       {return send_high_score;}
	public float getHighRating()                    {return send_high_rating;}
	public Vector<Integer> getStudentScores()       {return student_score;}
	synchronized public Vector<String> getStudents()        {return student_names;}
	synchronized public Vector<String> getIPs()       		{return IPs;}
	synchronized public int getNumStudents()        {return student_names.size();}
	synchronized public int getNumQuestions()       {return numQuestion_submitted;}
	synchronized public int getNumAnswers()         {return numAnswer_submitted;}
	public boolean hasImage(int i)                  {return has_image.get(i).booleanValue();}
	
	public Vector<String> getTopScorers()			{return name_high_scores;}
	public Vector<String> getTopRankers()			{return name_high_ratings;}
	
	
	//--------------------------------------------------------------------------------
	// Save and Restore
	//--------------------------------------------------------------------------------
	
	static final String MARKER = "_@JSQ%_";
	public boolean export_questions(String curr_dir, String target_dir, String filename)
	{
		System.out.println("Exporting Questions");
		// filename is an absolute path
		File f = new File(target_dir + "/" + filename);
		try {
			PrintWriter out = new PrintWriter(new FileWriter(f));
			//System.out.println("FILE = " + f.getAbsolutePath());

			out.println(MARKER);
			out.println("" + question.size());
			for(int i = 0; i < question.size(); i++)
			{
				out.println(MARKER);
				out.println("" + i);
				out.println(MARKER);
				out.println(question.elementAt(i));
				out.println(MARKER);
				out.println(choice1.elementAt(i));
				out.println(MARKER);
				out.println(choice2.elementAt(i));
				out.println(MARKER);
				out.println(choice3.elementAt(i));
				out.println(MARKER);
				out.println(choice4.elementAt(i));
				out.println(MARKER);
				out.println(has_image.elementAt(i).booleanValue() ? "Y" : "N");
				out.println(MARKER);
				out.println(answers.elementAt(i));
				out.println(MARKER);
				out.println(owner_name.elementAt(i));
				// should we save ratings also?
				// copy image file
				if (has_image.elementAt(i).booleanValue()) {
					my_copy_file(curr_dir+"/"+i+".jpg", target_dir + "/" + i +".jpg");
				}
			} // end of for questions
			out.println(MARKER);
			out.close();
		} catch(Exception e) {
			e.printStackTrace();
			return false;
		}
		
		return true;
	}	
	
	public boolean export_data(String curr_dir, String target_dir, String filename)
	{
		System.out.println("Exporting Questions from export()");
		// filename is an absolute path
		File f = new File(target_dir + "/" + filename);
		File dir = new File(target_dir);
		if (!dir.exists()) {
			dir.mkdirs();
		}
		
		// before we open the file check to see if it already exists
		//boolean alreadyExists = f.exists();
			
		try {
			// use FileWriter constructor that specifies open for appending
			//CsvWriter csvOutput = new CsvWriter(new FileWriter(f), ',');
			CsvWriter csvOutput = new CsvWriter(new BufferedWriter(new OutputStreamWriter(
					new FileOutputStream(f), "UTF8")), ',');
			
			// if the file didn't already exist then we need to write out the header line
		//	if (!alreadyExists)
		//	{
				//System.out.println("header");
				csvOutput.write("num");
				csvOutput.write("question");
				csvOutput.write("choice1");
				csvOutput.write("choice2");
				csvOutput.write("choice3");
				csvOutput.write("choice4");
				csvOutput.write("has_image");
				csvOutput.write("answers");
				csvOutput.write("owner_name");
				csvOutput.write("owner_IP");
				csvOutput.endRecord();
		//	}
			// else assume that the file already has the correct header line
			
			for(int i = 0; i < question.size(); i++)
			{
			    // write out a few records
			   csvOutput.write("" + i);
			   csvOutput.write(question.elementAt(i));
			   csvOutput.write(choice1.elementAt(i));
			   csvOutput.write(choice2.elementAt(i));
			   csvOutput.write(choice3.elementAt(i));
			   csvOutput.write(choice4.elementAt(i));
			   csvOutput.write(has_image.elementAt(i).booleanValue() ? "Y" : "N");
			   csvOutput.write(answers.elementAt(i));
			   csvOutput.write(owner_name.elementAt(i));
			   csvOutput.write(owner_IP.elementAt(i));
			   csvOutput.endRecord();
			
			    // should we save ratings also?
				// copy image file
				if (has_image.elementAt(i).booleanValue()) {
					my_copy_file(curr_dir+"/"+i+".jpg", target_dir + "/" + i +".jpg");
				}	
				
			} // end of for questions
			
			csvOutput.close();
			
		} catch (IOException e) {
			e.printStackTrace();
		}
		
		/*try {

			FileWriter out = new FileWriter(f);
			//System.out.println("FILE = " + f.getAbsolutePath());

			for(int i = 0; i < question.size(); i++)
			{
				out.append("" + i); out.append(',');
			    out.append("\""+question.elementAt(i)+"\""); out.append(',');
			    out.append("\""+choice1.elementAt(i)+"\""); out.append(',');
			    out.append("\""+choice2.elementAt(i)+"\""); out.append(',');
			    out.append("\""+choice3.elementAt(i)+"\""); out.append(',');
			    out.append("\""+choice4.elementAt(i)+"\""); out.append(',');
			    out.append(has_image.elementAt(i).booleanValue() ? "Y" : "N"); out.append(',');
			    out.append(answers.elementAt(i)); out.append(',');
			    out.append(owner_name.elementAt(i)); out.append(',');
			    out.append(owner_IP.elementAt(i)); out.append('\n');
			    
				// should we save ratings also?
				// copy image file
				if (has_image.elementAt(i).booleanValue()) {
					my_copy_file(curr_dir+"/"+i+".jpg", target_dir + "/" + i +".jpg");
				}				
			    
			} // end of for questions
			
			out.flush();
		    out.close();

		} catch(Exception e) {
			e.printStackTrace();
			return false;
		}*/
		
		return true;
	}
	
	private int check_q_owner(String sname) {
		
		int return_value = -1;
		
		for(int i = 0 ; i < owner_name.size(); i++) {
			String temp = owner_name.elementAt(i);
			
			if(sname.equals(temp)) return i; 
			
		}
		
		return return_value;
	}
	
	// Total students' score and question result
	public boolean save_total_result(String curr_dir, String target_dir, String filename)
	{
		if(cur_lang.equals("Arabic"))
			   set_locale("ar_HTML");
		else if(cur_lang.equals("Thai"))
			   set_locale("th_HTML");
		
		File f = new File(target_dir + "/" + filename);
		try {
			//BufferedWriter out = new BufferedWriter(new FileWriter(f));
			Writer out = new BufferedWriter(new OutputStreamWriter(
					new FileOutputStream(f), "UTF8"));
			
			out.write("<html>\n<head><strong>*** "+ cur_rb.getString("T_RESULT")+" ***</strong>" + getHTMLTagForUTF8() + "</head>\n<body>\n");
		    out.write("<P><table border=\"1\">");
			out.write("<tr><td><div align=\"center\"> "+ cur_rb.getString("S_NAME")+" </div></td>" +
					   "<td><div align=\"center\"> "+ cur_rb.getString("SCORE")+" </div></td>" +
					   "<td><div align=\"center\"> "+ cur_rb.getString("RATE")+" </div></td>" +
					   "<td><div align=\"center\"> "+ cur_rb.getString("Q_NUM")+" </div></td>" +
					   "<td><div align=\"center\"> "+ cur_rb.getString("Q_RATE")+" </div></td>" +
					   "</tr>\n");
			
			for(int i = 0 ; i < student_names.size(); i++) {
				out.write("<tr><td><div align=\"center\">");
				out.write(""+student_names.elementAt(i));
				out.write("</div></td>");
				
				out.write("<td><div align=\"center\">");
				out.write(""+student_score.elementAt(i));
				out.write("</div></td>");

				out.write("<td><div align=\"center\">");
				out.write(""+ String.format("%.2f", getStudentRating(i) )); 				
				out.write("</div></td>");
				
				String cur_stn_name = student_names.get(i);
				
				int temp_i = check_q_owner(cur_stn_name);
							
				out.write("<td><div align=\"center\">");
				if(temp_i > -1) out.write(""+(temp_i+1));
				else out.write(cur_rb.getString("NO_AVAILABLE"));
				out.write("</div></td>");

				out.write("<td><div align=\"center\">");
				if(temp_i > -1) out.write(""+String.format("%.2f", this.get_average_rate(temp_i))); 
				else out.write(cur_rb.getString("NO_AVAILABLE"));
				out.write("</div></td></tr>\n");
				
				
			}
			out.write("</table></p></center>\n");
			
			out.write("<p><table border=\"1\">");
			out.write("<tr><td><div align=\"center\"> "+ cur_rb.getString("Q_NUM")+" </div></td>" +
					   "<td><div align=\"center\"> "+ cur_rb.getString("Q")+" </div></td>" +
					   "<td><div align=\"center\"> "+ cur_rb.getString("OWNER")+" </div></td>" +
					   "<td><div align=\"center\"> "+ cur_rb.getString("CORRECT")+" </div></td>" +
					   "<td><div align=\"center\"> "+ cur_rb.getString("Q_RATE")+" </div></td>" +
					   "<td><div align=\"center\"> "+ cur_rb.getString("C_ANSWER")+" </div></td>" +
					   "<td colspan='4'><div align=\"center\"> "+ cur_rb.getString("CH_ANSWER")+" %</div></td>" +
					   "</tr>\n");
			for(int i=0;i < numQuestion_submitted; i ++) 
			{
		  	   //---------------------------------------
			   // Information per each question
			   //---------------------------------------
				out.write("<tr><td><div align=\"center\">"+(i+1)+"</div></td>");				   
				out.write("<td><div align=\"center\">"+question.elementAt(i)+"</div></td>");				
				out.write("<td><div align=\"center\">"+owner_name.elementAt(i)+"</div></td>");
				out.write("<td><div align=\"center\">"+String.format("%.2f", get_correct_percent_correct_people(i))+"</div></td>");
				out.write("<td><div align=\"center\">"+String.format("%.2f", get_average_rate(i))+"</div></td>\n");
				out.write("<td><div align=\"center\">"+answers.elementAt(i)+"</div></td>");
				
				Vector<Integer> numResponse = getNumResponse(i);
				out.write("<td>(1) " + String.format("%.2f", get_percent(numResponse.get(0))) + "</td>\n");
			    out.write("<td>(2) " + String.format("%.2f", get_percent(numResponse.get(1))) + "</td>\n");
			    out.write("<td>(3) " + String.format("%.2f", get_percent(numResponse.get(2))) + "</td>\n");
			    out.write("<td>(4) " + String.format("%.2f", get_percent(numResponse.get(3))) + "</td></tr>\n");			    
			}
			out.write("</table></p>\n");
			
			caclHighScore();		// calculate high score, and names of high scorers
			calcHighRating();		// calculate high ratings, and names of high ratings
		    out.write("<P><strong>** "+cur_rb.getString("WINNER")+" **</strong><br>");
		    out.write("<P>"+cur_rb.getString("H_SCORE")+ ": "+send_high_score+"<br>"+cur_rb.getString("TOP_SCORER")+": ");
		    for(int i = 0 ; i < name_high_scores.size(); i++) {
		    	if(i != (name_high_scores.size()-1)) out.write(" "+name_high_scores.elementAt(i)+", ");
		    	else out.write(" "+name_high_scores.elementAt(i)+ "</P>");
		    }
		    
		    out.write("<P>"+cur_rb.getString("H_RATE") + ": "+String.format("%.2f", send_high_rating)+"<br>"+cur_rb.getString("Q_OWNER")+": ");
		    for(int i = 0 ; i < name_high_ratings.size(); i++) {
		    	if(i != (name_high_ratings.size()-1)) out.write(" "+name_high_ratings.elementAt(i)+", ");
		    	else out.write(" "+name_high_ratings.elementAt(i)+ "</P>");
		    }
		    
		    if(import_questions_index == true)
		    	out.write("<P><br />*** "+cur_rb.getString("USED_Q_NOTICE")+" ***</P>\n");
		    
		    out.write("</P>\n</body></html>\n");
			out.close();
			
		} catch (Exception e){
			e.printStackTrace();
			if(cur_lang.equals("Arabic"))
				   set_locale("ar_AR");
			else if(cur_lang.equals("Thai"))
				   set_locale("th_TH");
			
			return false;
		}
		if(cur_lang.equals("Arabic"))
			   set_locale("ar_AR");
		else if(cur_lang.equals("Thai"))
			   set_locale("th_TH");
		
		return true;
	}
		
	// Should be called before any AddQuestion.
	public boolean read_marker(BufferedReader in) throws Exception
	{
		String t = in.readLine();
		if (t == null) return false;
		if (t.equals(MARKER)) return true;
		else return false;
	}
	public String read_until_marker(BufferedReader in) throws Exception
	{
		String s = "";
		boolean first = true;
		while (true) {
			String t = in.readLine();
			if (t == null) return s;
			if (t.equals(MARKER)) return s;
			if (first)
				s += t;
			else
				s += "\n" + t;
		}
	}	
	
	public boolean import_questions(String curr_dir, String target_dir, String filename)  // txt format
	{
		// parse and add data structure
		boolean ret = false;
		
		import_questions_index = true;
		String fake_IP = "No Available IP";
		
		// filename is an absolute path
		File f = new File(target_dir + "/" + filename);
		System.out.println("Importing Questions from :" + f.getAbsolutePath());
		
		do {
			try {
			    BufferedReader in = new BufferedReader(new FileReader(f)); 				
			    
				// read marker
				if (!read_marker(in)) {ret = false; break;}
				int n = Integer.valueOf(in.readLine());//number of questions	
				if (!read_marker(in)) {ret = false; break;}
				if (n <=0) return false;
				System.out.println("num questions = " + n);
				
				// 1. remove existent files at the current directory
				File temp_dir = new File(curr_dir);
				File[] files = temp_dir.listFiles();
				
				for(File file : files) {
					if (file.isDirectory())
						continue;

					if(!file.delete()){
						System.out.println("Failed to delete "+file);
					}
				}
				
				for(int i = 0; i < n; i++)	{
					
					// 2. copy jpg image file from target dir	
					//my_copy_file(target_dir+"/"+i+".jpg", curr_dir + "/" + i +".jpg");
										
					// read question number
					String q="";
					String c1="";
					String c2="";
					String c3="";
					String c4="";
					String ans="";
					String own="";
					boolean has_image_now=false;
					try {
						String s= read_until_marker(in);
						int qn = Integer.valueOf(s);//number of questions
						if (qn!=i) {	break;	} // invalid question number
					
						// read question
						q = read_until_marker(in);	System.out.println("*"+q);
						c1 = read_until_marker(in);
						c2 = read_until_marker(in);
						c3 = read_until_marker(in);
						c4 = read_until_marker(in);
						String has_image_str = read_until_marker(in);
						has_image_now = has_image_str.equals("Y");
						ans = read_until_marker(in); System.out.println("*"+ans);
						own = read_until_marker(in);
						
					} catch (NumberFormatException e) {
						e.printStackTrace();
						System.out.println("NFE Exception, ret = " + ret);
						return ret;
	
					} catch (Exception e) {
						e.printStackTrace();
						System.out.println("Exception, ret = " + ret);
						return ret;
					} 

					// executed only if no exception occurred
					//System.out.println("Saving question = " + q + ""+ c1);
					SaveQuestion(fake_IP, own, q, c1, c2, c3, c4, ans, has_image_now, true);
					if(has_image_now)
						my_copy_file(target_dir+"/"+i+".jpg", curr_dir + "/" + i +".jpg");
					
					ret = true; /// if at least 1 question is added, we can proceed						
				}
				
				in.close();
			} catch(Exception e) {
				e.printStackTrace();
				System.out.println("Exception, ret = " + ret);
				return ret;
			}
		} while(false);
		
		return ret;
	}
	
	public boolean import_data(String curr_dir, String target_dir, String filename)  // csv format
	{
		// parse and add data structure
		boolean ret = false;
		int index = -1;
		
		import_questions_index = true;
		//String fake_IP = "No Available IP";
		
		// filename is an absolute path
		File f = new File(target_dir + "/" + filename);
		System.out.println("Importing Questions from :" + f.getAbsolutePath());
		
		if (!f.exists()) return false;
		
        try {		
			//CsvReader products = new CsvReader(target_dir + "/" + filename);
			CsvReader products=new CsvReader(new InputStreamReader(new FileInputStream(target_dir + "/" + filename), "UTF-8"));
			boolean has_image_now=false;
			int row = 0;
			
			products.readHeaders();

			while (products.readRecord())
			{
				String num = products.get("num");
				String question = products.get("question");
				String choice1 = products.get("choice1");
				String choice2 = products.get("choice2");
				String choice3 = products.get("choice3");
				String choice4 = products.get("choice4");
				String has_image = products.get("has_image");
				String answers = products.get("answers");
				String owner_name = products.get("owner_name");
				String owner_IP = products.get("owner_IP");
				
				System.out.println(num + ":" + num);
				System.out.println(question + ":" + question);
				System.out.println(choice1 + ":" + choice1);
				System.out.println(choice2 + ":" + choice2);
				System.out.println(choice3 + ":" + choice3);
				System.out.println(choice4 + ":" + choice4);
				System.out.println(has_image + ":" + has_image);
				System.out.println(answers + ":" + answers);
				System.out.println(owner_name + ":" + owner_name);
				System.out.println(owner_IP + ":" + owner_IP);
				
				// perform program logic here
				has_image_now = has_image.equals("Y"); // has image
		           
		        index = SaveQuestion(owner_IP, owner_name, 
		        		   question, choice1, choice2, choice3, choice4, answers, has_image_now, true);
		           
		           if(index > -1) 
		           {
		        	   ret = true; /// at least 1 question is added, we can proceed	
		        	   
		        	   // copy jpg image file from target dir
		        	   if(has_image_now)
						  my_copy_file(target_dir+"/"+row+".jpg", curr_dir+"/"+row+".jpg");	        	   
		           }
		           
		           row++;
			}
	
			products.close();
			
		} catch (FileNotFoundException e) {
			e.printStackTrace();return ret;
		} catch (IOException e) {
			e.printStackTrace();return ret;
		}
		/*
		try {
			BufferedReader in = new BufferedReader(new FileReader(f)); 
			
			String strLine = "";
	        StringTokenizer st = null;
	        int row = 0, col = 0;
	        String[] data = new String[10];  // 10 columns in the csv import file
	        boolean has_image_now=false;
	        
	        // 1. remove existent files at the current directory
			File temp_dir = new File(curr_dir);
			File[] files = temp_dir.listFiles();
			
			for(File file : files) {
				if (file.isDirectory())
					continue;

				if(!file.delete()){
					System.out.println("Failed to delete "+file);
				}
			}
			
	        // 2. read comma separated file line by line
	        while( (strLine = in.readLine()) != null){	           
	                			   
	                //break comma separated line using ","
	           st = new StringTokenizer(strLine, ",");

	           while(st.hasMoreTokens() && col < 11){
	        	   data[col] = st.nextToken(); 
	        	   if(data[col].charAt(0)=='"' && data[col].charAt(data[col].length()-1)=='"')
	        	      data[col] = data[col].substring(1, data[col].length()-1);
	        	   col++;
	           }

	           //reset column number
	           col = 0; 
	           has_image_now = data[6].equals("Y"); // has image
	           
	           index = SaveQuestion(fake_IP, data[8], // owner's name
	        		        data[1], // question
	        		        data[2], // choice 1
	        		        data[3], // choice 2
	        		        data[4], // choice 3
	        		        data[5], // choice 4
	        		        data[7], // answer
	        		        has_image_now, 
	        		        true);
	           
	           if(index > -1) 
	           {
	        	   ret = true; /// at least 1 question is added, we can proceed	
	        	   
	        	   // copy jpg image file from target dir
	        	   if(has_image_now)
					  my_copy_file(target_dir+"/"+row+".jpg", curr_dir+"/"+row+".jpg");	        	   
	           }
	           
	           row++;
	        }
	        
	        in.close();
												
		} catch(Exception e) {
			e.printStackTrace();
			System.out.println("Exception, ret = " + ret);
			return ret;
		}		*/
		
		return ret;
	}
	
	private boolean my_copy_file(String src, String tgt)
	{	// source from : http://forums.sun.com/thread.jspa?threadID=644995
		try {
			/** Fast & simple file copy. */
			File source = new File(src);
			File dest = new File(tgt);
			FileChannel in = null, out = null;
			     try {          
			          in = new FileInputStream(source).getChannel();
			          out = new FileOutputStream(dest).getChannel();
			 
			          long size = in.size();
			          MappedByteBuffer buf = in.map(FileChannel.MapMode.READ_ONLY, 0, size);
			 
			          out.write(buf);
			 
			     } finally {
			          if (in != null)      in.close();
			          if (out != null)     out.close();
			     }
		} catch (Exception ex) {ex.printStackTrace();return false;}
		return true;
	}

	//----------------------------------------------------------
	// table model for student table 
	//----------------------------------------------------------
	class tableModel_student extends AbstractTableModel {
		
		public int getColumnCount() {return 4; } //return 6;}
		public Class getColumnClass(int cidx) {
			if (cidx == 0) return String.class;			// Name
			else if (cidx == 1) return Boolean.class;	// Submit Question
			else if (cidx == 2) return Boolean.class;	// Submit Answer
			else if (cidx == 3) return Integer.class;   // Score
			else if (cidx == 4) return Integer.class;	// Number
			else if (cidx == 5) return String.class;    // Ping-Pong Status
			
			return Object.class;
		}
		public String getColumnName(int cidx) {
			if      (cidx == 0) return cur_rb.getString("S_NAME");	// Name
			else if (cidx == 1) return cur_rb.getString("Q");	    // Submit Question
			else if (cidx == 2) return cur_rb.getString("ANSWER");  // Submit Answer
			else if (cidx == 3) return cur_rb.getString("SCORE");   // Score
			else if (cidx == 4) return "ID";		// Num added for sorting on 8/17/2012
			else if (cidx == 5) return cur_rb.getString("STAT");    // Ping-Pong Status
			
			return "";
		}
		
		public int getRowCount() {	return getNumStudents();}
		public Object getValueAt(int row, int col) {
			if (col == 0) return student_names.elementAt(row);
			else if (col == 1) return submit_question.elementAt(row);
			else if (col == 2) return submit_answer.elementAt(row);
			else if (col == 3) return student_score.elementAt(row);
			else if (col == 4) return IPs.elementAt(row);
			else if (col == 5) {
				int v = pong_number.elementAt(row).intValue();
				
				if (v >= (ping_number -2)) {
					return "ON";
				} else if (v >= (ping_number -10)) {
					return "SLOW";
				} else {
					return "OFF";
				}
				//return v + "";
			}
			
			return null;
		}
		public boolean isCellEditable(int arg0, int arg1) { return false;}
		
		void dataChanged() {
			fireTableChanged(new TableModelEvent(this));
		}
			
	}
	
	//----------------------------------------------------------
	// table model for student table 
	//----------------------------------------------------------
	class tableModel_question extends AbstractTableModel {
		
		public int getColumnCount() {return 4;}
		
		public Class getColumnClass(int cidx) {
			
			if (cidx == 0) 		return Integer.class;	// Number
			else if (cidx == 1) return String.class;	// From
			else if (cidx == 2) return Float.class;		// % Correct
			else if (cidx == 3) return Float.class;     // Rating
			return Object.class;
		}
		
		public String getColumnName(int cidx) {
			
			if (cidx == 0) 		return 	cur_rb.getString("NUM");			// Name
			else if (cidx == 1) return 	cur_rb.getString("OWNER");	        // Submit Que
			else if (cidx == 2) return  cur_rb.getString("CORRECT"); 	    // Submit Ans
			else if (cidx == 3) return  cur_rb.getString("RATE");           // Score
			return "";
		}
		
		public int getRowCount() {	return getNumQuestions();}
		
		public Object getValueAt(int row, int col) {
			
			if (col == 0)      return new Integer(row+1);
			else if (col == 1) return owner_name.elementAt(row);
			else if (col == 2) return new Float(get_correct_percent_correct_people(row));
			else if (col == 3) return new Float(get_average_rate(row));
			return null;
		}
		
		public boolean isCellEditable(int arg0, int arg1) { return false;}
		
		void refresh() {
			fireTableStructureChanged( );
		}
		
		void dataChanged() {
			fireTableChanged(new TableModelEvent(this));
		}
	}	// end of Class tableModel_question

}
