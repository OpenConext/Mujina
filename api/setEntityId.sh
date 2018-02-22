#Accept optional entity id and host address arguments
if [ -n "$1" ]; then
    entityId="$1"
else
    entityId="http://mock-idp"
fi

if [ -n "$2" ]; then
    deploymentURL="$2"
else
    deploymentURL="http://localhost:8080"
fi

curl -v -H "Accept: application/json" \
        -H "Content-type: application/json" \
        -X PUT -d ${entityId} \
        ${deploymentURL}/api/entityid
