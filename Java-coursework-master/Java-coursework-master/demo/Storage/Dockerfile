# Use an official Ubuntu runtime as a parent image
FROM ubuntu:20.04

# Install OpenSSH server
RUN apt-get update && \
    apt-get install -y openssh-server && \
    apt-get clean

# Set the working directory
WORKDIR /app

# Create the data directory and change ownership and permissions
RUN mkdir -p /app/storage && chown -R 1000:1000 /app/storage && chmod -R 755 /app/storage

# Create the privilege separation directory
RUN mkdir -p /run/sshd

# Expose port 22 for SSH access
EXPOSE 22

# Start OpenSSH server
CMD ["/usr/sbin/sshd", "-D"]
