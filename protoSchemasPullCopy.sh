#!/bin/bash
git submodule add git@github.com:Cerebellum-Network/ddc-schemas.git
git submodule init
git submodule update

mkdir proto/src/main/proto
cp ddc-schemas/storage/protobuf/* proto/src/main/proto