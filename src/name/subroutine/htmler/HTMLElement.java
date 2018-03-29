package name.subroutine.htmler;

import java.util.*;
import java.io.*;

public class HTMLElement extends AbstractElement
{
    public HTMLElement()
    {
	super();
    }
    public HTMLElement( String tag, Object content )
    {
	super();
	_tag = tag;
	_content = content;
    }
    public HTMLElement( String tag )
    {
	super();
	_tag = tag;
    }

    public Object clone()
	throws CloneNotSupportedException
    {
	HTMLElement o;
	o = (HTMLElement)super.clone();

	o._attr_lst = new ArrayList();
	o._style_lst = new ArrayList();
	o.setAttr( _attr_lst );
	o.setStyle( _style_lst );
	
	return o;
    }

    /**
     * This one clones without throwing exception, which is utterly annoying
     */
    public HTMLElement dup()
    {
	Object o;
	try{
	    o = clone();
	}
	catch( Exception ex ){
	    o = new HTMLElement();
	}
	return (HTMLElement)o;
    }
}
