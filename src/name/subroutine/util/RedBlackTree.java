package name.subroutine.util;

import java.util.*;

/**
 * Mostly for academic interest
 *
 * Java has its own implementation of the red-black tree, named
 * SortedSet
 *
 * Notes: a red-black tree is a "b+tree" with three keys and four
 * links per node, represented as parent and two children
 *
 *   1, ,      is represented as                 1b
 *
 *   1,2       is represented as                 1b             2b
 *                                              *      or      *
 *                                             2r             1r
 *
 *   1,2,3     as represented as                 2b
 *                                              *  *
 *                                            1r    3r
 *
 * There is only one black node in each "triplet".  The colors are
 * used to keep track of the b+tree levels.
 *
 * A red-black tree follows these rules:
 *     
 *     1. Every node is either red or black
 *
 *     2. Every null node is black
 *
 *     3. If a node is red, then both its children are black
 *
 *     4. Every simple pach from a node to a descendent leaf contains
 *        the same number of black nodes
 *
 * Now lets add 1 through 8 in both structures and see how the
 * red-black tree works.
 *
 * (The b and r are just colors attached to the red-black nodes)
 *
 * adding 1
 *
 *                  1, ,                                    1b
 *
 * adding 2
 *
 *                  1,2                                     1b
 *                                                            *
 *                                                             2r
 *
 * adding 3
 *
 *                  1,2,3                                   1b
 *                                                            *
 *                                                             2r
 *                                                               *
 *                                                                3r
 *
 *                                                   rotate left about 1b
 *                                                   and recolor
 *
 *                                                          2b
 *                                                         *  *
 *                                                       1r    3r
 *
 *                                                   (note a rotation is
 *                                                   used to adjust internal
 *                                                   positions within a b+
 *                                                   node)
 *
 * adding 4
 *                  1,2,3,(4)                               2b
 *                                                         *  *
 *                                                       1r    3r
 *                                                               *
 *                                                                4r
 *
 *                  bump 2 to higher level           recolor 2b as red
 *                                                   recolor 1r, 3r as black
 *
 *                  2,      ,                               2r
 *                 * *                                     *  *
 *            1, ,    3,4,                               1b    3b
 *                                                               *
 *                                                                4r
 *
 *                                                   recoler 2r as black
 *                                                   because it is root
 *
 *                                                          2b
 *                                                         *  *
 *                                                       1b    3b
 *                                                               *
 *                                                                4r
 *
 * adding 5
 *
 *                  2,      ,                               2b
 *                 * *                                     *  *
 *            1, ,    3,4,5                              1b    3b
 *                                                               *
 *                                                                4r
 *                                                                  *
 *                                                                   5r
 *
 *                                                   rotate about 3b
 *                                                   and recolor
 *
 *                                                          2b
 *                                                         *  *
 *                                                       1b    4b
 *                                                            *  *
 *                                                          3r    5r
 *
 * adding 6
 *
 *                  2,      ,                               2b
 *                 * *                                     *  *
 *            1, ,    3,4,5,(6)                          1b    4b
 *                                                            *  *
 *                                                          3r    5r
 *                                                                  *
 *                                                                   6r
 *
 *            bump 4 up                              recolor 4b red
 *                                                   recolor 3r and 5r black
 *
 *                  2,     4,                               2b
 *                 * *      *                              *  *
 *            1, ,    3, ,   5,6,                        1b    4r
 *                                                            *  *
 *                                                          3b    5b
 *                                                                  *
 *                                                                   6r
 *
 * adding 7
 *
 *                  2,     4,                               2b
 *                 * *      *                              *  *
 *             1, ,   3, ,   5,6,7                       1b    4r
 *                                                            *  *
 *                                                          3b    5b
 *                                                                  *
 *                                                                   6r
 *                                                                     *
 *                                                                      7r
 *
 *                                                   rotate, like in step 3
 *                                                   and step 5
 *
 *                                                   every time we reach
 *                                                   a full three (in the
 *                                                   b+ node), we may need
 *                                                   a rotation
 *
 *                                                          2b
 *                                                         *  *
 *                                                       1b    4r
 *                                                            *  *
 *                                                          3b    6b
 *                                                               *  *
 *                                                             5r    7r
 *
 * adding 8
 *
 *                  2,     4,                               2b
 *                 * *      *                              *  *
 *            1, ,    3, ,   5,6,7,(8)                   1b    4r
 *                                                            *  *
 *                                                          3b    6b
 *                                                               *  *
 *                                                             5r    7r
 *                                                                     *
 *                                                                      8r
 *
 *            bump 6 up                              recolor 6b red
 *                                                   recolor 5r and 7r black
 *
 *                  2,     4,     6                         2b
 *                 * *      *      *                       *  *
 *            1, ,    3, ,   5, ,   7,8,                 1b    4r
 *                                                            *  *
 *                                                          3b    6r
 *                                                               *  *
 *                                                             5b    7b
 *                                                                     *
 *                                                                      8r
 *
 *                                                   since 2,4,6 completes
 *                                                   a triple, we must see
 *                                                   if we need to rotate
 *                   
 *                                                   and we do
 *
 *                                                          4b
 *                                                        *    *
 *                                                     2r        6r
 *                                                    *  *      *  *
 *                                                  1b    3b  5b    7b
 *                                                                    *
 *                                                                     8r
 *
 * Work this out by yourself, and you shall understand yet another
 * structure (or two) in computer science
 */
