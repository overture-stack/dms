#############################
#   LATEST-VERSION-HELPER
#############################
FROM ubuntu:20.04 as latest-version-helper
RUN apt update && apt install -y jq curl
COPY ./src/main/bin/getLatestDockerTag.sh /tmp/run.sh
RUN chmod +x /tmp/run.sh
CMD ["/tmp/run.sh"]

#############################
#   Builder
#############################
FROM adoptopenjdk/openjdk11:jdk-11.0.6_10-alpine-slim as builder
WORKDIR /usr/src/app
ADD . .
RUN ./mvnw clean package -DskipTests

#############################
#   CLI
#############################
FROM adoptopenjdk/openjdk11:jre-11.0.6_10-alpine as client

ENV APP_HOME /srv
ENV APP_USER dmsadmin
ENV APP_UID 9999
ENV APP_GID 9999

COPY --from=builder /usr/src/app/target/dms-*.tar.gz $APP_HOME/dms-cli.tar.gz

RUN apk add bash bash-completion \
	&& mkdir /tmp/scratch \
	&& tar zxvf $APP_HOME/dms-cli.tar.gz -C /tmp/scratch \
	&& rm -rf $APP_HOME/dms-cli.tar.gz \
	&& mv /tmp/scratch/* /tmp/scratch/dms-cli \
	&& mv /tmp/scratch/dms-cli/* $APP_HOME \
	&& addgroup -S -g $APP_GID $APP_USER  \
    && adduser -S -u $APP_UID -G $APP_USER $APP_USER \
	&& chmod 777 -R $APP_HOME \
    && mkdir -p $APP_HOME /var/scratch/ \
    && chown -R $APP_UID:$APP_GID $APP_HOME \
	&& chmod 777 -R /var/scratch

ENV PATH $PATH:$APP_HOME/bin

WORKDIR $APP_HOME/bin

# CMD ["java", "-ea", "-jar", "/srv/dms.jar", "/var/scratch"]
