package name.subroutine.source;

public class Compile
{
    public static void main( String argv[] )
    {
	Source source = new Source();

	if( argv.length == 1 ){
	    source._compile( argv[0] );
	}
	else if( argv.length == 0 ){
	    System.out.println( "Java Package Compiler" );
	    System.out.println( "USAGE:" );
            System.out.println( "Compile <package>" );
	}
    }
}
