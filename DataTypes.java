package db;

public class DataTypes {
	String s[][]={ 
		{"tinynull", "00", "1"},
		{"smallnull", "01","2"},
		{"intnull", "02","4"}, 
		{"doublenull", "03", "8"},
		{"tinyint", "04","1"},
		{"smallint", "05","2"},
		{"int", "06","4"},
		{"bigint", "07","8"},
		{"real", "08","4"},
		{"double", "09","8"},
		{"datetime", "10","8"},
		{"date", "11","8"},
		{"text", "12","0"}
				};
	int getCode(String str)
	{
		int code=99;
//		System.out.println(str);
		for(int i=0;i<s.length;i++)
		{
			if (str.equals(s[i][0]))
			{
				code=Integer.parseInt(s[i][1]);
				break;
			}
		}
		return code;
	}
	int getSize(String str)
	{ 
		int size=99;
//		System.out.println("Data type string:"+ str);
		for(int i=0;i<s.length;i++)
		{
			if (str.equals(s[i][0]) || str.equals(String.valueOf(Integer.parseInt(s[i][1]))))
			{
				size=Integer.parseInt(s[i][2]);
//				System.out.println("Return size: "+size);
				break;
			}
		}
		return size;
	}
}
