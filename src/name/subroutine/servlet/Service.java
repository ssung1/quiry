package name.subroutine.servlet;

import java.io.*;
import java.util.*;
import java.sql.*;
import java.text.*;
import java.lang.reflect.*;

import name.subroutine.util.*;
import name.subroutine.htmler.*;

public class Service implements Comparable
{
    protected PrintWriter _out;
    protected Map _req;
    protected Map _ses;

    /**
     * Initializes the page with request and session
     */
    public void init( PrintWriter out, Map req, Map ses )
        throws Exception
    {
        _out = out;
        _req = req;
        _ses = ses;
    }

    public String contentType()
    {
	return "text/html";
    }

    public void get()
    {
        parseInput();
        if( checkRedirect() ){
            return;
        }
	getPage();
    }

    /**
     * Prints the entire contents of the page, including layout
     * and menu.
     *
     * Main entrance to the page content
     */
    public void getPage()
    {
        _out.println( "<html>" );

        _out.println( "<head>" );
        getHeader();
        _out.println( "</head>" );

        _out.println( "<body>" );
	getContent();
        getFooter();
        _out.println( "</body>" );

        _out.println( "</html>" );
    }

    public void getHeader()
    {
    }

    /**
     * writes the main content to the given PrintWriter
     */
    public void getContent()
    {
        _out.println( "This class is: " + getClass() + "<br/>" );

	HTMLTable ht = new HTMLTable();

        Map smap;

	_out.println( "Session variables:<br/>" );
        smap = new TreeMap();
        for( Iterator it = _ses.entrySet().iterator(); it.hasNext(); ){
            Map.Entry entry = (Map.Entry)it.next();
            smap.put( entry.getKey(), entry.getValue() );
        }
	ht.addAll( name.subroutine.util.Arrays.toArray( smap ) );
	ht.setColCnt( 2 );
	ht.setColAttrLst( new Object[] {
	    new String[] {
		"bgcolor", "powderblue"
            },
	    new String[] {
		"bgcolor", "mistyrose"
            }
        } );
      	_out.println( ht );
	
	_out.println( "Request variables:<br/>" );
        smap = new TreeMap();
        for( Iterator it = _req.entrySet().iterator(); it.hasNext(); ){
            Map.Entry entry = (Map.Entry)it.next();
            smap.put( entry.getKey(), entry.getValue() );
        }
	ht.setComponents( name.subroutine.util.Arrays.toArray( smap ) );
      	_out.println( ht );
        return;
    }

    /**
     * Stuff after content, like version info
     */
    public void getFooter()
    {
    }

    /**
     * @return the name of this service
     */
    public String getService()
    {
        String cstring = this.getClass().toString();
        int period = cstring.lastIndexOf( "." );
        return cstring.substring( period + 1 );
    }

    public String toString()
    {
	return getService();
    }

    public int compareTo( Object o )
    {
	if( o instanceof Service ){
	    Service s = (Service)o;
	    
	    int diff;
	    diff = order() - s.order();

	    if( diff != 0 ) return diff;

	    return getService().compareTo( s.getService() );
	}
	return toString().compareTo( o.toString() );
    }

    public int order()
    {
	return 0;
    }

    public void stderr( Exception ex )
    {
	_out.println( "<pre>" );
	ex.printStackTrace( _out );
	_out.println( "</pre>" );
    }

    public void stdout( String msg )
    {
	_out.println( "<pre>" );
        _out.println( msg );
	_out.println( "</pre>" );
    }

    /**
     * Adds current page to the history, so we can recall later.
     *
     * The history is stored in "_history" in the session map.
     */
    protected void pushURL()
    {
        Stack prev;
        prev = (Stack)_ses.get( "_prev" );

        if( prev == null ){
            prev = new Stack();
            _ses.put( "_prev", prev );
        }

        prev.push( _req.get( "_request_uri" ) );
    }

    /**
     * Returns the most recent page visited in history or null
     * if we cannot find one
     */
    protected String popURL()
    {
        try{
            Stack prev;
            prev = (Stack)_ses.get( "_prev" );

            return (String)prev.pop();
        }
        catch( Exception ex ){
            return null;
        }
    }

    /**
     * Returns path to a static page, starting at local root
     */
    public String toStaticURL( String file )
    {
        StringBuffer uri = new StringBuffer();
        uri.append( _req.get( "_context_path" ) );
        uri.append( "/" );
        uri.append( file );

        return URIToURL( uri.toString() );
    }

    /**
     * Returns a string to the URL of this service
     */
    public String toURL()
    {
	return toURL( getService() );
    }

