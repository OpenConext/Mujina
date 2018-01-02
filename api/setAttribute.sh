#Accept optional attribute name/value and host address arguments
if [ -n "$1" ]; then
    attributeName="$1"
else
    attributeName="foo"
fi

if [ -n "$2" ]; then
    attributeValueArg="${2}"
else
    attributeValueArg="bar"
fi
attributeValue="[\"${attributeValueArg}\"]"

if [ -n "$3" ]; then
    deploymentURL="$3"
else
    deploymentURL="http://localhost:8080"
fi

curl -v -H "Accept: application/json" \
        -H "Content-type: application/json" \
        -X PUT -d ${attributeValue} \
        ${deploymentURL}/api/attributes/${attributeName}
