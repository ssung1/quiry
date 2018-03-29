package name.subroutine.util;

import java.util.*;

/**
 * This came from remus.rutgers.edu/cs112, summer of 2001
 * <pre>
 * Question to ask yourself:
 *
 *     If the elements are ordered, why make the list circular?
 *     Would that make it difficult to find the minimum/maximum value?
 *
 *     ,--> node --> node --> node --> node --> tail --.
 *     |                                               |
 *     `-----------------------------------------------'
 * 
 * We don't keep a pointer to the head, because head is always
 * tail.next().
 *
 *
 * In this list, we keep a universal "traveling node" that is used as
 * starting position for any type of iteration.  This means the list is
 * TOTALLY THREAD UNSAFE.  Don't let more than one thread accessing this
 * list at the same time or you'll be so sorry.
 * </pre>
 */
public class OrderedCircularDoublyLinkedList
{
    /**
     * Points to a node in the list, any node
     */
    public DoublyLinkedNode _node;
    public DoublyLinkedNode _head;
    public DoublyLinkedNode _tail;
    public int _size;

    public OrderedCircularDoublyLinkedList()
    {
	_node = _tail = null;
	_size = 0;
    }

    /**
     * push involves a change in the tail...
     * <pre>
     *
     *         A   -->  B   -->  C   --> ...
     *        tail --> node --> node --> ...
     *
     * becomes
     *
     *         A        n        B       C
     *        node --> tail --> node --> ...
     *
     * n stands for the new node
     * </pre>
     */
    public void push( Object o )
    {
	DoublyLinkedNode n;
	DoublyLinkedNode a;
	DoublyLinkedNode b;
	a = _tail;

	n = new DoublyLinkedNode();
	n.val( o );

	if( size() > 0 ){
	    // this would work if we had 1+ nodes
	    b = a.next();
	}
	else{
	    // if _tail == null, a.link( n ) would throw exception
	    a = b = _tail = _node = n;
	}

	a.link( n );
	n.link( b );
	// resetting tail
	_tail = n;
	_size++;
    }

    /**
     * Removes an element from the end and returns it
     * <pre>
     *
     *         A        B        C        D
     *        node --> tail --> node --> node --> ...
     *
     * becomes
     *
     *         A        C        D
     *        tail --> node --> node --> ...
     *
     * </pre>
     */
    public Object pop()
    {
	DoublyLinkedNode n;
	DoublyLinkedNode a;
	DoublyLinkedNode b;
	DoublyLinkedNode c;
	DoublyLinkedNode d;
	b = _tail;

	// only one node, so we have to set tail and head to null
	if( size() == 1 ){
	    n = _tail;
	    _tail = _node = null;
	}
	// throw exception?  or not...
	else if( size() <= 0 ){
	    return null;
	}
	// if we have 2 or more nodes...
	else{
	    c = _tail.next();
	    a = _tail.prev();

	    a.link( c );
	    n = b;
	    _tail = a;
	}
	_size--;
	return n.val();
    }

    /**
     * Returns element at the given index
     */
    public Object get( int idx )
    {
	if( size() <= 0 ){
	    throw new IndexOutOfBoundsException();
	}

	// when there is only one node, the zeroeth node is the tail
	// but when there are more than one nodes, the zeroeth node is
	// the node after the tail
	//
	// in either case this algorithm works
	int cnt;
	for( _node = _tail.next(), cnt = 0; true; cnt++ ){
	    if( idx == cnt ){
		return _node.val();
	    }

	    if( _node == _tail ) break;
	    _node = _node.next();
	}
	throw new IndexOutOfBoundsException();
    }

    /**
     * Returns the index of the given object or the negative value 
     * of the insertion point - 1 if the object is not found. 
     *
     * The reason we have to do negative value - 1 is because when the
     * size of the list is zero, we have to indicate that we should
     * insert at position "negative zero," which is indistinguishable
     * from "positive zero." By using negative value - 1, we can
     * return -1 to indicate that the value was not found and the
     * insertion point is zero.
     */
    public int search( Object o )
    {
	if( size() <= 0 ){
	    return -1;
	}

	DoublyLinkedNode n;
	int cnt;
	for( n = _tail.next(), cnt = 0; true; cnt++ ){
	    int diff;
	    try{
		Comparable a = (Comparable)o;
		Comparable b = (Comparable)n.val();
		diff = a.compareTo( b );
	    }
	    catch( Exception ex ){
		diff = String.valueOf( o ).compareTo(
		       String.valueOf( n.val() ) );
	    }

	    if( diff == 0 ){
		return cnt;
	    }
	    else if( diff < 0 ){
		return -(cnt + 1);
	    }

	    n = n.next();
	    if( n == _tail.next() ) break;
	}
	return -(_size + 1);
    }

