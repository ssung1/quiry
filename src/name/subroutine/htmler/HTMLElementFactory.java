package name.subroutine.htmler;

import java.util.*;
import java.io.*;


public class HTMLElementFactory
{
    /**
     * Creates many HTMLElements using elem as template, and
     * them items in o as content
     */
    public static HTMLElement[] massCreate( HTMLElement elem, Object[] o )
    {
	HTMLElement[] retval = new HTMLElement[ o.length ];

	for( int i = 0; i < o.length; i++ ){
	    HTMLElement nu;
	    try{
		nu = (HTMLElement)elem.clone();
	    }
	    catch( CloneNotSupportedException ex ){
		nu = new HTMLElement( elem._tag );
	    }

	    nu.setContent( o[i] );
	    retval[i] = nu;
	}
	return retval;
    }

    /**
     * Creates an element by name
     */
    public static HTMLElement create( String element, String name,
				      Object content, int size )
    {
	String style_lst[] = {
	    "link",             "createLink",
	    "text",             "createTextField",
            "password",         "createPasswordField",
	    "hidden",           "createHiddenField",
	    "submit",           "createButton",
            "textarea",         "createTextArea",
	    "combobox",         "createComboBox",
	    "checkbox",         "createCheckBox",
	    "textfield",        "createTextField",
            "button",           "createButton",
	    "select",           "createComboBox",
	    "none",             "createText",
	};

	// this is actually more complicated than
	// if( style.equals( "text" ) return createTextField( ... )
	// if( style.equals( "password" )...
	//
	// but is sure a lot more fun
	Class c = HTMLElementFactory.class;
	for( int i = 0; i < style_lst.length; i++ ){
	    if( style_lst[i].equalsIgnoreCase( element ) ){
		try{
		    java.lang.reflect.Method method;
		    method = c.getMethod( style_lst[i + 1],
			new Class[] {
			    String.class,
                            Object.class,
                            int.class }
                    );
		    Object result;
		    result = method.invoke( null, new Object[] {
                        name,
                        content,
			new Integer( size ) }
                    );

		    return (HTMLElement)result;
		}
		catch( Exception ex ){
		    return null;
		}
	    }
	}
	return null;
    }

    /**
     * A wrapper element that has nothing but the content, as text
     */
    public static HTMLElement createText( String name, Object content,
					  int size )
    {
	HTMLElement elem = new HTMLElement( null );
	elem.setContent( content );
	return elem;
    }

    public static HTMLElement createTextField( String name, Object content,
					       int size )
    {
	HTMLElement inp = new HTMLElement( "input", "" );
	inp.setAttr( new Object[] {
	    "type", "text",
	    "name", name,
	    "value", content,
	    "size", String.valueOf( size ),
        } );
	return inp;
    }
    public static HTMLElement createPasswordField( String name, Object content,
						   int size )
    {
	HTMLElement inp = new HTMLElement( "input", "" );
	inp.setAttr( new Object[] {
	    "type", "password",
	    "name", name,
	    "value", content,
	    "size", String.valueOf( size ),
        } );
	return inp;
    }
    public static HTMLElement createHiddenField( String name, Object content,
					         int size )
    {
	HTMLElement inp = new HTMLElement( "input", "" );
	inp.setAttr( new Object[] {
	    "type", "hidden",
	    "name", name,
	    "value", content,
        } );
	return inp;
    }
    public static HTMLElement createTextArea( String name, Object content,
					      int size )
    {
	HTMLElement inp = new HTMLElement( "textarea", "" );
	inp.setAttr( new Object[] {
	    "name", name,
	    "cols", String.valueOf( size ),
	    "rows", "5",
        } );
	inp.setContent( content );
	return inp;
    }
    public static HTMLElement createTextArea( String name, Object content,
					      int width, int height )
    {
	HTMLElement inp = new HTMLElement( "textarea", "" );
	inp.setAttr( new Object[] {
	    "name", name,
	    "cols", String.valueOf( width ),
	    "rows", String.valueOf( height ),
        } );
	inp.setContent( content );
	return inp;
    }
    /**
     * Creates a combo box that contains the months of a year
     */
    public static HTMLElement createMonthComboBox( String name, int month )
    {
	Object month_list[] = {
	    "1",  "January",
	    "2",  "February",
	    "3",  "March",
	    "4",  "April",
	    "5",  "May",
	    "6",  "June",
	    "7",  "July",
	    "8",  "August",
	    "9",  "September",
	    "10", "October",
	    "11", "November",
	    "12", "December",
	};
	return createComboBox( name, month_list,
			       String.valueOf( month ), 10 );
    }

