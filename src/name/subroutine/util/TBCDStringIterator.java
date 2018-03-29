package name.subroutine.util;

/**
 * Iterates an array of bytes as TBCD characters
 */
public class TBCDStringIterator
{
    /**
     * number of nybbles
     */
    int _size;

    byte[] _buf;

    int _byte_idx;

    /**
     * ranges from 1 to 0 (backwards)
     */
    int _nyb_idx;

    int _cnt;

    byte _cur_byte;
    int _cur_mask;

    public TBCDStringIterator( byte[] buf, int size )
    {
        _init( buf, 0, 0, size );
    }

    public TBCDStringIterator( byte[] buf, int bit_offset, int size )
    {
        _init( buf, 0, bit_offset, size );
    }

    public TBCDStringIterator( byte[] buf, int byte_offset,
                               int bit_offset, int size )
    {
        _init( buf, byte_offset, bit_offset, size );
    }
    
    void _init( byte[] buf, int byte_offset,
                int nyb_offset, int size )
    {
        _buf = buf;
        _size = size;
        
        int[] idx;
        idx = TBCDString.toByteNybbleIdx( nyb_offset );
        
        _byte_idx = idx[0] + byte_offset;
        _nyb_idx = idx[1];

        _cnt = 0;
        
        _cur_mask = 0x0f << (_nyb_idx * 4);
    }    

    public char next()
    {
        if( _cur_mask == 0xf0 ){
            _cur_byte = _buf[_byte_idx];
        }

        int retval = _cur_byte & _cur_mask;
        
        _cur_mask = _cur_mask >> 4;
        --_nyb_idx;
        ++_cnt;
        if( _cur_mask == 0 ){
            // previous mask was low nybble
            _cur_mask = 0xf0;
            _nyb_idx = 1;
            ++_byte_idx;
        }
        else{
            // previous mask was high nybble, so we shift
            retval >>= 4;
        }
        
        // translate
        if( retval == (byte)10 ){
            retval = '0';
        }
        else if( retval == 0 ){
            retval = ' ';
        }
        else{
            retval += '0';
        }
        
        return (char)retval;
    }

    public boolean hasNext()
    {
        return _size > _cnt;
    }
}