    /**
     * Returns the node that contains the given object, by searching
     * through all the elements in the lists, assuming the elements
     * are unordered.
     */
    DoublyLinkedNode getNode( Object o )
    {
	if( size() <= 0 ){
	    return null;
	}

	DoublyLinkedNode n;
	int cnt;
	for( n = _tail.next(), cnt = 0; true; cnt++ ){
	    if( n.val().equals( o ) ){
		return n;
	    }

	    if( n == _tail ) break;
	    n = n.next();
	}
	return null;
    }

    /**
     * Returns the node that contains the given object, by searching
     * through a minimum number of elements, assuming all the elements
     * are in order
     *
     * <pre>
     *
     * We start at some random position, _node.
     *
     * If the node is larger than desired, go forward.
     * If the node is less than desired, go backward.
     * If the node is exactly as desired, return.
     *
     * We try again, looking at the _node,   <-------------------------.
     *                                                                 |
     * If the node is smaller than desired (but was larger before),    |
     * we know the node doesn't exist in our list.  Therefore we exit. |
     * Do the same if the situation is the othe way around.            |
     *                                                                 |
     * Also, if we pass through the "tail-head" threshold, we know     |
     * we can't find the desired node.                                 |
     *                                                                 |
     * Go back to -----------------------------------------------------'
     *
     * </pre>
     */
    public DoublyLinkedNode searchNode( Object o )
    {
	if( size() <= 0 ){
	    return null;
	}

	int dir = 0;
	int cnt;
	for( cnt = 0; true; cnt++ ){
	    Object aa = o;
	    Object bb = _node.val();
	    Comparable a;
	    Comparable b;

	    // Enhancement for the future:
	    // if the user has specified a comparator, use that!

	    int diff;
	    if( (aa instanceof Comparable) && (bb instanceof Comparable) ){
		a = (Comparable)aa;
		b = (Comparable)bb;
		diff = a.compareTo( b );
	    }
	    else{
		diff = String.valueOf( aa ).compareTo(
                       String.valueOf( bb ) );
	    }

	    if( _node.val().equals( o ) ){
		return _node;
	    }
	    else if( diff < 0 ){
		// first time -- we haven't established our direction of
		// travel yet
		//
		// so let's do that...
		//
		// this means the first comparison will dictate whether
		// we go "forward" or "backward".  We would not need to
		// travel in both directions during a search because all
		// the elements are ordered.
		if( cnt <= 0 ){
		    dir = -1;
		}
		// not first time -- we have to do two things:
		// 1. if the sign of the comparison (diff) is now
		//    opposite of our original direction established in
		//    the first comparison, we end our search with a
		//    not_found signal.
		// 2. if we reach tail (which happens to be the largest
		//    of the elements), we also end our search because
		//    it means we have just finished looking at the
		//    head, which was the smallest of elements.  And if
		//    our desired value is less than the smallest of 
		//    elements, we give up
		else{
		    if( dir > 0 ){
			return null;
		    }
		    if( _node == _tail ){
			return null;
		    }
		}
		_node = _node.prev();
	    }
	    // see the case where diff < 0
	    else if( diff > 0 ){
		if( cnt <= 0 ){
		    dir = 1;
		}
		else{
		    if( dir < 0 ){
			return null;
		    }
		    if( _node == _tail.next() ){
			return null;
		    }
		}
		_node = _node.next();
	    }
	}
    }

    /**
     * Used internally to get the node at a given index.  This can
     * later be used to manipulate the list
     */
    DoublyLinkedNode getNode( int idx )
    {
	DoublyLinkedNode n;
	int cnt;
	for( n = _head, cnt = 0; true; cnt++ ){
	    if( idx == cnt ){
		return n;
	    }

	    n = n.next();
	    if( n == _head ) break;
	}
	throw new IndexOutOfBoundsException();
    }

    public int size()
    {
	return _size;
    }

    /**
     * Inserts at the beginning of the list
     * <pre>
     *
     *      A   -->  B   -->  C   --> ...
     *     tail     node     node     
     *
     * becomes
     *
     *      A   -->  n   -->  B   -->  C    --> ...
     *     tail     node     node     node
     *                        
     * </pre>
     */
    public void unshift( Object o )
    {
	DoublyLinkedNode n;
	DoublyLinkedNode a;
	DoublyLinkedNode b;
	a = _tail;

	n = new DoublyLinkedNode();
	n.val( o );

	if( size() > 0 ){
	    // this would work if we had 1+ nodes
	    b = _tail.next();

	}
	else{
	    // if _tail == null, a.link( n ) would throw exception
	    a = b = _tail = _node = n;
	}

	a.link( n );
	n.link( b );

	// Note: we don't need to reset "head" since there is no head
	// (compare this with push)

	_size++;
    }

