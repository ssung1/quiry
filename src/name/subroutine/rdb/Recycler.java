package name.subroutine.rdb;

import java.util.*;
import java.sql.*;
import java.text.*;

import name.subroutine.util.EdgeGraph;
import name.subroutine.util.GraphEdge;
import name.subroutine.util.GraphNode;

/**
 * This class is the garbage collector for Rdb.  It maintains the
 * relations between tables and performs garbage collection.
 */
public class Recycler
{
    static int RELATION_ONE_TO_MANY                                   = 10;
    static int RELATION_MANY_TO_ONE                                   = 11;

    RdbSession _ses;
    public Rdb _db;
    EdgeGraph _relation_graph = null;
    EdgeGraph _reference_graph = null;
    public Set _root_set = null;

    String[] _root_array;
    String[] _relation_array;

    /**
     * This constructor does not load relation and reference graphs
     * upon object creation.  The graphs are "lazily loaded" later
     * when user makes the request.
     */
    public Recycler( Rdb db )
    {
        _db = db;
    }

    public Recycler( Rdb db, String[] root_array, String[] relation_array )
    {
        this( db );
        _root_array = root_array;
        _relation_array = relation_array;
    }

    /**
     * Must be called before anything can be done
     */
    public void setSession( RdbSession ses )
    {
        _ses = ses;
    }

    protected void unmarkAll()
        throws RdbException, SQLException
    {
        for( Iterator it = _db.getRtblLst().iterator(); it.hasNext(); ){
            Rtbl rtbl = (Rtbl)it.next();
            String name = _db.getTableName( rtbl );

            if( getRootSet().contains( name ) ) continue;

            _ses.execute( "UPDATE " + name + " SET is_deleted = 'T'" );
        }
    }

    public void markAll()
        throws RdbException, SQLException
    {
        // before we mark, we unmark
        unmarkAll();

        EdgeGraph rel_gr = getRelationGraph();

        for( Iterator it = rel_gr.getTopologicalLstByInDegree().iterator();
             it.hasNext(); ){
            String trainer;
            trainer = (String)it.next();

            GraphNode trainer_node = (GraphNode)rel_gr.getNode( trainer );

            mark( trainer_node );
        }
    }

    /**
     * marks all the child edges originating from the given node
     * 
     * Called exclusively by markAll
     *
     * @param trainer is either a root table or a previously marked
     * table
     */
    private void mark( GraphNode trainer )
        throws RdbException, SQLException
    {
        Iterator it;
        for( it = trainer.getChildEdgeIterator(); it.hasNext(); ){
            GraphEdge ge = (GraphEdge)it.next();
            String dest = (String)ge.getDestination();

            System.out.println( trainer.getVal() + " ==> " + dest );

            //////////////
            //Set pokemon_mark_set;
            //if( ge.getType() == RELATION_ONE_TO_MANY ){
            //    pokemon_mark_set = mark_one_to_many( trainer_mark_set,
            //                                         dest,
            //                                         (String)ge.getVal() );
            //}
            //else{
            //    pokemon_mark_set = mark_many_to_one( trainer_mark_set,
            //                                         (String)trainer.getVal(),
            //                                         (String)ge.getVal() );
            // }

            // now merge both
            //old_pokemon_mark_set.addAll( pokemon_mark_set );
            //////////

            if( ge.getType() == RELATION_ONE_TO_MANY ){
                mark_one_to_many( (String)trainer.getVal(),
                                  (String)ge.getVal(), dest );
            }
            else{
                mark_many_to_one( (String)trainer.getVal(),
                                  (String)ge.getVal(), dest );
            }
        }
    }

    /**
     * 1 to many ...
     *
     * @param trainer is the "one"
     * @param ref_field is the table in "many" that references the "one"
     * @param pokemon is the "many"
     */
    private void mark_one_to_many( String trainer,
                                   String ref_field, String pokemon )
        throws RdbException, SQLException
    {
        ResultSet rs_trainer;
        rs_trainer = _ses.execute( "SELECT oid FROM " + trainer +
                                   " WHERE NOT (is_deleted = 'T')" );

        while( rs_trainer.next() ){
            String oid = rs_trainer.getString( 1 );

            StringBuffer sb = new StringBuffer();
            sb.append( "UPDATE " );
            sb.append( pokemon );
            sb.append( " SET is_deleted = 'F'" );
            sb.append( " WHERE " );
            sb.append( ref_field );
            sb.append( " = " );
            sb.append( oid );

            _ses.execute( sb.toString() );
        }

        try{
            rs_trainer.getStatement().close();
        }
        catch( Exception ex ){
        }
        rs_trainer.close();
    }

