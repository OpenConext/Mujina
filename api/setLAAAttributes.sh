curl -v -H "Accept: application/json" \
     -H "Content-type: application/json" \
     -X PUT -d '["CCR_CCRGradeA1"]' \
     http://localhost:8080/api/attributes/LAA_APP_ROLES
curl -v -H "Accept: application/json" \
     -H "Content-type: application/json" \
     -X PUT -d '["0A123B"]' \
     http://localhost:8080/api/attributes/LAA_ACCOUNTS
