package db;

import java.io.RandomAccessFile;
import java.io.File;
import java.io.FileReader;
import java.util.Scanner;
import java.util.SortedMap;
import java.util.Stack;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.LinkedList;
import db.DataTypes;
import db.ErrorMessage;


/**
 *  @author Chris Irwin Davis
 *  @version 1.0
 *  <b>
 *  <p>This is an example of how to create an interactive prompt</p>
 *  <p>There is also some guidance to get started wiht read/write of
 *     binary data files using RandomAccessFile class</p>
 *  </b>
 *
 */
public class DavisBasePromptExample {

	/* This can be changed to whatever you like */
	static String prompt = "davisql> ";
	static String version = "v1.0b(example)";
	static String copyright = "©2016 Chris Irwin Davis";
	static boolean isExit = false;
	static ErrorMessage err=new ErrorMessage();
	/*
	 * Page size for alll files is 512 bytes by default.
	 * You may choose to make it user modifiable
	 */
	static int pageSize = 1024; 

	/* 
	 *  The Scanner class is used to collect user commands from the prompt
	 *  There are many ways to do this. This is just one.
	 *
	 *  Each time the semicolon (;) delimiter is entered, the userCommand 
	 *  String is re-populated.
	 */
	static Scanner scanner = new Scanner(System.in).useDelimiter(";");
	
	/** ***********************************************************************
	 *  Main method
	 */
    public static void main(String[] args) {

		/* Display the welcome screen */
		splashScreen();

		//Initialize metadata
		File f1 = new File("davisbase_tables.tbl");
		File f2 = new File("davisbase_columns.tbl");
		if (!f1.exists() || !f2.exists())
		{
			parseCreateString("create davisbase_tables (rowid int,table_name text)","meta");
			parseCreateString("create davisbase_columns (rowid int,table_name text,column_name text,data_type text,ordinal_position tinyint,is_nullable text)","meta");
 			parseInsertString("insert into davisbase_tables (table_name) values (\"davisbase_tables\")");
 			parseInsertString("insert into davisbase_tables (table_name) values (\"davisbase_columns\")");
  			parseInsertString("insert into davisbase_columns (table_name,column_name,data_type,ordinal_position,is_nullable) values (\"davisbase_tables\",\"rowid\",\"int\",\"1\",\"no\")");
  			parseInsertString("insert into davisbase_columns (table_name,column_name,data_type,ordinal_position,is_nullable) values (\"davisbase_tables\",\"table_name\",\"text\",\"2\",\"no\")");
  			parseInsertString("insert into davisbase_columns (table_name,column_name,data_type,ordinal_position,is_nullable) values (\"davisbase_columns\",\"rowid\",\"int\",\"1\",\"no\")");
  			parseInsertString("insert into davisbase_columns (table_name,column_name,data_type,ordinal_position,is_nullable) values (\"davisbase_columns\",\"table_name\",\"text\",\"2\",\"no\")");
  			parseInsertString("insert into davisbase_columns (table_name,column_name,data_type,ordinal_position,is_nullable) values (\"davisbase_columns\",\"column_name\",\"text\",\"3\",\"no\")");
  			parseInsertString("insert into davisbase_columns (table_name,column_name,data_type,ordinal_position,is_nullable) values (\"davisbase_columns\",\"data_type\",\"text\",\"4\",\"no\")");
  			parseInsertString("insert into davisbase_columns (table_name,column_name,data_type,ordinal_position,is_nullable) values (\"davisbase_columns\",\"ordinal_position\",\"tinyint\",\"5\",\"no\")");
  			parseInsertString("insert into davisbase_columns (table_name,column_name,data_type,ordinal_position,is_nullable) values (\"davisbase_columns\",\"is_nullable\",\"text\",\"6\",\"no\")");		
		}

		
		/* Variable to collect user input from the prompt */
		String userCommand = ""; 

		while(!isExit) {
			System.out.print(prompt);
			/* toLowerCase() renders command case insensitive */
			userCommand = scanner.next().replace("\n", "").replace("\r", "").trim().toLowerCase();
			// userCommand = userCommand.replace("\n", "").replace("\r", "");
			parseUserCommand(userCommand);
		}
		System.out.println("Exiting...");


	}

