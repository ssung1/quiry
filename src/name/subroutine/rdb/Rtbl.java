package name.subroutine.rdb;

import java.text.ParseException;
import java.sql.SQLException;

/**
 * This is the interface implemented by all classes that
 * represent a table in the relational database
 *
 * All members of the class that are to be considered as fields
 * must begin with two underscores
 */
public interface Rtbl
{
    /**
     * Returns the name of the table
     */
    public abstract String name();

    /**
     * Returns true if the given field name is primary key
     * (the field name should not include the __ prefix)
     */
    public abstract boolean isPrimaryKey( String field );

    /**
     * Returns a list of fields that composes the primary key
     */
    public abstract String[] primaryKey();

    /**
     * Returns a list of list of field names to index
     */
    public abstract String[][] indexLst();

    /**
     * Returns a list of list of field names to "unique index"
     */
    public abstract String[][] uniqueIndexLst();

    /**
     * Resolves internal references
     *
     * This is called when the object is loaded from database
     */
    public abstract void resolve( RdbSession ses );

    /**
     * Saves object into database
     */
    public abstract void save( RdbSession ses )
        throws RdbException, SQLException;

    /**
     * Sets oid
     */
    public abstract void set_oid( int val );

    /**    
     * Gets oid
     */
    public abstract int get_oid();

    /**
     * Gets oid as String
     */
    public abstract String strGet_oid();

    /**
     * Sets new flag
     *
     * In theory, no outsiders should be allowed to set this
     */
    public abstract void setIsNew( boolean val );

    /**
     * Gets new flag
     */
    public abstract boolean getIsNew();

    /**
     * Sets modified flag
     */
    public abstract void setIsModified( boolean val );

    /**
     * Gets modified flag
     */
    public abstract boolean getIsModified();

    /**
     * Sets delete flag
     *
     * Delete flag is stored in database
     */
    public abstract void set_isDeleted( char val );

    /**
     * Gets delete flag
     *
     * Delete flag is stored in database
     */
    public abstract char get_isDeleted();
}
