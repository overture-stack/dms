#!/bin/bash

function getLatestVersion {
	echo "1.0.0"
}

function genLatestConfig {
	version=$(getLatestVersion)
	echo "version: $version" > /tmp/$config_filename
	echo "/tmp/$config_filename"
}

function provisionVolume {
	volume_name="dms-config"
	config_filename="config.yaml"
	container_id=$(docker container create -v $volume_name:/temp-volume hello-world)
	$(docker cp $container_id:/temp-volume/$config_filename $config_filename)
	if [ $? -eq 0 ]; then
		echo "The file \"$config_filename\" exists"
	else
		echo "The file \"$config_filename\" DOES NOT exist, creating it..."
		new_config_filename=$(genLatestConfig)
		docker cp $new_config_filename $container_id:/temp-volume
	fi
	docker rm $container_id
}

function main {
	repo_name="overture/dms"
	volume_name="dms-config"
	config_filename="config.yaml"
	container_id=$(docker container create -v $volume_name:/temp-volume hello-world)
	$(docker cp $container_id:/temp-volume/$config_filename $config_filename)
	if [ $? -ne 0 ]; then
		echo "The file \"$config_filename\" DOES NOT exist, creating it..."
		new_config_filename=$(genLatestConfig)
		mv $new_config_filename $config_filename
		docker cp $config_filename $container_id:/temp-volume
	fi

	read_version=$(cat $config_filename | grep version | sed 's/:\s\+//')
	image_id=$(docker images -q $repo_name:$read_version)
	if [ "$image_id" == "" ]; then
		docker pull $repo_name:$read_version
	fi
	docker rm $container_id

	docker run --rm -it -e "DOCKER_MODE=true" $repo_name:$read_version  dms $@
}

main