	/** ***********************************************************************
	 *  Method definitions
	 */

	/**
	 *  Display the splash screen
	 */
	public static void splashScreen() {
		System.out.println(line("-",80));
        System.out.println("Welcome to DavisBaseLite"); // Display the string.
		System.out.println("DavisBaseLite Version " + getVersion());
		System.out.println(getCopyright());
		System.out.println("\nType \"help;\" to display supported commands.");
		System.out.println(line("-",80));
	}
	
	/**
	 * @param s The String to be repeated
	 * @param num The number of time to repeat String s.
	 * @return String A String object, which is the String s appended to itself num times.
	 */
	public static String line(String s,int num) {
		String a = "";
		for(int i=0;i<num;i++) {
			a += s;
		}
		return a;
	}
	
		/**
		 *  Help: Display supported commands
		 */
		public static void help() {
			System.out.println(line("*",80));
			System.out.println("SUPPORTED COMMANDS");
			System.out.println("All commands below are case insensitive");
			System.out.println();
			System.out.println("\tSELECT * FROM table_name;                        Display all records in the table.");
			System.out.println("\tSELECT * FROM table_name WHERE rowid = <value>;  Display records whose rowid is <id>.");
			System.out.println("\tDROP TABLE table_name;                           Remove table data and its schema.");
			System.out.println("\tVERSION;                                         Show the program version.");
			System.out.println("\tHELP;                                            Show this help information");
			System.out.println("\tEXIT;                                            Exit the program");
			System.out.println();
			System.out.println();
			System.out.println(line("*",80));
		}

	/** return the DavisBase version */
	public static String getVersion() {
		return version;
	}
	
	public static String getCopyright() {
		return copyright;
	}
	
	public static void displayVersion() {
		System.out.println("DavisBaseLite Version " + getVersion());
		System.out.println(getCopyright());
	}
		
	public static void parseUserCommand (String userCommand) {
		
		int sqlcode=0;
		/* commandTokens is an array of Strings that contains one token per array element 
		 * The first token can be used to determine the type of command 
		 * The other tokens can be used to pass relevant parameters to each command-specific
		 * method inside each case statement */
		// String[] commandTokens = userCommand.split(" ");
		ArrayList<String> commandTokens = new ArrayList<String>(Arrays.asList(userCommand.split(" ")));
		

		/*
		*  This switch handles a very small list of hardcoded commands of known syntax.
		*  You will want to rewrite this method to interpret more complex commands. 
		*/
		switch (commandTokens.get(0)) {
			case "select":
				int rownum=0;
				String [][] output= parseQueryString(userCommand);
				for (int i=0; i<output.length ; i++)
				{
					for (int j=0; j<output[i].length ; j++)
					{
						System.out.print(output[i][j]+"\t");
					}
					rownum++;
					System.out.println("");	
				}
				System.out.println((rownum-2)+" rows displayed");
				break;
			case "drop":
				System.out.println("STUB: Calling your method to drop items");
				dropTable(userCommand);
				break;
			case "create":
				sqlcode=parseCreateString(userCommand,"user");
				System.out.println("SQLCODE "+sqlcode+": "+err.getValue(sqlcode));
				break;
			case "insert":
				sqlcode=parseInsertString(userCommand);
				System.out.println("SQLCODE "+sqlcode+": "+err.getValue(sqlcode));
				break;
			case "help":
				help();
				break;
			case "version":
				displayVersion();
				break;
			case "exit":
				isExit = true;
				break;
			case "quit":
				isExit = true;
			default:
				System.out.println("I didn't understand the command: \"" + userCommand + "\"");
				break;
		}
	}
	

	/**
	 *  Stub method for dropping tables
	 *  @param dropTableString is a String of the user input
	 */
	public static void dropTable(String dropTableString) {
		System.out.println("STUB: Calling parseQueryString(String s) to process queries");
		System.out.println("Parsing the string:\"" + dropTableString + "\"");
	}
	