public class RedBlackTree
{
    /**
     * @deprecated
     */
    static final Integer LEFT = new Integer( 10 );
    /**
     * @deprecated
     */
    static final Integer RIGHT = new Integer( 11 );

    protected Comparator _comp = null;
    protected RedBlackTreeNode _root;
    protected int _size;

    public RedBlackTree()
    {
        _root = null;
        _size = 0;
    }

    public void setComparator( Comparator comp )
    {
	_comp = comp;
    }

    public void inOrder()
    {
        inOrder( _root );
    }

    public void inOrder( RedBlackTreeNode root )
    {
        if( root == null ) return;

        inOrder( root.getLeft() );
        System.out.print( root );
        if( root.getColor() == root.RED ){
            System.out.print( 'R' );
        }
        else{
            System.out.print( 'B' );
        }
        System.out.print( " " );
        inOrder( root.getRight() );
    }

    public void levelOrder()
    {
        List q = new LinkedList();
        if( _root != null ){
            q.add( _root );
        }

        while( true ){
            if( q.isEmpty() ) break;

            RedBlackTreeNode node;
            node = (RedBlackTreeNode)q.remove( 0 );

            if( node.getLeft() != null ){
                q.add( node.getLeft() );
            }
            if( node.getRight() != null ){
                q.add( node.getRight() );
            }
            
            System.out.print( node );
            if( node.getColor() == node.RED ){
                System.out.print( 'R' );
            }
            else{
                System.out.print( 'B' );
            }
            System.out.print( " " );
        }
    }

    int compare( Object a, Object b )
    {
        if( a == null && b == null ) return 0;

        // see if there is a special comparator
        if( _comp != null ){
            return _comp.compare( a, b );
        }

        // see if val is comparable
        if( a instanceof Comparable &&
            b instanceof Comparable ){
            Comparable ca = (Comparable)a;
            Comparable cb = (Comparable)b;
            return ca.compareTo( cb );
        }

        // turn to string if all else fails
        String as = String.valueOf( a );
        String bs = String.valueOf( b );
        return as.compareTo( bs );
    }

    public int size()
    {
        return _size;
    }

    /**
     * rotates to the left about the given node (b)
     *
     *                  P                    P
     *                 *                    *
     *                b                    d
     *               * *         ===>     *
     *              a   d                b
     *                 *                * *
     *                c                a   c
     *
     * note the sort order of the nodes is preserved
     *
     * we need the parent node because we need to update the link
     *
     * set parent to null if rotating the root
     *
     * @deprecated
     */
    public void rotateLeft( RedBlackTreeNode parent_node,
                            Integer direction )
    {
        RedBlackTreeNode node;
        if( parent_node == null ){
            node = _root;
        }
        else if( direction == LEFT ){
            node = parent_node.getLeft();
        }
        else{
            node = parent_node.getRight();
        }

        RedBlackTreeNode heir = node.getRight();
        // the orphan is the heir's original child
        //
        // when the original parent becomes the new child of the heir,
        // the original child becomes orphaned
        //
        // the original parent then adopts the orchan
        RedBlackTreeNode orphan = heir.getLeft();
        node.setRight( orphan );
        heir.setLeft( node );

        if( parent_node == null ){
            _root = heir;
        }
        else if( direction == LEFT ){
            parent_node.setLeft( heir );
        }
        else{
            parent_node.setRight( heir );
        }
    }

    /**
     * rotates to the left about the given node (b)
     *
     *                  P                    P
     *                 *                    *
     *                b                    d
     *               * *         ===>     *
     *              a   d                b
     *                 *                * *
     *                c                a   c
     *
     * note the sort order of the nodes is preserved
     */
    public void rotateLeft( RedBlackTreeNode node )
    {
        RedBlackTreeNode parent = node.getParent();
        
        RedBlackTreeNode heir = node.rotateLeft();

        heir.setParent( parent );
        if( parent == null ){
            _root = heir;
        }
        else if( parent.getLeft() == node ){
            parent.setLeft( heir );
        }
        else{
            parent.setRight( heir );
        }
    }

