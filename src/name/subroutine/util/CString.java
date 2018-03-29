package name.subroutine.util;

import java.util.*;
import java.text.*;

/**
 * This class is a C-style string, which is an array
 * of bytes, not chars
 */
public class CString
{
    /**
     * Returns the length of a null-terminated string
     */
    public static int strlen( byte[] str )
    {
	int i;
	i = 0;
	while( true ){
	    if( str[i] == 0 ) return i;
	    i++;
	    if( i >= str.length ) return i;
	}
    }

    /**
     * Fits a string into an array of bytes, left justified, C style!
     *
     * @param filler: used if the given string is shorter than length
     * @param min: minimum length after fitting; -1 means no minimum
     * @param max: maximum length after fitting; -1 means no maximum
     *
     * if min is greater than max, this is the result:
     * if the string is less than min, it'll be fit to length min
     * if the string is greater than or equal to min, it'll be fit to max
     *
     * exception is not thrown because something like this is not
     * critical
     */
    public static byte[] leftFit( byte[] dest, String str,
				  int min, int max, byte filler )
    {
	if( str == null ){
	    str = "";
	}

	char charbuf[] = str.toCharArray();
	int strlen = charbuf.length;

	int diff;
	diff = min - strlen;

	if( diff > 0 && min >= 0 ){
	    for( int i = 0; i < strlen; i++ ){
		dest[i] = (byte)charbuf[i];
	    }
	    java.util.Arrays.fill( dest, strlen, min, filler );
	    return dest;
	}

	diff = max - strlen;
	if( diff < 0 && max >= 0 ){
	    for( int i = 0; i < max; i++ ){
		dest[i] = (byte)charbuf[i];
	    }
	    return dest;
	}

	// when diff == 0
	for( int i = 0; i < max; i++ ){
	    dest[i] = (byte)charbuf[i];
	}
	return dest;
    }
    
    public static byte[] leftFit( byte[] dest, String str, int len,
				  byte filler )
    {
	return leftFit( dest, str, len, len, filler );
    }

    public static byte[] leftFit( byte[] dest, String str, int len )
    {
	return leftFit( dest, str, len, (byte)0 );
    }

    public static byte[] leftFit( byte[] dest, String str )
    {
	return leftFit( dest, str, str.length() + 1 );
    }

    /**
     * Fits a cstyle string into another cstyle string
     *
     * @param filler: used if the given string is shorter than length
     * @param min: minimum length after fitting; -1 means no minimum
     * @param max: maximum length after fitting; -1 means no maximum
     *
     * if min is greater than max, this is the result:
     * if the string is less than min, it'll be fit to length min
     * if the string is greater than or equal to min, it'll be fit to max
     *
     * exception is not thrown because something like this is not
     * critical
     */
    public static byte[] leftFit( byte[] dest, byte[] str,
				  int min, int max, byte filler )
    {
	if( str == null ){
	    str = new byte[1];
	    str[0] = 0;
	}

	int strlen = strlen( str );

	int diff;
	diff = min - strlen;

	if( diff > 0 && min >= 0 ){
	    for( int i = 0; i < strlen; i++ ){
		dest[i] = str[i];
	    }
	    java.util.Arrays.fill( dest, strlen, min, filler );
	    dest[min] = (byte)0;
	    return dest;
	}

	diff = max - strlen;

	for( int i = 0; i < max; i++ ){
	    dest[i] = str[i];
	}
	dest[max] = (byte)0;
	return dest;
    }
    
    public static byte[] leftFit( byte[] dest, byte[] str,
				  int len, byte filler )
    {
	return leftFit( dest, str, len, len, filler );
    }

    public static byte[] leftFit( byte[] dest, byte[] str, int len )
    {
	return leftFit( dest, str, len, (byte)' ' );
    }

    public static byte[] toCString( byte[] dest, Object o )
    {
	String str = String.valueOf( o );
	return leftFit( dest, str );
    }

    public static byte[] toCString( byte[] dest, String str )
    {
	return leftFit( dest, str );
    }

    /**
     * reverses a c-style string
     */
    public static byte[] reverse( byte[] str )
    {
	int head;
	int tail;
	byte tmp;

	head = 0;
	for( tail = 0; str[tail] != 0; tail++ ){
	    // do nothing
	}
	tail--;

	for( ; tail > head; tail--, head++ ){
	    tmp = str[tail];
	    str[tail] = str[head];
	    str[head] = tmp;
	}
	return str;
    }

    public static byte toupper( byte c )
    {
        if( (byte)'a' <= c && c <= (byte)'z' ){
            return (byte)(c + (byte)'A' - (byte)'a');
        }
        return c;
    }