	/**
	 *  Stub method for executing queries
	 *  @param queryString is a String of the user input
	 */
	public static String [][] parseQueryString(String queryString) 
	{
		System.out.println("QUERY: "+queryString);
		int i=1;
		int sqlcode=0;
		int numCol=0;
		int numRec=0;
		String search_keyword = null;
		int pos=99;
		ArrayList<String> tokens = new ArrayList<String>(Arrays.asList(queryString.split(" ")));
		
		String c_list=tokens.get(i++);	
		if (!tokens.get(i++).equals("from"))
		{
			sqlcode= -101;
			System.out.println("SQLCODE "+sqlcode+": "+err.getValue(sqlcode));
			return null;
		}
		String tableName=tokens.get(i++);
		String tableFileName =  tableName+ ".tbl";
		File f = new File(tableFileName);
		if (!f.exists())
		{
			sqlcode= -102;
			System.out.println("SQLCODE "+sqlcode+": "+err.getValue(sqlcode));
			return null;
		}
		//If where clause present
		boolean hasWhere=false;
		if (i<tokens.size())
		{
			hasWhere=true;
			if (!tokens.get(i++).equals("where"))
			{
				sqlcode=-101;
				System.out.println("SQLCODE "+sqlcode+": "+err.getValue(sqlcode));
				return null;
			}
			String clause=tokens.get(i++);
			ArrayList<String> clause_list= new ArrayList<String>(Arrays.asList(clause.split("=")));
 			pos=findPosition(clause_list.get(0));
 			int l=0;
			Stack<Character> st = new Stack<Character>();
			StringBuffer word= new StringBuffer();
			
 			while (l < clause_list.get(1).length())
			{
				char ch=clause_list.get(1).charAt(l++);		
//			    System.out.println("character:- "+ch);
			    switch (ch)
			    {
			    	case '"':	
			    		if (st.isEmpty())
			    			st.push('"');
			    		else if(st.peek() == '"') 	
						{
			    			search_keyword=word.toString();
//							System.out.println("End Word cumul: "+word);
							word.setLength(0);
							st.pop();
						}
			    		break;
			    	case ' ':
			    		break;
			    	default:
			    		word.append(ch);
//			    		System.out.println("Word: "+word);
			    		break;
			    }
			}
		}
		
		try 
		{
			/*  Create RandomAccessFile tableFile in read-write mode.
			 *  Note that this doesn't create the table file in the correct directory structure
			 */
			RandomAccessFile tableFile = new RandomAccessFile(tableFileName, "r");
			DataTypes dt=new DataTypes();
			tableFile.seek(1);
			numRec=tableFile.readByte();

			String [][] op=queryDavis("select table_name,column_name from davisbase_columns where table_name=\""+tableName+"\"");
			for (int m=0; m<op.length; m++)
			{
				if (op[m][0].equals(tableName))
				{
					numCol++;
				}
			}
			
			
			String output[][]=new String[numRec][numCol];
 			String filter_output[][]=null;
 			String temp_array[][]=new String [numRec][numCol];
 			int temp_row=0;
 			int temp_col=0;
			for (int j=0;j<numRec;j++)
			{
				tableFile.seek(j*2+8);
				int data_addr=tableFile.readShort();
				tableFile.seek(data_addr+6);
				int num_col=tableFile.readByte();
				int cumul_size=0;
				
				for (int k=0; k<numCol ;k++)
				{
					tableFile.seek(data_addr+7+k);
					int code=tableFile.readByte();
//					System.out.println(code);
					int size=dt.getSize(String.valueOf(code));
					tableFile.seek(data_addr+7+num_col+cumul_size);
					String s=null;
					switch (size)
					{
						case 99:
							size=code-12;
//							System.out.println(tableFile.getFilePointer());
							byte b[]=new byte[size];
							tableFile.readFully(b);
//							s=String.valueOf(b);
							s=new String(b);
							break;
						case 1:
							s=String.valueOf(tableFile.readByte());
							break;
						case 2:
							s=String.valueOf(tableFile.readShort());
							break;
						case 4:
							s=String.valueOf(tableFile.readInt());
							break;
						case 8:
							s=String.valueOf(tableFile.readDouble());
							break;
					} 
					cumul_size+=size;
//					System.out.print(s);
					output[j][k]=s;
				}
			}
			tableFile.close();

			ArrayList<String> column_list =new ArrayList<String>(Arrays.asList(c_list.split(",")));;
			ArrayList<Integer> allowed_pos=new ArrayList<Integer>();
			if (c_list.equals("*"))
			{
				column_list.clear();
				String ordinal_position[][]=queryDavis("select column_name,ordinal_position from davisbase_columns where table_name=\""+tableName+"\"");
				for (int m=0; m<ordinal_position.length; m++)
  				{
					column_list.add(ordinal_position[m][0]);
					allowed_pos.add(Integer.valueOf(ordinal_position[m][1]));
  				}

			}
			else
			{
				int position=99;
				for (int k=0;k<column_list.size();k++)
	 			{
						String ordinal_position[][]=queryDavis("select table_name,ordinal_position from davisbase_columns where column_name=\""+column_list.get(k)+"\"");
		  				for (int m=0; m<ordinal_position.length; m++)
		  				{
		  					if (ordinal_position[m][0].equals(tableName))
		  					{
		  						position=Integer.parseInt(ordinal_position[m][1]);
		  						break;
		  					}
		  				}
		  				if (position == 99)
		  				{
		  					sqlcode=-105;
		  					System.out.println("SQLCODE "+sqlcode+": "+err.getValue(sqlcode));
		  					return null;
		  				}		
		  				allowed_pos.add(position);
	 			}
			}
			
			//Filter rows
			if (hasWhere)
			{
				for (int j=0;j<numRec;j++)
				{
					if (output[j][pos].equals(search_keyword))
					{
						temp_col=0;
						for (int k=0;k<numCol;k++)
						{
							temp_array[temp_row][temp_col]=output[j][k];
							temp_col++;
						}
						temp_row++;
					}
				}
			}
			else
			{
				for (int j=0;j<numRec;j++)
				{
					temp_col=0;
					for (int k=0;k<numCol;k++)
					{
						temp_array[temp_row][temp_col]=output[j][k];
						temp_col++;
					}
					temp_row++;
				}		
			}
			
			int filter_row=0;
			int filter_col;

			//Header rows
			filter_output=new String [temp_row+2][allowed_pos.size()];
			for (int j=0;j<2;j++)
			{
				if (j==0)
				{
					filter_col=0;
					for (int k=0;k<allowed_pos.size();k++)
					{
						filter_output[filter_row][filter_col]=column_list.get(k);
						filter_col++;
					}
					//Display header
				}
				else
				{
					//Display =
					filter_col=0;
					for (int k=0;k<allowed_pos.size();k++)
					{
						StringBuffer line=new StringBuffer();
						for (int l=0;l<column_list.get(k).length() ;l++)
						{
							line.append("=");
						}
						filter_output[filter_row][filter_col]=line.toString();
						filter_col++;	
					}
				}
				filter_row++;
			}

//			filter_output=new String [temp_row][allowed_pos.size()];
			//Filter columns
			for (int j=0;j<temp_row;j++)
			{
				filter_col=0;
				for (int k=0;k<allowed_pos.size();k++)
				{
					int m=allowed_pos.get(k);
					filter_output[filter_row][filter_col]=temp_array[j][m];
					filter_col++;
				}
				filter_row++;
			}
			
			return filter_output;	
		}
		catch(Exception e) 
		{
			System.out.println(err.getValue(-1000));
			e.printStackTrace();
		}
		return null;
	}
	
