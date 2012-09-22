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
