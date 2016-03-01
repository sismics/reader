#!/bin/bash
docker rm -f sismics_reader
docker run \
    -d --name=sismics_reader --restart=always \
    --link sismics_reader_hsqldb:sismics_reader_hsqldb \
    -v sismics_reader_data:/data \
    -e 'VIRTUAL_HOST_SECURE=reader.sismics.com' -e 'VIRTUAL_PORT=80' \
    sismics/reader:latest
