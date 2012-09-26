import module namespace read-pdf = "http://www.zorba-xquery.com/modules/read-pdf";
import module namespace file = "http://expath.org/ns/file";

declare namespace rpo =
      "http://www.zorba-xquery.com/modules/read-pdf/read-pdf-options";


let $pdf := file:read-binary(resolve-uri("pdftex-l.pdf"))
return
    read-pdf:extract-text($pdf, ())


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

