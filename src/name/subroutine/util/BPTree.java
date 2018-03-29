package name.subroutine.util;

import java.io.*;
import java.util.Comparator;
import java.util.Iterator;
import java.util.Map;
import java.util.HashMap;

/**
 * A node of the B+ Tree
 */
public class BPTree
{
    static byte _readbuf[] = new byte[64];
    int _size;
    int _degree;
    int _key_size;
    int _value_size;
    int _link_size = 10;
    int _page_cnt;
    int _rec_size;

    int _header_size = 144;
    int _page_size;

    int _root;

    String _filename;
    RandomAccessFile _fp;

    Comparator _comp = null;

    /**
     * a map of cached nodes, id => BPTreeNode
     */
    Map _cache_map = new HashMap();

    public BPTree()
    {
        _size = 0;
        _page_cnt = 0;
        _root = -1;
    }

    public void create( File file, int degree, int key_size, int value_size )
        throws FileNotFoundException, IOException
    {
        create( file.getAbsolutePath(), degree, key_size, value_size );
    }

    public void create( String filename, int degree,
                        int key_size, int value_size )
        throws FileNotFoundException, IOException
    {
        _filename = filename;
        _degree = degree;
        _key_size = key_size;
        _value_size = value_size;
        
        _fp = new RandomAccessFile( _filename, "rw" );
        _fp.setLength( 0 );

        /**********
        // (2 * degree - 1)        keys and values
        // (2 * degree)            links
        //
        // 10                      page number
        // 10                      parent page number
        // 10                      size
        // 5                       node type
        // 2                       crlf
        //
        // 22                      bar

        // 3 --> an equal sign, one cr, and one lf
        int key_value = _key_size + _value_size + 3;
        // 2 --> cr and lf
        int link = _link_size + 2;

        _page_size = (2 * _degree - 1) * key_value +
                     (2 * _degree) * link +
                     37 +
                     22;
        ************/
        setPageSize();
    }

    int readInt( int length )
        throws IOException
    {
        _fp.read( _readbuf, 0, length );
        return (int)CString.atoi( _readbuf, 0, length );
    }

    /**
     * Open for reading/appending
     */
    public void open( String filename )
        throws FileNotFoundException, IOException
    {
        _filename = filename;
        _fp = new RandomAccessFile( _filename, "rw" );

        _fp.skipBytes( 6 );
        _size = readInt( 10 );
        _fp.skipBytes( 2 );

        _fp.skipBytes( 6 );
        _degree = readInt( 10 );
        _fp.skipBytes( 2 );

        _fp.skipBytes( 6 );
        _key_size = readInt( 10 );
        _fp.skipBytes( 2 );

        _fp.skipBytes( 6 );
        _value_size = readInt( 10 );
        _fp.skipBytes( 2 );

        _fp.skipBytes( 6 );
        _link_size = readInt( 10 );
        _fp.skipBytes( 2 );

        _fp.skipBytes( 6 );
        _page_cnt = readInt( 10 );
        _fp.skipBytes( 2 );

        _fp.skipBytes( 6 );
        _rec_size = readInt( 10 );
        _fp.skipBytes( 2 );

        _fp.skipBytes( 6 );
        _root = readInt( 10 );
        _fp.skipBytes( 2 );

        setPageSize();
    }

    /**
     * Open for reading/appending
     */
    public void open( File file )
        throws FileNotFoundException, IOException
    {
        open( file.getAbsolutePath() );
    }

    public void close()
        throws IOException
    {
        _fp.close();
    }

    protected void setPageSize()
    {
        // (2 * degree - 1)        keys and values
        // (2 * degree)            links
        //
        // 10                      page number
        // 10                      parent page number
        // 10                      parent link position to this node
        // 10                      size
        // 5                       node type
        // 2                       crlf
        //
        // 22                      bar

        // 3 --> an equal sign, one cr, and one lf
        int key_value = _key_size + _value_size + 3;
        // 2 --> cr and lf
        int link = _link_size + 2;

        _page_size = (2 * _degree - 1) * key_value +
                     (2 * _degree) * link +
                     47 +
                     22;
    }

