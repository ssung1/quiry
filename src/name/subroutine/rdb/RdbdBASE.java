package name.subroutine.rdb;

import java.lang.reflect.*;
import java.util.*;
import java.sql.*;

import name.subroutine.etable.*;
import name.subroutine.util.*;

/**
 * This is the Relational Database class that works on the
 * Rtbl classes.
 */
public class RdbdBASE extends Rdb
{
    /**
     * Retrieves a field object from t
     *
     * Case insensitive
     *
     */
    public java.lang.reflect.Field _getField( Class t, String field_ )
	throws RdbException
    {
	return _getFieldIgnoreCase( t, field_ );
    }

    /**
     * Retrieves a field object from t
     *
     * This is used because it searches for superclasses
     * 
     * For some reason, Java API only searches for superclasses if
     * it returns only public fields
     */
    public java.lang.reflect.Field _getFieldIgnoreCase( Class t,
						       String field_ )
	throws RdbException
    {
	String field = "__" + field_;

	java.lang.reflect.Field[] f;

	f = t.getDeclaredFields();

	for( int i = 0; i < f.length; i++ ){
	    String short_name;
	    short_name = f[i].getName();

	    if( short_name.length() > 12 ){
		short_name = short_name.substring( 0, 12 );
	    }

	    String short_field;
	    if( field.length() > 12 ){
		short_field = field.substring( 0, 12 );
	    }
	    else{
		short_field = field;
	    }

	    if( short_name.equalsIgnoreCase( short_field ) ){
		return f[i];
	    }
	}

	t = t.getSuperclass();

	if( t != null && !t.equals( Object.class ) ){
	    return _getFieldIgnoreCase( t, field_ );
	}

        throw new RdbException( "Cannot find Class: " + t +
                                " Field: " + field_ );
    }

    /**
     * 
     * FoxPro doesn't allow to have more 10 characters for its field's name,
     * return a list of field names.
     *
     **/
    public List getFieldNameLst( Rtbl table )
    {
	List list = new Vector();
	
	Class t;
	t = table.getClass();
	
	java.lang.reflect.Field[] f;
	f = t.getFields();
	
	for( int i = 0; i < f.length; i++ ){
	    if( f[i].getName().startsWith( "__" ) ){
		String fieldName = f[i].getName().substring(2);
		
		if ( fieldName.length() > 10 )
		    fieldName = fieldName.substring(0,10);
		
		list.add( fieldName );
	    } // end if
	} // end for
	
	return list;
    } // end public List getFieldNameLst()

    /**
     * 
     * Return default table name.
     *
     */
    public String getTableName( Rtbl table )
    {
	String name;
	name = super.getTableName( table );

	if( name.length() < 8 ) return name;
	
	return name.substring( 0, 8 );
    } // end public String getTableName()

} // end class RdbdBASE
