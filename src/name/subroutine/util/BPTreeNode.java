package name.subroutine.util;

import java.io.*;

import java.util.Comparator;

/**
 * A node of the B+ Tree
 */
public class BPTreeNode
{
    public static final int LEAF                                      = 10;
    public static final int BRANCH                                    = 11;

    Comparator _comp;

    Object[] _key_lst;
    Object[] _value_lst;
    int[] _link_lst;
    int _size;
    int _page_id;

    int _parent_id;
    int _parent_pos;

    int _max_key_cnt;
    int _max_link_cnt;

    int _key_size;
    int _value_size;

    int _node_type;

    public BPTreeNode( int degree, int key_size, int val_size )
    {
        _max_key_cnt = 2 * degree - 1;
        _max_link_cnt = _max_key_cnt + 1;

        _key_lst = new Object[_max_key_cnt];
        _value_lst = new Object[_max_key_cnt];
        _link_lst = new int[_max_link_cnt];

        _key_size = key_size;
        _value_size = val_size;

        _size = 0;
        _node_type = LEAF;
    }

    public BPTreeNode( int degree, int key_size,
                       int val_size, Comparator comp )
    {
        this( degree, key_size, val_size );
        _comp = comp;
    }

    public void setNodeType( int type )
    {
        _node_type = type;
    }

    public int getNodeType()
    {
        return _node_type;
    }

    public boolean isLeaf()
    {
        return (_node_type == LEAF);
    }

    public void setPageId( int page_id )
    {
        _page_id = page_id;
    }
    
    public int getPageId()
    {
        return _page_id;
    }

    public int getParentId()
    {
        return _parent_id;
    }
    
    public void setParentId( int val )
    {
        _parent_id = val;
    }

    public int getParentPos()
    {
        return _parent_pos;
    }
    
    public void setParentPos( int val )
    {
        _parent_pos = val;
    }

    public int getSize()
    {
        return _size;
    }

    /**
     * number of links is always size + 1
     */
    public int getLinkCnt()
    {
        return getSize() + 1;
    }
    
    /**
     * @return true if size >= max
     */
    public boolean isFull()
    {
        return getSize() >= _max_key_cnt;
    }

    public Object getKey( int pos )
    {
        return _key_lst[pos];
    }

    public Object getVal( int pos )
    {
        return _value_lst[pos];
    }

    public int getLink( int pos )
    {
        return _link_lst[pos];
    }

    public void setLink( int page_id, int pos )
    {
        _link_lst[pos] = page_id;
    }

    public int getMaxKeyCnt()
    {
        return _max_key_cnt;
    }
    
    public int getMaxLinkCnt()
    {
        return _max_link_cnt;
    }

    public String toString()
    {
        StringBuffer retval = new StringBuffer();

        retval.append( getPageId() );
        retval.append( ":" );
        retval.append( getParentId() );
        retval.append( ":" );
        retval.append( getParentPos() );
        retval.append( ":" );
        retval.append( getSize() );
        retval.append( ":" );
        retval.append( _node_type );
        retval.append( ": " );

        for( int i = 0; i < getSize(); ++i ){
            retval.append( _link_lst[i] );
            retval.append( ",(" );
            retval.append( _key_lst[i] );
            retval.append( ',' );
            retval.append( _value_lst[i] );
            retval.append( ")," );
        }
        if( getSize() > 0 ){
            retval.append( _link_lst[getSize()] );
        }

        return retval.toString();
    }

    public int compare( Object a, Object b )
    {
        if( a == null && b == null ) return 0;
        if( a != null && b == null ) return 1;
        if( a == null && b != null ) return -1;

        if( _comp != null ){
            return _comp.compare( a, b );
        }
        
        if( a instanceof Comparable &&
            b instanceof Comparable ){
            Comparable ca = (Comparable)a;

            return ca.compareTo( b );
        }

        return a.toString().compareTo( b.toString() );
    }

