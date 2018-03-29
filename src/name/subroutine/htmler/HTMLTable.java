package name.subroutine.htmler;

import java.util.*;
import java.io.*;


public class HTMLTable extends HTMLContainer
{
    int _col_cnt = -1;
    int _header_cnt = 0;
    HTMLElement _cell_template = new HTMLElement();
    HTMLElement _header_template = new HTMLElement();
    List _col_attr_lst = new ArrayList();
    List _row_attr_lst = new ArrayList();
    int _row_attr_cnt = 1;
    
    public HTMLTable()
    {
        _tag = "table";
    }

    public HTMLTable( int col_cnt )
    {
        _tag = "table";
        _col_cnt = col_cnt;
    }
    public HTMLTable( List content )
    {
        _tag = "table";
        _item_lst = content;
    }
    public HTMLTable( Object[] content )
    {
        _tag = "table";
        _item_lst.addAll( java.util.Arrays.asList( content ) );
    }
    public void setCellTemplate( HTMLElement t )
    {
        _cell_template = t;
    }
    public void setHeaderTemplate( HTMLElement t )
    {
        _header_template = t;
    }
    public void setCellAttr( Object[] a )
    {
        _cell_template.setAttr( a );
    }
    public void setHeaderAttr( Object[] a )
    {
        _header_template.setAttr( a );
    }
    /**
     * @param ct should be either a list of 2xN list or a 2xN array
     */
    public void setColAttrLst( Collection ct )
    {
        _col_attr_lst.clear();
        _col_attr_lst.addAll( ct );
    }
    /**
     * @param ct should be either an array of 2xN list or a 2xN array
     */
    public void setColAttrLst( Object[] ct )
    {
        _col_attr_lst.clear();
        _col_attr_lst.addAll( java.util.Arrays.asList( ct ) );
    }
    /**
     * This is an optional list of attributes added to each non-header
     * row.  The attribute will be "tiled"
     * @param ct should be either a list of 2xN list or a 2xN array
     */
    public void setRowAttrLst( Collection ct )
    {
        _row_attr_lst.clear();
        _row_attr_lst.addAll( ct );
    }
    /**
     * This is an optional list of attributes added to each non-header
     * row.  The attribute will be "tiled"
     * @param ct should be either an array of 2xN list or a 2xN array
     */
    public void setRowAttrLst( Object[] ct )
    {
        _row_attr_lst.clear();
        _row_attr_lst.addAll( java.util.Arrays.asList( ct ) );
    }
    /**
     * Sets the number of rows to display before using the next attribute
     * in the row attribute list
     *
     * The attribute used will be
     * <pre>
     * attribute[ (current_row - header) / row_attr_count ]
     * </pre>
     */
    public void setRowAttrCnt( int s )
    {
        _row_attr_cnt = s;
    }

    /**
     * Set number of rows as header
     */
    public void setHeaderCnt( int h )
    {
        _header_cnt = h;
    }
    public void setColCnt( int c )
    {
        _col_cnt = c;
    }
    public int getColCnt()
    {
        return _col_cnt;
    }

    public String toString()
    {
        StringBuffer sb = new StringBuffer();

        int col_cnt = _col_cnt;

        // silly lil fix
        if( col_cnt <= 0 ){
            col_cnt = _item_lst.size();
        }

        int row;
        int i;

        /*************************** remove later
         *************************** <col> doesn't work for
         *************************** mozilla
        for( i = 0; i < col_cnt; i++ ){
            try{
                HTMLElement col = new HTMLElement( "col" );

                Object obj;
                obj = _col_attr_lst.get( i % _col_attr_lst.size() );

                if( obj instanceof Collection ){
                    col.addAttr( (Collection)obj );
                }
                else{
                    col.addAttr( (Object[])obj );
                }

                sb.append( col );
            }
            catch( Exception ex ){
            }
        }
        **************************/

        for( i = 0, row = 0; i < _item_lst.size(); i += col_cnt, row++ ){
            HTMLElement template;
            if( row < _header_cnt ){
                template = _header_template.dup();
            }
            else{
                template = (HTMLElement)_cell_template.dup();
            }

            HTMLContainer tr;

            if( row < _header_cnt ){
                tr = createTr( _item_lst, i, col_cnt, template );
            }
            else{
                try{
                    // apply row attribute
                    int attr_idx;
                    attr_idx = (row - _header_cnt) / _row_attr_cnt;
                    
                    attr_idx = attr_idx % _row_attr_lst.size();
                    
                    Object obj;
                    obj = _row_attr_lst.get( attr_idx );
                    if( obj instanceof Collection ){
                        template.addAttr( (Collection)obj );
                    }
                    else{
                        template.addAttr( (Object[])obj );
                    }
                }
                catch( ArithmeticException ex ){
                }
                catch( IndexOutOfBoundsException iex ){
                }
                tr = createTr( _item_lst, i, col_cnt, template );
            }
            
            sb.append( tr );
            sb.append( "\n" );
        }

        return super.toString( sb );
    }

