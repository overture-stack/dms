FROM nginx:1.19.7 as insecure
COPY ./default.conf.template /etc/nginx/templates/default.conf.template


FROM nginx:1.19.7 as secure
COPY ./default.secure.conf.template /etc/nginx/templates/default.conf.template