package name.subroutine.servlet;

import java.io.*;
import java.util.*;
import java.net.*;
import java.sql.*;
import java.text.*;

import javax.servlet.*;
import javax.servlet.http.*;

import name.subroutine.util.*;

/**
 * Main servlet class
 */
public class WebIndex extends HttpServlet
{
    protected Service _service;

    static Map _session_map = new HashMap();

    /**
     * Gets all the sessions (a Map object full of more Maps)
     */
    public Map getSessionMap()
    {
        return _session_map;
    }

    /**
     * Gets the "session-map" by session id
     */
    public Map getSessionMap( String sesid )
    {
	Map o;
	o = (Map)_session_map.get( sesid );
	if( o == null ){
	    o = new HashMap();
	    synchronized( _session_map ){
		_session_map.put( sesid, o );
	    }
	}
	return o;
    }

    /**
     * Loads a service by request.  It uses the first path found in
     * "path info" to load the service by first appending the package
     * name and "Service" in front.
     *
     * If the service cannot be loaded, it will return the
     * generic service.
     */
    public void loadService( Map req )
    {
        try{
            String path_info;
            path_info = (String)req.get( "_path_info" );

            // path info has this format:
            //     /path/path/path...
            // the first path tells us what service to use

            int slash;
            slash = path_info.indexOf( "/", 1 );
            
            String service;
            if( slash >= 0 ){
                service = path_info.substring( 1, slash );
            }
            else{
                service = path_info.substring( 1 );
            }
            
            int period;
            period = service.indexOf( "." );
            if( period >= 0 ){
                service = service.substring( 0, period );
            }
            
            String classroot;
            classroot = getClass().getPackage().getName() + ".Service";

            Class sc;
            sc = Class.forName( classroot + service );
            _service = (Service)sc.newInstance();
        }
        catch( Exception ex ){
            _service = new Service();
        }
    }

    /**
     * Consult any Servlet programming manual for the use of this
     * method
     */
    public void doPost( HttpServletRequest req,
		       HttpServletResponse res )
	throws ServletException, IOException
    {
	doGet( req, res );
    }

    /**
     * Consult any Servlet programming manual for the use of this
     * method
     */
    public void doGet( HttpServletRequest req,
		       HttpServletResponse res )
	throws ServletException, IOException
    {
	PrintWriter out = new PrintWriter( res.getOutputStream(), true );

	HttpSession ses = req.getSession();

	// 12/05/2000
	//
	// added last access time for better debugging
	java.util.Date last_access = new java.util.Date();
	last_access.setTime( ses.getLastAccessedTime() );
	SimpleDateFormat sdf = new SimpleDateFormat( "MMM dd, yyyy  HH:mm" );
	
	// Sends all session information to a Map object, which can
	// be used by any class even without servlet support
        //
        // We use getSessionMap and reuse the session Map object.  If
        // we created a new Map each time, we would need to store the
        // contents back to the map using setAttribute
	Map s = getSessionMap( ses.getId() );
	Enumeration e;
	for( e = ses.getAttributeNames(); e.hasMoreElements(); ){
	    String name = (String)e.nextElement();
	    s.put( name, ses.getAttribute( name ) );
	}
	String slist[] = 
	{
	    "_id",            ses.getId(),
            "_last_accessed", sdf.format( last_access ),
	};
	Map smap;
	smap = Maps.toMap( slist );
	s.putAll( smap );

	Map r = new HashMap();

	for( e = req.getParameterNames(); e.hasMoreElements(); ){
	    String name = (String)e.nextElement();
	    r.put( name, req.getParameter( name ) );
	}

	ServletConfig sc = getServletConfig();
	ServletContext scx = sc.getServletContext();

	Object rlist[] = 
	{
            "__request",        req,
            "__response",       res,
            "_output_stream",   res.getOutputStream(),

            "_protocol",        req.getProtocol(),
            "_scheme",          req.getScheme(),
            "_server_name",     req.getServerName(),
            "_server_port",     String.valueOf( req.getServerPort() ),
            "_is_secure",       String.valueOf( req.isSecure() ),
            "_remote_addr",     req.getRemoteAddr(),
            "_remote_host",     req.getRemoteHost(),
            
	    "_context_path",    req.getContextPath(),
	    "_servlet_path",    req.getServletPath(),
	    "_request_uri",     req.getRequestURI(),
	    "_path_info",       req.getPathInfo(),
	    "_path_translated", req.getPathTranslated(),
	    "_query_string",    req.getQueryString(),
	    "_content_type",    req.getContentType(),
	    "_content_length",  String.valueOf( req.getContentLength() ),
	    "_wwwroot",         scx.getRealPath( "/" ),
	};
	Map rmap;
	rmap = Maps.toMap( rlist );
	r.putAll( rmap );

	String content_type;
	content_type = req.getContentType();

	if( content_type != null &&
	    content_type.startsWith( "multipart/form-data" ) ){

	    MultipartRequest multi;
	    try{
		multi = new MultipartRequest( req, 5 * 1024 * 1024 );
		for( e = multi.getParameterNames(); e.hasMoreElements(); ){
		    String name = (String)e.nextElement();
		    Object value = multi.getParameter( name );

		    if( value == null ) continue;

		    r.put( name, value );
		}
	    }
	    catch( Exception ex ){
		ex.printStackTrace( out );
	    }
	}

	loadService( r );

	// party time!
	try{
	    _service.init( out, r, s );
	}
	catch( Exception ex ){
	    res.setContentType( "text/html" );
	    out.println( "<html><body>" );
	    out.println( "Error: cannot initialize resources.<br>" );
	    ex.printStackTrace( out );
	    out.println( "</body></html>" );
	    return;
	}


        // 8/26/2002
        // content type cannot be determined until all contents
        // are generated
        // however--
        // content type cannot be set after contents are generated
        //
        // what to do, what to do, what to do, what to do, ...
        // res.addHeader( "Content-Type", _service.contentType() );

        // 8th Day of Pure Brightness, 2003
        // we will leave this here for now.  make sure the service
        // can provide the right content type before sending out
        // any responses
        //
        // Note: we must set content type for standard web browsers.
        // IE has its own content type detector, which is convenient
        // but may cause more problems
	res.setContentType( _service.contentType() );
	_service.get();

        String error = (String)s.get( "_error" );
        if( error != null ){
            s.remove( "_error" );
            res.sendError( Integer.parseInt( error ) );
            return;
        }

	// special redirection handler
	String redirect;
	redirect = (String)s.get( "_redirect" );
	if( redirect != null ){
	    s.remove( "_redirect" );
	    res.sendRedirect( redirect );
	}
    }
}
