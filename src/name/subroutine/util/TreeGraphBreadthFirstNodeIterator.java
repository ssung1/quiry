package name.subroutine.util;

import java.util.Map;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;

/**
 * This iterator traverses one layer at a time
 */
public class TreeGraphBreadthFirstNodeIterator implements Iterator
{
    Iterator _current;
    LinkedList _history;

    /**
     * This is the node we return when next() is called.  If this 
     * value is null, then there is no next
     */
    TreeNode _next_node;

    /**
     * Must be a list of TreeNodes
     */
    public TreeGraphBreadthFirstNodeIterator( TreeNode root )
    {
        _current = root.getChildNodeIterator();
        _history = new LinkedList();

        _next_node = prepareNext();
    }

    public boolean hasNext()
    {
        return (_next_node != null);
    }

    protected TreeNode prepareNext()
    {
        while( true ){
            if( _current.hasNext() ){
                TreeNode node;
                node = (TreeNode)_current.next();
                
                // already been there, try next one
                if( node.getMarked() ){
                    continue;
                }
                else{
                    //_history.push( _current );
                    //_current = node.getChildNodeIterator();
                
                    _history.add( node.getChildNodeIterator() );

                    node.mark();
                    return node;
                }
            }
            else{
                if( !_history.isEmpty() ){
                    // FIFO!
                    // FIVO
                    // VIVO
                    // Velveeta?
                    _current = (Iterator)_history.removeFirst();
                    continue;
                }
                else{
                    return null;
                }
            }
        }
    }

    public Object next()
    {
        Object o = _next_node;
        _next_node = prepareNext();
        return o;
    }

    public void remove()
    {
        throw new UnsupportedOperationException();
    }
}
