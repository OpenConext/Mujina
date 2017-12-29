#Set host address or accept from argument
if [ -n "$1" ]; then
	  deploymentURL="$1"
	else
		  deploymentURL="http://localhost:8080"
fi

curl -v -H "Accept: application/json" \
        -H "Content-type: application/json" \
        -X PUT -d "http://localhost:8081/java-saml-tookit-jspsample-2.2.0/acs.jsp" \
        $deploymentURL/api/acsendpoint
