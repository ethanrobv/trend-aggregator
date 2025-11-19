FROM eclipse-temurin:21-jre-jammy

WORKDIR /topics

COPY build/libs/*.jar topics.jar

EXPOSE 8080

ENTRYPOINT ["java", "-jar", "/topics/topics.jar"]
