package name.subroutine.servlet;

/**
 * class for controlling paged output
 *
 * <pre>
 * to use, first create a pager:
 *
 *     Pager pager = new Pager( offset, max_page_size, total_cnt )
 *
 * where
 *
 * offset is the current location (retrieved from user, possibly
 * a hidden field)
 *
 * max_page_size is the maximum number of records to jump when user
 * clicks on "previous" or "next"
 *
 * total_cnt is the total number of rows to display, possibly retrieved
 * from database
 *
 *
 *
 * now to display the current range, call getDispFrom() and
 * getDispTo()
 *
 * get the number of rows to display by calling getPageSize().  this
 * number may be less than max_page_size due to offset and total_cnt
 *
 * </pre>
 */
public class Pager
{
    /**
     * current offset
     */
    int _offset;

    /**
     * max page size -- each flip would try to advance by this many rows
     */
    int _max_page_size;

    /**
     * total count -- the complete size of record set / file
     */
    int _total_cnt;

    public Pager( int offset, int max_page_size, int total_cnt )
    {
        _offset = offset;
        _max_page_size = max_page_size;
        _total_cnt = total_cnt;
    }

    /**
     * resolve out-of-bounds values such as negative offsets
     */
    public void resolve()
    {
        if( _offset < 0 ){
            _offset = 0;
        }
        else if( _offset >= _total_cnt ){
            _offset = _total_cnt - _max_page_size;
        }
    }

    /**
     * go to the previous page, calculating the necessary values
     */
    public void prev()
    {
        _offset -= _max_page_size;
        resolve();
    }

    /**
     * @return true if there are records before current range
     */
    public boolean hasPrev()
    {
        return _offset > 0;
    }
     
    /**
     * go to the next page, calculating the necessary values
     */
    public void next()
    {
        _offset += _max_page_size;
        resolve();
    }

    /**
     * @return true if there are records after current range
     */
    public boolean hasNext()
    {
        return _offset + _max_page_size < _total_cnt;
    }

    /**
     * go to the specified record.  this is 1-based; actual offset
     * is the specified value - 1
     *
     * @see getDispForm regarding stupid users
     */
    public void goTo( int offset )
    {
        _offset = offset - 1;
    }

    /**
     * returns the displayable "from" record number, which is just
     * real offset + 1.  this is for stupid people who cannot count
     * from zero (but have money so can make everyone count from one)
     */
    public int getDispFrom()
    {
        return _offset + 1;
    }

    /**
     * returns the displayable "to" record number, which is the offset
     * of the last record displayed.  this is bad because it makes the
     * number of records displayed (to - from + 1), very confusing.
     * again, this is for stupid people who happen to be rich
     */
    public int getDispTo()
    {
        int disp_to = _offset + _max_page_size;
        if( disp_to > _total_cnt ){
            disp_to = _total_cnt;
        }

        return disp_to;
    }
    
    /**
     * returns the actual number of records displayed in this page,
     * after correcting all the values
     */
    public int getPageSize()
    {
        return getDispTo() - getDispFrom() + 1;
    }

    //---------------------Constructors-----------------
    public Pager()
    {
    }

    //---------------------Accessors--------------------
    public int getOffset()
    {
        return _offset;
    }

    public String strGetOffset()
    {
        return String.valueOf( getOffset() ).trim();
    }

    public int getMaxPageSize()
    {
        return _max_page_size;
    }

    public String strGetMaxPageSize()
    {
        return String.valueOf( getMaxPageSize() ).trim();
    }

    public int getTotalCnt()
    {
        return _total_cnt;
    }

    public String strGetTotalCnt()
    {
        return String.valueOf( getTotalCnt() ).trim();
    }

    //---------------------Mutators---------------------
    public void setOffset( int val )
    {
        this._offset = val;
    }

    public void setOffset( String val )
    {
        setOffset( Integer.parseInt( val.trim() ) );
    }

    public void setMaxPageSize( int val )
    {
        this._max_page_size = val;
    }

    public void setMaxPageSize( String val )
    {
        setMaxPageSize( Integer.parseInt( val.trim() ) );
    }

    public void setTotalCnt( int val )
    {
        this._total_cnt = val;
    }

    public void setTotalCnt( String val )
    {
        setTotalCnt( Integer.parseInt( val.trim() ) );
    }
}
