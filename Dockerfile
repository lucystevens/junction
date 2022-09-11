FROM openjdk:11 AS build

RUN mkdir /src
COPY . /src
WORKDIR /src
RUN ./gradlew shadowJar --no-daemon

FROM adoptopenjdk/openjdk11:alpine

RUN mkdir /app
COPY --from=build /src/build/libs/*.jar /app/application.jar

CMD ["java", "-jar", "/app/application.jar"]