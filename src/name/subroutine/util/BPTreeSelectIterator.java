package name.subroutine.util;

import java.io.*;
import java.util.Comparator;
import java.util.Stack;

public class BPTreeSelectIterator implements java.util.Iterator
{
    BPTree _parent;
    Object _key;

    BPTreeNode _curr;
    int _curr_pos;

    boolean _eof;
    boolean _bof;

    // /**
    //  * stack of nodes traveled
    //  */
    // Stack _st_node = new Stack();
    // 
    // /**
    //  * stack of positions traveled
    //  */
    // Stack _st_pos = new Stack();

    public BPTreeSelectIterator( BPTree parent, Object key,
                                 BPTreeNode node, int pos )
        throws IOException
    {
        _parent = parent;

        _curr = node;
        _curr_pos = pos;
        
        _key = key;

        _eof = false;
        _bof = false;

        while( hasPrevious() ){
            previous();
        }

        // if we stopped because we ran into another key, go next
        // however, if we stopped because we reached bof, don't go next
        if( !_bof ){
            next();
        }
    }

    /**
     * constructor for an empty iterator
     */
    public BPTreeSelectIterator()
    {
        _eof = true;
        _bof = true;
    }

    /**
     * moves cursor to the leftmost decendent
     */
    public void gotoLeftmostDecendent()
        throws IOException
    {
        while( true ){
            if( _curr.isLeaf() ) break;

            _curr = _parent.loadPage( _curr.getLink( _curr_pos ) );
            _curr_pos = 0;
        }
    }

    /**
     * moves cursor to the rightmost decendent
     */
    public void gotoRightmostDecendent()
        throws IOException
    {
        while( true ){
            if( _curr.isLeaf() ) break;

            int link_pos = _curr_pos + 1;
            _curr = _parent.loadPage( _curr.getLink( link_pos ) );
            _curr_pos = _curr.getSize() - 1;
        }
    }
    

    /**
     * returns true if success, false if not
     */
    public boolean gotoNextAncestor()
        throws IOException
    {
        BPTreeNode node = _curr;
        int pos = _curr_pos;
        while( true ){
            if( _parent.getRootId() == node.getPageId() ){
                return false;
            }

            int parent_pos = node.getParentPos();
            node = _parent.loadPage( node.getParentId() );
            pos = parent_pos;

            // stop only if we stop at a valid value
            if( pos < node.getSize() ){
                _curr = node;
                _curr_pos = pos;
                return true;
            }
        }
    }

    /**
     * returns true if success, false if not
     */
    public boolean gotoPreviousAncestor()
        throws IOException
    {
        BPTreeNode node = _curr;
        int pos = _curr_pos;
        while( true ){
            if( _parent.getRootId() == node.getPageId() ){
                return false;
            }

            int parent_pos = node.getParentPos() - 1;
            node = _parent.loadPage( node.getParentId() );
            pos = parent_pos;

            //System.out.println( "prev ance: " + node.getPageId() + " at " + pos );
            // stop only if we stop at a valid value
            if( pos >= 0 ){
                _curr = node;
                _curr_pos = pos;
                return true;
            }
        }
    }

    public boolean hasPrevious()
    {
        if( _bof ) return false;

        if( _curr.compare( _curr.getKey( _curr_pos ), _key ) != 0 ){
            return false;
        }

        return true;
    }

    public Object previous()
    {
        ////System.out.println( "curr: " + _curr.getPageId() + "   pos: " + _curr_pos + "/" + _curr.getSize() );
        Object retval = _curr.getVal( _curr_pos );

        --_curr_pos;

        if( _curr.isLeaf() ){
            if( _curr_pos < 0 ){
                try{
                    if( !gotoPreviousAncestor() ){
                        ++_curr_pos;
                        _bof = true;
                    }
                }
                catch( IOException ex ){
                    throw new RuntimeException( ex );
                }
            }
        }
        else{
            int link_pos = _curr_pos + 1;

            if( link_pos < 0 ){
                try{
                    if( !gotoPreviousAncestor() ){
                        ++_curr_pos;
                        _bof = true;
                    }
                }
                catch( IOException ex ){
                    throw new RuntimeException( ex );
                }
            }
            else{
                try{
                    _curr = _parent.loadPage( _curr.getLink( link_pos ) );
                    _curr_pos = _curr.getSize() - 1;
                    gotoRightmostDecendent();
                }
                catch( IOException ex ){
                    throw new RuntimeException( ex );
                }
            }
        }

        return retval;
    }

