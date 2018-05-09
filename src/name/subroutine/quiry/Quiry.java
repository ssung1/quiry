package name.subroutine.quiry;

import java.net.*;
import java.io.*;
import java.util.*;
import java.text.*;
import java.sql.*;

import oracle.jdbc.OracleTypes;
import org.mozilla.javascript.*;

import name.subroutine.util.Variant;

public class Quiry
{
    String _database;
    Statement _stmt;
    Connection _conn;
    boolean _is_ref_cur;

    public void setDatabase( String db )
    {
        _database = db;
    }

    Connection getConn( String driver, String url, String user, String passwd )
        throws ClassNotFoundException, SQLException
    {
        Class c = Class.forName( driver );
        Connection conn;
        conn = DriverManager.getConnection( url, user, passwd );

        return conn;
    }

    Connection getConn()
        throws ClassNotFoundException, SQLException
    {
        Scriptable world = Resource.getWorld();
        Scriptable dball = (Scriptable)world.get( "db", world );
        Scriptable db = (Scriptable)dball.get( _database, dball );

        String driver = Context.toString( db.get( "driver", db ) );
        String url = Context.toString( db.get( "url", db ) );
        String user = Context.toString( db.get( "user", db ) );
        String passwd = Context.toString( db.get( "password", db ) );

        return getConn( driver, url, user, passwd );
    }

    ResultSet execute( String sql )
        throws ClassNotFoundException, SQLException
    {
        _conn = getConn();
        _stmt = _conn.createStatement();

        if( _is_ref_cur ){
            CallableStatement stmt = _conn.prepareCall( sql );

            stmt.registerOutParameter( 1,
                OracleTypes.CURSOR );
            stmt.execute();

            return (ResultSet)stmt.getObject( 1 );
        }
        else if( _stmt.execute( sql ) ){
            return _stmt.getResultSet();
        }
        else{
            return null;
        }
    }

    /**
     * must run this after executing
     */
    void close()
    {
        try{
            _stmt.close();
        }
        catch( Exception ignored ){
        }

        try{
            _conn.close();
        }
        catch( Exception ignored ){
        }
    }

    /**
     * returns a Java array of Scriptable objects.  Each object should
     * contain attributes "width" and "align"
     */
    Scriptable[] getStyle( String style_name )
    {
        List retval = new ArrayList();

        Scriptable world = Resource.getWorld();

        Object val;
        
        Scriptable style;
        val = world.get( style_name, world );
        if( val == Scriptable.NOT_FOUND ) return null;
        style = (Scriptable)val;

        for( int i = 0; true; ++i ){
            Object column;
            column = style.get( i, style );
            if( column == Scriptable.NOT_FOUND ) break;

            retval.add( column );
        }

        return (Scriptable[])retval.toArray( new Scriptable[0] );
    }

    /**
     * formats the content of the value array according to style
     */
    String format( String[] value, Scriptable[] style )
    {
        StringBuffer retval;
        retval = new StringBuffer();

        for( int i = 0; i < value.length; ++i ){
            Object val;
            String str;
            val = style[i].get( "width", style[i] );
            str = Context.toString( val );
            int width = Integer.parseInt( str );
            val = style[i].get( "align", style[i] );
            str = Context.toString( val );
            int align = Integer.parseInt( str );

            String seg;
            seg = Variant.fit( value[i], width, align );

            retval.append( seg );
        }


        return retval.toString();
    }

    String[] toMetaDataStringArray( ResultSetMetaData md, int column )
        throws SQLException
    {
        List l;
        l = new ArrayList();
        l.add( md.getColumnName( column ) );
        l.add( md.getColumnTypeName( column ) );
        l.add( String.valueOf( md.getColumnDisplaySize( column ) ) );
        l.add( String.valueOf( md.getScale( column ) ) );
        String nullable = "";
        if( md.isNullable( column ) == ResultSetMetaData.columnNullable ){
            nullable = "NULLABLE";
        }
        l.add( nullable );
        
        String[] s;
        s = (String[])l.toArray( new String[0] );
        
        return s;
    }

    void runMetaDataQuiry( String sql, PrintStream out, String style_name )
        throws ClassNotFoundException, SQLException
    {
        ResultSet rs = execute( sql );
        
        ResultSetMetaData md = rs.getMetaData();

        int cnt = md.getColumnCount();

        Scriptable[] style;

        style = getStyle( style_name );
        if( style == null ){
            System.out.println( "Cannot find output style " + style_name );
            close();
            runMetaDataQuiryCsv( sql, out );
            return;
        }

        List l;
        for( int i = 1; i <= cnt; ++i ){
            String[] s;
            s = toMetaDataStringArray( md, i );
            out.println( format( s, style ) );
        }

        close();
    }

