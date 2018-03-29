package name.subroutine.etable;

import java.util.*;
import java.sql.*;

/**
 * A record very much like the one in relational databases.
 *
 * Its fields are defined elsewhere and are shared with other records.
 */
public class DbfRecordByte extends DbfRecord
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

            try{
                val = new String( (byte[])record.get( i ), "UTF-8" );
            }
            catch( java.io.UnsupportedEncodingException ex ){
                val = new String( (byte[])record.get( i ) );
            }
            catch( ClassCastException ex ){
                System.out.println( record.get( i ).getClass() );
                throw ex;
            }

	    int just;
	    if( f.type() == 'N' ){
		double num;
		num = Double.parseDouble( val );
		val = name.subroutine.util.Variant.fit( 
                          num,
                          f.size(),
                          f.prec()
                      );
	    }
	    else{
		val = name.subroutine.util.Variant.fit( val, f.size() );
	    }
	    
	    buf.append( val );      
	}
	return buf.toString();
    }

    public String toString()
    {
	return toString( this );
    }

    public DbfRecordByte()
    {
	_value_lst = new Vector();
    }

    public DbfRecordByte( List field_lst )
    {
	_field_lst = field_lst;
	_value_lst = new Vector();
    }

    /**
     * Sets a value in a record by index
     */
    public Record set( int idx, String value )
    {
	_value_lst.set( idx, value.getBytes() );
	return this;
    }
}
