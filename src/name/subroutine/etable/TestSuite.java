package name.subroutine.etable;

import java.util.*;
import java.lang.reflect.*;

/**
 * A collections of tests the package classes
 */
public class TestSuite
{
    public static void main( String[] argv )
    {
	{
	    //dependency
	    Field field;
	    AbstractField abstractfield;
	    ByteField bytefield;
	    EtableField etablefield;
	    DbfField dbffield;
	    CsvField csvfield;
	    SdfField sdffield;

	    Record record;
	    AbstractRecord abstractrecord;
	    EtableRecord etablerecord;
	    DbfRecord dbfrecord;
	    CsvRecord csvrecord;
	    SdfRecord sdfrecord;

	    Table table;
	    AbstractTable abstracttable;
	    Etable etable;
	    CsvTable csvtable;

	    DirectStorageTable directstoragetable;
	    AbstractDSTable abstractdstable;
	    DbfTable dbftable;
	    SdfTable sdftable;
	}


	TestSuite ts = new TestSuite();

	Class c;
	c = ts.getClass();

	Method[] m;
	m = c.getDeclaredMethods();

	if( argv.length <= 0 || argv[0].equals( "--help" ) ){
	    System.out.println( "Available functions are: " );
	    for( int i = 0; i < m.length; i++ ){
		if( m[i].getReturnType() != boolean.class ) continue;

		System.out.println( m[i].getName() );
	    }
	    return;
	}

	for( int i = 0; i < argv.length; i++ ){
	    Method mo;
	    try{
		mo = c.getDeclaredMethod( argv[i], new Class[] {} );
		
		Object result = mo.invoke( ts, new Object[] {} );

		String res;
		if( result.equals( new Boolean( true ) ) ){
		    res = "passed";
		}
		else{
		    res = "failed";
		}
		System.out.println( argv[i] + ": " + res );
	    }
	    catch( Exception ex ){
		System.out.println( "Error occurred during: " + argv[i] );
		ex.printStackTrace();
	    }
	}
    }

    public boolean etablePushline()
    {
	Etable e;
	e = new Etable();

	e.pushLine( "%Name   AC" );
	e.pushLine( "----------" );
	e.pushLine( " Frodo  0 " );

	e.first();
	System.out.println( "Name: " + e.getVal( "Name" ) );

	if( !e.getVal( "Name" ).toString().equals( "Frodo" ) ) return false;
	return true;
    }

    public boolean sorting()
    {
	int size = 1000000;
	System.out.println( "Creating array...:" + new Date() );
	String[] list = new String[size];

	int len = 128;
	char blank[] = new char[128];

	for( int i = 0; i < size; i++ ){
	    String rec;
	    rec = String.valueOf( (int)(Math.random() * 65000) );

	    String remain;
	    remain = new String( blank, 0, 128 - rec.length() );

	    list[i] = rec + remain;;
	}

	System.out.println( "done: " + new Date() );

	System.out.println( list[0] );
	System.out.println( list[1] );
	System.out.println( list[2] );

	System.out.println( "Sorting array...:" + new Date() );

	Arrays.sort( list );

	System.out.println( "done: " + new Date() );

	System.out.println( list[0] );
	System.out.println( list[1] );
	System.out.println( list[2] );

	return true;
    }