    /**
     * Removes an element from the top and returns it
     * <pre>
     *
     *         A        B        C        D
     *        node --> tail --> node --> node --> ...
     *
     * becomes
     *
     *         A        B        D
     *        node --> tail --> node --> ...
     *
     * </pre>
     */
    public Object shift()
    {
	DoublyLinkedNode n;
	DoublyLinkedNode a;
	DoublyLinkedNode b;
	DoublyLinkedNode c;
	DoublyLinkedNode d;
	b = _tail;

	// only one node, so we have to set tail and head to null
	if( size() == 1 ){
	    n = _tail;
	    _tail = _node = null;
	}
	// throw exception?  or not...
	else if( size() == 0 ){
	    return null;
	}
	// if we have 2 or more nodes...
	else{
	    c = _tail.next();
	    d = c.next();
	    b.link( d );
	    n = c;
	}
	_size--;
	return n.val();
    }

    /**
     * Insert object o at given position (pos)
     *
     * <pre>
     * During insertion, there are three main different scenarios:
     *     1. position is 0, in which case, _head must be updated
     *     2. position is size, in which case, _tail must be updated
     *     3. position is between 1 and size - 1, inclusive, in which
     *        case, neither _head or _tail should be updated.
     * </pre>
     */
    public void insert( Object o, int pos )
    {
	if( pos == 0 ){
	    // let unshift deal with the zero-size cases
	    unshift( o );
	    return;
	}
	if( pos == size() ){
	    // let push deal with the zero-size cases
	    push( o );
	    return;
	}

	DoublyLinkedNode n;
	DoublyLinkedNode nu;
	// if pos is invalid, exception will be thrown by getNode
	// so we don't have to worry about it
	n = getNode( pos );
	nu = new DoublyLinkedNode();
	nu.val( o );

	n.insert( nu );
	_size++;
    }

    /**
     * Insert an object according to the order
     */
    public void insertInOrder( Object o )
    {
	if( size() <= 0 ){
	    push( o );
	}
	DoublyLinkedNode n;
	n = new DoublyLinkedNode();
	n.val( o );

	int dir = 0;
	int cnt;
	for( cnt = 0; true; cnt++ ){
	    Object aa = o;
	    Object bb = _node.val();
	    Comparable a;
	    Comparable b;

	    // Enhancement for the future:
	    // if the user has specified a comparator, use that!
	    int diff;
	    if( (aa instanceof Comparable) && (bb instanceof Comparable) ){
		a = (Comparable)aa;
		b = (Comparable)bb;
		diff = a.compareTo( b );
	    }
	    else{
		diff = String.valueOf( aa ).compareTo(
                       String.valueOf( bb ) );
	    }

	    if( _node.val().equals( o ) ){
		_node.insert( n );
		_size++;
		return;
	    }
	    else if( diff < 0 ){
		// first time -- we haven't established our direction of
		// travel yet
		//
		// so let's do that...
		//
		// this means the first comparison will dictate whether
		// we go "forward" or "backward".  We would not need to
		// travel in both directions during a search because all
		// the elements are ordered.
		if( cnt <= 0 ){
		    dir = -1;
		}
		// not first time -- we have to do two things:
		// 1. if the sign of the comparison (diff) is now
		//    opposite of our original direction established in
		//    the first comparison, we end our search with a
		//    not_found signal.
		// 2. if we reach tail (which happens to be the largest
		//    of the elements), we also end our search because
		//    it means we have just finished looking at the
		//    head, which was the smallest of elements.  And if
		//    our desired value is less than the smallest of 
		//    elements, we give up
		else{
		    if( dir > 0 ){
			_node.insert( n );
			_size++;
			return;
		    }
		    if( _node == _tail ){
			//_node.next().insert( n );
			//_size++;
			unshift( o );
			return;
		    }
		}
		_node = _node.prev();
	    }
	    // see the case where diff < 0
	    else if( diff > 0 ){
		if( cnt <= 0 ){
		    dir = 1;
		}
		else{
		    if( dir < 0 ){
			_node.next().insert( n );
			_size++;
			return;
		    }
		    if( _node == _tail.next() ){
			//_node.insert( n );
			//_size++;
			push( o );
			return;
		    }
		}
		_node = _node.next();
	    }
	}

	/*
	 *
	// or we can just do this
	int pos;
	pos = search( o );
	if( pos < 0 ){
	    pos = (-pos) - 1;
	}
	insert( o, pos );
	 *
	 */
    }

