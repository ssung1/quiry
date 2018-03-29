package name.subroutine.util;

import java.util.*;

/**
 * Functions not included in the Java API
 */

public class Matrix
{
    /**
     * Returns the height of the matrix
     */
    public static int getHeight( Object[] a, int w )
    {
	return a.length / w;
    }

    /**
     * Puts b on the right side of a
     *
     * @param a a matrix
     * @param aw the width of a
     * @param b another matrix
     * @param bw the width of b
     */
    public static Object[] juxtapose( Object[] a, int aw,
				      Object[] b, int bw )
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

	List retval = new Vector();

	int ah = getHeight( a, aw );
	int bh = getHeight( b, bw );
	int h = Math.max( ah, bh );

	for( int row = 0; row < h; row++ ){
	    int aidx;
	    int bidx;

	    if( row < ah ){
		aidx = row * aw;
	    }
	    else{
		aidx = (ah - 1) * aw;
	    }

	    if( row < bh ){
		bidx = row * bw;
	    }
	    else{
		bidx = (bh - 1) * bw;
	    }

	    for( int i = 0; i < aw; i++ ){
		retval.add( a[aidx + i] );
	    }
	    for( int i = 0; i < bw; i++ ){
		retval.add( b[bidx + i] );
	    }
	}

	return retval.toArray( (Object[])o );
    }


    /**
     * Puts a on top of b
     *
     * @param a a matrix
     * @param aw the width of a
     * @param b another matrix
     * @param bw the width of b
     */
    public static Object[] stack( Object[] a, int aw,
				  Object[] b, int bw )
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

	List retval = new Vector();

	int ah = getHeight( a, aw );
	int bh = getHeight( b, bw );

	// this is the width we must fill up to if one matrix is
	// narrower than the other

	int w = Math.max( aw, bw );
	
	for( int row = 0; row < ah; row++ ){
	    int idx;

	    idx = row * aw;
	    for( int col = 0; col < w; col++ ){
		if( col < aw ){
		    retval.add( a[idx + col] );
		}
		else{
		    retval.add( a[idx + aw - 1] );
		}
	    }
	}

	for( int row = 0; row < bh; row++ ){
	    int idx;

	    idx = row * bw;
	    for( int col = 0; col < w; col++ ){
		if( col < bw ){
		    retval.add( b[idx + col] );
		}
		else{
		    retval.add( b[idx + aw - 1] );
		}
	    }
	}

	return retval.toArray( (Object[])o );
    }

    /**
     * Extracts rows by number
     *
     * @param begin    the row to start
     * @param end      the row to stop at and is not included in the extraction
     */
    public static Object[] extractRow( Object[] a, int aw, int begin, int end )
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

	List retval = new Vector();

	int row_cnt;
	row_cnt = end - begin;

	int init_idx;
	init_idx = begin * aw;

	int size;
	size = row_cnt * aw;

	for( int i = 0; i < size; i++ ){
	    int idx;
	    idx = init_idx + i;
	    retval.add( a[idx] );
	}

	return retval.toArray( (Object[])o );
    }

    /**
     * Extracts columns by number
     *
     * @param begin    the column to start
     * @param end      the column to stop at and is not included
     *                 in the extraction
     */
    public static Object[] extractCol( Object[] a, int aw, int begin, int end )
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

	List retval = new ArrayList();

        int size = a.length;
	for( int i = 0; i < size; i += aw ){
            for( int j = begin; j < end; j++ ){
                retval.add( a[i + j] );
            }
	}

	return retval.toArray( (Object[])o );
    }

    /**
     * <pre>
     * Transpose: a reflect about a \ line...err...somebody come up
     * with a better (mathematically correct) description
     *
     * User is responsible for finding out the new width
     *
     * Math is #1!
     *
     * A B C D
     * E F G H
     *
     * to
     *
     * A E
     * B F
     * C G
     * D H
     * </pre>
     */
    public static Object[] transpose( Object[] a, int aw )
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

	List retval = new Vector();

	int height;
	height = getHeight( a, aw );

	for( int j = 0; j < aw; j++ ){
	    for( int i = 0; i < height; i++ ){
		int idx;
		idx = i * aw + j;

		retval.add( a[idx] );
	    }
	}

	
	return retval.toArray( (Object[])o );
    }

    /**
     * Transforms an array to a two dimensional array
     *
     * @return an object array that holds individual rows of the
     * original matrix.  The returned object[] is a true Object[]
     * which cannot be casted into anything else
     */
    public static Object[] to2DMatrix( Object[] a, int aw )
    {
        Class cl;
        cl = a.getClass().getComponentType();

	int height = getHeight( a, aw );

	Object[] retval;
        try{
            retval = (Object[])java.lang.reflect.
                                   Array.newInstance( cl, height );
        }
        catch( Exception ex ){
            retval = new Object[height];
        }

	for( int i = 0; i < height; i++ ){
	    Object[] row;
	    row = extractRow( a, aw, i, i + 1 );

	    retval[i] = row;
	}

	return retval;
    }

    /**
     * Transforms an array to a one dimensional array
     *
     * @param a an array of arrays
     * @return an array containing all the elements in a
     */
    public static Object[] to1DMatrix( Object[] a )
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

	List retval;
	retval = new ArrayList();

	int height;
	height = a.length;
	for( int i = 0; i < height; i++ ){
	    int width;
	    Object[] row = (Object[])a[i];

	    width = row.length;
	    for( int j = 0; j < width; j++ ){
		retval.add( row[j] );
	    }
	}

	return retval.toArray( (Object[])o );
    }

    /**
     * Sorts the matrix by row, using natural ordering
     *
     * @param col_lst the list of column numbers to sort by
     */
    public static Object[] sortRow( Object[] a, int aw, int col_lst[] )
    {
	Object[] row_array;
	row_array = to2DMatrix( a, aw );

	ArrayComparator ac;
	ac = new ArrayComparator( col_lst );

	java.util.Arrays.sort( row_array, ac );

	return to1DMatrix( row_array );
    }

    /**
     * Sorts the matrix by row, using natural ordering
     *
     * @param col_lst the list of column numbers to sort by
     */
    public static List sortRow( List a, int aw, int col_lst[] )
    {
	Object o[];
	o = a.toArray();

	o = sortRow( o, aw, col_lst );

	return java.util.Arrays.asList( o );
    }
    
}
