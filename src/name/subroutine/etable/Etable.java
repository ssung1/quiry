package name.subroutine.etable;

import java.util.*;
import java.io.*;
import java.sql.*;

/**
 * This is used to generate classes with different trimming
 * properties.  By using these classes, we can use only one
 * "slice" function to trim the contents in different ways
 */
interface StringTrimmer
{
    abstract public String trim( String str );
}

/**
 * This is the full trim class
 *
 * @see RTrim
 */
class Trim implements StringTrimmer
{
    public String trim( String str )
    {
	return str.trim();
    }
}

/**
 * This is the right trim class
 *
 * An instance of this class is created as a static property of the
 * Etable object, so that when the Etable.slice is called, this object
 * can be used to trim the contents of a sliced field.  This acts as a
 * "trimming module", allowing the slicing method to be customized.
 */
class RTrim implements StringTrimmer
{
    public String trim( String str )
    {
        int idx;
	for( idx = str.length() - 1; idx >= 0; idx-- ){
	    if( !Character.isWhitespace( str.charAt( idx ) ) ) break;
	}
	return str.substring( 0, idx + 1 );
    }
}

/**
 * This is a string retriever.  It is used by the slice method to
 * retrieve either a new string or an old string, depending on the
 * line identifier.
 */
interface Retriever
{
    /**
     * Returns a StringBuffer.  Any StringBuffer
     *
     * Implemtors of this interface must return the appropriate
     * StringBuffer to match the needs of the slice method.
     */
    abstract public StringBuffer get( Etable table, int idx );

    /**
     * Returns a vector of objects
     */
    abstract public List get( Etable table );
}

/**
 * This class retrieves the field name of the table, given the table
 * and the index number of the field.  This is used when the easy
 * entry table attempts to continue a header line.
 */
class FieldRetriever implements Retriever
{
    /**
     * Returns the field name at position idx
     */
    public StringBuffer get( Etable table, int idx ){
	EtableField field;
	field = (EtableField)table._field_lst.elementAt( idx );
	return field._name;
    }
    /**
     * Returns an empty new vector
     *
     * In C++ we would worry about all those empty vectors, but in
     * this case, we can take advantage of the garbage collection
     */
    public List get( Etable table ){
	return new Vector();
    }
}

/**
 * This class returns a newly created string, for new records
 */
class NewRetriever implements Retriever
{
    /**
     * Returns a new string buffer
     *
     * This string buffer will eventually be added to the _value_lst
     * member of the Record object
     */
    public StringBuffer get( Etable table, int idx ){
	return new StringBuffer();
    }
    /**
     * Creates a new record and returns the _value_lst member
     */
    public List get( Etable table ){
	Record rec = new EtableRecord( table._field_lst );
	table.push( rec );

	return rec.valLst();
    }
}

/**
 * This class returns the field designated by idx
 * from the last record of the table
 */
class RecordRetriever implements Retriever
{
    public StringBuffer get( Etable table, int idx ){
	Record rec;
	rec = (Record)table._record_lst.lastElement();
	StringBuffer sb;
	sb = (StringBuffer)rec.valLst().get( idx );

	return sb;
    }
    /**
     * Creates an empty dummy vector
     *
     * Since slice() adds all the new sliced strings into the vector
     * returned by this function, we don't want our original record
     * added to twice by the same field values.
     *
     * Again, GC will take care of this dummy vector.
     */
    public List get( Etable table ){
	return new Vector();
    }
}



public class Etable extends AbstractTable
{
    /**
     * The trimmer is the full trimmer to be used later when
     * slicing a string into fields.
     *
     * In some cases, the fields need to be fully trimmed, but
     * in other cases, the fields need to be only right-trimmed.
     */
    public static Trim _trimmer = new Trim();

    /**
     * This is a right-trim unit used.
     *
     * The reason these two "functions" are used as classes is
     * because Java does not allow function pointers (a shame,
     * actually).  Oh well, you gain some, you lose some.
     *
     * @see #trimmer
     */
    public static RTrim _rtrimmer = new RTrim();

    /**
     * An instance of the NewRetriever
     */
    public static NewRetriever _newRet = new NewRetriever();

    /**
     * An instance of the FieldRetriever
     */
    public static FieldRetriever _fieldRet = new FieldRetriever();

