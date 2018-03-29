package name.subroutine.source;

import java.io.*;
import java.util.*;

public class Tokenize
{
    public static void main( String argv[] )
    {
	Source source = new Source();
	Hashtable token_set;

	try{
	    InputStream is = new FileInputStream( argv[0] );
	    
	    token_set = source.getWords( new BufferedInputStream( is ) );

	    Enumeration en;
	    for( en = token_set.keys(); en.hasMoreElements(); ){
		System.out.println( en.nextElement() );
	    }
	}
	catch( Exception e ){
	    e.printStackTrace();
	}
    }
}
