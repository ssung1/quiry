package name.subroutine.util;

import java.util.*;

import name.subroutine.etable.Record;

/**
 * Functions not included in the Java API
 */
public class Maps
{
    /**
     * turns a array of width 2 and height n into a Map
     */
    public static Map toMap( Object[] pair )
    {
	Map map = new HashMap();

	int i;

        // just in case pair.length is odd
	try{
	    for( i = 0; i < pair.length; i += 2 ){
                // just in case pair[i] is null
                try{
                    map.put( pair[i], pair[i + 1] );
                }
                catch( NullPointerException ex ){
                }
	    }
	}
	catch( ArrayIndexOutOfBoundsException ex ){
	}

	return map;
    }

    public static Map toMap( List list )
    {
        Iterator it = list.iterator();
        Map retval = new HashMap();

        while( true ){
            if( !it.hasNext() ) break;

            Object key;
            key = it.next();

            Object val;
            if( it.hasNext() ){
                val = it.next();
            }
            else{
                val = null;
            }

            retval.put( key, val );
        }
        return retval;
    }

    /**
     * turns a result set into a map
     */
    public static Map toMap( java.sql.ResultSet rs )
        throws java.sql.SQLException
    {
        return toMap( Lists.toList( rs ) );
    }

    /**
     * Converts a relational table record to a 2xN list
     */
    public static Map toMap( name.subroutine.rdb.Rdb rdb,
                             name.subroutine.rdb.Rtbl rtbl )
        throws name.subroutine.rdb.RdbException
    {
        return toMap( Lists.toList( rdb, rtbl ) );
    }

    /**
     * turns a Record into a map
     */
    public static Map toMap( Record rec )
    {
	return toMap( Lists.toList( rec ) );
    }

    /**
     * Converts a map into a name value pair for printing
     */
    public static String toPair( Map pair )
    {
	StringBuffer buf = new StringBuffer();

	Iterator it;
	for( it = pair.entrySet().iterator(); it.hasNext(); ){
	    Map.Entry entry;
	    entry = (Map.Entry)it.next();

	    buf.append( entry.getKey() );
	    buf.append( "=" );
	    buf.append( entry.getValue() );
	    buf.append( "\n" );
	}
	return buf.toString();
    }

    /**
     * Converts a map into a name value pair for printing
     *
     * @param hinge: a string to connect each pair
     */
    public static String toPair( Map pair, String hinge )
    {
	StringBuffer buf = new StringBuffer();

	Iterator it;
	for( it = pair.entrySet().iterator(); it.hasNext(); ){
	    Map.Entry entry;
	    entry = (Map.Entry)it.next();

	    buf.append( entry.getKey() );
	    buf.append( "=" );
	    buf.append( entry.getValue() );
	    buf.append( hinge );
	}
	return buf.toString();
    }
}
