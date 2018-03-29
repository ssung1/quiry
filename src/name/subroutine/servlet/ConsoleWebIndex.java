package name.subroutine.servlet;

import java.io.*;
import java.util.*;
import java.net.*;
import java.sql.*;
import java.text.*;

import name.subroutine.etable.*;
import name.subroutine.util.*;
import name.subroutine.comm.*;
import name.subroutine.servlet.*;

/**
 * This is a console "servlet" that prints to the console.  This allows
 * testing of Service classes without activating the HTTP server.
 */
public class ConsoleWebIndex extends WebIndex
{
    /**
     * Uses the input to call a service
     */
    public void doGet( Service service, PrintWriter out, Map req, Map ses )
	throws Exception
    {
        String class_name = "name.subroutine/servlet/ConsoleWebIndex.class";

        String webinf = "/WEB-INF/classes/";

        // find "wwwroot"
        ClassLoader loader = getClass().getClassLoader();
        URL class_url;
        class_url = loader.getResource( class_name );
        String class_filename;
        class_filename = class_url.getFile();
        String wwwroot;
        wwwroot = class_filename.substring( 0, 
                                            class_filename.length() -
                                            class_name.length() -
                                            webinf.length() );

        ses.put( "_wwwroot", wwwroot );

	// party time!
	try{
	    service.init( out, req, ses );
	}
	catch( Exception ex ){
	    ex.printStackTrace( out );
	    return;
	}

	service.get();

        String error = (String)ses.get( "_error" );
        if( error != null ){
            ses.remove( "_error" );
            out.print( "ERROR:" );
            out.println( error );
            return;
        }

	// special redirection handler
	String redirect;
	redirect = (String)ses.get( "_redirect" );
	if( redirect != null ){
	    ses.remove( "_redirect" );
            out.print( "REDIRECT:" );
            out.println( redirect );
            return;
	}
    }
}
