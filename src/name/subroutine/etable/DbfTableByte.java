package name.subroutine.etable;

import java.util.*;
import java.io.*;
import java.sql.*;

/**
 * This is similar to DbfTable but reads in bytes instead
 * of strings.  This makes it faster.
 *
 * (Still being evaluated)
 */
public class DbfTableByte extends AbstractDSTable
{
    /**
     * Size of the DBF header
     */
    public static final int HEADER_SIZE = 32;

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

    public DbfTableByte( String name )
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

        // remember, ugly starts with U
        // and DBF records start with 1 (0 is delete flag)
        offset = 1;

        Iterator it;
        for( it = _field_lst.iterator(); it.hasNext(); ){
            DbfField f;
            f = (DbfField)it.next();

            f.offset( offset );
            offset += f.size();
        }
    }

    void _updateHeader()
    {
        int total;

        // total also begins with 1
        total = 1;

        Iterator it;
        for( it = _field_lst.iterator(); it.hasNext(); ){
            DbfField f;
            f = (DbfField)it.next();

            total += f.size();
        }

        _rec_size = total;
        _rec = new byte[_rec_size];

        int start;
        start = (fieldCnt() + 1) * DbfField.STORAGE_SIZE;

        if( _type == 0x30 ){
            _header_size = start + 264;
        }
        else{
            _header_size = start + 1;
        }

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
     *
     * @param type can be foxpro or visual foxpro
     */
    public void create( String type )
        throws FileNotFoundException, IOException
    {
        init( _name );
        
        if( _fp != null ){
            _fp.close();
        }

        _fp = new RandomAccessFile( _name, "rw" );

        // truncate!
        _fp.setLength( 0 );

        String t;
        t = type.toLowerCase();

        if( t.startsWith( "visual" )
            || t.startsWith( "vfp" ) ){
            _type = 0x30;
        }
        else{
            _type = 0x03;
        }

        _updateFieldLst();
        _created = true;
    }

    /**
     * Reads bcnt of bytes and converts into an integer, little-endian
     *
     * <pre>
     * little-endian: people who eat eggs from the smaller end.
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
     * little-endian: people who eat eggs from the smaller end.
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
     * Opens the DBF file
     */
    public void open()
        throws FileNotFoundException, IOException
    {
        if( !(new File( _name )).exists() ){
            throw new FileNotFoundException( _name + " not found." );
        }

        _fp = new RandomAccessFile( _name, "rw" );

        _fp.seek( 0 );

        _type = _fp.read();

        _last_modify = 0;
        for( int i = 0; i < 3; i++ ){
            _last_modify *= 100;
            _last_modify += _fp.read();
        }

        _size = _intRead( 4 );

        _header_size = _intRead( 2 );

        _rec_size = _intRead( 2 );

        _fp.skipBytes( 2 );

        _transaction = _fp.read();

        // encryption flag
        _fp.skipBytes( 1 );

        // free record thread
        _fp.skipBytes( 4 );

        // multi-user: reserved
        _fp.skipBytes( 8 );
        
        _index = _fp.read();

        // language driver/code page
        _code_page = _fp.read();

        // reserved
        _fp.skipBytes( 2 );

        // now do fields......
        // needs more work here to emcompass more types

        // visual foxpro
        int fcnt;
        if( _type == 0x30 ){
            fcnt = (_header_size - 264) / 32 - 1;
        }
        else{
            fcnt = (_header_size - 1) / 32 - 1;
        }

        _rec = new byte[_rec_size];

        // 11 chars for field name, including null terminator
        byte fnamebuf[] = new byte[11];

        // offset starts with 1
        // because 0 is the "delete flag"
        int offset = 1;
        for( int i = 0; i < fcnt; i++ ){
            DbfField f;
            // look for null terminator
            _fp.read( fnamebuf );
            int z;
            for( z = 0; z < fnamebuf.length; z++ ){
                if( fnamebuf[z] == 0 ) break;
            }
            String fname;
            fname = new String( fnamebuf, 0, z );

            int ftype;
            ftype = _fp.read();

            f = new DbfField( fname, ftype );

            // offset, but we calculate our own anyways
            _fp.skipBytes( 4 );

            int fwidth;
            int fprec;
            if( ftype == 'N' ){
                fwidth = _fp.read();
                fprec = _fp.read();
                
                f.prec( fprec );
            }
            else{
                fwidth = _intRead( 2 );
            }
            f.size( fwidth );
            f.offset( offset );

            // reserved for mutli user
            _fp.skipBytes( 2 );
            // work area
            _fp.skipBytes( 1 );
            // reserved for mutli user
            _fp.skipBytes( 2 );
            // flag for set fields
            _fp.skipBytes( 1 );
            // reserved
            _fp.skipBytes( 7 );

            int idx;
            idx = _fp.read();

            f.index( idx );

            _field_lst.add( f );
            offset += f.size();
        }
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
     * @returns a Record
     */
    Record _slice( byte[] buf )
    {
        Record pieces = new DbfRecordByte( _field_lst );
        byte[] piece;

        if( _field_lst.size() < 1 ) return pieces;

        /*
         * As defined by the Easy Entry Table documentation, a record
         * needs not be complete.  And we must "do the right thing", as
         * Perl people would say...
         */
        DbfField field;
        int len = buf.length;
        int idx;
        for( idx = 0; idx < _field_lst.size(); idx++ ){
            /*
             * possible situations when slicing prev_idx:
             *
             * 1. buf isn't long enough to have anything sliced
             * 2. buf contains a partial slice
             * 3. buf contains a full slice
             */
            field = (DbfField)_field_lst.get( idx );
            
            /*
             * offset must be less than len or we have a problem
             */
            if( field._offset >= len ){
                continue;
            }

            int endidx = field._offset + field._size;
            if( endidx > len ){
                endidx = len;
            }

            int piece_len;
            if( endidx > len ){
                piece_len = len - field._offset;
            }
            else{
                piece_len = field._size;
            }

            piece = new byte[piece_len];

            System.arraycopy( buf, field._offset, piece, 0, piece_len );

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
        return get();
    }

    public Record get()
    {
        try{
            Record rec;

            _seek( _current );
            rec = read();
            prev();
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
            
            _current++;

            if( retval < 0 ) return null;

            //String x = new String( _rec, "UTF-8" );

            Record rec = _slice( _rec );

            return rec;
        }
        catch( IOException ex ){
            return null;
        }
    }

    public byte[] readLine()
    {
        try{
            int retval;
            retval = _fp.read( _rec, 0, _rec_size );

            _current++;

            if( retval < 0 ) return null;

            return _rec;
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
     * @return true if the record is a deleted record
     */
    public boolean isDeleted()
    {
        return (_rec[0] == '*');
    }


    /**
     * Closes a table
     */
    public void close()
        throws IOException
    {
        _updateFieldLst();
        _updateHeader();

        if( _modified || _created ){
            _writeHeader();
            _writeFieldLst();
        }
        if( _created ){
            _writeFiller();
        }

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
        _fp.seek( HEADER_SIZE );

        Iterator it;
        for( it = fieldLst().iterator(); it.hasNext(); ){
            DbfField f;
            f = (DbfField)it.next();

            // field name...truncate to 10 chars + null
            String fname;
            fname = name.subroutine.util.Variant.fit( f.name(),
                                                       11, 0, (char)0 );
            byte name[];
            name = fname.getBytes( "UTF-8" );
            name[10] = 0;
            _fp.write( name, 0, 11 );

            // field type
            _fp.writeByte( f.type() );

            // offset
            _intWrite( f.offset(), 4 );

            // width/size
            if( f.type() == 'N' ){
                _fp.writeByte( f.size() );
                _fp.writeByte( f.prec() );
            }
            else{
                _intWrite( f.size(), 2 );
            }

            // reserved
            if( _created ){
                _intWrite( 0, 2 );
            }
            else{
                _fp.skipBytes( 2 );
            }

            // work area id
            if( _created ){
                _intWrite( 0, 1 );
            }
            else{
                _fp.skipBytes( 1 );
            }

            // reserved
            if( _created ){
                _intWrite( 0, 2 );
            }
            else{
                _fp.skipBytes( 2 );
            }

            // flag for set fields
            if( _created ){
                _intWrite( 0, 1 );
            }
            else{
                _fp.skipBytes( 1 );
            }

            // reserved
            if( _created ){
                _intWrite( 0, 7 );
            }
            else{
                _fp.skipBytes( 7 );
            }

            // index flag
            _fp.writeByte( f.index() );
        }
    }

    void _writeHeader()
        throws IOException
    {
        _fp.seek( 0 );

        // type or "version number"
        _fp.writeByte( _type );

        // last modified date
        _fp.writeByte( _last_modify / 10000 );
        _fp.writeByte( (_last_modify / 100) % 100 );
        _fp.writeByte( (_last_modify % 100 ) );

        // number of records
        _intWrite( size(), 4 );

        // length of header
        _intWrite( _header_size, 2 );

        // length of each record
        _intWrite( _rec_size, 2 );

        // reserved
        _intWrite( 0, 2 );

        // transaction
        _fp.writeByte( _transaction );

        // encryption flag
        _fp.writeByte( 0 );
        // free record thread
        _intWrite( 0, 4 );
        // mutli user
        _intWrite( 0, 8 );

        // index (MDX) flag
        _fp.writeByte( _index );

        // language driver/code page
        _fp.writeByte( _code_page );

        // reserved
        _intWrite( 0, 2 );

    }

    public Table push( Record rec ){
        try{
            _seek( size() );

            _fp.writeBytes( DbfRecordByte.toString( rec ) );
        
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
        // 0x0d
        _fp.writeByte( 0x0d );

        if( _type == 0x30 ){
            byte buf[] = new byte[263];
            _fp.write( buf );
        }
    }

    /**
     * writes record to table and advances pointer
     */
    public void writeRecord( Record rec )
        throws IOException
    {
        _seek( _current );
        _fp.writeBytes( DbfRecordByte.toString( rec ) );

        ++_current;

        if( _size < _current ){
            _size = _current;
        }

        _modified = true;
    }

    public Record createRecord()
    {
        return new DbfRecordByte( fieldLst() );
    }
    public Field createField( String name )
    {
        return new DbfField( name );
    }
    public Field createField( String name, int type )
    {
        return new DbfField( name, type );
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
