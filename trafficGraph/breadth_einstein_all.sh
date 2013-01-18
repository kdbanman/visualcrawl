# Script for visualization of LDSpider's crawl behaviour using Gephi's HTTP
# Graph plugin

# 3 Round Breadth-first crawl from Albert Einstein's dbpedia URI
# max 30 URI crawl per round per PLD, no data stored, following all links

mkdir breadth_einstein_all

echo "http://dbpedia.org/resource/Albert_Einstein">breadth_einstein_all/seed.txt

java -Dhttp.proxyHost=localhost -Dhttp.proxyPort=8088 -jar ldspider-1.1e.jar -b 3 30 -s breadth_einstein_all/seed.txt

