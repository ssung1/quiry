package name.subroutine.util;

import java.util.*;

import name.subroutine.etable.*;

/**
 * Utilities involved in set operations
 */
public class Sets
{
    /**
     * Returns the intersection of two sets
     */
    public static Set getIntersection( Set a, Set b )
    {
	Set retval = new HashSet();
	Iterator it;
	for( it = b.iterator(); it.hasNext(); ){
	    Object e;
	    e = it.next();

	    if( a.contains( e ) ){
		retval.add( e );
	    }
	}

	return retval;
    }

    /**
     * Returns a set of elements that are in a but not in b
     */
    public static Set getDifference( Set a, Set b )
    {
        Set retval = new HashSet();
        Iterator it;
        for( it = a.iterator(); it.hasNext(); ){
            Object e;
            e = it.next();

            if( !b.contains( e ) ){
                retval.add( e );
            }
        }

        return retval;
    }

    /**
     * Returns the intersection of an array of sets
     */
    public static Set getIntersection( Set[] a ){
	Set retval = null;
	for( int i = 0; i < a.length; i++ ){
	    if( retval == null ){
		retval = a[i];
		continue;
	    }
	    retval = getIntersection( retval, a[i] );
	}
	return retval;
    }

    /**
     * Returns a sort list of elements inside a given set
     */
    public static List sort( Set input )
    {
        List retval;
        retval = new ArrayList();

        for( Iterator it = input.iterator(); it.hasNext(); ){
            retval.add( it.next() );
        }

        java.util.Collections.sort( retval );

        return retval;
    }
}