    void runMetaDataQuiryCsv( String sql, PrintStream out )
        throws ClassNotFoundException, SQLException
    {
        ResultSet rs = execute( sql );
        
        ResultSetMetaData md = rs.getMetaData();

        int cnt = md.getColumnCount();

        for( int i = 1; i <= cnt; ++i ){
            String[] s;
            s = toMetaDataStringArray( md, i );
            out.println( name.subroutine.etable.CsvRecord.toString( s ) );
        }

        close();
    }

    String toJavaName( String sqlName )
    {
        StringBuilder sb = new StringBuilder();
        boolean prevUnderScore = false;
        for( int i = 0; i < sqlName.length(); ++i ){
            char c = sqlName.charAt( i );
            boolean underScore = (c == '_');
            if( prevUnderScore ){
                sb.append( Character.toUpperCase( c ) );
                prevUnderScore = underScore;
            }
            else{
                if( !underScore ){
                    sb.append( c );
                }
                prevUnderScore = underScore;
            }
        }
        return sb.toString();
    }

    void runMetaDataQuiryJpa( String sql, PrintStream out )
        throws ClassNotFoundException, SQLException
    {
        ResultSet rs = execute( sql );
        
        ResultSetMetaData md = rs.getMetaData();

        int cnt = md.getColumnCount();

        for( int i = 1; i <= cnt; ++i ){
            String[] s;
            String name = toJavaName( md.getColumnName( i ).toLowerCase() );
            int type = md.getColumnType( i );
            String typeName = "(unknown)";
            String temporal = null;
            if( type == Types.INTEGER || type == Types.SMALLINT ){
                typeName = "Integer";
            }
            else if( type == Types.NUMERIC || type == Types.DECIMAL ){
                if( md.getScale( i ) > 0 ){
                    typeName = "BigDecimal";
                }
                else{
                    if( md.getPrecision( i ) > 18 ){
                        typeName = "BigInteger";
                    }
                    else if( md.getPrecision( i ) > 9 ){
                        typeName = "Long";
                    }
                    else if( md.getPrecision( i ) > 4 ){
                        typeName = "Integer";
                    }
                    else{
                        typeName = "Short";
                    }
                }
            }
            else if( type == Types.TIME ){
                temporal = "@Temporal(TemporalType.TIME)";
                typeName = "Date";
            }
            else if( type == Types.DATE ){
                temporal = "@Temporal(TemporalType.DATE)";
                typeName = "Date";
            }
            else if( type == Types.TIMESTAMP ){
                temporal = "@Temporal(TemporalType.TIMESTAMP)";
                typeName = "Date";
            }
            else if( type == Types.CHAR ){
                typeName = "String";
            }
            else if( type == Types.VARCHAR ){
                typeName = "String";
            }
            else{
                typeName = md.getColumnTypeName( i ) + String.valueOf( type );
            }
            if( temporal != null ){
                out.println( temporal );
            }
            if( md.isNullable( i ) == ResultSetMetaData.columnNoNulls ){
                out.println( "@Column(nullable = false)" );
            }
            out.print( "private " );
            out.print( typeName );
            out.print( " " );
            out.print( name );
            out.println( ";" );
            out.println();
        }

        close();
    }

    String[] toColumnNameStringArray( ResultSetMetaData md, int col_cnt )
        throws SQLException
    {
        List l = new ArrayList();
        for( int i = 1; i <= col_cnt; ++i ){
            l.add( md.getColumnName( i ) );
        }
        String[] s;
        s = (String[])l.toArray( new String[0] );
        
        return s;
    }

    String[] toDataStringArray( ResultSet rs, int col_cnt )
        throws SQLException
    {
        List l = new ArrayList();
        for( int i = 1; i <= col_cnt; ++i ){
            l.add( Variant.toString( rs.getString( i ) ) );
        }
        String[] s;
        s = (String[])l.toArray( new String[0] );
        
        return s;
    }

    void runDataQuiry( String sql, PrintStream out, String style_name )
        throws ClassNotFoundException, SQLException
    {
        ResultSet rs = execute( sql );
        
        ResultSetMetaData md = rs.getMetaData();

        int cnt = md.getColumnCount();

        Scriptable[] style;
        style = getStyle( style_name );
        if( style == null ){
            if( style_name != null &&
                style_name.trim().length() > 0 ){
                System.out.println( "Cannot find output style " + style_name );
            }
            close();
            runDataQuiry( sql, out );
            return;
        }

        String[] s;
        s = toColumnNameStringArray( md, cnt );
        out.println( format( s, style ) );

        while( rs.next() ){
            s = toDataStringArray( rs, cnt );
            out.println( format( s, style ) );
        }

        close();
    }