    public void insert( Object key, Object val, int pos )
    {
        ////if( _size > _max_key_cnt ){
        ////System.out.println( "wha wha wha: " + _size );
        ////    //System.out.println( this );
        ////    //}
        _link_lst[_size + 1] = _link_lst[_size];
        for( int i = _size; i > pos; --i ){
            _key_lst[i] = _key_lst[i - 1];
            _value_lst[i] = _value_lst[i - 1];
            _link_lst[i] = _link_lst[i - 1];
        }

        _key_lst[pos] = key;
        _value_lst[pos] = val;

        _size++;
    }

    public void insert( Object key, Object val, int link, int pos )
    {
        _link_lst[_size + 1] = _link_lst[_size];
        for( int i = _size; i > pos; --i ){
            _key_lst[i] = _key_lst[i - 1];
            _value_lst[i] = _value_lst[i - 1];
            _link_lst[i] = _link_lst[i - 1];
        }

        _key_lst[pos] = key;
        _value_lst[pos] = val;
        _link_lst[pos] = link;

        _size++;
    }

    /**
     * insert keys/values from [src] at [src_pos] to "this" at [pos]
     * for [cnt] keys/values.  Links surrounding and between the keys
     * are also inserted
     */
    public void insert( BPTreeNode src, int src_pos, int pos, int cnt )
    {
        // starting from end, going back
        _link_lst[_size + cnt] = _link_lst[_size];
        for( int i = _size - 1; i >= pos; --i ){
            _key_lst[i + cnt] = _key_lst[i];
            _value_lst[i + cnt] = _value_lst[i];
            _link_lst[i + cnt] = _link_lst[i];
        }

        for( int i = 0; i < cnt; ++i ){
            _key_lst[pos] = src.getKey( src_pos );
            _value_lst[pos] = src.getVal( src_pos );
            _link_lst[pos] = src.getLink( src_pos );

            ++src_pos;
            ++pos;
        }
        _link_lst[pos] = src.getLink( src_pos );

        _size += cnt;
    }

    /**
     * Deletes all keys at and after [pos]
     */
    public void truncate( int pos )
    {
        _size = pos;
    }

    public void save( RandomAccessFile fp )
        throws IOException
    {
        StringWriter sw = new StringWriter();
        PrintWriter pw = new PrintWriter( sw );

        pw.print( Variant.fit( _page_id, 10, 0 ) );
        pw.print( Variant.fit( _parent_id, 10, 0 ) );
        pw.print( Variant.fit( _parent_pos, 10, 0 ) );
        pw.print( Variant.fit( _size, 10, 0 ) );
        pw.print( Variant.fit( _node_type, 5,  0 ) );
        pw.print( "\r\n" );
        for( int i = 0; i < _max_key_cnt; ++i ){
            String key = String.valueOf( _key_lst[i] );
            String val = String.valueOf( _value_lst[i] );
            pw.print( Variant.fit( _link_lst[i], 10, 0 ) );
            pw.print( "\r\n" );
            pw.print( Variant.fit( key, _key_size ) );
            pw.print( "=" );
            pw.print( Variant.fit( val, _value_size ) );
            pw.print( "\r\n" );
        }
        pw.print( Variant.fit( _link_lst[_max_link_cnt - 1], 10, 0 ) );
        pw.print( "\r\n" );
        pw.print( "--------------------" );
        pw.print( "\r\n" );
        sw.close();

        fp.writeBytes( sw.toString() );
    }

    int readInt( RandomAccessFile fp, int length )
        throws IOException
    {
        byte[] readbuf = new byte[64];
        fp.read( readbuf, 0, length );

        ////System.out.println( "reading... " + new String( readbuf ) );

        return (int)CString.atoi( readbuf, 0, length );
    }

