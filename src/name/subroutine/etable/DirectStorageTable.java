package name.subroutine.etable;

import java.util.*;
import java.io.*;
import java.sql.*;

public interface DirectStorageTable extends Table
{
    /**
     * writes record to table and advances pointer
     */
    public void writeRecord( Record rec ) throws IOException;
}

