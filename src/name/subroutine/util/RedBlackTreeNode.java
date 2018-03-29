package name.subroutine.util;

/**
 * The node is a recursive structure
 *
 * The tree is just a container for the root node
 *
 * This is a little different than what you see in the books in
 * terminology, but the concept is identical
 */
public class RedBlackTreeNode
{
    public static final byte BLACK = 10;
    public static final byte RED = 11;

    byte _color;

    boolean _marked;

    RedBlackTreeNode _left;

    RedBlackTreeNode _right;

    RedBlackTreeNode _parent;

    Object _val;

    public RedBlackTreeNode()
    {
        _init();
    }

    public RedBlackTreeNode( Object val )
    {
        _init( val );
    }

    protected void _init()
    {
        _marked = false;
        _left = null;
        _right = null;
        _parent = null;
        _val = null;
        _color = RED;
    }

    protected void _init( Object val )
    {
        _init();
        _val = val;
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

    public void setVal( Object val )
    {
        _val = val;
    }

    public Object getVal()
    {
        return _val;
    }

    public void setLeft( RedBlackTreeNode node )
    {
        _left = node;
    }

    public RedBlackTreeNode getLeft()
    {
        return _left;
    }

    public void setRight( RedBlackTreeNode node )
    {
        _right = node;
    }

    public RedBlackTreeNode getRight()
    {
        return _right;
    }

    public void setParent( RedBlackTreeNode node )
    {
        _parent = node;
    }

    public RedBlackTreeNode getParent()
    {
        return _parent;
    }

    public void setColor( byte val )
    {
        _color = val;
    }

    public byte getColor()
    {
        return _color;
    }

    public void setColorBlack()
    {
        setColor( BLACK );
    }
    
    public void setColorRed()
    {
        setColor( RED );
    }

    public boolean isBlack()
    {
        return getColor() == BLACK;
    }

    public boolean isRed()
    {
        return getColor() == RED;
    }

    public boolean isRightChild()
    {
        return (!isRoot() && _parent.getRight() == this);
    }

    public boolean isLeftChild()
    {
        return (!isRoot() && _parent.getLeft() == this);
    }

    /**
     * Will throw exception if "this" is root
     */
    RedBlackTreeNode getSibling()
    {
        if( isLeftChild() ){
            return getParent().getRight();
        }
        else{
            return getParent().getLeft();
        }
    }

    public boolean isRoot()
    {
        return (_parent == null);
    }

    boolean isLeaf()
    {
        return getLeft() == null && getRight() == null;
    }

    /**
     * Returns greatest node that is lesser than "this"
     *
     * Or null if this is the smallest node
     */
    public RedBlackTreeNode getPrev()
    {
        RedBlackTreeNode c = getLeft();
        if( c != null ) return c.getRightmostDescendent();

        RedBlackTreeNode p = this;
        while( true ){
            if( p.isRightChild() ){
                return p.getParent();
            }
            else if( p.isRoot() ){
                return null;
            }
            else{
                p = p.getParent();
            }
        }
    }

    /**
     * Returns smallest node that is greater than "this"
     *
     * Or null if this is the greatest node
     */
    public RedBlackTreeNode getNext()
    {
        RedBlackTreeNode c = getRight();
        if( c != null ) return c.getLeftmostDescendent();

        RedBlackTreeNode p = this;
        while( true ){
            if( p.isLeftChild() ){
                return p.getParent();
            }
            else if( p.isRoot() ){
                return null;
            }
            else{
                p = p.getParent();
            }
        }
    }

    /**
     * Returns the rightmost node possible, including current node
     */
    public RedBlackTreeNode getRightmostDescendent()
    {
        RedBlackTreeNode r = getRight();
        if( r == null ) return this;
        return r.getRightmostDescendent();
    }

    /**
     * Returns the leftmost node possible, including current node
     */
    public RedBlackTreeNode getLeftmostDescendent()
    {
        RedBlackTreeNode l = getLeft();
        if( l == null ) return this;
        return l.getLeftmostDescendent();
    }

    /**
     * rotates to the left about "this" node (b)
     *
     *                  P                    P
     *                 *                    *
     *                b                    d
     *               * *         ===>     *
     *              a   d                b
     *                 *                * *
     *                c                a   c
     *
     * note the sort order of the nodes is preserved
     *
     * current node must have a right child or the method will
     * throw null pointer exception
     *
     * this method is not complete without updating link between
     * parent node and heir
     *
     * @return the heir (d)
     */
    public RedBlackTreeNode rotateLeft()
    {
        RedBlackTreeNode heir = getRight();
        // the orphan is the heir's original child
        //
        // when the original parent becomes the new child of the heir,
        // the original child becomes orphaned
        //
        // the original parent then adopts the orphan
        RedBlackTreeNode orphan = heir.getLeft();
        setRight( orphan );
        if( orphan != null ){
            orphan.setParent( this );
        }
        
        heir.setLeft( this );
        setParent( heir );

        return heir;
    }

    /**
     * rotates to the right about the given node (b)
     *
     *                  P                    P
     *                 *                    *
     *                b                    a
     *               * *         ===>       *
     *              a   d                    b
     *               *                      * *
     *                c                    c   d
     *
     * note the sort order of the nodes is preserved
     *
     * current node must have a right child or the method will
     * throw null pointer exception
     *
     * this method is not complete without updating link between
     * parent node and heir
     *
     * @return the heir (a)
     */
    RedBlackTreeNode rotateRight()
    {
        RedBlackTreeNode heir = getLeft();
        // the orphan is the heir's original child
        //
        // when the original parent becomes the new child of the heir,
        // the original child becomes orphaned
        //
        // the original parent then adopts the orphan
        RedBlackTreeNode orphan = heir.getRight();
        setLeft( orphan );
        if( orphan != null ){
            orphan.setParent( this );
        }

        heir.setRight( this );
        setParent( heir );

        return heir;
    }

    public String toString()
    {
	return String.valueOf( _val );
    }
}