    /**
     * Creates a row from a list, starting from offset 
     */
    HTMLContainer createTr( List list, int off, int size, HTMLElement templ )
    {
        HTMLContainer elem;
        elem = new HTMLContainer( "tr" );
        for( int i = 0; i < size; i++ ){
            Object o;
            try{
                o = list.get( i + off );
            }
            catch( IndexOutOfBoundsException ex ){
                break;
            }

            HTMLElement cell;
            cell = templ.dup();

            try{
                Object obj;
                obj = _col_attr_lst.get( i % _col_attr_lst.size() );

                if( obj instanceof Collection ){
                    cell.addAttr( (Collection)obj );
                }
                else{
                    cell.addAttr( (Object[])obj );
                }
            }
            catch( Exception ex ){
            }

            cell.setTag( "td" );

            if( o == null ){
                cell.setContent( "<br/>" );
            }
            else if( String.valueOf( o ).trim().length() <= 0 ){
                cell.setContent( "<br/>" );
            }
            else{
                cell.setContent( o );
            }

            elem.add( cell );
        }
        return elem;
    }

    void toPrintTr( PrintWriter pw, List list,
                    int off, int size, HTMLElement templ )
    {
        HTMLContainer elem;
        elem = new HTMLContainer( "tr" );

        elem.toPrintOpeningTag( pw );

        for( int i = 0; i < size; i++ ){
            Object o;
            try{
                o = list.get( i + off );
            }
            catch( IndexOutOfBoundsException ex ){
                break;
            }

            HTMLElement cell;
            cell = templ.dup();

            try{
                Object obj;
                obj = _col_attr_lst.get( i % _col_attr_lst.size() );

                if( obj instanceof Collection ){
                    cell.addAttr( (Collection)obj );
                }
                else{
                    cell.addAttr( (Object[])obj );
                }
            }
            catch( Exception ex ){
            }

            cell.setTag( "td" );

            if( o == null ){
                cell.setContent( "<br/>" );
            }
            else if( String.valueOf( o ).trim().length() <= 0 ){
                cell.setContent( "<br/>" );
            }
            else{
                cell.setContent( o );
            }

            cell.toPrint( pw );
        }

        elem.toPrintClosingTag( pw );
    }

    void toPrintContent( PrintWriter pw )
    {
        StringBuffer sb = new StringBuffer();

        int col_cnt = _col_cnt;

        // silly lil fix
        if( col_cnt <= 0 ){
            col_cnt = _item_lst.size();
        }

        int row;
        int i;
        /*************************** remove later
         *************************** <col> doesn't work for
         *************************** mozilla
        for( i = 0; i < col_cnt; i++ ){
            try{
                HTMLElement col = new HTMLElement( "col" );

                Object obj;
                obj = _col_attr_lst.get( i % _col_attr_lst.size() );

                if( obj instanceof Collection ){
                    col.addAttr( (Collection)obj );
                }
                else{
                    col.addAttr( (Object[])obj );
                }

                col.toPrint( pw );
            }
            catch( Exception ex ){
            }
        }
        **************************/

        for( i = 0, row = 0; i < _item_lst.size(); i += col_cnt, row++ ){
            HTMLElement template;

            if( row < _header_cnt ){
                template = _header_template.dup();
            }
            else{
                template = (HTMLElement)_cell_template.dup();
            }

            if( row < _header_cnt ){
                toPrintTr( pw, _item_lst, i, col_cnt, template );
            }
            else{
                try{
                    // apply row attribute
                    int attr_idx;
                    attr_idx = (row - _header_cnt) / _row_attr_cnt;
                    
                    attr_idx = attr_idx % _row_attr_lst.size();
                    
                    Object obj;
                    obj = _row_attr_lst.get( attr_idx );
                    if( obj instanceof Collection ){
                        template.addAttr( (Collection)obj );
                    }
                    else{
                        template.addAttr( (Object[])obj );
                    }
                }
                catch( ArithmeticException ex ){
                }
                catch( IndexOutOfBoundsException iex ){
                }
                toPrintTr( pw, _item_lst, i, col_cnt, template );
            }
        }
    }
}
