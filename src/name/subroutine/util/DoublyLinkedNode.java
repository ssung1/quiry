package name.subroutine.util;

public class DoublyLinkedNode
{
    public DoublyLinkedNode _prev;
    public DoublyLinkedNode _next;
    public Object _val;

    public DoublyLinkedNode prev( DoublyLinkedNode n )
    {
	_prev = n;
	return n;
    }
    public DoublyLinkedNode prev()
    {
	return _prev;
    }
    public DoublyLinkedNode next( DoublyLinkedNode n )
    {
	_next = n;
	return n;
    }
    public DoublyLinkedNode next()
    {
	return _next;
    }
    public Object val()
    {
	return _val;
    }
    public DoublyLinkedNode val( Object v )
    {
	_val = v;
	return this;
    }

    /**
     * Links n after the current node.  This method makes a complete link,
     * altering both nodes.
     *
     * @return n for the convenience of the user.
     */
    public DoublyLinkedNode link( DoublyLinkedNode n )
    {
	next( n );
	n.prev( this );

	return n;
    }

    /**
     * Inserts at current node and pushes the current node "back."
     * 
     * <pre>
     *          prev     -->    this     -->    next
     *
     * becomes
     *
     *          prev     -->     n       -->    this     -->    next
     *
     * </pre>
     *
     * where n is the new node to insert
     *
     * @return n for the convenience of the user.  Give me convenience
     * or give me death!  -- Patrick Henry
     */
    public DoublyLinkedNode insert( DoublyLinkedNode n )
    {
	DoublyLinkedNode prev;
	prev = prev();

	if( prev != null ){
	    prev.link( n );
	}
	// if n is null, then somebody's being stupid
        n.link( this );

	return n;
    }

    /**
     * Same as insert, but since we have an append, we should have a
     * prepend 
     */
    public DoublyLinkedNode prepend( DoublyLinkedNode n )
    {
	return insert( n );
    }

    /**
     * Appends at current node and pushes the current node "forward."
     * 
     * <pre>
     *                          prev     -->    this     -->    next
     *
     * becomes
     *
     *          prev     -->    this     -->     n       -->    next
     *
     * where n is the new node to insert
     *
     * </pre>
     *
     * @return n for the convenience of the user.  Give me convenience
     * or give me death!  -- Patrick Henry
     */
    public DoublyLinkedNode append( DoublyLinkedNode n )
    {
	DoublyLinkedNode prev;
	prev = prev();

	if( prev != null ){
	    prev.link( n );
	}
	// if n is null, then somebody's being stupid
        n.link( this );

	return n;
    }
}
