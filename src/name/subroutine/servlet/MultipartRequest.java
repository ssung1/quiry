package name.subroutine.servlet;

import java.io.*;
import java.util.*;
import javax.servlet.*;

/**
 * Modified from Java Servlet Programming from O'Reilly
 *
 * The Servlet API is required to compile this class
 */
public class MultipartRequest
{
    /**
     * 1 meg default max post size
     */
    private static final int DEFAULT_MAX_POST_SIZE = 1024 * 1024;

    private ServletRequest req;
    private File dir;
    private int maxSize;

    private Hashtable parameters = new Hashtable();
    private Hashtable files = new Hashtable();

    boolean _use_file = false;

    
    public MultipartRequest( ServletRequest request )
	throws IOException
    {
	this( request, null, DEFAULT_MAX_POST_SIZE );
    }

    public MultipartRequest( ServletRequest request, int maxPostSize )
	throws IOException
    {
	this( request, null, maxPostSize );
    }

    public MultipartRequest( ServletRequest request,
			     String saveDirectory )
	throws IOException
    {
	this( request, saveDirectory, DEFAULT_MAX_POST_SIZE );
    }

    public MultipartRequest( ServletRequest request,
			     String saveDirectory,
			     int maxPostSize )
	throws IOException
    {
	if( request == null ){
	    throw new IllegalArgumentException( "request cannot be null." );
	}
	if( maxPostSize <= 0 ){
	    throw new IllegalArgumentException( "maxPostSize <= 0" );
	}

	req = request;
	maxSize = maxPostSize;

	if( saveDirectory != null ){
	    dir = new File( saveDirectory );

	    if( !dir.isDirectory() ){
		throw new IllegalArgumentException( "Not a directory: " +
						    saveDirectory );
	    }
	    
	    if( !dir.canWrite() ){
		throw new IllegalArgumentException( "Not writable: " +
						    saveDirectory );
	    }
	    _use_file = true;
	}

	readRequest();
    }

    public Enumeration getParameterNames()
    {
	return parameters.keys();
    }

    public Enumeration getFileNames()
    {
	return files.keys();
    }

    /**
     * It is possible to get a string or a "file struct"
     * from a parameter name.  A file struct is a Map with
     * the fields "name" and "value", which are the filename
     * and the contents of that file.
     */
    public Object getParameter( String name )
    {
	return parameters.get( name );
    }

    public String getFilesystemName( String name )
    {
	try{
	    UploadedFile file = (UploadedFile)files.get( name );
	    return file.getFilesystemName();
	}
	catch( Exception e ){
	    return null;
	}
    }

    public String getContentType( String name )
    {
	try{
	    UploadedFile file = (UploadedFile)files.get( name );
	    return file.getContentType();
	}
	catch( Exception e ){
	    return null;
	}
    }

    public File getFile( String name )
    {
	try{
	    UploadedFile file = (UploadedFile)files.get( name );
	    return file.getFile();
	}
	catch( Exception e ){
	    return null;
	}
    }


    protected void readRequest()
	throws IOException
    {
	String type = req.getContentType();
	if( type == null 
	    || !type.toLowerCase().startsWith( "multipart/form-data" ) ){
	    throw new IOException( 
	        "Posted content type isn't multipart/form-data"
		);
	}

	int length = req.getContentLength();

	if( length > maxSize ){
	    throw new IOException( "Posted content length of " + length +
				   " exceeds limit of " + maxSize );
	}

	String boundary = extractBoundary( type );
	if( boundary == null ){
	    throw new IOException( "Separation boundary was not specified." );
	}

	MultipartInputStreamHandler in =
	    new MultipartInputStreamHandler( req.getInputStream(), 
					     boundary,
					     length );

	String line = in.readLine();
	if( line == null ){
	    throw new IOException( "Corrupt form data: premature ending" );
	}

	if( !line.startsWith( boundary ) ){
	    throw new IOException( "Corrupt form data: no leading boundary" );
	}

	boolean done = false;
	while( !done ){
	    done = readNextPart( in, boundary );
	}
    }