    public void load( RandomAccessFile fp )
        throws IOException
    {
        byte[] buf = new byte[100];
        ////fp.read( buf, 0, 10 );
        ////_page_id = (int)CString.atoi( buf, 0 );
        ////fp.skipBytes( 2 );
        _page_id = readInt( fp, 10 );
        _parent_id = readInt( fp, 10 );
        _parent_pos = readInt( fp, 10 );
        ////fp.read( buf, 0, 10 );
        ////_size = (int)CString.atoi( buf, 0 );
        ////fp.skipBytes( 2 );
        _size = readInt( fp, 10 );
        _node_type = readInt( fp, 5 );

        ////////
        ////if( _node_type != LEAF && _node_type != BRANCH ){
        ////    System.out.println( "bad read at page: " + _page_id );
        ////    System.out.println( "file pointer: " + fp.getFilePointer() );
        ////    throw new RuntimeException( "bad read at page: " + _page_id );
        ////}
        //////////

        fp.skipBytes( 2 );

        // we read one link and one entry with each iteration of the
        // loop
        //
        // when we are done with the loop, we have one more link to
        // read
        int link_and_entry_size;
        link_and_entry_size = 10 + 2 +
                              _key_size + 1 + _value_size + 2;
        buf = new byte[link_and_entry_size];
        for( int i = 0; i < _max_key_cnt; ++i ){
            //// remove later: don't know what these are for...
            ////String key = String.valueOf( _key_lst[i] );
            ////String val = String.valueOf( _value_lst[i] );

            /**************
            fp.read( buf, 0, 10 + 2 );
            _link_lst[i] = (int)CString.atoi( buf, 0, 10 );
            // fp.skipBytes( 2 );

            fp.read( buf, 0, _key_size + 1 );
            _key_lst[i] = new String( buf, 0, _key_size, "UTF-8" );
            // fp.skipBytes( 1 );

            fp.read( buf, 0, _value_size + 2 );
            _value_lst[i] = new String( buf, 0, _value_size, "UTF-8" );
            // fp.skipBytes( 2 );
            ******************/

            fp.read( buf );
            _link_lst[i] = (int)CString.atoi( buf, 0, 10 );
            _key_lst[i] = new String( buf, 12, _key_size, "UTF-8" );
            _value_lst[i] = new String( buf, 12 + _key_size + 1,
                                        _value_size, "UTF-8" );
        }

        // last link
        fp.read( buf, 0, 10 + 2 );
        _link_lst[_max_link_cnt - 1] = (int)CString.atoi( buf, 0, 10 );
        // fp.skipBytes( 2 );
    }

    public void add( Object key, Object val )
    {
        insert( key, val, getLinkPos( key ) );
        /******************
        // do a quick binary search
        int min = 0;
        int max = _size - 1;

        if( _size == 0 ){
            insert( key, val, 0 );
            return;
        }
        
        // also we take care of the case where key is less than /
        // the first value
        //
        // in the search algorithm, we are testing all the spaces
        // and values after, starting at 0
        //
        //          0   1   2   3   4   5
        //        a   b   c   d   e   f   g
        //
        // so we only tested gaps b, c, d, e, f, g.  
        //
        // a is special
        if( compare( key, _key_lst[0] ) < 0 ){
            insert( key, val, 0 );
            return;
        }

        while( true ){
            if( max < min ){
                throw new RuntimeException( "Binary insert failed" );
            }

            int mid = (min + max) / 2;
            int supermid = mid + 1;

            int diff;
            diff = compare( _key_lst[mid], key );

            boolean less_than_supermid;
            if( supermid == _size ||
                compare( key, _key_lst[supermid] ) < 0 ){
                less_than_supermid = true;
            }
            else{
                less_than_supermid = false;
            }
            // if the mid <= value < supermid, insert value
            // at the position of supermid

            if( diff <= 0 ){
                if( less_than_supermid ){
                    // insert at this spot
                    insert( key, val, supermid );
                    return;
                }
                else{
                    min = mid + 1;
                }
            }
            else{
                max = mid - 1;
            }
        }

        //_key_lst[_size] = key;
        //_value_lst[_size] = val;

        //_size++;        
        *************/
    }

    public Object get( Object key )
    {
        /************
        // do a quick binary search
        int min = 0;
        int max = _size - 1;

        if( _size == 0 ){
            return null;
        }
        
        while( true ){
            if( max < min ){
                return null;
            }

            int mid = (min + max) / 2;

            int diff;
            diff = compare( _key_lst[mid], key );

            if( diff < 0 ){
                min = mid + 1;
            }
            else if( diff > 0 ){
                max = mid - 1;
            }
            else{
                return _value_lst[mid];
            }
        }
        **********/
        int pos = getPos( key );
        if( pos == -1 ) return null;
        return getVal( pos );
    }

