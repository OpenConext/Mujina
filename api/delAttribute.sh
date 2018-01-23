#Accept optional username, attribute name, and host address arguments
if [ -n "$1" ]; then
    username="$1"
else
    username="test-a"
fi

if [ -n "$2" ]; then
    attributeName="$2"
else
    attributeName="foo"
fi

if [ -n "$3" ]; then
    deploymentURL="$3"
else
    deploymentURL="http://localhost:8080"
fi

curl -v -H "Accept: application/json" \
        -H "Content-type: application/json" \
        -X DELETE \
        ${deploymentURL}/api/attributes/${username}/${attributeName}
