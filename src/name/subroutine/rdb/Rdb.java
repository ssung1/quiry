package name.subroutine.rdb;

import java.lang.reflect.*;
import java.util.*;
import java.sql.*;
import java.text.*;

import name.subroutine.etable.*;
import name.subroutine.util.*;

/**
 * This is the Relational Database class that works on the Rtbl
 * classes.
 *
 * <pre>
 *
 * Organization of the methods:


   There are three versions of attribute finders.  

   One of thim is by String, recognized by this parameter list:

       getXXXX( String rtbl_name, String fieldname )

   One of them is by Rtbl, recognized by this parameter list:

       getXXXX( Rtbl rtbl, String fieldname )

   One of them by Class, recognized by this parameter list:

       getXXXX( Class c, String fieldname )

   The by-string version is only meant as a short cut to the by-rtbl
   version.  It must first to an internal lookup by calling getRtbl
   and then call the corresponding by-rtbl function.  The by-string
   version does not apply to functions that return field values.

   The by-class version does not exist for some attributes such as
   field size.  In cases where the by-class version exists, the 
   by-rtbl version must be based on the by-class version:

       getXXXX( Rtbl rtbl. String fieldname )
       {
           return getXXXX( rtbl.getClass(), fieldname );
       }


   The basic "field finder" is _getField and _getFieldIgnoreCase.
   _getField MUST BE CASE-SENSITIVE and _getFieldIgnoreCase MUST BE
   CASE-INSENSITIVE.


   The next level is the getField, which can be EITHER case-sensitive
   or case-insensitive, depending on the underlying database.  This
   method should be frequently overridden by child classes.

       _getField               -------------.
                             choose one     }---->   getField
       _getFieldIgnoreCase     -------------'


   That takes care of the case-sensitivity of databases.  All other
   property finders should base themselves on the getField method.

                 getSize 
 
   getField      getType
 
                 getValue
                  
   Please remember that by-rtbl functions still need to base
   themselves on the by-class functions if possible.  Do not do
   something like this:

       getType( Rtbl rtbl, String fieldname )
       {
           return getField( rtbl.getClass(), fieldname ).getType();
       }

       getType( Class c, String fieldname )
       {
           return getField( c, fieldname ).getType();
       }

   Do this instead:

       getType( Rtbl rtbl, String fieldname )
       {
           return getType( rtbl.getClass(), fieldname );
       }

       getType( Class c, String fieldname )
       {
           return getField( c, fieldname ).getType();
       }

   However, if the by-class version of the method is impractical, base
   the method on the getField( Rtbl ) method:

       getValue( Rtbl rtbl, String fieldname )
       {
           java.lang.reflect.Field f;
           f = getField( rtbl, fieldname );
           f.setAccessible( true );
           return f.get( rtbl );
       }

   Not:

       getValue( Rtbl rtbl, String fieldname )
       {
           java.lang.reflect.Field f;
           f = getField( rtbl.getClass(), fieldname );
           f.setAccessible( true );
           return f.get( rtbl );
       }


   About Select statements -- Level of complexity/customization:

   SELECT [field] [WHERE] [ORDER BY]          user controls all

   SELECT [WHERE] [ORDER BY]                  select all fields

   SELECT [WHERE]= [ORDER BY]                 select all fields with
                                              equality comparison only

   SELECT [WHERE]                             select some records without
                                              specific order

   SELECT [WHERE =]                           select some records using
                                              equality comparison only

   SELECT                                     select everything


   About Delete Statements

   DELETE [WHERE]
  
   DELETE [WHERE =]


   About Update Statements

   UPDATE [field] [WHERE]

   UPDATE [field] [WHERE =]

 * </pre>
 */
public abstract class Rdb
{
    String _driver;
    String _url;
    String _user;
    String _password;

    Recycler _recycler;

    Object[] _rtbl_lst;
    Map _rtbl_map;

    Connection _conn = null;

    int _max_pool_size = 10;
    Set _free_conn_set = new HashSet();
    Set _used_conn_set = new HashSet();

    /**
     * @param relation_array
     * The format of the relation array is as follows:
     *
     * <pre>
     * table1      relation            reference-field   table 2
     * table1      relation            reference-field   table 2
     * table1      relation            reference-field   table 2
     * .
     * .
     * .
     *
     * where
     *
     *     table1 is the trainer
     *    
     *     table2 is the pokemon
     *
     *     relation is either "1to*" or "*to1"
     *
     *     reference-field is the field that references the OID in the
     *     other table.
     *
     * </pre>
     *
     * Now order is not important (yay) (Dec, 2002)
     * 
     * This list will be processed and topologically sorted.  There is
     * a reason to study computer science after all.
     *
     *
     * @param rtbl_lst is a list of
     *
     * table_name                      object
     * table_name                      object
     * table_name                      object
     * ...
     *
     * (for example)
     * "cycle",                        new Cycle(),
     * "filegroup",                    new FileGroup(),
     * "dbfile",                       new DbFile(),
     * "filecall",                     new FileCall(),
     *
     * do not include "highlowkeys"
     *
     * @param root_array is just an array of directly accessible
     * tables
     *
     */
    public void init( Object[] rtbl_lst,
                      String[] root_array,
                      String[] relation_array,
                      String driver,
                      String url,
                      String user,
                      String password )
    {
        _recycler = new Recycler( this, root_array, relation_array );

        _rtbl_lst = rtbl_lst;

        _rtbl_map = new HashMap();

        for( int i = 0; i < _rtbl_lst.length; i += 2 ){
            _rtbl_map.put( _rtbl_lst[i], _rtbl_lst[i + 1] );
        }

        _driver = driver; _url = url;
        _user = user; _password = password;
    }

    public RdbSession getSession()
        throws RdbException, SQLException
    {
        Connection conn;
        SQLException last_ex = null;
        try{
            Class.forName( _driver );
        }
        catch( ClassNotFoundException ex ){
            throw new RdbException( "Cannot load class " + _driver, ex );
        }
        for( int i = 0; i < 3; ++i ){
            try{
                conn = DriverManager.getConnection( _url, _user, _password );
                return new RdbSession( this, conn );
            }
            catch( SQLException ex ){
                last_ex = ex;
                try{
                    Thread.sleep( 500 );
                }
                catch( Exception do_not_wake ){
                }
            }
        }
        if( last_ex != null ){
            throw last_ex;
        }
        return null;
    }

    public void setDriver( String driver )
    {
        _driver = driver;
    }

    public String getDriver()
    {
        return _driver;
    }

    public void setUrl( String url )
    {
        _url = url;
    }

    public String getUrl()
    {
        return _url;
    }

    public void setUser( String user )
    {
        _user = user;
    }
    public void setPassword( String password )
    {
        _password = password;
    }

    /**
     * Creates a new connection from the stored information
     */
    public Connection newConnection()
        throws ClassNotFoundException, SQLException
    {
        Class.forName( _driver );
        return DriverManager.getConnection( _url, _user, _password );
    }

    /**
     * Gets a brand new connection created by the information stored
     * in this class
     *
     * User is responsible for closing this connection later
     */
    public Connection getNewConn()
        throws ClassNotFoundException, SQLException
    {
        Class.forName( _driver );
        return DriverManager.getConnection( _url, _user, _password );
    }

    /**
     * Returns maximum allowable number of connections, free or
     * otherwise
     */
    public int getMaxPoolSize()
    {
        return _max_pool_size;
    }

