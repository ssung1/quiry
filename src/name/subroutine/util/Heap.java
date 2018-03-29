package name.subroutine.util;

import java.util.*;

/**
 * This is a min-heap implementation...smallest goes on top
 *
 * Remember:
 *
 *                    1
 *                   / \
 *                  2   3
 *                 / \
 *                4   5
 *
 * A heap starts with 1 so that the parent would be child / 2
 */
public class Heap
{
    Object _val[];

    int _max_size;
    int _size;
    Comparator _comp = null;

    public Heap( int capacity )
    {
	_init( capacity );
    }

    public Heap()
    {
	_init();
    }

    void _init( int capacity )
    {
	_max_size = capacity;
	_val = new Object[_max_size + 1];
	_size = 0;
    }
    void _init()
    {
	_init( 4 );
    }

    void _resize( int capacity )
    {
	Object big[];
	_max_size = capacity;
	big = new Object[_max_size + 1];
	for( int i = 1; i < _size + 1; i++ ){
	    big[i] = _val[i];
	}
	_val = big;
    }

    public void setComparator( Comparator comp )
    {
	_comp = comp;
    }

    public boolean add( Object o )
    {
	if( _size >= _max_size ){
	    _resize( _max_size * 2 );
	}
	_val[_size + 1] = o;
	_size++;
	_sift_up();
	return true;
    }

    /**
     * Removes the root of the heap.
     */
    public Object remove()
    {
	return remove( 0 );
    }

    /**
     * The index is zero-based
     *
     * (But our array is not, so we add one)
     */
    Object remove( int idx )
    {
	// first save the object at index
	Object retval = _val[idx + 1];

	int parent = idx + 1;
	int left_child;
	int right_child;

	// now take the last object and shove it
	// into the empty spot
	_val[parent] = _val[_size];

	// start the sifting loop
	while( true ){
	    left_child = parent * 2;
	    right_child = parent * 2 + 1;

	    Object parent_obj = _val[parent];

	    Object left_child_obj = null;
	    Object right_child_obj = null;
	    
	    if( left_child <= _size ){
		left_child_obj = _val[left_child];
	    }
	    if( right_child <= _size ){
		right_child_obj = _val[right_child];
	    }

	    // this step is not really necessary, since
	    // min will return parent if both children are null
	    if( left_child_obj == null ){
		// no left child means no right child either
		break;
	    }

	    Object min;
	    min = min( parent_obj, left_child_obj, right_child_obj );

	    // no need to shift down anymore, so we break
	    if( min == parent_obj ){
		break;
	    }

	    if( min == right_child_obj ){
		_val[parent] = right_child_obj;
		_val[right_child] = parent_obj;
		parent = right_child;
		continue;
	    }

	    if( min == left_child_obj ){
		_val[parent] = left_child_obj;
		_val[left_child] = parent_obj;
		parent = left_child;
		continue;
	    }
	}
	_size--;
	return retval;
    }

    public int size()
    {
	return _size;
    }

    void _sift_up()
    {
	// start with whatever's at size...that's the last one inserted
	int mover = _size;
	int parent;
	Object mover_obj;
	Object parent_obj;

	while( mover > 1 ){
	    parent = mover / 2;
	    mover_obj = _val[mover];
	    parent_obj = _val[parent];
	    
	    int diff;
	    diff = compare( mover_obj, parent_obj );

	    if( diff < 0 ){
		// swap
		_val[parent] = mover_obj;
		_val[mover] = parent_obj;
	    }
	    mover = parent;
	}
    }

    int compare( Object child, Object parent )
    {
	// first use comparator if there is one
	if( _comp != null ){
	    return _comp.compare( child, parent );
	}
	// or if objects are comparable, use compareTo method
	if( child instanceof Comparable &&
	    parent instanceof Comparable ){
	    Comparable cc = (Comparable)child;
	    Comparable pc = (Comparable)parent;
	    return cc.compareTo( pc );
	}
	// if that doesn't work, turn them into strings then compare
	String cs;
	String ps;
	cs = child.toString();
	ps = parent.toString();
	return cs.compareTo( ps );
    }

    /**
     * This version of compare handles nulls
     * (nulls are considered positive infinity)
     */
    int compareWithNullHandling( Object a, Object b )
    {
	if( a == null ) return 1;
	if( b == null ) return -1;
	return compare( a, b );
    }

    /**
     * Returns the minimum of the three objects
     */
    Object min( Object a, Object b, Object c )
    {
	Object min;
	min = a;

	if( compareWithNullHandling( min, b ) > 0 ){
	    min = b;
	}
	if( compareWithNullHandling( min, c ) > 0 ){
	    min = c;
	}

	return min;
    }

    public String toString()
    {
	StringBuffer buf = new StringBuffer();
	for( int i = 1; i < _size + 1; i++ ){
	    if( i > 1 ){
		buf.append( ", " );
	    }
	    buf.append( _val[i] );
	}
	return buf.toString();
    }
}

