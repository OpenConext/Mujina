#Accept optional LAA_APP_ROLES/LAA_ACCOUNTS attributes and host address arguments
if [ -n "$1" ]; then
    laaAppRolesArg="$1"
else
    laaAppRolesArg="CCR_Solicitor,CCR_CCRDirector,CCR_CCRGradeA1,CCR_CCRGradeA2,CCR_CCRGradeA3,CCR_CCRGradeB1,CCR_CCRGradeD"
fi
laaAppRoles="[\"${laaAppRolesArg}\"]"

if [ -n "$2" ]; then
    laaAccountsArg="$2"
else
    laaAccountsArg="0A123B:0A123C"
fi
laaAccounts="[\"${laaAccountsArg}\"]"

if [ -n "$3" ]; then
    deploymentURL="$3"
else
    deploymentURL="http://localhost:8080"
fi

curl -v -H "Accept: application/json" \
        -H "Content-type: application/json" \
        -X PUT -d "${laaAppRoles}" \
        ${deploymentURL}/api/attributes/LAA_APP_ROLES

curl -v -H "Accept: application/json" \
        -H "Content-type: application/json" \
        -X PUT -d "${laaAccounts}" \
        ${deploymentURL}/api/attributes/LAA_ACCOUNTS