    /**
     * Returns maximum allowable number of connections, free or
     * otherwise
     */
    public void setMaxPoolSize( int val )
    {
        _max_pool_size = val;
    }

    /**
     * Returns the number of connections currently in use
     */
    public int getUsedConnCnt()
    {
        return _used_conn_set.size();
    }

    /**
     * Returns the number of recycled connections
     */
    int getFreeConnCnt()
    {
        return _free_conn_set.size();
    }

    /**
     * Returns the number of connections currently available
     */
    public int getAvailableConnCnt()
    {
        return getMaxPoolSize() - getUsedConnCnt();
    }

    /**
     * Puts the given connection from the "free" pile to the "used"
     * pile
     */
    synchronized void acquireConn( Connection conn )
    {
        _free_conn_set.remove( conn );
        _used_conn_set.add( conn );
    }

    /**
     * Puts the given connection from the "used" pile to the "free"
     * pile
     */
    synchronized void releaseConn( Connection conn )
    {
        _used_conn_set.remove( conn );
        _free_conn_set.add( conn );
    }

    /**
     * Returns a connection, from a pool of available connections.  If
     * the pool has no available connections, the method will create
     * one if the maximum has not been reached
     */
    public Connection getPooledConn()
        throws ClassNotFoundException, SQLException,
               MaximumPoolSizeReachedException
    {
        // if we have at least one free
        if( getFreeConnCnt() > 0 ){
            // get the first one
            Iterator it;
            it = _free_conn_set.iterator();
            Connection conn = (Connection)it.next();
            acquireConn( conn );
            return conn;
        }
        // check to see if we have exceeded limit.  If not --
        // we just create a new one
        if( getUsedConnCnt() < getMaxPoolSize() ){
            Connection conn;
            conn = newConnection();
            synchronized( _free_conn_set ){
                _free_conn_set.add( conn );
            }
            return conn;
        }
        throw new MaximumPoolSizeReachedException( String.valueOf(
                                                       getMaxPoolSize() ) );
    }

    /**
     * Return default table name.
     */
    public String getTableName( Rtbl table )
    {
        return table.name();
    }

    /**
     * Return default table name.
     */
    public String getTableName( String rtbl_name )
    {
        return getTableName( getRtbl( rtbl_name ) );
    }

    /**
     * Returns a string that would be interpreted as a quote
     * if it is already surrounded by quotes
     */
    public String literalQuote()
    {
        return "\\'";
    }


    //
    // field finding/listing methods
    //

    /**
     * Returns a list of all the fields
     */
    public List getFieldLst( Rtbl table )
    {
        Set history = new HashSet();
        java.lang.reflect.Field[] f;

        List finallist = new ArrayList();
        List list = new ArrayList();

        Class t;
        t = table.getClass();

        while( t != null && !t.equals( Object.class ) ){
            f = t.getDeclaredFields();
            list.clear();
            for( int i = 0; i < f.length; i++ ){
                if( f[i].getName().startsWith( "__" )
                    && !history.contains( f[i].getName() ) ){
                    list.add( f[i] );
                    history.add( f[i].getName() );
                }
            }
            finallist.addAll( 0, list );
            t = t.getSuperclass();
        }

        return finallist;
    }

    public List getFieldLst( String rtbl_name )
    {
        return getFieldLst( getRtbl( rtbl_name ) );
    }

    /**
     * Returns a list of fields for the given Rtbl object
     */
    public List getFieldNameLst( Rtbl table )
    {
        List list = getFieldLst( table );

        List namelist = new ArrayList();

        for( Iterator it = list.iterator(); it.hasNext(); ){
            String name;
            name = ((java.lang.reflect.Field)it.next()).getName();

            namelist.add( name.substring( 2 ) );
        }

        return namelist;
    }

    /**
     * Returns a list of fields for the given Rtbl name
     */
    public List getFieldNameLst( String rtbl_name )
    {
        return getFieldNameLst( getRtbl( rtbl_name ) );
    }

    /**
     * Returns an array of fields for the given Rtbl object
     */
    public String[] getFieldNameArray( Rtbl table )
    {
        return (String[])(getFieldNameLst( table ).toArray( new String[0] ));
    }

    /**
     * Returns a list of fields for the given Rtbl name
     */
    public String[] getFieldNameArray( String rtbl_name )
    {
        return getFieldNameArray( getRtbl( rtbl_name ) );
    }

    /**
     * Retrieves a field object from t
     *
     * This is used because it searches for superclasses
     * 
     * For some reason, Java API only searches for superclasses if
     * it returns only public fields
     */
    protected java.lang.reflect.Field _getField( Class t, String field_ )
        throws RdbException
    {
        String field = "__" + field_;

        java.lang.reflect.Field f;
        while( t != null && !t.equals( Object.class ) ){
            try{
                f = t.getDeclaredField( field );
                return f;
            }
            catch( NoSuchFieldException nofield ){
                t = t.getSuperclass();
            }
        }

        throw new RdbException( "Cannot find Class: " + t +
                                " Field: " + field_ );
    }

