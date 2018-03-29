package name.subroutine.util;

import java.util.Map;
import java.util.HashMap;
import java.util.List;
import java.util.ArrayList;
import java.util.Iterator;


public class TreeNode
{
    /**
     * Value linked to this node
     */
    Object _val;

    /**
     * Utility marker for traversals
     */
    boolean _marked;

    /**
     * List of more TreeNodes
     */
    List _child_lst;

    /**
     * List of parent TreeNodes
     */
    List _parent_lst;

    TreeNode()
    {
        _init();
    }

    TreeNode( Object val )
    {
        _init();
        setVal( val );
    }

    /**
     * just to limit the number of methods that depend on the class
     * (which makes copy and pasting and then editing a lot more
     * tedious)
     */
    protected void _init()
    {
        _child_lst = new ArrayList();
        _parent_lst = new ArrayList();
    }

    public int getChildCnt()
    {
        return _child_lst.size();
    }

    public int getOutDegree()
    {
        return getChildCnt();
    }

    Iterator getChildNodeIterator()
    {
        return _child_lst.iterator();
    }

    public Iterator getChildIterator()
    {
        return new TreeNodeValIterator( _child_lst );
    }

    public int getParentCnt()
    {
        return _parent_lst.size();
    }

    public int getInDegree()
    {
        return getParentCnt();
    }

    Iterator getParentNodeIterator()
    {
        return _parent_lst.iterator();
    }

    public Iterator getParentIterator()
    {
        return new TreeNodeValIterator( _parent_lst );
    }

    public void setVal( Object val )
    {
        _val = val;
    }

    public Object getVal()
    {
        return _val;
    }

    public String toString()
    {
	return String.valueOf( _val );
    }

    /**
     * Not a complete operation, thus cannot be public
     */
    protected boolean addChild( TreeNode child )
    {
        if( _child_lst.contains( child ) ) return false;

        return _child_lst.add( child );
    }

    /**
     * Not a complete operation, thus cannot be public
     */
    protected boolean addParent( TreeNode parent )
    {
        if( _parent_lst.contains( parent ) ) return false;

        return _parent_lst.add( parent );
    }

    /**
     * Not a complete operation, thus cannot be public
     */
    protected void removeChild( TreeNode child )
    {
        _child_lst.remove( child );
    }

    /**
     * Not a complete operation, thus cannot be public
     */
    protected void removeParent( TreeNode parent )
    {
        _parent_lst.remove( parent );
    }

    protected void removeAllParents()
    {
        _parent_lst = new ArrayList();
    }

    protected void removeAllChildren()
    {
	_child_lst = new ArrayList();
    }

    protected void mark()
    {
        setMarked( true );
    }

    protected void unmark()
    {
        setMarked( false );
    }
    
    protected void setMarked( boolean val )
    {
        _marked = val;
    }

    public boolean getMarked()
    {
        return _marked;
    }

    public boolean isMarked()
    {
        return getMarked();
    }
}

class TreeNodeValIterator implements Iterator
{
    Iterator _it;

    /**
     * Must be a list of TreeNodes
     */
    public TreeNodeValIterator( List lst )
    {
        _it = lst.iterator();
    }

    public boolean hasNext()
    {
        return _it.hasNext();
    }

    public Object next()
    {
        TreeNode node = (TreeNode)_it.next();
        return node.getVal();
    }

    public void remove()
    {
        _it.remove();
    }
}
