# Use a base image with Java 11 and Maven installed
FROM adoptopenjdk:11-jdk-hotspot AS build

RUN apt-get update && \
    apt-get install -y maven
# Set the working directory
WORKDIR /app
# Copy the source code
COPY src ./src

# Copy the pom.xml file
COPY pom.xml ./pom.xml

# Build the application
RUN mvn clean package -DskipTests

# Create the final image
FROM adoptopenjdk:11-jre-hotspot
# Set the working directory
WORKDIR /root
ENV TZ Europe/Moscow
# Copy the JAR file from the build stage
COPY --from=build /app/target/*.jar motorexport.jar
EXPOSE 8080
# Set the entrypoint command
ENTRYPOINT ["java", "-jar", "motorexport.jar"]