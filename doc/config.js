importClass( java.io.File );

//////////////////////////////////////////////////////////////////////
// database aliases

// by putting all databases in db, the program can do a listing
db = new Object();
function Db( driver, url, user, password )
{
    this.driver = driver
    this.url = url
    this.user = user
    this.password = password
}

driver_mysql = "com.mysql.jdbc.Driver"
driver_oracle = "oracle.jdbc.driver.OracleDriver"
driver_informix = "com.informix.jdbc.IfxDriver"
driver_sybase = "com.sybase.jdbc3.jdbc.SybDriver"
driver_db2 = "com.ibm.db2.jcc.DB2Driver"
driver_derby_embedded = "org.apache.derby.jdbc.EmbeddedDriver"
driver_derby_client = "org.apache.derby.jdbc.ClientDriver"
driver_ms_sql_server = "com.microsoft.sqlserver.jdbc.SQLServerDriver"
driver_jtds = "net.sourceforge.jtds.jdbc.Driver"

db.sybase = new Db( driver_sybase,
    "jdbc:sybase:Tds:{host}:{port}/{database}",
    "username", "password" );

db.db2 = new Db( driver_db2,
    "jdbc:db2://{host}:{port}/{database}:currentSchema={schema}",
    "username", "password" );

db.mssql = new Db( driver_ms_sql_server,
    "jdbc:sqlserver://{host}:{port};databaseName={database}",
    "", "" );

db.mssql_jtds = new Db( driver_jtds,
    "jdbc:jtds:sqlserver://{host}:{port}/{database}",
    "", "" );

db.derby_network_server = new Db( driver_derby_client,
    "jdbc:derby://{host}:{port}/{datastore_path}", "username", "password" );

db.derby_embedded = new Db( driver_derby_embedded,
    "jdbc:derby:{datastore_path}", "", "" );

db.informix = new Db( driver_informix,
    "jdbc:informix-sqli://{host}:{port}/{database}:INFORMIXSERVER={server}",
    "username", "password" );

db.oracle = new Db( driver_oracle,
    "jdbc:oracle:thin:@//{host}:{port}/{database}",
    "username", "password" );

//////////////////////////////////////////////////////////////////////
// styles

// style is a structure of width and alignment (0 for left, 1 for right)
function Style( width, align ){
    this.width = width
    this.align = align
}

// style for data dictionary
// do not delete this, as it is the default style for data dictionary
// output
st_dictionary = new Array();
st_dictionary[0] = new Style(         20,      0       ) // name
st_dictionary[1] = new Style(         10,      0       ) // type
st_dictionary[2] = new Style(         10,      0       ) // width
st_dictionary[3] = new Style(         10,      0       ) // scale
st_dictionary[4] = new Style(         10,      0       ) // nullable

//////////////////////////////////////////////////////////////////////
// preset quiries
function Quiry( database, style, sql )
{
    this.database = database
    this.style = style
    this.sql = sql
}

str = new Quiry(
    "oracle", null,
    "select 1 from dual"
)
