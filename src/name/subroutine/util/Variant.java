package name.subroutine.util;

import java.util.*;
import java.text.*;

/**
 * This class represents a Variant, which is
 * a type that mutates to another type as needed.
 *
 * We can't actually use this type in our programs, but
 * this is a class used for type transformations and
 * miscellaneous string functions.
 */
public class Variant
{
    /**
     * Creates a string that uses escape codes for special characters
     */
    public static String escape( String a )
    {
	StringBuffer sb = new StringBuffer();

	// is this faster than using charAt?
	char[] aa;
	aa = new char[a.length()];

	a.getChars( 0, aa.length, aa, 0 );

	for( int i = 0; i < aa.length; i++ ){
	    switch( aa[i] ){
	    default:
		sb.append( aa[i] );
		break;
	    case '\n':
		sb.append( "\\n" );
		break;
	    case '\r':
		sb.append( "\\r" );
		break;
	    case '\t':
		sb.append( "\\t" );
		break;
	    case '\b':
		sb.append( "\\b" );
		break;
	    case '\"':
		sb.append( "\\\"" );
		break;
	    case '\'':
		sb.append( "\\\'" );
		break;
	    }
	}
	return sb.toString();
    }

    /**
     * Takes 1 character off both ends of the given string, if possible
     */
    public static String gnaw( String str )
    {
	int a;
	int b;
	a = 1;
	b = str.length() - 1;

	if( a > b ) return str;

	return str.substring( a, b );
    }

    /**
     * returns b + a + b.  Understands simple symbols that have
     * "opposite symbols" and will add that at the other end.
     * For example, grow( "a", "(" ) will return "(a)"
     *
     * @param a: the main string
     * @param b: the string to add at the both ends
     */
    public static String grow( String a, String b )
    {
	StringBuffer retval = new StringBuffer();

	Map m = new Hashtable();
	m.put( "(", ")" );
	m.put( "<", ">" );
	m.put( "[", "]" );
	m.put( "{", "}" );

	String negatron = (String)m.get( b );
	if( negatron == null ){
	    negatron = b;
	}
	retval.append( b ).append( a ).append( negatron );
	return retval.toString();
    }

    /**
     * Joins an array of strings into one, inserting
     * the hinge between each
     */
    public static String join( Object[] lst, String hinge )
    {
	StringBuffer retval = new StringBuffer();
	int i;
	for( i = 0; i < lst.length; i++ ){
	    if( i > 0 ){
		retval.append( hinge );
	    }
	    retval.append( lst[i] );
	}
	return retval.toString();
    }

    /**
     * Joins a list of strings into one, inserting
     * the hinge between each
     */
    public static String join( List lst, String hinge )
    {
	return join( lst.toArray( new Object[0] ), hinge );
    }

    /**
     * Same as split
     */
    public static String[] toArray( String str, String token )
    {
	return split( str, token );
    }

    /**
     * Splits a string into an array of string, seperated
     * by one or more given tokens
     */
    public static String[] split( String str, String token )
    {
	List lst = new ArrayList();
	int context = 0;
	int i;
	StringBuffer buf = new StringBuffer();

	for( i = 0; i < str.length(); i++ ){
	    char c;
	    c = str.charAt( i );

            // nothing
            if( context == 0 ){
                if( token.indexOf( c ) < 0 ){
                    buf.append( c );
                    context = 1;
                }
                else{
                    // (this will be empty)
                    lst.add( buf.toString() );
                    buf = new StringBuffer();
                    context = 2;
                }
                continue;
            }
            // has seen a non-separator
            else if( context == 1 ){
                if( token.indexOf( c ) < 0 ){
                    buf.append( c );
                }
                else{
                    lst.add( buf.toString() );
                    buf = new StringBuffer();
                    context = 2;
                }
                continue;
            }
            // has just seen a separator
            // this is the same as context 0 but we keep a separate
            // context in case we need to change it
            else if( context == 2 ){
                if( token.indexOf( c ) < 0 ){
                    buf.append( c );
                    context = 1;
                }
                else{
                    // (this will be empty)
                    lst.add( buf.toString() );
                    buf = new StringBuffer();
                    context = 2;
                }
                continue;
            }
	}

        lst.add( buf.toString() );

	return (String[])lst.toArray( new String[0] );
    }

