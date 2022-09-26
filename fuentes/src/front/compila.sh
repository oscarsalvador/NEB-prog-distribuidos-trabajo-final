#!/bin/bash
pushd src/front
idlj -fall Peticion.idl
popd

src/front/cliente/compila.sh
src/front/proxy/compila.sh