    /**
     * Insert an object according to the order
     */
    public void remove( Object o )
    {
	if( size() <= 0 ){
	    // if you like exceptions and you know it,
	    // throw new NoSuchElementException
	    return;
	}

	DoublyLinkedNode n;

	int dir = 0;
	int cnt;
	for( cnt = 0; true; cnt++ ){
	    Object aa = o;
	    Object bb = _node.val();
	    Comparable a;
	    Comparable b;

	    // Enhancement for the future:
	    // if the user has specified a comparator, use that!
	    int diff;
	    if( (aa instanceof Comparable) && (bb instanceof Comparable) ){
		a = (Comparable)aa;
		b = (Comparable)bb;
		diff = a.compareTo( b );
	    }
	    else{
		diff = String.valueOf( aa ).compareTo(
                       String.valueOf( bb ) );
	    }

	    if( _node.val().equals( o ) ){
		_node.prev().link( _node.next() );
		_size--;
		return;
	    }
	    else if( diff < 0 ){
		// first time -- we haven't established our direction of
		// travel yet
		//
		// so let's do that...
		//
		// this means the first comparison will dictate whether
		// we go "forward" or "backward".  We would not need to
		// travel in both directions during a search because all
		// the elements are ordered.
		if( cnt <= 0 ){
		    dir = -1;
		}
		// not first time -- we have to do two things:
		// 1. if the sign of the comparison (diff) is now
		//    opposite of our original direction established in
		//    the first comparison, we end our search with a
		//    not_found signal.
		// 2. if we reach tail (which happens to be the largest
		//    of the elements), we also end our search because
		//    it means we have just finished looking at the
		//    head, which was the smallest of elements.  And if
		//    our desired value is less than the smallest of 
		//    elements, we give up
		else{
		    if( dir > 0 ){
			return;
		    }
		    if( _node == _tail ){
			return;
		    }
		}
		_node = _node.prev();
	    }
	    // see the case where diff < 0
	    else if( diff > 0 ){
		if( cnt <= 0 ){
		    dir = 1;
		}
		else{
		    if( dir < 0 ){
			return;
		    }
		    if( _node == _tail.next() ){
			return;
		    }
		}
		_node = _node.next();
	    }
	}
    }

    /**
     * Creates an enumeration.
     */
    public Enumeration forwardElements()
    {
	ForwardOrderedCircularEnumeration en;
	en = new ForwardOrderedCircularEnumeration();
	en._size = size();
	if( size() > 0 ){
	    en._node = _tail.next();
	}
	else{
	    en._node = null;
	}
	return en;
    }

    /**
     * Creates an enumeration.
     */
    public Enumeration reverseElements()
    {
	ReverseOrderedCircularEnumeration en;
	en = new ReverseOrderedCircularEnumeration();
	en._size = size();
	if( size() > 0 ){
	    en._node = _tail;
	}
	else{
	    en._node = null;
	}
	return en;
    }

    public String toString()
    {
	StringBuffer sb;
	sb = new StringBuffer();

	if( size() <= 0 ){
	    sb.append( "[none]" );
	    return sb.toString();
	}

	DoublyLinkedNode node;
	node = _tail.next();

	while( true ){
	    sb.append( String.valueOf( node.val() ) );
	    if( node == _tail ) break;
	    sb.append( ", " );
	    node = node.next();
	}

	return sb.toString();
    }
}

class ForwardOrderedCircularEnumeration implements Enumeration
{
    DoublyLinkedNode _node;
    int _size;
    int _cnt;
    
    public ForwardOrderedCircularEnumeration()
    {
	_cnt = 0;
    }
    public boolean hasMoreElements()
    {
	return _cnt < _size;
    }
    public Object nextElement()
    {
	Object o;
	o = _node.val();
	_node = _node.next();
	_cnt++;
	
	return o;
    }
}

class ReverseOrderedCircularEnumeration implements Enumeration
{
    DoublyLinkedNode _node;
    int _size;
    int _cnt;
    
    public ReverseOrderedCircularEnumeration()
    {
	_cnt = 0;
    }
    public boolean hasMoreElements()
    {
	return _cnt < _size;
    }
    public Object nextElement()
    {
	Object o;
	o = _node.val();
	_node = _node.prev();
	_cnt++;
	
	return o;
    }
}