    /**
     * formats the content of the value array according to metadata
     */
    String format( String[] value, ResultSetMetaData md )
        throws SQLException
    {
        StringBuffer retval;
        retval = new StringBuffer();

        for( int i = 0; i < value.length; ++i ){
            String name = md.getColumnName( i + 1 );
            int width = md.getColumnDisplaySize( i + 1 );

            width = Math.max( width, name.length() ) + 1;

            String seg;
            seg = Variant.fit( value[i], width );

            retval.append( seg );
        }


        return retval.toString();
    }

    void runDataQuiry( String sql, PrintStream out )
        throws ClassNotFoundException, SQLException
    {
        ResultSet rs = execute( sql );
        
        ResultSetMetaData md = rs.getMetaData();

        int cnt = md.getColumnCount();

        String[] s;
        s = toColumnNameStringArray( md, cnt );
        out.println( format( s, md ) );

        while( rs.next() ){
            s = toDataStringArray( rs, cnt );
            out.println( format( s, md ) );
        }

        close();
    }

    void runDataQuiryCsv( String sql, PrintStream out )
        throws ClassNotFoundException, SQLException
    {
        ResultSet rs = execute( sql );
        
        ResultSetMetaData md = rs.getMetaData();

        int cnt = md.getColumnCount();

        String[] s;
        s = toColumnNameStringArray( md, cnt );
        out.println( name.subroutine.etable.CsvRecord.toString( s ) );

        while( rs.next() ){
            s = toDataStringArray( rs, cnt );
            out.println( name.subroutine.etable.CsvRecord.toString( s ) );
        }

        close();
    }



    static void greet()
    {
        System.out.println( Resource.version() );
    }

    static void help()
    {
        System.out.println( "usage: " );
        System.out.println( "Quiry [options] [sql_statement]" );
        System.out.println();
        System.out.println( "options: " );
        System.out.println( "-q <Quiry>      runs a predefined Quiry" );
        System.out.println( "-d <database>   specifies database connection " );
        System.out.println( "-s <style>      specifies output format style " );
        System.out.println( "-c              uses csv output" );
        System.out.println( "-o <output>     specifies output file" );
        System.out.println( "-t              prints structure only" );
        System.out.println( "-jpa            prints structure in JPA style");
        System.out.println( "-r              result is an Oracle ref cursor" );
        System.out.println( "-v              prints version" );
        System.out.println( "-dls            shows all database aliases" );
        System.out.println( "-C <config>     specifies config file" );
    }

    static void showDatabase( String id, Scriptable db )
    {
        name.subroutine.util.Variant v = new name.subroutine.util.Variant();
        String buf;
      
        buf = v.fit( id, 10 );
        System.out.print( buf );

        Object val;
        val = db.get( "driver", db );
        if( val != Scriptable.NOT_FOUND ){
            buf = Context.toString( val );
        }
        else{
            buf = "";
        }
        buf = v.fit( buf, 30 );
        System.out.print( buf );

        val = db.get( "url", db );
        if( val != Scriptable.NOT_FOUND ){
            buf = Context.toString( val );
        }
        else{
            buf = "";
        }
        buf = v.fit( buf, 30 );
        System.out.print( buf );

        System.out.println();

        System.out.print( v.fit( "", 10 ) );

        val = db.get( "user", db );
        if( val != Scriptable.NOT_FOUND ){
            buf = Context.toString( val );
        }
        else{
            buf = "";
        }
        buf = v.fit( buf, 30, 0 );
        System.out.print( buf );

        val = db.get( "password", db );
        if( val != Scriptable.NOT_FOUND ){
            buf = Context.toString( val );
        }
        else{
            buf = "";
        }
        buf = v.fit( buf, 30 );
        System.out.print( buf );

        System.out.println();
        System.out.println();
    }

    static void showDatabases()
    {
        Scriptable world = Resource.getWorld();
        Scriptable dball = (Scriptable)world.get( "db", world );

        Object[] id_array = dball.getIds();

        for( int i = 0; i < id_array.length; ++i ){
            String id = Context.toString( id_array[i] );
            Scriptable db = (Scriptable)dball.get( id, dball );
            showDatabase( id, db );
        }
    }

