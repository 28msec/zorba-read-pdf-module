<?xml version="1.0" encoding="UTF-8"?>
Harnessing Flexible Data in the Cloud
Matthias Brantner (brantner@28msec.com)
Thursday, August 23, 12Demo
Thursday, August 23, 12Flexible Data
JSON
NITF
(XML)
JSON
SportsML
Text
SportsML
SportsML
Thursday, August 23, 12Harness Data
•
store
•
query &amp; update
•
full-text search
•
complex queries
•
transform
reliability, availability, performance, and scalability
Thursday, August 23, 12Possible Solution Stack
XML 
DB
Thursday, August 23, 12Development Time
Demo App
10 Weeks
2 Weeks
28msec
“manual” approach
Thursday, August 23, 12How do we do this?
Thursday, August 23, 12XML / JSON / Text
Processing Language
f
u
n
c
t
i
o
n
a
l
q
u
e
r
y
d
e
c
l
a
r
a
t
i
v
e
s
c
r
i
p
t
i
n
g
f
u
l
l
-
t
e
x
t
u
p
d
a
t
e
JSONiq
Thursday, August 23, 12Example (1)
1  for $zip in db:collection("zips")
2  group by $zip("state")
3  let $count := count($zip)
4  order by $count descending
5  return { “state” : $state, “count” : $count }
{ "state" : "TX",  "count" : 1676 }
{ "state" : "NY",  "count" : 1596 }
{ "state" : "CA",  "count" : 1500 }
...
Thursday, August 23, 12Example (2)
1  let $search-result := jn:parse-json(
2    http:get-text("twitter.com/search.json?q=.")[2])
3  for $result in jn:members($search-result("results"))
4  let $text := $result("text")
5  for $token in ft:tokenize-string($text)
6  where not(ft:is-stop-word($token))
7  let $lc := ft:strip-diacritics(lower-case($token))
8  group by $lc
9  let $count := count($token)
10 order by $count descending
11 return { “token” : $lc, “count” : $count }
{ "token" : "hard",  "count" : 3 }
{ "token" : "truths",  "count" : 3 }
{ "token" : "sql",  "count" : 3 }
{ "token" : "revolution",  "count" : 2 }
Thursday, August 23, 12Module Library
Thursday, August 23, 12Productivity
0
3000
6000
9000
12000
15000
Scientific Publication Forum             AWS
6,531
2,496
3,490
13,803
8,100
L
i
n
e
s
 
o
f
 
C
o
d
e
Java JSONiq PHP
Thursday, August 23, 12Data Store
JSON / XML
d
o
c
u
m
e
n
t
 
s
t
o
r
e
p
e
r
f
o
r
m
a
n
t
h
i
g
h
 
a
v
a
i
l
a
b
i
l
i
t
y
s
c
a
l
a
b
l
e
a
t
o
m
i
c
 
u
p
d
a
t
e
s
Thursday, August 23, 12Storage Layout (1)
JSONiq
Collection
MongoDB
Collection
{
  "name" : "Matthias"
  "dob" : 1978-09-12
}
&lt;person&gt;
  &lt;name&gt;Matthias&lt;/name&gt;
  &lt;dob&gt;1978-09-12&lt;/dob&gt;
&lt;/person&gt;
...
&lt;person&gt;
  &lt;name&gt;Matthias&lt;/name&gt;
  &lt;dob&gt;1978-09-12&lt;/dob&gt;
&lt;/person&gt;
{
  "name" : "Matthias"
  "dob" : 1978-09-12
}
Thursday, August 23, 12Storage Layout (2)
JSONiq
Collection
MongoDB
Collection
{
  "name" : "Matthias"
  "dob" : 1978-09-12
}
&lt;person&gt;
  &lt;name&gt;Matthias&lt;/name&gt;
  &lt;dob&gt;1978-09-12&lt;/dob&gt;
&lt;/person&gt;
...
Index (BTree)
&lt;person&gt;
  &lt;name&gt;Matthias&lt;/name&gt;
  &lt;dob&gt;1978-09-12&lt;/dob&gt;
&lt;/person&gt;
{
  "name" : "Matthias"
  "dob" : 1978-09-12
}
Thursday, August 23, 12Cloud Infrastructure - Request Processing
28msec
MongoDB
Request Handler
JSONiq Processor
ELB
DataCompiled Code
HTTP Client
1
2
3
4
5 6
7
8
R
R
Store
Processor
R
9
Thursday, August 23, 12Conclusion
•
JSONiq to process flexible data
•
using MongoDB as data store and index
    Database
•
up to 5x more (cost) effective
•
scalable infrastructure on AWS
Thursday, August 23, 12JSONiq talks
•
11:00 am
•
Jonathan Robie (EMC)
•
11:45am
•
Chris Hillery (FLWOR Foundation)
Thursday, August 23, 12Matthias Brantner (brantner@28msec.com)
Thank You!
Questions?
Visit us at our booth!
Thursday, August 23, 12