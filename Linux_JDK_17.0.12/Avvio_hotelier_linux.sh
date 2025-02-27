#!/bin/bash


gnome-terminal -- bash -c "java -jar HOTELIERServer.jar; exec bash"

NUM_CLIENT=5

for ((i=1; i<=NUM_CLIENT; i++)); do
    gnome-terminal -- bash -c "java -jar HOTELIERClient.jar; exec bash"
done
