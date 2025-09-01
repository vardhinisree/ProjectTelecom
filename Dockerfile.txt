FROM openjdk:latest
#FROM adoptopenjdk/openjdk11:alpine-jre
#RUN addgroup -S spring && adduser -S spring -G spring
#USER spring:spring
#VOLUME /tmp
ARG VER=1.0
ARG JAR_FILE=target/sampleproject-0.0.1-SNAPSHOT.jar
#COPY ${JAR_FILE} spring-boot-docker-jenkins.jar
ADD ${JAR_FILE} sampleproject-0.0.1-SNAPSHOT.jar
ENTRYPOINT ["java","-jar","sampleproject-0.0.1-SNAPSHOT.jar"]
#EXPOSE 2222
EXPOSE 8086