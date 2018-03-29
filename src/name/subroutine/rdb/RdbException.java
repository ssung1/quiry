package name.subroutine.rdb;

public class RdbException extends Exception
{
    public RdbException()
    {
        super();
    }

    public RdbException( String message )
    {
        super( message );
    }

    public RdbException( String message, Throwable cause )
    {
        super( message, cause );
    }

    public RdbException( Throwable cause )
    {
        super( cause );
    }
}
