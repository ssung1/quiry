package name.subroutine.source;

import java.io.*;
import java.util.*;

public class Source
{
    /**
     * set _out to your favorite output stream (PrintWriter)
     */
    public PrintWriter _out = new PrintWriter( System.out, true );

    /**
     * temporary buffer...not threadsafe
     */
    StringBuffer _sb = new StringBuffer();
    /**
     * filter for filenames
     */
    FileFilter _filter = new Filter();

    /**
     * old path before repackaging
     */
    String _old_path;
    /**
     * new path after repackaging
     */
    String _new_path;

    /**
     * This is the version of copy that handles exceptions
     */
    public int _copy( String dst_name, File src )
    {
	try{
	    return __copy( dst_name, src );
	}
	catch( Exception ex ){
	    return 0;
	}
    }

    /**
     * Copy a java source file.
     *
     * Writes the package name as the first line, depending
     * on the subdirectory.
     *
     * @param dst_name Name of the destination file.  This will
     * be appended to the _new_path.
     * @param src Source file
     */
    public int __copy( String dst_name, File src )
	throws FileNotFoundException, IOException
    {
	File dst = new File( _new_path, dst_name );

	_out.println( "Read: " + src.getPath() );
	_out.println( "Copy to: " + dst.getPath() );

	File new_path = dst.getParentFile();
	if( !new_path.exists() ){
	    _out.println( "Create " + new_path.getPath() );

	    if( !new_path.mkdirs() ) return 0;
	}

	FileReader fr = new FileReader( src );
	BufferedReader br = new BufferedReader( fr );

	FileWriter fw = new FileWriter( dst );
	PrintWriter pw = new PrintWriter( fw, true );


	String pkg;
	int sep;

	sep = dst_name.lastIndexOf( File.separatorChar );
	pkg = dst_name.replace( File.separatorChar, '.' );
	/*
	 * first character is "/" which we remove
	 * and only put in package if there is a package
	 *
	 * pkg[0] is always a "/".  We need to look for the
	 * last "/" that is not at zero position
	 */
	if( sep > 0 ){
	    pw.println( "package " + pkg.substring( 1, sep ) + ";" );
	}

	while( true ){
	    String buf;
	    buf = br.readLine();
	    if( buf == null ) break;

	    /*
	     * ignore package directives -- we already put in one
	     */
	    if( buf.startsWith( "package " ) && buf.endsWith( ";" ) ){
		continue;
	    }

	    pw.println( buf );
	}

    	_out.println( "" );

	return 1;
    }

    public int repackage( String dst, String src )
    {
	_old_path = src;
	_new_path = dst;
	
	return _repackage( src, "" );
    }

    public int _repackage( String path, String name )
    {
	_out.println( "path: " + path );
	_out.println( "name: " + name );

	File f = new File( path );
	if( f.isFile() ){
	    _copy( name, f );
	}
	if( f.isDirectory() ){
	    File[] flst = f.listFiles( _filter );
	    String subpath;
	    int i;

	    File dir = new File( _new_path, name );
	    //_out.println( "Create " + dir.getPath() );
	    for( i = 0; i < flst.length; i++ ){
		subpath = name + File.separator + flst[i].getName();
		_repackage( flst[i].getPath(), subpath );
	    }
	    return 1;
	}
	return 1;
    }

    /**
     * requires sun.tools.javac package
     * 
     * Cannot recursively recompile dependencies :(
     */
    public int _compile( String name )
    {
	File f = new File( name );
	if( f.isFile() ){
	    String lst[] = { name };
	    _out.println( name );
	    //sun.tools.javac.Main.main( lst );
	}
	if( f.isDirectory() ){
	    File[] flst = f.listFiles( _filter );

	    int i;
	    for( i = 0; i < flst.length; i++ ){
		_compile( flst[i].getPath() );
	    }
	    return 1;
	}
	return 1;
    }

    /**
     * reads from an input stream and parses consecutive java identifier
     * characters into tokens.
     */
    public Hashtable getWords( InputStream is )
	throws Exception
    {
	Hashtable token_set = new Hashtable();
	return getWords( token_set, is );
    }

    /**
     * appends more tokens into an existing set
     *
      @see #getWords( InputStrem is )
     */
    public Hashtable getWords( Hashtable set, InputStream is_ )
	throws Exception
    {
	Hashtable token_set = set;
	InputStreamReader isr = new InputStreamReader( is_ );
	LineNumberReader is = new LineNumberReader( isr );

	char c;
	/*
	 * 0: last char is not a legal identifier char
	 * 1: last char is a legal identifier char
	 */
	int state;

	StringBuffer token = null;
	state = 0;

	while( true ){
	    int cc = is.read();
	    if( cc < 0 ) break;
	    c = (char)cc;

	    if( state == 0 ){
		if( Character.isJavaIdentifierPart( c ) ){
		    token = new StringBuffer();
		    token.append( c );

		    state = 1;
		}
		continue;
	    }
	    if( state == 1 ){
		if( Character.isJavaIdentifierPart( c ) ){
		    token.append( c );

		    continue;
		}
		int line_num;
		line_num = is.getLineNumber();

		Vector o;
		String key = token.toString();
		o = (Vector)token_set.get( key );
		if( o == null ){
		    o = new Vector();
		    token_set.put( key, o );
		}
		o.add( new Integer( line_num ) );
		state = 0;
		continue;
	    }
	}

	return token_set;
    }

    public static void main( String argv[] )
    {
	Source source = new Source();

	if( argv.length == 1 ){
	    source.repackage( argv[0], "." );
	}
	else if( argv.length == 2 ){
	    source.repackage( argv[0], argv[1] );
	}
	else if( argv.length == 0 ){
	    System.out.println( "Java Sourcecode Repackager" );
	    System.out.println( "USAGE:" );
            System.out.println( "Source <destination dir> [source dir]" );
	}
    }
}

class Filter implements FileFilter
{
    public boolean accept( File f )
    {
	if( f.isDirectory() ) return true;
	if( f.getAbsolutePath().endsWith( ".java" ) ) return true;

	return false;
    }
}
