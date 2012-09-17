package org.zorbaxquery.modules.readPdf;

import java.io.IOException;
import java.io.OutputStream;

public class CppOutputStreamCreator
    implements OutputStreamCreator
{
    private long _p;

    private native long nativeCreateNewOutputStream(long p);

    public CppOutputStreamCreator(long p)
    {
        _p = p;
    }
    
    public static CppOutputStreamCreator newInstance(long p)
    {
        return new CppOutputStreamCreator(p);
    }

    @Override
    public OutputStream newOutputStream() throws IOException
    {
        if ( _p==0 )
            return null;

        long pOs = nativeCreateNewOutputStream(_p);
        return new OutputStreamToCpp(pOs);
    }

    public void close()
    {
        _p = 0;
    }
}
