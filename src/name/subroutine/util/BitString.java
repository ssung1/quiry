package name.subroutine.util;

/**
 * String of bits
 */
public class BitString
{
    /**
     * max size, in bytes (because we need it to allocate memory)
     */
    int _max_byte_size;

    /**
     * size, in bits
     */
    int _bit_size;

    byte[] _buf;

    /**
     * converts absolute bit index to a combination of byte
     * and 7-to-0 bit index
     *
     * @return array of 2 integers, byte index and 7-to-0 bit index
     */
    public static int[] toByteBitIdx( int abs_bit_idx )
    {
        int[] retval = new int[2];

        retval[0] = abs_bit_idx / 8;
        retval[1] = abs_bit_idx % 8;
        retval[1] = 7 - retval[1];
        
        return retval;
    }

    public BitString()
    {
        _bit_size = 0;
        _max_byte_size = 0;
        _buf = new byte[_max_byte_size];
    }

    void _resize( int max )
    {
        byte[] new_buf;
        new_buf = new byte[max];
        
        System.arraycopy( _buf, 0, new_buf, 0, _max_byte_size );
        
        _max_byte_size = max;
        _buf = new_buf;
    }
    
    /**
     * resize if new size (in bits) is bigger than max
     */
    void _conditionalResize( int new_size )
    {
        int new_byte_size = (new_size + 7) / 8;
        if( new_byte_size > getMaxByteSize() ){
            _resize( new_byte_size );
        }
    }

    /**
     * resize if added_bit + current size is bigger than max
     */
    void _conditionalResizeAdded( int added_bit )
    {
        int new_size = getSize() + added_bit;
        _conditionalResize( new_size );
    }

    public int getSize()
    {
        return _bit_size;
    }
    
    public int getMaxByteSize()
    {
        return _max_byte_size;
    }

    public void append( byte val )
    {
        _conditionalResizeAdded( 1 );
        
        setBitAt( getSize(), val );
        
        ++_bit_size;
    }

    public void append( byte[] buf, int byte_offset, 
                        int bit_offset, int count )
    {
        BitStringIterator it;
        it = new BitStringIterator( buf, byte_offset, bit_offset, count );
        while( it.hasNext() ){
            append( it.next() );
        }
    }

    public void append( byte[] buf, int count )
    {
        BitStringIterator it;
        it = new BitStringIterator( buf, count );
        while( it.hasNext() ){
            append( it.next() );
        }
    }

    public byte[] getBytes( int byte_offset )
    {
        byte[] retval = new byte[_buf.length - byte_offset];
        System.arraycopy( _buf, byte_offset,
                          retval, 0,
                          _buf.length - byte_offset );
        return retval;
    }

    public void assignBytes( byte[] buf, int byte_offset, int byte_count )
    {
        if( getMaxByteSize() < byte_count ){
            _resize( byte_count );
        }
        System.arraycopy( buf, byte_offset, _buf, 0, byte_count );
    }

    public BitStringIterator iterator( int begin, int count )
    {
        return new BitStringIterator( _buf, begin, count );
    }

    public long toLong( int offset, int count )
    {
        long retval = 0;
        BitStringIterator it = iterator( offset, count );
        while( it.hasNext() ){
            retval <<= 1;
            retval += it.next();
        }
        return retval;
    }

    public long toLong( byte[] buf, int offset, int count )
    {
        long retval = 0;
        BitStringIterator it;
        it = new BitStringIterator( buf, offset, count );
        while( it.hasNext() ){
            retval <<= 1;
            retval += it.next();
        }
        return retval;
    }

    /**
     * index
     *
     * byte  0      1       2
     * bit   76543210765432107654321
     */
    byte bitAt( int byte_idx, int bit_idx )
    {
        byte c = _buf[byte_idx];
        int mask = 1 << bit_idx;

        if( (mask & c) != 0 ){
            return 1;
        }
        else{
            return 0;
        }
    }
    
    /**
     * index
     *
     * absolute_bit   0, 1, 2, ...
     */
    public byte bitAt( int abs_bit_idx )
    {
        int[] idx = toByteBitIdx( abs_bit_idx );
        return bitAt( idx[0], idx[1] );
    }

    /**
     * index
     *
     * byte  0      1       2
     * bit   76543210765432107654321
     */
    void setBitAt( int byte_idx, int bit_idx, byte val )
    {
        byte c = _buf[byte_idx];
        int mask = 1 << bit_idx;
        
        // with 1, it is sufficient to set a single bit with OR
        //
        // mask only has one bit on, and we set it
        //
        // original 1010       1010
        // mask     0100       1000
        // ----------------or--------------
        //          1110       1010
        //
        // with 0, we need to XOR the mask (with 11111111b) first,
        // then do an AND
        //
        // original 1010       1010
        // mask     0100       1000
        // ~mask    1011       0111
        // ---------------and--------------
        //          1010       0010
        //
        if( val == 0 ){
            mask ^= 0xff;
            c = (byte)(mask & c);
        }
        else{
            c = (byte)(mask | c);
        }
        
        _buf[byte_idx] = c;
    }

    public void setBitAt( int abs_bit_idx, byte val )
    {
        int[] idx;
        idx = toByteBitIdx( abs_bit_idx );
        setBitAt( idx[0], idx[1], val );
    }

    /**
     * Stores an int6, for count bits, into the bitstring, starting
     * at abs_bit_idx.  The result is "right-justified" and "0-padded"
     */
    public void setLongAt( int abs_bit_idx, long val, int count )
    {
        byte[] bits = new byte[count];
        
        for( int i = count - 1; i >= 0; --i ){
            if( (val & 1) != 0 ){
                bits[i] = 1;
            }
            else{
                bits[i] = 0;
            }
            val >>= 1;
        }
        
        for( int i = 0; i < count; ++i ){
            setBitAt( abs_bit_idx + i, bits[i] );
        }
    }
}
