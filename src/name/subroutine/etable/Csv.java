package name.subroutine.etable;

import java.util.*;
import java.lang.*;
import java.io.*;

/**
 * This class contains functions for processing comma separated values
 *
 * It follows the same convention as that of Excel
 */
public class Csv
{

    /**
     * Calls parseCSV
     */
    public static String[] toArray( String str )
    {
	return parseCSV( str );
    }

    /**
     * returns a list of String objects resulting from parsing
     * a given string as comma separated values
     */
    public static String[] parseCSV( String str )
    {
	//in this function we will convert normal strings in a line
	//read from a CSV file into list format separating them. It is
	//cool

	//strings may start with a " if it does, then we must make
	//sure to handle the commas inbetween the quotes

	String single; //used to hold a single word
	String newstr; //new string

	//vectors to hold the single words we grab out of the function
	//allocate enough memory to hold all the words
	Vector words = new Vector(); 

	newstr = str;

	//anonymouse usage counter variable
	//mightymouse gets better velocity
	int i; 

	//anonymouse usage boolean variable
	boolean check;

	//used to get all the words and reduce string everytime
	while( newstr.length() > 0 ){
	    //first we trim off a string
	    //remove spaces at front and end (what a nice function)
	    //newstr = newstr.trim(); 

	    //we no longer trim because the field might be a space =(

	    //now we need to determine whether to remove quotes or not
	    if( newstr.charAt(0) == '\"') {

		//we will have to determine which " is not part of a
		//double quote. When we do, we will have determined
		//the end of the string

		check = false;
		single = "error";
		i = 1; //we start searching from the beginning
		do{ //keep searching until we find a quote by itself

		    i = newstr.indexOf('\"',i); //find it starting with i
		    if(i==-1){
			throw new IllegalArgumentException( 
			      "Could not find end quote"
                        );
		    } //we did not find any more quotes!!
		    else {
			//check for a double quote
			if( newstr.length() > i+1 
			    && newstr.charAt(i+1) == '\"' ){
			    //it is a double, start search over
			    //at next letter
			    
			    //we must remove the double quote
			    newstr = newstr.substring(0, i).
				concat( newstr.substring(i+1) );

			    //increment i by one so we don't find that
			    //first quote again
			    i++;

			    if( i > newstr.length() ){		
				//we could not determine an end quote
				throw new IllegalArgumentException(
				    "Could not find end quote"
				);
			    }
							
			}
			else{
			    //we have found the end character
			    //set string to 1st word, without quotes
			    single = newstr.substring(1,i);

			    //set newstr to everything after first word
			    newstr = newstr.substring(i+1);
			    check = true;
			}
			//the marker is at the end
			if( i == newstr.length()-1 ){
			    //set string to 1st word, without quotes
			    single = newstr.substring(1,i);
			    //set newstr to everything after first word
			    newstr = "";
			    check = true;
			}	
		    } //we have found a match
 		} while(check == false);

		//in case there is stuff after the quote

		i = newstr.indexOf(',');

		if( i != -1 ){ //that means there IS a comma

		    //For now, we just add whatever is before comma onto single
		    //and whatever is after comma into the new str!
				
		    //add stuff to single
		    if( i > 0 ){
			//concat single to include stuff before the next comma
			single = single.concat( newstr.substring(0, i ) ); 
		    }
		    if( i != newstr.length() - 1 ){
			newstr = newstr.substring(i+1);
		    } 
		    else{ 
			//this means the comma is the last letter,
			//that's just weird
			throw new IllegalArgumentException(
			    "Comma is at end of line"
			    );
		    }
		}
		else{ //no more commas, make single everything
		    single = single.concat(newstr);
		    newstr = "";
		} //end of else

					
	    }  //end of IF to determine if word has quotes around it or not
	    else { //this one does not contain a quote, (no quotes around word)
		//very simple, we just search for the next comma
		i = newstr.indexOf(',');
		//see if we found a comma or not
		if( i != -1){ //this means we found a comma

		    //make everything before the comma into single and
		    //after into newstr

		    single = newstr.substring(0, i);
		    newstr = newstr.substring(i+1);	
		}
		else{	
		    //we did not find a comma, make single everything
		    //and make newstr empty
		    single = newstr;
		    newstr = "";
		} 
	    }
	    //end of else that determine that the word did NOT have a
	    //quote around it
			
	    //System.out.println(single);				
	    //System.out.println(newstr);
	    words.add( single );
	}

	String retval[] = new String[ words.size() ];
	retval = (String[])words.toArray( retval );
	return retval;

    } //end function parseCSV

