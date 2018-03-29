package name.subroutine.rdb;

import java.util.*;
import java.sql.*;
import java.text.*;

import name.subroutine.etable.*;
import name.subroutine.util.*;

/**
 * Analogous to the java.sql.Connection class, without having to
 * provide connection string every time
 */
public class RdbSession
{
    Rdb _db;
    Connection _conn;

    public RdbSession( Rdb db, Connection conn )
    {
        _db = db;
        _conn = conn;
    }

    public void close()
        throws SQLException
    {
        _conn.close();
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

    /**
     * Just in case people want to access connection directly
     */
    public Connection getConn()
    {
        return _conn;
    }

    public Rdb getRdb()
    {
        return _db;
    }

    /**
     * Prepares a callable statement
     */
    public CallableStatement prepareCall( String sql )
        throws SQLException
    {
        CallableStatement retval;

        retval = _conn.prepareCall( sql );

        return retval;
    }

    public PreparedStatement prepareStatement( String sql )
        throws SQLException
    {
        PreparedStatement retval;

        retval = _conn.prepareStatement( sql );

        return retval;
    }

    public ResultSet execute( String statement )
        throws SQLException
    {
        Statement stmt;
        stmt = _conn.createStatement();
        if( stmt.execute( statement ) ){
            return stmt.getResultSet();
        }
        else{
            stmt.close();
            return null;
        }
    }

    public RdbResultSet openRdbResultSet( Rtbl rtbl, String statement )
        throws SQLException
    {
        Statement stmt;
        stmt = _conn.createStatement();
        if( stmt.execute( statement ) ){
            return new RdbResultSet( this, rtbl, stmt, stmt.getResultSet() );
        }
        else{
            stmt.close();
            return null;
        }
    }

    /**
     * Sets auto commit status
     */
    public void setAutoCommit( boolean ac )
        throws SQLException
    {
        _conn.setAutoCommit( ac );
    }
    
    /**
     * Commit!
     */
    public void commit()
        throws SQLException
    {
        _conn.commit();
    }

    /**
     * Not commit!
     */
    public void rollback()
        throws SQLException
    {
        _conn.rollback();
    }

    /**
     * Creates the table
     */
    public void create( Rtbl table )
        throws RdbException, SQLException
    {
        try{
            drop( table );
        }
        catch( Exception ignored ){
        }

        String sql;
        sql = _db.toCreate( table );
        
        execute( sql );
    }

    /**
     * Drops the table
     */
    public void drop( Rtbl table )
        throws SQLException
    {
        String sql;
        sql = _db.toDrop( table );
        
        execute( sql );
    }

    public void index( Rtbl table )
        throws SQLException
    {
        String[] sql;
        sql = _db.toIndex( table );

        for( int i = 0; i < sql.length; i++ ){
            execute( sql[i] );
        }
    }

    /**
     * Inserts a record into the database
     */
    public void insert( Rtbl rec )
        throws RdbException, SQLException
    {
        execute( _db.toInsert( rec ) );
    }

    /**
     * Convert the current record in the result set to an Rtbl object
     *
     * @param rtbl is the object to fit the result set into.  It will
     * not be changed; rather, a new Rtbl object will be created.
     *
     * @param rs is the resultset from a query.  Since the fieldnames
     * in resultset are not reliable, we assume the order of the
     * columns are the same as the order returned by getFieldNameLst
     */
    public Rtbl toRtbl( Rtbl rtbl, ResultSet rs )
        throws RdbException, SQLException
    {
        List rec = name.subroutine.util.Lists.toList( rs );
        List flst = _db.getFieldNameLst( rtbl );
        // reset the column names with standard names
        for( int i = 0; i < rec.size(); i += 2 ){
            rec.set( i, flst.get( i / 2 ) );
        }

        Rtbl retval;
        retval = _db.toRtbl( rtbl, rec );
        retval.resolve( this );
        retval.setIsNew( false );
        retval.setIsModified( false );

        return retval;
    }

    /**
     * Convert the current record in the result set to an Rtbl object
     *
     * @param rtbl_name is the table name suitable for calling
     * Rdb.getRtbl
     *
     * @param rs is the resultset from a query.  Since the fieldnames
     * in resultset are not reliable, we assume the order of the
     * columns are the same as the order returned by getFieldNameLst
     */
    public Rtbl toRtbl( String rtbl_name, ResultSet rs )
        throws RdbException, SQLException
    {
        return toRtbl( _db.getRtbl( rtbl_name ), rs );
    }

    /**
     * Convert the result set to a list of Rtbl objects
     *
     * @param rtbl is the object to fit the result set into.  It will
     * not be changed; rather, a new Rtbl object will be created.
     *
     * @param rs is the resultset from a query.  Since the fieldnames
     * in resultset are not reliable, we assume the order of the
     * columns are the same as the order returned by getFieldNameLst
     */
    protected List toRtblLst( Rtbl rtbl, ResultSet rs )
        throws RdbException, SQLException
    {
        List retval = new ArrayList();

        while( rs.next() ){
            Rtbl rec;
            rec = toRtbl( rtbl, rs );
            retval.add( rec );
        }
        
        return retval;
    }
    
    /**
     * Convert the result set to a list of Rtbl objects
     *
     * @param rtbl_name is the table name suitable for calling
     * Rdb.getRtbl
     *
     * @param rs is the resultset from a query.  Since the fieldnames
     * in resultset are not reliable, we assume the order of the
     * columns are the same as the order returned by getFieldNameLst
     */
    protected List toRtblLst( String rtbl_name, ResultSet rs )
        throws RdbException, SQLException
    {
        return toRtblLst( _db.getRtbl( rtbl_name ), rs );
    }

    /**
     * Convert the result set to a list of Rtbl objects
     *
     * @param rtbl is the object to fit the result set into.  It will
     * not be changed; rather, a new Rtbl object will be created.
     *
     * @param rs is the resultset from a query.  Since the fieldnames
     * in resultset are not reliable, we assume the order of the
     * columns are the same as the order returned by getFieldNameLst
     */
    public List toRtblLst( Rtbl rtbl, ResultSet rs,
                           int row_offset, int row_count )
        throws RdbException, SQLException
    {
        List retval = new ArrayList();

        for( int i = 0; i < row_offset; i++ ){
            if( !rs.next() ){
                //throw new IndexOutOfBoundsException( "Cannot reach offset " +
                //                                     row_offset );
                return retval;
            }
        }

        for( int i = 0; i < row_count; i++ ){
            if( !rs.next() ){
                //throw new IndexOutOfBoundsException( "Cannot reach count " +
                //                                     row_count );
                return retval;
            }
            Rtbl rec;
            rec = toRtbl( rtbl, rs );
            retval.add( rec );
        }
        
        return retval;
    }

    /**
     * Convert the result set to a list of Rtbl objects
     *
     * @param rtbl_name is the table name suitable for calling
     * Rdb.getRtbl
     *
     * @param rs is the resultset from a query.  Since the fieldnames
     * in resultset are not reliable, we assume the order of the
     * columns are the same as the order returned by getFieldNameLst
     */
    public List toRtblLst( String rtbl_name, ResultSet rs,
                           int row_offset, int row_count )
        throws RdbException, SQLException
    {
        return toRtblLst( _db.getRtbl( rtbl_name ), rs,
                          row_offset, row_count );
    }

    /**
     * Returns an array of objects created from the result of the
     * select statement 
     *
     * @param vlist a 4 by n matrix that looks like this:
     * <pre>
     *      logical   not   field       compare      value
     *     operator                     operator
     * 
     *           "",  "",   "attack",   ">",         "3"
     *         "or",  "",   "cost",     "<",         "1"
     * </pre>
     *
     * The first logical operator always has to be empty
     *
     * @param olist an array of field names to order by
     */
    public RdbResultSet select( Rtbl rtbl, String[] vlist, String[] olist )
        throws RdbException, SQLException
    {
        return openRdbResultSet( rtbl, _db.toSelect( rtbl, vlist, olist ) );
    }

    /**
     * Returns an array of objects created from the result of the
     * select statement 
     *
     * @param vlist a 4 by n matrix that looks like this:
     * <pre>
     *      logical   not   field       compare      value
     *     operator                     operator
     * 
     *           "",  "",   "attack",   ">",         "3"
     *         "or",  "",   "cost",     "<",         "1"
     * </pre>
     *
     * The first logical operator always has to be empty
     *
     * @param olist an array of field names to order by
     */
    public RdbResultSet select( String rtbl_name, String[] vlist,
                                String[] olist )
        throws RdbException, SQLException
    {
        return select( _db.getRtbl( rtbl_name ), vlist, olist );
    }

    /**
     * Returns an array of objects created from the result of the
     * select statement, from given row offset and row count
     *
     * @param vlist a 4 by n matrix that looks like this:
     * <pre>
     *      logical   not   field       compare      value
     *     operator                     operator
     * 
     *           "",  "",   "attack",   ">",         "3"
     *         "or",  "",   "cost",     "<",         "1"
     * </pre>
     *
     * The first logical operator always has to be empty
     *
     * @param olist an array of field names to order by
     */
    public List select( Rtbl rtbl, String[] vlist, String[] olist,
                        int row_offset, int row_count )
        throws RdbException, SQLException
    {
        ResultSet rs;
        rs = execute( _db.toSelect( rtbl, vlist, olist ) );
        List retval;
        retval = toRtblLst( rtbl, rs, row_offset, row_count );
        
        try{
            rs.close();
        }
        catch( SQLException sqlex ){
        }
        
        try{
            rs.getStatement().close();
        }
        catch( Exception sqlex ){
            // oh wells
        }
        
        return retval;
    }

    /**
     * Returns an array of objects created from the result of the
     * select statement 
     *
     * @param vlist a 4 by n matrix that looks like this:
     * <pre>
     *      logical   not   field       compare      value
     *     operator                     operator
     * 
     *           "",  "",   "attack",   ">",         "3"
     *         "or",  "",   "cost",     "<",         "1"
     * </pre>
     *
     * The first logical operator always has to be empty
     */
    public RdbResultSet select( Rtbl rtbl, String[] vlist )
        throws RdbException, SQLException
    {
        return openRdbResultSet( rtbl, _db.toSelect( rtbl, vlist ) );
    }

    /**
     * Returns an array of objects created from the result of the
     * select statement, from given row offset and row count
     *
     * @param vlist a 4 by n matrix that looks like this:
     * <pre>
     *      logical   not   field       compare      value
     *     operator                     operator
     * 
     *           "",  "",   "attack",   ">",         "3"
     *         "or",  "",   "cost",     "<",         "1"
     * </pre>
     *
     * The first logical operator always has to be empty
     */
    public List select( Rtbl rtbl, String[] vlist,
                        int row_offset, int row_count )
        throws RdbException, SQLException
    {
        ResultSet rs;
        rs = execute( _db.toSelect( rtbl, vlist ) );
        List retval;
        retval = toRtblLst( rtbl, rs, row_offset, row_count );
        
        try{
            rs.close();
        }
        catch( SQLException sqlex ){
        }
        
        try{
            rs.getStatement().close();
        }
        catch( Exception sqlex ){
            // oh wells
        }
        
        return retval;
    }

    /**
     * Returns an array of objects created from the result of the
     * select statement 
     *
     * @param vlist a 4 by n matrix that looks like this:
     * <pre>
     *      logical   not   field       compare      value
     *     operator                     operator
     * 
     *           "",  "",   "attack",   ">",         "3"
     *         "or",  "",   "cost",     "<",         "1"
     * </pre>
     *
     * The first logical operator always has to be empty
     */
    public RdbResultSet select( String rtbl_name, String[] vlist )
        throws RdbException, SQLException
    {
        return select( _db.getRtbl( rtbl_name ), vlist );
    }

    /**
     * Returns a list of objects by the select statement with
     * equality comparison
     *
     * @param vlist a 2 by n matrix that looks like this:
     * <pre>
     *  field         value
     *             
     * 
     *  "attack",     "3"
     *  "cost",       "1"
     * </pre>
     *
     * The first logical operator always has to be empty
     */
    public RdbResultSet selectEq( Rtbl rtbl, String[] vlist )
        throws RdbException, SQLException
    {
        return openRdbResultSet( rtbl, _db.toSelectEq( rtbl, vlist ) );
    }

    /**
     * Returns a list of objects by the select statement with
     * equality comparison
     *
     * @param vlist a 2 by n matrix that looks like this:
     * <pre>
     *  field         value
     *             
     * 
     *  "attack",     "3"
     *  "cost",       "1"
     * </pre>
     *
     * The first logical operator always has to be empty
     */
    public RdbResultSet selectEq( String rtbl_name, String[] vlist )
        throws RdbException, SQLException
    {
        return selectEq( _db.getRtbl( rtbl_name ), vlist );
    }

    /**
     * Selects by a field named oid
     *
     * @return one object if oid exists
     * @return null if oid does not exist
     */
    public Rtbl select( Rtbl rtbl, int oid )
        throws RdbException, SQLException
    {
        RdbResultSet rs;
        rs = selectEq( rtbl, new String[] {
            "oid", String.valueOf( oid )
        } );

        if( rs.next() ){
            Rtbl retval = rs.get();
            rs.close();
            return retval;
        }
        return null;
    }

    /**
     * Selects by a field named oid
     *
     * @return one object if oid exists
     * @return null if oid does not exist
     */
    public Rtbl select( String rtbl_name, int oid )
        throws RdbException, SQLException
    {
        return select( _db.getRtbl( rtbl_name ), oid );
    }

    /**
     * Select all records from table
     */
    public RdbResultSet selectAll( Rtbl rtbl )
        throws RdbException, SQLException
    {
        return openRdbResultSet( rtbl, _db.toSelectAll( rtbl ) );
    }

    /**
     * Select all records from table
     */
    public RdbResultSet selectAll( String rtbl_name )
        throws RdbException, SQLException
    {
        return selectAll( _db.getRtbl( rtbl_name ) );
    }

    /**
     * Delete by matrix!
     * @param vlist a 4 by n matrix that looks like this:
     * <pre>
     *      logical   not   field       compare      value
     *     operator                     operator
     * 
     *           "",  "",   "attack",   ">",         "3"
     *         "or",  "",   "cost",     "<",         "1"
     * </pre>
     *
     * The first logical operator always has to be empty
     */
    public void delete( Rtbl rec, Object vlist[] )
        throws RdbException, SQLException
    {
        String sql;
        sql = _db.toDelete( rec, vlist );

        execute( sql );
    }

    /**
     * Delete by matrix!
     * @param vlist a 2 by n matrix that looks like this:
     * <pre>
     *      field         value
     *                 
     * 
     *      "attack",     "3"
     *      "cost",       "1"
     * </pre>
     */
    public void deleteEq( Rtbl rec, Object vlist[] )
        throws RdbException, SQLException
    {
        String sql;
        sql = _db.toDeleteEq( rec, vlist );

        execute( sql );
    }

    /**
     * Deletes a record that has the same value for the primary key
     * as the given object
     */
    public void delete( Rtbl rec )
        throws RdbException, SQLException
    {
        String sql;
        sql = _db.toDelete( rec );

        execute( sql );
    }

    /**
     * Updates a given field matrix and where clause matrix
     *
     * @param ulist a 2 by n matrix of 
     * <pre>
     * field    value
     * field    value
     * ...
     * </pre>
     * 
     * @param vlist a 2 by n matrix of
     * <pre>
     * field    value
     * field    value
     * ...
     * </pre>
     */
    public void updateEq( Rtbl rec, Object ulist[], Object vlist[] )
        throws RdbException, SQLException
    {
        String sql = _db.toUpdateEq( rec, ulist, vlist );
        execute( sql );
    }

    /**
     * Creates an update statement using a matrix
     * @param ulist a 2 by n matrix of 
     * <pre>
     * field    value
     * field    value
     * ...
     * </pre>
     * 
     * @param toWhere for the structure of the vlist matrix
     */
    public void update( Rtbl rec, Object ulist[], Object vlist[] )
        throws RdbException, SQLException
    {
        String sql = _db.toUpdate( rec, ulist, vlist );
        execute( sql );
    }

    /**
     * Updates from the given record, update all columns using the
     * values in the record except the primary key, which is used
     * to locate the record to update
     */
    public void update( Rtbl rec )
        throws RdbException, SQLException
    {
        String sql;
        sql = _db.toUpdate( rec );
        execute( sql );
    }

    /**
     * Returns an unused ID for a given table
     */
    public int createId( String table )
        throws RdbException, SQLException
    {
        return _db.createId( this, table );
    }
}
