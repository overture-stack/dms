#!/bin/bash

docker run \
	--rm \
	-it \
	--name="dms" \
	-v /var/run/docker.sock:/var/run/docker.sock \
	-v /usr/bin/docker:/usr/bin/docker \
	dms-wrapper:latest dms $@

