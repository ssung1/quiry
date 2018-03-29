package name.subroutine.rdb;

import java.lang.reflect.*;
import java.util.*;
import java.sql.*;
import java.text.*;

/**
 * This is similar to java.lang.Class but always returns a valid
 * "getComponentType" when called so that the user does not need
 * to make awkward isArray() checks all the time
 */
public class RdbClass
{
    Class _base;

    boolean _is_array;
    Class _type;

    public RdbClass( java.lang.Class c )
    {
        _is_array = isArray( c );
        _type = getType( c );
    }

    public boolean isArray()
    {
        return _is_array;
    }

    public Class getType()
    {
        return _type;
    }


    public static boolean isArray( java.lang.Class c )
    {
        return c.isArray();
    }

    /**
     * Returns the component class if the given class is an array
     * Otherwise returns itself
     */
    public static Class getType( java.lang.Class c )
    {
        if( c.isArray() ){
            return c.getComponentType();
        }
        else{
            return c;
        }
    }

}
