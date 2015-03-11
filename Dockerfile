FROM sismics/debian-java7-jetty9
MAINTAINER benjamin.gam@gmail.com

ADD reader-web/target/reader-web-*.war /opt/jetty/webapps/reader.war
ADD reader.xml /opt/jetty/webapps/reader.xml
