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
   7. Optionally specify an **Issuer** and a **Key ID**. If you leave the fields empty, some random values will be automatically assigned. It is IMPORTANT to remember both the Issuer and the Key ID. You will have to update the corresponding settings in the web.config later with those values later.
   8. Click Create <br>
   ![image](https://user-images.githubusercontent.com/6170297/169548503-30d14e7f-a1fa-4dc4-a70b-081ccdc0fa8f.png)

   9. In the Management Console, open the section **Web**.
   10. Clik **Create new** to create a new web integration.
   11. Enter a name in Name field, eg. JAVA App
   12. Enter http://localhost:8080 (8080 is the port your web app will run on when published, change to correct port in your setup) in Add an Origin and Click Add
   13. Click Create <br>
   ![image](https://user-images.githubusercontent.com/6170297/169548961-575c5d2e-154e-4b61-acb3-44d4b5ab27c3.png)

   14. You need the auto-generated ID from the list for later use <br>
   ![image](https://user-images.githubusercontent.com/6170297/169549360-bc64b948-dafc-4272-aa04-5635a2b96468.png)

   15. Click on Settings in the menu
   16. Make sure that "Enable dynamic assignment of professional users and/or analyzer users depending on the use case" and "Creation of groups" both are toogled on.
   ![image](https://user-images.githubusercontent.com/6170297/169549600-d4337cc6-966d-48e4-9a3d-94f799903eb0.png) ![image](https://user-images.githubusercontent.com/6170297/169549817-d530945d-92fa-4b53-b929-65e207d7f6e2.png)


5. Open the code from the git repository in your favorite IDE 
6. Navigate to the src/main/example/main.java file
7. Change the static values in the top of the main function
   1. certsPath should point to the directory where the certificates are stored
   2. issuer is the Issuer you saved when created the IDP in Qlik Sense SaaS
   3. keyID is the Key ID you saved when created the IDP in Qlik Sense SaaS
   4. QlikSaaSInstance is the SaaS instance URL (eg mytenant.eu.qlikcloud.com/)
   5. QlikIntegrationID is the ID found in the Management Console after the Webintegration form was created. You can still go back to the Management Console and click on Web to find the ID.
8. Now you should be able to build the war file and test it.
9. Navigate to the main directory of the solution, the direcorty which contains the pom.xml file, and "run mvn install" in a command prompt. Now you will find a war file in the target directory.
10. Install the war file on web server and try to run the solution. If you are using VS Code and the Tomcat extension is used, then this step can be done by right clicking the war file and select Run On Tomcat Server.
11. Navigate to the webserver in a browser.
## Explanation of the code##
The getJWT function is the one creating the signed jwt, most of the values are taken from static variables defined in the beginning of the main function. <br>
There are 4 more values you most likely will change<br>
1. claims.put("sub", "SomeSampleSeedValue1"); this will in most case be a static value identical for all users.
2. claims.put("name", "John Doe"); here the name of the user you are generating the JWT for should be specified.
3. claims.put("email", "JohnD@john.com"); here the email of the user you are generating the JWT for should be specified.
4. .withArrayClaim("groups", new String[]{"Administrators", "Sales", "Marketing"}). groups can be applied dynamically based on the access level the user needs in Qlik SaaS