    public int getLinkSize()
    {
        return _link_size;
    }

    public int getPageCnt()
    {
        return _page_cnt;
    }

    public int getRecSize()
    {
        return _rec_size;
    }

    public void setRecSize( int val )
    {
        _rec_size = val;
    }

    public int getKeySize()
    {
        return _key_size;
    }

    public void setKeySize( int val )
    {
        _key_size = val;
    }

    public int getHeaderSize()
    {        
        return _header_size;
    }

    public int getRootId()
    {
        return _root;
    }

    void setRootId( int val )
    {
        ////
        ////System.out.println( "Setting root to: " + val );
        _root = val;
        reCache();
    }

    /**
     * Header structure
     *
     *                                  off    width
     * "Size: "                           0        6
     * size (number of index records)     6       10      *
     * "Degr: "                          16        6
     * degree                            22       10      *
     * "KeyS: "                          32        6
     * key size                          38       10      *
     * "ValS: "                          48        6
     * value size                        54       10      *
     * "LinS: "                          64        6
     * link size                         70       10      *
     * "PCnt: "                          80        6
     * page count                        86       10      *
     * "RecS: "                          96        6
     * record size of the data file     102       10      *
     * "Root: "                         112        6
     * root page number                 118       10      *
     */
    void saveHeader()
        throws IOException
    {
        _fp.seek( 0 );
        
        StringWriter sw = new StringWriter();
        PrintWriter pw = new PrintWriter( sw );
        pw.print( "Size: " );
        pw.print( Variant.fit( _size, 10, 0 ) );
        pw.print( "\r\n" );

        pw.print( "Degr: " );
        pw.print( Variant.fit( _degree, 10, 0 ) );
        pw.print( "\r\n" );

        pw.print( "KeyS: " );
        pw.print( Variant.fit( _key_size, 10, 0 ) );
        pw.print( "\r\n" );

        pw.print( "ValS: " );
        pw.print( Variant.fit( _value_size, 10, 0 ) );
        pw.print( "\r\n" );

        pw.print( "LinS: " );
        pw.print( Variant.fit( _link_size, 10, 0 ) );
        pw.print( "\r\n" );

        pw.print( "PCnt: " );
        pw.print( Variant.fit( _page_cnt, 10, 0 ) );
        pw.print( "\r\n" );

        pw.print( "RecS: " );
        pw.print( Variant.fit( _rec_size, 10, 0 ) );
        pw.print( "\r\n" );

        if( _size > 0 ){
            pw.print( "Root: " );
            pw.print( Variant.fit( _root, 10, 0 ) );
            pw.print( "\r\n" );
        }
        sw.close();

        _fp.writeBytes( sw.toString() );
    }

    public void save()
        throws IOException
    {
        saveHeader();
        saveCache();
    }

    /**
     * returns the size of each page
     */
    public int getPageSize()
    {
        return _page_size;
    }

    public void seekPage( int page_id )
        throws IOException
    {
        long addr;
        addr = (long)page_id * (long)_page_size;
        addr += (long)_header_size;

        ////////////////////
        ////if( addr <= 0 || addr > 35960200000l ){
        ////    System.out.println( "seeking bad addr: " );
        ////    System.out.println( addr );
        ////    System.out.println( page_id );
        ////    System.out.println( _page_size );
        ////    System.out.println( _header_size );
        ////}

        _fp.seek( addr );
    }

    /**
     * writes to file, without caching
     */
    void _savePage( BPTreeNode page )
        throws IOException
    {
        seekPage( page.getPageId() );

        page.save( _fp );

        //////////////
        ////if( page.getPageId() == 228 ){
        ////    System.out.println( "saving page 228 -- link 2 = " +
        ////                        page.getLink( 2 ) );
        ////    if( page.getLink( 2 ) == 3051296 ){
        ////        System.out.println( "halt" );
        ////        System.exit( -1 );
        ////    }
        ////}
    }