	public static String [][] queryDavis(String queryString) 
	{
		System.out.println("DAVIS QUERY: "+queryString);
		int i=1;
		int numCol=0;
		int numRec=0;
		int sqlcode=0;
		String search_keyword = null;
		int pos=99;
		ArrayList<String> tokens = new ArrayList<String>(Arrays.asList(queryString.split(" ")));
		
		String c_list=tokens.get(i++);	
		if (!tokens.get(i++).equals("from"))
		{
			return null;
		}
		String tableName=tokens.get(i++);
		String tableFileName =  tableName+ ".tbl";
		File f = new File(tableFileName);
		if (!f.exists())
		{
			sqlcode= -102;
			System.out.println("SQLCODE "+sqlcode+": "+err.getValue(sqlcode));
			return null;
		}
		//If where clause present
		boolean hasWhere=false;
		if (i<tokens.size())
		{
			hasWhere=true;
			if (!tokens.get(i++).equals("where"))
			{
				sqlcode=-101;
				System.out.println("SQLCODE "+sqlcode+": "+err.getValue(sqlcode));
				return null;
			}
			String clause=tokens.get(i++);
			ArrayList<String> clause_list= new ArrayList<String>(Arrays.asList(clause.split("=")));
 			pos=findPosition(clause_list.get(0));
 			int l=0;
			Stack<Character> st = new Stack<Character>();
			StringBuffer word= new StringBuffer();
			
 			while (l < clause_list.get(1).length())
			{
				char ch=clause_list.get(1).charAt(l++);		
//			    System.out.println("character:- "+ch);
			    switch (ch)
			    {
			    	case '"':	
			    		if (st.isEmpty())
			    			st.push('"');
			    		else if(st.peek() == '"') 	
						{
			    			search_keyword=word.toString();
//							System.out.println("End Word cumul: "+word);
							word.setLength(0);
							st.pop();
						}
			    		break;
			    	case ' ':
			    		break;
			    	default:
			    		word.append(ch);
//			    		System.out.println("Word: "+word);
			    		break;
			    }
			}
		}
		
		try 
		{
			/*  Create RandomAccessFile tableFile in read-write mode.
			 *  Note that this doesn't create the table file in the correct directory structure
			 */
			RandomAccessFile tableFile = new RandomAccessFile(tableFileName, "r");
			DataTypes dt=new DataTypes();
			tableFile.seek(1);
			numRec=tableFile.readByte();
			//HARD CODED
			numCol=5;
			
			String output[][]=new String[numRec][numCol];
 			String filter_output[][]=null;
 			String temp_array[][]=new String [numRec][numCol];
 			int temp_row=0;
 			int temp_col=0;
			for (int j=0;j<numRec;j++)
			{
				tableFile.seek(j*2+8);
				int data_addr=tableFile.readShort();
				tableFile.seek(data_addr+6);
				int num_col=tableFile.readByte();
				int cumul_size=0;
				
				for (int k=0; k<numCol ;k++)
				{
					tableFile.seek(data_addr+7+k);
					int code=tableFile.readByte();
//					System.out.println(code);
					int size=dt.getSize(String.valueOf(code));
					tableFile.seek(data_addr+7+num_col+cumul_size);
					String s=null;
					switch (size)
					{
						case 99:
							size=code-12;
//							System.out.println(tableFile.getFilePointer());
							byte b[]=new byte[size];
							tableFile.readFully(b);
//							s=String.valueOf(b);
							s=new String(b);
							break;
						case 1:
							s=String.valueOf(tableFile.readByte());
							break;
						case 2:
							s=String.valueOf(tableFile.readShort());
							break;
						case 4:
							s=String.valueOf(tableFile.readInt());
							break;
						case 8:
							s=String.valueOf(tableFile.readDouble());
							break;
					} 
					cumul_size+=size;
//					System.out.print(s);
					output[j][k]=s;
				}
			}
			tableFile.close();

			ArrayList<String> column_list = new ArrayList<String>(Arrays.asList(c_list.split(",")));
			ArrayList<Integer> allowed_pos=new ArrayList<Integer>();
			for (int k=0;k<column_list.size();k++)
 			{
				//HARD CODED
	  				switch (column_list.get(k))
	  				{
	  				case "table_name":
	  					allowed_pos.add(0);
	  					break;
	  				case "column_name":
	  					allowed_pos.add(1);
	  					break;
	  				case "data_type":
	  					allowed_pos.add(2);
	  					break;
	  				case "ordinal_position":
	  					allowed_pos.add(3);
	  					break;
	  				case "is_nullable":
	  					allowed_pos.add(4);
	  					break;
	  				}
 			}
			
			//Filter rows
			if (hasWhere)
			{
				for (int j=0;j<numRec;j++)
				{
					if (output[j][pos].equals(search_keyword))
					{
						temp_col=0;
						for (int k=0;k<numCol;k++)
						{
							temp_array[temp_row][temp_col]=output[j][k];
							temp_col++;
						}
						temp_row++;
					}
				}
			}
			else
			{
				for (int j=0;j<numRec;j++)
				{
					temp_col=0;
					for (int k=0;k<numCol;k++)
					{
						temp_array[temp_row][temp_col]=output[j][k];
						temp_col++;
					}
					temp_row++;
				}		
			}
			
			int filter_row=0;
			int filter_col;
			filter_output=new String [temp_row][allowed_pos.size()];
			//Filter columns
			for (int j=0;j<temp_row;j++)
			{
				filter_col=0;
				for (int k=0;k<allowed_pos.size();k++)
				{
					int m=allowed_pos.get(k);
					filter_output[filter_row][filter_col]=temp_array[j][m];
					filter_col++;
				}
				filter_row++;
			}
			
			return filter_output;	
		}
		catch(Exception e) 
		{
			System.out.println(err.getValue(-1000));
			e.printStackTrace();
		}
		return null;
	}
	