    /**
     * An instance of the RecordRetriever
     */
    public static RecordRetriever _recRet = new RecordRetriever();

    /**
     * This is a identification number for the various line interpretations.
     * A HEADER line is a line that defines the fields within a table.
     */
    public static final int HEADER       = 100;

    /**
     * A RECORD is a line that contains data
     */
    public static final int RECORD       = 101;

    /**
     * A CONTINUATION of a previous line
     */
    public static final int CONTINUATION = 102;

    /**
     * A horizontal rule, for visual effect, really
     */
    public static final int RULE         = 103; // horizontal rule, <hr>

    /**
     * A comment
     */
    public static final int COMMENT      = 104;

    /**
     * A DELETED_RECORD is a line that contains data no longer needed
     */
    public static final int DELETED_RECORD = 105;

    /**
     * Current record being accessed
     */
    int _current;

    /**
     * Keeps the previou status for those "continuation" lines
     */
    public int _prev_status;
    /**
     * Current status, for reference, in case needed
     */
    public int _status;

    /**
     * For debugging only.  Please remove this later.
     */
    public String _errmsg;

    /**
     * Name of the table
     */
    public String _name;
    /**
     * Type of the table
     */
    public int _type;

    /**
     * This vector contains a list of Field objects.  Each field has
     * an offset and a width.
     */
    public Vector _field_lst;

    /**
     * A vector to hold all the record objects
     */
    public Vector _record_lst;

    public Etable()
    {
	init();
    }

    /**
     * initializes the etable
     */
    public void init()
    {
	_record_lst = new Vector();
	_field_lst = new Vector();
	_prev_status = 0;
	_status = 0;
    }

    /**
     * Clears all contents
     */
    public void clear()
    {
	_record_lst.clear();
	_field_lst.clear();
    }

    /**
     * Clear only the records
     */
    public void clearRecordLst()
    {
	_record_lst.clear();
    }

    /**
     * Gets number of fields
     */
    public int fieldCnt()
    {
	return _field_lst.size();
    }

    /**
     * Gets number of records
     */
    public int recordCnt()
    {
	return _record_lst.size();
    }

    /**
     * Appends a record at the end of the record set
     */
    public Table push( Record rec )
    {
	_record_lst.addElement( rec );
	return this;
    }

    /**
     * Appends an array of String objects at the end of record set.
     * The values need not match the field definitions.  This function
     * is provided to those who wish to create a record set
     */
    public Table push( String[] value )
    {
	Record rec = new EtableRecord( _field_lst );

	int i;
	for( i = 0; i < value.length; i++ ){
	    rec.push( value[i] );
	}

	return push( rec );
    }

    /**
     * Appends from current record in a result set
     */
    public Table push( ResultSet value )
    {
	Record rec = new EtableRecord( _field_lst );
	try{
	    rec.pushLst( value );
	}
	catch( Exception ex ){
	}

	return push( rec );
    }

    /**
     * Appends all the records from the current record set on
     */
    public Table pushLst( ResultSet rs )
	throws SQLException
    {
	do{
	    push( rs );
	} while( rs.next() );

	return this;
    }

    /**
     * Creates a record using given array of String objects.
     *
     * This function is provided for users who are not loading from an
     * etable file but are using the Etable object as a storage area
     * for record sets.
     */
    public Record createRecord( String[] value )
    {
	Record rec = new EtableRecord( _field_lst );

	int i;
	for( i = 0; i < value.length; i++ ){
	    rec.push( value[i] );
	}

	return rec;
    }

    /**
     * Creates a record using given List of String objects.
     *
     * This function is provided for users who are not loading from an
     * etable file but are using the Etable object as a storage area
     * for record sets.
     */
    public Record createRecord( List value )
    {
	String[] string_value = new String[0];

	string_value = (String[])value.toArray( string_value );

	return createRecord( string_value );
    }

    /**
     * Put the cursor in the first record and returns it
     *
     * @return the first record or null if there are no records
     */
    public Record first()
    {
	_current = 0;
	if( _record_lst.size() <= 0 ) return null;

	return (Record)_record_lst.elementAt( _current );
    }

    /**
     * Gets the record at current cursor position
     *
     * @return record at current position or null if there are no records
     */
    public Record get()
    {
	if( _record_lst.size() <= 0 ) return null;

	return (Record)_record_lst.elementAt( _current );
    }