    protected boolean readNextPart( MultipartInputStreamHandler in,
				    String boundary )
	throws IOException
    {
	// Read the first line, should look like this:
	// content-disposition: form-data; name="field1"; filename="file1.txt"
	String line = in.readLine();

	if( line == null ){
	    return true;
	}

	// Prase the content-disposition line
	String[] dispInfo = extractDispositionInfo( line );
	String disposition = dispInfo[0];
	String name = dispInfo[1];
	String filename = dispInfo[2];

	// Now onto the next line.  This will either be empty or
	// contain a Content-Type and then an empty line.
	line = in.readLine();
	if( line == null ){
	    // No parts left, so we are done
	    return true;
	}

	// Get the content type, or null if none specified
	String contentType = extractContentType( line );
	if( contentType != null ){
	    // Eat!
	    line = in.readLine();
	    if( line == null || line.length() > 0 ){
		throw new
		    IOException( "Malformed line after content type: " + 
				 line );
	    }
	}
	else{
	    contentType = "application/octet-stream";
	}

	// Now, finally, we read the content (end after reading the boundary)
	if( filename == null ){
	    // this is a parameter
	    String value = readParameter( in, boundary );
	    parameters.put( name, value );
	}
	else if( _use_file ){
	    // this is a file
	    // and we want to save it somewhere...
	    // in some case, saving the file is inappropriate
	    //
	    readAndSaveFile( in, boundary, filename );
	    if( filename.equals( "unknown" ) ){
		files.put( name, new UploadedFile( null, null, null ) );
	    }
	    else{
		files.put( name,
			   new UploadedFile( dir.toString(),
					     filename, contentType ) );
	    }
	}
	else{
	    String value = readParameter( in, boundary );
	    Map file = new HashMap();

	    file.put( "name", filename );
	    file.put( "value", value );

	    parameters.put( name, file );
	}
	return false;
    }

    protected String readParameter( MultipartInputStreamHandler in,
				    String boundary )
	throws IOException
    {
	StringBuffer sbuf = new StringBuffer();
	String line;

	while( (line = in.readLine()) != null ){
	    if( line.startsWith( boundary ) ) break;
	    sbuf.append( line + "\r\n" );
	}

	if( sbuf.length() == 0 ){
	    return null;
	}

	// cut off the last line's \r\n
	sbuf.setLength( sbuf.length() - 2 );

	return sbuf.toString();
    }

    protected void readAndSaveFile( MultipartInputStreamHandler in,
				    String boundary,
				    String filename )
	throws IOException
    {
	File f = new File( dir, filename );
	FileOutputStream fos = new FileOutputStream( f );

	BufferedOutputStream out = new BufferedOutputStream( fos, 8 * 1024 );

	byte[] bbuf = new byte[8 * 1024];

	int result;
	String line;

	// ServletInputStream.readLine has the annoying habit of 
	// adding a \r\n to the end of the last line.
	// Since we want a byte-for-byte transfer, we have to cut those chars
	boolean rnflag = false;

	while( (result = in.readLine( bbuf, 0, bbuf.length )) != -1 ){
	    if( result > 2 && bbuf[0] == '-' && bbuf[1] == '-' ){
		line = new String( bbuf, 0, result, "ISO-8859-1" );
		if( line.startsWith( boundary ) ) break;
	    }

	    // Are we supposed to write \r\n for the last iteration?
	    if( rnflag ){
		out.write( '\r' );
		out.write( '\n' );
		rnflag = false;
	    }

	    // write the buffer, postpone any ending \r\n
	    if( result >= 2 &&
		bbuf[result - 2] == '\r' &&
		bbuf[result - 1] == '\n' ){
		out.write( bbuf, 0, result - 2 );
		rnflag = true;
	    }
	    else{
		out.write( bbuf, 0, result );
	    }
	}

	out.flush();
	out.close();
	fos.close();
    }

    private String extractBoundary( String line ){
	int index = line.indexOf( "boundary=" );
	if( index == -1 ){
	    return null;
	}

	// 9 is the length of "boundary="
	String boundary = line.substring( index + 9 );

	// the real bounary is always preceeded by an extra "--"
	boundary = "--" + boundary;

	return boundary;
    }
	

