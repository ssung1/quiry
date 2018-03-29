package name.subroutine.io;

import java.io.*;
import java.util.*;

public class FileTimeComparator implements Comparator
{
    public int compare( Object a_, Object b_ )
    {
	java.io.File a = (java.io.File)a_;
	java.io.File b = (java.io.File)b_;

	return (int)(a.lastModified() - b.lastModified());

    }
}
