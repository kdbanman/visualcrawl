# Script for visualization of LDSpider's crawl behaviour using Gephi's HTTP
# Graph plugin

# Depth-first (load balanced) crawl from The Shining's LinkedMDB URI
# 100 URI crawl ceiling, no data stored, following all links

mkdir depth_shining_all

echo "http://data.linkedmdb.org/resource/film/2014">depth_shining_all/seed.txt

java -Dhttp.proxyHost=localhost -Dhttp.proxyPort=8088 -jar ldspider-1.1e.jar -c 100 -s depth_shining_all/seed.txt

