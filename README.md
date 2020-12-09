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

## Tips
### Checking for OOM messages when a container/service is killed
Sometimes, if the reserved/limit memory is too low, a container will get killed by the kernel. To find out if this is the case, run
`journalctl -k | grep -i -e memory -e oom`. For java apps, the status `"task: non-zero exit (137)"` is usually the case.

