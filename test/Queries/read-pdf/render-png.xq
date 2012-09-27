import module namespace read-pdf = "http://www.zorba-xquery.com/modules/read-pdf";
import module namespace file = "http://expath.org/ns/file";

declare namespace rpo =
      "http://www.zorba-xquery.com/modules/read-pdf/read-pdf-options";


let $pdf := file:read-binary(resolve-uri("28msec-NoSQLNow.pdf"))
let $options  :=
    <rpo:render-to-images-options>
        <rpo:image-kind>png</rpo:image-kind>
        <rpo:start-page>4</rpo:start-page>
        <rpo:end-page>5</rpo:end-page>
    </rpo:render-to-images-options>
let $imgs := read-pdf:render-to-images($pdf, $options)
for $img in $imgs
return
    fn:string-length(fn:string($img)) > 1000

