package name.subroutine.htmler;

import java.util.*;
import java.io.*;

/**
 * This is an HTML element that can contain other HTML elements...
 *
 * (which is sort of like every HTML element there is, but this is
 * is used for pure container elements, which cannot contain 
 * "text" nodes)
 */
public class HTMLContainer extends HTMLElement
{
    List _item_lst = new ArrayList();

    public HTMLContainer()
    {
	super();
    }
    public HTMLContainer( String tag, List content )
    {
	super();
	_tag = tag;
	_item_lst = content;
    }
    public HTMLContainer( String tag, Object[] content )
    {
	super();
	_tag = tag;
	_item_lst.addAll( java.util.Arrays.asList( content ) );
    }
    public HTMLContainer( String tag )
    {
	super();
	_tag = tag;
    }

    /**
     * Get the underlying components...one may alter the returned
     * item directly to modify the container
     */
    public List getComponents()
    {
	return _item_lst;
    }
    public void setComponents( Collection items )
    {
	_item_lst.clear();
	addAll( items );
    }
    public void setComponents( Object[] items )
    {
	_item_lst.clear();
	addAll( items );
    }

    public Object get( int i )
    {
	return _item_lst.get( i );
    }
    public void add( Object o )
    {
	_item_lst.add( o );
    }
    public void addAll( Object[] o )
    {
	_item_lst.addAll( java.util.Arrays.asList( o ) );
    }
    public void addAll( Collection o )
    {
	_item_lst.addAll( o );
    }
    public void clear()
    {
	_item_lst.clear();
    }
    public int size()
    {
	return _item_lst.size();
    }

    public String toString()
    {
	StringBuffer sb = new StringBuffer();

	Iterator it;
	for( it = _item_lst.iterator(); it.hasNext(); ){
	    Object o = it.next();
            if( o == null ){
                sb.append( _null );
            }
            else{
                sb.append( o );
            }

            sb.append( System.getProperty( "line.separator" ) );
	}

	return super.toString( sb );
    }

    void toPrintContent( PrintWriter pw )
    {
	Iterator it;
	for( it = _item_lst.iterator(); it.hasNext(); ){
	    Object o = it.next();
            toPrintContent( pw, o );
	}
    }
}
