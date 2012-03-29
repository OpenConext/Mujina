<pre>
                   _         _      _   _
  /\/\   ___   ___| | _____ | | ___| |_| |__
 /    \ / _ \ / __| |/ / _ \| |/ _ \ __| '_ \
/ /\/\ \ (_) | (__|   &lt; (_) | |  __/ |_| | | |
\/    \/\___/ \___|_|\_\___/|_|\___|\__|_| |_|

    Mock Identity Provider using OpenSAML
</pre>

Mockoleth
=========

Run the IDP using jetty
-----------------------

<pre>
mvn clean install
cd mockoleth-idp
mvn jetty:run
</pre>

Then, go to https://localhost:8443/idp or http://localhost:8080/idp

Setting attribute urn:mace:dir:attribute-def:foo to bar
-------------------------------------------------------

<pre>
curl -v -H "Accept: application/json" \
        -H "Content-type: application/json" \
        -X POST -d '{"name": "urn:mace:dir:attribute-def:foo", "value": "bar"}' \
        http://localhost:8080/idp/api/set-attribute
</pre>