package name.subroutine.htmler;

import java.util.*;
import java.io.*;


public class HTMLInputTable extends HTMLTable
{
    List _type_lst = new ArrayList();
    int[] _input_size_array = null;
    int _shoulder_cnt = 0;
    String _root = "input";

    public HTMLInputTable()
    {
    }

    public HTMLInputTable( int col_cnt )
    {
	_tag = "table";
	_col_cnt = col_cnt;
    }
    public HTMLInputTable( List content )
    {
	_tag = "table";
	_item_lst = content;
    }
    public HTMLInputTable( Object[] content )
    {
	_tag = "table";
	_item_lst.addAll( java.util.Arrays.asList( content ) );
    }
    public String toString()
    {
	StringBuffer sb = new StringBuffer();

	int col_cnt = _col_cnt;

	// silly lil fix
	if( col_cnt <= 0 ){
	    col_cnt = _item_lst.size();
	}

	int row;
	int i;
	for( i = 0; i < col_cnt; i++ ){
	    try{
		HTMLElement col = new HTMLElement( "col" );

		Object obj;
		obj = _col_attr_lst.get( i % _col_attr_lst.size() );

		if( obj instanceof Collection ){
		    col.addAttr( (Collection)obj );
		}
		else{
		    col.addAttr( (Object[])obj );
		}

		sb.append( col );
	    }
	    catch( Exception ex ){
	    }
	}

	for( i = 0, row = 0; i < _item_lst.size(); i += col_cnt, row++ ){
	    HTMLContainer tr;

	    if( row < _header_cnt ){
		tr = createTr( _item_lst, i, col_cnt, _header_template );
	    }
	    else{
		tr = createInputTr( _item_lst, i, col_cnt, _cell_template );
	    }
	    
	    sb.append( tr );
	    sb.append( "\n" );
	}

	int total;
	total = _item_lst.size();

	total -= _header_cnt * col_cnt;
	total -= _shoulder_cnt * (row - _header_cnt);

	HTMLElement count = HTMLElementFactory.createHiddenField( 
            _root + "Cnt",
            new Integer( total ), 0 );

	sb.append( count );

	return super.toString( sb );
    }

    public void setType( Object[] t )
    {
	setType( java.util.Arrays.asList( t ) );
    }
    public void setType( List t )
    {
	_type_lst.clear();
	_type_lst.addAll( t );
    }
    public void setInputSize( int[] s )
    {
	_input_size_array = s;
    }
    /**
     * Set number of columns as shoulder (not to display input control)
     */
    public void setShoulderCnt( int s )
    {
	_shoulder_cnt = s;
    }
    /**
     * Set the root of the input field names.  The input fields will
     * be named Root0, Root1, Root2, and so on
     */
    public void setRoot( String s )
    {
	_root = s;
    }

    /**
     * Creates a row from a list, starting from offset 
     */
    HTMLContainer createInputTr( List list, int off, int size,
				 HTMLElement templ )
    {
	HTMLContainer elem;
	elem = new HTMLContainer( "tr" );
	for( int i = 0; i < size; i++ ){
	    Object o;
	    try{
		o = list.get( i + off );
	    }
	    catch( IndexOutOfBoundsException ex ){
		break;
	    }

	    HTMLElement cell;
	    cell = templ.dup();

	    cell.setTag( "td" );

	    if( i < _shoulder_cnt ){
		if( String.valueOf( o ).trim().length() <= 0 ){
		    cell.setContent( "<br/>" );
		}
		else{
		    cell.setContent( o );
		}
		elem.add( cell );
		continue;
	    }

	    String type;
	    try{ 
		type = String.valueOf( _type_lst.get( i % _type_lst.size() ) );
	    }
	    catch( Exception ex ){
		type = "text";
	    }

	    int input_size;
	    try{
		input_size = _input_size_array[ i % _input_size_array.length ];
	    }
	    catch( Exception ex ){
		input_size = 16;
	    }

	    String root;
	    root = _root + ( ( i + off ) -
                             ( size * _header_cnt ) -
                             ( ( i + off ) / size + 1 ) * _shoulder_cnt );

	    // people are the worst...
	    HTMLElement inp;
	    inp = HTMLElementFactory.create( type, root, o,
					     input_size );

	    cell.setContent( inp );

	    elem.add( cell );
	}
	return elem;
    }

    public static List parseInput( Map map, String root )
    {
	List list = new ArrayList();

	int cnt = -1;
	try{
	    cnt = Integer.parseInt( (String)map.get( root + "Cnt" ) );
	}
	catch( Exception ex ){
	}

	int i;
	for( i = 0; true; i++ ){
	    String key;
	    key = root + i;

	    Object o;
	    o = map.get( key );

	    if( cnt >= 0 ){
		if( i >= cnt ) break;

		list.add( o );
	    }
	    else{
		if( o == null ){
		    break;
		}
		else{
		    list.add( o );
		}
	    }
	}
	return list;
    }
}