    public static HTMLElement createComboBox( String name, Object content,
					      int size )
    {
	HTMLContainer inp = new HTMLContainer( "select" );
	inp.setAttr( new Object[] {
	    "name", name,
        } );

	Object[] items;
	if( content instanceof Collection ){
	    Collection collect = (Collection)content;
	    items = collect.toArray( new Object[0] );
	}
	else if( content instanceof Object[] ){
	    items = (Object[])content;
	}
	else{
	    items = new Object[] { content, content };
	}
	for( int i = 0; i < items.length; i += 2 ){
	    HTMLElement opt;
	    opt = new HTMLElement( "option" );

	    opt.setAttr( new Object[] {
                "value", items[i] } );
	    try{
		opt.setContent( items[i + 1] );
	    }
	    catch( Exception ex ){
		opt.setContent( items[i] );
	    }

	    inp.add( opt );
	}

	return inp;
    }

    public static HTMLElement createComboBox( String name, Object content,
					      Object defval, int size )
    {
	HTMLContainer inp = new HTMLContainer( "select" );
	inp.setAttr( new Object[] {
	    "name", name,
        } );

	Object[] items;
	if( content instanceof Collection ){
	    Collection collect = (Collection)content;
	    items = collect.toArray( new Object[0] );
	}
	else if( content instanceof Object[] ){
	    items = (Object[])content;
	}
	else{
	    items = new Object[] { content, content };
	}
	for( int i = 0; i < items.length; i += 2 ){
	    HTMLElement opt;
	    opt = new HTMLElement( "option" );

	    opt.setAttr( new Object[] {
                "value", items[i] } );

	    if( items[i].equals( defval ) ){
		opt.setAttr( new Object[] {
		    "value", items[i],
                    "selected", "",
		} );
	    }

	    try{
		opt.setContent( items[i + 1] );
	    }
	    catch( Exception ex ){
		opt.setContent( items[i] );
	    }

	    inp.add( opt );
	}

	return inp;
    }

    public static HTMLElement createButton( String name, Object content,
					    int size )
    {
	HTMLElement submit;
	submit = new HTMLElement( "input" );
	submit.setAttr( new Object[] {
            "type", "submit",
            "value", content,
	    "name", name } );

	return submit;
    }

    public static HTMLElement createButton( String name, Object content,
					    String hint )
    {
	HTMLElement submit;
	submit = new HTMLElement( "input" );
	submit.setAttr( new Object[] {
            "type", "submit",
            "value", content,
	    "name", name,
            "title", hint } );

	return submit;
    }

    /**
     * Creates a link
     *
     * @param name the URL to link to
     * @param content the description
     *                of the link (the blue stuff users click on)
     * @param size ignored
     */
    public static HTMLElement createLink( String name, Object content,
					  int size )
    {
	HTMLElement elem;
	elem = new HTMLElement( "a" );
	elem.setAttr( new Object[] {
            "href", name, } );
	elem.setContent( content );

	return elem;
    }

    /**
     * Creates a link without passing in size
     *
     * @param name the URL to link to
     * @param content the description
     *                of the link (the blue stuff users click on)
     */
    public static HTMLElement createLink( String name, Object content )
    {
	return createLink( name, content, 0 );
    }

    /**
     * Creates a link that looks like a button
     */
    public static HTMLElement createLinkButton( String name, Object content,
                                                int size )
    {
	HTMLElement submit;
	submit = new HTMLElement( "input" );
	submit.setAttr( new Object[] {
            "type", "submit",
            "value", content,
            "onClick", "document.location.href = \"" + name + "\"",
        } );

	return submit;
    }

    /**
     * Creates a checkbox
     * @param size if greater than 0, then the box is checked
     */
    public static HTMLElement createCheckBox( String name, Object content,
					  int size )
    {
	HTMLElement elem;
	elem = new HTMLElement( "input" );
	elem.setAttr( new Object[] {
            "type", "checkbox",
            "name", name,
        } );
	if( size > 0 ){
	    elem.addAttr( new Object[] {
                "checked", "true"
	    } );
	}
	elem.setContent( content );

	return elem;
    }

    public static HTMLElement createRadio( String name, Object content,
					   int size )
    {
	Object items[];
	if( content instanceof Collection ){
	    Collection collect = (Collection)content;
	    items = collect.toArray( new Object[0] );
	}
	else if( content instanceof Object[] ){
	    items = (Object[])content;
	}
	else{
	    items = new Object[] { content, content };
	}

	HTMLElement elem;
	elem = new HTMLElement( "input" );
	elem.setAttr( new Object[] {
	    "type", "radio",
		"name", name,
		"value", items[0],
		} );
	if( size > 0 ){
	    elem.addAttr( new Object[] {
		"checked", "true"
		    } );
	}
	elem.setContent( items[1] );
	
	return elem;
    }
}