    /**
     * Gets the record with the given record number
     *
     * @param num: record number
     */
    public Record get( int num )
    {
	if( _record_lst.size() <= num ) return null;
	return (Record)_record_lst.elementAt( num );
    }

    /**
     * Gets from the current record the field specified by fld_idx
     */
    public Object getVal( int fld_idx )
    {
	Record rec = get();
	if( rec == null ) return null;

	return rec.get( fld_idx );
    }

    /**
     * Gets from the current record the field specified by field name
     */
    public Object getVal( String name )
    {
	Record rec = get();
	if( rec == null ) return null;

	return rec.get( name );
    }

    /**
     * Gets from the specified record the field specified by fld_idx
     */
    public Object getVal( int rec_idx, int fld_idx )
    {
	Record rec = get( rec_idx );
	if( rec == null ) return null;

	return rec.get( fld_idx );
    }

    /**
     * Gets from the current record the field specified by field name
     */
    public Object getVal( int rec_idx, String name )
    {
	Record rec = get( rec_idx );
	if( rec == null ) return null;

	return rec.get( name );
    }

    /**
     * sets a value of the current record
     */
    public void setVal( int idx, String val )
    {
	Record rec = get();
	if( rec == null ) return;
	rec.set( idx, val );
    }

    /**
     * sets a value of the current record
     */
    public void setVal( String field, String val )
    {
	Record rec = get();
	if( rec == null ) return;

	rec.set( field, val );
    }

    /**
     * Advances the cursor to the next record
     */
    public void next()
    {
	_current++;
    }

    /**
     * Returns true if the cursor is past last record
     */
    public boolean eof()
    {
	return (_current >= _record_lst.size());
    }

    /**
     * Sets the current record to the last record and returns it
     *
     * @return last record in the record set
     */
    public Record last()
    {
	_current = _record_lst.size() - 1;

	if( _current < 0 ){
	    return null;
	}
	
	/**
	 * Could have used lastElement() too, but this way if the
	 * _current value is off, we'd know
	 */

	return (Record)_record_lst.elementAt( _current );
    }

    /**
     * Adds a field to the field list by name
     */
    public int pushFld( String name )
    {
	Field field = createField( name );
	_field_lst.add( field );
	return 1;
    }

    /**
     * Adds an array of strings into the field list
     */
    public int pushFld( String[] name_a )
    {
	for( int i = 0; i < name_a.length; i++ ){
	    pushFld( name_a[i] );
	}
	return 1;
    }

    /**
     * Gets a field by index number
     */
    public Field getFld( int idx )
    {
	Field field = (Field)_field_lst.get( idx );
	return field;
    }
    /**
     * Gets a field index by name or -1 if not found
     */
    public int getFld( String name )
    {
	for( int i = 0; i < _field_lst.size(); i++ ){
	    Field field = getFld( i );
	    if( name.equalsIgnoreCase( field.name() ) ){
		return i;
	    }
	}
	return -1;
    }

    /**
     * This function sets the field list to the provided array
     * of String objects.  The field widths and offsets are 
     * left at zero
     *
     * @return field list
     */
    public List fieldLst( String[] list )
    {
	_field_lst = new Vector();

	int i;
	for( i = 0; i < list.length; i++ ){
	    Field field = createField( list[i].trim() );
	    _field_lst.add( field );
	}
	return _field_lst;
    }

    /**
     * This function sets the field list to the provided vector
     * of String objects.  The field widths and offsets are 
     * left at zero
     *
     * @return field list
     */
    public List fieldLst( List list )
    {
	String[] string_lst = new String[0];
	string_lst = (String[])list.toArray( string_lst );

	return fieldLst( string_lst );
    }

    /**
     * Returns the field list
     */
    public List fieldLst()
    {
	return _field_lst;
    }

    /**
     * This function sets the field list to the provided vector
     * of String objects.  The field widths and offsets are 
     * left at zero
     *
     * @return field list
     */
    public List fieldLst( ResultSetMetaData list )
	throws SQLException
    {
	int count;
	count = list.getColumnCount();

	String[] string_lst = new String[count];

	for( int i = 0; i < count; i++ ){
	    String column;
	    column = list.getColumnName( i + 1 );

	    // 10/15/2000
	    //
	    // we use only the name after the period, because
	    // some databases keep the table name in the column
	    // name too

	    int period;
	    period = column.lastIndexOf( '.' );
	    if( period >= 0 ){
		column = column.substring( period + 1 );
	    }

	    string_lst[i] = column;
	}
	return fieldLst( string_lst );
    }

