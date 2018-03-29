package name.subroutine.htmler;

import java.util.*;
import java.io.*;
import name.subroutine.util.Variant;


public abstract class AbstractElement implements Element, Cloneable
{
    public String _tag;
    public List _attr_lst = new ArrayList();
    public List _style_lst = new ArrayList();;

    public Object _content = "";

    /**
     * This is the string that should be used to represent
     * null
     */
    public String _null = "";

    public void setNullString( String n )
    {
        _null = n;
    }
    public String getNullString()
    {
        return _null;
    }

    public void setContent( Object o )
    {
	_content = o;
    }

    public Object getContent()
    {
	return _content;
    }

    public String strGetStyle()
    {
	StringBuffer buf = new StringBuffer();

	buf.append( "style = \"" );

	int i;
	try{
	    for( i = 0; i < _style_lst.size(); i += 2 ){
		buf.append( _style_lst.get( i ) );
		buf.append( ": " );
		buf.append( _style_lst.get( i + 1 ) );
		buf.append( "; " );
	    }
	}
	catch( Exception ex ){
	    buf.append( ";" );
	}
	buf.append( "\"" );

	return buf.toString();
    }

    public String strGetAttr()
    {
	StringBuffer buf = new StringBuffer();
	int i;
	try{
	    for( i = 0; i < _attr_lst.size(); i += 2 ){
		if( i > 0 ){
		    buf.append( " " );
		}
		buf.append( _attr_lst.get( i ) );
		buf.append( "=" );
		buf.append( '"' );
		String tmp;
		tmp = String.valueOf( _attr_lst.get( i + 1 ) );
		buf.append( encode( tmp ) );
		buf.append( '"' );
	    }
	}
        catch( Exception ex ){
	}

	return buf.toString();
    }

    public void setStyle( Object[] s )
    {
	_style_lst.clear();
	addStyle( s );
    }
    public void setStyle( Collection s )
    {
	_style_lst.clear();
	addStyle( s );
    }
    public void addStyle( Object[] s )
    {
	addStyle( java.util.Arrays.asList( s ) );
    }
    public void addStyle( Collection s )
    {
	_style_lst.addAll( s );
    }
    public void addStyle( Object key, Object val )
    {
        _style_lst.add( key );
        _style_lst.add( val );
    }

    public void setAttr( Object[] s ){
	_attr_lst.clear();
	addAttr( s );
    }
    public void addAttr( Object[] s )
    {
	addAttr( java.util.Arrays.asList( s ) );
    }
    public void setAttr( Collection s )
    {
	_attr_lst.clear();
	addAttr( s );
    }
    public void addAttr( Collection s )
    {
	_attr_lst.addAll( s );
    }
    public void addAttr( Object key, Object val )
    {
        _attr_lst.add( key );
        _attr_lst.add( val );
    }
    public List getAttr()
    {
	return _attr_lst;
    }

    public void setTag( String tag )
    {
	_tag = tag;
    }
    public String getTag()
    {
	return _tag;
    }

    public String toString()
    {
	return toString( _content );
    }

    public String toString( Object content )
    {
	if( _tag == null || _tag.trim().length() <= 0 ){
	    return String.valueOf( content );
	}

	StringBuffer sb = new StringBuffer();
	sb.append( "<" ).append( _tag );

	if( _style_lst.size() > 0 ){
	    sb.append( " " );
	    sb.append( strGetStyle() );
	}

	if( _attr_lst.size() > 0 ){
	    sb.append( " " );
	    sb.append( strGetAttr() );
	}

	sb.append( ">" );
        sb.append( System.getProperty( "line.separator" ) );
        if( content == null ){
            sb.append( _null );
        }
        else{
            sb.append( String.valueOf( content ) );
        }
        sb.append( System.getProperty( "line.separator" ) );
	sb.append( Variant.grow( "/" + _tag, "<" ) );

	return sb.toString();
    }

    /**
     * Prints the opening tag
     */
    void toPrintOpeningTag( PrintWriter pw )
    {
	if( _tag == null || _tag.trim().length() <= 0 ){
	    return;
	}

        // we still use a little string buffer just in case the
        // printwriter is not buffered
        StringBuffer sb = new StringBuffer();

	sb.append( "<" ).append( _tag );

	if( _style_lst.size() > 0 ){
	    sb.append( " " );
	    sb.append( strGetStyle() );
	}

	if( _attr_lst.size() > 0 ){
	    sb.append( " " );
	    sb.append( strGetAttr() );
	}

	sb.append( ">" );

        pw.print( sb );
    }

    /**
     * Prints the closing tag
     */
    void toPrintClosingTag( PrintWriter pw )
    {
	if( _tag == null || _tag.trim().length() <= 0 ){
	    return;
	}

        StringBuffer sb = new StringBuffer();

        // may cause problems with TEXTAREA
        //sb.append( System.getProperty( "line.separator" ) );
	sb.append( Variant.grow( "/" + _tag, "<" ) );

        pw.println( sb );
    }

    void toPrintContent( PrintWriter pw )
    {
        toPrintContent( pw, _content );
    }

    void toPrintContent( PrintWriter pw, Object o )
    {
        if( o == null ){
            pw.print( _null );
        }
        else{
            if( o instanceof AbstractElement ){
                AbstractElement ele;
                ele = (AbstractElement)o;
                ele.toPrint( pw );
            }
            else{
                pw.print( String.valueOf( o ) );
            }
        }
    }

    /**
     * Prints the element to a writer, without forming a string first
     */
    public void toPrint( PrintWriter pw )
    {
        toPrintOpeningTag( pw );
        toPrintContent( pw );
        toPrintClosingTag( pw );
    }

    public PrintWriter getPrintWriter()
    {
	_content = new StringWriter();

	PrintWriter pr = new PrintWriter( (Writer)_content );

	return pr;
    }

    public void close()
    {
	try{
	    ((Writer)_content).close();
	}
	catch( Exception ex ){
	}
    }

    public static String encode( String s )
    {
	StringBuffer buf = new StringBuffer();
	for( int i = 0; i < s.length(); i++ ){
	    char c;
	    c = s.charAt( i );

            // leave those alone for now...
            //
            // IE doesn't understand them

	    //if( c == '\'' ){
            //    buf.append( "&apos;" );
            //    continue;
	    //}
	    //else if( c == '"' ){
            //    buf.append( "&quot;" );
            //    continue;
            //}

	    if( c == '<' ){
		buf.append( "&lt;" );
		continue;
	    }
	    else if( c == '>' ){
		buf.append( "&gt;" );
		continue;
	    }
	    else if( c == '&' ){
		buf.append( "&amp;" );
		continue;
	    }

	    buf.append( c );
	}
	return buf.toString();
    }
}
