package name.subroutine.util;

import java.util.Map;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Stack;

/**
 * This is a graph that is implemented as a tree
 *
 * There is a special requirement that we have in our tree node -- the
 * values inserted must be unique to each other.  This way the user
 * can manipulate the graph without accessing the nodes themselves.
 */
public class TreeGraphDepthFirstIterator implements Iterator
{
    Iterator _current;
    Stack _history;

    /**
     * This is the node we return when next() is called.  If this 
     * value is null, then there is no next
     */
    TreeNode _next_node;

    /**
     * Must be a list of TreeNodes
     */
    public TreeGraphDepthFirstIterator( TreeNode root )
    {
        _current = root.getChildNodeIterator();
        _history = new Stack();

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
                    _history.push( _current );
                    _current = node.getChildNodeIterator();
                
                    node.mark();
                    return node;
                }
            }
            else{
                if( !_history.empty() ){
                    _current = (Iterator)_history.pop();
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
        Object o = _next_node.getVal();
        _next_node = prepareNext();
        return o;
    }

    public void remove()
    {
        throw new UnsupportedOperationException();
    }
}
