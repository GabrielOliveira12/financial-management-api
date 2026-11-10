FROM eclipse-temurin:25-jdk AS build

WORKDIR /app

RUN apt-get update \
    && apt-get install -y --no-install-recommends ca-certificates ca-certificates-java \
    && update-ca-certificates \
    && rm -rf /var/lib/apt/lists/*

ARG IMPORT_ZSCALER_CERT=false

COPY certs/ /tmp/custom-certs/

RUN if [ "$IMPORT_ZSCALER_CERT" = "true" ] && [ -f /tmp/custom-certs/zscaler-root-ca.crt ]; then \
        cp /tmp/custom-certs/zscaler-root-ca.crt /usr/local/share/ca-certificates/zscaler-root-ca.crt; \
        update-ca-certificates; \
        keytool -importcert \
            -noprompt \
            -trustcacerts \
            -alias zscaler-root-ca \
            -file /usr/local/share/ca-certificates/zscaler-root-ca.crt \
            -keystore /etc/ssl/certs/java/cacerts \
            -storepass changeit; \
    fi \
    && rm -rf /tmp/custom-certs

COPY .mvn/ .mvn/
COPY mvnw pom.xml ./

RUN chmod +x mvnw
RUN ./mvnw dependency:go-offline

COPY src ./src

RUN ./mvnw clean package -DskipTests


FROM eclipse-temurin:25-jre

WORKDIR /app

RUN apt-get update \
    && apt-get install -y --no-install-recommends ca-certificates ca-certificates-java \
    && update-ca-certificates \
    && rm -rf /var/lib/apt/lists/*

ARG IMPORT_ZSCALER_CERT=false

COPY certs/ /tmp/custom-certs/

RUN if [ "$IMPORT_ZSCALER_CERT" = "true" ] && [ -f /tmp/custom-certs/zscaler-root-ca.crt ]; then \
        cp /tmp/custom-certs/zscaler-root-ca.crt /usr/local/share/ca-certificates/zscaler-root-ca.crt; \
        update-ca-certificates; \
        keytool -importcert \
            -noprompt \
            -trustcacerts \
            -alias zscaler-root-ca \
            -file /usr/local/share/ca-certificates/zscaler-root-ca.crt \
            -keystore /etc/ssl/certs/java/cacerts \
            -storepass changeit; \
    fi \
    && rm -rf /tmp/custom-certs

RUN groupadd --system spring \
    && useradd --system --gid spring --no-create-home spring

COPY --from=build --chown=spring:spring /app/target/*.jar app.jar

USER spring

EXPOSE 8080

ENTRYPOINT ["java", "-jar", "app.jar"]
