package name.subroutine.etable;

/**
 * Definition of a field.
 */
public class SdfField extends ByteField
{
    /**
     * 32 bytes to store an instance of this class
     * after being "serialized"
     */
    public static final int STORAGE_SIZE = 32;

    int _prec;

    int _index;

    public SdfField()
    {
	super();
    }
    public SdfField( String name )
    {
	super( name );
    }
    public SdfField( String name, int type )
    {
	super( name, type );
    }

    /**
     * Sets precision
     */
    public void prec( int p )
    {
	_prec = p;
    }
    /**
     * Returns precision
     */
    public int prec()
    {
	return _prec;
    }

    /**
     * Sets index flag
     */
    public void index( int i )
    {
	_index = i;
    }
    /**
     * Returns index flag
     */
    public int index()
    {
	return _index;
    }
    
    public Object clone()
    {
	SdfField nu = new SdfField( name(), type() );

	nu.prec( prec() );
	nu.size( size() );
	nu.index( index() );

	return nu;
    }
}
