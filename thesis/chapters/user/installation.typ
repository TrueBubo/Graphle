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

3. Run the application by executing the startup script from the top-level project directory.

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
The `port` value must match the port exposed by `GraphleManager`.
The client connects to `localhost` on that port; remote backends are therefore reached through the SSH port forwarding setup described below.
The `localhost` flag describes whether the backend and GUI run on the same machine, which affects how files are opened.
When it is set to `false`, opened files are downloaded through `/download` before they are handed to the local operating system.

=== Remote access over SSH port forwarding <admin-ssh-port-forwarding>

For remote use, the recommended security model is not to expose the `GraphleManager` HTTP port directly to the network.
Instead, the administrator starts `GraphleManager`, Neo4j, and Valkey on the remote machine and lets SSH provide the authenticated transport from the user's machine.
With the default backend port, the user creates the tunnel from the local machine with:

```bash
ssh -N -L 5824:127.0.0.1:5824 user@remote-host
```

The first `5824` is the port opened on the local machine.
The second `5824` is the `GraphleManager` port on the remote machine.
After the tunnel is established, `GraphleUI` still connects to `localhost:5824`, but the traffic is carried by SSH to the remote backend.

For this setup, the GUI configuration should keep the forwarded local port and mark the backend as remote:

```yaml
server:
  port: 5824
  localhost: false
```

The `localhost: false` setting is important because paths returned by the remote backend are meaningful on the remote machine, not on the machine running the GUI.
When the user opens a file, Graphle therefore downloads a local copy instead of attempting to open the remote path directly.

Another installation option is to create a native desktop package for `GraphleUI`.
This can be done from the UI directory with the following task:

```bash
cd GraphleUI
./gradlew packageDistributionForCurrentOS
```

The generated package installs only the desktop client.
It does not replace the backend, Neo4j, or Valkey deployment, so those services still need to be started with the commands described above.
