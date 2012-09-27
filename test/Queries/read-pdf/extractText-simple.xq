import module namespace read-pdf = "http://www.zorba-xquery.com/modules/read-pdf";
import module namespace file = "http://expath.org/ns/file";

declare namespace rpo =
      "http://www.zorba-xquery.com/modules/read-pdf/read-pdf-options";


let $pdf := file:read-binary(resolve-uri("pdftex-l.pdf"))
let $str := read-pdf:extract-text($pdf, ())
return
 (fn:contains($str, "DOCTYPE html PUBLIC"),
  fn:contains($str, "The pdfTEX user manual"),
  fn:contains($str, "Sebastian Rahtz"),
  fn:contains($str, "Hans Hagen"),
  fn:contains($str, "Hartmut Henkel"),
  fn:contains($str, "Jackowski"),
  fn:contains($str, "Martin"),
  fn:string-length($str) > 1000 )


(: Debug script to write images to disk

let $pdf := file:read-binary(resolve-uri("pdftex-l.pdf"))
let $imgs := read-pdf:render-to-images($pdf, () )
for $img at $pos in $imgs
   return
   {
     file:write-binary("img-" || $pos || ".jpg", $img);
     $pos
   }
:)

