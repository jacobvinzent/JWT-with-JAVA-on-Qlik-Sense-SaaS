# Use JWT with Qlik Sensee SaaS and JAVA#

## Introduction ##
This article describes how to use JWT in Qlik Sense SaaS using JAVA code.

## Prerequsites ##
1. JAVA is installed
2. Maven is installed and configured (https://maven.apache.org/) 
3. A web server eg. Tomcat (https://tomcat.apache.org/download-90.cgi)
4. OpenSSL is installed
5. Your preferred IDE

In this article VS Code and Tomcat is used.

## Installation ##
1. Clone this repositorty or donwload and unzip it
2. Open the directory <solutionDir>/certs in a command line
3. Run the three commands specified in the createsCerts.txt
4. After running all four commands you have four certificate files in the folder. The two "der" files are used in the JAVA code when creating the JWT. The publickey.cer needs used in a JWT IDP in Qlik Sense SaaS.
   1. Login to Qlik Sense SaaS and navigate to the Manament Console.
   2. Select Indentity Provider in the menu.
   3. Click create new.
   4. Select JWT in the Type dropdown.
   5. Copy the certificate from the publickey.cer file into the Certificate field.
   6. You can either specify an Issuer and a Key ID, if you don't enter values, some random values will be automatically assigned. It is IMPORTANT to remember both the Issuer and the Key ID.
   7. Click Create
   8. Click on Web in the menu
   9.  Create new
   10. Enter a name in Name field, eg. JAVA App
   11. Enter http://localhost:8080 (8080 is the port your web app will run on when published, change to correct port in your setup) in Add an Origin and Click Add
   12. Click Create
   13. You need the auto-generated ID from the list for later use
   14. Click on Settings in the menu
   15. Make sure that "Enable dynamic assignment of professional users" and "Creation of groups" both are toogled on.
5. Open the git repository in your favorite IDE 
6. Navigate to the src/main/example/main.java file
7. Change the static values in the top of the main function
   1. certsPath should point to the directory where the certificates are stored
   2. issuer is the Issuer you saved when created the IDP in Qlik Sense SaaS
   3. keyID is the Key ID you saved when created the IDP in Qlik Sense SaaS
   4. QlikSaaSInstance is the SaaS instance URL (eg mytenant.eu.qlikcloud.com/)
   5. QlikIntegrationID is the ID found in the Management Console after the Webintegration form was created. You can still go back to the Management Console and click on Web to find the ID.
8. Now you should be able to build the war file and test it.
9. Navigate to the main directory of the solution, the direcorty which contains the pom.xml file, and "run mvn install" Now you will find a war file in the target directory.
10. Install the war file on web server and try to run the solution. If you are using VS Code and the Tomcat extension is used, then this step can be done by right clicking the war file and select Run On Tomcat Server.
11. Navigate to the webserver in a browser.
## Explanation of the code##
The getJWT function is the one creating the signed jwt, most of the values are take from static variables defined in the beginning of the main function. <br>
There are 4 more values you most like will change<br>
1. claims.put("sub", "SomeSampleSeedValue1"); this will in most case be a static value identical for all users.
2. claims.put("name", "John Doe"); here the name of the user you are generating the JWT for should be specified.
3. claims.put("email", "JohnD@john.com"); here the email of the user you are generating the JWT for should be specified.
4. .withArrayClaim("groups", new String[]{"Administrators", "Sales", "Marketing"}). groups can be applied dynamically based on the access level the user need in Qlik SaaS