	public static int parseCreateString(String createTableString,String str) {
		int sqlcode=0;
		int i=1;
//		System.out.println("STUB: Calling your method to create a table");
//		System.out.println("Parsing the string:\"" + createTableString + "\"");
		ArrayList<String> createTokens = new ArrayList<String>(Arrays.asList(createTableString.split(" ")));
		
		/* Define table file name */
		String tableName=createTokens.get(i++);
		String tableFileName =  tableName+ ".tbl";
		File f = new File(tableFileName);
		if (f.exists())
			return -104;

		/* YOUR CODE GOES HERE */
		
		/*  Code to create a .tbl file to contain table data */
		try {
			/*  Create RandomAccessFile tableFile in read-write mode.
			 *  Note that this doesn't create the table file in the correct directory structure
			 */
			RandomAccessFile tableFile = new RandomAccessFile(tableFileName, "rw");
			tableFile.setLength(pageSize);
//			int recordLocation = 0;
//			int currentPage = 0;
//			int pageLocation = pageSize * currentPage;
			// Page code
			tableFile.writeByte(13);
			//Number of records in page
			tableFile.writeByte(0);
			//Last occupied memory location
			tableFile.writeShort(pageSize);
			//Right sibling(4 bytes)
			for(int j=0;j<4;j++)
				tableFile.writeByte(255);
			
			tableFile.close();
			if (str.equals("user") )
			{
				parseInsertString("insert into davisbase_tables (table_name) values (\""+tableName+"\")");
				
				//Extract column list
				String c_list=createTableString.substring(createTableString.indexOf("(")+1,createTableString.indexOf(")"));
				ArrayList<String> column_list = new ArrayList<String>(Arrays.asList(c_list.split(",")));
				for (int j=0;j< column_list.size();j++)
				{
					ArrayList<String> c = new ArrayList<String>(Arrays.asList(column_list.get(j).split(" ")));
					parseInsertString("insert into davisbase_columns (table_name,column_name,data_type,ordinal_position,is_nullable) "
						+ "values (\""+tableName+"\",\""+c.get(0)+"\",\""+c.get(1)+"\",\""+j+"\",\"no\")");
				}
			}
			System.out.println(tableFileName+" created.");
		}
		catch(Exception e) 
		{
			System.out.println(err.getValue(-1000));
			e.printStackTrace();
		}

		return sqlcode;
	}
	
