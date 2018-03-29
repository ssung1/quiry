package name.subroutine.etable;

/**
 * Definition of a field.
 */
public class DbfField extends ByteField
{
    /**
     * 32 bytes to store an instance of this class
     * after being "serialized"
     */
    public static final int STORAGE_SIZE = 32;

    int _prec;

    int _index;

    public DbfField()
    {
	super();
    }
    public DbfField( String name )
    {
	_name = new StringBuffer( name.toUpperCase() );
	_type = -1;
    }
    public DbfField( String name, int type )
    {
	_name = new StringBuffer( name.toUpperCase() );
	_type = type;
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

    public void name( String n )
    {
	_name = new StringBuffer( n.toUpperCase() );
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
	DbfField nu = new DbfField( name(), type() );

	nu.prec( prec() );
	nu.size( size() );
	nu.index( index() );

	return nu;
    }
}
