#!/bin/bash
arch=$1

set -euo pipefail
function getLatestDigest {
	arch=$1
	curl -sL https://hub.docker.com/v2/repositories/overture/dms/tags/latest | jq -r '.images[] | select(.architecture=="amd64") | .digest '
}

	
digest=$(getLatestDigest $arch)
curl -sL https://hub.docker.com/v2/repositories/overture/dms/tags \
	| jq -r -e ".results[] | select(.name!=\"latest\") | select( any(.images[]; .architecture==\"$arch\" and .digest==\"$digest\")) | .name" \
	| grep "^[0-9]\+\.[0-9]\+\.[0-9]\+$" \
	| sort -V \
	| tail -1


