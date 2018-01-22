#Accept optional Assertion Consumer Service URL and host address arguments
if [ -n "$1" ]; then
    acsUrl=$1
else
    acsUrl="http://localhost:8081/java-saml-tookit-jspsample-2.2.0/acs.jsp"
fi

if [ -n "$2" ]; then
    deploymentURL="$2"
else
    deploymentURL="http://localhost:8080"
fi

curl -v -H "Accept: application/json" \
        -H "Content-type: application/json" \
        -X PUT -d $acsUrl \
        ${deploymentURL}/api/acsendpoint
