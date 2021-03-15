# dms
Overture Data Management System


## Developmenet
### Configure remote docker daemon control
It is possible to run the dms locally while controlling a remote docker engine.
1. Port forward the docker.sock [Forwarding the Docker Socket over SSH](https://medium.com/@dperny/forwarding-the-docker-socket-over-ssh-e6567cfab160)
   ```
   ssh -nNT -L /some/local/path/to/docker.sock:/var/run/docker.sock user@someremote
   ```
2. Run the dms with the following env variable set:
   ```
   DOCKER_HOST=unix:///some/local/path/to/docker.sock
   ```

### Test Score uploads and downloads
To test the score uploads and downloads, ego, song and score services must be running and healthy. Since the score service responds with presigned s3 urls that use the minio-api as the service name, the urls would not be resolvable outside of the defined `docker-swarm-network`. To fix this, a convienece docker image state was created in the Dockerfile (target is called `genomic-transfer-helper`) which pulls the appropriate song-client and score-client distributions and configures them with the right JWT. The following instructions will allow you to download and upload using this tool:
1. Log in to the EGO-UI service. The default local url is http://localhost:9002
2. Log out
3. By default, new users obtain the role `USER`. In order to proceed, the `ADMIN` role is needed. To do this, run the following command
```
docker exec -it $(docker service ps ego-db --no-trunc --format '{{ .Name }}.{{.ID}}' | head -1)  psql -U postgres ego -c "UPDATE egouser set type='ADMIN' where email='<the-email-you-logged-in-with-previously>' and providertype='<one of: GOOGLE,LINKEDIN,FACEBOOK,GITHUB,ORCID>';"
```
5. Log in again into EGO-UI (http://localhost:9002) and now you should be able to use the UI
6. Select `Users` in the side bar, and then select your user record. On the right most pane, click the `Edit` button at the top and then click the `+ Add` button for the `Groups` section, and add your self to the `dcc-admin` group. Then click the `Save` button at the top. This will give you the neccessary permissions to use song and score.
7. Log out
8. Start the browsers inspector tool (this will be different for each browser) and filter for XHR requests.
9. Log in to the EGO-UI. Once logged in, refer to response for the `ego-token?client_id=ego-ui` request. This is the JWT
10. Copy and paste the JWT from the previous step into the file `./jwt.txt`. 
11. Run `make start-transfer-shell`. This will automatically run the `genomic-transfer-helper` and load the contents of `jwt.txt` as the JWT to allow authorized access to song and score.
12. Once logged into the container, you can use the score and song clients. Run `./song-client/bin/sing ping` and `curl $(curl -sL http://score-api:8080/download/ping)` to do a health check.

## Gateway
The gateway is based on nginx.
the config template file is under ./nginx/path-based, there is also a docker file
tagging the gateway is done in Jenkinsfile, it will always have a new tag with the same version as the dms version.

## Tips
### Checking for OOM messages when a container/service is killed
Sometimes, if the reserved/limit memory is too low, a container will get killed by the kernel. To find out if this is the case, run
`journalctl -k | grep -i -e memory -e oom`. For java apps, the status `"task: non-zero exit (137)"` is usually the case.

