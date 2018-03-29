package name.subroutine.rdb;

import java.lang.reflect.*;
import java.util.*;
import java.sql.*;
import java.text.*;

import name.subroutine.etable.*;
import name.subroutine.util.*;

public class HighLowKeys extends AbstractRtbl
{
    char __tablename[] = new char[32];
    int __maxid = 0;

    public boolean isPrimaryKey( String f )
    {
	if( f.equalsIgnoreCase( "tablename" ) ) return true;

	return false;
    }

    static String[] _pk = {
	"tablename"
    };
    public String[] primaryKey()
    {
	return _pk;
    }

    public void setMaxId( int id )
    {
	__maxid = id;
    }
    public int getMaxId()
    {
	return __maxid;
    }
    public void advanceMaxId( int grabsize )
    {
	__maxid += grabsize;
    }

    public void setTableName( String table )
    {
	__tablename = table.toCharArray();
    }
    public String getTableName()
    {
	return new String( __tablename );
    }

    public String[][] indexLst()
    {
	return new String[][] {};
    }

    public String[][] uniqueIndexLst()
    {
	return new String[][] { 
            { "tablename" },
        };
    }

    // static class Id
    // {
    //     int _current;
    //     int _valid_until;
    // }
    // 
    // static Map _id_map = new HashMap();
    // 
    // /**
    //  * Selects an ID from database, and if ID is not found,
    //  * return an initial value and adds an entry to the database.
    //  *
    //  * This will not update database if an ID is found.
    //  */
    // public synchronized static HighLowKeys _selectId( Rdb db, String table )
    //     throws NoSuchFieldException, IllegalAccessException,
    //            InstantiationException, ParseException,
    //            SQLException, ClassNotFoundException
    // {
    //     HighLowKeys hl;
    //     hl = new HighLowKeys();
    // 
    //     table = table.toLowerCase();
    // 
    //     List rs;
    //     rs = db.selectEq( hl, new String[] {
    //         "tablename", table
    //     } );
    // 
    //     if( rs.size() <= 0 ){
    //         hl.setMaxId( hl.getStartingMaxId() );
    //         hl.setTableName( table );
    // 
    //         db.insert( hl );
    //     }
    //     else{
    //         hl = (HighLowKeys)rs.get( 0 );
    //     }
    //     
    //     return hl;
    // }
    // 
    // /**
    //  * Returns an unused ID for a given table
    //  */
    // public synchronized static int createId( Rdb db, String table )
    //     throws NoSuchFieldException, IllegalAccessException,
    //            InstantiationException, ParseException,
    //            SQLException, ClassNotFoundException
    // {
    //     HighLowKeys hl;
    //     Id id;
    //     id = (Id)_id_map.get( table );
    // 
    //     if( id == null ){
    //         hl = _selectId( db, table );
    // 
    //         id = new Id();
    // 
    //         id._current = hl.getMaxId();
    //         id._valid_until = id._current + getGrabSize();
    // 
    //         _id_map.put( table, id );
    // 
    //         hl.advanceMaxId();
    // 
    //         db.update( hl );
    //     }
    //     else{
    //         id._current++;
    // 
    //         // need to call database for a new chunk
    //         if( id._current >= id._valid_until ){
    //     	hl = _selectId( db, table );
    // 
    //     	id._current = hl.getMaxId();
    //     	id._valid_until = id._current + getGrabSize();
    // 
    //     	hl.advanceMaxId();
    //     	db.update( hl );
    //         }
    //     }
    // 
    //     return id._current;
    // }
    // 
    // /**
    //  * Sets the next id to the given integer, and start
    //  * all future ids from there.
    //  */
    // public synchronized static void setId( Rdb db, String table, int idnum )
    //     throws NoSuchFieldException, IllegalAccessException,
    //            InstantiationException, ParseException,
    //            SQLException, ClassNotFoundException
    // {
    //     HighLowKeys hl;
    //     Id id;
    //     id = (Id)_id_map.get( table );
    // 
    //     if( id == null ){
    //         id = new Id();
    //         _id_map.put( table, id );
    //     }
    // 
    //     hl = _selectId( db, table );
    // 
    //     id._current = idnum;
    //     id._valid_until = id._current + getGrabSize();
    // 
    //     hl.setMaxId( idnum );
    //     hl.advanceMaxId();
    // 
    //     db.update( hl );
    // }
}

