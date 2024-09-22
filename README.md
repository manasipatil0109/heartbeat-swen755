# Smart Doorbell Heartbeat Monitoring

## Overview:
This project implements the "heartbeat" tactic for monitoring a critical process. It simulates a smart doorbell system where three features—**video recording, motion detection,** and **lock control**—are monitored. The heartbeat mechanism detects the failure of any feature in real time.

## Key Features:
Non-deterministic failure simulation.
Heartbeat monitoring to detect crashes of the critical features.
Client alerts when any feature stops functioning.

## How to Run:
### Server Setup:
1. Compile and run the SmartDoorbellServer.java.
2. This will start a server that continuously monitors the status of video recording, motion detection, and lock control features.
3. To stop a feature, input commands like:
+ stop video
+ stop motion
+ stop lock
The feature will be marked as stopped, and the heartbeat message will reflect this change.

### Client Setup:
1. Compile and run the SmartDoorbellClient.java.
2. The client will connect to the server and listen for heartbeat messages.
3. The client will print the status of the features and raise an alert if any feature has stopped.

## Commands:
+ stop video – Simulate video recording feature crash.
+ stop motion – Simulate motion detection feature crash.
+ stop lock – Simulate lock control feature crash.
+ exit – Shut down the server.

## Dependencies:
+ Java 8 or later
+ No external libraries required

## Note:
+ The server and client should be run on the same machine, or the host address in SmartDoorbellClient should be updated accordingly.
+ The server sends heartbeat messages every second, which the client receives and processes in real time.

## Failure Simulation:
+ Each feature can be stopped by the user, simulating random non-deterministic failures.
+ The client detects these failures by processing the heartbeat messages and raises an alert when a failure is detected.