    /**
     * This function sets the field list to the provided vector
     * of String objects.  The field widths and offsets are 
     * left at zero
     *
     * @return field list
     */
    public List fieldLst( ResultSet list )
	throws SQLException
    {
	return fieldLst( list.getMetaData() );
    }

    /**
     * This function shall parse a line and determine the offset
     * and size of each field.  
     */
    public int _createFieldLst( String buf )
    {
	_field_lst = createFieldLst( buf );
        return _field_lst.size();
    }

    /**
     * This function shall parse a line and determine the offset
     * and size of each field.
     *
     * @return a vector of Field objects
     */
    public static Vector createFieldLst( String buf )
    {
        /*
	 * we only need to states: space, and non-space
	 *
	 * we mark each time we go from space to non-space
	 */
        final int space = 100;
	final int notspace = 101;

	int status;

	Vector field_lst = new Vector();

	/*
	 * initialize to space for now, so when we hit the first
	 * nonspace we mark it
	 */
	status = space;

	/*
	 * idx starts at 1 because the 0 position is the
	 * identifier
	 */
        EtableField field = null;
	for( int idx = 1; idx <= buf.length(); idx++ ){
	    char chr;

	    /*
	     * this is a workaround.  we need to make sure the last
	     * field * gets registered, so we are appending a space
	     * character to flush out the last field 
	     */
	    if( idx < buf.length() ){
		chr = buf.charAt( idx );
	    }
	    else{
		chr = ' ';
	    }

            switch( status ){
	    case 100:
		if( !Character.isWhitespace( chr ) ){
		    /*
		     * Update information for the previous field, if
		     * there is one
		     *
		     * There must not be any gap between two fields.
		     * This is done for the convenience of the user.
		     */
		    if( field != null ){
			field.size( idx - field._offset );
		    }

		    /*
		     * make a new field now
		     */
		    field = new EtableField();
		    field._offset = idx;

		    field._name.append( chr );

		    status = notspace;
		    break;
		}
		break;
	    case 101:
		if( Character.isWhitespace( chr ) ){
		    status = space;
		    field_lst.add( field );
		    break;
		}
		field._name.append( chr );
		break;
	    }
	}
	
        return field_lst;
    }

    /**
     * Adds a line (must be a complete line, up until linefeed) to the
     * table.  The line must include the line identifier as the first
     * character.  It will take the appropriate actions.
     */
    public int pushLine( String buf )
    {
	_prev_status = _status;

	if( buf.trim().length() <= 0 ) return 0;

	switch( buf.charAt( 0 ) ){
	case '%':
	    _status = HEADER;
	    _createFieldLst( buf );
	    return 1;
	case ' ':
	    _status = RECORD;
	    /*
	     * slice the string into the new record, with
	     * new stringbuffers, right trimmed
	     *
	     * By doing some mix and match, we can create
	     * all sorts of effects, such as appending a record
	     * onto the field names.
	     */
	    slice( buf, _newRet, _trimmer );
            return 1;
	case '*':
	    /**
	     * deleted record
	     */
	    _status = DELETED_RECORD;
	    return 1;
        case '_':
	    /**
	     * Don't set _status here Since we are just using the
	     * previous status
	     *
	     * Thus, we really don't need _prev_status, but it's too
	     * late now
	     */
	    return pushCont( buf );
        case '-':
            return 1;
	case '#':
	    return 1;
	}
	return 0;
    }
    /**
     * Only used if the line is a continuation
     */
    public int pushCont( String buf )
    {
	switch( _prev_status ){
	case HEADER:
	    slice( buf, _fieldRet, _rtrimmer );
	    return 1;
	case RECORD:
	    /*
	     * slice the string and append the results into
	     * the last record processed
	     *
	     * We can even append to the first record if
	     * we want to.
	     */
	    slice( buf, _recRet, _rtrimmer );
            return 1;
	case DELETED_RECORD:
	    /*
	     * skip the continuation of a deleted record
	     */
            return 1;
	}
	return 0;
    }