    /**
     * many to 1 ...
     *
     * @param trainer is the "many"
     * @param ref_field is the table in "many" that references the "one"
     * @param pokemon is the "one"
     */
    private void mark_many_to_one( String trainer, String ref_field,
                                   String pokemon )
        throws RdbException, SQLException
    {
        ResultSet rs_trainer;
        rs_trainer = _ses.execute( "SELECT " + ref_field + 
                                   " FROM " + trainer +
                                   " WHERE NOT (is_deleted = 'T')" );

        while( rs_trainer.next() ){
            String ref = rs_trainer.getString( 1 );

            StringBuffer sb = new StringBuffer();
            sb.append( "UPDATE " );
            sb.append( pokemon );
            sb.append( " SET is_deleted = 'F'" );
            sb.append( " WHERE oid = " );
            sb.append( ref );

            _ses.execute( sb.toString() );
        }

        try{
            rs_trainer.getStatement().close();
        }
        catch( Exception ex ){
        }
        rs_trainer.close();
    }

    public void sweepAll()
        throws RdbException, SQLException
    {
        for( Iterator it = _db.getRtblLst().iterator(); it.hasNext(); ){
            Rtbl rtbl = (Rtbl)it.next();
            String name = _db.getTableName( rtbl );
            sweep( name );
        }
    }

    /**
     * called exclusively by sweepAll
     */
    private void sweep( String table )
        throws RdbException, SQLException
    {
        String sql =  "DELETE FROM " + table + " WHERE is_deleted = 'T'";
        _ses.execute( sql );
    }

    /**
     * marks all the child edges originating from the given node
     * 
     * Called exclusively by markAll
     *
     * @param mark_set_map is a map of table name to its set of marked
     * oids
     * @deprecated
     */
    private void mark( Map mark_set_map, GraphNode trainer )
        throws RdbException, SQLException
    {
        Set trainer_mark_set;
        trainer_mark_set = (Set)mark_set_map.get( trainer.getVal() );

        if( trainer_mark_set == null ){
            // the relations aren't sorted correctly
            throw new NullPointerException( "Relations are not " +
                                            "sorted correctly.  " +
                                            "Table " + trainer.getVal() +
                                            " needs to be marked first." );
        }
        
        Iterator it;
        for( it = trainer.getChildEdgeIterator(); it.hasNext(); ){
            GraphEdge ge = (GraphEdge)it.next();
            String dest = (String)ge.getDestination();
            // if one pokemon has two masters (possible in our case),
            // we add up both
            Set old_pokemon_mark_set;
            old_pokemon_mark_set = (Set)mark_set_map.get( dest );
            if( old_pokemon_mark_set == null ){
                old_pokemon_mark_set = new HashSet();
                mark_set_map.put( dest, old_pokemon_mark_set );
            }

            System.out.println( trainer.getVal() + " ==> " + dest );

            Set pokemon_mark_set;
            if( ge.getType() == RELATION_ONE_TO_MANY ){
                pokemon_mark_set = mark_one_to_many( trainer_mark_set,
                                                     dest,
                                                     (String)ge.getVal() );
            }
            else{
                pokemon_mark_set = mark_many_to_one( trainer_mark_set,
                                                     (String)trainer.getVal(),
                                                     (String)ge.getVal() );
            }

            // now merge both
            old_pokemon_mark_set.addAll( pokemon_mark_set );
        }
    }

    /**
     * Uses internal getRelationGraph to mark all the tables
     *
     * @return a map of table name to a set of oids that are marked
     * @deprecated
     */
    public Map markAll1()
        throws RdbException, SQLException
    {
        Map mark_set_map = new HashMap();

        EdgeGraph rel_gr = getRelationGraph();

        System.out.println( "Root set" );
        for( Iterator it = rel_gr.getChildIterator(); it.hasNext(); ){
            String root_table = (String)it.next();

            System.out.println( root_table );
            mark_set_map.put( root_table, mark( root_table ) );
        }

        // important ////////////////////
        // this algorithm has been tested for 1 to many relations only
        // (for now)
        System.out.println( "Relation Set" );
        for( Iterator it = rel_gr.getTopologicalLstByInDegree().iterator();
             it.hasNext(); ){
            String trainer;
            trainer = (String)it.next();

            GraphNode trainer_node = (GraphNode)rel_gr.getNode( trainer );

            mark( mark_set_map, trainer_node );
        }

        // now we should have a map of tables and their marked oids
        /*********** for testing 
        for( Iterator it = mark_set_map.entrySet().iterator();
             it.hasNext(); ){
            Map.Entry entry = (Map.Entry)it.next();

            System.out.print( entry.getKey() );
            System.out.print( " --> " );
            
            Set ms = (Set)entry.getValue();

            for( Iterator iit = ms.iterator(); iit.hasNext(); ){
                System.out.print( iit.next() );
                System.out.print( " " );
            }

            System.out.println();
        }
        ***************/

        return mark_set_map;
    }