    /**
     * Fits a number into a specified length, right justified
     *
     * calls fit( num, len, prec, 1 )
     */
    public static String fit( double num, int len, int prec )
    {
	return fit( num, len, prec, 1 );
    }

    /**
     * Fits a number into a specified length
     *
     * calls fit( num, len, prec, just, ' ' )
     */
    public static String fit( double num, int len, int prec, int just )
    {
	return fit( num, len, prec, just, ' ' );
    }

    /**
     * Fits a number into a specified length
     *
     * calls fit( num, len, len, prec, just, filler )
     */
    public static String fit( double num, int len, int prec,
			      int just, char filler )
    {
	return fit( num, len, len, prec, just, filler );
    }

    /**
     * Fits a number into a specified range of lengths
     *
     * @param just: 0=left, 1=right
     * @param filler: used if the given string is shorter than length
     * @param prec: number of digits to display after decimal point
     */
    public static String fit( double num, int min, int max, int prec,
			      int just, char filler )
    {
	String orig;

	StringBuffer sb = new StringBuffer();
	sb.append( "#0" );
	if( prec > 0 ){
	    sb.append( '.' );
	    for( int i = 0; i < prec; i++ ){
		sb.append( '0' );
	    }
	}

	DecimalFormat nf = new DecimalFormat( sb.toString() );
	//nf.setMinimumFractionDigits( prec );
	nf.setGroupingSize( 0 );
	orig = nf.format( num );

	// use the fit function, justify <just>, absolute length <len>,
	// filler <filler>
	orig = fit( orig, min, max, just, filler );

	return orig;
    }

    /**
     * Fits a string into a specified length
     * same as fit( str, len, 0, ' ' )
     */
    public static String fit( String str, int len )
    {
	return fit( str, len, 0, ' ' );
    }

    /**
     * Fits a string into a specified length
     * same as fit( str, len, just, ' ' )
     */
    public static String fit( String str, int len, int just )
    {
	return fit( str, len, just, ' ' );
    }

    /**
     * Fits a string into a specified length
     *
     * @param just: 0=left, 1=right
     * @param filler: used if the given string is shorter than length
     */
    public static String fit( String str, int len, int just, char filler )
    {
	return fit( str, len, len, just, filler );
    }

    /**
     * Fits a string into a specified range
     *
     * @param just: 0=left, 1=right
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
    public static String fit( String str, int min, int max,
			      int just, char filler )
    {
	if( str == null ){
	    str = "";
	}

	int diff;

	diff = min - str.length();
	if( diff > 0 && min >= 0 ){
	    StringBuffer sb = new StringBuffer();
	    char[] f = new char[diff];
	    java.util.Arrays.fill( f, filler );
	    if( just == 1 ){
		sb.append( f ).append( str );
	    }
	    else{
		sb.append( str ).append( f );
	    }
	    return sb.toString();
	}

	diff = max - str.length();
	if( diff < 0 && max >= 0 ){
	    if( just == 1 ){
		return str.substring( -diff );
	    }
	    else{
		return str.substring( 0, str.length() + diff );
	    }
	}

	return str;
    }

    /**
     * Capitalize a string
     * This function only changes the first letter to capital;
     * it does not set the remaining characters to lower case.
     */
    public static String capitalize( String s )
    {
	char[] char_array;

	char_array = s.toCharArray();

	// don't try to trick me with an empty string.  i can tell :p
	if( char_array.length <= 0 ){
	    return s;
	}

	char_array[0] = Character.toUpperCase( char_array[0] );
	return new String( char_array );
    }

