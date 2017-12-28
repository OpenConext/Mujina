curl -v -H "Accept: application/json" \
        -H "Content-type: application/json" \
        -X PUT -d "http://localhost:8081/java-saml-tookit-jspsample-2.2.0/acs.jsp" \
        http://localhost:8080/api/acsendpoint
