package name.subroutine.source;

import java.io.*;
import java.util.*;
import java.lang.reflect.*;


/**
 * A source code generator that writes the accessors and mutators
 *
 * Special things:
 *
 * If the classs contains a "_is_modified" flag, the generator will
 * add _is_modified = true for every mutator
 *
 */
public class Skeletor
{
    /**
     * Loads a class by filename
     */
    static class SkeletoClassLoader extends ClassLoader
    {
	public Class findClass( String name )
	    throws ClassNotFoundException
	{
	    try{
		byte[] b = loadClassData( name );
		return defineClass( null, b, 0, b.length );
	    }
	    catch( Exception ex ){
		throw new ClassNotFoundException( ex.getMessage() );
	    }
	}
	private byte[] loadClassData( String name )
	    throws Exception
	{
	    File f;
	    f = new File( name );

	    long len;
	    len = f.length();

	    if( len > 0x80000000L ){
		throw new Exception( "Class file too large." );
	    }
	    int intlen;
	    intlen = (int)len;

	    byte[] def = new byte[intlen];
	    
	    FileInputStream fis = new FileInputStream( f );
	    fis.read( def );

	    return def;
	}

    }

    public void rewrite( String class_file )
	throws Exception
    {
	ClassLoader cl;
	cl = new SkeletoClassLoader();

	Class clazz;
	clazz = cl.loadClass( class_file );

	String name;
	name = clazz.getName();
	
	int dot;
	dot = name.lastIndexOf( '.' );
	
	String pkg = null;
	if( dot > 0 ){
	    pkg = name.substring( 0, dot );
	    name = name.substring( dot + 1 );
	}

	if( pkg != null ){
	    _out.println( "package " + pkg + ";" );
	    _out.println();
	}

	Object obj = clazz.newInstance();
	writeClass( obj, name );
    }

    int _tab = 4;
    public transient PrintWriter _out = new PrintWriter( System.out, true );
    String _accessor_prefix = "get";
    String _str_accessor_prefix = "strGet";
    String _mutator_prefix = "set";
    boolean _uses_is_modified;
    boolean _uses_is_new;
    boolean _is_modified;

    String getIndent()
    {
	char[] ind = new char[_tab];
	Arrays.fill( ind, ' ' );
	String indent = new String( ind );
	return indent;
    }

    static String toString( Class c )
    {
	if( c.isArray() ){
	    String cn;
	    cn = c.getComponentType().getName();
	    return cn + "[]";
	}

	return c.getName();
    }

    /**
     * Returns the length of the array object, or 0 if
     * the object is not an array
     */
    static int getLength( Object o )
	throws Exception
    {
	Class c = o.getClass();
	Class cc = c.getComponentType();

	if( cc.equals( char.class ) ){
	    char[] tmp = (char[])o;
	    return tmp.length;
	}

	if( cc.equals( int.class ) ){
	    int[] tmp = (int[])o;
	    return tmp.length;
	}

	if( cc.equals( byte.class ) ){
	    byte[] tmp = (byte[])o;
	    return tmp.length;
	}

	if( cc.equals( long.class ) ){
	    long[] tmp = (long[])o;
	    return tmp.length;
	}

	if( cc.equals( Object.class ) ){
	    Object[] tmp = (Object[])o;
	    return tmp.length;
	}

	if( cc.equals( String.class ) ){
	    String[] tmp = (String[])o;
	    return tmp.length;
	}

	throw new Exception( "Cannot determine length." );
    }
    
    /**
     * Returns a string that initializes an array into the
     * size as defined by object o, without the "new" word
     */
    static String getArrayInitializer( Object o )
	throws Exception
    {
	Class c = o.getClass();
	String type = c.getComponentType().getName();
	int len = getLength( o );
	return type + "[" + len + "]";
    }

