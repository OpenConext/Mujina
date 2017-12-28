curl -v -H "Accept: application/json" \
	        -H "Content-type: application/json" \
					        -X PUT -d '["bar"]' \
									        http://localhost:8080/api/attributes/$1
