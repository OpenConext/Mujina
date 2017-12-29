#Set host address or accept from argument
if [ -n "$1" ]; then
	  deploymentURL="$1"
	else
		  deploymentURL="http://localhost:8080"
	fi

curl -v -H "Accept: application/json" \
	      -H "Content-type: application/json" \
			  -X DELETE \
			  $deploymentURL/api/attributes/urn:mace:dir:attribute-def:$1
