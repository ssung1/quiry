package name.subroutine.rdb;

import java.util.*;
import java.sql.*;
import java.text.*;

import name.subroutine.etable.*;
import name.subroutine.util.*;

/**
 * Analogous to the java.sql.ResultSet class, but each "row" is an
 * object
 */
public class RdbResultSet
{
    RdbSession _ses;
    Rtbl _rtbl;
    Statement _stmt;
    ResultSet _rs;

    public RdbResultSet( RdbSession ses, Rtbl rtbl,
                         Statement stmt, ResultSet rs )
    {
        _ses = ses;
        _rtbl = rtbl;
        _stmt = stmt;
        _rs = rs;
    }

    public void close()
        throws SQLException
    {
        if( _stmt != null ){
            _stmt.close();
        }
        if( _rs != null ){
            _rs.close();
        }
    }

    protected void finalize()
        throws Throwable
    {
        try{
            close();
        }
        catch( Exception ex ){
        }
        super.finalize();
    }

    public boolean next()
        throws SQLException
    {
        return _rs.next();
    }

    public boolean previous()
        throws SQLException
    {
        return _rs.previous();
    }

    public Rtbl get()
        throws RdbException, SQLException
    {
        return _ses.toRtbl( _rtbl, _rs );
    }
}
