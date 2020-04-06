
import java.io.*;
import java.lang.Exception.*;
import java.util.ArrayList;

public class ChallengeGetter 
{
	public ArrayList<String> challenges;
	
	public ChallengeGetter(String file) 
	{
		challenges = new ArrayList<String>();
		try
		{
			BufferedReader in = new BufferedReader(new FileReader(new File("src/"+file)));
			String line;
			System.out.println("Reading file " + file);
			while((line = in.readLine()) != null)
			{
				challenges.add(line);
			}
			System.out.println("File read sucessfully!");
		}
		catch (Exception e)
		{
			System.out.println(e);
		}
	}
}
