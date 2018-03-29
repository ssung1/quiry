package name.subroutine.etable;

import java.util.*;
import java.io.*;
import java.sql.*;

public abstract class AbstractDSTable extends AbstractTable
    implements DirectStorageTable
{
    /**
     * Adds a field
     */
    public int pushFld( String name )
    {
	/************* fix later
	pushFld( name, 'C' );
	return 1;
	**********************/
	throw new UnsupportedOperationException( 
            "Cannot add fields to this type of table (yet)"
        );
    }

    /**
     * Adds a field
     */
    public int pushFld( String name, int type )
    {
	/***************
	DbfField newfield = new DbfField( name, type );
	_field_lst.add( newfield );
	return 1;
	***************/
	throw new UnsupportedOperationException( 
            "Cannot add fields to this type of table (yet)"
        );
    }

    /**
     * initializes the table.  Does not need record list because all
     * records are directly stored in the storage system, whatever it
     * may be.
     */
    public void init()
    {
	super.init();

	_record_lst = null;
	_field_lst = new Vector();
    }

}