    private String[] extractDispositionInfo( String line )
	throws IOException
    {
	// Retrun the line's data as an array: disposition, name, filename
	
        String retval[] = new String[3];

	// Convert the line to a lowercase string without the ending \r\n
	// keep the original line for error messages and for variable names.
	String origline = line;
	line = origline.toLowerCase();

	// Get the content disposition, should be "form-data"
	int start = line.indexOf( "content-disposition: " );
	int end = line.indexOf( ";" );
	if( start == -1 || end == - 1 ){
	    throw new IOException( "Content disposition corrupt: " +
				   origline );
	}

	String disposition = line.substring( start + 21, end );
	if( !disposition.equals( "form-data" ) ){
	    throw new IOException( "Invalid content disposition: " +
				   disposition );
	}

	// get the field name
	start = line.indexOf( "name=\"", end );
	end = line.indexOf( "\"", start + 7 );

	if( start == -1 || end == - 1 ){
	    throw new IOException( "Content disposition corrupt: " +
				   origline );
	}

	String name = origline.substring( start + 6, end );

	// Get the filename, if given
	String filename = null;
	start = line.indexOf( "filename=\"", end + 2 );
	end = line.indexOf( "\"", start + 10 );
	if( start != -1 && end != -1 ){
	    filename = origline.substring( start + 10, end );
	    // The filename may contain a full path. cut to just
	    // the filename

	    int slash =
		Math.max( filename.lastIndexOf( '/' ),
			  filename.lastIndexOf( '\\' ) );
	    if( slash > -1 ){
		filename = filename.substring( slash + 1 );
	    }
	    if( filename.equals( "" ) ){
		filename = "unknown";
	    }
	}

	retval[0] = disposition;
	retval[1] = name;
	retval[2] = filename;

	return retval;
    }


    private String extractContentType( String line )
	throws IOException
    {
	String contentType = null;

	// convert the line to a lowercase string
	String origline = line;
	line = origline.toLowerCase();

	// get the content type, if any
	if( line.startsWith( "content-type" ) ){
	    int start = line.indexOf( " " );
	    if( start == -1 ){
		throw new IOException( "Content type corrupt: " + origline );
	    }
	    contentType = line.substring( start + 1 );
	}
	else if( line.length() != 0 ){
	    throw new IOException( "Malformed line after disposition: " +
				   origline );
	}
	return contentType;
    }
}

// a class to hold information about an uploaded file
class UploadedFile
{
    private String dir;
    private String filename;
    private String type;

    UploadedFile( String dir, String filename, String type )
    {
	this.dir = dir;
	this.filename = filename;
	this.type = type;
    }

    public String getContentType()
    {
	return type;
    }
    public String getFilesystemName()
    {
	return filename;
    }
    public File getFile()
    {
	if( dir == null || filename == null ){
	    return null;
	}
	else{
	    return new File( dir, filename );
	}
    }
}


// A class to aid in reading multipart/form-data from a
// ServletInputStream.  It keeps track of how many bytes have been
// read and detects when the Content-Length limit has been reached.
// This is necessary because some servlet engines are slow to notice
// the end of stream.
//
class MultipartInputStreamHandler
{
    ServletInputStream in;
    String boundary;
    int totalExpected;
    int totalRead = 0;
    byte[] buf = new byte[8 * 1024];

    public MultipartInputStreamHandler( ServletInputStream in,
					String boundary,
					int totalExpected )
    {
	this.in = in;
	this.boundary = boundary;
	this.totalExpected = totalExpected;
    }

    public String readLine()
	throws IOException
    {
	StringBuffer sbuf = new StringBuffer();
	int result;
	String line;

	do{
	    String newstring;
	    result = this.readLine( buf, 0, buf.length );
	    if( result != -1 ){
		newstring = new String( buf, 0, result, "ISO-8859-1" );
		sbuf.append( newstring );
	    }
	    
	} while( result == buf.length );

	if( sbuf.length() == 0 ){
	    return null;
	}
	
	sbuf.setLength( sbuf.length() - 2 );
	return sbuf.toString();
    }

    public int readLine( byte b[], int off, int len )
	throws IOException
    {
	if( totalRead >= totalExpected ){
	    return -1;
	}
	else{
	    int result = in.readLine( b, off, len );
	    if( result > 0 ){
		totalRead += result;
	    }
	    return result;
	}
    }
}

