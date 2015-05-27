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

import org.apache.pdfbox.exceptions.InvalidPasswordException;
import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.pdmodel.PDDocumentCatalog;
import org.apache.pdfbox.pdmodel.PDDocumentNameDictionary;
import org.apache.pdfbox.pdmodel.PDEmbeddedFilesNameTreeNode;
import org.apache.pdfbox.pdmodel.common.filespecification.PDComplexFileSpecification;
import org.apache.pdfbox.pdmodel.common.filespecification.PDEmbeddedFile;
import org.apache.pdfbox.pdmodel.common.COSObjectable;
import org.apache.pdfbox.pdmodel.encryption.AccessPermission;
import org.apache.pdfbox.pdmodel.encryption.StandardDecryptionMaterial;
import org.apache.pdfbox.util.PDFText2HTML;
import org.apache.pdfbox.util.PDFTextStripper;

import java.awt.image.BufferedImage;
//import java.io.ByteArrayInputStream;
//import java.io.ByteArrayOutputStream;
//import java.io.FileInputStream;
//import java.io.FileNotFoundException;
//import java.io.FileOutputStream;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.StringWriter;
import java.io.Writer;
import java.lang.reflect.Field;
import java.util.Map;



public class ZorbaReadPdfModule
{
    static
    {
        // this modifies the java.library.path system property at runtime.
        addDir(System.getProperty("java.library.path") , Config.ZORBA_MODULE_RELATIVE_DIR);
        System.loadLibrary(Config.ZORBA_MODULE_LIBRARY_NAME);
    }

    private static void addDir(String baseLibPath, String relPath)
    {
        //System.out.println("        jlp " + System.getProperty("java.library.path") ); System.out.flush();

        String[] bases = baseLibPath.split(File.pathSeparator);

        for (String base: bases)
        {
            String path = base + File.separator + relPath;

            //System.out.println("addDir:  " + path); System.out.flush();

            try
            {
                // This enables the java.library.path to be modified at runtime
                // From a Sun engineer at http://forums.sun.com/thread.jspa?threadID=707176
                //
                Field field = ClassLoader.class.getDeclaredField("usr_paths");
                field.setAccessible(true);
                String[] paths = (String[])field.get(null);
                for (int i = 0; i < paths.length; i++)
                {
                    if (path.equals(paths[i]))
                    {
                        return;
                    }
                }
                String[] tmp = new String[paths.length+1];
                System.arraycopy(paths,0,tmp,0,paths.length);
                tmp[paths.length] = path;
                field.set(null,tmp);
                System.setProperty("java.library.path", System.getProperty("java.library.path") + File.pathSeparator + path);
            }
            catch (IllegalAccessException e)
            {
                throw new RuntimeException("Failed to get permissions to set library path");
            }
            catch (NoSuchFieldException e)
            {
                throw new RuntimeException("Failed to get field handle to set library path");
            }
        }
    }



    public static class Options
    {
        // common options
        private String _password = null;
        //private boolean _nonSequentialParser = false;
        private int _startPage = 1;
        private int _endPage = Integer.MAX_VALUE-1;

        // extractText only
        private static int TEXT_SIMPLE = 1;
        private static int TEXT_HTML = 2;
        private int _textKind = TEXT_HTML;

        private boolean _ignoreCorruptObjects = false;
        private boolean _ignoreBeads = false;

        // renderToImage only
        private static int IMAGE_JPG = 1;
        private static int IMAGE_PNG = 2;
        private int _imageKind = IMAGE_JPG;

        private String _startPageSeparator = "";
        private String _endPageSeparator = "";

        public Options()
        {
        }

        public Options(String password, boolean nonSequentialParser, int startPage, int endPage,
                       int textOutput, boolean ignoreCorruptObjects, boolean ignoreBeads,
                       String startPageSeparator, String endPageSeparator)
        {
            _password = password;
            //_nonSequentialParser = nonSequentialParser;
            _startPage = startPage;
            _endPage = endPage;
            _textKind = (textOutput == TEXT_SIMPLE ? TEXT_SIMPLE : TEXT_HTML);
            _ignoreCorruptObjects = ignoreCorruptObjects;
            _ignoreBeads = ignoreBeads;
            _startPageSeparator = startPageSeparator;
            _endPageSeparator = endPageSeparator;
        }