	public static int parseInsertString(String insertString) 
	{
		RandomAccessFile tableFile=null;
		DataTypes dt=new DataTypes();
		int sqlcode=0;
		int i=1;
		ArrayList<String> createTokens = new ArrayList<String>(Arrays.asList(insertString.split(" ")));
		ArrayList<String> column_value_list = new ArrayList<String>();
		try {
			//Validate syntax
			if (! createTokens.get(i++).equals("into"))
			{
				System.out.println("Expected into, got "+createTokens.get(i-1));
				return -101;
			}
			String tableName=createTokens.get(i++);
			String tableFileName = tableName+ ".tbl";
			File f = new File(tableFileName);
			if (!f.exists())
				return -102;
			
			String columns=createTokens.get(i++);
			//Extract column list
			String c_list=insertString.substring(insertString.indexOf("(")+1,insertString.indexOf(")"));
			ArrayList<String> column_list = new ArrayList<String>(Arrays.asList(c_list.split(",")));
//			System.out.println(column_list);
			
			if (!createTokens.get(i++).equals("values"))
				return -101;
			
			//Extract values
			String column_values=insertString.substring(insertString.indexOf("values")+7,insertString.lastIndexOf(')')+1);
			int k=0;
			Stack<Character> st = new Stack<Character>();
			StringBuffer word= new StringBuffer();
//			System.out.println(column_values);
			int recordSize=0;
			int totrecordSize=0;
			while (k < column_values.length())
			{
				char ch=column_values.charAt(k++);		
//			    System.out.println("character:- "+ch);
			    switch (ch)
			    {
			    	case '(':
			    		st.push('(');
			    		break;
			    	case ')':	
			    		if (st.peek() == '(')
						{
							column_value_list.add(word.toString());
//							System.out.println("End Word cumul: "+word);
							word.setLength(0);
						}
			    		st.pop();
			    		break;
			    	case ',':
						if (st.peek() == '(')
						{
							column_value_list.add(word.toString());
//							System.out.println("Word cumul: "+word);
							word.setLength(0);
						}
						else
						{
							word.append(ch);
//							System.out.println(word);
						}
						break;
			    	case '"':	
			    		if (st.peek() == '(')
			    			st.push('"');
			    		else
			    			st.pop();
			    		break;
			    	case ' ':
			    		break;
			    	default:
			    		word.append(ch);
//			    		System.out.println("Word: "+word);
			    		break;
				}
//			    System.out.println("Stack:- "+st.peek());	
			}
			if (!st.isEmpty())
			{
				System.out.println("Values incorrect");
				return -101;
			}
//			System.out.println(column_value_list);	
			
			tableFile = new RandomAccessFile(tableFileName, "rw");
			tableFile.setLength(pageSize);
			for (int j=0;j<column_list.size();j++)
			{
				//Get column datatype from davisbase_column
//				System.out.println(column_list.get(j));
//				String data_type=findDataType(column_list.get(j));
				String data_type[][]=null;
				String type=null;
//				if (column_list.get(j).equals("davisbase_tables"))
//				{
//					type="text";
//				}
//				else if(column_list.get(j).equals("davisbase_columns"))
//				{
//					switch(column_list.get(j))
//					{
//					case "table_name":
//					case "column_name":
//					case "data_type":
//					case "is_nullable":
//						type="text";
//						break;
//					case "ordinal_position":
//						type="tinyint";
//					}
//				}
//				else
//				{
					data_type=parseQueryString("select table_name,data_type from davisbase_columns where column_name=\""+column_list.get(j)+"\"");
					for (int m=0; m<data_type.length; m++)
					{
						if (data_type[m][0].equals(tableName))
						{
							type=data_type[m][1];
						break;
						}
					}
//				}
//				System.out.println("Data type: "+data_type[0][0]);
				int size=0;
				size=dt.getSize(type);
				if (size==0)
				{
					size=column_value_list.get(j).length();
				}
				recordSize+=size;
			}
//			System.out.println(column_value_list.size());
// 			System.out.println(recordSize);
			totrecordSize=2+4+1+column_value_list.size()+recordSize;
//			System.out.println(totrecordSize);
			//Page header
			tableFile.seek(1);
			int numRec=tableFile.readByte();
			tableFile.seek(1);
			tableFile.write(numRec+1);
			tableFile.seek(2);
			int currentLocation=tableFile.readShort();
			tableFile.seek(2);
			tableFile.writeShort(currentLocation-totrecordSize);	
			tableFile.seek(numRec*2+8);
			tableFile.writeShort(currentLocation-totrecordSize);
			
			//Record header
			tableFile.seek(currentLocation-totrecordSize);
				//payload
			int payload=1+column_value_list.size()+recordSize;
			tableFile.writeShort(payload);
				//RowID
			tableFile.writeInt(numRec+1);
				//Number of columns
			tableFile.writeByte(column_value_list.size());
				//Code of each column
			if (column_list.size() != column_value_list.size())
			{
				System.out.println("Incorrect number of columns"+column_list.size()+","+column_value_list.size());
				return -101;
			}	
			for (int j=0;j<column_list.size();j++)
			{
				String data_type[][]=queryDavis("select table_name,data_type from davisbase_columns where column_name=\"+column_list.get(j)+\"");
//				System.out.println("Data type: "+data_type[0][0]);
				String type=null;
				for (int m=0; m<data_type.length; m++)
				{
					if (data_type[m][0].equals(tableName))
					{
						type=data_type[m][1];
						break;
					}
				}
				int c=dt.getCode(type);
				
				//If text
				if (c==12)
				{
					c+=column_value_list.get(j).length();
				}
//				System.out.println(totrecordSize);
// 				System.out.println(recordSize);
// 				System.out.println(c);
 				tableFile.writeByte(c);
			}
			
			
			//Record 
//			System.out.println(currentLocation);
			for (int j=0; j<column_value_list.size();j++)
			{
//				
				String data_type[][]=parseQueryString("select table_name,data_type from davisbase_columns where column_name=\""+column_list.get(j)+"\"");
//				System.out.println("Data type: "+data_type[0][0]);
				int bytes=0;
				String type=null;
				for (int m=0; m<data_type.length; m++)
				{
					if (data_type[m][0].equals(tableName))
					{
						type=data_type[m][1];
						break;
					}
				}
				bytes=dt.getSize(type);
//				System.out.println(bytes);
				switch (bytes)
				{
					case 0:
						tableFile.writeBytes(column_value_list.get(j));
						break;
					case 1:
						tableFile.writeByte(Integer.parseInt(column_value_list.get(j)));
						break;
					case 2:
						tableFile.writeShort(Integer.parseInt(column_value_list.get(j)));
						break;
					case 4:
						tableFile.writeInt(Integer.parseInt(column_value_list.get(j)));
						break;
					case 8:
						tableFile.writeDouble(Double.parseDouble(column_value_list.get(j)));
						break;
				} 
			}
			tableFile.close();
		}
		catch(Exception e) 
		{
			System.out.println(err.getValue(-1000));
			e.printStackTrace();
		}
		finally 
		{
			try
			{
				tableFile.close();
			}
			catch(Exception e) 
			{
				System.out.println(err.getValue(-1000));
				e.printStackTrace();
			}
	    }
	return sqlcode;
	}
	
	static String findDataType(String str)
	{
		switch(str)
		{
		case "table_name":
		case "column_name":
		case "data_type":
		case "is_nullable":
		case "emp_name":
			return "text";
		case "ordinal_position":
			return "tinyint";
		case "emp_id":
			return "int";
		case "ph_num":
			return "int";
		default:
			return null;
		}
	}
	static int findPosition(String str)
	{
		switch(str)
		{
		case "table_name":
			return 0;
		case "column_name":
			return 1;
		case "data_type":
			return 2;
		case "ordinal_position":
			return 3;
		case "is_nullable":
			return 4;
		default:
			return 99;
		}	
	}
}