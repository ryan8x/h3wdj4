package com.ryanliang.knockknock;

import javax.swing.JOptionPane;

/**
 * Utility class defines static utility method(s). 
 * @author Ryan L.
 * @version $Revision$
 * @since 1.7
 */
public class Utility {

	/**
	 * This method determines if a string content is a number.
	 * @param str Is a string
	 * @return True if the string content is a number otherwise false.
	 */
	public static boolean isNumeric(String str)  
	{  
	  try  
	  {  
	    double d = Double.parseDouble(str);  
	  }  
	  catch(NumberFormatException ee)  
	  {  
	    return false;  
	  }  
	  return true;  
	}
	
	/**
	 * This method simply pops up a dialog window with a message.
	 * @param message Is a string
	 */
	public static void displayErrorMessage(String message) {

		JOptionPane.showMessageDialog(null, message, "alert", JOptionPane.ERROR_MESSAGE);  

	}
}
