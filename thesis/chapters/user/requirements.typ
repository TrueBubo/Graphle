#import "../../template/shared.typ": *
== Hardware and Software Requirements

The hardware and software configuration described below is necessary for Graphle to function optimally.
The application is split into four runtime parts: `GraphleManager`, `GraphleUI`, Neo4j, and Valkey.
The first two are JVM applications, while Neo4j and Valkey are started by the Docker Compose configuration in `GraphleManager`.
The requirements therefore cover both the desktop application resources and the resources consumed by the two background data services.

Hardware requirements:

- *CPU* - The application can put a noticeable load on the processor while actively running.
  The backend, the desktop UI, Neo4j, and Valkey run as separate processes, so the work can be spread across several cores.
  - Suggestions: A multi-core processor is recommended to run graph queries and all background tasks at the same time without sharing a single core with the GUI.

- *Memory* - Minimum 8 GB RAM.
  This is sufficient to run all application parts - the JVM backend, the Compose desktop client, the Neo4j container, and the Valkey container, together with a small or medium-sized file collection.
  Recommended is 16 GB RAM.
  This amount keeps everything running smoothly, because both Neo4j and Valkey keep their data in memory for fast access, and Graphle does the same for the most frequently used autocomplete entries.
  Bigger file collections therefore need more memory, even though Graphle only keeps information about the files, not the files themselves.

- *Disk space* - The source code itself is small, only a few tens of megabytes.
  Much more space is taken up by the downloaded dependencies, the Docker images for Neo4j and Valkey, and the stored data, which grows as more files are added.
  - Suggestions: At least 10 GB of free disk space should be reserved for installation and basic use, and 20 GB is recommended to leave room for the stored data to grow.
    Extra space is also used when the application opens files from a remote `GraphleManager`, because a copy is downloaded first.

- *Internet connection* - Local use does not require an internet connection after installation, but a stable connection with reasonable response times is required when `GraphleUI` connects to a remote `GraphleManager` instance.

Software requirements:

- *Operating system* - The application is intended to run on macOS and Linux.

- *Docker and Docker Compose* - Docker containerization is utilized for the deployment path employed in the next section,
  so Docker Engine and Docker Compose must be installed. The backend repository contains a `compose.yaml` file that starts Neo4j and Valkey and creates persistent Docker volumes for both services.

- *File permissions* - `GraphleManager` must run under an operating system account that has permission to read and modify the files it manages.