    /**
     * rotates to the right about the given node (b)
     *
     *                  P                    P
     *                 *                    *
     *                b                    a
     *               * *         ===>       *
     *              a   d                    b
     *               *                      * *
     *                c                    c   d
     *
     * note the sort order of the nodes is preserved
     *
     * we need the parent node because we need to update the link
     *
     * set parent to null if rotating the root
     *
     * @deprecated
     */
    protected void rotateRight( RedBlackTreeNode parent_node,
                                Integer direction )
    {
        RedBlackTreeNode node;
        if( parent_node == null ){
            node = _root;
        }
        else if( direction == LEFT ){
            node = parent_node.getLeft();
        }
        else{
            node = parent_node.getRight();
        }

        RedBlackTreeNode heir = node.getLeft();
        // the orphan is the heir's original child
        //
        // when the original parent becomes the new child of the heir,
        // the original child becomes orphaned
        //
        // the original parent then adopts the orchan
        RedBlackTreeNode orphan = heir.getRight();
        node.setLeft( orphan );
        heir.setRight( node );

        if( parent_node == null ){
            _root = heir;
        }
        else if( direction == LEFT ){
            parent_node.setLeft( heir );
        }
        else{
            parent_node.setRight( heir );
        }
    }

    /**
     * rotates to the right about the given node (b)
     *
     *                  P                    P
     *                 *                    *
     *                b                    a
     *               * *         ===>       *
     *              a   d                    b
     *               *                      * *
     *                c                    c   d
     *
     * note the sort order of the nodes is preserved
     */
    public void rotateRight( RedBlackTreeNode node )
    {
        RedBlackTreeNode parent = node.getParent();;

        RedBlackTreeNode heir = node.rotateRight();

        heir.setParent( parent );
        if( parent == null ){
            _root = heir;
        }
        else if( parent.getLeft() == node ){
            parent.setLeft( heir );
        }
        else{
            parent.setRight( heir );
        }
    }

    /**
     * @deprecated
     */
    protected void adjustColorAfterAdding( List history, List leftright,
                                           RedBlackTreeNode node )
    {
        // level of the node to check
        int node_level;
        // level of the parent
        int parent_level;
        // level of the grandparent
        int grandparent_level;

        RedBlackTreeNode parent;
        RedBlackTreeNode grandparent;
        RedBlackTreeNode uncle;

        node_level = history.size();

        while( true ){
            parent_level = node_level - 1;
            grandparent_level = parent_level - 1;
    
            // only worry if there is a red parent
            if( parent_level < 0 ) return;
    
            parent = (RedBlackTreeNode)history.get( parent_level );
    
            if( !parent.isRed() ) return;
    
            // if there is no grandparent, then we just set parent to
            // black and be done
            if( grandparent_level < 0 ){
                parent.setColorBlack();
                return;
            }
    
            grandparent = (RedBlackTreeNode)history.get( grandparent_level );
    
            // how do we find uncle?
            // 
            // we get the child of the opposite direction of the
            // grandparent
    
            if( leftright.get( grandparent_level ) == LEFT ){
                uncle = grandparent.getRight();
            }
            else{
                uncle = grandparent.getLeft();
            }
    
            // case 1:
            //
            // Br is the node of interest
            //
            // Dr is the red uncle
            //
            // *s are links because backslash confuses emacs a little
            //
            //          Cb                      Cr
            //         *  *                    *  *
            //       Ar    Dr         ===>   Ab    Db
            //         *                       *
            //          Br                      Br
            //
            // do the same even if Br is on the other side of Ar 
            //
            //          Cb                      Cr
            //         *  *                    *  *
            //       Ar    Dr         ===>   Ab    Db
            //      *                       *   
            //    Br                      Br      
            // 
            // After this step, we have to check Cr to see if Cr's parent
            // is red
    
            if( uncle != null && uncle.isRed() ){
                parent.setColorBlack();
                uncle.setColorBlack();
                grandparent.setColorRed();
                node_level = grandparent_level;
                continue;
            }

            // case 2:
            //
            // Br is the node of interest
            // 
            // Uncle is missing
            //
            // Grandparent-parent direction is opposite of parent-node
            // direction
            //
            //          Cb                      Cb             Bb
            //         *                       *              *  *
            //       Ar               ===>   Br      ===>   Ar    Cr
            //         *                    *
            //          Br                Ar               see case 3
            //
            // We "straighten" in out by rotating about the parent
            //
            // Then we treat it as case 3.  But we can't just let
            // the logic fall through since our "history" in incorrect
            // after rotation

            // we need the great-grandparent for this one
            int greatgrandparent_level = grandparent_level - 1;
            RedBlackTreeNode greatgrandparent;
            Integer ggp_dir;
            if( greatgrandparent_level >= 0 ){
                greatgrandparent = (RedBlackTreeNode)history.get(
                                       greatgrandparent_level
                                   );
                ggp_dir = (Integer)leftright.get( greatgrandparent_level );
            }
            else{
                greatgrandparent = null;
                // just a dummy value
                ggp_dir = LEFT;
            }

            if( leftright.get( grandparent_level ) == LEFT &&
                leftright.get( parent_level ) == RIGHT ){
                rotateLeft( grandparent, LEFT );
                rotateRight( greatgrandparent, ggp_dir );
                grandparent.setColorRed();
                // after first rotation, node becomes parent
                // after second rotation, node (parent) becomes grandparent
                // and that need to be painted black
                node.setColorBlack();
                break;
            }
            else if( leftright.get( grandparent_level ) == RIGHT &&
                     leftright.get( parent_level ) == LEFT ){
                rotateRight( grandparent, RIGHT );
                rotateLeft( greatgrandparent, ggp_dir );
                grandparent.setColorRed();
                node.setColorBlack();
                break;
            }

            // case 3:
            //
            // Ar is the node of interest
            // 
            // Uncle is missing
            //
            // Grandparent-parent direction is the same as that of
            // parent-node direction
            //
            //          Cb                      Bb
            //         *                       *  *
            //       Br               ===>   Ar    Cr
            //      *                        
            //    Ar                        
            //
            // We balance by rotating about the grandparent in the
            // opposite direction, and we color parent black and
            // grandparent red
            //
            // Then we are done with adjustment            

            if( leftright.get( grandparent_level ) == LEFT &&
                leftright.get( parent_level ) == LEFT ){
                rotateRight( greatgrandparent, ggp_dir );
                grandparent.setColorRed();
                parent.setColorBlack();
                break;
            }
            else if( leftright.get( grandparent_level ) == RIGHT &&
                     leftright.get( parent_level ) == RIGHT ){
                rotateLeft( greatgrandparent, ggp_dir );
                grandparent.setColorRed();
                parent.setColorBlack();
                break;
            }
        }
    }

