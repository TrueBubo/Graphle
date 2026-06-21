#import "../../template/shared.typ": *
== Installation and Deployment

Graphle is distributed as source code and is intended to be run as a small group of cooperating services.
The recommended deployment path uses the provided `start.sh` script, which starts all required parts from the top-level project directory.
The script ensures the user does not have to manually start each component in the correct order.

For proper deployment of the application, ensure that the following components are installed in the operating system:

- *JDK 21 or newer* - Required for running both `GraphleManager` and `GraphleUI`.
- *Docker* - Required for the Neo4j and Valkey containers.
- *Docker Compose* - Either the `docker compose` plugin or the older `docker-compose` command can be used.
- *Access to source code* - The source code can be obtained by cloning the GitHub repository with `git clone`.

Initializing the installation with the recommended script consists of the following steps:

1. Move to the project directory, where the `start.sh` script is located.

```bash
cd Graphle
```

2. Make the startup script executable.

```bash
chmod +x start.sh
```

3. Run the application - Execute the startup script from the top-level project directory.

```bash
./start.sh
```

The script starts Neo4j and Valkey from `GraphleManager/compose.yaml`, waits until both services accept connections,
starts the `GraphleManager` backend with `./gradlew bootRun`, and finally starts the `GraphleUI` desktop client with `./gradlew run`.
When the script is interrupted, for example by pressing `Ctrl+C`, it stops the UI and backend processes and shuts down the Docker Compose services.

After a successful start, the desktop application opens on the screen.

The same deployment can also be performed manually. This is useful during development or when one of the components is already running.
The manual procedure is:

1. Start the data services from the backend directory.

```bash
cd GraphleManager
docker compose up -d
```

This starts Neo4j on ports `7687` for the Bolt protocol used by `GraphleManager` and `7474` for the HTTP interface used mainly by Neo4j Browser, and Valkey on port `6379`.
Both services store their data in Docker volumes, so the graph database and autocomplete cache survive container restarts.

2. Start the backend service.

```bash
./gradlew bootRun
```

The backend reads its configuration from `GraphleManager/src/main/resources/application.properties`.
By default, it connects to Neo4j at `bolt://localhost:7687`, Valkey at `localhost:6379`, and exposes the application server on port `5824`.

3. Start the desktop client in a second terminal.

```bash
cd GraphleUI
./gradlew run
```

The client reads its backend configuration from `GraphleUI/src/main/resources/config.yaml`.
If the backend is deployed on a different host or port, the server configuration must be changed there as well.

Another installation option is to create a native desktop package for `GraphleUI`.
This can be done from the UI directory with the following task:

```bash
cd GraphleUI
./gradlew packageDistributionForCurrentOS
```

The generated package installs only the desktop client.
It does not replace the backend, Neo4j, or Valkey deployment, so those services still need to be started with the commands described above.
