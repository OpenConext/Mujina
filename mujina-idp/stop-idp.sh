#!/usr/bin/env bash
kill `jps -l | grep "laa-saml-mock-idp" | cut -d " " -f 1`