    /**
     * @deprecated
     */
    public boolean deprecated_add( Object val )
    {
        // search for stuff
        RedBlackTreeNode root;
        RedBlackTreeNode node = new RedBlackTreeNode( val );

        // remembers two levels up, since we can't go up
        // the tree
        //
        // history level is the level of the tree.  when finish
        // adding, the new node will be at level [history.size()]
        //
        // the new node is not part of history
        //
        //      0          b      
        //                * *
        //      1        a   c
        //                    *
        //                     d
        //
        List history = new ArrayList();
        // leftright level is the path traveled after reaching
        // the history level.  the path to the new node is at
        // [leftright.size()]
        //
        //                 b      
        //      0         * *
        //               a   c
        //      1             *
        //                     d
        // 
        // the path leading to the new node is included in leftright
        List leftright = new ArrayList();

        // insert into tree with standard algorithm for binary
        // sorted tree
        if( _root == null ){
            _root = node;
            ++_size;
            return true;
        }

        root = _root;

        int diff;
        while( true ){
            RedBlackTreeNode child;

            history.add( root );

            diff = compare( val, root.getVal() );
            if( diff < 0 ){
                child = root.getLeft();
                leftright.add( LEFT );
                if( child == null ){
                    root.setLeft( node );
                    break;
                }
                else{
                    root = child;
                    continue;
                }
            }
            else if( diff > 0 ){
                child = root.getRight();
                leftright.add( RIGHT );
                if( child == null ){
                    root.setRight( node );
                    break;
                }
                else{
                    root = child;
                    continue;
                }
            }
            else{
                // return false if key already exists
                return false;
            }
        }

        // now we have one more
        ++_size;

        // now we adjust colors
        adjustColorAfterAdding( history, leftright, node );

        return true;
    }

    /**
     * @param node is the newly added node
     */
    protected void adjustColorAfterAdding( RedBlackTreeNode node )
    {
        RedBlackTreeNode parent;
        RedBlackTreeNode grandparent;
        RedBlackTreeNode uncle;

        while( true ){
            parent = node.getParent();

            // only worry if there is a red parent
            //
            // if there is no parent, set to black (it's nicer to have
            // root black)
            //
            // if there is a black parent, do nothing
            if( parent == null ){
                node.setColorBlack();
                break;
            }
            else if( parent.isBlack() ){
                break;
            }
    
            grandparent = parent.getParent();
            // if there is no grandparent, then we just set parent to
            // black and be done
            //
            // this is a redundant case check, since if we set root to
            // black, a red parent would always have a parent
            if( grandparent == null ){
                parent.setColorBlack();
                break;
            }
    
            // now that we make sure there is a grandparent, we can
            // safely get uncle
            uncle = parent.getSibling();

            // case 1:
            //
            // Br is the node of interest
            //
            // Dr is the red uncle
            //
            // *s are links because backslash confuses emacs a little
            //
            //          Cb                      Cr
            //         *  *                    *  *
            //       Ar    Dr         ===>   Ab    Db
            //         *                       *
            //          Br                      Br
            //
            // do the same even if Br is on the other side of Ar 
            //
            //          Cb                      Cr
            //         *  *                    *  *
            //       Ar    Dr         ===>   Ab    Db
            //      *                       *   
            //    Br                      Br      
            // 
            // After this step, we have to check Cr to see if Cr's parent
            // is red
    
            if( uncle != null && uncle.isRed() ){
                parent.setColorBlack();
                uncle.setColorBlack();
                grandparent.setColorRed();
                node = grandparent;
                continue;
            }

            // case 2:
            //
            // Br is the node of interest
            // 
            // Uncle is missing or is black
            //
            // Grandparent-parent direction is opposite of parent-node
            // direction
            //
            //          Cb                      Cb             Bb
            //         *                       *              *  *
            //       Ar               ===>   Br      ===>   Ar    Cr
            //         *                    *
            //          Br                Ar      see case 3
            //
            // We "straighten" in out by rotating about the parent
            //
            // Then we do a "fall through" to case 3

            if( parent.isLeftChild() && node.isRightChild() ){
                rotateLeft( parent );

                // previous parent is now the "node of interest"
                node = parent;

                parent = node.getParent();
            }
            else if( parent.isRightChild() && node.isLeftChild() ){
                rotateRight( parent );

                // previous parent is now the "node of interest"
                node = parent;

                parent = node.getParent();
            }

            // case 3:
            //
            // Ar is the node of interest
            // 
            // Uncle is missing or is black
            //
            // Grandparent-parent direction is the same as that of
            // parent-node direction
            //
            //          Cb                      Bb
            //         *                       *  *
            //       Br               ===>   Ar    Cr
            //      *                        
            //    Ar                        
            //
            // We balance by rotating about the grandparent in the
            // opposite direction, and we color parent black and
            // grandparent red
            //
            // Then we are done with adjustment

            if( parent.isLeftChild() && node.isLeftChild() ){
                rotateRight( grandparent );
                grandparent.setColorRed();
                parent.setColorBlack();
                break;
            }
            else if( parent.isRightChild() && node.isRightChild() ){
                rotateLeft( grandparent );
                grandparent.setColorRed();
                parent.setColorBlack();
                break;
            }
        }
    }

