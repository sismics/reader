#!/bin/bash
docker rm -f sismics_reader_demo
docker run \
    -d --name=sismics_reader_demo --restart=always \
    -e 'VIRTUAL_HOST_SECURE=reader-demo.sismics.com' -e 'VIRTUAL_PORT=80' \
    sismics/reader_demo:latest