    public void savePage( BPTreeNode page )
        throws IOException
    {
        // if the page is in cache, just replace cache
        BPTreeNode cached;
        cached = (BPTreeNode)_cache_map.get( new Integer( page.getPageId() ) );
        if( cached != null ){
            _cache_map.put( new Integer( page.getPageId() ), page );
        }
        else{
            _savePage( page );
        }
    }

    void saveCache()
        throws IOException
    {
        for( Iterator it = _cache_map.values().iterator(); it.hasNext(); ){
            BPTreeNode page = (BPTreeNode)it.next();
            _savePage( page );
        }
    }

    void loadCache()
        throws IOException
    {
        // for now, just load root
        BPTreeNode page;
        page = getRootPage();

        ////
        ////System.out.println( "cache page: " + page.getPageId() );
        ////System.out.println( "root is: " + _root );

        ////if( _root != page.getPageId() ){
        ////    System.exit( -1 );
        ////}

        _cache_map.put( new Integer( _root ), page );

        ////List child_list = new ArrayList();
        for( int i = 0; i < page.getLinkCnt(); ++i ){
            BPTreeNode child;
            child = loadPage( page.getLink( i ) );
            _cache_map.put( new Integer( page.getLink( i ) ), child );
        }
    }

    /**
     * cleans cache and reloads new cache
     */
    void reCache()
    {
        try{
            saveCache();
            _cache_map.clear();
            loadCache();
        }
        catch( Exception ex ){
        }
    }

    public BPTreeNode getRootPage()
        throws IOException
    {
        /******
        if( getPageCnt() <= 0 ) return null;

        seekPage( _root );

        BPTreeNode page = new BPTreeNode( _degree, _key_size, _value_size,
                                          _comp );
        page.load( _fp );

        return page;
        ******/

        //////////System.out.println( "root id: " + _root );
        return loadPage( _root );
    }

    /**
     * load a page from file, even if the page is cached
     */
    BPTreeNode _loadPage( int id )
        throws IOException
    {
        if( getPageCnt() <= 0 ) return null;

        seekPage( id );
        // we do not use createNode because this is an existing node
        BPTreeNode page = new BPTreeNode( _degree, _key_size, _value_size,
                                          _comp );
        page.load( _fp );

        /////////////////////////
        ////if( page.getPageId() == 228 ){
        ////    System.out.println( "loading page 228 -- link 2 = " +
        ////                        page.getLink( 2 ) );
        ////}

        return page;
    }

    public BPTreeNode loadPage( int id )
        throws IOException
    {
        if( getPageCnt() <= 0 ) return null;

        BPTreeNode cached;
        cached = (BPTreeNode)_cache_map.get( new Integer( id ) );
        if( cached != null ){
            ////
            ////System.out.println( "cache hit for page: " + id );
            return cached;
        }
        else{
            return _loadPage( id );
        }
    }

    /**
     * creates a node and gives it a new ID
     */
    BPTreeNode createNode()
    {
        BPTreeNode nu;
        nu = new BPTreeNode( _degree, _key_size, _value_size, _comp );
        nu.setPageId( _page_cnt );
        ++_page_cnt;

        return nu;
    }

    /**
     * links two nodes
     */
    void link( BPTreeNode parent, BPTreeNode child, int position )
    {
        parent.setLink( child.getPageId(), position );
        child.setParentId( parent.getPageId() );
        child.setParentPos( position );
    }

