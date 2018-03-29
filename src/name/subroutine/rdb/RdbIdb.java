package name.subroutine.rdb;

import java.lang.reflect.*;
import java.util.*;
import java.sql.*;
import java.text.*;

import name.subroutine.etable.*;
import name.subroutine.util.*;

/**
 * This is the Relational Database class that works on the
 * Rtbl classes.
 */
public class RdbIdb extends Rdb
{
    public java.lang.reflect.Field getField( Rtbl table, String field )
	throws RdbException
    {
	return _getFieldIgnoreCase( table.getClass(), field );
    }

    /**
     * InstantDB (which has lost support from Lutris :() does not
     * allow program to exit unless the connection is shut down, so
     * we have to reconnect and disconnect for every statement
     * execution, which is not too bad because it is a local database
     * anyways.
     *
     * but we try not to for now......
    public ResultSet execute( String statement )
	throws SQLException
    {
        try{
            reconnect();
        }
        catch( ClassNotFoundException cnfex ){
            throw new SQLException( "JDBC Driver " +
                                    _driver +
                                    " cannot be found" );
        }

	Statement stmt;

        stmt = _conn.createStatement();

        boolean has_result;

        has_result = stmt.execute( statement );

        _conn.close();
        if( has_result ){
            return stmt.getResultSet();
        }
        else{
            stmt.close();
            return null;
        }
    }
    ***************************/
}
