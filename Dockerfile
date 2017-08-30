FROM openjdk:8-jdk-alpine
ADD target/kona-travels-1.0-SNAPSHOT.jar kona-travels.jar
ENV JAVA_OPTS="-Xmx3500m -Xms3500m -server -XX:+UseConcMarkSweepGC -XX:CMSInitiatingOccupancyFraction=95 -XX:+UseCMSInitiatingOccupancyOnly"
EXPOSE 80
ENTRYPOINT [ "sh", "-c", "java $JAVA_OPTS -jar /kona-travels.jar /tmp/data/data.zip" ]