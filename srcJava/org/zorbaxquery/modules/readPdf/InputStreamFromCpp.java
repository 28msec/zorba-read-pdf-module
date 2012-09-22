/*
 * Copyright 2006-2012 The FLWOR Foundation.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.zorbaxquery.modules.readPdf;

import java.io.IOException;
import java.io.InputStream;

public class InputStreamFromCpp
    extends InputStream
{
    native int nativeRead(byte[] buf, int buf_size, long streamPointer);


    private static int BUF_SIZE = 100*1024;

    private long _cppObjectPointer = 0;
    /* data buffer - if empty is filled from _is
                   - else read from it

       0 ...         _index ...       _max ...     BUF_SIZE
         ^already read      ^available     ^unused
     */
    private byte[] _buf;
    private int _index, _max;


    public InputStreamFromCpp(long p)
    {
        _buf = new byte[BUF_SIZE];
        _index = 0;
        _max = 0;

        if ( p == 0 )
            throw new IllegalArgumentException("Pointer value not allowed to be 0.");

        _cppObjectPointer = p;

        //System.out.println("InputStreamFromCpp() " + p); System.out.flush();
    }

    @Override
    public int read()
            throws IOException
    {
        if (_cppObjectPointer == 0)
            throw new IOException("Stream already closed.");

        if ( _index >= _max )
        {
            _index = 0;
            _max = nativeRead(_buf, BUF_SIZE, _cppObjectPointer);
            //System.out.println("java:     read() " + _max); System.out.flush();
            if ( _max == -2 )
            {
                throw new IOException("Native istream failed.");
            }
        }

        if ( _index < _max )
        {
            byte b = _buf[_index];
            _index++;
            return b;
        }

        if ( _max == 0)
        {
            assert _index == 0 : "_index is not 0 when _max == 0";
            return 0;
        }

        return -1;
    }


    @Override
    public int read(byte[] buf)
            throws IOException
    {
        return read(buf, 0, buf.length);
    }

    @Override
    public int read(byte[] buf, int off, int len)
            throws IOException
    {
        //System.out.println("java:    read1() " + _max); System.out.flush();
        if (_cppObjectPointer == 0)
            throw new IOException("Stream already closed.");

        if ( _index >= _max )
        {
            _index = 0;
            //System.out.println("java:    read2() " + _max); System.out.flush();
            _max = nativeRead(_buf, BUF_SIZE, _cppObjectPointer);
            //System.out.println("java:    read3() " + _max); System.out.flush();
            if ( _max == -2 )
            {
                throw new IOException("Native istream failed.");
            }
            //System.out.println("java:    read4() " + new String(_buf, 0, _max)); System.out.flush();
        }

        if ( _index < _max )
        {
            int min = Math.min(len, _max - _index);
            System.arraycopy(_buf, _index, buf, off, min);
            _index = _index + min;
            return min;
        }

        if ( _max == 0)
        {
            assert _index == 0 : "_index is not 0 when _max == 0";
            return 0;
        }

        return -1;
    }

    @Override
    public void close()
            throws IOException
    {
        //System.out.println("javaP: InputStreamFromCpp.close "); System.out.flush();
        super.close();
        _cppObjectPointer = 0;
        _index = 0;
        _max = 0;
        _buf = null;
    }
}