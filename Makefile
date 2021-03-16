.PHONY:

# Required System files
DOCKER_EXE := $(shell which docker)
CURL_EXE := $(shell which curl)
MVN_EXE := $(shell which mvn)

# Variables
DOCKER_ORG := basharlabadi
DOCKER_REPO := dms
DOCKER_GATEWAY_REPO := dms-gateway
DOCKER_TAG := test-3
DOCKER_IMAGE_NAME := $(DOCKER_ORG)/$(DOCKER_REPO):$(DOCKER_TAG)
ROOT_DIR := $(shell dirname $(realpath $(lastword $(MAKEFILE_LIST))))
MY_UID := $$(id -u)
MY_GID := $$(id -g)
THIS_USER := $(MY_UID):$(MY_GID)
PROJECT_NAME := $(shell echo $(ROOT_DIR) | sed 's/.*\///g')
#PROJECT_VERSION := $(shell $(MVN_EXE) -f $(ROOT_DIR) help:evaluate -Dexpression=project.version -q -DforceStdout 2>&1  | tail -1)

# STDOUT Formatting
RED := $$(echo  "\033[0;31m")
YELLOW := $$(echo "\033[0;33m")
END := $$(echo  "\033[0m")
ERROR_HEADER :=  [ERROR]:
INFO_HEADER := "**************** "
DONE_MESSAGE := $(YELLOW)$(INFO_HEADER) "- done\n" $(END)

# Paths
SCRATCH_DIR := $(DOCKER_DIR)/scratch/
RETRY_CMD := $(DOCKER_DIR)/retry-command.sh


# Commands
MVN_CMD := $(MVN_EXE) -f $(ROOT_DIR)/pom.xml

#############################################################
# Internal Targets
#############################################################

#############################################################
# Help
#############################################################

# Help menu, displaying all available targets
help:
	@echo
	@echo "**************************************************************"
	@echo "**************************************************************"
	@echo "To dry-execute a target run: make -n <target> "
	@echo
	@echo "Available Targets: "
	@grep '^[A-Za-z][A-Za-z0-9_-]\+:.*' $(ROOT_DIR)/Makefile | sed 's/:.*//' | sed 's/^/\t/'
	@echo


#############################################################
#  Cleaning targets
#############################################################


#############################################################
#  Building targets
#############################################################
package:
	@mvn clean package

package-no-tests:
	@rm -rf ./target/dms-*
	@mvn package -DskipTests
	@tar zxvf ./target/dms-*-dist.tar.gz
	

#############################################################
#  Docker targets
#############################################################
build-image:
	@$(DOCKER_EXE) build -t $(DOCKER_IMAGE_NAME) ./

build-gateway:
	docker build --target insecure ./nginx/path-based -t $(DOCKER_ORG)/$(DOCKER_GATEWAY_REPO):edge --no-cache -f./nginx/path-based/Dockerfile
	docker build --target secure ./nginx/path-based -t $(DOCKER_ORG)/$(DOCKER_GATEWAY_REPO)-secure:edge --no-cache -f./nginx/path-based/Dockerfile
push-image: build-image
	@$(DOCKER_EXE) push $(DOCKER_IMAGE_NAME)

build-transfer-shell-image:
	@$(DOCKER_EXE) build --target genomic-transfer-helper -t genomic-transfer-helper:latest ./

BEARER_TOKEN := $(shell cat jwt.txt | sed 's/\s\+//g')

#NOTE: make sure the whole dms cluster is running
start-transfer-shell: build-transfer-shell-image
	@$(DOCKER_EXE) run --rm -it --network dms-swarm-network -e CLIENT_ACCESS_TOKEN=$(BEARER_TOKEN) -e ACCESSTOKEN=$(BEARER_TOKEN) genomic-transfer-helper:latest bash