        public Options(String password, /*boolean nonSequentialParser,*/ int startPage, int endPage,
                       int imageOutput)
        {
            _password = password;
            //_nonSequentialParser = nonSequentialParser;
            _startPage = startPage;
            _endPage = endPage;
            _imageKind = (imageOutput == IMAGE_PNG ? IMAGE_PNG : IMAGE_JPG);
        }

        public String getPassword()
        {
            return _password;
        }

        public void setPassword(String password)
        {
            this._password = password;
        }

        /*public boolean isNonSequentialParser()
        {
          return _nonSequentialParser;
        }

        public void setNonSequentialParser(boolean nonSequentialParser)
        {
          this._nonSequentialParser = nonSequentialParser;
        }*/

        public int getStartPage()
        {
            return _startPage;
        }

        public void setStartPage(int startPage)
        {
            this._startPage = startPage;
        }

        public int getEndPage()
        {
            return _endPage;
        }

        public void setEndPage(int endPage)
        {
            this._endPage = endPage;
        }

        public int getTextKind()
        {
            return _textKind;
        }

        public void setTextKind(int textKind)
        {
            this._textKind = textKind;
        }

        public boolean isTextKindHtml()
        {
            return _textKind == TEXT_HTML;
        }

        public boolean isIgnoreCorruptObjects()
        {
            return _ignoreCorruptObjects;
        }

        public void setIgnoreCorruptObjects(boolean ignoreCorruptObjects)
        {
            this._ignoreCorruptObjects = ignoreCorruptObjects;
        }

        public boolean isIgnoreBeads()
        {
            return _ignoreBeads;
        }

        public void setIgnoreBeads(boolean ignoreBeads)
        {
            this._ignoreBeads = ignoreBeads;
        }

        public int getImageKind()
        {
            return _imageKind;
        }

        public void setImageKind(int imageKind)
        {
            this._imageKind = imageKind;
        }

        public boolean isImageKindJpg()
        {
            return this._imageKind == IMAGE_JPG;
        }

        public String getStartPageSeparator()
        {
            return _startPageSeparator;
        }

        public void setStartPageSeparator(String startPageSeparator)
        {
            _startPageSeparator = startPageSeparator;
        }

        public String getEndPageSeparator()
        {
            return _endPageSeparator;
        }

        public void setEndPageSeparator(String endPageSeparator)
        {
            _endPageSeparator = endPageSeparator;
        }

        @Override
        public String toString()
        {
            return "Options{" +
                    "_password='" + _password + '\'' +
                    ", _startPage=" + _startPage +
                    ", _endPage=" + _endPage +
                    ", _textKind=" + (_textKind == 1 ? "SIMPLE" : "HTML" )+
                    ", _ignoreCorruptObjects=" + _ignoreCorruptObjects +
                    ", _ignoreBeads=" + _ignoreBeads +
                    ", _imageKind=" + (_imageKind == IMAGE_JPG ? "JPG" : "PNG" ) +
                    ", _startPageSep='" + _startPageSeparator +
                    "', _endPageSep='" + _endPageSeparator +
                    "'}";
        }
    }



/*    public static class ByteArrayOutputStreamCreator
        implements  OutputStreamCreator
    {
        public OutputStream newOutputStream()
        {
            return new ByteArrayOutputStream();
        }
    }

    public static class FileOutputStreamCreator
            implements  OutputStreamCreator
    {
        int _index = 1;
        String _prefix = "Img";
        String _sufix  = "jpg";

        public FileOutputStreamCreator(String prefix, String sufix)
        {
            _prefix = prefix;
            _sufix = sufix;
        }

        public OutputStream newOutputStream() throws IOException
        {
            return new FileOutputStream(_prefix + (_index++) + "." + _sufix);
        }
    }*/


/*    public static String extractText(byte[] pdf, Options options)
    {
        InputStream pdfIs = new ByteArrayInputStream(pdf);
        return extractText(pdfIs, options);
    }*/

