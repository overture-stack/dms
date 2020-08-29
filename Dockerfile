#############################
#   Builder
#############################
FROM adoptopenjdk/openjdk11:jdk-11.0.6_10-alpine-slim as builder
WORKDIR /usr/src/app
ADD . .
RUN ./mvnw clean package -DskipTests

#############################
#   Client
#############################
FROM adoptopenjdk/openjdk11:jre-11.0.6_10-alpine as client

ENV APP_HOME /srv
ENV APP_USER dmsadmin
ENV APP_UID 9999
ENV APP_GID 9999


COPY --from=builder /usr/src/app/target/dms-*exec.jar $APP_HOME/dms.jar
#VOLUME /var/scratch

RUN addgroup -S -g $APP_GID $APP_USER  \
    && adduser -S -u $APP_UID -G $APP_USER $APP_USER \
	&& chmod 777 -R $APP_HOME \
    && mkdir -p $APP_HOME /var/scratch/ \
    && chown -R $APP_UID:$APP_GID $APP_HOME \
	&& chmod 777 -R /var/scratch

WORKDIR $APP_HOME

USER $APP_UID

CMD ["java", "-ea", "-jar", "/srv/dms.jar", "/var/scratch"]
