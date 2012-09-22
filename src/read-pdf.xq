xquery version "3.0";

(:
 : Copyright 2006-2012 The FLWOR Foundation.
 :
 : Licensed under the Apache License, Version 2.0 (the "License");
 : you may not use this file except in compliance with the License.
 : You may obtain a copy of the License at
 :
 : http://www.apache.org/licenses/LICENSE-2.0
 :
 : Unless required by applicable law or agreed to in writing, software
 : distributed under the License is distributed on an "AS IS" BASIS,
 : WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 : See the License for the specific language governing permissions and
 : limitations under the License.
 :)


(:~
 : This module provides funtionality to read the text from PDF documents and
 : to render PDF documents to images.
 : <a href="http://pdfbox.apache.org">Apache PDFBox</a> library is used to
 : implement these functions.
 : <br />
 : <br />
 : <b>Note:</b> Since this module has a Java library dependency a JVM required
 : to be installed on the system. For Windows: jvm.dll is required on the system
 : path ( usually located in "C:\Program Files\Java\jre6\bin\client".
 : <b>Note:</b> For Debian based Linux distributions install PdfBox and FontBox
 : packages: sudo apt-get install libpdfbox-java libfontbox-java
 :
 : @author Cezar Andrei
 : @see http://pdfbox.apache.org/
 : @library <a href="http://www.oracle.com/technetwork/java/javase/downloads/index.html">JDK - Java Development Kit</a>
 : @project data converters/read-pdf
 : @library <a href="http://pdfbox.apache.org/">Apache PDFBox</a>
 :)
module namespace read-pdf = "http://www.zorba-xquery.com/modules/read-pdf";


import schema namespace rp-options = "http://www.zorba-xquery.com/modules/read-pdf/read-pdf-options";

(:~
 : Import module for checking if options element is validated.
 :)
import module namespace sch = "http://www.zorba-xquery.com/modules/schema";


declare namespace err = "http://www.w3.org/2005/xqt-errors";

declare namespace ver = "http://www.zorba-xquery.com/options/versioning";
declare option ver:module-version "1.0";



(:~
 : Extracts the text of the input pdf.
 : <br />
 : Please consult the
 : <a href="http://pdfbox.apache.org/">official PDFBox documentation for further
 : information</a>.
 : <br />
 : Example:<pre class="brush: xquery;">
 :
 :  import module namespace read-pdf = "http://www.zorba-xquery.com/modules/read-pdf";
 :  import module namespace file = "http://expath.org/ns/file";
 :  declare namespace rpo =
 :      "http://www.zorba-xquery.com/modules/read-pdf/read-pdf-options";
 :  let $pdf := file:read-binary("path/to/my.pdf")
 :  let $options  :=
 :     <rpo:extract-text-options xmlns:rpo="http://www.zorba-xquery.com/modules/read-pdf/read-pdf-options">
 :       <rpo:text-kind>simple</rpo:text-kind>
 :       <rpo:start-page>2</rpo:start-page>
 :       <rpo:end-page>3</rpo:end-page>
 :       <rpo:password>decription_password</rpo:password>
 :       <rpo:ignore-corrupt-objects>false</rpo:ignore-corrupt-objects>
 :       <rpo:ignore-beads>false</rpo:ignore-beads>
 :       <rpo:start-page-separator>---start-page-separator---</rpo:start-page-separator>
 :       <rpo:end-page-separator>---end-page-separator---</rpo:end-page-separator>
 :     </rpo:extract-text-options>
 :  return
 :      read-pdf:extract-text($pdf, $options)
 :
 : </pre>
 : <br />
 : @param $pdf The input PDF instance as xs:base64Binary
 : @param $options Options:<br />
 :    <ul>
 :      <li>text-kind: string (default html) html: format output as HTML, simple: plain text.</li>
 :      <li>start-page: int (default 1) Which page to start with.</li>
 :      <li>end-page: int (default last document page) Which page to end with.</li>
 :      <li>password: string The decription password. Optional if PDF is password protected. </li>
 :      <li>ignore-corrupt-objects: boolean (default false) If true try recoved in case of corrupt objects,
 :          othewise exit with error.</li>
 :      <li>ignore-beads: boolean (default false) If true disables the separation by beads.</li>
 :      <li>start-page-separator: string The optional separator at every start page. </li>
 :      <li>end-page-separator: string The optional separator at every end page. </li>
 :    </ul>
 :
 :
 : @return The text contained in the PDF document.
 : @error read-pdf:VM001 If Zorba was unable to start the JVM.
 : @error read-pdf:JAVA-EXCEPTION If Apache PDFBox throws an exception.
 : example test/Queries/read-pdf/extractText-opt1.xq
 : example test/Queries/read-pdf/extractText-noOpt.xq
 : example test/Queries/read-pdf/extractText-badOpt.xq
 :)
declare function
read-pdf:extract-text($pdf as xs:base64Binary, $options as element(rp-options:extract-text-options)? )
  as xs:string
{
  let $validated-options :=
    if(empty($options))
    then
      $options
    else if(sch:is-validated($options))
    then
      $options
    else
      validate{$options}
  return
    read-pdf:extract-text-internal($pdf, $validated-options)
};


declare %private function
read-pdf:extract-text-internal( $pdf as xs:base64Binary,
    $options as element(rp-options:extract-text-options, rp-options:extract-text-optionsType)? )
  as xs:string external;


(:~
 : Renders each page of the PDF document as an image.
 : <br />
 : Please consult the
 : <a href="http://pdfbox.apache.org/">official PDFBox documentation for further
 : information</a>.
 : <br />
 : Example:<pre class="brush: xquery;">
 :
 :  import module namespace read-pdf = "http://www.zorba-xquery.com/modules/read-pdf";
 :  import module namespace file = "http://expath.org/ns/file";
 :  declare namespace rpo =
 :      "http://www.zorba-xquery.com/modules/read-pdf/read-pdf-options";
 :  let $pdf := file:read-binary("path/to/my.pdf")
 :  let $options  :=
 :     <rpo:render-to-images-options xmlns:rpo="http://www.zorba-xquery.com/modules/read-pdf/read-pdf-options">
 :       <rpo:image-kind>jpg</rpo:image-kind>
 :       <rpo:start-page>2</rpo:start-page>
 :       <rpo:end-page>3</rpo:end-page>
 :       <rpo:password>decription_password</rpo:password>
 :     </rpo:render-to-images-options>
 :  let $imgs := read-pdf:render-to-images($pdf, $options)
 :  for $img at $pos in $imgs
 :  return
 :  {
 :    file:write-binary("img-page" || $pos || ".jpg", $img);
 :    $pos
 :  }
 :
 : </pre>
 : <br />
 : @param $pdf The input PDF instance as xs:base64Binary
 : @param $options Options:<br />
 :    <ul>
 :      <li>image-kind: string (default jpg) Image type encoding. Supported encodings: jpg, png.</li>
 :      <li>start-page: int (default 1) Which page to start with.</li>
 :      <li>end-page: int (default last document page) Which page to end with.</li>
 :      <li>password: string  The decription password. Optional if PDF is password protected. </li>
 :    </ul>
 :
 : @return The rendered pages in the PDF document, as images.
 : @error read-pdf:VM001 If Zorba was unable to start the JVM.
 : @error read-pdf:JAVA-EXCEPTION If Apache PDFBox throws an exception.
 : example test/Queries/read-pdf/render-to-images-opt1.xq
 : example test/Queries/read-pdf/render-to-images-noOpt.xq
 : example test/Queries/read-pdf/render-to-images-badOpt.xq
 :)
declare function
read-pdf:render-to-images($pdf as xs:base64Binary,
    $options as element(rp-options:render-to-images-options)? )
  as xs:base64Binary*
{
  let $validated-options :=
    if(empty($options))
    then
      $options
    else if(sch:is-validated($options))
    then
      $options
    else
      validate{$options}
  return
    read-pdf:render-to-images-internal($pdf, $validated-options)
};


declare %private function
read-pdf:render-to-images-internal($pdf as xs:base64Binary,
    $options as element(rp-options:render-to-images-options, rp-options:render-to-images-optionsType)? )
  as xs:base64Binary* external;
