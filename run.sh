docker build -t dms ./
docker_group_id=$(stat -c "%g"  /var/run/docker.sock)
docker run \
	-it \
	--name="dms" \
	-e "DOCKER_MODE=true" \
	-v /var/run/docker.sock:/var/run/docker.sock \
	-v /usr/bin/docker:/usr/bin/docker \
	-u "$(id -u):$docker_group_id" \
	dms:latest \
	sh
