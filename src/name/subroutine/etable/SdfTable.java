package name.subroutine.etable;

import java.util.*;
import java.io.*;

/**
 * A Fixed Length Format (SDF) Table
 */
public class SdfTable extends AbstractDSTable
{
    /**
     * Size of the Sdf header
     */
    public static final int HEADER_SIZE = 0;

    /**
     * modified flag
     */
    boolean _modified;

    /**
     * last modify date (yymmdd)
     */
    int _last_modify;
   
    /**
     * File reference
     */
    RandomAccessFile _fp;

    /**
     * Size of header
     */
    int _header_size;

    /**
     * Record count
     */
    int _size;

    /**
     * record size
     */
    int _rec_size;

    /**
     * Transaction started
     */
    int _transaction;

    /**
     * MDX/CDX
     */
    int _index;

    /**
     * Code page/Language
     */
    int _code_page;

    /**
     * Record buffer
     */
    byte _rec[] = null;

    /**
     * To see if this table is newly created
     */
    boolean _created;

    public SdfTable( String name )
    {
	init( name );
    }

    /**
     * Initialization for the table.
     */
    public void init( String name )
    {
	super.init();

	_name = name;
	_created = false;
	_modified = false;

	_current = 0;
	_size = 0;
	_header_size = 0;
	_rec_size = 0;
	_transaction = 0;
	_index = 0;
	// windows ANSI for now
	_code_page = 0x03;
	_rec = null;
    }


    /**
     * Updates the each field in the field list after a change by the
     * user (such as adding a field 
     */
    void _updateFieldLst()
    {
	int offset;

	offset = 0;

	Iterator it;
	for( it = _field_lst.iterator(); it.hasNext(); ){
	    SdfField f;
	    f = (SdfField)it.next();

	    f.offset( offset );
	    offset += f.size();
	}
    }

    void _updateHeader()
    {
	int total;

	total = 0;

	Iterator it;
	for( it = _field_lst.iterator(); it.hasNext(); ){
	    SdfField f;
	    f = (SdfField)it.next();

	    total += f.size();
	}

	// need return-linefeed
	String lf = System.getProperty( "line.separator" );

	_rec_size = total + lf.length();
	_rec = new byte[_rec_size];

	// sdf files have no header
	_header_size = 0;

	java.util.Calendar cal;
	cal = new java.util.GregorianCalendar();

	int yy = java.util.Calendar.YEAR;
	int mm = java.util.Calendar.MONTH;
	int dd = java.util.Calendar.DAY_OF_MONTH;

	// (not century compliant in this case...)
	_last_modify = (cal.get( yy ) % 100) * 10000 +
	    (cal.get( mm ) + 1) * 100 +
	    cal.get( dd );
    }


    /**
     * Creates a new table
     */
    public void create()
	throws FileNotFoundException, IOException
    {
	init( _name );
	
	if( _fp != null ){
	    _fp.close();
	}

	_fp = new RandomAccessFile( _name, "rw" );

	// truncate!
	_fp.setLength( 0 );

	_created = true;
    }

    /**
     * Reads bcnt of bytes and converts into an integer, little-endian
     *
     * <pre>
     * little-endian: people who eats eggs from the smaller end.
     * </pre>
     */
    int _intRead( int bcnt )
	throws IOException
    {
	int result;
	result = 0;
	int factor = 1;
	for( int i = 0; i < bcnt; i++ ){
	    result += _fp.read() * factor;
	    factor *= 256;
	}
	return result;
    }

    /**
     * Writes bcnt of bytes and converts into an integer, little-endian
     *
     * <pre>
     * little-endian: people who eats eggs from the smaller end.
     * </pre>
     */
    void _intWrite( int val, int bcnt )
	throws IOException
    {
	for( int i = 0; i < bcnt; i++ ){
	    _fp.writeByte( val % 256 );
	    val /= 256;
	}
    }

    /**
     * Opens the Sdf file
     */
    public void open()
	throws FileNotFoundException, IOException
    {
	_fp = new RandomAccessFile( _name, "rw" );
	_fp.seek( 0 );

	String buf;
	buf = _fp.readLine();

	_rec_size = buf.length();
	_rec = new byte[_rec_size];
    }

    /**
     * Overrides AbstractTable.recordCnt()
     */
    public int recordCnt()
    {
	return _size;
    }

    /**
     * Relocates the file pointer to the given record but does not update
     * _current
     */
    void _seek( int recnum )
	throws IOException
    {
	if( recnum < 0 ) return;

	int loc;
	loc = _header_size + _rec_size * recnum;

	_fp.seek( loc );

	return;
    }