    public boolean add( Object val )
    {
        // search for stuff
        RedBlackTreeNode parent;
        RedBlackTreeNode node = new RedBlackTreeNode( val );

        // insert into tree with standard algorithm for binary
        // sorted tree
        if( _root == null ){
            _root = node;
            ++_size;
            adjustColorAfterAdding( node );
            return true;
        }

        parent = _root;

        int diff;
        while( true ){
            RedBlackTreeNode child;

            diff = compare( val, parent.getVal() );
            if( diff < 0 ){
                child = parent.getLeft();
                if( child == null ){
                    parent.setLeft( node );
                    node.setParent( parent );
                    break;
                }
                else{
                    parent = child;
                    continue;
                }
            }
            else if( diff > 0 ){
                child = parent.getRight();
                if( child == null ){
                    parent.setRight( node );
                    node.setParent( parent );
                    break;
                }
                else{
                    parent = child;
                    continue;
                }
            }
            else{
                // return false if key already exists
                return false;
            }
        }

        // now we have one more
        ++_size;

        // now we adjust colors
        adjustColorAfterAdding( node );

        return true;
    }

    public boolean contains( Object val )
    {
        // search for stuff
        RedBlackTreeNode root;

        root = _root;

        int diff;
        while( true ){
            RedBlackTreeNode child;
            if( root == null ) return false;

            diff = compare( val, root.getVal() );
            if( diff < 0 ){
                root = root.getLeft();
                continue;
            }
            else if( diff > 0 ){
                root = root.getRight();
                continue;
            }
            else{
                return true;
            }
        }
    }

    RedBlackTreeNode get( Object val )
    {
        // search for stuff
        RedBlackTreeNode root;

        root = _root;

        int diff;
        while( true ){
            RedBlackTreeNode child;
            if( root == null ) return null;

            diff = compare( val, root.getVal() );
            if( diff < 0 ){
                root = root.getLeft();
                continue;
            }
            else if( diff > 0 ){
                root = root.getRight();
                continue;
            }
            else{
                return root;
            }
        }
    }

    /**
     * get the rightmost decendent of the given node
     * @deprecated
     */
    public RedBlackTreeNode getRightmost( RedBlackTreeNode node )
    {
        while( true ){
            RedBlackTreeNode child = node.getRight();
            if( child == null ) return node;
            node = child;
        }
    }

    /**
     * get the leftmost decendent of the given node
     * @deprecated
     */
    public RedBlackTreeNode getLeftmost( RedBlackTreeNode node )
    {
        while( true ){
            RedBlackTreeNode child = node.getLeft();
            if( child == null ) return node;
            node = child;
        }
    }

    protected void adjustColorAfterRemoving( List history, List leftright,
                                             RedBlackTreeNode node )
    {
        System.out.println( "deleted node: " + node );
    }

    public int getHeight()
    {
        return getHeight( _root );
    }

    public int getHeight( RedBlackTreeNode node )
    {
        if( node == null ) return 0;
        return Math.max( getHeight( node.getLeft() ),
                         getHeight( node.getRight() ) ) + 1;
    }

