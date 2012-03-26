# Setting attribute urn:mace:dir:attribute-def:foo to bar
curl -v -H "Accept: application/json" \
        -H "Content-type: application/json" \
        -X POST -d '{"name": "urn:mace:dir:attribute-def:foo", "value": "bar"}' \
        http://localhost:8080/idp/api/set-attribute