    public static boolean isdigit( byte c )
    {
        if( (byte)'0' <= c && c <= (byte)'9' ) return true;
        return false;
    }

    public static boolean isalpha( byte c )
    {
        if( (byte)'a' <= c && c <= (byte)'z' ) return true;
        return false;
    }

    public static int atoi( byte c )
    {
        if( isdigit( c ) ){
            return c - (byte)'0';
        }
        if( isalpha( c ) ){
            return toupper( c ) - (byte)'A' + 10;
        }
        return -1;
    }

    public static long atoi( byte[] s, int offset )
    {
        long num;
        int sp;
        sp = offset;
        
        byte sign;
        
        boolean has_seen_digits = false;
        num = 0;
        sign = 1;
        while( s[sp] != 0 ){
            byte c = s[sp];
            if( !isdigit( c ) && has_seen_digits ){
                break;
            }
            if( c == '-' ){
                sign = (byte)(-sign);
            }
            if( isdigit( c ) ){
                has_seen_digits = true;
                num *= 10;
                num += c - (byte)'0';
            }
            sp++;
        }
        return num * sign;
    }

    public static long atoi( byte[] s, int offset, int maxlen )
    {
        long num;
        int sp;
        int count;
        sp = offset;
        
        byte sign;
        
        boolean has_seen_digits = false;
        num = 0;
        sign = 1;
        count = 0;
        while( s[sp] != 0 && count < maxlen ){
            byte c = s[sp];
            if( !isdigit( c ) && has_seen_digits ){
                break;
            }
            if( c == '-' ){
                sign = (byte)(-sign);
            }
            if( isdigit( c ) ){
                has_seen_digits = true;
                num *= 10;
                num += c - (byte)'0';
            }
            sp++;
            count++;
        }
        return num * sign;
    }

    public static double atof( byte[] s, int offset )
    {
        double num;
        int sp;
        int sign;
        boolean has_seen_digits;
        boolean has_seen_period;
        int cnt_after_period;

        sp = offset;
        num = 0;
        sign = 1;
        has_seen_digits = false;
        has_seen_period = false;
        cnt_after_period = 0;

        while( s[sp] != 0 ){
            byte c = s[sp];
            if( !isdigit( c ) && c != (byte)'.' && has_seen_digits ){
                break;
            }
            if( c == '.' && has_seen_period ){
                break;
            }

            if( c == '-' ){
                sign = -sign;
            }
            if( isdigit( c ) ){
                has_seen_digits = true;
                num *= 10;
                num += c - (byte)'0';
                if( has_seen_period ){
                    cnt_after_period++;
                }
            }
            if( c == '.' ){
                has_seen_period = true;
            }
            sp++;
        }
        for( int i = 0; i < cnt_after_period; i++ ){
            num /= 10;
        }
        return num * sign;
    }

    public static double atof( byte[] s, int offset, int maxlen )
    {
        double num;
        int sp;
        int sign;
        boolean has_seen_digits;
        boolean has_seen_period;
        int cnt_after_period;
        int count;

        sp = offset;
        num = 0;
        sign = 1;
        has_seen_digits = false;
        has_seen_period = false;
        cnt_after_period = 0;
        count = 0;

        while( s[sp] != 0 && count < maxlen ){
            byte c = s[sp];
            if( !isdigit( c ) && c != (byte)'.' && has_seen_digits ){
                break;
            }
            if( c == '.' && has_seen_period ){
                break;
            }

            if( c == '-' ){
                sign = -sign;
            }
            if( isdigit( c ) ){
                has_seen_digits = true;
                num *= 10;
                num += c - (byte)'0';
                if( has_seen_period ){
                    cnt_after_period++;
                }
            }
            if( c == '.' ){
                has_seen_period = true;
            }
            sp++;
            count++;
        }
        for( int i = 0; i < cnt_after_period; i++ ){
            num /= 10;
        }
        return num * sign;
    }

    /**
     * Interprets the given byte array as a long integer,
     * big-endian
     */
    public static long toLong( byte[] str, int byte_offset,
                               int bit_offset, int bit_count )
    {
        int mask;
        int i;
        int val;
        
        val = 0;
        mask = 1 << bit_offset;
        for( i = 0; i < bit_count; i++ ){
            byte c = str[byte_offset];
            val = val << 1;
            if( (c & mask) > 0 ){
                val++;
            }
            //
            // rotate to the right
            // and grab next byte if needed
            //
            if( mask == 1 ){
                mask = 0x0080;
                byte_offset++;
            }
            else{
                mask = mask >> 1;
            }
        }
        return val;
    }

