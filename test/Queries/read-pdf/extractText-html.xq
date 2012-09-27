import module namespace read-pdf = "http://www.zorba-xquery.com/modules/read-pdf";
import module namespace file = "http://expath.org/ns/file";

declare namespace rpo =
      "http://www.zorba-xquery.com/modules/read-pdf/read-pdf-options";


let $pdf := file:read-binary(resolve-uri("28msec-NoSQLNow.pdf"))
let $options  :=
    <rpo:extract-text-options>
        <rpo:text-kind>html</rpo:text-kind>
    </rpo:extract-text-options>
let $str := read-pdf:extract-text($pdf, $options)
return
     (fn:contains($str, "DOCTYPE html PUBLIC"),
      fn:contains($str, 'http-equiv="Content-Type" content="text/html; charset=UTF-8"'),
      fn:contains($str, "Harnessing Flexible Data in the Cloud"),
      fn:contains($str, "SportsML"),
      fn:contains($str, "reliability, availability, performance, and scalability"),
      fn:contains($str, "FLWOR Foundation"),
      fn:contains($str, "Visit us at our booth!"),
      fn:string-length($str) > 1000 )

