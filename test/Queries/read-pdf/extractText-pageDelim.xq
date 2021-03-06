import module namespace read-pdf = "http://www.zorba-xquery.com/modules/read-pdf";
import module namespace file = "http://expath.org/ns/file";

declare namespace rpo =
      "http://www.zorba-xquery.com/modules/read-pdf/read-pdf-options";


let $pdf := file:read-binary(resolve-uri("28msec-NoSQLNow.pdf"))
let $options  :=
    <rpo:extract-text-options>
        <rpo:text-kind>simple</rpo:text-kind>
        <rpo:end-page>3</rpo:end-page>
        <rpo:start-page-separator>
        ---start-page-sep---
        </rpo:start-page-separator>
        <rpo:end-page-separator>
        ---end-page-sep---
        </rpo:end-page-separator>
    </rpo:extract-text-options>
let $str := read-pdf:extract-text($pdf, $options)
return
     (fn:contains($str, "---start-page-sep---"),
      fn:contains($str, "Harnessing Flexible Data in the Cloud"),
      fn:contains($str, "Thursday, August 23, 12"),
      fn:contains($str, "---end-page-sep---"),
      fn:string-length($str) > 250 )