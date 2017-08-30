This requires a newer version of Maven than Ubuntu is shipping.

To get the new version of mvn, follow the instructions on this blog
to add a backport repo, purge mvn and install the new version of mvn.

https://launchpad.net/~andrei-pozolotin/+archive/ubuntu/maven3

Then, verify that your installed version is >= 3.3.5

To run the service provider on a host that is not the localhost
(like a VM on EC2), you'll need to update this file and
replace the instances of localhost with that hosts IP address:
mujina-sp/src/main/resources/application.yml

To generate a new private signing key and public cert:
openssl req -subj '/O=cerebrodata, CN=cerebrodata.com/' -newkey rsa:2048 -new -x509 -days 3652 -nodes -out cerebro.sasl.crt -keyout cerebro.sasl.pem

Java KeyStore expects privates keys to be in pkcs8 DER format, so do this:
openssl pkcs8 -nocrypt  -in cerebro.sasl.pem -topk8 -out cerebro.sasl.der

Strip header, footer and whitespace from crt and dir files
cat cerebro.sasl.der |head -n -1 |tail -n +2 | tr -d '\n' > cerebro.sasl.der.stripped
mv cerebro.sasl.der.stripped cerebro.sasl.der
cat cerebro.sasl.crt |head -n -1 |tail -n +2 | tr -d '\n' > cerebro.sasl.crt.stripped
mv cerebro.sasl.crt.stripped cerebro.sasl.crt


NOTE: You will need to update the base_uri setting in the mujina-idp/src/main/resources/application.yaml 
and mujina-sp/src/main/resources/application.yaml files
