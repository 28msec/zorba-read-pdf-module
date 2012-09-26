import module namespace read-pdf = "http://www.zorba-xquery.com/modules/read-pdf";
import module namespace file = "http://expath.org/ns/file";

declare namespace rpo =
      "http://www.zorba-xquery.com/modules/read-pdf/read-pdf-options";


let $pdf := file:read-binary(resolve-uri("28msec-NoSQLNow.pdf"))
let $options  := 
    <rpo:render-to-images-options>
        <rpo:image-kind>jpg</rpo:image-kind>
        <rpo:start-page>2</rpo:start-page>
        <rpo:end-page>3</rpo:end-page>
    </rpo:render-to-images-options> 
return
    read-pdf:render-to-images($pdf, $options)