    public boolean dbf()
    {
	boolean retval = true;
	try{
	    DbfTable e;
	    e = new DbfTable( "name.subroutine/etable/vendor.dbf" );
	    
	    // read test ----------------------------------------
	    e.open();
	    e.first();

	    Record rec;
	    rec = e.get();

	    if( !rec.get( "VendorNum" ).toString().equals( "0001" ) ){
		System.out.println( "VendorNum != 0001" );
		retval = false;
	    }

	    e.next();
	    rec = e.get();
	    if( !rec.get( "VendorNum" ).toString().equals( "0002" ) ){
		System.out.println( "VendorNum != 0002" );
		retval = false;
	    }

	    rec = e.last();
	    if( !rec.get( "VendorNum" ).toString().equals( "0111" ) ){
		System.out.println( "VendorNum != 0111" );
		retval = false;
	    }

	    for( e.first(); !e.eof(); e.next() ){
		//System.out.println( e.getVal( "LongName" ) );
	    }

	    e.close();

	    // write test 1 --------------------------------------

	    e = new DbfTable( "name.subroutine/etable/temp.dbf" );
	    e.create( "foxpro" );
	    DbfField field = new DbfField( "name" );
	    field.type( 'C' );
	    field.size( 10 );
	    e.pushFld( field );

	    field.name( "AC" );
	    field.type( 'N' );
	    field.size( 5 );
	    field.prec( 0 );
	    e.pushFld( field );

	    field = (DbfField)e.getFld( 1 );

	    if( field.offset() != 11 ){
		System.out.println( "Offset error: not 11" );
		retval = false;
	    }

	    DbfRecord dbfrec = new DbfRecord( e.fieldLst() );
	    dbfrec.push( "marine" );
	    dbfrec.push( "0" );

	    if( !dbfrec.toString( dbfrec ).
		equals( " marine        0" ) ){
		System.out.println( "DbfRecord.toString error" );
		retval = false;
	    }

	    e.push( dbfrec );

	    e.pushLst( new String[] {
		    "firebat", "1",
		    "tank",    "1",
	        }
	    );

	    e.close();

	    // write test 2 --- with precision ----------------------

	    e.create( "foxpro" );

	    field.name( "Name" );
	    field.type( 'C' );
	    field.size( 10 );
	    e.pushFld( field );

	    field.name( "AC" );
	    field.type( 'N' );
	    field.size( 5 );
	    field.prec( 2 );
	    e.pushFld( field );

	    dbfrec = new DbfRecord( e.fieldLst() );
	    dbfrec.push( "marine" );
	    dbfrec.push( "0" );

	    if( !dbfrec.toString( dbfrec ).
		equals( " marine     0.00" ) ){
		System.out.println( "DbfRecord.toString error" );
		retval = false;
	    }

	    e.push( dbfrec );

	    e.pushLst( new String[] {
		    "firebat", "1",
		    "tank",    "1",
	        }
	    );

	    e.close();

	    // test CSV creation --------------------------
	    //CsvRecord csvrec = new CsvRecord( e.fieldLst() );

	    CsvTable csvtable = new CsvTable();
	    rec = csvtable.createRecord();

	    csvtable.pushLine( "marine,0,\"rifle\",stim\"stim" );

	    if( !csvtable.first().toString().
		equals( "marine,0,rifle,\"stim\"\"stim\"" ) ){
		System.out.println( "CsvRecord.toString error" );
		retval = false;
	    }

	    // test dbf to csv conversion -----------------
	    e = new DbfTable( "name.subroutine/etable/vendor.dbf" );

	    e.open();
	    csvtable.clear();

	    for( e.first(); !e.eof(); e.next() ){
		csvtable.push( e.get() );
	    }

	    csvtable.toString( csvtable );
	    csvtable.toString( e );

	    e.close();

	    // test dbf to sdf conversion -----------------
	    e = new DbfTable( "name.subroutine/etable/vendor.dbf" );
	    e.open();
	    SdfTable sdftable;
	    sdftable = new SdfTable( "name.subroutine/etable/vendor.dat" );
	    sdftable.create();

	    SdfField sdffield = new SdfField( "" );
	    for( int i = 0; i < e.fieldCnt(); i++ ){
		sdffield.name( e.getFld( i ).name() );
		sdffield.size( e.getFld( i ).size() );
		sdffield.prec( e.getFld( i ).prec() );

		sdftable.pushFld( sdffield );
	    }

	    for( e.first(); !e.eof(); e.next() ){
		sdftable.push( e.get() );
	    }

	    sdftable.close();
	    e.close();

	    return true;
	}
	catch( Exception ex ){
	    ex.printStackTrace();
	    retval = false;
	}
	return retval;
    }
}