    /**
     * removes and returns the rightmost node, given the parent node
     * and direction after the parent node as the first node to search
     * for.
     *
     * the importance of this is that we don't have to check to see
     * if the node to be removed has two children (which makes things
     * very complicated)
     *
     * we can't just pass in the node itself because we need to update
     * the link for the parent
     *
     * if the node to start is the root, pass null and any direction
     *
     * @deprecated
     */
    protected RedBlackTreeNode removeRightmost( RedBlackTreeNode parent,
                                                Integer direction )
    {
        RedBlackTreeNode node;
        if( parent == null ){
            node = _root;
        }
        else if( direction == LEFT ){
            node = parent.getLeft();
        }
        else{
            node = parent.getRight();
        }

        while( true ){
            RedBlackTreeNode child = node.getRight();
            // found rightmost
            if( child == null ){
                // we are removing root
                if( parent == null ){
                    _root = null;
                }
                else if( direction == LEFT ){
                    // no right node, hehe
                    parent.setLeft( node.getLeft() );
                }
                else if( direction == RIGHT ){
                    parent.setRight( node.getLeft() );
                }
                return node;
            }
            parent = node;
            direction = RIGHT;
            node = child;
        }
    }

    /**
     * @deprecated
     */
    public RedBlackTreeNode removeRightmost()
    {
        return removeRightmost( null, LEFT );
    }

    /**
     * removes and returns the leftmost node, given the parent node
     * and direction after the parent node as the first node to search
     * for.
     *
     * the importance of this is that we don't have to check to see
     * if the node to be removed has two children (which makes things
     * very complicated)
     *
     * we can't just pass in the node itself because we need to update
     * the link for the parent
     *
     * if the node to start is the root, pass null and any direction
     *
     * @deprecated
     */
    protected RedBlackTreeNode removeLeftmost( RedBlackTreeNode parent,
                                               Integer direction )
    {
        RedBlackTreeNode node;
        if( parent == null ){
            node = _root;
        }
        else if( direction == LEFT ){
            node = parent.getLeft();
        }
        else{
            node = parent.getRight();
        }

        while( true ){
            RedBlackTreeNode child = node.getLeft();
            // found leftmost
            if( child == null ){
                // we are removing root
                if( parent == null ){
                    _root = null;
                }
                else if( direction == LEFT ){
                    // no left node, hehe
                    parent.setLeft( node.getRight() );
                }
                else if( direction == RIGHT ){
                    parent.setRight( node.getRight() );
                }
                return node;
            }
            parent = node;
            direction = LEFT;
            node = child;
        }
    }

    /**
     * @deprecated
     */
    public RedBlackTreeNode removeLeftmost()
    {
        return removeLeftmost( null, LEFT );
    }

    /**
     * remove is very different from add
     * @deprecated
     */
    public boolean deprecated_remove( Object val )
    {
        // search for stuff
        RedBlackTreeNode node;

        // remembers two levels up, since we can't go up
        // the tree
        List history = new ArrayList();
        List leftright = new ArrayList();

        if( _root == null ){
            return false;
        }

        node = _root;

        int diff;
        while( true ){
            RedBlackTreeNode child;

            diff = compare( val, node.getVal() );
            if( diff < 0 ){
                child = node.getLeft();
                history.add( node );
                leftright.add( LEFT );
                if( child == null ){
                    return false;
                }
                else{
                    node = child;
                    continue;
                }
            }
            else if( diff > 0 ){
                child = node.getRight();
                history.add( node );
                leftright.add( RIGHT );
                if( child == null ){
                    return false;
                }
                else{
                    node = child;
                    continue;
                }
            }
            else{
                // general notes:
                //
                // deleting is easy when one child of the node
                // is null, in which case just let the non-null
                // child take its place
                //
                // (deleting c)
                //
                //         c                      b
                //        *          ===>        *
                //       b                      a
                //      *
                //     a
                //
                // if the node has two non-null children, we have
                // to replace the node with (not necessarily one
                // of the children but) the node with the closest
                // value (this is a sorted tree after all)
                //
                // (deleting d)
                //
                //         d                     (c)
                //        * *                    * *
                //       b  (e)      ===>       b  (e)
                //      * *                    *
                //     a  (c)                 a
                //
                //                               (e)
                //                               *
                //                    or        b
                //                             * *
                //                            a  (c)
                //
                // it's probably better to take from the taller
                // subtree
                //

                final RedBlackTreeNode left_child = node.getLeft();
                final RedBlackTreeNode right_child = node.getRight();
                RedBlackTreeNode heir = null;

                // only has left child, so we promote it
                if( left_child != null && right_child == null ){
                    heir = left_child;
                }
                // only has right child, so we promote it
                else if( left_child == null && right_child != null ){
                    heir = right_child;
                }
                // we promote the left child...works for the right too
                else if( left_child != null && right_child != null ){
                    int left_height = getHeight( left_child );
                    int right_height = getHeight( right_child );

                    // pick the taller
                    if( left_height >= right_height ){
                        heir = removeRightmost( node, LEFT );
                    }
                    else{
                        heir = removeLeftmost( node, RIGHT );
                    }
                    heir.setRight( node.getRight() );
                    heir.setLeft( node.getLeft() );
                    break;
                }

                int parent_level = history.size() - 1;
                RedBlackTreeNode parent;
                if( parent_level >= 0 ){
                    parent = (RedBlackTreeNode)history.get( parent_level );
                    if( leftright.get( parent_level ) == LEFT ){
                        parent.setLeft( heir );
                    }
                    else{
                        parent.setRight( heir );
                    }
                }
                else{
                    _root = heir;
                }
                break;
            }
        }

        --_size;

        int length = history.size();
        // the last item in history is the parent

        // lets see the history
        System.out.println( "History: " );
        for( int i = 0; i < length; ++i ){
            System.out.print( history.get( i ) );
            System.out.print( " " );
            if( leftright.get( i ) == LEFT ){
                System.out.print( "L" );
            }
            else if( leftright.get( i ) == RIGHT ){
                System.out.print( "R" );
            }
            else{
                System.out.print( "?" );
            }
            System.out.print( " " );
        }
        System.out.println();

        // now we adjust colors
        adjustColorAfterRemoving( history, leftright, node );

        return true;
    }

