#Set host address or accept from argument
if [ -n "$1" ]; then
	  deploymentURL="$1"
	else
		  deploymentURL="http://localhost:8080"
fi

curl -v -H "Accept: application/json" \
     -H "Content-type: application/json" \
     -X PUT -d '["CCR_CCRGradeA1"]' \
     $deploymentURL/api/attributes/LAA_APP_ROLES
curl -v -H "Accept: application/json" \
     -H "Content-type: application/json" \
     -X PUT -d '["0A123B"]' \
     $deploymentURL/api/attributes/LAA_ACCOUNTS
