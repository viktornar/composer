Composer - Web application for creating map atlas.
==================================================
Composer is a simple web application  that can be used for creating map atlas by using Lithuanian online base map that are distributed by National Land Agency.
![image](https://raw.githubusercontent.com/viktornar/composer/master/img/1.PNG)

![image](https://raw.githubusercontent.com/viktornar/composer/master/img/2.PNG)

![image](https://raw.githubusercontent.com/viktornar/composer/master/img/3.PNG)

![image](https://raw.githubusercontent.com/viktornar/composer/master/img/4.PNG)

The main technologies (frameworks, libraries) that was used in project
-------------
- Maven – For project management maven was used. Maven handles dependencies management for project and it can be used for task execution (war packaging, removing libraries in package stage, copying resources while project is building etc.). Gradle as alternative exist, but on my computer project building is much quicker with maven than Gradle. In project Maven wrapper was used. So every user who would like to quickly test application can run project even if maven and tomcat isn’t installed on computer;
- Bootstrap – For quick UI mockup Bootstrap was used. Bootsrap allows creating well looking responsive UI.  Bootstrap 3 was used as the latest stable version;
- Spring Framework – Spring framework was used for dependency injection and other service layer oriented tasks (e.g. Rest Template for communication with remote service);
- Spring Boot - for speed up development process with spring framework (embedded tomcat servlet container, hsql database etc);
- Junit for testing, BeanUtils for manipulation with Java beans (copying of properties), slf4j for logging, Lombok for syntactic sugar;
- WK<html>TOpdf for html page rendering in pdf format;

> **Note:**
> - Project was written by using Java SDK 1.8. Source was compiled without compatibility/
> - Java 1.7 and earlier versions is not supported.
> - You need to install wkhtmltopdf command line utility to render HTML into PDF

Application architecture
------------------------
In application MVC architecture was used by using Spring MVC. wkhtmltopdf was used as pdf renderer from html page converting into pdf. For atlas distinct page printing in separate threads by using external wkhtmltopdf process java ExecutorService was used. How application works is described in principle schema displayed below.
![image](https://raw.githubusercontent.com/viktornar/composer/master/img/5.PNG)

How to build and run project
-------------
Clone the project with the following command:
```bash
$ git clone https://github.com/viktornar/composer
```

Go to the project directory:
```bash
$ cd composer
```

> **Note:**
> Before run application make sure you have properly configured application. Go to [project_home]/src/main/resources/application.properties and change `atlas.folder` property according to your OS. On windows it could be atlas.folder=D:/Tmp/atlas and on Linux it could be atlas.folder=/tmp/atlas.

On Windows OS run:
```bash
mvnw.cmd spring-boot:run
```

On Linux OS run:
```bash
$ chmod u+x mvnw && ./mvnw spring-boot:run
```

Maven will download dependencies, build project and generate startup script to start web application in console. After launching startup script application will be accessible from the web browser through http://localhost:9000/composer address.