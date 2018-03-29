package name.subroutine.util;

import java.util.Map;
import java.util.HashMap;
import java.util.List;
import java.util.ArrayList;
import java.util.Iterator;

/**
 * This represents an edge in a graph
 */
public class GraphEdge
{
    /**
     * type of the edge
     */
    int _type;

    /**
     * value of the edge...can be a name of sorts
     */
    Object _val;

    /**
     * destination of the edge
     */
    GraphNode _destination_node;

    public boolean equals( Object o )
    {
        if( o instanceof GraphEdge ){
            GraphEdge ed = (GraphEdge)o;

            if( getType() != ed.getType() ) return false;
            if( getDestinationNode() != ed.getDestinationNode() ) return false;

	    Object a = getVal();
	    Object b = ed.getVal();

	    if( getVal() != null &&
                !getVal().equals( ed.getVal() ) ) return false;
	    if( getVal() == null && ed.getVal() != null ) return false;

            return true;
        }

        return false;
    }

    public Object getDestination()
    {
        GraphNode gn;
        gn = getDestinationNode();
        if( gn == null ) return null;

        return gn.getVal();
    }

    //---------------------Constructors-----------------
    public GraphEdge()
    {
    }

    //---------------------Accessors--------------------
    public int getType()
    {
        return _type;
    }

    public String strGetType()
    {
        return String.valueOf( getType() ).trim();
    }

    public java.lang.Object getVal()
    {
        return _val;
    }

    public String strGetVal()
    {
        return String.valueOf( getVal() ).trim();
    }

    public name.subroutine.util.GraphNode getDestinationNode()
    {
        return _destination_node;
    }

    public String strGetDestinationNode()
    {
        return String.valueOf( getDestinationNode() ).trim();
    }

    //---------------------Mutators---------------------
    public void setType( int val )
    {
        this._type = val;
    }

    public void setType( String val )
    {
        setType( Integer.parseInt( val.trim() ) );
    }

    public void setVal( java.lang.Object val )
    {
        this._val = val;
    }

    public void setDestinationNode( name.subroutine.util.GraphNode val )
    {
        this._destination_node = val;
    }
}
