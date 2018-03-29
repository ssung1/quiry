package name.subroutine.util;

import java.util.*;

/**
 * Compares an array using only the chosen components in the array
 */
public class ArrayComparator implements Comparator
{
    /**
     * List of component indices to compare
     */
    int _c_lst[];

    public ArrayComparator( int c_lst[] )
    {
	_c_lst = c_lst;
    }

    /**
     * Optional comparator to use
     */
    Comparator _cmp;
    public ArrayComparator( int c_lst[], Comparator cmp )
    {
	_c_lst = c_lst;
	_cmp = cmp;
    }

    public int compare( Object a_, Object b_ )
    {
	Object[] a = (Object[])a_;
	Object[] b = (Object[])b_;

	for( int i = 0; i < _c_lst.length; i++ ){
	    int idx = _c_lst[i];

	    int diff;
	    if( _cmp != null ){
		diff = _cmp.compare( a[idx], b[idx] );
	    }
	    else{
		Comparable as = (Comparable)a[idx];
		Comparable bs = (Comparable)b[idx];

		diff = as.compareTo( bs );
	    }

	    if( diff != 0 ){
		return diff;
	    }
	}
	return 0;
    }
}
