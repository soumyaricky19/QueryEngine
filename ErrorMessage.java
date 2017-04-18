package db;

import java.util.HashMap;

public class ErrorMessage 
{
	HashMap<Integer,String> hm=new HashMap<Integer,String>();
	ErrorMessage()
	{
		hm.put(0,"Successful");
		hm.put(100,"No records found");
		hm.put(-101, "Incorrect syntax");
		hm.put(-102, "Table not found");
		hm.put(-103, "Cannot find");
		hm.put(-104, "Table already exists");
		hm.put(-105, "Incorrect column name");
		hm.put(-106, "Insufficient columns");
		hm.put(-107, "Primary key not present");
		hm.put(-108, "Inserting null into not null column");
		hm.put(-109, "Primary key must be integer type");
		hm.put(-110, "Unrecognized constraint");
		hm.put(-111, "Primary key column not given in where");
		hm.put(-112, "Non integer value not supported");
		hm.put(-113, "Duplicate in primary key column");
		hm.put(-114, "Unsupported operator in where clause");
		hm.put(-1000, "Error -1000: Unexpected error");
		
	}
	String getValue(int x)
	{
		return hm.get(x);
	}
}
