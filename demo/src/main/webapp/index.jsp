<%@ page import="com.example.main" %>
    <html>
    <script src="https://code.jquery.com/jquery-3.6.0.min.js"
        integrity="sha256-/xUj+3OJU5yExlq6GSYGSHk7tPXikynS7ogEvDej/m4=" crossorigin="anonymous">
        </script>
    <script>
        function getJWT() {
            var jwt = "<%=main.getJWT()%>";
            var url = "<%=main.getQlikCloudURL()%>";
            var integrationID = "<%=main.getQlikIntegrationID()%>";
            console.log("JWT: " + jwt);            

            var myHeaders = new Headers();
            myHeaders.append("qlik-web-integration-id", integrationID);
            myHeaders.append("content-type", "application/json");
            myHeaders.append("Authorization", "Bearer " + jwt);

            var raw = "";

            var requestOptions = {
                method: 'POST',
                headers: myHeaders,
                body: raw,
                credentials: "include",
                mode: "cors",
                rejectunAuthorized: false
            };
           
            fetch("https://" +  url + "/login/jwt-session?qlik-web-integration-id=" + integrationID, requestOptions)
                .then(response => response.text())
                .then(result => {
                    console.log(result);
                    window.location.replace("https://" +  url );
                })
                .catch(error => console.log('error', error));
        }

        getJWT();

    </script>

    <body>
        <!-- <h1>JWT</h1>
        <div id="jwt"></div> -->
    </body>

    </html>