    static String reformatName( String fname )
    {
	StringBuffer ffname = new StringBuffer();
	boolean seen_underscore = false;
	for( int j = 0; j < fname.length(); j++ ){
	    char fc;
	    fc = fname.charAt( j );
	    if( !seen_underscore ){
		if( fc == '_' ){
		    seen_underscore = true;
		    continue;
		}
		ffname.append( fc );
		continue;
	    }
	    // kinda stupid, but we may add more "contexts" later on
	    else if( seen_underscore ){
		// we keep double underscores for now
		if( fc == '_' ){
		    ffname.append( fc );
		    seen_underscore = false;
		    continue;
		}
		
		ffname.append( Character.toUpperCase( fc ) );
		seen_underscore = false;
		continue;
	    }
	}
	char first = Character.toUpperCase( ffname.charAt( 0 ) );
	ffname.setCharAt( 0, first );
	return ffname.toString();
    }

    /**
     * Better than the getField method in the Class class.  This
     * one returns nonpublic inherited fields
     */
    static Field getField( Class c, String fname )
	throws NoSuchFieldException
    {
	Field f;
	while( true ){
	    try{
		f = c.getDeclaredField( fname );
		return f;
	    }
	    catch( NoSuchFieldException ex ){
		c = c.getSuperclass();
		if( c == null ){
		    throw ex;
		}
		continue;
	    }
	}
    }

    private void checkForIsModified( Class c )
    {
	try{
	    Field f;
	    f = getField( c, "_is_modified" );
	    Class fc;
	    fc = f.getType();
	    if( fc.equals( boolean.class ) ){
		_uses_is_modified = true;
		return;
	    }
	    _uses_is_modified = false;
	    return;
	}
	catch( Exception ex ){
	    _uses_is_modified = false;
	    return;
	}
    }

    private void checkForIsNew( Class c )
    {
	try{
	    Field f;
	    f = getField( c, "_is_new" );
	    Class fc;
	    fc = f.getType();
	    if( fc.equals( boolean.class ) ){
		_uses_is_new = true;
		return;
	    }
	    _uses_is_new = false;
	    return;
	}
	catch( Exception ex ){
	    _uses_is_new = false;
	    return;
	}
    }

    private void writeClass( Object o, String name )
	throws Exception
    {
	String indent = getIndent();
	checkForIsModified( o.getClass() );
	checkForIsNew( o.getClass() );

	_out.println( "public class " + name );
	_out.println( "{" );

	_out.print( indent );
	_out.println( "//---------------------Members----------------------" );
	writeAttributes( o );

	_out.print( indent );
	_out.println( "//---------------------Constructors-----------------" );
	writeConstructors( o );

	_out.print( indent );
	_out.println( "//---------------------Accessors--------------------" );
	writeAccessors( o );

	_out.print( indent );
	_out.println( "//---------------------Mutators---------------------" );
	writeMutators( o );

	_out.println( "}" );
    }

    private void writeAttributes( Object o )
	throws Exception
    {
	Class c = o.getClass();
	String indent = getIndent();

	Field[] f_array = c.getDeclaredFields();
	for( int i = 0; i < f_array.length; i++ ){
	    _out.print( indent );
	    Field f;
	    f = f_array[i];

	    String modi_string = Modifier.toString( f.getModifiers() );
	    if( modi_string.length() > 0 ){
		_out.print( modi_string );
		_out.print( ' ' );
	    }

	    String ftype;
	    ftype = toString( f.getType() );
	    _out.print( ftype );

	    _out.print( ' ' );

	    _out.print( afterDot( f.getName() ) );

	    if( f.getType().isArray() ){
		try{
		    f.setAccessible( true );
		    String initializer = getArrayInitializer( f.get( o ) );
		    _out.print( " = " );
		    _out.print( initializer );
		}
		catch( Exception ignored ){
		}
	    }
	    _out.print( ';' );

	    _out.println();
	}
	_out.println();
    }

    private void writeConstructors( Object o )
    {
	Class c = o.getClass();
	String indent = getIndent();

	// accessor
	_out.print( indent );
	_out.print( "public " );
	_out.print( afterDot( o.getClass().getName() ) );
	_out.println( "()" );
	_out.println( indent + '{' );
	
	if( _uses_is_new ){
	    _out.print( indent );
	    _out.print( indent );
	    _out.println( "_is_new = true;" );
	}
	if( _uses_is_modified ){
	    _out.print( indent );
	    _out.print( indent );
	    _out.println( "_is_modified = false;" );
	}
	
	_out.println( indent + '}' );
	
	_out.println();
    }

