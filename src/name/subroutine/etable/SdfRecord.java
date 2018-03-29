package name.subroutine.etable;

import java.util.*;
import java.sql.*;

/**
 * A record very much like the one in relational databases.
 *
 * Its fields are defined elsewhere and are shared with other records.
 */
public class SdfRecord extends AbstractRecord
{
    /**
     * Creates a string representation of the given record
     */
    public static String toString( Record record )
    {
	StringBuffer buf = new StringBuffer( "" );

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
	buf.append( System.getProperty( "line.separator" ) );
	return buf.toString();
    }

    public String toString()
    {
	return toString( this );
    }

    public SdfRecord()
    {
	_value_lst = new Vector();
    }

    public SdfRecord( List field_lst )
    {
	_field_lst = field_lst;
	_value_lst = new Vector();
    }
}
