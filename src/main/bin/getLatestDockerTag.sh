#!/bin/bash
arch=${1:-${ARCH:-amd64}}
tag=${DMS_TAG:-latest}

set -euo pipefail
function getLatestDigest {
	curl -sL https://hub.docker.com/v2/repositories/overture/dms/tags/$tag | jq -r ".images[] | select(.architecture==\"$arch\") | .digest"
}

digest=$(getLatestDigest $arch)
curl -sL https://hub.docker.com/v2/repositories/overture/dms/tags \
	| jq -r -e ".results[] | select(.name!=\"latest\") | select( any(.images[]; .architecture==\"$arch\" and .digest==\"$digest\")) | .name" \
	| grep "^[0-9]\+\.[0-9]\+\.[0-9]\+$" \
	| sort -V \
	| tail -1
