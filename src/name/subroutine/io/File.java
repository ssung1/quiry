package name.subroutine.io;

import java.io.*;

public class File
{
    public static void copy( String src, String dst )
	throws FileNotFoundException, IOException
    {
	copy( src, dst, true );
    }

    /**
     * Copies a file or a directroy from src to dst
     * Works just like the shell-style copy, except that this does
     * not accept wildcards.
     * 
     * @param recurse if set to true, it will copy subdirectories also 
     */
    public static void copy( String src, String dst, boolean recurse )
	throws FileNotFoundException, IOException
    {
	java.io.File dst_f;
	java.io.File src_f;

	src_f = new java.io.File( src );
	dst_f = new java.io.File( dst );

	// if the destination is an existing directory, copy INTO
	// the direcotry
	//
	// if the destination is an existing file, copy OVER the
	// file
	//
	// if the destination does not exist, copy AS the file
	//
	java.io.File m_dst_f;
	String m_dst;
	if( dst_f.isDirectory() && dst_f.exists() ){
	    m_dst_f = new java.io.File( dst_f, src_f.getName() );
	    m_dst = m_dst_f.getAbsolutePath();
	}
	else{
	    m_dst = dst;
	    m_dst_f = dst_f;
	}

	// m_dst is now the real destination.
	// (dst can be either the destination directory or file)
	if( !src_f.isDirectory() ){
	    copyFile( src, m_dst );
	    return;
	}

	// source is a directory...if recurse, copy all contents too
	// otherwise, just create a directory
	if( m_dst_f.exists() ){
	    m_dst_f.delete();
	}
	m_dst_f.mkdirs();

	if( recurse ){
	    java.io.File[] flist;
	    flist = src_f.listFiles();

	    for( int i = 0; i < flist.length; i++ ){
		java.io.File r_dst;
		java.io.File r_src;

		r_src = flist[i];
		r_dst = new java.io.File( m_dst_f, r_src.getName() );

		copy( r_src.getAbsolutePath(), r_dst.getAbsolutePath(),
		      recurse );
	    }
	}
    }

    /**
     * Copy a file from src to dst
     * @param dst output file name, not just directory
     * @param src input file name, not just directory
     */
    public static void copyFile( String src, String dst )
	throws FileNotFoundException, IOException
    {
	FileInputStream in; // resource file
	FileOutputStream out; // destination file

	byte[] buf = new byte[1024];
	int cnt;

	in = new FileInputStream( src );
	out = new FileOutputStream( dst );

        int i = 0;
	while( true ){
	    cnt = in.read( buf );

	    if( cnt == -1 ) break;
	    
	    out.write( buf, 0, cnt );
	} // end while

	in.close();
	out.close();

    } // end class public static copy()


    /**
     * Delete all the files (not including sub-directory) 
     * from giving path
     * @param path, directory which has all the files to be deleted
     */
    public static void deleteFilesFromPath( String path )
	throws Exception
    {
	java.io.File file = new java.io.File( path );
	java.io.File fileNames[] = file.listFiles();
	
	for (int i = 0; i < fileNames.length; i++){
	    if ( fileNames[i].isFile() )
		fileNames[i].delete();
	} // end for
	
    } // end public static void deleteFilesFromPath()
    
    public static void clear( String path )
    {
	clear( path, true );
    }

    /**
     * Delete all the files from given path
     *
     * @param path, directory which has all the files to be deleted
     */
    public static void clear( String path, boolean recurse )
    {
	java.io.File file = new java.io.File( path );
	java.io.File fileNames[] = file.listFiles();
	
	for (int i = 0; i < fileNames.length; i++){
	    java.io.File f;
	    f = fileNames[i];
	    if ( f.isFile() ){
		f.delete();
	    }
	    if( f.isDirectory() && recurse ){
		clear( f.getAbsolutePath(), recurse );
		f.delete();
	    }
	} // end for
    }


     /**
     * Copy the files from src directory to dst directory
     * @param dst output files directory, not a file
     * @param src input file directory, not a file
     */
    public static void copyFiles( String src, String dst )
	throws Exception
    {
	java.io.File in_files = new java.io.File( src );
        java.io.File fileNames[] = in_files.listFiles();
	
	java.io.File out_files = new java.io.File( dst );

	// if destination directory is not exists
	if (!out_files.exists())
	  out_files.mkdir();

	FileInputStream in; // resource file
	FileOutputStream out; // destination file

	byte[] buf = new byte[1024];
	int cnt;

	for (int i = 0; i < fileNames.length; i++)
	{
	  if ( fileNames[i].isFile() )
	  {
	    in = new FileInputStream( fileNames[i] );
	    java.io.File dst_file = new java.io.File( dst, fileNames[i].getName() );
	    out = new FileOutputStream( dst_file.getAbsolutePath() );

	    while( true ) 
	    {
	      cnt = in.read( buf );
  	      if( cnt == -1 ) break;
	      out.write( buf, 0, cnt );
	    } // end while
	  } // end if
	} // end for

    } // end class public static copyFiles()



} // end class File
