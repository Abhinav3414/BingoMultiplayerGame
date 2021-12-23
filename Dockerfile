FROM openjdk:8-jdk-alpine
ENV APP_HOME /app
WORKDIR $APP_HOME
ARG JAR_FILE=target/*.jar
COPY ${JAR_FILE} app.jar
ENTRYPOINT ["java","-Dspring.data.mongodb.uri=mongodb://db:27017/bingo_db", "-jar", "app.jar"]


#commands
#docker build -t bingo-docker .
#docker images
#docker rmi -f df897c4fbc07
#docker run bingo-docker
#docker pull mongo
#mkdir mongodata
#

