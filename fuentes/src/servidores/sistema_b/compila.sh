#!/bin/bash
# export JAVA_HOME=/all/tools/jdk-11.0.10+9
# export PATH=$JAVA_HOME/bin:$PATH
export CLASSPATH=lib/*:bin/*

javac -cp $CLASSPATH -d target src/servidores/sistema_b/*.java

jar cf bin/servidor-b-multiservidor.jar -C target MultiservidorB.class -C target 'MultiservidorB$1.class' -C target 'MultiservidorB$AtiendeCentral.class' -C target 'MultiservidorB$Peticion.class' -C target TestigoB.class
jar cf bin/servidor-b-nodoanillo.jar -C target NodoAnilloB.class -C target 'NodoAnilloB$DetMultsrv.class' -C target TestigoB.class
jar cf bin/servidor-b-adminfiltros.jar -C target AdminFiltrosB.class -C target 'AdminFiltrosB$Anillo.class' -C target 'AdminFiltrosB$AtiendeAnillos.class' -C target 'AdminFiltrosB$EnviaCodigo.class'
cp -r src/servidores/filtros bin/