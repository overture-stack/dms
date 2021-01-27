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
#
################################################# 
# Genomic Transfer Helper
# A helper that contains the song and score clients
#################################################
FROM adoptopenjdk/openjdk11:jre-11.0.6_10-alpine as genomic-transfer-helper

ENV APP_HOME /srv
ENV EXAMPLE_DATA_DIR=$APP_HOME/example-data
ENV SONG_VERSION=4.4.0
ENV SCORE_VERSION=5.1.0

# Song config
ENV CLIENT_ACCESS_TOKEN=some-jwt
ENV CLIENT_STUDY_ID=ABC123
ENV CLIENT_DEBUG=false
ENV CLIENT_SERVER_URL=http://song-api:8080
ENV SONG_CLIENT_DOWNLOAD_URL=https://artifacts.oicr.on.ca/artifactory/dcc-release/bio/overture/song-client/$SONG_VERSION/song-client-$SONG_VERSION-dist.tar.gz


# Score config
ENV ACCESSTOKEN=some-jwt
ENV METADATA_URL=http://song-api:8080
ENV STORAGE_URL=http://score-api:8080
ENV SCORE_CLIENT_DOWNLOAD_URL=https://artifacts.oicr.on.ca/artifactory/dcc-release/bio/overture/score-client/$SCORE_VERSION/score-client-$SCORE_VERSION-dist.tar.gz

RUN apk add bash curl vim bash-completion \
	&& mkdir -p /tmp/scratch  $APP_HOME /var/scratch \
	&& curl -sL $SONG_CLIENT_DOWNLOAD_URL --output /tmp/song-client.tar.gz \
	&& tar zxvf /tmp/song-client.tar.gz -C /tmp/scratch \
	&& rm -rf /tmp/song-client.tar.gz \
	&& mv /tmp/scratch/* /tmp/scratch/song-client \
	&& mv /tmp/scratch/song-client $APP_HOME \
	&& chmod 777 -R /var/scratch \
	&& mkdir $EXAMPLE_DATA_DIR

COPY ./example-data/ $EXAMPLE_DATA_DIR/

RUN curl -sL $SCORE_CLIENT_DOWNLOAD_URL --output /tmp/score-client.tar.gz \
	&& tar zxvf /tmp/score-client.tar.gz -C /tmp/scratch \
	&& rm -rf /tmp/score-client.tar.gz \
	&& mv /tmp/scratch/* /tmp/scratch/score-client \
	&& mv /tmp/scratch/score-client $APP_HOME

RUN echo "alias ls='ls --color'" >> /root/.bashrc \
	&& echo "alias ll='ls -l'" >> /root/.bashrc  \
	&& echo "alias lr='ll -rt'" >> /root/.bashrc  \
	&& echo "alias l='ll'" >> /root/.bashrc 

WORKDIR $APP_HOME
