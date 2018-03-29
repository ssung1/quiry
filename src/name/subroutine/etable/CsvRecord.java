package name.subroutine.etable;

import java.util.*;
import java.sql.*;

/**
 * A record very much like the one in relational databases.
 *
 * Its fields are defined elsewhere and are shared with other records.
 */
public class CsvRecord extends AbstractRecord
{
    public CsvRecord( List field_lst )
    {
	super( field_lst );
    }


    public String toString()
    {
	return toString( this );
    }

    /**
     * Creates a string representation of the given record
     */
    public static String toString( Record record )
    {
	//this will turn a vector of words into a string of CSV format
	//it's very simple, if the word contains a comma then surround
	//it by quotes if not, just put it in and add a comma, the
	//END!

	int i; //super counter variable
	StringBuffer buf = new StringBuffer();
	String temp; //temporary string to hold the element in the vector
	int j; //counter variable
		
	for( i = 0; i < record.size(); i++ ){
	    temp = record.get( i ).toString();
	    
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
	    if( (temp.indexOf( ',' ) != -1 ) 
	    ||  (temp.indexOf( '\"' ) != -1 ) ){
		
		//we need to add quotes around the word
		buf.append( '\"' );
		buf.append( temp );
		buf.append( '\"' );	
	    }
	    else{ //no quotes around the word
		buf.append( temp );
	    }

	    //make sure we add a comma to separate the words
	    if( record.size() - i > 1 ){
		buf.append( ',' );
	    }
	} //end for to go through all the words
	
	return buf.toString(); //tada i wrote java!!
    }

    /**
     * Creates a CSV record from an array of strings
     */
    public static String toString( String[] record )
    {
	//this will turn a vector of words into a string of CSV format
	//it's very simple, if the word contains a comma then surround
	//it by quotes if not, just put it in and add a comma, the
	//END!

	int i; //super counter variable
	StringBuffer buf = new StringBuffer();
	String temp; //temporary string to hold the element in the vector
	int j; //counter variable
		
	for( i = 0; i < record.length; i++ ){
	    temp = record[i];
	    
	    //make sure we add a comma to separate the words
            if( i > 0 ){
                buf.append( ',' );
            }

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
	    if( (temp.indexOf( ',' ) != -1 ) 
	    ||  (temp.indexOf( '\"' ) != -1 ) ){
		
		//we need to add quotes around the word
		buf.append( '\"' );
		buf.append( temp );
		buf.append( '\"' );	
	    }
	    else{ //no quotes around the word
		buf.append( temp );
	    }

	} //end for to go through all the words
	
	return buf.toString(); //tada i wrote java!!
    }
}
