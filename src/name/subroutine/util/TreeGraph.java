package name.subroutine.util;

import java.util.Map;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Stack;
import java.util.Set;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.ArrayList;
import java.util.List;

/**
 * This is a graph that is implemented as a tree
 *
 * There is a special requirement that we have in our tree node -- the
 * values inserted must be unique to each other.  This way the user
 * can manipulate the graph without accessing the nodes themselves.
 *
 * The user may insert either a single value or a pair of values,
 * implying a link.  When a single value is inserted, the value is
 * attached to the "root," which is an internal structure that manages
 * which nodes are accessible.  When a pair of values is inserted,
 * the * class creates one or both values as verices, if necessary,
 * and adds an edge from the former to the latter.
 *
 * Functions provided:
 *
 * The class is required to provide an iterator that traverses from
 * the internal root.
 *
 * The class is required to provide an iterator of all nodes added,
 * even if the node is not accessible from the root.
 *
 * The class is required to provide a set of vertices that have no
 * parent if asked.  This is to aid the user.  Note however, that this
 * list may not represent all the entry points to the graph.  A vertex
 * that links to itself will not be included in the said list.
 *
 * The class is required to provide a list of vertices in topological
 * order.  Vertices that participate in a cycle may be omitted without
 * notice (throwing exception).
 *
 * The class is required to provide a list of vertices, in topological
 * order, that are accessible from the "root" and do not connect to
 * any cycles in the graph (very different from the other topological
 * sort).
 *
 * The class is required to tell the user if the graph has a cycle, if
 * the user asks.
 *
 * The class is required to list all the cycles and their components 
 */

public class TreeGraph implements Graph
{
    /**
     * Root rootyrootyroot
     */
    TreeNode _root;

    /**
     * A map of user objects to nodes
     */
    Map _node_map;

    public TreeGraph()
    {
        _root = new TreeNode( "_root" );
        _node_map = new HashMap();
    }

    public TreeNode createNode( Object val )
    {
        TreeNode node = new TreeNode( val );
        _node_map.put( val, node );

        return node;
    }

    public boolean contains( Object o )
    {
        return _node_map.containsKey( o );
    }

    /**
     * Adds an object to the root
     */
    public boolean add( Object number_one )
    {
        boolean changed = false;
        TreeNode node = getNode( number_one );
        if( node == null ){
            node = createNode( number_one );
            changed = true;
        }
        changed = _root.addChild( node ) || changed;
        changed = node.addParent( _root ) || changed;

        return changed;
    }

    /**
     * Adds the edge from parent to child.
     * 
     * If parent or child does not exist in the graph already, it will
     * be added also
     */
    public boolean add( Object parent, Object child )
    {
        boolean changed = false;
        TreeNode node = getNode( child );
        if( node == null ){
            node = createNode( child );
            changed = true;
        }

        TreeNode p = getNode( parent );
        if( p == null ){
            p = createNode( parent );
            changed = true;
        }

        changed = p.addChild( node ) || changed;
        changed = node.addParent( p ) || changed;

        return changed;
    }

    /**
     * Removes the node and all links to that node.  However, if it
     * results in disconnecting a subtree from the root tree, the
     * subtree is not deleted unless the user explicitly calls
     * garbageCollection
     */
    public boolean remove( Object object )
    {
        TreeNode node;
        TreeNode parent;
	TreeNode child;

        node = getNode( object );
        if( node == null ) return false;

        Iterator pit = node.getParentNodeIterator();
        for( ; pit.hasNext(); ){
            parent = (TreeNode)pit.next();
            parent.removeChild( node );
        }

        node.removeAllParents();

	for( pit = node.getChildNodeIterator(); pit.hasNext(); ){
	    child = (TreeNode)pit.next();
	    child.removeParent( node );
	}

	node.removeAllChildren();

        _node_map.remove( object );

        return true;
    }

    /**
     * @return first generation
     */
    public Iterator getChildIterator()
    {
        return _root.getChildIterator();
    }

    /**
     * @return immediate child of the give node
     */
    public Iterator getChildIterator( Object parent )
    {
        TreeNode p = getNode( parent );
        return p.getChildIterator();
    }

