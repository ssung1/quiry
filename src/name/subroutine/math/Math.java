package name.subroutine.math;

public class Math
{
    public static long pow( long n, int exp )
    {
        int i;
        long result;
        
        if( exp < 0 ){
            return 0;
        }
        
        result = 1;
        for( i = 0; i < exp; i++ ){
            result *= n;
        }
        return result;
    }

    /**
     * returns the smallest integer of log(n)
     * or -1 if undefined
     */
    public static long log_floor( long n, int base )
    {
        long log;
        long val;
        
        log = 0;
        val = 1;
        while( true ){
            if( val > n ){
                return log - 1;
            }
            if( val == n ){
                return log;
            }
            log++;
            val = val * base;
        }
    }
}
