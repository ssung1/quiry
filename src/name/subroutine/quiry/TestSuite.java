package name.subroutine.quiry;

import java.util.*;
import java.io.*;
import java.text.*;

import org.mozilla.javascript.*;

import name.subroutine.rdb.*;
import name.subroutine.util.Variant;
import name.subroutine.util.BPTree;
import name.subroutine.util.BPTreeNode;
import name.subroutine.util.BPTreeSelectIterator;

public class TestSuite
{
    public void quiry( String[] argv )
        throws Exception
    {
        Quiry.main( argv );
    }

    public void getStyle( String[] argv )
        throws Exception
    {
        Scriptable world = Resource.getWorld();
        Object val;
        
        Scriptable style;
        val = world.get( "st_dictionary", world );
        if( val == Scriptable.NOT_FOUND ) return;
        style = (Scriptable)val;

        for( int i = 0; true; ++i ){
            Scriptable column;
            val = style.get( i, style );
            if( val == Scriptable.NOT_FOUND ) break;
            column = (Scriptable)val;

            Object width;
            width = column.get( "width", column );
            
            System.out.println( Context.toString( width ) );
        }
    }
}