    /**
     * @return immediate parent of the give node
     */
    public Iterator getParentIterator( Object child )
    {
        TreeNode c = getNode( child );
        return c.getParentIterator();
    }

    /**
     * @return a TreeNode that is associated with the value
     */
    public TreeNode getNode( Object val )
    {
        return (TreeNode)_node_map.get( val );
    }

    /**
     * Returns the internal root of the graph, since graphs do not
     * necessarily have a root.
     */
    public TreeNode getRoot()
    {
        return _root;
    }

    /**
     * @return a set of nodes that have no parents
     */
    protected Set getOrphanSet()
    {
        Set orphan_set = new HashSet();
        for( Iterator it = _node_map.values().iterator(); it.hasNext(); ){
            TreeNode node = (TreeNode)it.next();
            if( node.getParentCnt() <= 0 ){
                orphan_set.add( node );
            }
        }
        return orphan_set;
    }

    /**
     * adds orphans to the root node
     */
    public void addOrphanSet()
    {
        Set orphan_set = getOrphanSet();
        for( Iterator it = orphan_set.iterator(); it.hasNext(); ){
            TreeNode node = (TreeNode)it.next();
            getRoot().addChild( node );
            node.addParent( getRoot() );
        }
    }

    /**
     * Returns a list of lists that contain nodes which belong to a cycle:
     *
     * <pre>
     * 0:     a, b, c                a, b, and c form a cycle
     * 1:     d                      d forms a cycle on its own
     * ...
     * 
     */
    public List getCycleLst()
    {
        List retval = new ArrayList();

	// reset only once so we don't test more than necessary
	setMarked( false );

        for( Iterator it = _node_map.values().iterator(); it.hasNext(); ){
            List cycle;

	    TreeNode node;
	    node = (TreeNode)it.next();

	    cycle = getCycle( node );

            if( cycle != null ){
		retval.add( cycle );
	    };
        }

        return retval;
    }

    /**
     * @return true if the graph contains at least one cycle
     */
    public boolean hasCycle()
    {
	// A topological sort by indegree counting will yield
	// only nodes not in cycles
	//
	// Thus, if the number of total nodes is greater than
	// what is returned by getTopologicalLstByInDegree, then
	// there is at least one cycle
	return _node_map.size() > getTopologicalLstByInDegree().size();
    }

    /**
     * Returns true if the given node or any of the nodes accessible
     * from this node contains a cycle
     *
     * All nodes must be marked first
     * @deprecated
     */
    private boolean hasCycle( TreeNode root )
    {
        Iterator current = null;
        Stack history = new Stack();
        Stack other_history = new Stack();

        TreeNode node;
        node = root;

        //current = node.getChildNodeIterator();
        //other_history.push( root );

        while( true ){
            // if other_history already has it, then it means
            // we are traveling in a loop
            //
            // (other history contains the nodes that are in
            // our stack)
            if( other_history.contains( node ) ) return true;
            other_history.push( node );

            // already been there, try next one
            if( node.getMarked() ){
                continue;
            }
            else{
                node.mark();

                if( current != null ){
                    history.push( current );
                }
                //current = node.getChildNodeIterator();
                current = node.getChildNodeIterator();
            }

            if( current.hasNext() ){
                node = (TreeNode)current.next();

                //System.out.println( "node: " + node.getVal() );
                //if( !other_history.add( node ) ) return true;
                //if( other_history.contains( node ) ) return true;
                //other_history.push( node );


                /////System.out.print( "node: " + node.getVal() );
                /////System.out.println();
            }
            else{
                if( !history.empty() ){
                    current = (Iterator)history.pop();
                    other_history.pop();
                    continue;
                }
                else{
                    return false;
                }
            }
        }
    }