    /**
     * Gets a set of all root OIDs, which are always marked
     * @deprecated
     */
    public Set mark( String table )
        throws RdbException, SQLException
    {
        ResultSet rs;
        rs = _ses.execute( "SELECT oid FROM " + table );

        Set retval = new HashSet();

        Integer oid;
        while( rs.next() ){
            oid = Integer.valueOf( rs.getString( 1 ) );
            retval.add( oid );
        }

        try{
            rs.getStatement().close();
        }
        catch( Exception ignored ){
        }
        rs.close();

        return retval;
    }

    /**
     * 1 to many ...
     *
     * @param mark_set contains oids (of another table) that are marked
     * @param table is the table to check
     * @param ref_field is the field in table that may have values in
     * mark_set
     *
     * @return a set of oids of @param table that references oids in
     * mark_set
     * @deprecated
     */
    public Set mark_one_to_many( Set mark_set,
                                 String table, String ref_field )
        throws RdbException, SQLException
    {
        ResultSet rs;
        rs = _ses.execute( "SELECT oid, " + ref_field + " FROM " + table );

        Set retval = new HashSet();
        Integer oid;
        Integer ref;
        while( rs.next() ){
            oid = Integer.valueOf( rs.getString( 1 ) );
            ref = Integer.valueOf( rs.getString( 2 ) );
            if( mark_set.contains( ref ) ){
                retval.add( oid );
            }
        }

        rs.close();
        try{
            rs.getStatement().close();
        }
        catch( Exception ignored ){
        }

        return retval;
    }

    /**
     * many to 1 ...
     *
     * @param mark_set contains oids (of another table) that are marked
     * @param table is the table to check
     * @param ref_field is the field in table that may have values in
     * mark_set
     *
     * @return a set of oids of @param table that references oids in
     * mark_set
     *
     * @deprecated WARNING: this function has not been tested
     */
    public Set mark_many_to_one( Set mark_set,
                                 String table, String ref_field )
        throws RdbException, SQLException
    {
        ResultSet rs;
        rs = _ses.execute( "SELECT oid, " + ref_field + " FROM " + table );

        Set retval = new HashSet();
        Integer oid;
        Integer ref;
        while( rs.next() ){
            oid = Integer.valueOf( rs.getString( 1 ) );
            ref = Integer.valueOf( rs.getString( 2 ) );
            if( mark_set.contains( oid ) ){
                retval.add( ref );
            }
        }

        rs.close();
        try{
            rs.getStatement().close();
        }
        catch( Exception ignored ){
        }

        return retval;
    }

    /**
     * Collect garbage using the mark and sweep algorithm
     */
    public void markAndSweep()
        throws RdbException, SQLException
    {
        markAll();
        sweepAll();
        

        //Map mas = markAll();
        ////sweepAll( mas );
    }

    /**
     * @param mark_set_map a map of table name to a set of oids that
     * are marked
     * @deprecated
     */
    public void sweepAll1( Map mark_set_map )
        throws RdbException, SQLException
    {
        for( Iterator it = mark_set_map.entrySet().iterator();
             it.hasNext(); ){
            Map.Entry entry = (Map.Entry)it.next();

            String ms_key = (String)entry.getKey();
            Set ms_val = (Set)entry.getValue();

            sweep( ms_key, ms_val );
        }
    }

    /**
     * deletes records whose oid is not in mark_set
     * @deprecated
     */
    public void sweep( String table, Set mark_set )
        throws RdbException, SQLException
    {
        Set sweep_set = getSweepSet( table, mark_set );

        Integer oid;
        for( Iterator it = sweep_set.iterator(); it.hasNext(); ){
            oid = (Integer)it.next();
            _ses.execute( "DELETE FROM " + table + " WHERE oid = " + oid );
        }
    }

