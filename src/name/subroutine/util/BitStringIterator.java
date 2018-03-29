package name.subroutine.util;

/**
 * Iterates the given byte array one bit at a time
 */
public class BitStringIterator
{
    /**
     * number of bits to iterate
     */
    int _size;
    byte[] _buf;
    
    int _byte_idx;
    /**
     * ranges from 7 to 0 (backwards)
     */
    int _bit_idx;

    int _cnt;

    byte _cur_byte;
    int _cur_mask;

    public BitStringIterator( byte[] buf, int size )
    {
        _init( buf, 0, 0, size );
    }

    public BitStringIterator( byte[] buf, int bit_offset, int size )
    {
        _init( buf, 0, bit_offset, size );
    }

    public BitStringIterator( byte[] buf, int byte_offset,
                              int bit_offset, int size )
    {
        _init( buf, byte_offset, bit_offset, size );
    }

    void _init( byte[] buf, int byte_offset, int bit_offset, int size )
    {
        _buf = buf;
        _size = size;

        int[] idx = BitString.toByteBitIdx( bit_offset );

        _byte_idx = idx[0] + byte_offset;
        _bit_idx = idx[1];

        _cnt = 0;

        _cur_mask = 1 << _bit_idx;
    }

    public byte next()
    {
        // get _cur_byte if first time accessing it
        if( _cur_mask == 0x80 ){
            _cur_byte = _buf[_byte_idx];
        }

        int retval = _cur_byte & _cur_mask;

        _cur_mask = _cur_mask >>> 1;
        --_bit_idx;
        ++_cnt;
        if( _cur_mask == 0 ){
            _cur_mask = 0x80;
            _bit_idx = 7;
            ++_byte_idx;
        }

        if( retval == 0 ){
            return 0;
        }
        else{
            return 1;
        }
    }
    
    public boolean hasNext()
    {
        return _size > _cnt;
    }
}
