
Problem Statement  ####

Task:​ ​ ​ Construct​ ​ the​ ​ Inverted​ ​ Index​ ​ from​ ​ the​ ​ given​ ​ small​ ​ snapshot​ ​ of​ ​ Wikipedia​ ​ dump. 

Basic​ ​ Stages​ ​ (in​ ​ order): 
 
● XML parsing [Prefer SAX parser over DOM parser. If you use DOM parser, you can’t scale​ ​ it​ ​ up​ ​ for​ ​ the​ ​ full​ ​ Wikipedia​ ​ dump​ ​ later​ ​ on.] 
● Tokenization 
● Case​ ​ folding 
● Stop​ ​ words​ ​ removal 
● Stemming  
● Posting​ ​ List​ ​ /​ ​ Inverted​ ​ Index​ ​ Creation 
● Optimize 
 
Desirable​ ​ Features: 
● Support for Field Queries ​ . Fields include Title, Infobox, Body, Category, Links, and References of a Wikipedia page. This helps when a user is interested in searching for the movie ‘Up’ where he would like to see the page containing the word ‘Up’ in the title and the word ‘Pixar’ in the Infobox. You can store field type along with the word when you​ ​ index. 
● Index​ ​ size​ ​ should​ ​ be​ ​ less​ ​ than​ ​ 1⁄4​ ​ of​ ​ dump​ ​ size.​ ​ 
● Scalable​ ​ index​ ​ construction​ ​ [See​ ​ Chapter​ ​ 4​ ​ in​ ​ the​ ​ ‘Intro​ ​ to​ ​ IR’​ ​ book.] 


Project details ####

Title 		: Wikipedia search engine (IIITH) 
Platform    : J2EE
Description :
   A Wikipedia search engine built using  Java, XML Parsing using SAX Parser,Ranking   Algorithms High level of indexing which reduces the search time ,The index terms are hashed to  characters 'a' - 'z', Index is compressed at bitlevel, Special infobox parsing to provide direct  answers if possible, Index compression to make index half of its size, Special search fields  provided so that user can directly search Special Features  :  Index compression to make index half of its size. ( bit level compression ) Special search fields provided so that user can directly search info infobox    

Statistics :  

For 100 GB of data Wiki XML Dump      
Size of index (primary + secondary ) 		: 25 GB     
Time to index								: 6hr (average)     
Time to search								: 0.251 sec (average on 100 searches) 
 

 
#############################################################################################################