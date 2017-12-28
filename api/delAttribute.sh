curl -v -H "Accept: application/json" \
	      -H "Content-type: application/json" \
			  -X DELETE \
			  http://localhost:8080/api/attributes/urn:mace:dir:attribute-def:$1
