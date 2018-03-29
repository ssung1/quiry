package name.subroutine.math;

import java.text.*;

/**
 * This is a mathy matrix, with numbers inside!
 */
public class Matrix
{
    /**
     * Our internal representation of the matrix
     */
    public double[] _m;

    /**
     * Number of columns
     */
    public int _col_cnt;

    /**
     * Row count is dependent on "order" (number of total elements)
     * and column count.
     */
    public int getRowCnt()
    {
        return _m.length / _col_cnt;
    }

    public Matrix( double[] init, int col_cnt )
    {
        _m = init;
        _col_cnt = col_cnt;
    }

    /**
     * Multiplies all the elements in the given row by the given real
     * number 
     */
    void multiplyRow( int row, double real )
    {
        for( int i = row * _col_cnt; i < (row + 1) * _col_cnt; i++ ){
            _m[i] = _m[i] * real;
        }
    }

    /**
     * Subtract the contents of row_a by contents of row_b
     * and replaces row_a with the result
     */
    void subtractRows( int row_a, int row_b )
    {
        int a_idx;
        int b_idx;

        for( a_idx = row_a * _col_cnt, b_idx = row_b * _col_cnt;
             a_idx < (row_a + 1) * _col_cnt;
             a_idx++, b_idx++ ){

            _m[a_idx] = _m[a_idx] - _m[b_idx];
        }
    }

    /**
     * Turns the matrix into echelon matrix.
     *
     * Only makes sense if you are solving equations
     */
    void echelon()
    {
        /*
         * here is how I'm going to do this
         *
         * for each [row]
         *     for each [column] that has a number smaller than [row]
         *         divide the whole row by m[ [row], [column] ]
         *         subtract [row] by the row with same number as [column]
         *     endloop
         *     divide the whole row by m[ [row], [row] ]
         * endloop
         */
        for( int r = 0; r < getRowCnt(); r++ ){
            for( int c = 0; c < r; c++ ){
                multiplyRow( r, (1 / getValue( r, c )) );
                subtractRows( r, c );

                System.out.println( this );
                System.out.println();
            }

            multiplyRow( r, (1 / getValue( r, r )) );
            System.out.println( this );
            System.out.println();
        }
    }

    /**
     * solves after it's in echelon form
     *
     *   1  1  1  3   
     *   0  1  1  2
     *   0  0  1  1
     *
     * generally, for each equation, there is one solution
     * we define sol[col] to be the value of the unknown at
     * column [col]
     */
    public double[] solveEchelon()
    {
        int rowcnt = getRowCnt();
        double sol[] = new double[ getRowCnt() ];

        // now solve each solution, from the bottom up
        for( int n = rowcnt - 1; n >= 0; n-- ){
            // this is just the number at the right of the
            // equal sign...
            // 
            // it is always the number in the last column
            double sum;
            sum = getValue( n, rowcnt );

            // now for each sum, we must subtract each term
            // until we arrive at the unknown we are looking
            // for.  Notice that for the bottom row, we don't
            // need to subtract any terms.

            for( int i = rowcnt - 1; i > n; i-- ){
                sum = sum - sol[i] * getValue( n, i );
            }
            
            sol[n] = sum;

        }
        return sol;
    }

    public double getValue( int row, int col )
    {
        return _m[ row * _col_cnt + col ];
    }

    public String toString()
    {
        StringBuffer sb = new StringBuffer();

        for( int i = 0; i < _m.length; i++ ){
            String s;
            s = name.subroutine.util.Variant.fit( _m[i], 10, 2 );

            if( i > 0 && i % _col_cnt == 0 ){
                sb.append( System.getProperty( "line.separator" ) );
            }
            sb.append( s );
        }

        return sb.toString();
    }



    public static void main( String[] argv )
    {
        Matrix m;
        m = new Matrix( new double[] {
               2,       1,      3,        6,
               3,       1,      2,        6,
               4,       4,      4,       12, }, 4 );

        System.out.println( m );
        System.out.println();

        m.echelon();
        double sol[] = m.solveEchelon();

        for( int i = 0; i < sol.length; i++ ){
            System.out.println( "unknown " + (char)('x' + i) + " = " +
                                sol[i] );
        }
    }
}
