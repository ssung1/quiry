package name.subroutine.util;

import java.util.Map;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Stack;
import java.util.Set;

/**
 * This is a graph that is created by sending edges.  The user does
 * not know the topology of the graph, only the relations between
 * nodes.
 *
 * There is a special requirement that we have in our tree node -- the
 * values inserted must be unique to each other.  This way the user
 * can manipulate the graph without accessing the nodes themselves.
 */
public class EdgeGraph extends TreeGraph
{
    public EdgeGraph()
    {
        _root = new GraphNode( "_root" );
        _node_map = new HashMap();
    }

    public TreeNode createNode( Object val )
    {
        GraphNode node = new GraphNode( val );
        _node_map.put( val, node );

        return node;
    }

    /**
     * Adds an object to the root
     */
    public boolean add( Object number_one )
    {
        return add( number_one, 0, null );
    }

    /**
     * Adds an object to the root
     */
    public boolean add( Object number_one, int type, Object tag )
    {
        GraphNode root = (GraphNode)getRoot();

        boolean changed = false;
        GraphNode node = (GraphNode)getNode( number_one );
        if( node == null ){
            node = (GraphNode)createNode( number_one );
            changed = true;
        }
        changed = root.addChild( node, type, tag ) || changed;
        changed = node.addParent( root, type, tag ) || changed;

        return changed;
    }

    /**
     * Adds the edge from parent to child.
     * 
     * If parent or child does not exist in the graph already, it will
     * be added also
     *
     * @param type an optional type flag for the edge
     * @param tag an optional tag for the edge.  The tag is used to
     * determine edge equality (in addition to destination node)
     */
    public boolean add( Object parent, Object child, int type, Object tag )
    {
        boolean changed = false;
        GraphNode node = (GraphNode)getNode( child );
        if( node == null ){
            node = (GraphNode)createNode( child );
            changed = true;
        }

        GraphNode p = (GraphNode)getNode( parent );
        if( p == null ){
            p = (GraphNode)createNode( parent );
            changed = true;
        }

        changed = p.addChild( node, type, tag ) || changed;
        changed = node.addParent( p, type, tag ) || changed;

        return changed;
    }

    /**
     * @return child edges from root
     */
    public Iterator getChildEdgeIterator()
    {
        return ((GraphNode)getRoot()).getChildEdgeIterator();
    }

    /**
     * @return edges from parent
     */
    public Iterator getChildEdgeIterator( Object parent )
    {
        GraphNode p = (GraphNode)getNode( parent );
        return p.getChildEdgeIterator();
    }

    /**
     * @return edges that leads to child
     */
    public Iterator getParentEdgeIterator( Object child )
    {
        GraphNode c = (GraphNode)getNode( child );
        return c.getParentEdgeIterator();
    }

    /**
     * adds orphans to the root node
     */
    public void addOrphanSet()
    {
        GraphNode root = (GraphNode)getRoot();
        Set orphan_set = getOrphanSet();
        for( Iterator it = orphan_set.iterator(); it.hasNext(); ){
            GraphNode node = (GraphNode)it.next();
            root.addChild( node, 0, null );
            node.addParent( root, 0, null );
        }
    }
}
