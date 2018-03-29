package name.subroutine.source;

import java.io.*;
import java.util.*;
import java.sql.*;

/**
 * Searches in the designated MSSQL database for any words
 * that appears in the source code and reports them.
 */
public class MSSQLDepend
{
    PrintWriter _out = new PrintWriter( System.out, true );

    String _argv[];
    Source _source;

    Hashtable _token_set;
    Hashtable _dependency_set;

    public void init( String argv[] )
	throws IllegalArgumentException
    {
	if( argv.length < 3 ){
	    throw new IllegalArgumentException( "Not enough parameters." );
	}
	_argv = argv;
	_source = new Source();
	_token_set = new Hashtable();
	_dependency_set = new Hashtable();
    }

    protected Hashtable getWords()
	throws Exception
    {
	int i;
	Hashtable token_set;

	for( i = 2; i < _argv.length; i++ ){
	    InputStream is = new FileInputStream( _argv[i] );
	    BufferedInputStream bis = new BufferedInputStream( is );

	    token_set = _source.getWords( bis );

	    /*
	     * fix token set to have file names attached
	     */
	    Enumeration enu;
	    for( enu = token_set.keys(); enu.hasMoreElements(); ){
		Object k = enu.nextElement();
		Vector o_ = (Vector)token_set.get( k );

		/*
		 * creates a string out of a vector of integers
		 */
		StringBuffer lines = new StringBuffer();
		lines.append( _argv[i] ).append( ": " );
		for( int oi = 0; oi < o_.size(); oi++ ){
		    if( oi > 0 ){
			lines.append( ", " );
		    }
		    lines.append( o_.get( oi ) );
		}
		/*
		 * puts all the entries in
		 * token_set into _token_set
		 * while appending filename
		 */
		Vector o__ = (Vector)_token_set.get( k );

		if( o__ == null ){
		    o__ = new Vector();

		    _token_set.put( k, o__ );
		}
		o__.add( lines );
	    }
	}
	return _token_set;
    }

    /**
     * Searches the database to and stores all the system objects
     * in the database that also appeared in the source code
     * (tokenized into _token_set) in _dependency_set
     *
     * to search for dependencies in the dependency set, use
     * this sql statement:
     *
     * select obj.name, obj.id, dep.id, dep.depid, obj1.name
     *
     * from sysobjects obj, sysdepends dep, sysobjects obj1
     *
     * where obj.id = dep.id and dep.depid = obj1.id
     */
    protected void searchDatabase()
	throws Exception
    {
	Class.forName( "sun.jdbc.odbc.JdbcOdbcDriver" );

	String url = "jdbc:odbc:" + _argv[0];

	Connection con = DriverManager.getConnection(url, "", "" );

	Statement stmt = con.createStatement();

	String table = _argv[1] + "..sysobjects";
	String sql = "select name from " + table;
	ResultSet rs = stmt.executeQuery( sql );

	while (rs.next()) {
	    String s = rs.getString( 1 );
	    Object o = _token_set.get( s );
	    if( o != null ){
		_dependency_set.put( s, o );
	    }
	}
    }

    public void printHelp()
    {
	_out.println( "MSSQLDepend <dsn> <database> <source>" );
	_out.println( "where:" );
	_out.println( "dsn         is the ODBC Data Source Name" );
	_out.println( "database    is the name of the database" );
	_out.println( "source      is the source code" );
    }

    public static void main( String argv[] )
    {
	MSSQLDepend d = new MSSQLDepend();

	try{
	    d.init( argv );
	    d.getWords();
	    d.searchDatabase();
	}
	catch( IllegalArgumentException e ){
	    d.printHelp();
	    System.exit( 0 );
	}
	catch( Exception e ){
	    e.printStackTrace();
	}

	Hashtable h = d._dependency_set;
	Enumeration enu;

	for( enu = h.keys(); enu.hasMoreElements(); ){
	    Object k = enu.nextElement();
	    Vector o = (Vector)h.get( k );
	    System.out.println( k );
	    for( int oi = 0; oi < o.size(); oi++ ){
		System.out.println( "    " + o.get( oi ) );
	    }
	}
    }
}