    /**
     * Retrieves a field object from t
     *
     * This is used because it searches for superclasses
     * 
     * For some reason, Java API only searches for superclasses if
     * it returns only public fields
     */
    protected java.lang.reflect.Field _getFieldIgnoreCase( Class t,
                                                           String field_ )
        throws RdbException
    {
        String field = "__" + field_;

        java.lang.reflect.Field[] f;

        f = t.getDeclaredFields();

        for( int i = 0; i < f.length; i++ ){
            if( f[i].getName().equalsIgnoreCase( field ) ){
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
     * Returns a field object by name.  Can be case-sensitive or case-
     * insensitive, depending on the underlying database definition.
     */
    public java.lang.reflect.Field getField( Class c, String field )
        throws RdbException
    {
        return _getFieldIgnoreCase( c, field );
    }

    /**
     * Returns a field object by name.  Can be case-sensitive or case-
     * insensitive, depending on the underlying database definition.
     */
    public java.lang.reflect.Field getField( Rtbl table, String field )
        throws RdbException
    {
        return getField( table.getClass(), field );
    }

    /**
     * Returns a field object by name.  Can be case-sensitive or case-
     * insensitive, depending on the underlying database definition.
     */
    public java.lang.reflect.Field getField( String rtbl_name, String field )
        throws RdbException
    {
        return getField( getRtbl( rtbl_name ), field );
    }

    /**
     * Returns a java.lang.Class object that represents the class
     * of the given fieldname (field_)
     *
     * Note that the Class object can signify an array, in which
     * case the programmer needs to process it more.
     */
    public Class getType( Class t, String field_ )
        throws RdbException
    {
        java.lang.reflect.Field f;
        f = getField( t, field_ );

        return f.getType();
    }

    /**
     * Returns a java.lang.Class object that represents the class
     * of the given fieldname (field_)
     *
     * Note that the Class object can signify an array, in which
     * case the programmer needs to process it more.
     */
    public Class getType( Rtbl rtbl, String field )
        throws RdbException
    {
        return getType( rtbl.getClass(), field );
    }

    /**
     * Returns a java.lang.Class object that represents the class
     * of the given fieldname (field_)
     *
     * Note that the Class object can signify an array, in which
     * case the programmer needs to process it more.
     */
    public Class getType( String rtbl_name, String field )
        throws RdbException
    {
        return getType( getRtbl( rtbl_name ), field );
    }

    /**
     * Returns a RdbClass object representing the class of the
     * given field.
     */
    public RdbClass getRdbClass( Class t, String field )
        throws RdbException
    {
        return new RdbClass( getType( t, field ) );
    }

    /**
     * Returns a java.lang.Class object that represents the class
     * of the given fieldname (field_)
     *
     * Note that the Class object can signify an array, in which
     * case the programmer needs to process it more.
     */
    public RdbClass getRdbClass( Rtbl rtbl, String field )
        throws RdbException
    {
        return getRdbClass( rtbl.getClass(), field );
    }

    /**
     * Returns a java.lang.Class object that represents the class
     * of the given fieldname (field_)
     *
     * Note that the Class object can signify an array, in which
     * case the programmer needs to process it more.
     */
    public RdbClass getRdbClass( String rtbl_name, String field )
        throws RdbException
    {
        return getRdbClass( getRtbl( rtbl_name ), field );
    }

    /**
     * Returns the type in SQL format
     */
    public String getTypeAsSQLString( Rtbl rtbl, String field )
        throws RdbException
    {
        RdbClass rdbc;
        rdbc = getRdbClass( rtbl, field );

        Class ftype;
        ftype = rdbc.getType();
        
        // this is maps Java types to SQL types
        Object[] tmap = {
            double.class,         float.class,
            java.util.Date.class, int.class,
        };

        for( int i = 0; i < tmap.length; i += 2 ){
            if( ftype.equals( tmap[i] ) ){
                ftype = (Class)tmap[i + 1];
                break;
            }
        }

        String type_str;
        int period;

        type_str = ftype.toString();

        period = type_str.lastIndexOf( '.' );
        if( period >= 0 ){
            type_str = type_str.substring( period + 1 );
        }

        int size;
        size = getSize( rtbl, field );

        if( size > 0 ){
            type_str += "(" + size + ")";
        }

        return type_str;
    }

    /**
     * Returns the type in SQL format
     */
    public String getTypeAsSQLString( String rtbl_name, String field )
        throws RdbException
    {
        return getTypeAsSQLString( getRtbl( rtbl_name ), field );
    }

    /**
     * Returns the value of the given field of the given rtbl
     */
    public Object getValue( Rtbl rtbl, String field )
        throws RdbException
    {
        java.lang.reflect.Field f;
        f = getField( rtbl, field );

        f.setAccessible( true );
        try{
            return f.get( rtbl );
        }
        catch( IllegalAccessException ex ){
            throw new RdbException( "Cannot access " + field +
                                    " in " + rtbl.getClass(), ex );
        }
    }

    /**
     * Returns the size of the given field of the given rtbl if
     * applicable.  The size only exists if the field type is an
     * array type.
     *
     * @return -1 if size cannot be determined
     */
    public int getSize( Rtbl rtbl, String field )
        throws RdbException
    {
        Class c;
        c = getType( rtbl, field );

        if( c.isArray() && c.getComponentType().equals( char.class ) ){
            char[] v;
            v = (char[])getValue( rtbl, field );

            return v.length;
        }
        
        // no clue what the size is
        return -1;
    }


    //
    // Field conversion methods: accessors
    // 


    /**
     * Returns a string representation of the value in the format
     * acceptable by the associated database server
     *
     * @param rtbl must be a valid relational table object
     * @param obj_fname must be a valid field name (without double
     *        underscore)
     * @param obj_val is the value which can be null
     */
    public String toLiteral( Rtbl rtbl, Object obj_fname, Object obj_val )
        throws RdbException
    {
        // get prototype to make sure the size of each field is as
        // defined
        Rtbl proto;
        try{
            proto = (Rtbl)rtbl.getClass().newInstance();
        }
        catch( IllegalAccessException ex ){
            throw new RdbException( "Cannot get fresh copy of " +
                                    rtbl.getClass(), ex );
        }
        catch( InstantiationException ex ){
            throw new RdbException( "Cannot get fresh copy of " +
                                    rtbl.getClass(), ex );
        }

        // field name should be converted to string
        String fname = String.valueOf( obj_fname );

        // type of the source (obj_val)
        Class src_c;
        if( obj_val != null ){
            src_c = obj_val.getClass();
        }
        else{
            src_c = null;
        }

        // type of the destination
        Class dst_c = getType( proto, fname );

        // for simplicity, we convert source into a string first
        // String.valueOf won't convert char[] correctly unless it is
        // explicitily converting char[]
        String val;
        if( src_c == null ){
            val = null;
        }
        else if( src_c.isArray() && src_c.getComponentType() == char.class ){
            val = String.valueOf( (char[])obj_val );
        }
        else{
            val = String.valueOf( obj_val );
        }
        
        // destination is character array, which is really how
        // database "strings" are.
        if( dst_c.isArray() && dst_c.getComponentType() == char.class ){
            if( obj_val == null ){
                return "''";
            }

            // get the source as char array also
            char source[];
            if( src_c.isArray() && src_c.getComponentType() == char.class ){
                source = (char[])obj_val;
            }
            else{
                source = String.valueOf( obj_val ).toCharArray();
            }

            // now we choose the shorter of the two arrays
            int min;
            min = Math.min( source.length, getSize( proto, fname ) );

            // make a new string based on source, encoding characters
            // as necessary
            StringBuffer retval = new StringBuffer();
            for( int i = 0; i < min; i++ ){
                char chr = source[i];
                if( chr == '\'' ){
                    retval.append( literalQuote() );
                }
                else if( chr == (char)0 ){
                    continue;
                }
                else{
                    retval.append( chr );
                }
            }

            return Variant.grow( retval.toString(), "'" );
        }
        else if( dst_c.equals( int.class ) ||
                 dst_c.equals( long.class ) ){
            if( obj_val == null ){
                return "0";
            }
            val = val.trim();
            if( val.length() <= 0 ){
                return "0";
            }
            return val;
        }
        else if( dst_c.equals( double.class ) ||
                 dst_c.equals( float.class ) ){
            if( obj_val == null ){
                return "0";
            }
            val = val.trim();
            if( val.length() <= 0 ){
                return "0";
            }
            return val;
        }
        else if( dst_c.equals( java.util.Date.class ) ){
            // date should be represented as integer
            if( obj_val == null ){
                return "0";
            }

            DateFormat df;
            df = new SimpleDateFormat( "yyyyMMdd" );

            if( src_c == java.util.Date.class ){
                return df.format( (java.util.Date)obj_val );
            }

            try{
                return df.format( Variant.toDate( val ) );
            }
            catch( ParseException ex ){
                throw new RdbException( "Cannot parse as date " + val, ex );
            }
        }
        else if( dst_c.equals( char.class ) ){
            if( obj_val == null ){
                return "''";
            }
            StringBuffer retval = new StringBuffer();
            char chr;
            chr = val.charAt( 0 );
            if( chr == '\'' ){
                retval.append( literalQuote() );
            }
            else{
                retval.append( chr );
            }
            return Variant.grow( retval.toString(), "'" );
        }
        else{
            return val;
        }
    }

    /**
     * Returns a property of the given Rtbl as a String suitable
     * for use in an SQL statement
     */
    public String toLiteral( Rtbl table, Object obj_fname )
        throws RdbException
    {
        String fname = String.valueOf( obj_fname );
        return toLiteral( table, fname, getValue( table, fname ) );
    }


    //
    // Field Conversion Methods: mutators
    //

    /**
     * Generic method to edit a table
     */
    public void setValue( Rtbl retval, String fname, Object value )
        throws RdbException
    {
        Class cl = retval.getClass();

        // use a prototype so that our field length
        // is surely to be correct
        Rtbl proto;
        try{
            proto = (Rtbl)cl.newInstance();
        }
        catch( IllegalAccessException ex ){
            throw new RdbException( "Cannot get fresh copy of " + cl, ex );
        }        
        catch( InstantiationException ex ){
            throw new RdbException( "Cannot get fresh copy of " + cl, ex );
        }
        
        // here goes...big messy chunk of ifs
        // only because we can't use "Object"
        // ( primitive types are annoying :( )
        try{
            java.lang.reflect.Field field = getField( proto, fname );
            field.setAccessible( true );
            Class type = field.getType();
            boolean is_array;
            if( type.isArray() ){
                is_array = true;
                type = type.getComponentType();
            }
            else{
                is_array = false;
            }
            
            if( is_array && type.equals( char.class ) ){
                setCharArray( retval, field, value );
                return;
            }
            else if( type.equals( int.class ) ){
                setInt( retval, field, value );
                return;
            }
            else if( type.equals( double.class ) ){
                setDouble( retval, field, value );
                return;
            }
            else if( type.equals( float.class ) ){
                setFloat( retval, field, value );
                return;
            }
            else if( type.equals( java.util.Date.class ) ){
                setDate( retval, field, value );
                return;
            }
            else if( !is_array && type.equals( char.class ) ){
                setChar( retval, field, value );
                return;
            }
            else{
                field.set( retval, value );
                return;
            }
        } // end try
        catch( NullPointerException ex ){
            //System.err.println( ex );
        } // end catch
        catch( Exception ex ){
        }
    }

    /**
     * Sets the given field in rtbl to the given value
     *
     * @param f must be an integer field
     */
    void setInt( Rtbl rtbl, java.lang.reflect.Field f, Object val )
        throws RdbException
    {
        if( val == null ){
            try{
                f.setInt( rtbl, 0 );
            }
            catch( IllegalAccessException ex ){
                throw new RdbException( "Cannot set integer in " +
                                        rtbl.getClass() + " field " +
                                        f, ex );
            }
            return;
        }
        if( val.getClass() == Integer.class ){
            try{
                f.setInt( rtbl, ((Integer)val).intValue() );
            }
            catch( IllegalAccessException ex ){
                throw new RdbException( "Cannot set integer in " +
                                        rtbl.getClass() + " field " +
                                        f, ex );
            }
            return;
        }
        int v = Integer.parseInt( String.valueOf( val ).trim(), 10 );
        try{
            f.setInt( rtbl, v );
        }
        catch( IllegalAccessException ex ){
            throw new RdbException( "Cannot set integer in " +
                                    rtbl.getClass() + " field " +
                                    f, ex );
        }
    }

    /**
     * Sets the given field in rtbl to the given value
     *
     * @param f must be a double field
     */
    void setDouble( Rtbl rtbl, java.lang.reflect.Field f, Object val )
        throws RdbException
    {
        if( val == null ){
            try{
                f.setDouble( rtbl, 0 );
            }
            catch( IllegalAccessException ex ){
                throw new RdbException( "Cannot set double in " +
                                        rtbl.getClass() + " field " +
                                        f, ex );
            }            
            return;
        }
        if( val.getClass() == Double.class ){
            try{
                f.setDouble( rtbl, ((Double)val).intValue() );
            }
            catch( IllegalAccessException ex ){
                throw new RdbException( "Cannot set double in " +
                                        rtbl.getClass() + " field " +
                                        f, ex );
            }                        
            return;
        }
        double v = Double.parseDouble( String.valueOf( val ).trim() );
        try{
            f.setDouble( rtbl, v );
        }
        catch( IllegalAccessException ex ){
            throw new RdbException( "Cannot set double in " +
                                    rtbl.getClass() + " field " +
                                    f, ex );
        }
    }

    /**
     * Sets the given field in rtbl to the given value
     *
     * @param f must be a float field
     */
    void setFloat( Rtbl rtbl, java.lang.reflect.Field f, Object val )
        throws RdbException
    {
        if( val == null ){
            try{
                f.setFloat( rtbl, 0 );
            }
            catch( IllegalAccessException ex ){
                throw new RdbException( "Cannot set float in " +
                                        rtbl.getClass() + " field " +
                                        f, ex );
            }
            return;
        }
        if( val.getClass() == Float.class ){
            try{
                f.setFloat( rtbl, ((Float)val).intValue() );
            }
            catch( IllegalAccessException ex ){
                throw new RdbException( "Cannot set float in " +
                                        rtbl.getClass() + " field " +
                                        f, ex );
            }            
            return;
        }
        float v = Float.parseFloat( String.valueOf( val ).trim() );
        try{
            f.setFloat( rtbl, v );
        }
        catch( IllegalAccessException ex ){
            throw new RdbException( "Cannot set float in " +
                                    rtbl.getClass() + " field " +
                                    f, ex );
        }
    }

    /**
     * Sets the given field in rtbl to the given value
     *
     * @param f must be a Date field
     */
    void setDate( Rtbl rtbl, java.lang.reflect.Field f, Object val )
        throws RdbException
    {
        if( val == null ){
            try{
                f.set( rtbl, val );
            }
            catch( IllegalAccessException ex ){
                throw new RdbException( "Cannot set date in " +
                                        rtbl.getClass() + " field " +
                                        f, ex );
            }
            return;
        }
        if( val.getClass() == java.util.Date.class ){
            try{
                f.set( rtbl, val );
            }
            catch( IllegalAccessException ex ){
                throw new RdbException( "Cannot set date in " +
                                        rtbl.getClass() + " field " +
                                        f, ex );
            }
            return;
        }
        
        java.util.Date v;
        try{
            v = toDate( String.valueOf( val ).trim() );
        }
        catch( ParseException ex ){
            throw new RdbException( "Cannot parse as date " + 
                                    String.valueOf( val ), ex );
        }

        try{
            f.set( rtbl, v );
        }
        catch( IllegalAccessException ex ){
            throw new RdbException( "Cannot set date in " +
                                    rtbl.getClass() + " field " +
                                    f, ex );
        }
    }

    /**
     * Sets the given field in rtbl to the given value
     *
     * @param f must be a character array field
     */
    void setCharArray( Rtbl rtbl, java.lang.reflect.Field f, Object val )
        throws RdbException
    {
        if( val == null ){
            try{
                f.set( rtbl, val );
            }
            catch( IllegalAccessException ex ){
                throw new RdbException( "Cannot set char[] in " +
                                        rtbl.getClass() + " field " +
                                        f, ex );
            }
            return;
        }
        if( val.getClass().isArray() &&
            val.getClass().getComponentType() == char.class ){
            try{
                f.set( rtbl, val );
            }
            catch( IllegalAccessException ex ){
                throw new RdbException( "Cannot set char[] in " +
                                        rtbl.getClass() + " field " +
                                        f, ex );
            }
            return;
        }
        
        char[] v = String.valueOf( val ).trim().toCharArray();
        try{
            f.set( rtbl, v );
        }
        catch( IllegalAccessException ex ){
            throw new RdbException( "Cannot set char[] in " +
                                    rtbl.getClass() + " field " +
                                    f, ex );
        }
    }

    /**
     * Sets the given field in rtbl to the given value
     *
     * @param f must be an char field
     */
    void setChar( Rtbl rtbl, java.lang.reflect.Field f, Object val )
        throws RdbException
    {
        if( val == null ){
            try{
                f.setChar( rtbl, (char)0 );
            }
            catch( IllegalAccessException ex ){
                throw new RdbException( "Cannot set char in " +
                                        rtbl.getClass() + " field " +
                                        f, ex );
            }
            return;
        }
        if( val.getClass() == Character.class ){
            try{
                f.setChar( rtbl, ((Character)val).charValue() );
            }
            catch( IllegalAccessException ex ){
                throw new RdbException( "Cannot set char in " +
                                        rtbl.getClass() + " field " +
                                        f, ex );
            }
            return;
        }
        String v = String.valueOf( val ).trim();
        try{
            f.setChar( rtbl, v.charAt( 0 ) );
        }
        catch( IllegalAccessException ex ){
            throw new RdbException( "Cannot set char in " +
                                    rtbl.getClass() + " field " +
                                    f, ex );
        }
    }

    /**
     * @param lst a 2 by N array of values:
     * <pre>
     * Field0        Value0
     * Field1        Value1
     * ...
     * and so on
     * </pre>
     * that are to be inserted into the rtbl object
     */
    public Rtbl toRtbl( Rtbl rtbl, Object[] array )
        throws RdbException
    {
        Class cl = rtbl.getClass();
        Rtbl retval;
        try{
            retval = (Rtbl)cl.newInstance();
        }
        catch( IllegalAccessException ex ){
            throw new RdbException( "Cannot get fresh copy of " + cl, ex );
        }
        catch( InstantiationException ex ){
            throw new RdbException( "Cannot get fresh copy of " + cl, ex );
        }
        
        for( int i = 0; i < array.length; i += 2 ){
            Object key;
            Object val;

            key = array[i];
            try{
                val = array[i + 1];
            }
            catch( ArrayIndexOutOfBoundsException ex ){
                val = null;
            }

            setValue( retval, String.valueOf( key ), val );
        }
        return retval;
    }

    /**
     * @param lst a 2 by N array of values:
     * <pre>
     * Field0        Value0
     * Field1        Value1
     * ...
     * and so on
     * </pre>
     * that are to be inserted into the rtbl object
     */
    public Rtbl toRtbl( String rtbl_name, Object[] array )
        throws RdbException
    {
        return toRtbl( getRtbl( rtbl_name ), array );
    }

    /**
     * Converts a list (in key-value format) to a Rtbl object
     *
     * @param rtbl is only used to provide object information.  It
     * will not be altered by this method
     */
    public Rtbl toRtbl( Rtbl rtbl, List lst )
        throws RdbException
    {
        return toRtbl( rtbl, lst.toArray() );
    }

    /**
     * Converts a list (in key-value format) to a Rtbl object
     *
     * @param rtbl is only used to provide object information.  It
     * will not be altered by this method
     */
    public Rtbl toRtbl( String rtbl_name, List lst )
        throws RdbException
    {
        return toRtbl( getRtbl( rtbl_name ), lst );
    }

    /**
     * @param map a hashtable of String values
     * that are to be inserted into the rtbl object
     */
    public Rtbl toRtbl( Rtbl rtbl, Map map )
        throws RdbException
    {
        List rec = name.subroutine.util.Lists.toList( map );
        return toRtbl( rtbl, rec );
    }

    /**
     * @param map a hashtable of String values
     * that are to be inserted into the rtbl object
     */
    public Rtbl toRtbl( String rtbl_name, Map map )
        throws RdbException
    {
        return toRtbl( getRtbl( rtbl_name ), map );
    }

    //
    // SQL Table Definition Methods
    //

    /**
     * Returns an SQL statement for dropping this table
     */
    public String toDrop( Rtbl table )
    {
        return "DROP TABLE " + getTableName( table );
    }

    /**
     * Returns an SQL statement for dropping this table
     */
    public String toDrop( String rtbl_name )
    {
        return toDrop( getRtbl( rtbl_name ) );
    }

    /**
     * Returns an SQL statement for creating this table
     */
    public String toCreate( Rtbl table )
        throws RdbException
    {
        StringBuffer sql = new StringBuffer();
        String tableName = getTableName( table );

        sql.append( "CREATE TABLE " ).append( tableName ).append( "( " );
        
        Rtbl rtbl;
        // make a fresh copy just in case
        try{
            rtbl = (Rtbl)table.getClass().newInstance();
        }
        catch( IllegalAccessException ex ){
            throw new RdbException( "Cannot get fresh copy of " +
                                    table.getClass(), ex );
        }
        catch( InstantiationException ex ){
            throw new RdbException( "Cannot get fresh copy of " +
                                    table.getClass(), ex );
        }

        
        List flist;
        flist = getFieldNameLst( rtbl );

        Iterator it;
        for( it = flist.iterator(); it.hasNext(); ){
            String f;
            f = (String)it.next();

            sql.append( f ).append( " " );
            sql.append( getTypeAsSQLString( rtbl, f ) );

            if( table.isPrimaryKey( f ) ){
                sql.append( " NOT NULL" );
            }

            if( it.hasNext() ){
                sql.append( ", " );
            }
        }

        // primary key clause
        //
        String[] primary_key = table.primaryKey();
        if( primary_key.length > 0 ){
            sql.append( ", PRIMARY KEY (" );
            sql.append( Variant.join( primary_key, ", " ) );
            sql.append( ")" );
        }

        sql.append( " )" );
        return sql.toString();
    }

    /**
     * Returns an SQL statement for creating this table
     */
    public String toCreate( String rtbl_name )
        throws RdbException
    {
        return toCreate( getRtbl( rtbl_name ) );
    }

    /**
     * Creates an index by number
     */
    private String _toIndex( Rtbl table, String[] fields,
                             int number, String directive )
    {
        StringBuffer sql = new StringBuffer();

        String tname = getTableName( table );
        sql.append( directive );
        sql.append( tname ).append( '_' ).append( number );
        sql.append( " ON " );
        sql.append( tname ).append( " ( " );
        sql.append( Variant.join( fields, ", " ) );
        sql.append( " )" );

        return sql.toString();
    }

    /**
     * Creates an index by number
     */
    private String _toUniqueIndex( Rtbl table, String[] fields, int number )
    {
        return _toIndex( table, fields, number, "CREATE UNIQUE INDEX " );
    }

    /**
     * Creates an index by number
     */
    private String _toIndex( Rtbl table, String[] fields, int number )
    {
        return _toIndex( table, fields, number, "CREATE INDEX " );
    }

    /**
     * creates all indices
     */
    public String[] toIndex( Rtbl table )
    {
        List list = new ArrayList();

        // unique index goes first for better performance
        String idx[][] = table.uniqueIndexLst();
        int cnt;
        
        cnt = 0;

        if( idx != null ){
            for( int i = 0; i < idx.length; i++ ){
                list.add( _toUniqueIndex( table, idx[i], i ) );
            }
            cnt = idx.length;
        }

        idx = table.indexLst();

        if( idx != null ){
            for( int i = 0; i < idx.length; i++ ){
                list.add( _toIndex( table, idx[i], i + cnt ) );
            }
        }

        return (String[])list.toArray( new String[0] );
    }

    /**
     * creates all indices
     */
    public String[] toIndex( String rtbl_name )
    {
        return toIndex( getRtbl( rtbl_name ) );
    }


    //
    // SQL Record Manipulation Methods
    //


    /**
     * creates an sql insert statement
     * @param table: name of the table
     * @param columns: names of the columns
     * @param values: the values (must be quoted for non-numericals)
     */
    public static String toInsert( String table, String columns[],
                                   String values[] )
    {
        StringBuffer retval = new StringBuffer();
        retval.append( "INSERT INTO " );
        retval.append( table ).append( " " );
        retval.append( Variant.grow( Variant.join( columns, ", " ), "(" ) );
        retval.append( " VALUES " );
        retval.append( Variant.grow( Variant.join( values, ", " ), "(" ) );

        return retval.toString();
    }

    /**
     * creates an sql insert statement
     * @param table: name of the table
     * @param columns: names of the columns
     * @param values: the values (must be quoted for non-numericals)
     */
    public static String toInsert( String table, List columns,
                                   List values )
    {
        String[] col_array;
        String[] val_array;
        col_array = name.subroutine.util.Arrays.toStringArray( 
                        columns.toArray() );
        val_array = name.subroutine.util.Arrays.toStringArray(
                        values.toArray() );

        return toInsert( table, col_array, val_array );
    }

    /**
     * Create an SQL insert statement from a matrix:
     * <pre>
     * Field0       Value0
     * Field1       Value1
     * ...
     * and so on
     * </pre>
     *
     * This is slightly different from the toInsert in
     * SQLer, for this method does not query the database
     * at all for the properties of the table fields
     *
     * @param table is responsible for providing the field
     *        attributes for the table
     */
    public String toInsert( Rtbl rtbl, Object[] rec )
    {
        List col_lst = new ArrayList();
        List val_lst = new ArrayList();

        for( int i = 0; i < rec.length; i += 2 ){
            Object fld;
            fld = rec[i];

            Object val;
            try{
                val = rec[i + 1];
            }
            catch( Exception ex ){
                val = null;
            }

            try{
                val = toLiteral( rtbl, fld, val );
            }
            catch( Exception ex ){
                continue;
            }

            col_lst.add( fld );
            val_lst.add( val );
        }

        return toInsert( getTableName( rtbl ), col_lst, val_lst );
    }

    public String toInsert( Rtbl rtbl, Map rec )
    {
        Object r[];
        r = name.subroutine.util.Arrays.toArray( rec );
        return toInsert( rtbl, r );
    }

    /**
     * Create an SQL insert statement from a Record...
     *
     * This is slightly different from the toInsert in
     * SQLer, for this method does not query the database
     * at all for the properties of the rtbl fields
     *
     * @param rtbl is responsible for providing the field
     *        attributes for the rtbl
     */
    public String toInsert( Rtbl rtbl, Record rec )
    {
        Object[] matrix;
        matrix = name.subroutine.util.Arrays.toArray( rec );

        return toInsert( rtbl, matrix );
    }

    /**
     * Create an SQL insert statement ONLY from the current Record in
     * the Etable
     *
     * This is slightly different from the toInsert in SQLer, for this
     * method does not query the database at all for the properties of
     * the table fields
     *
     * @param table is responsible for providing the field attributes
     * for the table
     */
    public String toInsert( Rtbl table, Etable etable )
    {
        return toInsert( table, etable.get() );
    }    

    /**
     * Create an SQL insert statement with the values included
     * in the Rtbl object
     */
    public String toInsert( Rtbl rtbl )
        throws RdbException
    {
        List col_lst = new ArrayList();
        List val_lst = new ArrayList();

        List fnlist = getFieldNameLst( rtbl );
        
        for( int i = 0; i < fnlist.size(); i++ ){
            String fname = (String)fnlist.get( i );

            String value;
            try{
                value = toLiteral( rtbl, fname );
                col_lst.add( fname );
                val_lst.add( value );
            }
            catch( Exception ex ){
                // if anything goes wrong, don't add
            }
        }
        
        return toInsert( getTableName( rtbl ), col_lst, val_lst );
    }

    /**
     * Creates a WHERE clause using a matrix
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
    public String toWhere( Rtbl table, Object vlist[] )
        throws RdbException
    {
        StringBuffer sql = new StringBuffer();

        if( vlist != null && vlist.length > 0 ){
            sql.append( " WHERE" );

            StringBuffer buf = new StringBuffer();
            for( int i = 0; i < vlist.length; i += 5 ){
                // because the 0th element is not used
                if( i > 0 ){
                    buf.append( ' ' ).append( vlist[i] ).append( ' ' );
                }
                else{
                    buf.append( ' ' );
                }

                buf.append( '(' );
                buf.append( vlist[i + 1] ).append( ' ' );  // NOT
                // if there's a NOT (or whatever), we need parentheses
                if( String.valueOf( vlist[i + 1] ).trim().length() > 0 ){
                    buf.append( "(" );
                }
                buf.append( vlist[i + 2] ).append( ' ' );  // field name
                buf.append( vlist[i + 3] ).append( ' ' );  // >, =, <
          
                String val;
                val = toLiteral( table, vlist[i + 2], vlist[i + 4] );

                buf.append( val ).append( ' ' );  // value
                // if there's a NOT (or whatever), we need parentheses
                if( String.valueOf( vlist[i + 1] ).trim().length() > 0 ){
                    buf.append( ')' );
                }
                buf.append( ')' );
            }

            sql.append( buf );
        }
        return sql.toString();
    }

    /**
     * Creates a WHERE clause made of equality comparisons that are
     * ANDed together
     *
     * This method is created because most of the queries are of this
     * form.
     *
     * @param vlist is a 2xN matrix identifying the values desired
     */
    public String toWhereEq( Rtbl table, Object vlist[] )
        throws RdbException
    {
        StringBuffer sql = new StringBuffer();

        if( vlist != null && vlist.length > 0 ){
            sql.append( " WHERE " );

            StringBuffer buf = new StringBuffer();
            for( int i = 0; i < vlist.length; i += 2 ){
                // because the 0th element is not used
                if( i > 0 ){
                    buf.append( " AND " );
                }

                buf.append( '(' );
                buf.append( vlist[i] ).append( " = " );  // field name

                String val;
                val = toLiteral( table, vlist[i], vlist[i + 1] );

                buf.append( val );
                buf.append( ')' );
            }

            sql.append( buf );
        }
        return sql.toString();
    }

    /**
     * Creates a WHERE clause using primary key of the given record
     */
    public String toWhere( Rtbl rec )
        throws RdbException
    {
        List v = new ArrayList();

        String[] pkey = rec.primaryKey();

        for( int i = 0; i < pkey.length; i++ ){
            v.add( pkey[i] );
            v.add( getValue( rec, pkey[i] ) );
        }

        return toWhereEq( rec, v.toArray() );
    }

    /**
     * Creates an ORDER BY clause made of the given array of columns
     */
    public static String toOrderBy( String columns[] )
    {
        StringBuffer sql = new StringBuffer();
        sql.append( " ORDER BY " );
        sql.append( Variant.join( columns, ", " ) );
        return sql.toString();
    }

    /**
     * creates a select statement from a list of required columns
     */
    public static String toSelect( String table, String columns[] )
    {
        StringBuffer sql = new StringBuffer();

        sql.append( "SELECT " );
        sql.append( Variant.join( columns, ", " ) );
        sql.append( " FROM " );
        sql.append( table );

        return sql.toString();
    }
    
    /**
     * Creates a select statement to select all the fields,
     * explicitly, without using *
     *
     * This will let us know the exact fields returned and
     * the order of those fields.
     *
     * Sometimes when using metadata, we get capitalized field
     * names and so on...don't want that
     */
    public String toSelectAll( Rtbl table )
    {
        List lst;
        lst = getFieldNameLst( table );

        return toSelect( getTableName( table ),
                         (String[])lst.toArray( new String[0] ) );
    }

    /**
     * Creates an SQL statement that selects from the table that
     * matches the equality search.  The resulting statement can be
     * used to select all records that have field values exactly
     * matching the desired value.
     *
     * @param vlist a 2 by n matrix that looks like this:
     * <pre>
     *          field0         value0
     *          field1         value1
     *          ...
     * </pre>
     *
     */
    public String toSelectEq( Rtbl table, String[] sel )
        throws RdbException
    {
        StringBuffer sql = new StringBuffer();

        sql.append( toSelectAll( table ) );
        sql.append( toWhereEq( table, sel ) );
        
        return sql.toString();
    }

    /**
     * Creates an SQL statement that selects from the table
     * that matches the search
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
     */
    public String toSelect( Rtbl table, String[] vlist )
        throws RdbException
    {
        StringBuffer sql = new StringBuffer();
        sql.append( toSelectAll( table ) );
        sql.append( toWhere( table, vlist ) );

        return sql.toString();
    }

    /**
     * Returns a SELECT statement
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
    public String toSelect( Rtbl table, String[] vlist, String[] olist )
        throws RdbException
    {
        StringBuffer sql = new StringBuffer();

        sql.append( toSelect( table, vlist ) );
        sql.append( toOrderBy( olist ) );

        return sql.toString();
    }

    /**
     * Returns a SELECT statement
     *
     * @param flist a list of fields to select
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
    public String toSelect( Rtbl table, String[] flist,
                            String[] vlist, String[] olist )
        throws RdbException
    {
        StringBuffer sql = new StringBuffer();

        sql.append( toSelect( getTableName( table ), flist ) );
        sql.append( toWhere( table, vlist ) );
        sql.append( toOrderBy( olist ) );

        return sql.toString();
    }

    /**
     * Creates an SQL string that selects only the record
     * that has the same primary key as the given object
     */
    public String toSelect( Rtbl rtbl )
        throws RdbException
    {
        StringBuffer sql = new StringBuffer();

        sql.append( toSelectAll( rtbl ) );
        sql.append( toWhere( rtbl ) );

        return sql.toString();
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
    public String toDelete( Rtbl rtbl, Object vlist[] )
        throws RdbException
    {
        StringBuffer sql = new StringBuffer();
        sql.append( "DELETE FROM " ).append( getTableName( rtbl ) );
        sql.append( toWhere( rtbl, vlist ) );
        return sql.toString();
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
     *
     * The first logical operator always has to be empty
     */
    public String toDeleteEq( Rtbl rtbl, Object vlist[] )
        throws RdbException
    {
        StringBuffer sql = new StringBuffer();
        sql.append( "DELETE FROM ").append( getTableName( rtbl ) );
        sql.append( toWhereEq( rtbl, vlist ) );
        return sql.toString();
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
     *
     * The first logical operator always has to be empty
     */
    public String toDeleteEq( Rtbl rtbl, List vlist )
        throws RdbException
    {
        return toDeleteEq( rtbl, vlist.toArray() );
    }

    /**
     * Deletes the records that match the key-value pairs included
     * in the given map
     */
    public String toDelete( Rtbl rtbl, Map rec )
        throws RdbException
    {
        List vlist;
        vlist = name.subroutine.util.Lists.toList( rec );
        return toDeleteEq( rtbl, vlist );
    }

    /**
     * Deletes a record that has the same value for the primary key
     * as the given object
     */
    public String toDelete( Rtbl rtbl )
        throws RdbException
    {
        StringBuffer sql = new StringBuffer();
        sql.append( "DELETE FROM ").append( getTableName( rtbl ) );
        sql.append( toWhere( rtbl ) );
        return sql.toString();
    }

    /**
     * Creates an update statement using a matrix.  Do not use it
     * alone unless you really want to update ALL records
     *
     * @param ulist a 2 by n matrix of 
     * <pre>
     * field    value
     * field    value
     * ...
     * </pre>
     * 
     */
    public String toUpdateAll( Rtbl rec, Object ulist[] )
        throws RdbException
    {
        StringBuffer sql = new StringBuffer();

        sql.append( "UPDATE " );
        sql.append( getTableName( rec ) );
        sql.append( " SET " );

        for( int i = 0; i < ulist.length; i += 2 ){
            if( i > 0 ){
                sql.append( ", " );
            }
            sql.append( ulist[i] );
            sql.append( " = " );
            sql.append( toLiteral( rec, ulist[i], ulist[i + 1] ) );
        }
        return sql.toString();
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
    public String toUpdate( Rtbl rec, Object ulist[], Object vlist[] )
        throws RdbException
    {
        StringBuffer sql = new StringBuffer();

        sql.append( toUpdateAll( rec, ulist ) );
        sql.append( toWhere( rec, vlist ) );

        return sql.toString();
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
     * @param toWhereEq for the structure of the vlist matrix
     */
    public String toUpdateEq( Rtbl rec, Object ulist[], Object vlist[] )
        throws RdbException
    {
        StringBuffer sql = new StringBuffer();

        sql.append( toUpdateAll( rec, ulist ) );
        sql.append( toWhereEq( rec, vlist ) );

        return sql.toString();
    }

    /**
     * Creates an update statement using a matrix
     *
     * The update statement includes a where clause that would
     * only select the records that have the same primary key
     * as the given rec
     *
     * @param rec provides field information and primary key.  The
     * primary key must be correct
     *
     * @param ulist a 2 by n matrix of 
     * <pre>
     * field    value
     * field    value
     * ...
     * </pre>
     */
    public String toUpdate( Rtbl rec, Object ulist[] )
        throws RdbException
    {
        StringBuffer sql = new StringBuffer();

        sql.append( toUpdateAll( rec, ulist ) );
        sql.append( toWhere( rec ) );

        return sql.toString();
    }

    /**
     * Creates an update statement using a matrix
     *
     * The update statement includes a where clause that would
     * only select the records that have the same primary key
     * as the given rec
     *
     * @param rec provides field information and primary key.  The
     * primary key must be correct
     *
     * @param ulist a 2 by n matrix of 
     * <pre>
     * field    value
     * field    value
     * ...
     * </pre>
     */
    public String toUpdate( Rtbl rec, List ulist )
        throws RdbException
    {
        return toUpdate( rec, ulist.toArray() );
    }

    /**
     * Creates an update statement from the given record, update all
     * columns using the values in the record except the primary key 
     */
    public String toUpdate( Rtbl rec )
        throws RdbException
    {
        List set_list = name.subroutine.util.Lists.toList( this, rec );
        return toUpdate( rec, set_list );
    }

    /**
     * Converts a date object into yyyymmdd integer
     */
    public static int toInt( java.util.Date d )
    {
        DateFormat df = new SimpleDateFormat( "yyyyMMdd" );

        String str;
        str = df.format( d );

        return Integer.parseInt( str );
    }

    /**
     * Converts a date object into yyyymmdd integer
     */
    public static String toString( java.util.Date d )
    {
        DateFormat df = new SimpleDateFormat( "yyyyMMdd" );

        String str;
        str = df.format( d );

        return str;
    }

    /**
     * Parses an integer into date object in every way
     */
    public static java.util.Date toDate( int d )
        throws ParseException
    {
        DateFormat df = new SimpleDateFormat( "yyyyMMdd" );

        String str;
        str = String.valueOf( d );

        return df.parse( str );
    }

    /**
     * Parses a string into date object in every way
     */
    public static java.util.Date toDate( String s )
        throws ParseException
    {
        DateFormat df;
        ParsePosition pp;
        java.util.Date retval;

        pp = new ParsePosition( 0 );

        df = DateFormat.getDateInstance( DateFormat.SHORT );
        pp.setIndex( 0 );
        retval = df.parse( s, pp );
        if( retval != null ) return retval;

        df = DateFormat.getDateInstance( DateFormat.MEDIUM );
        pp.setIndex( 0 );
        retval = df.parse( s, pp );
        if( retval != null ) return retval;

        df = DateFormat.getDateInstance( DateFormat.LONG );
        pp.setIndex( 0 );
        retval = df.parse( s, pp );
        if( retval != null ) return retval;

        df = DateFormat.getDateInstance( DateFormat.FULL );
        pp.setIndex( 0 );
        retval = df.parse( s, pp );
        if( retval != null ) return retval;

        df = new SimpleDateFormat( "M.d.y" );
        pp.setIndex( 0 );
        retval = df.parse( s, pp );
        if( retval != null ) return retval;

        df = new SimpleDateFormat( "y-M-d" );
        pp.setIndex( 0 );
        retval = df.parse( s, pp );
        if( retval != null ) return retval;

        // Made just for Oracle
        df = new SimpleDateFormat( "y-M-d H:m:s.S" );
        pp.setIndex( 0 );
        retval = df.parse( s, pp );
        if( retval != null ) return retval;

        df = new SimpleDateFormat( "yyyyMMdd" );
        pp.setIndex( 0 );
        retval = df.parse( s, pp );
        if( retval != null ) return retval;

        throw new ParseException( "Cannot parse " + s, 0 );
    }

    /**
     * Creates all the tables in the _rtbl_lst
     */
    public void createTables()
        throws RdbException, SQLException
    {
        RdbSession ses = getSession();
        for( int i = 1; i < _rtbl_lst.length; i += 2 ){
            String sql;
            sql = toCreate( (Rtbl)_rtbl_lst[i] );

            System.out.println( sql );

            ses.create( (Rtbl)_rtbl_lst[i] );
            ses.index( (Rtbl)_rtbl_lst[i] );
        }

        HighLowKeys hl = new HighLowKeys();

        ses.create( hl );
        ses.index( hl );

        ses.close();
    }

    /**
     * Retrieves a new Rtbl object by name
     */
    public Rtbl getRtbl( String name )
    {
        Rtbl retval;
        retval = (Rtbl)_rtbl_map.get( name.toLowerCase() );

        return retval;
    }

    /**
     * Retrieves a new Rtbl object by name
     */
    public Collection getRtblLst()
    {
        return _rtbl_map.values();
    }

    /**
     * Runs the garbage collector, erasing all records that cannot be
     * accessed, either directly or indirectly, through the "rootSet"
     * defined in the database
     */
    public void markAndSweep()
        throws SQLException, RdbException
    {
        RdbSession ses = getSession();
        _recycler.setSession( ses );
        _recycler.markAndSweep();
        ses.close();
    }

    /**
     * Relocates OIDs so they would be continuous
     *
     * @param batch_size is the number of records to update at one
     * time
     * @param batch_count is the maximum number of batches to run.
     * a value of -1 means to run until all oids are contiguous
     */
    public void relocate( int batch_size, int batch_count )
        throws SQLException, RdbException
    {
        RdbSession ses = getSession();
        _recycler.setSession( ses );
        _recycler.relocate( batch_size, batch_count );
        _recycler.updateHighLowKeys();
        ses.close();
    }

    /**
     * Updates highlowkeys with the highest OID found in the actual
     * tables so it can assign the correct OIDs
     */
    public void updateHighLowKeys()
        throws RdbException, SQLException
    {
        _recycler.updateHighLowKeys();
    }
        
    //
    // key management functions
    //

    static class Id
    {
        int _current;
        int _valid_until;
    }

    Map _id_map = new HashMap();

    int _grab_size = 10;
    int _starting_max_id = 1000;

    public void setStartingMaxId( int val )
    {
        _starting_max_id = val;
    }

    public int getStartingMaxId()
    {
        return _starting_max_id;
    }

    public void setGrabSize( int val )
    {
        _grab_size = val;
    }

    public int getGrabSize()
    {
        return _grab_size;
    }

    /**
     * Selects an ID from database, and if ID is not found,
     * return an initial value and adds an entry to the database.
     *
     * This will not update database if an ID is found.
     */
    public synchronized HighLowKeys _selectId( RdbSession ses, String table )
        throws RdbException, SQLException
    {
        HighLowKeys hl;
        hl = new HighLowKeys();

        table = table.toLowerCase();

        RdbResultSet rs;
        rs = ses.selectEq( hl, new String[] {
            "tablename", table
        } );

        if( !rs.next() ){
            hl.setMaxId( getStartingMaxId() );
            hl.setTableName( table );

            ses.insert( hl );
        }
        else{
            hl = (HighLowKeys)rs.get();
        }
        
        return hl;
    }

    /**
     * Returns an unused ID for a given table
     */
    public synchronized int createId( RdbSession ses, String table )
        throws RdbException, SQLException
    {
        return createId( ses, table, getGrabSize() );
    }

    /**
     * Returns an unused ID for a given table
     */
    public synchronized int createId( RdbSession ses, String table,
                                      int grab_size )
        throws RdbException, SQLException
    {
        HighLowKeys hl;
        Id id;
        id = (Id)_id_map.get( table );

        if( id == null ){
            hl = _selectId( ses, table );

            id = new Id();

            id._current = hl.getMaxId();
            id._valid_until = id._current + grab_size;

            _id_map.put( table, id );

            hl.advanceMaxId( grab_size );

            ses.update( hl );
        }
        else{
            id._current++;

            // need to call database for a new chunk
            if( id._current >= id._valid_until ){
                hl = _selectId( ses, table );

                id._current = hl.getMaxId();
                id._valid_until = id._current + grab_size;

                hl.advanceMaxId( grab_size );
                ses.update( hl );
            }
        }

        return id._current;
    }

    /**
     * Sets the next id to the given integer, and start
     * all future ids from there.
     */
    public synchronized void setId( RdbSession ses, String table, int idnum )
        throws RdbException, SQLException
    {
        HighLowKeys hl;
        Id id;
        id = (Id)_id_map.get( table );

        if( id == null ){
            id = new Id();
            _id_map.put( table, id );
        }

        hl = _selectId( ses, table );

        id._current = idnum;
        id._valid_until = id._current + getGrabSize();

        hl.setMaxId( idnum );
        hl.advanceMaxId( getGrabSize() );

        ses.update( hl );
    }
}
