FROM maven:3.8.2-openjdk-16
WORKDIR /app
COPY pom.xml /app/pom.xml
RUN mvn dependency:go-offline
COPY . /app
RUN mvn package

FROM openjdk:16
COPY --from=0 /app/target/PhadowsocksR-1.0-SNAPSHOT-jar-with-dependencies.jar /app/psr.jar
CMD java -jar /app/psr.jar -port 1080


