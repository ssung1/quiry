package name.subroutine.etable;

/**
 * Definition of a field.
 */
public class CsvField extends ByteField
{
    public CsvField()
    {
	super();
    }
    public CsvField( String name )
    {
	super( name );
    }
    public CsvField( String name, int type )
    {
	super( name, type );
    }
}
