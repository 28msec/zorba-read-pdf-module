import module namespace read-pdf = "http://www.zorba-xquery.com/modules/read-pdf";
import module namespace file = "http://expath.org/ns/file";

declare namespace rpo =
      "http://www.zorba-xquery.com/modules/read-pdf/read-pdf-options";


let $pdf := file:read-binary(resolve-uri("pdftex-l.pdf"))
let $imgs := read-pdf:render-to-images($pdf, ())
for $img in $imgs
return
    fn:string-length(fn:string($img)) > 1000