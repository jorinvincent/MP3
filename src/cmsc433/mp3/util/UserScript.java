package cmsc433.mp3.util;

import java.io.File;
import java.io.FileNotFoundException;
import java.util.ArrayList;
import java.util.Scanner;

import cmsc433.mp3.enums.AccessRequestType;
import cmsc433.mp3.enums.AccessType;

/**
 * Class of scripts run by user actors.
 * 
 * A script consists of an ArrayList of steps, where each step is an ArrayList of
 * AccessRequest / ManagementRequest objects.  The idea is that each request in step
 * should be sent, then the corresponding response received, before the next step is
 * executed.
 * 
 * @author Rance Cleaveland
 *
 */
public class UserScript {
	
	private ArrayList<ArrayList<Object>> script;  // List of steps
	// TODO Add sleep step. 
	
	
	/**
	 * Create empty script.
	 */
	public UserScript() {
		this.script = new ArrayList<ArrayList<Object>>();
	}

	/**
	 * Create script from list of steps.  List is not copied.
	 * 
	 * @param script	List of steps
	 */
	public UserScript(ArrayList<ArrayList<Object>> script) {	
		this.script = script;
	}
	
	/**
	 * Return list of steps in script.
	 * 
	 * @return	List of steps
	 */
	private ArrayList<ArrayList<Object>> getScript() {
		return script;
	}
	
	/**
	 * Given list of requests, return script of sequence of steps, each step
	 * containing a single request.
	 * 
	 * @param msgSeq	List of requests
	 * @return			Script in which each step contains a single request
	 */
	public static UserScript makeSequential (ArrayList<Object> msgSeq) {
		ArrayList<ArrayList<Object>> newScript = new ArrayList<ArrayList<Object>>();
		for (Object m : msgSeq) {
			ArrayList<Object> step = new ArrayList<Object>();
			step.add(m);
			newScript.add(step);
		}
		return new UserScript(newScript);
	}
	
	/**
	 * Give list of requests, return script containing single step of all requests.
	 * @param msgs	List of messages
	 * @return		Script containing single step of (clone of) list of messages.
	 */
	public static UserScript makeConcurrent (ArrayList<Object> msgs) {
		ArrayList<ArrayList<Object>> newScript = new ArrayList<ArrayList<Object>>();
		newScript.add((ArrayList<Object>)msgs.clone());
		return new UserScript (newScript);
	}
	
	/**
	 * Form new script by concatenating second script onto end of first one.
	 * 
	 * @param s1	First script in concatenation
	 * @param s2	Second script in concatenation
	 * @return		Concatenated script
	 */
	public static UserScript concatenate (UserScript s1, UserScript s2) {
		ArrayList<ArrayList<Object>> newScript = new ArrayList<ArrayList<Object>>();
		newScript.addAll(s1.getScript());
		newScript.addAll(s2.getScript());
		return new UserScript(newScript);
	}
	
	/**
	 * Determines is script has no steps in it.
	 * 
	 * @return	Boolean indicating if script is finished
	 */
	public boolean isDone() {
		return script.isEmpty();
	}
	
	/**
	 * Return first step in a script, if script is non-empty.
	 * 
	 * @return	First step
	 * @throws Exception	Thrown if script has no steps
	 */
	public ArrayList<Object> firstStep () throws Exception {
		if (isDone()) {
			throw new Exception ("Empty script");
		}
		else {
			return ((ArrayList<Object>) script.get(0).clone());
		}
	}
	
	/**
	 * Returns script minus first step, if script is non-empty.
	 * @return	Rest of script, minus first step
	 * @throws Exception	Thrown is script has no steps
	 */
	public UserScript rest() throws Exception {
		if (isDone()) {
			throw new Exception ("Empty script");
		}
		else {			
			return new UserScript (new ArrayList<ArrayList<Object>> (script.subList(1, script.size())));
		}
	}
	
	/**
	 * 
	 * @param script A string representation of the script, in the form:<br>
	 * e = (Write/Read)-(Request-n/Request-b/Release) (Resource Name)<br>
	 *   = (Enable/Disable) (Resource Name)<br>
	 *   = Sleep (Duration)<br>
	 *   = e | e<br>
	 * Where the entire script is a series of one or more e's on different lines    
	 * @return A UserScript object representing the script
	 */
	public static UserScript fromString (String script) {
		ArrayList<ArrayList<Object>> result = new ArrayList<ArrayList<Object>> ();
		
		String[] lines = script.split("(\r|\n)+");
		for (String line : lines) {
			if (line.isEmpty()) 
				continue;
			
			ArrayList<Object> thisLine = new ArrayList<Object> ();
			
			String[] statements = line.trim().split("\\|");
			for (String statement : statements) {				
				
				String[] parts = statement.trim().split("\\s");
				
				if (parts.length < 2) 
					throw new IllegalArgumentException("Script statement: " + statement + " did not have enough arguments\n\ton line: " + line);
				
				String command = parts[0];
				String resource_name = parts[parts.length - 1];
				
				for (int i = 1; i < parts.length - 1; i++)
					if (!parts[i].isEmpty())
						throw new IllegalArgumentException("Script statement: " + statement + " had too many arguments\n\ton line: " + line);
				
				Object action;
				if (command.equalsIgnoreCase("write-request-n")) {
					action = new AccessRequest(resource_name, AccessRequestType.EXCLUSIVE_WRITE_NONBLOCKING); 
				} else if (command.equalsIgnoreCase("write-request-b")) {
					action = new AccessRequest(resource_name, AccessRequestType.EXCLUSIVE_WRITE_BLOCKING);
				} else if (command.equalsIgnoreCase("read-request-n")) {
					action = new AccessRequest(resource_name, AccessRequestType.CONCURRENT_READ_NONBLOCKING);
				} else if (command.equalsIgnoreCase("read-request-b")) {
					action = new AccessRequest(resource_name, AccessRequestType.CONCURRENT_READ_BLOCKING);
				} else if (command.equalsIgnoreCase("write-release")) {
					action = new AccessRelease(resource_name, AccessType.EXCLUSIVE_WRITE);
				} else if (command.equalsIgnoreCase("read-release")) {
					action = new AccessRelease(resource_name, AccessType.CONCURRENT_READ);
				} else if (command.equalsIgnoreCase("sleep")) { 
					action = new SleepStep (Long.parseLong(resource_name));
				} else {
					throw new IllegalArgumentException("Illegal command: " + command + "\n\ton the line: " + line);
				}
				thisLine.add(action);
			}
			result.add(thisLine);
		}
		return new UserScript(result);
	}
	
	/**
	 * Creates a UserScript object from the specified file (using the same grammar as the fromString method)
	 * @param filename - The name of the file to make the script from
	 * @return A UserScript that contains the commands specified by the script in the file.
	 * @throws FileNotFoundException
	 */
	public static UserScript fromFile (String filename) throws FileNotFoundException {
		Scanner input = new Scanner (new File(filename));
		StringBuilder str = new StringBuilder();
		while (input.hasNext()) {
			str.append(input.nextLine());
			str.append('\n');
		}
		input.close();
		
		return fromString(str.toString());
	}
}