    public boolean hasNext()
    {
        if( _eof ) return false;

        if( _curr.compare( _curr.getKey( _curr_pos ), _key ) != 0 ){
            return false;
        }

        return true;
    }

    /***************
    public Object next()
    {
        System.out.println( "curr: " + _curr.getPageId() + "   pos: " + _curr_pos + "/" + _curr.getSize() );
        Object retval = _curr.getVal( _curr_pos );

        if( _curr.isLeaf() ){
            ++_curr_pos;
            
                    System.out.println( "get!" );
            // if overflow, get "parent" by popping travel stack
            if( _curr_pos >= _curr.getSize() ){
                    System.out.println( "pop!" );
                if( !_st_node.empty() ){
                    _curr = (BPTreeNode)_st_node.pop();
                    _curr_pos = ((Integer)_st_pos.pop()).intValue();
                }
                else{
                    _curr = null;
                    _curr_pos = -1;
                }
            }
        }
        else{
            ++_curr_pos;
            //
            //   k0    k1    k2
            // l0    l1    l2    l3
            //                        ^
            if( _curr_pos >= _curr.getLinkCnt() ){
                if( !_st_node.empty() ){
                    _curr = (BPTreeNode)_st_node.pop();
                    _curr_pos = ((Integer)_st_pos.pop()).intValue();
                }
                else{
                    _curr = null;
                    _curr_pos = -1;
                }
            }
            //   k0    k1    k2
            // l0    l1    l2    l3
            //                   ^
            else if( _curr_pos >= _curr.getSize() ){
                try{
                    _curr = _parent.loadPage( _curr.getLink( _curr_pos ) );
                }
                catch( IOException ex ){
                    throw new RuntimeException( ex );
                }
                _curr_pos = 0;
            }
            else{
                System.out.println( "next: push: " + _curr.getPageId() + " at " +  _curr_pos );
                _st_node.push( _curr );
                _st_pos.push( new Integer( _curr_pos ) );
                
                try{
                    _curr = _parent.loadPage( _curr.getLink( _curr_pos ) );
                }
                catch( IOException ex ){
                    throw new RuntimeException( ex );
                }
                _curr_pos = 0;
            }
        }

        if( _curr != null ){
            if( _curr.compare( _curr.getKey( _curr_pos ), _key ) != 0 ){
                _curr = null;
                _curr_pos = -1;
            }
        }

        return retval;
    }
    ************/

    /********************
    public Object next()
    {
        System.out.println( "curr: " + _curr.getPageId() + "   pos: " + _curr_pos + "/" + _curr.getSize() );
        Object retval = _curr.getVal( _curr_pos );

        ++_curr_pos;
        while( true ){
            if( _curr.isLeaf() ){
                if( _curr_pos >= _curr.getSize() ){
                    if( _st_node.empty() ){
                        _curr = null;
                        _curr_pos = -1;
                    }
                    else{
                        _curr = (BPTreeNode)_st_node.pop();
                        _curr_pos = ((Integer)_st_pos.pop()).intValue();
                        System.out.println( "next: pop: " + _curr.getPageId() );
                    }
                }
                break;
            }
            else{
                if( _curr_pos >= _curr.getSize() ){
                    try{
                        _curr = _parent.loadPage( _curr.getLink( _curr_pos ) );
                    }
                    catch( IOException ex ){
                        throw new RuntimeException( ex );
                    }
                    _curr_pos = 0;
                }
                else{
                    System.out.println( "next: push: " + _curr.getPageId() + " at " +  _curr_pos );
                    _st_node.push( _curr );
                    _st_pos.push( new Integer( _curr_pos ) );
                    
                    try{
                        _curr = _parent.loadPage( _curr.getLink( _curr_pos ) );
                    }
                    catch( IOException ex ){
                        throw new RuntimeException( ex );
                    }
                    _curr_pos = 0;
                }
            }
        }

        if( _curr != null ){
            if( _curr.compare( _curr.getKey( _curr_pos ), _key ) != 0 ){
                _curr = null;
                _curr_pos = -1;
            }
        }

        return retval;
    }
    ***********/

