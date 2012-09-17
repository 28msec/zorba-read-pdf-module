package org.zorbaxquery.modules.readPdf;

import java.io.IOException;
import java.io.OutputStream;

public class OutputStreamToCpp
    extends OutputStream
{
    static
    {
        System.loadLibrary("read-pdf_1.0");
    }

    native int nativeWrite(byte[] buf, int off, int len, long streamPointer);
//    native void nativeDelete(long streamPointer);


    private static int BUF_SIZE = 100*1024;


    private long _cppObjectPointer = 0;
    /* data buffer - if full is emptied to native
                   - else write to it

       0 ...         _index   ...              _end   ...    BUF_SIZE
         ^already written     ^ to be written         ^unused
     */
    private byte[] _buf;
    private int _start, _end, _size;

    OutputStreamToCpp(long p)
    {
        _buf = new byte[BUF_SIZE];
        _start = 0;
        _end = 0;
        _size = 0;

        if ( p == 0 )
            throw new IllegalArgumentException("Pointer value not allowed to be 0.");

        _cppObjectPointer = p;
    }

    private boolean isCase1()
    {
        if ( _start == _end && _size == 0 )
            return true;
        if ( _start < _end )
        {
            assert _size == (_end - _start) : "Invalid buffer state.";
            return true;
        }
        return false;
    }

    @Override
    public void write(int i)
            throws IOException
    {
        //System.out.println("java:     write1()  int: " + i ); System.out.flush();

        if ( _cppObjectPointer == 0 )
            throw new IOException("OutputStream already deleted.");

        assert _start < BUF_SIZE;
        assert _end   < BUF_SIZE;
        assert _size == ( _start <= _end ? _end - _start : BUF_SIZE - (_start - _end));

        if ( _size==BUF_SIZE )
        {
            // make sure it is at least 1 empty space left
            emptyAll(); // this empties the entire buffer
            System.out.println("java:        OutputStreamToCpp.write(int) " + _size + " must be zero "); System.out.flush();
        }

        // since it's not full should have at least one empty space
        _buf[_end] = (byte)i;  // what OutputStream javadoc says: The 24 high-order bits of b are ignored.
        _end += 1;
        _end = _end % BUF_SIZE;
        _size += 1;
    }


    @Override
    public void write(byte[] buf)
            throws IOException
    {
        //System.out.println("java:     write2()  buf: " + new String(buf) + "  len: " + buf.length ); System.out.flush();
        write(buf, 0, buf.length);
    }

    @Override
    public void write(byte[] buf, int off, int  len)
            throws IOException
    {
        //System.out.println("java:     write3()  off: " + off + "  len: " + len ); System.out.flush();

        if ( _cppObjectPointer == 0 )
            throw new IOException("OutputStream already deleted.");

        assert _start < BUF_SIZE;
        assert _end   < BUF_SIZE;
        assert _size == ( _start <= _end ? _end - _start : BUF_SIZE - (_start - _end));

        while ( len>0 )
        {
            if ( _size >= BUF_SIZE )
            {
                // make sure it is at least 1 empty space left
                emptyAll(); // this empties the entire buffer
            }

            int moveToBufferSize;
            if ( isCase1() )
            {
                //System.out.println("            s " + _start + " <= e " + _end + " _size: " + _size); System.out.flush();
                moveToBufferSize = Math.min(len, BUF_SIZE -_end);
                //System.out.println("                copy " + moveToBufferSize ); System.out.flush();
                System.arraycopy(buf, off, _buf, _end, moveToBufferSize);
                _end = (_end + moveToBufferSize) % BUF_SIZE;
                _size += moveToBufferSize;
                off += moveToBufferSize;
                len = len - moveToBufferSize;
                //System.out.println("              s " + _start + " e " + _end + " _size: " + _size + " len: " + len); System.out.flush();
            }
            else
            {
                //System.out.println("            s " + _start + " > e " + _end + " _size: " + _size); System.out.flush();
                moveToBufferSize = Math.min(len, _start -_end);
                System.arraycopy(buf, off, _buf, _end, moveToBufferSize);
                _end = (_end + moveToBufferSize) % BUF_SIZE;
                _size += moveToBufferSize;
                off += moveToBufferSize;
                len = len - moveToBufferSize;
                //System.out.println("              s " + _start + " e " + _end + " _size: " + _size + " len: " + len); System.out.flush();
            }
        }
    }

    /**
     * Empties the entire buffer
     */
    public void emptyAll()
            throws IOException
    {
        //System.out.println("java:     emptyAll()  size: " + _size ); System.out.flush();
        while( _size>0 )
        {
            emptyOnce();
        }
    }

    /**
     * Tries to empty some of the buffer. Returns the emptied size.
     */
    public int emptyOnce()
            throws IOException
    {
        //System.out.println("java:     emptyOnce():start  size: " + _size ); System.out.flush();
        if ( _size == 0 )
            return 0;

        if ( isCase1() )
        {
            int l = nativeWrite(_buf, _start, _size, _cppObjectPointer);

            if ( l>_size )
                throw new IllegalStateException("wrong nativeWrite implementation");

            if ( l == -2 )
                throw new IOException("Native ostream failed.");

            _start += l;
            _start = _start % BUF_SIZE;
            _size -= l;
            //System.out.println("java:        emptyOnce() " + _size + "  " + _start + " - " + _end); System.out.flush();
            return l;
        }
        else
        {
            int l = nativeWrite(_buf, _start, BUF_SIZE - _start, _cppObjectPointer);

            if ( l>BUF_SIZE-_start )
                throw new IllegalStateException("wrong nativeWrite implementation");

            if ( l == -2 )
                throw new IOException("Native ostream failed.");

            _start += l;
            _start = _start % BUF_SIZE;
            _size -=l;
            //System.out.println("java:        emptyOnce() size:" + _size + "  " + _start + " - " + BUF_SIZE); System.out.flush();
            return l;
        }
    }

    @Override
    public void close()
            throws IOException
    {
        flush();
        super.close();
        _cppObjectPointer = 0;
        _buf = null;
        _start = 0;
        _end = 0;
        _size = 0;
    }

    @Override
    public void flush()
            throws IOException
    {
        super.flush();
        emptyAll();
    }
}
