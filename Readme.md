# Use JWT with Qlik Sensee SaaS and JAVA

## Introduction ##
This article describes how to generate a JSON Web Token (JWT) in a Java web application in order to authenticate a user with Qlik Sense SaaS.

## Prerequsites ##
* Java is installed either as a Java Runtime Environment (JRE) or Java SE Development Kit (JDK). For this example the [Java 17 JDK](https://www.oracle.com/java/technologies/downloads/#jdk17-windows) has been used.
* A webserver installed that can run the Java web application. For this example [Apache Tomcat 10](https://tomcat.apache.org/download-10.cgi) has been used.
* [Apache Maven](https://maven.apache.org/) is installed and configured. This example uses a Maven POM file to build the final application that can be run by the webserver.
* Install OpenSSL on the developer machine
* You have an IDE installed to modify the Java web application. For this example [Visual Studio Code](https://code.visualstudio.com/) has been used.
* If you are are using Visual Studio Code to modify this project, it is recommended to install the following VS Code Extensions:
   * [Extension Pack for Java](https://marketplace.visualstudio.com/items?itemName=vscjava.vscode-java-pack)
   * [Maven for Java](https://marketplace.visualstudio.com/items?itemName=vscjava.vscode-maven)
   
## Installation ##
1. Use one of the options available when clicking on the "Code" button above to clone the repository. Easiest option will be to download it as a ZIP file, unzip it and open the folder that contains the pom.xml in VS Code.
2. Create a new directory where you will create the certificate files e.g. `C:\temp\certs` and in the Command Prompt change to this directory:
```
cd c:\temp\certs
```
3. Generate the signing certificates (public and private key) by running the following commands from the Command Prompt. The commands can also be found in the file `demo\certs\createCerts.txt`.
```
openssl genrsa -out privatekey.pem 4096
openssl req -new -x509 -key privatekey.pem -out publickey.cer -days 1825
openssl pkcs8 -topk8 -inform PEM -outform DER -in privatekey.pem -out private_key.der -nocrypt
openssl rsa -in privatekey.pem -pubout -outform DER -out public_key.der
```
4. After running all four commands you will have four certificate files in the certificate folder. The two `*.der` files are used in the Java code when creating the JWT. The `publickey.cer` is used in the configuration of a JWT identity privider (IdP) in Qlik Sense SaaS.
   1. Login to Qlik Sense SaaS
   2. In the Management Console, open the section **Identity provider**.
   3. Click **Create new**.
   4. Select IdP type **JWT** in the dropdown.
   5. Optionally, enter a description.
   6. Copy the content from the `publickey.cer` file into the **Certificate** field.
   7. Optionally specify an **Issuer** and a **Key ID**. If you leave the fields empty, some random values will be automatically assigned. It is IMPORTANT to remember both the Issuer and the Key ID. You will have to update the corresponding settings in the application.properties later.
   8. Click Create <br>
   ![image](https://user-images.githubusercontent.com/6170297/169548503-30d14e7f-a1fa-4dc4-a70b-081ccdc0fa8f.png)
   9. In the Management Console, open the section **Web**.
   10. Clik **Create new** to create a new web integration.
   11. Enter a value in the **Name** field, eg. `Java`.
   12. Enter `http://localhost:8080` in the **Add an Origin** field and **Click Add**. This will add you local web application to the list of trusted origins. 8080 is the default port of your web application when you run it in in Tomcat. If you are using a different port, please amend as needed.
   13. Click **Create** to finish this step. <br>
   ![image](https://user-images.githubusercontent.com/6170297/169548961-575c5d2e-154e-4b61-acb3-44d4b5ab27c3.png)
   14. Remeber the auto-generated **ID** of the list of web integrations for later use. You will have to insert it into the application.properties file of your web application.<br>
   ![image](https://user-images.githubusercontent.com/6170297/169549360-bc64b948-dafc-4272-aa04-5635a2b96468.png)
   15. In the Management Console, open the section **Settings**.
   16. Make sure that **_"Enable dynamic assignment of professional users and/or analyzer users depending on the use case"_** and **_"Creation of groups"_** are toogled on.<br>
   ![image](https://user-images.githubusercontent.com/6170297/169549600-d4337cc6-966d-48e4-9a3d-94f799903eb0.png) ![image](https://user-images.githubusercontent.com/6170297/169549817-d530945d-92fa-4b53-b929-65e207d7f6e2.png)
17. If you are using VS Code, run it and open the folder containing the `pom.xml` (should be under .\demo) from the repository that you have downloaded and unzipped in the beginning or follow similar steps in the IDE you are using. 
18. Navigate to the file `.\src\main\resources\application.properties` to configure the app to use your Qlik Cloud tenant.
19. Change the values inside the application.properties file to match your environment/configuration:
    - **certificatesPath**: Enter the path to the directory where the certificates are stored, e.g. `certificatesPath=C:\\temp\\certs\\`
    - **issuer**: Enter the value of the `Issuer` from your IdP configuration in Qlik Sense SaaS, e.g. `issuer=test`
    - **keyID**: Enter the value of the `Key ID` from your IdP configuration in Qlik Sense SaaS, e.g. `keyID=test`
    - **qlikSaaSInstance**: Enter the name of your Qlik Cloud tenant, e.g `qlikSaaSInstance=mytenant.eu.qlikcloud.com`
    - **qlikIntegrationID**: Enter the `ID` that was generated for your web integration configuration in Qlik Sense SaaS. If you forgot the ID, you can still go back to the `Web` section in the Management Console and copy it from your web integration configuration, e.g. `qlikIntegrationID=1234_1a2b3c4d5ezaLIgrRS5otABCD`.
20. Save your changes to the `application.properties`.
21. Navigate to the root directory of the solution where the `pom.xml` resides and select the **MAVEN** section in the VS Code Explorer. First run **compile** and then **package** in the Maven lifecycle. Alternatively you can also initiate the build process with Maven in the Terminal window, e.g. `run mvn package`. The output of the build process will be a `\*.WAR`file under `.\demo\target\`.<br>
![image](https://user-images.githubusercontent.com/72072893/193283548-5f5cd1f2-b6e6-4227-a706-1aba3550a746.png)
22. Copy the WAR file to the deployment directory of your webserver. If you are using Tomcat, you can copy it to the webapps directory e.g. `c:\Java.Tomcat 10.0\webapps` and Tomcat will automatically deploy the war file (if this option has been enabled). Otherwise go to the management console of Tomcat and manually deploy the war file from there.
23. Once deployed open a browser window and enter `http://localhost:8080/jwt-example` in the addressbar. If everything has been configured correctly, the Java web application will now create a JWT and automatically log you into Qlik Sense SaaS with it. 

## Explanation of the code
There are two main parts in this example:
1. The Java function getJWT in `.\src\main\java\com\example\main.java` will create a JSON Web Token and hand it over to a JavaScript function in `.\src\main\webapp\index.jsp`
2. Inside `.\src\main\webapp\index.jsp` an API call will be built with the JWT for the automatic login with Qlik Sense SaaS.

For simplicity of this example, the payload required for the creation of the JWT is hardcoded in `main.java'. In a production environment, the following 4 claims of the payload will most likely be dynamically set:
- `sub`: This will in most cases be a static value identical for all users.
- `name`: Assign the name of the user you are generating the JWT for.
- `email`: Assign the email of the user you are generating the JWT for.
- `groups`: Groups can be applied dynamically based on the access level the user needs in Qlik SaaS.
