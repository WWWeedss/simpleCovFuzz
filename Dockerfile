# Use an official Ubuntu as a parent image
FROM ubuntu:22.04

# Set environment variables to avoid user interaction during installation
ENV DEBIAN_FRONTEND=noninteractive

# Install Java 21 and Python
RUN apt-get update && \
    apt-get install -y openjdk-21-jdk python3 python3-pip && \
    apt-get clean

# Install matplotlib
RUN pip3 install matplotlib

# Set the working directory in the container
WORKDIR /app

# Copy the current directory contents into the container at /app
COPY . /app

# Compile Java code (assuming there's a build script or use javac directly)
# RUN ./build.sh
# or
RUN javac -d bin src/*.java

# Make port 80 available to the world outside this container
EXPOSE 80

# Define environment variable
ENV NAME World

# Run the application (adjust the command as needed for your project)
# For example, if you have a Java entry point:
CMD ["java", "-cp", "bin", "MainClass"]