    public static String extractText(long pdfInputStreamPointer, Options options)
    {
        org.zorbaxquery.modules.readPdf.InputStreamFromCpp inputStreamFromCpp = new org.zorbaxquery.modules.readPdf.InputStreamFromCpp(pdfInputStreamPointer);
        return extractText(inputStreamFromCpp, options);
    }

    public static String extractText(InputStream pdfIs, Options options)
    {
        //System.out.println("java:    extractText() " + options); System.out.flush();
        try
        {
            Writer output = new StringWriter();
            PDDocument document = null;
            try
            {
                document = PDDocument.load(pdfIs, options.isIgnoreCorruptObjects());
                if( document.isEncrypted() )
                {
                    StandardDecryptionMaterial sdm = new StandardDecryptionMaterial( options.getPassword() );
                    document.openProtection( sdm );
                }

                AccessPermission ap = document.getCurrentAccessPermission();
                if( ! ap.canExtractContent() )
                {
                    throw new IOException( "You do not have permission to extract text" );
                }

                String encoding = "UTF-8";

                PDFTextStripper stripper = null;
                if(options.isTextKindHtml())
                {
                    stripper = new PDFText2HTML(encoding);
                }
                else
                {
                    stripper = new PDFTextStripper(encoding);
                }
                stripper.setPageStart( options.getStartPageSeparator() );
                stripper.setPageEnd( options.getEndPageSeparator() );

                stripper.setForceParsing(options.isIgnoreCorruptObjects());
                //stripper.setSortByPosition( sort );
                stripper.setShouldSeparateByBeads( options.isIgnoreBeads() );
                stripper.setStartPage( options.getStartPage() );
                stripper.setEndPage( options.getEndPage() );

                // Extract text for main document:
                stripper.writeText( document, output );

                // ... also for any embedded PDFs:
                PDDocumentCatalog catalog = document.getDocumentCatalog();
                PDDocumentNameDictionary names = catalog.getNames();
                if (names != null)
                {
                    PDEmbeddedFilesNameTreeNode embeddedFiles = names.getEmbeddedFiles();
                    if (embeddedFiles != null)
                    {
                        Map<String,? extends Object> embeddedFileNames = embeddedFiles.getNames();
                        if (embeddedFileNames != null) {
                            for (Map.Entry<String,? extends Object> ent : embeddedFileNames.entrySet())
                            {
                                PDComplexFileSpecification spec = (PDComplexFileSpecification) ent.getValue();
                                PDEmbeddedFile file = spec.getEmbeddedFile();
                                if (file.getSubtype().equals("application/pdf"))
                                {
                                    InputStream fis = file.createInputStream();
                                    PDDocument subDoc = null;
                                    try
                                    {
                                        subDoc = PDDocument.load(fis);
                                    }
                                    finally
                                    {
                                        fis.close();
                                    }
                                    try
                                    {
                                        stripper.writeText( subDoc, output );
                                    }
                                    finally
                                    {
                                        subDoc.close();
                                    }
                                }
                            }
                        }
                    }
                }
            }
            finally
            {
                if( output != null )
                {
                    output.close();
                }
                if( document != null )
                {
                    document.close();
                }
            }
            //System.out.println("java:    extractText() result '" + output.toString() + "'"); System.out.flush();
            return output.toString();
        }
        catch (IOException e)
        {
            throw new RuntimeException("Bad PDF document or input can not be processed.", e);
        }
        catch (Exception e)
        {
            throw new RuntimeException(e);
        }
    }

/*    public static byte[][] renderToImages(byte[] pdf, Options options)
    {
        InputStream pdfIs = new ByteArrayInputStream(pdf);

        OutputStream[] streamImages = renderToImages(pdfIs, options, new ByteArrayOutputStreamCreator());

        byte[][] result = new byte[streamImages.length][];
        for ( int i = 0 ; i<result.length ; i++)
        {
            result[i] = ((ByteArrayOutputStream)(streamImages[i])).toByteArray();
        }
        return result;
    }*/

