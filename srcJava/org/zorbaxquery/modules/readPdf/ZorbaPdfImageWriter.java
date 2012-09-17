package org.zorbaxquery.modules.readPdf;

import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.pdmodel.PDPage;
import org.apache.pdfbox.util.PDFStreamEngine;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

import javax.imageio.IIOException;
import javax.imageio.IIOImage;
import javax.imageio.ImageIO;
import javax.imageio.ImageTypeSpecifier;
import javax.imageio.ImageWriteParam;
import javax.imageio.ImageWriter;
import javax.imageio.metadata.IIOInvalidTreeException;
import javax.imageio.metadata.IIOMetadata;
import javax.imageio.metadata.IIOMetadataNode;
import javax.imageio.stream.ImageOutputStream;
import java.awt.image.BufferedImage;
import java.awt.image.RenderedImage;
import java.io.IOException;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

public class ZorbaPdfImageWriter
        extends PDFStreamEngine
{
    private List<OutputStream> images = new ArrayList<OutputStream>();
    private static OutputStream[] OS_ARRAY = new OutputStream[0];
    
    public boolean writeImages(PDDocument document, String imageFormat,
                              int startPage, int endPage, int imageType, int resolution,
                              OutputStreamCreator osCreator)
            throws IOException
    {
        boolean bSuccess = true;
        List pages = document.getDocumentCatalog().getAllPages();
        for( int i = startPage - 1; i < endPage && i < pages.size(); i++ )
        {
            PDPage page = (PDPage)pages.get( i );
            BufferedImage image = page.convertToImage(imageType, resolution);

            OutputStream outputStream = osCreator.newOutputStream();
            images.add(outputStream);

            bSuccess &= writeImage(image, imageFormat, outputStream, resolution);
            outputStream.close();
        }
        return bSuccess;
    }


    public OutputStream[] getImagesAsStreams()
    {
        return images.toArray(OS_ARRAY);
    }

    private static boolean writeImage(BufferedImage image, String imageFormat, OutputStream outputStream, int resolution)
            throws IOException
    {
        boolean bSuccess = true;
        ImageOutputStream output = null;
        ImageWriter imageWriter = null;
        try
        {
            output = ImageIO.createImageOutputStream(outputStream);

            boolean foundWriter = false;
            Iterator<ImageWriter> writerIter = ImageIO.getImageWritersByFormatName( imageFormat );
            while( writerIter.hasNext() && !foundWriter )
            {
                try
                {
                    imageWriter = (ImageWriter)writerIter.next();
                    ImageWriteParam writerParams = imageWriter.getDefaultWriteParam();
                    if( writerParams.canWriteCompressed() )
                    {
                        writerParams.setCompressionMode(ImageWriteParam.MODE_EXPLICIT);
                        // reset the compression type if overwritten by setCompressionMode
                        if (writerParams.getCompressionType() == null)
                        {
                            writerParams.setCompressionType(writerParams.getCompressionTypes()[0]);
                        }
                        writerParams.setCompressionQuality(1.0f);
                    }
                    IIOMetadata meta = createMetadata( image, imageWriter, writerParams, resolution);
                    imageWriter.setOutput( output );
                    imageWriter.write( null, new IIOImage( image, null, meta ), writerParams );
                    foundWriter = true;
                }
                catch( IIOException io )
                {
                    throw new IOException( io.getMessage() );
                }
                finally
                {
                    if( imageWriter != null )
                    {
                        imageWriter.dispose();
                    }
                }
            }
            if( !foundWriter )
            {
                bSuccess = false;
            }
        }
        finally
        {
            if( output != null )
            {
                output.flush();
                output.close();
            }
        }
        return bSuccess;
    }

    private static IIOMetadata createMetadata(RenderedImage image, ImageWriter imageWriter,
                                              ImageWriteParam writerParams, int resolution)
    {
        ImageTypeSpecifier type;
        if (writerParams.getDestinationType() != null)
        {
            type = writerParams.getDestinationType();
        }
        else
        {
            type = ImageTypeSpecifier.createFromRenderedImage( image );
        }
        IIOMetadata meta = imageWriter.getDefaultImageMetadata( type, writerParams );
        return (addResolution(meta, resolution) ? meta : null);
    }

    private static final String STANDARD_METADATA_FORMAT = "javax_imageio_1.0";

    private static boolean addResolution(IIOMetadata meta, int resolution)
    {
        if (!meta.isReadOnly() && meta.isStandardMetadataFormatSupported())
        {
            IIOMetadataNode root = (IIOMetadataNode)meta.getAsTree(STANDARD_METADATA_FORMAT);
            IIOMetadataNode dim = getChildNode(root, "Dimension");
            if (dim == null)
            {
                dim = new IIOMetadataNode("Dimension");
                root.appendChild(dim);
            }
            IIOMetadataNode child;
            child = getChildNode(dim, "HorizontalPixelSize");
            if (child == null)
            {
                child = new IIOMetadataNode("HorizontalPixelSize");
                dim.appendChild(child);
            }
            child.setAttribute("value",
                    Double.toString(resolution / 25.4));
            child = getChildNode(dim, "VerticalPixelSize");
            if (child == null)
            {
                child = new IIOMetadataNode("VerticalPixelSize");
                dim.appendChild(child);
            }
            child.setAttribute("value",
                    Double.toString(resolution / 25.4));
            try
            {
                meta.mergeTree(STANDARD_METADATA_FORMAT, root);
            }
            catch (IIOInvalidTreeException e)
            {
                throw new RuntimeException("Cannot update image metadata: "
                        + e.getMessage());
            }
            return true;
        }
        return false;
    }

    private static IIOMetadataNode getChildNode(Node n, String name)
    {
        NodeList nodes = n.getChildNodes();
        for (int i = 0; i < nodes.getLength(); i++)
        {
            Node child = nodes.item(i);
            if (name.equals(child.getNodeName()))
            {
                return (IIOMetadataNode)child;
            }
        }
        return null;
    }
}
