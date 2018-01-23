#Accept optional username, attribute name/value, and host address arguments
if [ -n "$1" ]; then
    username="$1"
else
    username="user"
fi

if [ -n "$2" ]; then
    attributeName="$2"
else
    attributeName="foo"
fi

if [ -n "$3" ]; then
    attributeValueArg="${3}"
else
    attributeValueArg="bar"
fi
attributeValue="[\"${attributeValueArg}\"]"

if [ -n "$4" ]; then
    deploymentURL="$4"
else
    deploymentURL="http://localhost:8080"
fi

curl -v -H "Accept: application/json" \
        -H "Content-type: application/json" \
        -X PUT -d ${attributeValue} \
        ${deploymentURL}/api/attributes/${username}/${attributeName}
