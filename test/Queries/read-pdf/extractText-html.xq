import module namespace read-pdf = "http://www.zorba-xquery.com/modules/read-pdf";
import module namespace file = "http://expath.org/ns/file";

declare namespace rpo =
      "http://www.zorba-xquery.com/modules/read-pdf/read-pdf-options";


let $pdf := file:read-binary(resolve-uri("28msec-NoSQLNow.pdf"))
let $options  := 
    <rpo:extract-text-options>
        <rpo:text-kind>html</rpo:text-kind>
    </rpo:extract-text-options> 
return
    read-pdf:extract-text($pdf, $options)


