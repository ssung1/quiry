package name.subroutine.etable;

import java.util.*;
import java.sql.*;

/**
 * A record very much like the one in relational databases.
 *
 * Its fields are defined elsewhere and are shared with other records.
 */
public class DbfRecord extends AbstractRecord
{
    /**
     * Creates a string representation of the given record
     */
    public static String toString( Record record )
    {
	// starts with undeleted record
	StringBuffer buf = new StringBuffer( " " );

	for( int i = 0; i < record.fieldCnt(); i++ ){
	    String val;
	    Field f;
	    f = record.getFld( i );

	    int just;
	    if( f.type() == 'N' ){
		double num;
		num = Double.parseDouble( record.get( i ).toString() );
		val = name.subroutine.util.Variant.fit(
			  num,
                          f.size(),
			  f.prec()
                      );
	    }
	    else{
		val = name.subroutine.util.Variant.fit(
		          record.get( i ).toString(),
			  f.size()
		      );
	    }
	    
	    buf.append( val );      
	}
	return buf.toString();
    }

    public String toString()
    {
	return toString( this );
    }

    public DbfRecord()
    {
	_value_lst = new Vector();
    }

    public DbfRecord( List field_lst )
    {
	_field_lst = field_lst;
	_value_lst = new Vector();
    }

    /**
     * Gets a field index by name or -1 if not found
     *
     * Compares only first 10 characters.  Case insensitive.
     */
    public int getFld( String name )
    {
	if( name.length() > 10 ){
	    name = name.substring( 0, 10 );
	}

	for( int i = 0; i < _field_lst.size(); i++ ){
	    Field field = getFld( i );

	    String fname;
	    fname = field.name();

	    if( fname.length() > 10 ){
		fname = fname.substring( 0, 10 );
	    }

	    if( name.equalsIgnoreCase( fname ) ){
		return i;
	    }
	}
	return -1;
    }
    
}
