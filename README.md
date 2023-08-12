**Prerequisites**

*Java Development Kit (JDK) 8 or later sudo 
*JavaFX library,
*SQLite JDBC driver,
*okhttp3 library,
*minio library,

Linux Machine
*Java Development Kit(JDK) 
*Min1O client library
*SQLite JDBC driver

Java JDK 8 or later:
*Open the terminal on your Linux machine.
*Check if Java is already installed by typing the following command: java -version
*If Java is not installed, install it by running the following command: sudo apt-get install default-jdk

Maven Download
*Open the terminal on Linux machine
*Download the latest version of Maven from the official Apache Maven website: https://maven.apache.org/download.cgi
*Extract the downloaded archive to the desired location on your machine: tar -zxvf apache-maven-{version}-bin.tar.gz
*Set the M2_HOME environment variable to the location where you extracted the archive: export M2_HOME=/path/to/apache-maven-{version}
*Add Maven to your PATH environment variable: export PATH=$M2_HOME/bin:$PATH


If you encounter the following error while executing or building the program:

```

/Cannot run program "..../abc.exe": error=13, Permission denied

```
Run the following command in the terminal:

```ruby
sudo chmod -R 777 <path to the program>
Password = "ntu-user"
```

**For any issue related to the dependencies visit the following website:**

```
https://search.maven.org
```

**Introduction**

This Java program is a file management application that runs in Docker containers.
It offers features such as user management encryption/decryption of passwords and files file management (creation deletion renaming moving and copying) file storage in different containers emulation of a terminal multi-user access single user login module container usage error/warning monitor file sharing and file restoration within 30 days of deletion. Admin users can also monitor errors and warnings generated in the central/file-storage containers.


**Docker and guidance**

We've used the docker container in our project. Some of the useful commands to be used are:


Runs a container from an image```
docker run <image_name> Runs a container from an image```

Lists all the running containers.```
docker ps```

Stops a running container```
docker stop <container_id>```

Removes a stopped container from your system```
docker rm <container_id>```

Removes an image from your system```
docker rmi <image_name>```

Builds a Docker image from a Dockerfile located in the current directory, and tags it with the specified name```
docker build -t <image_name>```

Starts containers based on the configuration in a Docker Compose file```
docker-compose up```

Stops and removes containers created by docker-compose up.```
docker-compose down```

Opens a Bash shell in a running container, allowing you to execute commands inside```
docker exec -it <container_id> bash```


**Installation**

The installation requirement in this project would be JavaFX library for the User interface. Additionally SQLite Library was used for the database connection in this project. Docker and MinIo server are also used in this repositary.

**Usage**

This software was designed to provide a user-friendly interface for file management and storage. It offers a range of features such as uploading, downloading, editing, and deleting files. The ability to organize files into folders and search for specific files by name or type provides a convenient way to manage large amounts of data. Additionally, the software offers a secure login system to ensure that only authorized users have access to sensitive data. The ability to recover deleted files and automatic deletion after 30 days provides a safety net against accidental data loss. Overall, this software is a useful tool for individuals or organizations looking for an efficient and organized way to manage their files.


**Contributing**

There are many ways in which others can contribute to this project. One way is to help improve the code by identifying and fixing bugs, adding new features, or optimizing existing ones. Another way is to write documentation or tutorials that explain how to use the software or contribute to the project. Additionally, users can provide feedback or report issues they encounter while using the software. Finally, those with design skills can help improve the user interface and make the software more user-friendly. If you're interested in contributing to this project, please feel free to fork the repository, make changes, and submit a pull request.

**License**

As the developer's of this project, We have chosen to license it under the MIT License. This means that anyone is free to use, modify, and distribute this software, both for commercial and non-commercial purposes, as long as they include a copy of the license in any redistributed code and give appropriate credit to the original creator. However, the software is provided as-is, without any warranties or guarantees of its functionality or suitability for any particular purpose. Any contributions made to the project must also be made under the terms of the MIT License.







