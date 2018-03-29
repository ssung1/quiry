package name.subroutine.util;

import java.util.*;

import name.subroutine.etable.Record;
import name.subroutine.etable.Etable;

/**
 * Functions not included in the Java API
 */
public class Lists
{
    /**
     * turns an array into a list.
     *
     * We only make this method because java.util.Arrays.asList
     * returns a non-mutable list
     */
    public static List toList( Object[] array )
    {
        List retval = new ArrayList();
        retval.addAll( java.util.Arrays.asList( array ) );
        return retval;
    }

    /**
     * turns an array into an immutable list.
     *
     */
    public static List toImmutableList( Object[] array )
    {
        return java.util.Arrays.asList( array );
    }

    /**
     * Converts a resultset into a 2xN list
     *
     * Warning: field names recorded in resultset are inaccurate
     */
    public static List toList( java.sql.ResultSet rs )
        throws java.sql.SQLException
    {
        List retval = new ArrayList();

        java.sql.ResultSetMetaData meta;
        meta = rs.getMetaData();

        int cnt = meta.getColumnCount();
        for( int i = 0; i < cnt; i++ ){
            retval.add( meta.getColumnName( i + 1 ) );
            retval.add( rs.getObject( i + 1 ) );
        }
        return retval;
    }

    /**
     * Converts a relational table record to a 2xN list
     */
    public static List toList( name.subroutine.rdb.Rdb rdb,
                               name.subroutine.rdb.Rtbl rtbl )
        throws name.subroutine.rdb.RdbException
    {
        List retval = new ArrayList();
        List flst = rdb.getFieldNameLst( rtbl );
        for( Iterator it = flst.iterator(); it.hasNext(); ){
            String fieldname;
            fieldname = (String)it.next();

            retval.add( fieldname );
            retval.add( rdb.getValue( rtbl, fieldname ) );
        }
        return retval;
    }

    /**
     * turns an etable to a list
     */
    public static List toList( Etable et )
    {
	List retval = new ArrayList();

	// first add the fields
	for( int i = 0; i < et.fieldCnt(); i++ ){
	    retval.add( et.getFld( i ).name() );
	}

	for( et.first(); !et.eof(); et.next() ){
	    for( int i = 0; i < et.fieldCnt(); i++ ){
		try{
		    retval.add( et.getVal( i ) );
		}
		catch( Exception ex ){
		    retval.add( "" );
		}
	    }
	}

	return retval;
    }

    /**
     * turns an etable to a list
     */
    public static List toList( Record rec )
    {
	List retval = new ArrayList();

        for( int i = 0; i < rec.fieldCnt(); i++ ){
            try{
                retval.add( rec.getFld( i ) );
            }
            catch( Exception ex ){
                continue;
            }
            try{
                retval.add( rec.get( i ) );
            }
            catch( Exception ex ){
                retval.add( "" );
            }
        }

	return retval;
    }

    /**
     * Turns a map into a list
     */
    public static List toList( Map map )
    {
        List retval = new ArrayList();
        for( Iterator it = map.entrySet().iterator(); it.hasNext(); ){
            Map.Entry entry;
            entry = (Map.Entry)it.next();
            retval.add( entry.getKey() );
            retval.add( entry.getValue() );
        }
        return retval;
    }

    /**
     * Converts a 2xn array into a name value pair for printing
     *
     * @param hinge: the string to join the pairs
     */
    public static String toPair( List pair, String hinge )
    {
	Object[] o;
	o = pair.toArray( new Object[0] );

	return Arrays.toPair( o, hinge );
    }

}