    public BPTreeNode splitPage( BPTreeNode page )
        throws IOException
    {
        //                       2
        // 1,2,3   becomes      * *
        //                     1   3
        //
        //     2                         2,  4
        //    * *                       * * * *
        //   1   3,4,5      becomes    1   3   5
        BPTreeNode middle;

        // update root if [page] is root, our middle node will be
        // a new node
        // 
        // and middle will be the new root
        if( page.getPageId() == _root ){
            middle = createNode();
            middle.setNodeType( BPTreeNode.BRANCH );
            
            savePage( middle );
            ////_root = middle.getPageId();
            setRootId( middle.getPageId() );
        }
        else{
            // else, middle will just be our parent node
            middle = loadPage( page.getParentId() );
        }

        ////System.out.println( "middle: " + middle.getPageId() );
        // max count is 2 * _degree - 1
        //
        // if size is 2 * _degree - 1, then mid_pos is also _degree - 1
        int mid_pos = page.getSize() / 2;

        // number of keys on the right would be
        //
        // (total - 1) / 2
        //
        int right_half_size = (page.getSize() - 1) / 2;

        // insert the key in the middle to "middle"
        //
        // we do not use the auto insert because we want to know
        // the position at which the key was inserted, so we can
        // update the links
        Object middle_key = page.getKey( mid_pos );
        int middle_pos;
        middle_pos = middle.getLinkPos( middle_key );

        middle.insert( middle_key, page.getVal( mid_pos ), middle_pos );

        // right_half is always new...
        BPTreeNode right_half = createNode();

        right_half.setNodeType( page.getNodeType() );
        right_half.insert( page, mid_pos + 1, 0, right_half_size );

        // we use the original as the left half
        BPTreeNode left_half = page;
        left_half.truncate( mid_pos );

        // now link!
        ////System.out.println( "linking: " + middle.getPageId() );
        link( middle, left_half, middle_pos );
        link( middle, right_half, middle_pos + 1 );
        ////System.out.println( "left parent: " + left_half.getParentId() );

        savePage( left_half );
        savePage( middle );
        savePage( right_half );

        // children on the right half should be updated
        if( !right_half.isLeaf() ){
            for( int i = 0; i <= right_half.getSize(); ++i ){
                BPTreeNode right_child;

                /////////
                /////if( right_half.getLink( i ) == 27221995 ){
                /////    System.out.println( "left half: " + left_half.getPageId() );
                /////    System.out.println( "middle: " + middle.getPageId() );
                /////    System.out.println( "right half: " + right_half.getPageId() );
                /////
                /////    save();
                /////    close();
                /////    throw new RuntimeException( "bad page id at: " +
                /////                                right_half.getPageId() +
                /////                                " position " + i );
                /////}

                right_child = loadPage( right_half.getLink( i ) );

                ////System.out.println( "setting right child: " + right_child.getPageId() );
                right_child.setParentId( right_half.getPageId() );
                right_child.setParentPos( i );
                savePage( right_child );
            }
        }
        if( !middle.isLeaf() ){
            for( int i = middle_pos + 2; i <= middle.getSize(); ++i ){
                BPTreeNode middle_child;
                middle_child = loadPage( middle.getLink( i ) );
                ////System.out.println( "setting right child: " + right_child.getPageId() );
                ////middle_child.setParentId( middle.getPageId() );
                middle_child.setParentPos( i );
                savePage( middle_child );
            }
        }

        ////System.out.println( "left parent: " + left_half.getParentId() );
        return middle;
    }

    public void add( Object key, Object val )
        throws IOException
    {
        if( _size <= 0 ){
            BPTreeNode root;
            root = createNode();
            ////_root = root.getPageId();
            savePage( root );
            setRootId( root.getPageId() );

            root.add( key, val );

            savePage( root );

            ++_size;

            return;
        }

        BPTreeNode curr = getRootPage();

        /////////////
        ////int counter = 0;
        /////////////

        while( true ){
            int child_pos;

            //////////////
            ////++counter;
            ////if( counter > 100 ){
            ////    System.out.println( "stuck on " + curr.getPageId() );
            ////    System.out.println( key );
            ////}
            //////////////

            // if full, split and find the proper child (without checking
            // the parent)
            if( curr.isFull() ){
                BPTreeNode middle = splitPage( curr );
                child_pos = middle.getLinkPos( key );
                curr = getChild( middle, child_pos );
                continue;
            }
            
            // leaf, and not full
            if( curr.isLeaf() ){
                curr.add( key, val );
                savePage( curr );
                break;
            }
            else{
                child_pos = curr.getLinkPos( key );

                ///////
                ////if( curr.getLink( child_pos ) < 0 ){
                ////    System.out.println( "key: " );
                ////    System.out.println( "child pos: " + child_pos );
                ////    System.out.println( "page id: " + curr.getPageId() );
                ////    System.out.println( "link: " + curr.getLink( child_pos ) );
                ////}
                curr = getChild( curr, child_pos );
                continue;
            }
        }

        ++_size;
    }