    /**
     * Parses a string into date object in every way
     */
    public static java.util.Date toDate( String s )
	throws ParseException
    {
	DateFormat df;
	ParsePosition pp;
	java.util.Date retval;

	pp = new ParsePosition( 0 );

	df = DateFormat.getDateInstance( DateFormat.SHORT );
	pp.setIndex( 0 );
	retval = df.parse( s, pp );
	if( retval != null ) return retval;

	df = DateFormat.getDateInstance( DateFormat.MEDIUM );
	pp.setIndex( 0 );
	retval = df.parse( s, pp );
	if( retval != null ) return retval;

	df = DateFormat.getDateInstance( DateFormat.LONG );
	pp.setIndex( 0 );
	retval = df.parse( s, pp );
	if( retval != null ) return retval;

	df = DateFormat.getDateInstance( DateFormat.FULL );
	pp.setIndex( 0 );
	retval = df.parse( s, pp );
	if( retval != null ) return retval;

	df = new SimpleDateFormat( "M.d.y" );
	pp.setIndex( 0 );
	retval = df.parse( s, pp );
	if( retval != null ) return retval;

	df = new SimpleDateFormat( "y-M-d" );
	pp.setIndex( 0 );
	retval = df.parse( s, pp );
	if( retval != null ) return retval;

	// Made just for Oracle
	df = new SimpleDateFormat( "y-M-d H:m:s.S" );
	pp.setIndex( 0 );
	retval = df.parse( s, pp );
	if( retval != null ) return retval;

	df = new SimpleDateFormat( "yyyyMMdd" );
	pp.setIndex( 0 );
	retval = df.parse( s, pp );
	if( retval != null ) return retval;

	throw new ParseException( "Cannot parse " + s, 0 );
    }

    /**
     * Parses an integer into date object in every way
     */
    public static java.util.Date toDate( int d )
	throws ParseException
    {
	DateFormat df = new SimpleDateFormat( "yyyyMMdd" );

	String str;
	str = String.valueOf( d );

	return df.parse( str );
    }

    /**
     * Converts a date object into yyyymmdd integer
     */
    public static int toInt( java.util.Date d )
    {
	DateFormat df = new SimpleDateFormat( "yyyyMMdd" );

	String str;
	str = df.format( d );

	return Integer.parseInt( str );
    }

    /**
     * A convenience method to convert string to char[]
     * @deprecated use String.toCharArray() instead
     */
    public static char[] toChars( String str )
    {
	char[] retval;
	int len;
	
	len = str.length();

	retval = new char[len];

	str.getChars( 0, len, retval, 0 );

	return retval;
    }
    
    /**
     * Converts a partial string into an integer
     *
     * It's better because it converts without throwing exception
     */
    public static int toInt( String str, int start, int maxlen )
    {
        return atoi( str, start, maxlen );
    }

    /**
     * Converts a partial string into an integer
     *
     * It's better because it converts without throwing exception
     */
    public static int atoi( String str, int start, int maxlen )
    {
	long num = atoi64( str, start, maxlen );
	return (int)num;
    }
    
    public static int toInt( String str, int start )
    {
	return atoi( str, start );
    }

    public static int atoi( String str, int start )
    {
	return atoi( str, start, str.length() - start );
    }

    public static int toInt( String str )
    {
        return atoi( str );
    }

    public static int atoi( String str )
    {
	return atoi( str, 0 );
    }

    /**
     * Converts a partial string into an integer
     *
     * It's better because it converts without throwing exception
     */
    public static long atoi64( String str, int start, int maxlen )
    {
	if( str == null ) return 0;

	long num;
	int cnt;
	char sp;
	
	int sign;
	
	boolean has_seen_digits = false;
	num = 0;
	cnt = 0;
	sign = 1;

	maxlen = Math.min( maxlen, str.length() - start );

	while( cnt < maxlen ){
	    sp = str.charAt( cnt + start );
	    if( !Character.isDigit( sp ) && has_seen_digits ){
		break;
	    }
	    if( sp == '-' ){
		sign = -sign;
	    }
	    if( Character.isDigit( sp ) ){
		has_seen_digits = true;
		num *= 10;
		num += sp - '0';
	    }
	    cnt++;
	}
	return num * sign;
	
    }