    /*************
    public Object next()
    {
        System.out.println( "curr: " + _curr.getPageId() + "   pos: " + _curr_pos + "/" + _curr.getSize() );
        Object retval = _curr.getVal( _curr_pos );

        ++_curr_pos;
        while( true ){
            if( _curr.isLeaf() ){
                if( _curr_pos >= _curr.getSize() ){
                    if( _parent.getRootId() == _curr.getPageId() ){
                        _eof = true;
                        --_curr_pos;
                    }
                    else{
                        int parent_pos = _curr.getParentPos();
                        try{
                            _curr = _parent.loadPage( _curr.getParentId() );
                        }
                        catch( IOException ex ){
                            throw new RuntimeException( ex );
                        }
                        _curr_pos = parent_pos;
                        continue;
                    }
                }
                break;
            }
            else{
                if( _curr_pos >= _curr.getLinkCnt() ){
                    if( _parent.getRootId() == _curr.getPageId() ){
                        _eof = true;
                        --_curr_pos;
                    }
                    else{
                        int parent_pos = _curr.getParentPos();
                        try{
                            _curr = _parent.loadPage( _curr.getParentId() );
                        }
                        catch( IOException ex ){
                            throw new RuntimeException( ex );
                        }
                        _curr_pos = parent_pos;
                    }
                    break;
                }
                //else if( _curr_pos >= _curr.getSize() ){
                //    try{
                //        _curr = _parent.loadPage( _curr.getLink( _curr_pos ) );
                //    }
                //    catch( IOException ex ){
                //        throw new RuntimeException( ex );
                //    }
                //    _curr_pos = 0;
                //}
                else{
                    //System.out.println( "next: push: " + _curr.getPageId() + " at " +  _curr_pos );
                    //_st_node.push( _curr );
                    //_st_pos.push( new Integer( _curr_pos ) );
                    
                    try{
                        _curr = _parent.loadPage( _curr.getLink( _curr_pos ) );
                    }
                    catch( IOException ex ){
                        throw new RuntimeException( ex );
                    }
                    _curr_pos = 0;
                }
            }
        }

        //if( _curr != null ){
        //    if( _curr.compare( _curr.getKey( _curr_pos ), _key ) != 0 ){
        //        _curr = null;
        //        _curr_pos = -1;
        //    }
        //}

        return retval;
    }
    ******/

    public Object next()
    {
        ////System.out.println( "curr: " + _curr.getPageId() + "   pos: " + _curr_pos + "/" + _curr.getSize() );
        Object retval = _curr.getVal( _curr_pos );

        ++_curr_pos;

        if( _curr.isLeaf() ){
            if( _curr_pos >= _curr.getSize() ){
                try{
                    if( !gotoNextAncestor() ){
                        --_curr_pos;
                        _eof = true;
                    }
                }
                catch( IOException ex ){
                    throw new RuntimeException( ex );
                }
            }
        }
        else{
            if( _curr_pos >= _curr.getLinkCnt() ){
                try{
                    if( !gotoNextAncestor() ){
                        --_curr_pos;
                        _eof = true;
                    }
                }
                catch( IOException ex ){
                    throw new RuntimeException( ex );
                }
            }
            else{
                try{
                    _curr = _parent.loadPage( _curr.getLink( _curr_pos ) );
                    _curr_pos = 0;
                    gotoLeftmostDecendent();
                }
                catch( IOException ex ){
                    throw new RuntimeException( ex );
                }
            }
        }

        return retval;
    }

    public void remove()
    {
        throw new UnsupportedOperationException( "Use the tree to remove" );
    }
}