    /**
     * Creates a CSV record from given words
     *
     * Handles quotes and commas correctly
     */
    public static String createCSV( List words )
    {
	String[] word_list;
	word_list = (String[])words.toArray( new String[0] );

	return createCSV( word_list );
    }

    /**
     * Calls createCSV
     */
    public static String toCSV( String[] words )
    {
	return createCSV( words );
    }

    /**
     * Creates a CSV record from given words
     *
     * Handles quotes and commas correctly
     */
    public static String createCSV( String[] words )
    {
	//this will turn a vector of words into a string of CSV format
	//it's very simple, if the word contains a comma then surround
	//it by quotes if not, just put it in and add a comma, the
	//END!

	int i; //super counter variable
	StringBuffer buf = new StringBuffer();
	String temp; //temporary string to hold the element in the vector
	int j; //counter variable
		
	for(i=0;i<words.length;i++){
	    temp = words[i];
	    
	    //we have to change all the quotes into double quotes
	    //inside temp
	    j=0;
	    //keep going until it gets to the end
	    while(j<temp.length()){	
		//if the character is a " then we must
		//make it a double quote
		if( temp.charAt(j) == '\"'){
		    temp = temp.substring(0,j+1).
			concat( temp.substring(j) );
		    j++; //skip past the double quote
		}
		j++;
	    }	
	    //this means there IS a comma in there
	    if( (temp.indexOf(',') != -1) || (temp.indexOf('\"') != -1) ){
		
		//we need to add quotes around the word
		buf.append( '\"' );
		buf.append(temp);
		buf.append( '\"' );	
	    }
	    else{ //no quotes around the word
		buf.append(temp);
	    }
	    //make sure we add a comma to separate the words
	    buf.append( ',' );
	} //end for to go through all the words
	
	// we now need to move the last comma because well, there's no
	// more words after that
	
	//this should remove the last character
	buf.setLength( buf.length() - 1 );
	
	return buf.toString(); //tada i wrote java!!
	
    } //end function createCSV


    /**
     * Creates a CSV record set from the given words and column count
     *
     * Handles quotes and commas correctly
     */
    public static void toCSV( PrintWriter out, List words, int col_cnt )
    {
	toCSV( out, (String[])words.toArray( new String[0] ), col_cnt );
    }

    /**
     * Creates a CSV record set from the given words and column count
     *
     * Handles quotes and commas correctly
     */
    public static void toCSV( PrintWriter out, String[] words, 
			      int col_cnt )
    {
	//this will turn a vector of words into a string of CSV format
	//it's very simple, if the word contains a comma then surround
	//it by quotes if not, just put it in and add a comma, the
	//END!

	int i; //super counter variable
	String temp; //temporary string to hold the element in the vector
	int j; //counter variable

	int col_idx = 0;
	for( i = 0; i < words.length; i++ ){
	    //make sure we add a comma to separate the words
	    if( col_idx > 0 ){
		out.print( ',' );
	    }

	    temp = words[i];
	    
	    //we have to change all the quotes into double quotes
	    //inside temp
	    j = 0;
	    //keep going until it gets to the end
	    while( j < temp.length() ){
		//if the character is a " then we must
		//make it a double quote
		if( temp.charAt( j ) == '\"' ){
		    temp = temp.substring( 0, j + 1 ).
			concat( temp.substring( j ) );
		    j++; //skip past the double quote
		}
		j++;
	    }	

	    //this means there IS a comma in there
	    if( (temp.indexOf( ',' ) != -1 ) || (temp.indexOf( '\"' ) != -1) ){
		
		//we need to add quotes around the word
		out.print( '"' );
		out.print( temp );
		out.print( '"' );
	    }
	    else{ //no quotes around the word
		out.print( temp );
	    }

	    col_idx++;
	    if( col_idx >= col_cnt ){
		out.println( "" );
		col_idx = 0;
	    }
	} //end for to go through all the words
    } //end function createCSV

    public static void main(String[] argc){
	int i;
	String[] words = parseCSV(createCSV(parseCSV("123ddd,\"I hate you\"\"\"\"\"\"\",\"!\"\",\"\",\"\"\"\"\"\"\"\"\"\"\",\",,,,, helo\",,,,Darn,, ")));


	System.out.print(" Number of words: " );
	System.out.println( words.length );		

	for(i=0;i<words.length;i++){
		System.out.println( words[i] );
	}
    } //end public class main
}
