#!/bin/bash
# export JAVA_HOME=/all/tools/jdk-11.0.10+9
# export PATH=$JAVA_HOME/bin:$PATH
export CLASSPATH=lib/*:bin/*

javac -cp $CLASSPATH -d target src/servidores/sistema_a/*.java

jar cf bin/servidor-a-multiservidor.jar -C target MultiservidorA.class -C target 'MultiservidorA$1.class' -C target 'MultiservidorA$AtiendeCentral.class' -C target 'MultiservidorA$Peticion.class' -C target TestigoA.class
jar cf bin/servidor-a-nodoanillo.jar -C target NodoAnilloA.class -C target TestigoA.class
jar cf bin/servidor-a-adminfiltros.jar -C target AdminFiltrosA.class -C target TestigoA.class
cp -r src/servidores/filtros bin/