    public static void main( String[] argv )
        throws Exception
    {
        final int INIT                                      = 10;
        final int DATABASE                                  = 11;
        final int STYLE                                     = 12;
        final int QUIRY                                     = 13;
        final int OUTPUT                                    = 14;
        final int CONFIG                                    = 15;

        // parse command line options
        int context = INIT;
        String sql = null;
        String database = null;
        String style = null;
        String output = null;
        boolean struct = false;
        boolean csv = false;
        boolean jpa = false;
        boolean is_ref_cur = false;
        boolean quiet = false;
        for( int i = 0; i < argv.length; ++i ){
            if( context == INIT ){
                if( "-d".equals( argv[i] ) ){
                    context = DATABASE;
                }
                else if( "-s".equals( argv[i] ) ){
                    context = STYLE;
                }
                else if( "-q".equals( argv[i] ) ){
                    context = QUIRY;
                }
                else if( "-o".equals( argv[i] ) ){
                    context = OUTPUT;
                }
                else if( "-t".equals( argv[i] ) ){
                    struct = true;
                }
                else if( "-c".equals( argv[i] ) ){
                    csv = true;
                }
                else if( "-r".equals( argv[i] ) ){
                    is_ref_cur = true;
                }
                else if( "-dls".equals( argv[i] ) ){
                    showDatabases();
                    return;
                }
                else if( "-v".equals( argv[i] ) ){
                    System.out.println( Resource.version() );
                    return;
                }
                else if( "-C".equals( argv[i] ) ){
                    context = CONFIG;
                }
                else if( "-jpa".equals( argv[i] ) ){
                    struct = true;
                    jpa = true;
                }
                else{
                    sql = argv[i];
                }
            }
            else if( context == DATABASE ){
                database = argv[i];
                context = INIT;
            }
            else if( context == STYLE ){
                style = argv[i];
                context = INIT;
            }
            else if( context == QUIRY ){
                Scriptable world = Resource.getWorld();
                Object val;
                val = world.get( argv[i], world );
                try{
                    Scriptable quiry = (Scriptable)val;
                    val = quiry.get( "database", quiry );
                    if( val != Scriptable.NOT_FOUND ){
                        database = Context.toString( val );
                    }
                    val = quiry.get( "style", quiry );
                    if( val != Scriptable.NOT_FOUND ){
                        style = Context.toString( val );
                    }
                    val = quiry.get( "sql", quiry );
                    if( val != Scriptable.NOT_FOUND ){
                        sql = Context.toString( val );
                    }
                }
                catch( Exception ex ){
                }
                context = INIT;
            }
            else if( context == OUTPUT ){
                output = argv[i];
                context = INIT;
            }
            else if( context == CONFIG ){
                Resource._config_file = argv[i];
                context = INIT;
            }
        }

        Quiry q = new Quiry();
        q._is_ref_cur = is_ref_cur;

        if( database == null ){
            System.err.println( "no database selected" );
            help();
            return;
        }
        else{
            // test connection
            try{
                q.setDatabase( database );
                Connection conn = q.getConn();
                conn.close();
            }
            catch( ClassNotFoundException ex ){
                System.err.println( "cannot find database driver for " +
                    database );
            }
            catch( Exception ex ){
                System.err.println( "invalid database " + database );
                ex.printStackTrace();
                return;
            }
        }

        if( sql == null ){
            System.err.println( "no sql statement specified" );
            help();
            return;
        }

        PrintStream out;
        FileOutputStream fout = null;
        if( output == null ){
            out = System.out;
        }
        else{
            fout = new FileOutputStream( output );
            out = new PrintStream( fout );
        }

        try{
            if( struct ){
                if( csv ){
                    q.runMetaDataQuiryCsv( sql, out );
                }
                else if( jpa ){
                    q.runMetaDataQuiryJpa( sql, out );
                }
                else{
                    if( style == null ){
                        style = "st_dictionary";
                    }
                    q.runMetaDataQuiry( sql, out, style );
                }
            }
            else{
                if( csv ){
                    q.runDataQuiryCsv( sql, out );
                }
                else{
                    if( style == null ){
                        q.runDataQuiry( sql, out );
                    }
                    else{
                        System.out.println( "using style " + style );
                        q.runDataQuiry( sql, out, style );
                    }
                }
            }
        }
        catch( ClassNotFoundException ex ){
            System.err.println( "Cannot find database driver" );
        }
        catch( SQLException ex ){
            System.err.println( ex.getMessage() );
        }

        if( fout != null ){
            out.flush();
            fout.flush();
            fout.close();
        }
    }
}