    /**
     * Returns a string to the URL of the named service
     */
    public String toURL( String service )
    {
        StringBuffer uri = new StringBuffer();
        uri.append( _req.get( "_context_path" ) );
        uri.append( _req.get( "_servlet_path" ) );
        uri.append( "/" );
        uri.append( service );

        return URIToURL( uri.toString() );
    }

    /**
     * Returns a string to the URL of the named service
     */
    public String toURL( String service, String query_string )
    {
        StringBuffer uri = new StringBuffer();
        uri.append( _req.get( "_context_path" ) );
        uri.append( _req.get( "_servlet_path" ) );
        uri.append( "/" );
        uri.append( service );

        return URIToURL( uri.toString(), query_string );
    }

    /**
     * Builds a complete URL from request uri
     */
    public String URIToURL( String uri )
    {
        String scheme;
        scheme = (String)_req.get( "_scheme" );
        String is_secure;
        is_secure = (String)_req.get( "_is_secure" );
        String host;
        host = (String)_req.get( "_server_name" );
        String port;
        port = (String)_req.get( "_server_port" );
        String query_string;
        query_string = (String)_req.get( "_query_string" );

        return toURL( scheme, is_secure, host, port, uri, query_string );
    }

    /**
     * Builds a complete URL from request uri, with custom
     * query string
     */
    public String URIToURL( String uri, String query_string )
    {
        String scheme;
        scheme = (String)_req.get( "_scheme" );
        String is_secure;
        is_secure = (String)_req.get( "_is_secure" );
        String host;
        host = (String)_req.get( "_server_name" );
        String port;
        port = (String)_req.get( "_server_port" );

        return toURL( scheme, is_secure, host, port, uri, query_string );
    }

    /**
     * Builds URL out of everything.  Parameters can be null
     */
    public String toURL( String scheme, String is_secure,
                         String host, String port,
                         String request_uri, String query_string )
    {
        StringBuffer url = new StringBuffer();

        url.append( toURL( scheme, is_secure,
                           host, port,
                           request_uri ) );

        if( query_string != null ){
            url.append( '?' ).append( query_string );
        }

        return url.toString();
    }

    /**
     * Builds URL out of everything.  Parameters can be null
     */
    public String toURL( String scheme, String is_secure,
                         String host, String port,
                         String request_uri )
    {
        StringBuffer url = new StringBuffer();

        if( scheme == null ){
            if( is_secure != null &&
                Boolean.valueOf( is_secure ).booleanValue() ){
                scheme = "https";
            }
            else{
                scheme = "http";
            }
        }

        url.append( scheme );
        url.append( "://" );

        if( host != null ){
            url.append( host );
        }

        if( port != null &&
            !"80".equals( port.trim() ) &&
            !"443".equals( port.trim() ) ){
            url.append( ':' ).append( port );
        }

        if( request_uri != null ){
            url.append( request_uri );
        }

        return url.toString();
    }

    /**
     * Prints the contents of the file in the same directory
     * as the class with the given filename
     */
    public void printLocalFile( String filename )
        throws FileNotFoundException, IOException
    {
        String cname = getClass().getName();

        // first we find the last "."
        int period;
        period = cname.lastIndexOf( "." );

        String local_path;
        if( period > 0 ){
            local_path = cname.substring( 0, period );
        }
        else{
            local_path = "";
        }

        // now replace all . with /
        local_path = local_path.replace( '.', '/' );

        File file;
        file = new File( local_path, filename );

        printFile( file );
    }

    /**
     * Prints the contents of the given file
     */
    public void printFile( String filename )
        throws FileNotFoundException, IOException
    {
        printFile( new File( filename ) );
    }

    /**
     * Prints the contents of the given file
     */
    public void printFile( String filepath, String filename )
        throws FileNotFoundException, IOException
    {
        printFile( new File( filepath, filename ) );
    }

    /**
     * Prints the contents of the given file
     */
    public void printFile( File file )
        throws FileNotFoundException, IOException
    {
        FileReader fr = new FileReader( file );
        char buf[] = new char[1024];
        int cnt;
        while( (cnt = fr.read( buf )) >= 0 ){
            _out.write( buf, 0, cnt );
        }
        fr.close();
        
    }

    /**
     * Parses input from request map
     */
    protected void parseInput()
    {
        Field[] flst = this.getClass().getDeclaredFields();
        for( int i = 0; i < flst.length; ++i ){
            String ext_name = flst[i].getName().substring( 1 );
            flst[i].setAccessible( true );
            try{
                flst[i].set( this, _req.get( ext_name ) );
            }
            catch( Exception ex ){
            }
        }
    }

    protected boolean checkRedirect()
    {
        return false;
    }
}
