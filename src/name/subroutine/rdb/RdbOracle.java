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
public class RdbOracle extends Rdb
{
    /**
     * Retrieves a field object from t
     *
     * Case insensitive
     *
     */
    public java.lang.reflect.Field getField( Class t, String field_ )
        throws RdbException
    {
	return _getFieldIgnoreCase( t, field_ );
    }
}
