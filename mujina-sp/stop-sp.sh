#!/usr/bin/env bash
kill `jps -l | grep "laa-saml-mock-sp" | cut -d " " -f 1`
