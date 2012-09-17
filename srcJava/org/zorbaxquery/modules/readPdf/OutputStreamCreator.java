package org.zorbaxquery.modules.readPdf;

import java.io.IOException;
import java.io.OutputStream;


public interface OutputStreamCreator
{
    public OutputStream newOutputStream() throws IOException;
}
