package name.subroutine.quiry;

import java.lang.reflect.*;
import java.net.*;
import java.io.*;
import java.util.*;
import java.text.*;
import java.sql.SQLException;

import org.mozilla.javascript.*;

import name.subroutine.rdb.*;

public class Resource
{
    static long _script_time = -1;
    static Scriptable _world;
    
    /**
     * yyyymmdd
     */
    static int _log_file_number = -1;
    static FileWriter _log_file;
    static PrintWriter _log_out;

    public static String _config_file;

    public static String version()
    {
        return "Quick Query   version 1.2" +
               "   2018-03-29";
    }

    public static File getInitScript()
    {
        if( _config_file != null ){
            return new File( _config_file );
        }
        else{
            URL url = Resource.class.getClassLoader().
                getResource( "conf/system.js" );
            return new File( url.getFile() );
        }
    }

    public static Scriptable getWorld()
    {
        try{
            if( _script_time == -1 ||
                _script_time < getInitScript().lastModified() ){

                Context cx = Context.enter();
                cx.setOptimizationLevel( -1 );

                try{
                    _world = new ImporterTopLevel( cx );
                    
                    FileReader r = new FileReader( getInitScript() );
                    BufferedReader br = new BufferedReader( r );
                    cx.evaluateReader( _world, r,
                                       getInitScript().getAbsolutePath(),
                                       0, null );
                    r.close();
                    _script_time = getInitScript().lastModified();
                }
                catch( Exception ex ){
                    ex.printStackTrace();
                    _world = null;
                }

                Context.exit();
            }
        }
        catch( Exception ex ){
            ex.printStackTrace();
            _world = null;
        }

        return _world;
    }

    public static String conf( String key )
    {
        try{
            Scriptable world = getWorld();
            Object val;
            val = world.get( key, world );
            if( val == Scriptable.NOT_FOUND ) return null;

            return Context.toString( val );
        }
        catch( Exception ex ){
            return null;
        }
    }

    static void openLog( String file )
    {
        try{
            _log_file = new FileWriter( new File( Resource.conf( "log_dir" ),
                                                  file + ".log" ) );
            _log_out = new PrintWriter( _log_file, true );
        }
        catch( IOException ex ){
            _log_file = null;
            _log_out = new PrintWriter( System.out, true );
        }
    }

    public static PrintWriter log()
    {
        // get today
        DateFormat df;
        df = new SimpleDateFormat( "yyyyMMddhhmm" );
        String s_date = df.format( new Date() );
        //// int date = Integer.parseInt( s_date );

        if( _log_file_number == -1 ){
            openLog( s_date );
            _log_file_number = 1;
        }
        // we don't change log file name during a single process
        // anymore.  this way we have a better chance of not having
        // a log file conflict
        //
        //else if( _log_file_number != date ){
        //    try{
        //        _log_file.close();
        //    }
        //    catch( Exception ex ){
        //    }
        //    openLog( s_date );
        //    _log_file_number = date;
        //}

        return _log_out;
    }

    public static void main( String[] argv )
        throws Exception
    {
        // dependencies
        Quiry                                                         x0001;

        System.out.println( version() );

        TestSuite ts = new TestSuite();
        Class c = ts.getClass();

        if( argv.length == 0 || argv[0].equals( "--help" ) ){
            System.out.println( "Available services:" );
            Method[] m = c.getDeclaredMethods();
            for( int i = 0; i < m.length; ++i ){
                System.out.println( m[i].getName() );
            }
            return;
        }
        
        Method m = c.getMethod( argv[0], new Class[] { String[].class } );
        argv = (String[])name.subroutine.util.Arrays.splice( argv, 1 );
        try{
            m.invoke( ts, new Object[] { argv } );
        }
        catch( InvocationTargetException ex ){
            Throwable cause = ex.getCause();
            if( ex.getCause() != null &&
                cause instanceof Exception ){
                throw (Exception)cause;
            }
            else{
                // re-throw
                throw ex;
            }
        }
    }
}

