#Accept optional argument name and host address arguments
if [ -n "$1" ]; then
    attributeName="$1"
else
    attributeName="foo"
fi

if [ -n "$2" ]; then
    deploymentURL="$2"
else
    deploymentURL="http://localhost:8080"
fi

curl -v -H "Accept: application/json" \
        -H "Content-type: application/json" \
        -X DELETE \
        ${deploymentURL}/api/attributes/${attributeName}