    public static long atoi64( String str, int start )
    {
	return atoi64( str, start, str.length() - start );
    }

    public static long atoi64( String str )
    {
	return atoi64( str, 0 );
    }

    /**
     * Returns the character representation of a number
     */
    public static char itoa( int n )
    {
	n = Math.abs( n );
	if( n < 10 ){
	    return (char)(n + '0');
	}
	return (char)(n - 10 + 'a');
    }

    /**
     * converts an integer into a c-style string (8 bits, null
     * terminated)
     */
    public static byte[] itoa( byte[] dst, long n, int radix )
    {
	byte sign = 1;
	int idx = 0;

	if( n < 0 ){
	    n = -n;
	    sign = (byte)(-sign);
	}

	while( true ){
	    dst[idx] = (byte)itoa( (int)(n % radix) );
	    n /= radix;
	    idx++;
	    if( n == 0 ) break;
	}

	if( sign < 0 ){
	    dst[idx] = '-';
	    idx++;
	}
	dst[idx] = 0;

	CString.reverse( dst );

	return dst;
    }

    public static byte[] itoa( byte[] dst, long n )
    {
	return itoa( dst, n, 10 );
    }

    public static long i64get( byte[] buf, int start_byte,
			       int start_bit, int bit_count )
    {
	int mask;
	int i;
	long val;
	int byte_count;

	val = 0;
	mask = 1 << start_bit;
	for( i = 0, byte_count = start_byte; i < bit_count; i++ ){
	    val = val << 1;

	    if( (buf[byte_count] & mask) > 0 ){
		val++;
	    }

	    // rotate to the right and grab next byte if neded
	    if( mask == 1 ){
		mask = 0x80;
		byte_count++;
	    }
	    else{
		mask = mask >> 1;
	    }
	}
	return val;
    }

    /**
     * Get a 64 number from an array of bytes interpreted as
     * a TBCD string
     *
     * The smallest unit in a TBCD string is the nybble
     */
    public static long tbcd64get( byte[] buf, int start_byte,
                                  int start_nybble, int nybble_count )
    {
	int i;
	long val;
	long tbcd;
	int byte_count;
	int nybble_index = start_nybble;

	val = 0;
	for( i = 0, byte_count = start_byte; i < nybble_count; i++ ){
	    if( nybble_index == 1 ){
		tbcd = i64get( buf, byte_count, 7, 4 );
		nybble_index = 0;
	    }
	    else{
		tbcd = i64get( buf, byte_count, 3, 4 );
		nybble_index = 1;
		byte_count++;
	    }

	    if( tbcd == 0 ){
                continue;
            }

	    val = val * 10;
	    if( 1 <= tbcd && tbcd <= 9 ){
		val += tbcd;
	    }
	}
	return val;
    }

    /**
     * Converts an object to string, just like String.valueOf
     * except nulls are translated into "", and results are trimmed.
     */
    public static String toString( Object o )
    {
	if( o == null ){
	    return "";
	}
	return String.valueOf( o ).trim();
    }

    /**
     * trims string on the left, using ws as whitespace definition
     */
    public static String ltrim( String str, String ws )
    {
        int idx;
        for( idx = 0; idx < str.length(); ++idx ){
            char c = str.charAt( idx );

            // if c is not whitespace:
            if( ws.indexOf( c ) < 0 ){
                break;
            }
        }
        return str.substring( idx );
    }

