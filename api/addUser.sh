curl -v -H "Accept: application/json" \
	      -H "Content-type: application/json" \
			  -X PUT -d '{"name": "test-a", "password": "welcome1", "authorities": ["ROLE_USER", "ROLE_ADMIN"]}' \
		    http://localhost:8080/api/users