    public static void renderToImages(long pdfInputStreamPointer,
                                                Options options,
                                                OutputStreamCreator osCreator)
    {
        org.zorbaxquery.modules.readPdf.InputStreamFromCpp inputStreamFromCpp =
                new org.zorbaxquery.modules.readPdf.InputStreamFromCpp(pdfInputStreamPointer);

        //return
        renderToImages(inputStreamFromCpp, options, osCreator);
    }

    public static OutputStream[] renderToImages(InputStream pdfIs, Options options, OutputStreamCreator osCreator)
    {
        //System.out.println("java:    renderToImages() " + options); System.out.flush();
        try
        {
            PDDocument document = null;
            try
            {
                document = PDDocument.load( pdfIs );
                if( document.isEncrypted() )
                {
                    try
                    {
                        document.decrypt( options.getPassword() );
                    }
                    catch( InvalidPasswordException e )
                    {
                        if( options.getPassword()!=null ) //they supplied the wrong password
                        {
                            throw new RuntimeException("Error: Wrong password.");
                        }
                        else
                        {
                            //they didn't supply a password and the default of "" was wrong.
                            throw new RuntimeException( "Error: The document is encrypted." );
                        }
                    }
                }

                int imageType = 24;
                String color = "rgb";

                if ("bilevel".equalsIgnoreCase(color))
                {
                    imageType = BufferedImage.TYPE_BYTE_BINARY;
                }
                else if ("indexed".equalsIgnoreCase(color))
                {
                    imageType = BufferedImage.TYPE_BYTE_INDEXED;
                }
                else if ("gray".equalsIgnoreCase(color))
                {
                    imageType = BufferedImage.TYPE_BYTE_GRAY;
                }
                else if ("rgb".equalsIgnoreCase(color))
                {
                    imageType = BufferedImage.TYPE_INT_RGB;
                }
                else if ("rgba".equalsIgnoreCase(color))
                {
                    imageType = BufferedImage.TYPE_INT_ARGB;
                }
                else
                {
                    new RuntimeException( "Error: Unrecognized color type. The number of bits per pixel must be 1, 8 or 24." );
                }

                String imageFormat = ( options.isImageKindJpg() ? "jpg" : "png");
                int resolution = 96;

                //Make the call
                ZorbaPdfImageWriter imageWriter = new ZorbaPdfImageWriter();
                boolean success = imageWriter.writeImages(document, imageFormat,
                        options.getStartPage(), options.getEndPage(), imageType, resolution, osCreator);
                if (!success)
                {
                    throw new RuntimeException( "Error: no writer found for image format '"
                            + imageFormat + "'" );
                }

                return imageWriter.getImagesAsStreams();
            }
            finally
            {
                if( document != null )
                {
                    document.close();
                }
            }
        }
        catch (IOException e)
        {
            throw new RuntimeException("Bad PDF document or input can not be processed.", e);
        }
        catch (Exception e)
        {
            throw new RuntimeException(e);
        }
    }


/*    public static void extractTest()
        throws FileNotFoundException
    {
        System.out.println("Extract : ");

        InputStream pdfIs = new FileInputStream("SCRFriday_Email_5.25.12.pdf");
        String res = extractText(pdfIs, new Options());

        System.out.println("Result : " + res);
    }

    public static void renderTest()
            throws FileNotFoundException
    {
        System.out.println("Render :");

        InputStream pdfIs = new FileInputStream("SCRFriday_Email_5.25.12.pdf");
        OutputStream[] res = renderToImages(pdfIs, new Options(), new FileOutputStreamCreator("page", "jpg"));
    }

    public static void main(String[] args)
            throws FileNotFoundException
    {
        renderTest();
    }*/
}