    /**
     * Returns true if the given node or any of the nodes accessible
     * from this node contains a cycle
     *
     * All nodes must be marked first
     * @deprecated because the loop is not as good
     */
    private boolean hasCycle2( TreeNode root )
    {
        TreeNode node;
        Iterator it;

        Stack node_history = new Stack();
        Stack iterator_history = new Stack();

        // initialize
        node = root;
        it = node.getChildNodeIterator();

        while( true ){
            if( node_history.contains( node ) ) return true;

            if( !node.getMarked() ){
                // process current node

                // get child list / adjacency

                if( it.hasNext() ){
                    TreeNode child = (TreeNode)it.next();

                    // recursively call self with child node, which means
                    // we have to keep track of a few things...

                    // equivalent to hasCycle2( child );
                    node_history.push( node );
                    iterator_history.push( it );

                    // go back
                    node = child;
                    it = node.getChildNodeIterator();

                    continue;
                }

                node.mark();
            }
            // do not recurse...just return
            // equivalent to "return from recursion"
            if( node_history.isEmpty() ) return false;
            node = (TreeNode)node_history.pop();
            it = (Iterator)iterator_history.pop();
        }
    }

    /**
     * Returns true if the given node is part of a cycle
     *
     * All nodes must be marked first
     */
    private boolean hasCycle3( TreeNode root )
    {
        TreeNode node;
        Iterator it;

        Stack iterator_history = new Stack();
	Stack node_history = new Stack();

	node = root;
        
        if( node.getMarked() ){
	    return false;
        }			
        else{
	    node_history.push( node );
	    node.mark();
            it = node.getChildNodeIterator();
	}
	while( true ){
	    if( it.hasNext() ){
		node = (TreeNode)it.next();

		if( node_history.contains( node ) ) return true;

		if( node.getMarked() ){
		    continue;
		}
		else{
		    node_history.push( node );
		    iterator_history.push( it );
		    it = node.getChildNodeIterator();
		    node.mark();
		}
	    }
	    else{
		if( iterator_history.isEmpty() ){
		    return false;
		}
		else{
		    node = (TreeNode)node_history.pop();
		    it = (Iterator)iterator_history.pop();
		    continue;
		}
	    }
	}
    }

    /**
     * Returns true if the given node is part of a cycle
     *
     * All nodes must be marked first
     * @deprecated and replaced by getCycle
     */
    private boolean isCycle( TreeNode root )
    {
        TreeNode node;
        Iterator it;

        Stack iterator_history = new Stack();

        // how can a depth first traversal be so hard? :(

        // this is the layout of the loopy loop
	//
	// voted most elegant by me, Jan 18, 2003

        // initialize
        // node = root;
        //
        // if( node.getMarked() ){              --.
        //     // go to next one                  |   The puzzle is to
        // }					  |   fit this into the
        // else{				  }-->loop
        //     it = node.getChildNodeIterator();  |
	//     node.mark()                        |
	// }                                    --'
        //
        // GO_THROUGH_ITERATOR:
        // if( it.hasNext() ){
        //
        //     node = (TreeNode)it.next();
        //
        //     if( node.getMarked() ){
        //         // go to next one
        //     }
        //
        //     else{
        //         iterator_history.push( it );
        //         it = child.getChildNodeIterator();
        //         node.mark();
        //         // (... on and on)
        //         // go back to GO_THROUGH_ITERATOR
        //     }
	//
        // }
        // else{
        //     if( iterator_history.isEmpty() ){
        //         return false;
        //     }
        //     else{
        //         it = (Iterator)iterator_history.pop();
        //         // go back to GO_THROUGH_ITERATOR
        //     }
        // }

	node = root;
        
        if( node.getMarked() ){
	    return false;
        }			
        else{
	    node.mark();
            it = node.getChildNodeIterator();
	}
        
	while( true ){
	    if( it.hasNext() ){
		node = (TreeNode)it.next();
		
		if( node == root ) return true;

		if( node.getMarked() ){
		    continue;
		}
		else{
		    iterator_history.push( it );
		    it = node.getChildNodeIterator();
		    node.mark();
		}
	    }
	    else{
		if( iterator_history.isEmpty() ){
		    return false;
		}
		else{
		    it = (Iterator)iterator_history.pop();
		    continue;
		}
	    }
	}
    }

