#!/bin/bash
set -euo pipefail

arch=${1:-${ARCH:-amd64}}
tag=${DMS_TAG:-latest}

function getLatestDigest {
	curl -sL https://hub.docker.com/v2/repositories/overture/dms/tags/$tag |
		jq -r ".images[] | select(.architecture==\"$arch\") | .digest"
}

curl -sL https://hub.docker.com/v2/repositories/overture/dms/tags |
	jq -r -e ".results[] | select(.name!=\"latest\") | select( any(.images[]; .architecture==\"$arch\" and .digest==\"$(getLatestDigest)\")) | .name" |
	if [[ "$tag" == 'latest' ]];
		then grep "^[0-9]\+\.[0-9]\+\.[0-9]\+$";
		else cat;
	fi |
	sort -V |
	tail -1