    /**
     * does the simple relinking after parent, node, and heir have
     * been determined
     */
    private void remove( RedBlackTreeNode parent, RedBlackTreeNode node,
                         RedBlackTreeNode heir )
    {
        if( parent == null ){
            _root = heir;
        }
        else if( node.isLeftChild() ){
            parent.setLeft( heir );
        }
        else{
            parent.setRight( heir );
        }

        if( heir != null ){
            heir.setParent( parent );
        }
    }

    /**
     * adjusts the color
     */
    private void adjustColorAfterRemoving( RedBlackTreeNode parent,
                                           RedBlackTreeNode deleted,
                                           RedBlackTreeNode heir )
    {
        // if we delete a red node, we don't do anything because
        // our "black height" does not change
        //
        if( deleted != null && deleted.isRed() ) return;

        // case Red
        //
        // if the heir is red, color it black and be done
        //
        // this happens when we delete a black node with one child
        // (when a black node has only one child, the child is
        // always red)
        if( heir != null && heir.isRed() ){
            heir.setColorBlack();
            return;
        }

        // from this point on, heir is null (and black)
        //
        // and heir will never turn red

        while( true ){
            // check for parent
            //
            // a null parent means can't check for siblings
            //
            // it also means we are done with coloring
            if( parent == null ) return;

            RedBlackTreeNode sibling;
            if( parent.getLeft() == heir ){
                sibling = parent.getRight();
            }
            else{
                sibling = parent.getLeft();
            }

            // case 1
            //
            //     if heir (1b) is black
            // and sibling of heir is red
            //
            // rotate about parent away from sibling
            // color parent red
            // color sibling black
            // and recheck (with new sibling)
            //
            //                2b                            4b
            //               *  *                          *  *
            //             1b    4r         ==>          2r    5b
            //                  *  *                    *  *
            //                3b    5b                1b    3b
            //
            if( sibling != null && sibling.isRed() ){
                parent.setColorRed();
                sibling.setColorBlack();
                if( parent.getLeft() == sibling ){
                    rotateRight( parent );
                }
                else{
                    rotateLeft( parent );
                }
                continue;
            }

            RedBlackTreeNode opp_nephew;
            RedBlackTreeNode sam_nephew;
            if( parent.getLeft() == sibling ){
                opp_nephew = sibling.getLeft();
                sam_nephew = sibling.getRight();
            }
            else{
                opp_nephew = sibling.getRight();
                sam_nephew = sibling.getLeft();
            }

            // case 4
            //
            //     if heir (1b) is black 
            // and sibling (4b) is black
            // and sibling's opposite-side child (5r) is red
            //
            // rotate about parent away from sibling
            // set parent black
            // set sibling to parent's original color
            // set original sibling's opposide child black
            // //and recheck using parent as heir
            //
            //                2!                            4!
            //               *  *                          *  *
            //             1b    4b         ==>          2b    5b
            //                  *  *                    *  *
            //                3?    5r                1b    3?
            //
            if( opp_nephew != null && opp_nephew.isRed() ){
                sibling.setColor( parent.getColor() );
                parent.setColorBlack();
                opp_nephew.setColorBlack();
                if( parent.getLeft() == sibling ){
                    rotateRight( parent );
                }
                else{
                    rotateLeft( parent );
                }

                //heir = parent;
                //parent = heir.getParent();
                //continue;
                return;
            }

            // case 3
            //
            //     if heir (1b) is black
            // and sibling (4b) is black
            // and sibling's same-side child (3r) is red
            // and sibling's opposite-side child (5b) is black
            //
            // rotate about sibling away from the same-side child (or heir)
            // set original sibling to red
            // set original sibling's same-side child to black
            // and recheck (with new sibling and sibling's opp-child)
            //
            //                2?                            2?
            //               *  *                          *  *
            //             1b    4b                      1b    3b
            //                  *  *                             *
            //                3r    5b                            4r
            //                                                      *
            //                                                       5b

            // (we already made sure sibling's opposite-side child is
            // black by checking case 4 first)
            if( sam_nephew != null && sam_nephew.isRed() ){
                sibling.setColorRed();
                sam_nephew.setColorBlack();
                if( parent.getLeft() == sibling ){
                    rotateLeft( sibling );
                }
                else{
                    rotateRight( sibling );
                }
                continue;
            }

            // case 2a
            //
            //     if heir (1b) is black
            // and sibling (4b) is black
            // and both nephews (3b), (5b) are black
            // and parent (2b) is black
            //
            // we set sibling (4b) to red
            // and recheck using original parent as heir
            //
            //                2b                            2b
            //               *  *                          *  *
            //             1b    4b                      1b    4r
            //                  *  *                          *  *
            //                3b    5b                      3b    5b
            //
            
            // we already took care of the cases where any of the
            // nephews are red
            //
            // from this point on, nephews are black (or null)
            
            if( parent.isBlack() ){
                sibling.setColorRed();
                heir = parent;
                parent = heir.getParent();
                continue;
            }
            
            // case 2b
            //
            //     if heir (1b) is black
            // and sibling (4b) is black
            // and both nephews (3b), (5b) are black
            // and parent (2r) is red
            //
            // we set sibling (4b) to red
            // set parent (2r) to black
            // and we are done
            //
            //                2r                            2b
            //               *  *                          *  *
            //             1b    4b                      1b    4r
            //                  *  *                          *  *
            //                3b    5b                      3b    5b
            //

            // we already took care of the cases where any of the
            // nephews are red
            //
            // from this point on, nephews are black (or null)

            if( parent.isRed() ){
                sibling.setColorRed();
                parent.setColorBlack();
                return;
            }

            throw new RuntimeException( "Unexpected case after romoval" );
        }
    }

