FROM gradle:7.6.1-jdk11 as builder
WORKDIR /app
COPY src ./src
COPY build.gradle.kts ./build.gradle.kts
RUN gradle assemble

FROM openjdk:11 as backend
WORKDIR /root
RUN mkdir images
ENV TZ Europe/Moscow
COPY --from=builder /app/build/libs/*.jar app.jar
EXPOSE 8080
ENTRYPOINT ["java","-jar","app.jar"]
