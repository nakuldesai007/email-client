FROM maven:3.9.8-eclipse-temurin-21

ARG BW_CLI_DOWNLOAD_URL="https://vault.bitwarden.com/download/?app=cli&platform=linux"

RUN apt-get update && \
    apt-get install -y --no-install-recommends jq zsh curl unzip && \
    rm -rf /var/lib/apt/lists/*

RUN curl -fsSL "${BW_CLI_DOWNLOAD_URL}" -o /tmp/bw.zip && \
    unzip /tmp/bw.zip -d /usr/local/bin && \
    chmod +x /usr/local/bin/bw && \
    rm /tmp/bw.zip

WORKDIR /app

COPY backend/pom.xml backend/pom.xml
RUN mvn -f backend/pom.xml dependency:go-offline

COPY backend /app/backend
COPY scripts /app/scripts
COPY backend/docker-entrypoint.sh /usr/local/bin/backend-entrypoint.sh

RUN chmod +x /usr/local/bin/backend-entrypoint.sh

WORKDIR /app/backend

EXPOSE 9090

ENTRYPOINT ["backend-entrypoint.sh"]
CMD ["mvn", "spring-boot:run"]