    /**
     * @deprecated
     */
    public Set getSweepSet( String table, Set mark_set )
        throws RdbException, SQLException
    {
        ResultSet rs;
        rs = _ses.execute( "SELECT oid FROM " + table );

        Set sweep_set = new HashSet();
        Integer oid;
        while( rs.next() ){
            oid = Integer.valueOf( rs.getString( 1 ) );
            if( !mark_set.contains( oid ) ){
                sweep_set.add( oid );
            }
        }

        rs.close();
        try{
            rs.getStatement().close();
        }
        catch( Exception ignored ){
        }

        return sweep_set;
    }

    /**
     * update table.field with the values in relomap
     *
     * @param relomap a mapping of old value to new value
     * @param table name of the table to update
     * @param field field of the table to update
     */
    public void relocate( Map relomap,
                          String table, String field )
        throws RdbException, SQLException
    {
        for( Iterator iit = relomap.entrySet().iterator();
             iit.hasNext(); ){
            Map.Entry entry = (Map.Entry)iit.next();
            System.out.print( "    " );
            System.out.print( entry.getKey() );
            System.out.print( " --> " );
            System.out.println( entry.getValue() );

            String sql;
            sql = "UPDATE " + table + 
                  " SET " + field + " = " + entry.getValue() +
                  " WHERE " + field + " = " + entry.getKey();

            System.out.println( sql );

            _ses.execute( sql );
        }
    }

    /**
     * Rearranges the oids and references to oids so that they
     * become as continuous as possible
     *
     * @param batch_size is the number of records to update at one
     * time
     * @param batch_count is the maximum number of batches to run.
     * a value of -1 means to run until all oids are contiguous
     */
    public void relocate( int batch_size, int batch_count )
        throws RdbException, SQLException
    {
        EdgeGraph rg = getReferenceGraph();
        for( Iterator it = rg.getTopologicalLstByTraversal().iterator();
             it.hasNext(); ){
            GraphNode node = (GraphNode)rg.getNode( it.next() );
            String trainer = (String)node.getVal();

            Map relomap;

            for( int i = 0; i < batch_count || batch_count == -1; ++i ){
                relomap = getRelocationMap( trainer, batch_size );

                // break if we are done
                if( relomap.size() <= 0 ) break;

                relocate( relomap, trainer, "oid" );
                
                System.out.print( "Relocating: " );
                System.out.print( trainer );
                System.out.print( "(oid)" );
                System.out.println();
                
                Iterator pokemon_it;
                for( pokemon_it = node.getChildEdgeIterator();
                     pokemon_it.hasNext(); ){
                    GraphEdge ge = (GraphEdge)pokemon_it.next();
                    String field_name = (String)ge.getVal();
                    String pokemon = (String)ge.getDestination();
                    
                    relocate( relomap, pokemon, field_name );
                    
                    System.out.print( "Relocating: " );
                    System.out.print( trainer + " and " + pokemon );
                    System.out.print( "   oid = " + trainer );
                    System.out.print( "   ref = " );
                    System.out.print( pokemon );
                    System.out.print( "(" + field_name + ")" );
                    System.out.println();
                }
            }
        }
    }

    /**
     * Returns a list of oids that need to be copied so that
     * we would eliminate gaps between oids
     *
     * @param limit maximum relocation entries desired (to save
     *              memory)
     */
    public Map getRelocationMap( String table, int limit )
        throws RdbException, SQLException
    {
        ResultSet rs;
        rs = _ses.execute( "SELECT oid FROM " + table + " ORDER BY oid" );
        ResultSet rs_max;
        rs_max = _ses.execute( "SELECT oid FROM " + table +
                               " ORDER BY oid DESC" );

        Map map = new HashMap();

        int quantum = 1;
        int ideal_oid = _db.getStartingMaxId();

        Integer id = null;

        int cnt = 0;
        while( true ){
            // new algorithm that saves on memory, since now
            // we have millions of records...
            //
            // this is very similar to comparing two sorted files

            // loop:
            //
            // if [id] is less (!) than ideal or first time
            //     get next [id]; done if no more
            // else if [id] is good
            //     calculate next [ideal_id]
            //     get next [id]; done if no more
            // else
            //     get next [max]; done if no more
            //     if [max] <= [ideal_id]
            //         then we are done
            //     else
            //         add to map [max] => [ideal_id]
            //         calculate next [ideal_id]
            //

            if( id == null || id.intValue() < ideal_oid ){
                if( !rs.next() ) break;
                id = new Integer( rs.getInt( 1 ) );
            }
            else if( id.intValue() == ideal_oid ){
                if( !rs.next() ) break;
                id = new Integer( rs.getInt( 1 ) );
                ideal_oid += quantum;
            }
            else{
                if( !rs_max.next() ) break;
                Integer tid;
                tid = new Integer( rs_max.getInt( 1 ) );

                if( tid.intValue() <= ideal_oid ){
                    break;
                }
                else{
                    map.put( tid, new Integer( ideal_oid ) );
                    ideal_oid += quantum;
                    ++cnt;
                    if( cnt >= limit ) break;
                }
            }
        }

        try{
            rs.getStatement().close();
        }
        catch( Exception ex ){
        }
        rs.close();
        
        try{
            rs_max.getStatement().close();
        }
        catch( Exception ex ){
        }
        rs_max.close();
        
        return map;
    }

