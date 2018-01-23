#Accept optional host address argument
if [ -n "$1" ]; then
    deploymentURL="$1"
else
    deploymentURL="http://localhost:8080"
fi

curl -v -H "Accept: application/json" \
        -H "Content-type: application/json" \
        -X POST \
        ${deploymentURL}/api/reset
