#!/bin/bash
dms_exe=/usr/bin/dms
echo "#!/bin/bash" > $dms_exe
#echo 'docker run --rm -it -u $(id -u):$(id -g) rob dms $@' >> $dms_exe
echo 'docker run --rm -it rtisma1/dms:latest dms $@' >> $dms_exe
chmod +x $dms_exe