    private void writeAccessorIsModified( Field f, String ftype,
					  String fname, String ffname,
					  String indent )
    {
	// accessor
	_out.print( indent );
	_out.print( "public " );
	// check if static, just for fun
	if( Modifier.isStatic( f.getModifiers() ) ){
	    _out.print( "static " );
	}
	_out.print( ftype );
	_out.print( ' ' );
	_out.print( _accessor_prefix + ffname );
	_out.println( "()" );
	_out.println( indent + '{' );
	
	// if there is also _is_new, we must use that too
	if( _uses_is_new ){
	    _out.print( indent + indent );
	    _out.println( "if( _is_new ) return true;" );
	}

	_out.print( indent + indent );
	_out.println( "return " + fname + ';' );
	
	_out.println( indent + '}' );
	
	_out.println();
	
	// string accessor -- always gets the value as string
	_out.print( indent );
	_out.print( "public " );
	// check if static, just for fun
	if( Modifier.isStatic( f.getModifiers() ) ){
	    _out.print( "static " );
	}
	_out.print( "String " );
	_out.print( _str_accessor_prefix + ffname );
	_out.println( "()" );
	_out.println( indent + '{' );
	
	_out.print( indent + indent );
	_out.print( "return String.valueOf( " );
	_out.print( _accessor_prefix + ffname + "()" );
	_out.println( " );" );
	
	_out.println( indent + '}' );
	
	_out.println();
    }

    private void writeAccessors( Object o )
    {
	Class c = o.getClass();
	String indent = getIndent();

	Field[] f_array = c.getDeclaredFields();
	for( int i = 0; i < f_array.length; i++ ){
	    Field f;
	    f = f_array[i];
            
            if( Modifier.isFinal( f.getModifiers() ) ) continue;

	    String ftype;
	    ftype = toString( f.getType() );
	    String fname;
	    fname = afterDot( f.getName() );

	    // we now reformat the name, removing underscores and
	    // capitalize chars after the underscores
	    String ffname = reformatName( fname );

	    // special handling for _is_modified
	    if( fname.equals( "_is_modified" ) ){
		writeAccessorIsModified( f, ftype, fname, ffname, indent );
		continue;
	    }

	    // accessor
	    _out.print( indent );
	    _out.print( "public " );
	    // check if static, just for fun
	    if( Modifier.isStatic( f.getModifiers() ) ){
		_out.print( "static " );
	    }
	    _out.print( ftype );
	    _out.print( ' ' );
	    _out.print( _accessor_prefix + ffname );
	    _out.println( "()" );
	    _out.println( indent + '{' );

	    _out.print( indent + indent );
	    _out.println( "return " + fname + ';' );

	    _out.println( indent + '}' );

	    _out.println();

	    // string accessor -- always gets the value as string
	    _out.print( indent );
	    _out.print( "public " );
	    // check if static, just for fun
	    if( Modifier.isStatic( f.getModifiers() ) ){
		_out.print( "static " );
	    }
	    _out.print( "String " );
	    _out.print( _str_accessor_prefix + ffname );
	    _out.println( "()" );
	    _out.println( indent + '{' );

	    _out.print( indent + indent );
	    _out.print( "return String.valueOf( " );
	    _out.print( _accessor_prefix + ffname + "()" );
	    _out.println( " ).trim();" );

	    _out.println( indent + '}' );

	    _out.println();
	}
    }

    /**
     * Returns a piece of code that converts a string to
     * the given field. 
     *
     * @return null if conversion is not supported
     */
    private String getStringToXConverter( Field f )
    {
	if( f.getType() == int.class ){
	    return "Integer.parseInt( val.trim() )";
	}
	else if( f.getType() == float.class ){
	    return "Float.parseFloat( val.trim() )";
	}
	else if( f.getType() == long.class ){
	    return "Long.parseLong( val.trim() )";
	}
	else if( f.getType() == double.class ){
	    return "Double.parseDouble( val.trim() )";
	}
	else if( f.getType().isArray() &&
		 f.getType().getComponentType() == char.class ){
	    return "val.toCharArray()";
	}
	else if( f.getType().isArray() &&
		 f.getType().getComponentType() == byte.class ){
	    return "val.getBytes()";
	}
        else if( f.getType() == java.util.Date.class ){
            return "name.subroutine.util.Variant.toDate( val )";
        }

	else return null;
    }

