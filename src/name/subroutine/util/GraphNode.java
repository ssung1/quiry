package name.subroutine.util;

import java.util.Map;
import java.util.HashMap;
import java.util.List;
import java.util.ArrayList;
import java.util.Iterator;


/**
 * A graph node differs from a tree node by having extra
 * information in the edge.  A tree does not have interesting
 * edges; all the fun is in the nodes.
 *
 * In child list and parent list, the content is not GraphNode
 * but GraphEdge.  Each GraphEdge will then link to a GraphNode.
 */
public class GraphNode extends TreeNode
{
    GraphNode()
    {
        super();
    }

    GraphNode( Object val )
    {
        super( val );
    }

    public Iterator getChildEdgeIterator()
    {
        return _child_lst.iterator();
    }

    Iterator getChildNodeIterator()
    {
        return new GraphNodeIterator( _child_lst );
    }

    public Iterator getChildIterator()
    {
        return new GraphNodeValIterator( _child_lst );
    }

    public Iterator getParentEdgeIterator()
    {
        return _parent_lst.iterator();
    }

    Iterator getParentNodeIterator()
    {
        return new GraphNodeIterator( _parent_lst );
    }

    public Iterator getParentIterator()
    {
        return new GraphNodeValIterator( _parent_lst );
    }

    /**
     * Always creates a new edge
     */
    protected GraphEdge getEdge( GraphNode dst, int type, Object edgetag )
    {
        GraphEdge edge = new GraphEdge();
        edge.setDestinationNode( dst );
        edge.setType( type );
        edge.setVal( edgetag );

        return edge;
    }

    /**
     * Creates an edge of type zero
     */
    protected GraphEdge getEdge( TreeNode dst )
    {
        return getEdge( (GraphNode)dst, 0, null );
    }

    protected boolean addChild( TreeNode child )
    {
        GraphEdge edge = getEdge( child );

        if( _child_lst.contains( edge ) ) return false;

        return _child_lst.add( edge );
    }

    protected boolean addChild( GraphNode child, int type, Object edgetag )
    {
        GraphEdge edge = getEdge( child, type, edgetag );

        if( _child_lst.contains( edge ) ) return false;

        return _child_lst.add( edge );
    }

    protected void removeChild( TreeNode child )
    {
        Iterator it;
        it = getChildNodeIterator();
        for( ; it.hasNext(); ){
            GraphNode node = (GraphNode)it.next();

            if( child == node ){
                it.remove();
            }
        }
    }

    protected boolean addParent( TreeNode parent )
    {
        GraphEdge edge = getEdge( parent );

        if( _parent_lst.contains( edge ) ) return false;

        return _parent_lst.add( edge );
    }

    protected boolean addParent( GraphNode parent, int type, Object edgetag )
    {
        GraphEdge edge = getEdge( parent, type, edgetag );

        if( _parent_lst.contains( edge ) ) return false;

        return _parent_lst.add( edge );
    }

    protected void removeParent( TreeNode parent )
    {
        Iterator it;
        it = getParentNodeIterator();
        for( ; it.hasNext(); ){
            GraphNode node = (GraphNode)it.next();

            if( parent == node ){
                it.remove();
            }
        }
    }
}

class GraphNodeValIterator extends GraphNodeIterator
{
    /**
     * Must be a list of GraphEdges
     */
    public GraphNodeValIterator( List lst )
    {
        super( lst );
    }

    public Object next()
    {
        GraphNode node = (GraphNode)super.next();
        return node.getVal();
    }
}

class GraphNodeIterator extends TreeNodeValIterator
{
    /**
     * Must be a list of GraphEdges
     */
    public GraphNodeIterator( List lst )
    {
        super( lst );
    }

    public Object next()
    {
        GraphEdge edge = (GraphEdge)_it.next();
        GraphNode node = edge.getDestinationNode();
        return node;
    }
}
