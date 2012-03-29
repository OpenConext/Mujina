                   _         _      _   _
  /\/\   ___   ___| | _____ | | ___| |_| |__
 /    \ / _ \ / __| |/ / _ \| |/ _ \ __| '_ \
/ /\/\ \ (_) | (__|   < (_) | |  __/ |_| | | |
\/    \/\___/ \___|_|\_\___/|_|\___|\__|_| |_|

    Mock Identiry Provider using OpenSAML


# Run the IDP using jetty

mvn clean install
cd mockoleth-idp
mvn jetty:run

Then, go to https://localhost:8443/idp

# Setting attribute urn:mace:dir:attribute-def:foo to bar
curl -v -H "Accept: application/json" \
        -H "Content-type: application/json" \
        -X POST -d '{"name": "urn:mace:dir:attribute-def:foo", "value": "bar"}' \
        https://localhost:8080/idp/api/set-attribute