    /**
     * returns the position of the given key
     *
     * or -1 if not found
     */
    public int getPos( Object key )
    {
        // do a quick binary search
        int min = 0;
        int max = _size - 1;

        if( _size == 0 ){
            return -1;
        }
        
        while( true ){
            if( max < min ){
                return -1;
            }

            int mid = (min + max) / 2;

            int diff;
            diff = compare( _key_lst[mid], key );

            if( diff < 0 ){
                min = mid + 1;
            }
            else if( diff > 0 ){
                max = mid - 1;
            }
            else{
                return mid;
            }
        }
    }

    /**
     * returns the position where the key belongs to
     *
     * a link position is between two keys
     */
    public int getLinkPos( Object key )
    {
        // do a quick binary search
        int min = 0;
        int max = _size - 1;

        if( _size == 0 ){
            return 0;
        }
        
        // also we take care of the case where key is less than or
        // equal to the first value
        //
        // in the search algorithm, we are testing all the spaces
        // and values after, starting at 0
        //
        //          0   1   2   3   4   5
        //        a   b   c   d   e   f   g
        //
        // so we only tested gaps b, c, d, e, f, g.  
        //
        // a is special
        if( compare( key, _key_lst[0] ) < 0 ){
            return 0;
        }

        while( true ){
            if( max < min ){
                throw new RuntimeException( "Binary insert failed" );
            }

            int mid = (min + max) / 2;
            int supermid = mid + 1;

            int diff;
            diff = compare( _key_lst[mid], key );

            boolean less_than_supermid;
            if( supermid == _size ||
                compare( key, _key_lst[supermid] ) < 0 ){
                less_than_supermid = true;
            }
            else{
                less_than_supermid = false;
            }
            // if the mid <= value < supermid, insert value
            // at the position of supermid

            if( diff <= 0 ){
                if( less_than_supermid ){
                    // insert at this spot
                    return supermid;
                }
                else{
                    min = mid + 1;
                }
            }
            else{
                max = mid - 1;
            }
        }
    }

    ////
    ////public void setKey( Object key, int pos )
    ////{
    ////    _key_lst[pos] = key;
    ////}

    ////public void setVal( Object val, int pos )
    ////{
    ////    _value_lst[pos] = val;
    ////}

    ////public void push( Object key, Object val )
    ////{
    ////    _key_lst[_size] = key;
    ////    _value_lst[_size] = val;

    ////    ++_size;
    ////}

    ////public void push( Object key, Object val, int link )
    ////{
    ////    _link_lst[_size] = link;
    ////    push( key, val );
    ////}

    /**
     * sends a section (some keys) of the node into given destination
     *
     * links on both sides are copied
     *
     *        |--------|splice range
     *     K0   K1   K2                       keys
     *   L0   L1   L2   L3                    links
     *
     * dest is appended with:
     *
     *     K1   K2                            keys
     *   L1   L2   L3                         links
     *
     * the object itself is unchanged
     */
    /*******************
    public void splice( BPTreeNode dst, int dst_pos, int pos, int len )
    {
        for( int i = 0; i < len; ++i ){
            ////
            ////dst.push( _key_lst[pos + i],
            ////          _value_lst[pos + i],
            ////          _link_lst[pos + i] );
            dst.insert( _key_lst[pos + i],
                        _value_lst[pos + i],
                        _link_lst[pos + i],
                        pos );
        }
        dst.setLink( _link_lst[pos + len], dst.getSize() );
    }
    ************/

    /**
     * removes elements, keeping only from [begin], for [size]
     * elements
     *
     * @deprecated
     */
    /**************
    public void crop( int begin, int size )
    {
        for( int i = begin; i < begin + size; ++i ){
            _key_lst[i - begin] = _key_lst[i];
            _value_lst[i - begin] = _value_lst[i];
        }
        _size = size;
    }
    ***********/
}
