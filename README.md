# kona-travels

Java + rapidoid-http-fast solution for highloadcup #1

build executable jar:

mvn clean package

run server:

java -jar <path_to_jar> <path_to_data.zip> -Xmx3700m -Xms3700m -server -XX:+UseConcMarkSweepGC -XX:CMSInitiatingOccupancyFraction=95