    public List slice( String buf, Retriever retriever,
			 StringTrimmer trimmer )
    {
	try{
	    return _slice( buf, retriever, trimmer );
	}
	catch( Exception ex ){
	    System.out.println( "Error: " + buf );
	    return null;
	}
    }
    /**
     * Cuts a string into slices according to the field definitions.
     *
     * @returns a Vector of StringBuffers
     */
    public List _slice( String buf, Retriever retriever, 
			  StringTrimmer trimmer )
    {
	List pieces = retriever.get( this );
	StringBuffer piece;
	String newpiece;

	if( _field_lst.size() < 1 ) return pieces;

	/*
	 * A bit tricky here.  We only slice one piece when the next
	 * slice has been found.  This is to ensure that we have a
	 * full slice before we cut or know that the slice will be
	 * incomplete so we can take the appropriate actions.
	 *
	 * As defined by the Easy Entry Table documentation, a record
	 * needs not be complete.  And we must "do the right thing", as
	 * Perl people would say...
	 */
	EtableField field;
	int len = buf.length();
	int idx;
	for( idx = 1; idx < _field_lst.size(); idx++ ){
	    int prev_idx = idx - 1;
	    /*
	     * possible situations when slicing prev_idx:
	     *
	     * 1. buf isn't long enough to have anything sliced
	     * 2. buf contains a partial slice
	     * 3. buf contains a full slice
	     */
	    field = (EtableField)_field_lst.elementAt( prev_idx );
	    
	    piece = retriever.get( this, prev_idx );
	    pieces.add( piece );
	    /*
	     * offset must be less than len or we have a problem
	     */
	    if( field._offset >= len ){
		continue;
	    }

	    int endidx = field._offset + field._size;
	    /*
	     * Compare this with the previous statement.  We only
	     * check for "greater than" because it is okay if the 
	     * offset of the NEXT field is out of range.
	     */
	    if( endidx > len ){
                newpiece = buf.substring( field._offset );
		newpiece = trimmer.trim( newpiece );
		piece.append( newpiece );
		continue;
	    }
	    newpiece = trimmer.trim( buf.substring( field._offset, endidx ) );
	    piece.append( newpiece );
	}

	/*
	 * The last field is a special variable length field.
	 *
	 * idx after the loop is equal to _field_lst.size(),
	 * so we get the element at idx - 1
	 */
	piece = retriever.get( this, idx - 1 );
        field = (EtableField)_field_lst.elementAt( idx - 1 );

	pieces.add( piece );
	if( field._offset >= len ) return pieces;

	newpiece = trimmer.trim( buf.substring( field._offset ) );
	piece.append( newpiece );

	return pieces;
    }

    /**
     * Similar to pushLine, but adds an entire file named [fname]
     *
     * This is provided to make life easier.
     *
     * Life is hard.  Please die.
     */
    public int pushFile( String fname )
	throws FileNotFoundException, IOException
    {
        return pushFile( new File( fname ) );
    }

    /**
     * Similar to pushLine, but adds an entire file
     *
     * This is provided to make life easier.
     *
     * Life is hard.  Please die.
     */
    public int pushFile( File file )
	throws FileNotFoundException, IOException
    {
	FileReader fr = new FileReader( file );
	BufferedReader br = new BufferedReader( fr );

	int i;
	while( true ){
	    String buf = br.readLine();
	    if( buf == null ) break;

	    pushLine( buf );
	}
	br.close();
	fr.close();
	return 1;
    }

    /**
     * This pushes lines in "etable form"
     */
    public void pushLineLst( String[] line_lst )
    {
	int i;
	for( i = 0; i < line_lst.length; i++ ){
	    pushLine( line_lst[i] );
	}
    }

    /**
     * Sets the current etable to the contents of data, whose first
     * row contains the field names
     */
    public void set( String[] data, int col_cnt )
    {
	int row;
	int col;

	int max_row = data.length / col_cnt;

	clear();
	for( row = 0; row < max_row; row++ ){
	    Record rec = null;

	    if( row > 0 ){
		rec = new EtableRecord( fieldLst() );
		push( rec );
	    }
	    for( col = 0; col < col_cnt; col++ ){
		String val;

		val = data[ row * col_cnt + col ];
		if( row == 0 ){
		    pushFld( val );
		    continue;
		}
		rec.push( val );
	    }
	}
    }