    /**
     * Cuts a string into slices according to the field definitions.
     *
     * @returns a Vector of StringBuffers
     */
    Record _slice( String buf )
    {
	Record pieces = createRecord();
	String piece;

	if( _field_lst.size() < 1 ) return pieces;

	/*
	 * As defined by the Easy Entry Table documentation, a record
	 * needs not be complete.  And we must "do the right thing", as
	 * Perl people would say...
	 */
	Field field;
	int len = buf.length();
	int idx;
	for( idx = 0; idx < fieldLst().size(); idx++ ){
	    /*
	     * possible situations when slicing prev_idx:
	     *
	     * 1. buf isn't long enough to have anything sliced
	     * 2. buf contains a partial slice
	     * 3. buf contains a full slice
	     */
	    field = (Field)fieldLst().get( idx );
	    
	    /*
	     * offset must be less than len or we have a problem
	     */
	    if( field.offset() >= len ){
		continue;
	    }

	    int endidx = field.offset() + field.size();

	    /*
	     * Compare this with the previous statement.  We only
	     * check for "greater than" because it is okay if the 
	     * offset of the NEXT field is out of range.
	     */
	    if( endidx > len ){
                piece = buf.substring( field.offset() );
	    }
	    else{
		piece = buf.substring( field.offset(), endidx );
	    }
	    pieces.push( piece );
	}
	return pieces;
    }

    /**
     * Go to the first record
     *
     * In our Java implementation, first record is 0
     */
    public Record first()
    {
	_current = 0;
	try{
	    _seek( _current );

	    return get();
	}
	catch( IOException ex ){
	    return null;
	}
    }

    public Record get()
    {
	try{
	    Record rec;

	    _seek( _current );
	    rec = read();
	    _seek( _current );

	    return rec;
	}
	catch( IOException ex ){
	    return null;
	}
    }

    public Record read()
    {
	try{
	    int retval;
	    retval = _fp.read( _rec, 0, _rec_size );
	    
	    if( retval < 0 ) return null;

	    String x = new String( _rec, "UTF-8" );

	    Record rec = _slice( x );

	    return rec;
	}
	catch( IOException ex ){
	    return null;
	}
    }

    public void prev()
    {
	_current--;
    }

    public void next()
    {
	_current++;
    }

    public Record last()
    {
	_current = size() - 1;

	if( _current < 0 ){
	    return null;
	}
	
	try{
	    _seek( _current );

	    return get();
	}
	catch( IOException ex ){
	    return null;
	}
    }

    /**
     * Closes a table
     */
    public void close()
	throws IOException
    {
	_fp.close();
    }


    /**
     * Adds a field
     */
    public int pushFld( String name )
    {
	return pushFld( name, 'C' );
    }

    /**
     * Adds a field
     */
    public int pushFld( String name, int type )
    {
	Field f = createField( name, type );
	
	return pushFld( f );
    }

    /**
     * Overrides AbstractTable.pushFld to also update header
     * information 
     */
    public int pushFld( Field field )
    {
	if( !_created ){
	    throw new UnsupportedOperationException(
	        "Cannot add fields to existing table.  Sorry."
	    );
	}

	super.pushFld( field );
	_updateFieldLst();
	_updateHeader();

	return 1;
    }


    void _writeFieldLst()
	throws IOException
    {
    }

    void _writeHeader()
	throws IOException
    {
	_fp.seek( 0 );
    }

    public Table push( Record rec ){
	try{
	    _seek( size() );

	    _fp.writeBytes( SdfRecord.toString( rec ) );
	
	    _size++;
	    // go to eof, just because it makes sense...
	    _current = _size;
	    _modified = true;
	}
	catch( IOException ex ){
	    return null;
	}

	return this;
    }

    public void _writeFiller()
	throws IOException
    {
    }

    /**
     * writes record to table and advances pointer
     */
    public void writeRecord( Record rec )
        throws IOException
    {
        _seek( _current );
        _fp.writeBytes( SdfRecord.toString( rec ) );

        ++_current;

        if( _size < _current ){
            _size = _current;
        }

        _modified = true;
    }

    public Record createRecord()
    {
	return new SdfRecord( fieldLst() );
    }
    public Field createField( String name )
    {
	return new SdfField( name );
    }
    public Field createField( String name, int type )
    {
	return new SdfField( name, type );
    }

    public Table push( java.sql.ResultSet rs ){
	//////////// finish later
	return this;
    }
    public Table pushLst( java.sql.ResultSet rs ){
	//////////// finish later
	return this;
    }
    public int pushLine( String line ){
	//////////// finish later
	return 0;
    }
    public int pushFile( String file ){
	//////////// finish later
	return 0;
    }
}