    private void writeStringMutator( Object o, Field f )
    {
	Class c = o.getClass();
	String indent = getIndent();

	String ftype;
	ftype = toString( f.getType() );

	String fname;
	fname = afterDot( f.getName() );

	String ffname;
	ffname = reformatName( fname );
	
	String conversion_code;
	conversion_code = getStringToXConverter( f );

	// don't write anything if there is no conversion available
	if( conversion_code == null ) return;

	// string mutator, set value using string
	_out.print( indent );
	_out.print( "public " );
	// check if static, just for fun
	if( Modifier.isStatic( f.getModifiers() ) ){
	    _out.print( "static " );
	}
	_out.print( "void " );
	_out.print( _mutator_prefix + ffname );
	_out.println( "( String val )" );

        // exceptions :(
        if( f.getType() == java.util.Date.class ){
            _out.println( indent + indent + 
                          "throws java.text.ParseException" );
        }

	_out.println( indent + '{' );
	    
	_out.print( indent + indent );
	_out.print( _mutator_prefix + ffname + "( " );

	// for different types we have to use different
	// converters
    	_out.print( conversion_code );

	_out.println( " );" );

	_out.println( indent + '}' );
	    
	_out.println();
    }

    private void writeMutators( Object o )
    {
	Class c = o.getClass();
	String indent = getIndent();

	Field[] f_array = c.getDeclaredFields();
	for( int i = 0; i < f_array.length; i++ ){
	    Field f;
	    f = f_array[i];

            if( Modifier.isFinal( f.getModifiers() ) ) continue;

	    String ftype;
	    ftype = toString( f.getType() );

	    String fname;
	    fname = afterDot( f.getName() );

	    // we now reformat the name, removing underscores and
	    // capitalize chars after the underscores
	    String ffname = reformatName( fname );

	    // mutator
	    _out.print( indent );
	    _out.print( "public " );
	    // check if static, just for fun
	    if( Modifier.isStatic( f.getModifiers() ) ){
		_out.print( "static " );
	    }
	    _out.print( "void " );
	    _out.print( _mutator_prefix + ffname );
	    _out.print( "( " );
	    _out.print( ftype );
	    _out.print( " val" );
	    _out.println( " )" );
	    _out.println( indent + '{' );

	    if( _uses_is_modified ){
		_out.print( indent + indent );
		_out.println( "_is_modified = true;" );
	    }

	    _out.print( indent + indent );
	    if( !Modifier.isStatic( f.getModifiers() ) ){
		_out.print( "this." );
	    }
	    else{
		_out.print( afterDot( c.getName() ) );
		_out.print( '.' );
	    }
	    _out.print( fname );
	    _out.println( " = val;" );

	    _out.println( indent + '}' );

	    _out.println();

	    writeStringMutator( o, f );
	}
    }

    public static String afterDot( String s )
    {
	int dot;
	dot = s.lastIndexOf( '.' );

	if( dot < 0 ) return s;

	return s.substring( dot + 1 );
    }

    public static void main( String argv[] )
	throws Exception
    {
	Skeletor helper = new Skeletor();
	String file = null;
	String output = null;

	for( int i = 0; i < argv.length; i++ ){
	    if( argv[i].startsWith( "-o" ) ){
		output = argv[i].substring( 2 );
		continue;
	    }
	    if( file == null ){
		file = argv[i];
		continue;
	    }
	}

	if( file == null ){
	    System.out.println( "USAGE: Skeletor [-ooutputfile] inputfile" );
	    return;
	}

	if( output != null ){
	    FileOutputStream fos = new FileOutputStream( output );
	    PrintWriter pw = new PrintWriter( fos, true );
	    helper._out = pw;
	}

	helper.rewrite( file );
    }
}
