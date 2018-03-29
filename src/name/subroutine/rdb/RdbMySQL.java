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
public class RdbMySQL extends Rdb
{
    public java.lang.reflect.Field getField( Rtbl table, String field )
        throws RdbException
    {
	return _getFieldIgnoreCase( table.getClass(), field );
    }
}