    protected void setHighLowKeysId( String table, int idnum )
        throws RdbException, SQLException
    {
	// this has no effect unless createId also uses
	// highlowkeys
        _db.setId( _ses, table, idnum );
    }

    /**
     * Updates highlowkeys table to make sure it contains the max oid
     * for all the tables.  This is necessary, along with oid
     * relocation, in order to recover unused oids.
     */
    public void updateHighLowKeys( String table )
        throws RdbException, SQLException
    {
	// update highlowkeys with current max id
	String sql;
	sql = _db.toSelect( table, new String[] { "max(oid)" } );
	ResultSet rs;
	rs = _ses.execute( sql );

	rs.next();

	int max = rs.getInt( 1 );
	setHighLowKeysId( table, max );

        rs.close();
	try{
	    rs.getStatement().close();
	}
	catch( Exception ex ){
	}
    }	

    public void updateHighLowKeys()
        throws RdbException, SQLException
    {
        EdgeGraph rg = getReferenceGraph();
        for( Iterator it = rg.iterator();
             it.hasNext(); ){
            GraphNode node = (GraphNode)rg.getNode( it.next() );
            String trainer = (String)node.getVal();

            updateHighLowKeys( trainer );
        }
    }

    public Set getRootSet()
    {
        if( _root_set != null ) return _root_set;

        String root[] = getRootArray();

        Set s = new HashSet();

        for( int i = 0; i < root.length; i++ ){
            s.add( root[i] );
        }

        _root_set = s;
        return s;
    }

    public EdgeGraph getRelationGraph()
    {
        if( _relation_graph != null ) return _relation_graph;

        EdgeGraph g = new EdgeGraph();
        
        String[] root_array = getRootArray();
        for( int i = 0; i < root_array.length; i++ ){
            g.add( root_array[i] );
        }

        String[] relation_array = getRelationArray();

        for( int i = 0; i < relation_array.length; i += 4 ){
            int type;
            if( "1to*".equals( relation_array[i + 1] ) ){
                type = RELATION_ONE_TO_MANY;
            }
            else{
                type = RELATION_MANY_TO_ONE;
            }
            g.add( relation_array[i],
                   relation_array[i + 3],
                   type, relation_array[i + 2] );
        }

        _relation_graph = g;
        return g;
    }

    /**
     * In a reference graph, the "oid holder" is always the parent
     */
    public EdgeGraph getReferenceGraph()
    {
        if( _reference_graph != null ) return _reference_graph;

        EdgeGraph g = new EdgeGraph();
        
        // should be replaced by automatic root detection
        String[] root_array = getRootArray();
        for( int i = 0; i < root_array.length; i++ ){
            g.add( root_array[i] );
        }
        
        String[] relation_array = getRelationArray();

        for( int i = 0; i < relation_array.length; i += 4 ){
            int type;
            if( "1to*".equals( relation_array[i + 1] ) ){
                g.add( relation_array[i],
                       relation_array[i + 3],
                       0, relation_array[i + 2] );
            }
            else{
                g.add( relation_array[i + 3],
                       relation_array[i],
                       0, relation_array[i + 2] );
            }
        }

        _reference_graph = g;
        return g;
    }

    /**
     * Array of immediately accessible tables
     *
     * It is important to override this list
     */
    public String[] getRootArray()
    {
        return _root_array;
    }

    /**
     * The format of the relation array is as follows:
     *
     * <pre>
     * table1      relation            reference-field   table 2
     * table1      relation            reference-field   table 2
     * table1      relation            reference-field   table 2
     * .
     * .
     * .
     *
     * where
     *
     *     table1 is the trainer
     *    
     *     table2 is the pokemon
     *
     *     relation is either "1to*" or "*to1"
     *
     *     reference-field is the field that references the OID in the
     *     other table.
     *
     * </pre>
     *
     * Now order is not important (yay) (Dec, 2002)
     * 
     * This list will be processed and topologically sorted.  There is
     * a reason to study computer science after all.
     */
    public String[] getRelationArray()
    {
        return _relation_array;
    }
}
