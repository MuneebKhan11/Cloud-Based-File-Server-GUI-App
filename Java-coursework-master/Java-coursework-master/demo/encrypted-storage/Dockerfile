FROM ubuntu:20.04

# Update packages and install required tools
RUN apt-get update && apt-get install -y \
    openjdk-11-jre \
    openssh-server \
    openssl

# Set up SFTP server
RUN mkdir -p /var/run/sshd
RUN echo 'Subsystem sftp internal-sftp' >> /etc/ssh/sshd_config

# Set up the user and their directory
ARG USERNAME
ARG PASSWORD
RUN useradd -m -p $(openssl passwd -1 $PASSWORD) -s /usr/sbin/nologin $USERNAME
RUN mkdir -p /home/$USERNAME/data
RUN chown $USERNAME:$USERNAME /home/$USERNAME/data

# Expose the SFTP port
EXPOSE 22

# Start the SSH server and keep the container running
CMD service ssh start && tail -f /dev/null