    /**
     * Cuts a word off a string and then returns the word and the
     * remainder of the string.  The word is the first value, and the
     * remainder is the second value.
     *
     * A word is either a string of nonspaces or a string between two
     * quote marks, either single or double
     */
    public static String[] wget( String str )
    {
	String[] retval = new String[2];

	if( str.length() <= 0 ){
	    retval[0] = "";
	    retval[1] = "";
	    return retval;
	}

	// default delimiter
	char cend = ' ';

	int src = 0;
	while( Character.isWhitespace( str.charAt( src ) ) ){
	    src++;
	}
	
	char c;
	c = str.charAt( src );
	if( c == '\'' || c == '"' ){
	    cend = c;
	    src++;
	}

	StringBuffer word = new StringBuffer();
	while( src < str.length() ){
	    c = str.charAt( src );
	    if( cend == ' ' && (c == '\'' || c == '"') ){
		break;
	    }
	    else if( (cend == ' ' && Character.isWhitespace( c )) ||
		     (c == cend) ){
		src++;
		break;
	    }

	    word.append( c );
	    src++;
	}

	retval[0] = word.toString();
	retval[1] = str.substring( src );

	return retval;
    }

    /**
     * Filters only the characters from the given string.
     *
     * @param src source string
     * @param chars the characters wanted
     *
     * @return the characters in the source string that are also
     *         in "chars"
     */
    public static String select( String src, String chars )
    {
        StringBuffer retval;
        retval = new StringBuffer();

        char[] buf = src.toCharArray();

        for( int i = 0; i < buf.length; i++ ){
            char c = buf[i];
            if( chars.indexOf( (int)c ) >= 0 ){
                retval.append( c );
            }
        }

        return retval.toString();
    }

    /**
     * Compares to pointers by "equal" method
     *
     * For now, a null equals to another null
     */
    public static boolean equals( Object a, Object b )
    {
        if( a == null ){
            if( b != null ) return false;
            return true;
        }

        return a.equals( b );
    }

    /**
     * Does it big-endian, byte-wise
     *
     * the offset is defined as
     *
     * byte      0       1       2       3       4
     * offset    7654321076543210765432107654321076543210
     *
     * which makes it little-endian bitwise...weird enough
     *
     *
     * There has to be a simpler way to do this, but I don't
     * know it yet :(
     */
    public static void toByteArray( byte buf[], long val,
                                    int offset, int cnt )
    {
        int dst_byte_offset;
        int dst_bit_offset;
        
        int src_bit_offset;
        
        int dst_mask;
        int src_mask;
        
        int working_val;
        
        boolean keep_going;

        // strategy:
        //
        // takes off value one bit at a time, and fit into target buffer
        
        // buf      0       1       2       3       4
        //          7654321076543210765432107654321076543210
        //                         ^offset
        // cnt                     xxxxxx
        //
        // val               000001001101
        
        // we must count [cnt] bits into the value, from the smaller
        // end (because the big end is usually filled with zeroes)
        
        // whatever address of buf we were given is considered 0 point
        dst_byte_offset = 0;
        // and we start at the given bit offset
        dst_bit_offset = offset;
        
        // because we are getting source in the opposite direction
        //
        // for example, a count of 3 bits means we start at position 2
        //
        // 76543210
        //      xxx    <-- 3 bits
        src_bit_offset = cnt - 1;
        
        // let source mask be blank
        src_mask = 0;
        // let working value be all 1s
        working_val = 0;
        
        keep_going = true;
        while( keep_going ){
            if( cnt > 0 ){
                // source mask has bit value of one whenever we have a bit
                // we want to set
                src_mask = (src_mask << 1) + 1;
                
                // working value is the value from source up to byte boundary
                // using getBit is not as efficient but is more modular
                working_val = (working_val << 1) +
                              getBit( val, src_bit_offset );
            }
            else{
                // if we exceeded count, we have to allow original value;
                // thus we set source mask to 0 (and dest mask to 1)
                src_mask = src_mask << 1;
                working_val = working_val << 1;
            }
            
            // eat a value, which will be replaced by the one in
            // src_mask
            //dst_mask := dst_mask shr 1;
            
            // we hit byte boundary
            if( dst_bit_offset == 0 ){
                // destination mask is the opposite of source mask
                // because wherever source mask wants to change, destination
                // doesn't
                //
                // can we calculate destination mask without using source
                // mask?
                dst_mask = src_mask ^ 0xff;
                
                working_val = (buf[dst_byte_offset] & dst_mask) |
                                     working_val;
                
                buf[dst_byte_offset] = (byte)(working_val);
                // reset offset
                dst_bit_offset = 8;
                // advance byte offset of course
                dst_byte_offset++;
                
                // if we reached count, get ready for exit
                if( cnt <= 0 ){
                    keep_going = false;
                }
            }
            
            dst_bit_offset--;
            src_bit_offset--;
            cnt--;
        }
    }