    public BPTreeNode getChild( BPTreeNode node, int link_pos )
        throws IOException
    {
        int link = node.getLink( link_pos );
        return loadPage( link );
    }

    public Object get( Object key )
        throws IOException
    {
        BPTreeNode curr;

        curr = getRootPage();
        ///////////
        ////System.out.println( "root page id: " + curr.getPageId() );

        if( curr == null ) return null;

        Object val;
        while( true ){
            //////////////
            ////System.out.println( "trying node: " + curr.getPageId() );
            val = curr.get( key );
            if( val == null ){
                if( curr.isLeaf() ){
                    return null;
                }
                int child_pos = curr.getLinkPos( key );
                curr = getChild( curr, child_pos );
            }
            else{
                return val;
            }
        }
    }

    /**
     * returns first occurrence of [key] that is either in the [node]
     * given or is in any of the decendents.  [node] and [pos] point
     * to any of the occurrences (found by binary search).
     */
    protected void getFirst( Object key, BPTreeNode node, int pos )
        throws IOException
    {
        // set prev to the first occurrence in this node
        while( true ){
            int prev;
            while( true ){
                prev = pos;
                --pos;
                if( pos < 0 ) break;
                if( node.compare( node.getKey( pos ), key ) != 0 ) break;
            }
            if( node.isLeaf() ){
                /////////////
                ////System.out.println( node.getVal( prev ) );
                return;
            }
            else{
                node = loadPage( node.getLink( prev ) );
                pos = node.getSize() - 1;
            }
        }
    }

    public Iterator select( Object key )
        throws IOException
    {
        BPTreeNode curr;

        curr = getRootPage();

        if( curr == null ) return new BPTreeSelectIterator();

        //Object val;
        int val_pos;
        while( true ){
            //val = curr.get( key );
            val_pos = curr.getPos( key );
            if( val_pos == -1 ){
                if( curr.isLeaf() ){
                    return new BPTreeSelectIterator();
                }
                int child_pos = curr.getLinkPos( key );
                curr = getChild( curr, child_pos );
            }
            else{
                //return val;
                //

                // now get back
                //
                // we only travel children, because if the same key
                // appears in any ancestor, we would have found it
                // in the ancestor

                // go back within current node, stop

                //getFirst( key, curr, val_pos );
                //return curr.get( key );

                return new BPTreeSelectIterator( this, key, curr, val_pos );
            }
        }
    }

    public void toPrint( PrintWriter out )
        throws IOException
    {
        out.print( "Size: " );
        out.println( Variant.fit( _size, 10, 0 ) );
        out.print( "Degr: " );
        out.println( Variant.fit( _degree, 10, 0 ) );
        out.print( "KeyS: " );
        out.println( Variant.fit( _key_size, 10, 0 ) );
        out.print( "ValS: " );
        out.println( Variant.fit( _value_size, 10, 0 ) );
        out.print( "LinS: " );
        out.println( Variant.fit( _link_size, 10, 0 ) );
        out.print( "PCnt: " );
        out.println( Variant.fit( _page_cnt, 10, 0 ) );
        if( _size > 0 ){
            out.print( "Root: " );
            out.println( Variant.fit( _root, 10, 0 ) );
        }

        for( int i = 0; i < _page_cnt; ++i ){
            BPTreeNode node = loadPage( i );
            out.println( node );
        }
    }
}
