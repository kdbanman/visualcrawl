# Script for visualization of LDSpider's crawl behaviour using Gephi's HTTP
# Graph plugin

# Depth-first (load balanced) crawl from Albert Einstein's dbpedia URI
# 110 URI crawl ceiling, no data stored, following all links

mkdir depth_einstein_all

echo "http://dbpedia.org/resource/Albert_Einstein">depth_einstein_all/seed.txt

java -Dhttp.proxyHost=localhost -Dhttp.proxyPort=8088 -jar ldspider-1.1e.jar -c 110 -s depth_einstein_all/seed.txt