    /**
     * removes a leaf node or a "half-leaf" node
     *
     * name of the function taken from example by Michael Conrad and
     * John Franco
     *
     * @param node the node to remove.  do not pass null!
     *
     * @return true if a node is pruned.  if the node has two children,
     *         then nothing is done, and the function returns false
     */
    protected boolean prune( RedBlackTreeNode node )
    {
        RedBlackTreeNode parent = node.getParent();
        RedBlackTreeNode heir = null;

        // deletion case d-1
        //
        // the node to be removed (A) has no children (is a leaf)
        //
        //          B                   B
        //         * *        ==>        *
        //        A   C                   C
        //
        
        if( node.isLeaf() ){
            remove( parent, node, null );
            adjustColorAfterRemoving( parent, node, heir );
            return true;
        }

        // deletion case d-2
        //
        // the node to be removed (B) has one child
        //
        //          D                   D
        //         * *        ==>      * *
        //        B   E               A   E
        //       *
        //      A
        //                    or
        //          D                   D
        //         * *        ==>      * *
        //        B   E               C   E
        //         *
        //          C

        if( node.getLeft() != null && node.getRight() == null ){
            heir = node.getLeft();
            remove( parent, node, heir );
            adjustColorAfterRemoving( parent, node, heir );
            return true;
        }
        if( node.getRight() != null && node.getLeft() == null ){
            heir = node.getRight();
            remove( parent, node, heir );
            adjustColorAfterRemoving( parent, node, heir );
            return true;
        }

        // ---- THIS FUNCTION DOES NOT HANDLE CASE D-3 ----

        // deletion case d-3
        //
        // the node to be removed (D) has both children
        //
        //          D                   D                   B
        //         * *        ==>      * *        ==>      * *
        //        B   E               A   E  + B          A   E
        //       * *         case      *                   *
        //      A   C        d-2        C       replace     C
        
        // ---- THIS FUNCTION DOES NOT HANDLE CASE D-3 ----

        return false;
    }

    /**
     * remove is very different from add
     */
    public boolean remove( Object val )
    {
        RedBlackTreeNode node;
        node = get( val );

        // can't find it...
        if( node == null ) return false;

        if( prune( node ) ) return true;

        // prune will take care of cases d-1 and d-2

        // deletion case d-3
        //
        // the node to be removed (D) has both children
        //
        //          D                   D                   B
        //         * *        ==>      * *        ==>      * *
        //        B   E               A   E  + B          A   E
        //       * *         case      *                   *
        //      A   C        d-2        C       replace     C

        // regarding heir selection:
        //
        // in one internet example, the author chooses the "next"
        // largest node usless it is a black leaf

        // let's try always choosing "next"

        RedBlackTreeNode heir;

        heir = node.getNext();

        // once "next" is found (next is not necessarily the right
        // child, but in case d-3, is also not null), we prune it
        //
        // due to the property of the "getNext" algorithm, heir 
        // can only be a leaf or "half-leaf".

        if( !prune( heir ) ){
            throw new RuntimeException( "Tried to prune a two-child node" );
        }

        // now replace the contents of the node (to be deleted) with 
        // the heir
        //
        // there is no relinking
        
        node.setVal( heir.getVal() );

        return true;
    }
}

