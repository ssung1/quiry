package name.subroutine.rdb;

import java.text.ParseException;
import java.sql.SQLException;

public abstract class ExternalRtbl implements Rtbl, Cloneable
{
    protected boolean _is_new = true;
    protected boolean _is_modified = true;

    public String name()
    {
	String cstring = this.getClass().getName();
	int period = cstring.lastIndexOf( "." );
	return cstring.substring( period + 1 );
    }

    public boolean isPrimaryKey( String str )
    {
	if( str == null ) return false;

	for( int i = 0; i < primaryKey().length; i++ ){
	    if( str.equalsIgnoreCase( primaryKey()[i] ) ){
		return true;
	    }
	}
	return false;
    }

    public void resolve( RdbSession ses )
    {
    }

    /**
     * saves only current object, without updating any related objects
     */
    public void save( RdbSession ses )
        throws RdbException, SQLException
    {
        //
        // this is a sample
        //
        
        // MANY_TO_ONE:
        // {
        //     LbStatus st = getStatus( ses );
        //     if( st != null ){
        //         set_status( st.get_oid() );
        //     }
        // }

        // do whatever needs to be done...for this object
        // this is an example of a mutable object, with
        // no additional restrictions

        STORING_THIS_OBJECT:
        {
            if( getIsNew() ){
                ses.insert( this );
            }
            else{
                if( getIsModified() ){
                    ses.update( this );
                }
            }
            // turns flags off so it won't be saved again
            setIsNew( false );
            setIsModified( false );
        }

        // ONE_TO_MANY:
        // {
        //     if( _history_lst != null ){
        //         for( Iterator it = _history_lst.iterator(); it.hasNext(); ){
        //             LbHistory his = (LbHistory)it.next();
        //             if( his.get_lockbox() != get_oid() ){
        //                 his.set_lockbox( get_oid() );
        //             }
        //             his.commit( ses );
        //         }
        //     }
        // }
    }

    public void set_oid( int val )
    {
    }
    
    public int get_oid()
    {
        return 0;
    }

    public String strGet_oid()
    {
        return String.valueOf( get_oid() );
    }

    /**
     * In theory, no outsiders should be allowed to set this
     */
    public void setIsNew( boolean val )
    {
        _is_new = val;
    }

    public boolean getIsNew()
    {
        return _is_new;
    }

    public void setIsModified( boolean val )
    {
        _is_modified = val;
    }

    public boolean getIsModified()
    {
        return _is_modified;
    }

    public void set_isDeleted( char val )
    {
    }

    public char get_isDeleted()
    {
        return 'F';
    }
}