    /**
     * Returns a cycle that has the given "root" as one of the
     * vertices or null if a cycle does not exist
     *
     * All nodes must be marked first; that is why the method
     * is private
     */
    private List getCycle( TreeNode root )
    {
        TreeNode node;
        Iterator it;

	Stack history = new Stack();
        Stack iterator_history = new Stack();

        // how can a depth first traversal be so hard? :(

        // this is the layout of the loopy loop
	//
	// voted most elegant by me, Jan 18, 2003

        // initialize
        // node = root;
        //
        // if( node.getMarked() ){              --.
        //     // go to next one                  |   The puzzle is to
        // }					  |   fit this into the
        // else{				  }-->loop
        //     it = node.getChildNodeIterator();  |
	//     node.mark()                        |
	// }                                    --'
        //
        // GO_THROUGH_ITERATOR:
        // if( it.hasNext() ){
        //
        //     node = (TreeNode)it.next();
        //
        //     if( node.getMarked() ){
        //         // go to next one
        //     }
        //
        //     else{
        //         iterator_history.push( it );
        //         it = child.getChildNodeIterator();
        //         node.mark();
        //         // (... on and on)
        //         // go back to GO_THROUGH_ITERATOR
        //     }
	//
        // }
        // else{
        //     if( iterator_history.isEmpty() ){
        //         return false;
        //     }
        //     else{
        //         it = (Iterator)iterator_history.pop();
        //         // go back to GO_THROUGH_ITERATOR
        //     }
        // }

	node = root;
        
        if( node.getMarked() ){
	    return null;
        }			
        else{
	    history.push( node.getVal() );
	    node.mark();
            it = node.getChildNodeIterator();
	}
        
	while( true ){
	    if( it.hasNext() ){
		node = (TreeNode)it.next();
		
		if( node == root ) return history;

		if( node.getMarked() ){
		    continue;
		}
		else{
		    history.push( node.getVal() );
		    iterator_history.push( it );
		    it = node.getChildNodeIterator();
		    node.mark();
		}
	    }
	    else{
		if( iterator_history.isEmpty() ){
		    return null;
		}
		else{
		    history.pop();
		    it = (Iterator)iterator_history.pop();
		    continue;
		}
	    }
	}
    }

    /**
     * This is an alternate algorithm to sort.  This strategy involves
     * the counting of indegrees instead of traversing the tree.  The
     * disadvantage here is that any loop inside the graph will result
     * in a cycle and program exception
     *
     * @return a topologically sorted list of vertices that do not
     * participate in a cyclic subgraph but may connect to a cyclic
     * subgraph
     */
    public List getTopologicalLstByInDegree()
    {
        setMarked( false );

        List retval = new ArrayList();

        // we are forcing "root" because root is really a virtual vertex
        // that should not be part of the result; yet it is used to
        // link all subgraphs together
        LinkedList pull_out = new LinkedList();
        pull_out.addLast( getRoot() );

        // scan through and save nodes and their indegrees because
        // we will have to change indegrees later
        //
        // meanwhile, if a node has indegree of zero, it can be added
        // to the return list (already? already!)
        Map indegree_map = new HashMap();
        for( Iterator it = _node_map.values().iterator(); it.hasNext(); ){
            TreeNode node = (TreeNode)it.next();
            indegree_map.put( node, new Integer( node.getInDegree() ) );

            if( node.getInDegree() == 0 ){
                pull_out.addLast( node );
                retval.add( node.getVal() );
                node.mark();
            }
        }

        // for each node pulled out, tug on the edges and see if we can
        // get more vertices out by making their indegree 0

        while( !pull_out.isEmpty() ){
            TreeNode node = (TreeNode)pull_out.removeFirst();
            for( Iterator it = node.getChildNodeIterator(); it.hasNext(); ){
                TreeNode adj = (TreeNode)it.next();

                // oh no! we have a cycle
                if( adj.getMarked() ){
                    throw new RuntimeException( "A cycle is found" );
                }

                Integer indegree = (Integer)indegree_map.get( adj );

                // since node has been pulled out, all nodes adjacent to
                // node will have one less indegree
                int indegree_int = indegree.intValue();
                indegree_int--;
                indegree = new Integer( indegree_int );

                indegree_map.put( adj, indegree );

                // and if an adjacent node ends up with an indegree of 0,
                // we can pull it out too
                if( indegree_int == 0 ){
                    pull_out.addLast( adj );
                    retval.add( adj.getVal() );
                    adj.mark();
                }
            }
        }

        return retval;
    }

