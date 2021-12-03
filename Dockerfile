FROM openjdk:11

WORKDIR /app

COPY MockMock-1.4.0.one-jar.jar .

EXPOSE 8282 25

ENTRYPOINT ["java", "-jar", "MockMock-1.4.0.one-jar.jar"]
