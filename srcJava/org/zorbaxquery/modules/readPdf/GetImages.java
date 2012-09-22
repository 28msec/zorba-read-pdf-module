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

import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.pdmodel.PDPage;
import org.apache.pdfbox.pdmodel.PDResources;
import org.apache.pdfbox.pdmodel.common.PDMetadata;
import org.apache.pdfbox.pdmodel.graphics.xobject.PDXObjectImage;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

/**
 * Sample code to extract the images embedded into Pdf documents
 */
public class GetImages
{
  private int imageCounter = 0;


  public static void main(String[] args)
  {
    if ( args.length != 1 )
    {
      System.out.println("USAGE: GetImages file.pdf");
      return;
    }

    GetImages gi = new GetImages();
    gi.getXMPInformation(args[0]);
  }

  public void getXMPInformation(String path)
  {
    // Open PDF document
    PDDocument document = null;
    try
    {
      document = PDDocument.load(path);
    }
    catch (IOException e)
    {
      e.printStackTrace();
    }
    // Get all pages and loop through them
    List pages = document.getDocumentCatalog().getAllPages();
    Iterator iter = pages.iterator();
    int pageNo = 1;
    while (iter.hasNext())
    {
      System.out.println("Examining page " + pageNo ++ + " :");
      PDPage page = (PDPage) iter.next();
      PDResources resources = page.getResources();
      Map images = null;
      // Get all Images on page
      try
      {
        images = resources.getImages();
      }
      catch (IOException e)
      {
        e.printStackTrace();
      }
      if (images != null)
      {
        // Check all images for metadata
        Iterator imageIter = images.keySet().iterator();
        while (imageIter.hasNext())
        {
          String key = (String) imageIter.next();
          PDXObjectImage image = (PDXObjectImage) images.get(key);
          PDMetadata metadata = image.getMetadata();
          System.out.println("Found a image: Analyzing for Metadata");
          if (metadata == null)
          {
            System.out.println("No Metadata found for this image.");
            System.out.println("image: " + image.getWidth() + "x" + image.getHeight() + " " + image.getSuffix());
            try
            {
              System.out.println("       bitsPerComponent: " + image.getBitsPerComponent() +
                      "  colorSpace: " + image.getColorSpace().getName() + "  hasImageMask: " + image.getImageMask());
            }
            catch (IOException e)
            {
              e.printStackTrace();
            }
          }
          else
          {
            InputStream xmlInputStream = null;
            try
            {
              xmlInputStream = metadata.createInputStream();
            }
            catch (IOException e)
            {
              e.printStackTrace();
            }
            try
            {
              System.out.println("--------------------------------------------------------------------------------");
              String mystring = convertStreamToString(xmlInputStream);
              System.out.println(mystring);
            }
            catch (IOException e)
            {
              e.printStackTrace();
            }
          }
          // Export the images
          String name = getUniqueFileName(key, image.getSuffix());
          System.out.println("Writing image:" + name);
          try
          {
            //image.write2file(name);
            File f = new File(name);
            OutputStream os = new FileOutputStream(f);
            image.write2OutputStream(os);
          }
          catch (IOException e)
          {
            e.printStackTrace();
          }
          System.out.println("--------------------------------------------------------------------------------");
        }
      }
    }
  }

  public String convertStreamToString(InputStream is) throws IOException
  {
    /*
     * To convert the InputStream to String we use the BufferedReader.readLine()
     * method. We iterate until the BufferedReader return null which means
     * there's no more data to read. Each line will appended to a StringBuilder
     * and returned as String.
     */
    if (is != null)
    {
      StringBuilder sb = new StringBuilder();
      String line;

      try
      {
        BufferedReader reader = new BufferedReader(new InputStreamReader(is, "UTF-8"));
        while ((line = reader.readLine()) != null)
        {
          sb.append(line).append("\n");
        }
      }
      finally
      {
        is.close();
      }
      return sb.toString();
    }
    else
    {
      return "";
    }
  }

  private String getUniqueFileName(String prefix, String suffix)
  {
    String uniqueName = null;
    File f = null;
    while (f == null || f.exists())
    {
      uniqueName = prefix + "-" + imageCounter + "." + suffix;
      f = new File(uniqueName);
      imageCounter++;
    }

    return uniqueName;
  }

}
