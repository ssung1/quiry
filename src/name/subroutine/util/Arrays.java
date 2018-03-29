package name.subroutine.util;

import java.util.*;

import name.subroutine.etable.Record;
import name.subroutine.etable.Etable;

/**
 * Functions not included in the Java API
 */
public class Arrays
{
    /**
     * Returns a subset
     */
    public static Object[] splice( Object[] a, int start, int end )
    {
	Class cl;
	Object o;

	try{
	    cl = a.getClass().getComponentType();
            o = (Object[])java.lang.reflect.Array.newInstance( cl, 0 );
	}
	catch( Exception ex ){
	    o = new Object[0];
	}

	java.util.List l;

	l = java.util.Arrays.asList( a );

	java.util.List sl;

	sl = l.subList( start, end );

	return sl.toArray( (Object[])o );
    }

    public static Object[] splice( Object[] a, int start )
    {
	int end;
	end = a.length;

	return splice( a, start, end );
    }

    public static Object[] toArray( List list )
    {
        if( list == null ){
            return null;
        }
        return list.toArray();
    }

    /**
     * Converts a resultset into an 2xN array
     *
     * Warning: field names recorded in resultset are inaccurate
     */
    public static Object[] toArray( java.sql.ResultSet rs )
        throws java.sql.SQLException
    {
        return Lists.toList( rs ).toArray();
    }

    /**
     * Converts a relational table record to a 2xN list
     */
    public static Object[] toArray( name.subroutine.rdb.Rdb rdb,
                                    name.subroutine.rdb.Rtbl rtbl )
        throws name.subroutine.rdb.RdbException
    {
        return Lists.toList( rdb, rtbl ).toArray();
    }

    /**
     * turns a record into an array of 2 x N:
     * <pre>
     * Field0        Value0
     * Field1        Value1
     * ...
     * and so on
     * </pre>
     */
    public static Object[] toArray( Record rec )
    {
        return Lists.toList( rec ).toArray();
    }

    /**
     * From a map to a 2 by n array
     */
    public static Object[] toArray( Map map )
    {
        return Lists.toList( map ).toArray();
    }

    /**
     * turns an etable to an array
     */
    public static Object[] toArray( Etable et )
    {
	List l = Lists.toList( et );

	return l.toArray();
    }

    /**
     * Converts all objects in an array into strings
     */
    public static String[] toStringArray( Object[] ar )
    {
        return toStringArray( ar, "(null)" );
    }

    /**
     * Converts all objects in an array into strings
     */
    public static String[] toStringArray( Object[] ar, String null_ )
    {
        if( ar == null ) return null;

        String[] retval = new String[ar.length];
        for( int i = 0; i < ar.length; i++ ){
            Object o = ar[i];

            if( o == null ){
                retval[i] = null_;
                continue;
            }

            // array types
            if( o.getClass().isArray() ){
                if( o.getClass().getComponentType() == char.class ){
                    retval[i] = String.valueOf( (char[])o );
                    continue;
                }
            }

            // regular types
            retval[i] = String.valueOf( o );
        }
        return retval;
    }

    /**
     * Converts a 2xn array into a name value pair, much like a text
     * version of a map.  This function is for representation only.
     */
    public static String toPair( Object[] pair )
    {
	return toPair( pair, "\n" );
    }

    /**
     * Converts a 2xn array into a name value pair, much like a text
     * version of a map.  This function is for representation only.
     *
     * @param hinge: the string to join the pairs
     */
    public static String toPair( Object[] pair, String hinge )
    {
	StringBuffer buf = new StringBuffer();
	int i;
	try{
	    for( i = 0; i < pair.length; i += 2 ){
		if( i > 0 ){
		    buf.append( hinge );
		}
		buf.append( pair[i] );
		buf.append( "=" );
		buf.append( pair[i + 1] );
	    }
	}
	catch( ArrayIndexOutOfBoundsException ex ){
	}

	return buf.toString();
    }
}
