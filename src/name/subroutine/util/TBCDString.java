package name.subroutine.util;

/**
 * Strinf of TBCD characters, each occupies 1 nybble of storage space
 */
public class TBCDString
{
    /**
     * converts absolute nybble index to a combination of byte and 1-0
     * nybble index
     *
     * @return array of 2 integers, byte index and 1-0 nybble index
     */
    public static int[] toByteNybbleIdx( int abs_nyb_idx )
    {
        int[] retval = new int[2];

        retval[0] = abs_nyb_idx / 2;
        retval[1] = abs_nyb_idx % 2;
        retval[1] = 1 - retval[1];
        
        return retval;
    }

    /**
     * Attempted to speed up this function
     *
     * See original version to see how it really should be done.
     */
    public static void toBytes( char[] dst, byte[] buf, int byte_offset,
                                int nyb_offset, int count )
    {
        int byte_idx;
        int nyb_idx;
        
        int[] idx;
        idx = TBCDString.toByteNybbleIdx( nyb_offset );
        byte_idx = idx[0] + byte_offset;
        nyb_idx = idx[1];
        
        for( int i = 0; i < count; ++i ){
            byte cur_byte = buf[byte_idx];
            int c;
            // 0 means high nybble
            // 1 means low nybble
            //
            // since this is a "tbcd string"
            if( nyb_idx != 0 ){
                c = (cur_byte & 0xf0) >> 4;
                nyb_idx = 0;
            }
            else{
                c = (cur_byte & 0x0f);
                nyb_idx = 1;
                ++byte_idx;
            }
        
            // translate
            if( c == (byte)10 ){
                c = '0';
            }
            else if( c == 0 ){
                c = ' ';
            }
            else{
                c += '0';
            }
            
            dst[i] = (char)c;
        }

        // Original Version
        // TBCDStringIterator it( (char *)buf, offset, count );
        // int idx = 0;
        // while( it.hasNext() ){
        //     dst[idx] = (char)it.next();
        //     ++idx;
        // }    
    }

}

