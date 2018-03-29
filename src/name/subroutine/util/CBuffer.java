package name.subroutine.util;

/**
 * Implementation of a circular buffer
 */
public class CBuffer
{
    public int _max_size;
    public int _size;
    public int _tail;
    public Object[] _val;

    public CBuffer( int capacity )
    {
	init( capacity );
    }
    public CBuffer()
    {
	init( 2 );
    }

    void init( int capacity )
    {
	_max_size = capacity;

	_val = new Object[_max_size];

	_size = 0;
	_tail = 0;
    }

    public void push( Object o )
    {
        if( _max_size <= 0 ) return;

	_val[_tail] = o;
	_tail = (_tail + 1) % _max_size;

	if( _size < _max_size ){
	    _size++;
	}

	return;
    }

    public void clear()
    {
	_size = 0;
    }

    public int size()
    {
	return _size;
    }

    /**
     * Returns the nth element, starting from the beginning
     */
    public Object get( int n )
    {
	if( n >= _size ){
	    throw new IndexOutOfBoundsException( "size: " + _size );
	}
	int idx;
	idx = (_tail + _max_size + n - _size) % _max_size;
	return _val[idx];
    }
}