    /**
     * @return a topologically sorted list of vertices that are
     * accessible from "root" and that do not connect to any cyclic
     * subgraphs
     */
    public List getTopologicalLstByTraversal()
    {
        List retval = new ArrayList();

        for( Iterator it = getRoot().getChildNodeIterator(); it.hasNext(); ){
	    boolean has_cycle;
            TreeNode relative_root;
            relative_root = (TreeNode)it.next();

            setMarked( false );
	    has_cycle = hasCycle3( relative_root );

            if( has_cycle ) continue;

	    setMarked( false );
            List partial;
            partial = getTopologicalLstByTraversal( relative_root );

            retval.addAll( partial );
        }

        return retval;
    }

    /**
     * @return a topologically ordered list of vertices that can be
     * reached from the given relative root
     *
     * The algorithm is truly simple.  Perform a depth-first traversal
     * (or a breadth-first traversal).  A node that runs out of
     * vertices to go on first is topologically last.  Thus, we keep
     * track of the nodes that are "done" and just reverse the order.
     *
     * called exclusively by getTopologicalLstByTraversal()
     */
    private List getTopologicalLstByTraversal( TreeNode root )
    {
        TreeNode node;
        Iterator it;

        Stack node_history = new Stack();
        Stack iterator_history = new Stack();
        LinkedList retval = new LinkedList();

        // initialize
	node = root;
        
	// this part is the hasNext section in the loop except the
	// pushing iterator part
        if( node.getMarked() ){
	    return retval;
        }			
        else{
	    node_history.push( node );
	    node.mark();
            it = node.getChildNodeIterator();
	}
        
	while( true ){
	    if( it.hasNext() ){
		node = (TreeNode)it.next();
		
		if( node.getMarked() ){
		    continue;
		}
		else{
		    node_history.push( node );
		    iterator_history.push( it );
		    it = node.getChildNodeIterator();
		    node.mark();
		}
	    }
	    else{
		TreeNode pop = (TreeNode)node_history.pop();
		retval.addFirst( pop.getVal() );

		if( iterator_history.isEmpty() ){
		    return retval;
		}
		else{
		    it = (Iterator)iterator_history.pop();
		    continue;
		}
	    }
	}
    }

    /**
     * Iterates through all the node values added, even if it is not
     * connected to root
     */
    public Iterator iterator()
    {
        return _node_map.keySet().iterator();
    }

    /**
     * Iterates through all the node values that can be reached from
     * root, depth first
     */
    public Iterator biterator()
    {
        setMarked( false );
        return new TreeGraphBreadthFirstIterator( _root );
    }

    /**
     * Iterates through all the node values that can be reached from
     * root, breadth first
     */
    public Iterator diterator()
    {
        setMarked( false );
        return new TreeGraphDepthFirstIterator( _root );
    }

    /**
     * Iterates through all the nodes added, even if it is not
     * connected to root
     */
    public Iterator nodeIterator()
    {
        return _node_map.values().iterator();
    }
    
    /**
     * Iterates through all the nodes that can be reached from root,
     * depth first
     */
    public Iterator nodeBiterator()
    {
        setMarked( false );
        return new TreeGraphBreadthFirstNodeIterator( _root );
    }

    /**
     * Iterates through all the nodes that can be reached from root,
     * breadth first
     */
    public Iterator nodeDiterator()
    {
        setMarked( false );
        return new TreeGraphDepthFirstNodeIterator( _root );
    }

    /**
     * Set the marked flag for all nodes
     */
    protected void setMarked( boolean val )
    {
        // can't use iterator to mark, because iterators depend on
        // a correct marking
        //
        // thus, we are using a modified iterator
        for( Iterator it = _node_map.values().iterator();
             it.hasNext(); ){
            TreeNode node = (TreeNode)it.next();
            node.setMarked( val );
        }

        getRoot().setMarked( val );
    }

    /**
     * Removes all nodes not connected to "root"
     *
     * Nasty shortcut algorithm, eh?
     */
    public void garbageCollect()
    {
	// mark
	Map new_map = new HashMap();
	for( Iterator it = diterator(); it.hasNext(); ){
	    Object o = it.next();
	    new_map.put( o, getNode( o ) );
	}

	// and sweep
	_node_map = new_map;
    }
}
