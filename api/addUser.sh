#Accept optional username/password and host address arguments
if [ -n "$1" ]; then
    username="$1"
else
    username="test-a"
fi

if [ -n "$2" ]; then
    password="$2"
else
    password="welcome1"
fi
userData="{\"name\": \"${username}\", \"password\": \"${password}\", \"authorities\": [\"ROLE_USER\", \"ROLE_ADMIN\"]}"

if [ -n "$3" ]; then
    deploymentURL="$3"
else
    deploymentURL="http://localhost:8080"
fi

curl -v -H "Accept: application/json" \
        -H "Content-type: application/json" \
        -X PUT -d "${userData}" \
        ${deploymentURL}/api/users