    /**
     * Deletes a column
     */
    public Table delete( String name )
    {
	int idx;
	idx = getFld( name );

	if( idx >= 0 ){
	    delete( idx );
	}
	return this;
    }

    /**
     * Deletes a column
     */
    public Table delete( int idx )
    {
	_field_lst.remove( idx );

	for( first(); !eof(); next() ){
	    get().valLst().remove( idx );
	}

	return this;
    }

    /**
     * Creates a comma separated value file from the table
     */
    public String toCsv()
    {
	StringBuffer retval = new StringBuffer();
	Vector v;
	Enumeration e;

	v = _field_lst;
	for( e = v.elements();
	     e.hasMoreElements(); ){
	    retval.append( ((Field)(e.nextElement())).name() + ", " );
	}
	retval.delete( retval.length() - 2, retval.length() );
	retval.append( "\n" );

	Record rec;
	for( first(); !eof(); next() ){
	    rec = get();

	    retval.append( Csv.createCSV( rec.toArray() ) );
	    retval.append( "\n" );
	}
	
	return retval.toString();
    }

    /**
     * Creates an html table document of this table
     *
     * This function is the equivalent of toHtmlTable( "" )
     */
    public String toHtmlTable()
    {
	return toHtmlTable( "" );
    }

    /**
     * creates an html table document of this table
     *
     * @param param is the optional param string after the table tag
     */
    public String toHtmlTable( String param )
    {
	StringBuffer retval = new StringBuffer();
	List v;
	Iterator e;

	retval.append( "<table " + param + ">\n" );

	retval.append( "<tr>" );
	v = _field_lst;
	for( e = v.iterator();
	     e.hasNext(); ){
	    retval.append( "<th>" );
	    retval.append( ((Field)(e.next())).name() );
	    retval.append( "</th>" );
	}
	retval.append( "</tr>\n" );

	Record rec;
	for( first(); !eof(); next() ){
	    rec = get();

	    retval.append( "<tr>" );
	    v = rec.valLst();
	    for( e = v.iterator();
		 e.hasNext(); ){
		retval.append( "<td>" );

		String buf;

		buf = e.next().toString();
		if( buf.trim().length() > 0 ){
		    retval.append( buf );
		}
		else{
		    retval.append( "&nbsp;" );
		}
		retval.append( "</td>" );
	    }
	    retval.append( "</tr>\n" );
	}

	retval.append( "</table>\n" );
	
	return retval.toString();
    }

    /**
     * creates an html table document of this table
     *
     * @param tablep is the parameter for the table
     * @param thp is the parameter for the header
     * @param tdp is the parameter for the cell
     */
    public String toHtmlTable( String tablep, String thp, String tdp )
    {
	StringBuffer retval = new StringBuffer();
	List v;
	Iterator e;

	retval.append( "<table " + tablep + ">\n" );

	retval.append( "<tr>" );
	v = _field_lst;
	for( e = v.iterator();
	     e.hasNext(); ){
	    retval.append( "<td " + thp + ">" );
	    retval.append( ((Field)(e.next())).name() );
	    retval.append( "</th>" );
	}
	retval.append( "</tr>\n" );

	Record rec;
	for( first(); !eof(); next() ){
	    rec = get();

	    retval.append( "<tr>" );
	    v = rec.valLst();
	    for( e = v.iterator();
		 e.hasNext(); ){
		retval.append( "<td " + tdp + ">" );

		String buf;

		buf = e.next().toString();
		if( buf.trim().length() > 0 ){
		    retval.append( buf );
		}
		else{
		    retval.append( "&nbsp;" );
		}
		retval.append( "</td>" );
	    }
	    retval.append( "</tr>\n" );
	}

	retval.append( "</table>\n" );
	
	return retval.toString();
    }

    public Record createRecord()
    {
	return new EtableRecord( fieldLst() );
    }
    public Field createField( String name )
    {
	return new EtableField( name );
    }

    /**
     * for debugging only
     */
    public static void main( String argv[] ) throws Exception
    {
	Etable etable = new Etable();

	etable.pushFile( argv[0] );

	String[] a = {
	    "koofy", "Koofy", "Koofy@tolkien.org", "???", "gerbil"
	};
	etable.push( a );
	etable.fieldLst( a );

	System.out.println( etable.toCsv() );
    }
}