    /**
     * Creates an alpha representation of an integer
     */
    static byte itoa( int n )
    {
        n = Math.abs( n );
        if( n < 10 ){
            return (byte)(n + '0');
        }
        return (byte)(n - 10 + 'a');
    }

    /**
     * Creates an alpha representation of an integer
     *
     * @param dst is where the result should be
     */
    public static byte[] itoa( byte[] dst, long n, int radix )
    {
        int sign = 1;
        int result_offset;

        result_offset = 0;
        if( n < 0 ){
            n = -n;
            sign = -sign;
        }
        
        while( true ){
            dst[result_offset] = itoa( (int)(n % radix) );
            n /= radix;
            result_offset++;
            if( n == 0 ) break;
        }
        
        if( sign < 0 ){
            dst[result_offset] = (byte)'-';
            result_offset++;
        }
        dst[result_offset] = 0;
        reverse( dst );
        
        return dst;
    }

    /**
     * Get a 64 bit number from an array of bytes interpreted as
     * a TBCD string
     *
     * Also, it returns the value as a string in destination buffer
     *
     * The smallest unit in a TBCD string is the nybble
     *
     * Note:
     *     TBCD is like reading hex, except 'A' is interpreted
     *     as 0, and 'B' to 'F' do not appear.
     *
     * @param start_nybble can either be 1 or 0, 1 being the most
     *        significant nybble (of the two)
     */
    public static long toTbcd( byte[] dst, byte[] buf, int start_byte,
                               int start_nybble, int nybble_count )
    {
        int i;
        int val;
        int tbcd;
        int byte_count;
        int nybble_index = start_nybble;
        
        val = 0;
        for( i = 0, byte_count = start_byte; i < nybble_count; i++ ){
            if( nybble_index == 1 ){
                tbcd = (int)toLong( buf, byte_count, 7, 4 );
                nybble_index = 0;
            }
            else{
                tbcd = (int)toLong( buf, byte_count, 3, 4 );
                nybble_index = 1;
                byte_count++;
            }
            
            if( tbcd == 0 ){
                dst[i] = (byte)0;
                return val;
            }
            
            // adjust tbcd
            // an original tbcd value of 0 means end of string (see above)
            // an original tbcd value of 10 means 0
            if( tbcd == 10 ){
                tbcd = 0;
            }
            dst[i] = itoa( tbcd );
            val = val * 10;
            val += tbcd;
        }
        return val;
    }

    /**
     * Like itoa but does not add terminating null
     */
    public static byte[] intom( byte[] dst, int dst_offset,
                                long n, int len, int radix )
    {
        int cnt  = 0;
        
        if( n < 0 ){
            n = -n;
            dst[dst_offset] = (byte)'-';
            dst_offset++;
            //
            // sign takes up space too
            //
            cnt++;
        }
        
        //
        // f is the largest value that is a power of radix
        //
        long f;
        if( n > 0 ){
            f = name.subroutine.math.Math.pow( radix,
                (int)name.subroutine.math.Math.log_floor( n, radix ) );
        }
        else{
            f = 1;
        }
        
        while( cnt < len ){
            if( f > 0 ){
                long nn = n / f;
                dst[dst_offset] = itoa( (int)(nn % radix) );
            }
            else{
                dst[dst_offset] = ' ';
            }
            
            f /= radix;
            dst_offset++;
            cnt++;
        }
        
        return dst;
    }

    /**
     * Returns the smallest index greater than offset that contains
     * the byte c in the string or -1 if not found
     */
    public static int strchr( byte[] str, int offset, byte c )
    {
        for( int i = offset; i < str.length; i++ ){
            byte x = str[i];
            if( x == 0 ) break;
            if( x == c ) return i;
        }
        return -1;
    }

    /**
     * Selects only the wanted bytes from a string of bytes
     *
     * Good for filtering out symbols in phone numbers
     */
    public static byte[] select( byte[] dst, byte[] src,
                                 int src_offset, byte[] mask )
    {
        int dst_offset = 0;
        for( ; src[src_offset] != (byte)0; src_offset++ ){
            if( strchr( mask, 0, src[src_offset] ) >= 0 ){
                dst[dst_offset] = src[src_offset];
                dst_offset++;
            }
        }
        dst[dst_offset] = (byte)0;
        
        return dst;
    }

    /**
     * Converts a c-style string into a java string
     */
    public static String toString( byte[] str )
    {
	if( str == null ){
	    return "";
	}
	int len = strlen( str );
	try{
	    return new String( str, 0, len, "UTF-8" );
	}
	catch( Exception ex ){
	    return new String( str, 0, len );
	}
    }
}
