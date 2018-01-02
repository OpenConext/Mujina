#Accept optional host address argument
if [ -n "$1" ]; then
    deploymentURL="$1"
else
    deploymentURL="http://localhost:8080"
fi

#todo this script is not working, probably something to do with a Java keystore
#todo "status":500,"error":"Internal Server Error","exception":"java.lang.RuntimeException","message":"Unable to append signing credential"

export CERT=MIICHzCCAYgCCQD7KMJ17XQa7TANBgkqhkiG9w0BAQUFADBUMQswCQYDVQQGEwJO\
	TDEQMA4GA1UECAwHVXRyZWNodDEQMA4GA1UEBwwHVXRyZWNodDEQMA4GA1UECgwH\
	U3VyZm5ldDEPMA0GA1UECwwGQ29uZXh0MB4XDTEyMDMwODA4NTQyNFoXDTEzMDMw\
	ODA4NTQyNFowVDELMAkGA1UEBhMCTkwxEDAOBgNVBAgMB1V0cmVjaHQxEDAOBgNV\
	BAcMB1V0cmVjaHQxEDAOBgNVBAoMB1N1cmZuZXQxDzANBgNVBAsMBkNvbmV4dDCB\
	nzANBgkqhkiG9w0BAQEFAAOBjQAwgYkCgYEA2slVe459WUDL4RXxJf5h5t5oUbPk\
	PlFZ9lQysSoS3fnFTdCgzA6FzQzGRDcfRj0HnWBdA1YH+LxBjNcBIJ/nBc7Ssu4e\
	4rMO3MSAV5Ouo3MaGgHqVq6dCD47f52b98df6QTAA3C+7sHqOdiQ0UDCAK0C+qP5\
	LtTcmB8QrJhKmV8CAwEAATANBgkqhkiG9w0BAQUFAAOBgQCvPhO0aSbqX7g7IkR7\
	9IFVdJ/P7uSlYFtJ9cMxec85cYLmWL1aVgF5ZFFJqC25blyPJu2GRcSxoVwB3ae8\
	sPCECWwqRQA4AHKIjiW5NgrAGYR++ssTOQR8mcAucEBfNaNdlJoy8GdZIhHZNkGl\
	yHfY8kWS3OWkGzhWSsuRCLl78A==

export KEY=MIICeAIBADANBgkqhkiG9w0BAQEFAASCAmIwggJeAgEAAoGBANrJVXuOfVlAy+EV8SX+YebeaFGz\
	5D5RWfZUMrEqEt35xU3QoMwOhc0MxkQ3H0Y9B51gXQNWB/i8QYzXASCf5wXO0rLuHuKzDtzEgFeT\
	rqNzGhoB6launQg+O3+dm/fHX+kEwANwvu7B6jnYkNFAwgCtAvqj+S7U3JgfEKyYSplfAgMBAAEC\
	gYBaPvwkyCTKYSD4Co37JxAJJCqRsQtv7SyXoCl8zKcVqwaIz4rUQRVN/Hv3/WjIFzqB3xLe4mjN\
	YBIF31YWt/6ZslaLL5YJIXISrMgDuQzPKL8VqvvsH9XEpi/qSUsVAWa9Vaqqwa8JTPELK8QhHKaX\
	TxGtatEuW1x6kSNXFCoasQJBAPUaYdj9oCDOGTaOaupF0GB6TIgIItpQESY1Dfpn4cvwB0jH8wBJ\
	SBVeBqSa6dg4RI5ydD3J82xlF7NrQnvWpYkCQQDkg26KzQckoJ39HX2gYS4olSeQDAyIDzeCMkj7\
	McDhigy0cL6k9nOQrKlq6V3vkBISTRg7JceJ4z3QE00edXWnAkEAoggv2WBJxIYbOurJmVhP2gff\
	oiomyEYYIDcAp6KXLdffKOkuJulLIv0GzTiwEMWZ5MWbPOHN78Gg+naU/AM5aQJBALfbsANpt4eW\
	28ceBUgXKMZqS+ywZRzL8YOF5gaGH4TYSCSeWiXsTUtoQN/OaFAqAQBMm2Rrn0KoXcGe5fvN0h0C\
	QQDgNLxVcByrVgmRmTPTwLhSfIveOqE6jBlQ8o0KyoQl4zCSDDtMEb9NEFxxvI7NNjgdZh1RKrzZ\
	5JCAUQcdrEQJ

curl -v -H "Accept: application/json" \
        -H "Content-type: application/json" \
        -X POST -d "{\"certificate\": \"$CERT\",\"key\":\"$KEY\"}" \
        $deploymentURL/api/signing-credential
