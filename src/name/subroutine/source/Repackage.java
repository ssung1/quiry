package name.subroutine.source;

public class Repackage
{
    public static void main( String argv[] )
    {
	Source source = new Source();

	if( argv.length == 1 ){
	    source.repackage( argv[0], "." );
	}
	else if( argv.length == 2 ){
	    source.repackage( argv[0], argv[1] );
	}
	else if( argv.length == 0 ){
	    System.out.println( "Java Sourcecode Repackager" );
	    System.out.println( "USAGE:" );
            System.out.println( "Repackage <destination dir> [source dir]" );
	}
    }
}
