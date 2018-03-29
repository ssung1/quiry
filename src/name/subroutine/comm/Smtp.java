package name.subroutine.comm;

import java.util.*;
import java.net.*;
import java.lang.*;
import java.io.*;

/**
 * All that is mail
 */
public class Smtp
{
        
    /*
      private String from="";

      public void from(String f)
      { //sets where from
      from = f;
      }

      private String to="";
        
      public void to(String f){ //sets who to send to
      to = f;
      }

      private String subject="";
                
      public void subject(String f){ //sets the subject of the letter
      subject = f;
      }

      private String message="";

      public void message(String f){ //sets the message to be sent
      message = f; //TA-da
      }
    */


    //Meanie Jojo is gonna separate this function into many pieces!!
    //STop him!!!  7/26/2000

    private static boolean ReadUntil(String s, InputStream into)
	throws Exception
    {
        //If S is found then it is good
        //Usually s = 250 because that means that it is a OK
        //Bad numbers include: 553,503,502,500... possibly more

        StringBuffer buf = new StringBuffer();
        int ret; //return value

        do
            { //infinite loop!?!
                ret = into.read();
                buf = buf.append((char)ret);    

                if( buf.toString().indexOf(s) != -1 ){
                                //flush out the buffer
                    return true; //returns true, we found it
                }
                        
                if( (buf.toString().indexOf("553") != -1) ||
                    (buf.toString().indexOf("503") != -1) ||
                    (buf.toString().indexOf("502") != -1) ||
                    (buf.toString().indexOf("500") != -1)) return false; 
                                
                
		//keep doing while return value is not -1 which means
		//it's empty
            } while(ret != -1);

        throw new IllegalArgumentException("Connection refused");
        //return false;
                
    } //waits until it receives that string

    public static void send( String ip, String to, String from, 
			     String subject, String message )
        throws Exception
    {
        String buf;     //anonymous buffer string
        //1 function to do all the word of those 4 functions above
        //open socket to port 25 (mailing thing)
        Socket mail = new Socket(ip, 25 );
        OutputStream telo; //used for sending information
        InputStream into;
        
        telo = mail.getOutputStream();
        into = mail.getInputStream();
        PrintStream tel = new PrintStream( telo, true );
                
        tel.print("HELO COVISTA\r\n");

        //read until hit ok
        if( ReadUntil( "250", into ) == false){
	    throw new IllegalArgumentException("Error in HELO");
	}

        buf = "MAIL FROM: ";
        buf = buf.concat(from);
        buf = buf.concat("\r\n");
        tel.print(buf);
        if( ReadUntil( "250", into ) == false){
	    throw new IllegalArgumentException("Error in MAIL FROM");
	}
                

        buf = "RCPT TO: ";
        buf = buf.concat(to);
        buf = buf.concat("\r\n");
        tel.print(buf);
        if( ReadUntil( "250", into ) == false){
	    throw new IllegalArgumentException("Error in RCPT TO");
	}

        tel.print("DATA\r\n");
        if( ReadUntil( "354", into ) == false){
	    throw new IllegalArgumentException("Error in DATA(?)");
	}

        buf = "FROM: ";
        buf = buf.concat(from);
        buf = buf.concat("\r\n");
        tel.print(buf);

        buf = "TO: ";
        buf = buf.concat(to);
        buf = buf.concat("\r\n");
        tel.print(buf);
                
        buf = "SUBJECT: ";
        buf = buf.concat(subject);
        buf = buf.concat("\r\n");
        tel.print(buf);
                
        tel.print("\r\n");
                
        tel.print(message.trim());
        tel.print("\r\n");      
                
        tel.print(".\r\n");
        if( ReadUntil( "250", into ) == false){
	    throw new IllegalArgumentException("Error in message sent");
	}
                
        tel.flush(); //make sure everything is written               
        tel.close();
                                        
    }
                        

    public static void main(String[] argv) throws Exception
    {
        
        send(argv[0],argv[1],argv[2],argv[3],argv[4]);

        System.out.println("Done");

    } //end main

} //end of public class mailer
