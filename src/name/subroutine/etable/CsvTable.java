package name.subroutine.etable;

import java.util.*;
import java.io.*;
import java.sql.*;

public class CsvTable extends AbstractTable
{
    public CsvTable()
    {
	init();
    }

    public void init()
    {
	super.init();
    }

    public int pushFld( String name )
    {
	CsvField f;
	f = new CsvField( name );

	fieldLst().add( f );
	return 1;
    }

    public Record createRecord()
    {
	return new CsvRecord( fieldLst() );
    }
    public Field createField( String name )
    {
	return new CsvField( name );
    }

    /**
     * returns a list of String objects resulting from parsing
     * a given string as comma separated values
     */
    public static String[] parseCSV( String str )
    {
        if( str.length() == 0 ){
            return new String[0];
        }

	//in this function we will convert normal strings in a line
	//read from a CSV file into list format separating them. It is
	//cool

	//strings may start with a " if it does, then we must make
	//sure to handle the commas inbetween the quotes

	String single; //used to hold a single word
	String newstr; //new string

	//vectors to hold the single words we grab out of the function
	//allocate enough memory to hold all the words
	List words = new ArrayList(); 

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
	    if( newstr.charAt(0) == '"' ){

		//we will have to determine which " is not part of a
		//double quote. When we do, we will have determined
		//the end of the string

		check = false;
		single = "error";
		i = 1; //we start searching from the beginning
		do{ //keep searching until we find a quote by itself

		    i = newstr.indexOf( '"', i ); //find it starting with i
		    if( i == -1 ){
			throw new IllegalArgumentException( 
			      "Could not find end quote"
                        );
		    } //we did not find any more quotes!!
		    else {
			//check for a double quote
			if( newstr.length() > i + 1 
			    && newstr.charAt( i + 1 ) == '"' ){
			    //it is a double, start search over
			    //at next letter
			    
			    //we must remove the double quote
			    newstr = newstr.substring( 0, i ).
				concat( newstr.substring( i + 1 ) );

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
			    single = newstr.substring( 1, i );

			    //set newstr to everything after first word
			    newstr = newstr.substring( i + 1 );
			    check = true;
			}
			//the marker is at the end
			if( i == newstr.length() - 1 ){
			    //set string to 1st word, without quotes
			    single = newstr.substring( 1, i );
			    //set newstr to everything after first word
			    newstr = "";
			    check = true;
			}	
		    } //we have found a match
 		} while( !check );

		//in case there is stuff after the quote

		i = newstr.indexOf( ',' );

		if( i != -1 ){ //that means there IS a comma

		    //For now, we just add whatever is before comma onto single
		    //and whatever is after comma into the new str!
				
		    //add stuff to single
		    if( i > 0 ){
			//concat single to include stuff before the next comma
			single = single.concat( newstr.substring( 0, i ) ); 
		    }
		    if( i != newstr.length() - 1 ){
			newstr = newstr.substring( i + 1 );
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
		i = newstr.indexOf( ',' );
		//see if we found a comma or not
		if( i != -1){ //this means we found a comma

		    //make everything before the comma into single and
		    //after into newstr

		    single = newstr.substring( 0, i );
		    newstr = newstr.substring( i + 1 );
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
        words.add( newstr );

	return (String[])words.toArray( new String[0] );

    } //end function parseCSV

    /**
     * returns a list of String objects resulting from parsing
     * a given string as comma separated values
     */
    public static String[] toArray( String str )
    {
        List retval = new ArrayList();

        final int COMMA = 0;
        final int QUOTE = 1;
        final int TEXT = 2;
        final int EOS = 3;
        // this is one of those deterministic finite automaton
        // thingies

        // stores the next context of a given context (row)
        // and character seen (column)
        byte[] next_context_lst = {
            //comma   quote   text    eos     
              0      ,0      ,0      ,0       ,// 0 reserved
              2      ,3      ,4      ,6       ,// 1 initial
              2      ,3      ,4      ,6       ,// 2 last char == comma
              3      ,5      ,3      ,6       ,// 3 last char == quote
              2      ,4      ,4      ,6       ,// 4 last char == text
              2      ,3      ,4      ,6       ,// 5 last char == second quote
              0      ,0      ,0      ,0       ,// 6 end of stream
        };

        final int PUSH = 1;                    // push existing segment
                                               // to result array and
                                               // start a new segment
        final int NOTHING = 2;                 // do absolutely nothing
        final int APPEND = 3;                  // append current char to
                                               // existing segment
        

        // stores the action to perform for a given context (row)
        // and character seen (column)
        byte[] action_lst = {
            //comma   quote   text    eos     
              0      ,0      ,0      ,0       ,// 0 reserved
              1      ,2      ,3      ,2       ,// 1 initial
              1      ,2      ,3      ,1       ,// 2 last char == comma
              3      ,2      ,3      ,2       ,// 3 last char == quote
              1      ,3      ,3      ,1       ,// 4 last char == text
              1      ,3      ,3      ,1       ,// 5 last char == second quote
              0      ,0      ,0      ,0       ,// 6 end of stream
        };

        StringBuffer seg = new StringBuffer();
        char[] buf       = str.toCharArray();
        byte   context   = 1;
        char   c         = 0;
        int    len       = buf.length;

        int  idx;
        int  chartype;
        byte action;

        for( idx = 0; ; idx++ ){
            //System.out.print( "Context: " + context );

            // find char type
            if( idx == len ){
                chartype = EOS;
            }
            else{
                c = buf[idx];
                if( c == ',' ){
                    chartype = COMMA;
                }
                else if( c == '"' ){
                    chartype = QUOTE;
                }
                else{
                    chartype = TEXT;
                }
            }

            //System.out.print( "    chartype: " + chartype );

            action = action_lst[context * 4 + chartype];

            //System.out.println( "    action: " + action );

            if( action == PUSH ){
                retval.add( seg.toString() );
                seg = new StringBuffer();
            }
            else if( action == APPEND ){
                seg.append( c );
            }

            context = next_context_lst[context * 4 + chartype];

            if( context == 6 ) break;
        }

        return (String[])retval.toArray( new String[0] );
    }

    public int pushLine( String line ){
	String[] val = toArray( line );

	Record rec = createRecord();

	for( int i = 0; i < val.length; i++ ){
	    rec.push( val[i] );
	}

	push( rec );

	return 1;
    }


    public static String toString( Table t )
    {
	StringBuffer buf = new StringBuffer();
	
	String lf = System.getProperty( "line.separator" );

	for( t.first(); !t.eof(); t.next() ){
	    buf.append( CsvRecord.toString( t.get() ) );
	    buf.append( lf );
	}
	return buf.toString();
    }

    public Table push( java.sql.ResultSet rs ){
	//////////// finish later
	return this;
    }
    public Table pushLst( java.sql.ResultSet rs ){
	//////////// finish later
	return this;
    }
    public int pushFile( String file ){
	//////////// finish later
	return 0;
    }
}