    static int getBit( long val, int offset )
    {
        //            76543210
        // val        01010101
        // offset         ^          (3)
        val = val >> (offset);

        // after above step, we get this
        //            76543210
        // val           01010
        // offset            ^
        
        // now just return the least significant bit
        return (int)(val & 0x01);
    }

    public String base64Encode( byte[] bytes )
    {
        StringBuffer retval = new StringBuffer();

        int mask = 0x80;

        int byte_idx = 0;
        // stores the value of 6-bit output
        byte out_6_bit = 0;
        // number of bits completed in 6-bit output
        byte count_6_bit = 0;
        // number of bytes completed -- rotates every 4 bytes
        byte count_4_byte = 0;

        while( true ){
            byte b = bytes[byte_idx];
            int val = (int)(b & mask);

            out_6_bit <<= 1;

            if( val > 0 ){
                ++out_6_bit;
            }
            
            ++count_6_bit;
            if( count_6_bit >= 6 ){
                // 0 to 25 is A to Z
                if( 0 <= out_6_bit &&  out_6_bit <= 25 ){
                    retval.append( (char)('A' + out_6_bit) );
                }
                // 26 to 51 is a to z
                else if( 26 <= out_6_bit && out_6_bit <= 51 ){
                    retval.append( (char)('a' + out_6_bit - 26) );
                }
                // 52 to 61 is 0 to 9
                else if( 52 <= out_6_bit && out_6_bit <= 61 ){
                    retval.append( (char)('0' + out_6_bit - 52) );
                }
                else if( out_6_bit == 62 ){
                    retval.append( '+' );
                }
                else if( out_6_bit == 63 ){
                    retval.append( '/' );
                }

                out_6_bit = 0;
                count_6_bit = 0;
                ++count_4_byte;
                if( count_4_byte >= 4 ){
                    count_4_byte = 0;
                }
            }

            if( mask == 1 ){
                mask = 0x80;
                ++byte_idx;
                if( byte_idx >= bytes.length ) break;
            }
            else{
                mask >>=  1;
            }
        }

        // finish any incomplete 6 bits
        if( count_6_bit > 0 ){
            for( int i = 0; i < 6 - count_6_bit; ++i ){
                out_6_bit <<= 1;
            }

            // 0 to 25 is A to Z
            if( 0 <= out_6_bit &&  out_6_bit <= 25 ){
                retval.append( (char)('A' + out_6_bit) );
            }
            // 26 to 51 is a to z
            else if( 26 <= out_6_bit && out_6_bit <= 51 ){
                retval.append( (char)('a' + out_6_bit - 26) );
            }
            // 52 to 61 is 0 to 9
            else if( 52 <= out_6_bit && out_6_bit <= 61 ){
                retval.append( (char)('0' + out_6_bit - 52) );
            }
            else if( out_6_bit == 62 ){
                retval.append( '+' );
            }
            else if( out_6_bit == 63 ){
                retval.append( '/' );
            }

            ++count_4_byte;
        }
        // finish any incomplete 4 bytes
        if( count_4_byte > 0 ){
            for( int i = 0; i < 4 - count_4_byte; ++i ){
                retval.append( '=' );
            }
        }

        return retval.toString();
    }
}
