#!/bin/bash
if [ ! -d bin ] || [ ! -d lib ] || [ ! -d maquinas ]; then
    echo "la estructura de carpetas no es valida"
    exit -1
fi
echo "carpeta correcta, instalando sistema B"
rm -rf maquinas/*

#========================================
echo 'eliminando la red'
docker network rm npractica
echo 'preparando la red'
docker network create --driver=bridge --subnet=192.168.2.0/24 --gateway=192.168.2.1 npractica

#========================================
echo 'creando orbd 192.168.2.3:1050'
mkdir maquinas/orbd


#----------------------------------------
echo '#!/bin/bash
export CLASSPATH=
orbd -ORBInitialPort 1050 -ORBInitialHost 192.168.2.3 > /mnt/maquina.log 2>&1' > maquinas/orbd/comando.sh
chmod a+x maquinas/orbd/comando.sh

#----------------------------------------
echo '#!/bin/bash
docker run --rm --network npractica -h orbd --name orbd --ip 192.168.2.3 -v /run/media/user/Compartido/Z/documentos/academico/carrera_3/cuatrimestre_2/distribuidos/practicas/trabajo_final/distribuidos-final/fuentes/maquinas/orbd:/mnt java  /mnt/comando.sh &
sleep 2 
tail -f maquinas/orbd/maquina.log' > maquinas/orbd/ejecuta.sh
chmod a+x maquinas/orbd/ejecuta.sh


#========================================
echo 'creando samba //192.168.2.4/'
mkdir maquinas/samba

mkdir -p /run/media/user/Compartido/Z/documentos/academico/carrera_3/cuatrimestre_2/distribuidos/practicas/trabajo_final/distribuidos-final/fuentes/maquinas/samba/{filtros,imagenes}

#----------------------------------------
echo '#!/bin/bash
export CLASSPATH=
/bin/bash' > maquinas/samba/comando.sh
chmod a+x maquinas/samba/comando.sh

#----------------------------------------
echo '#!/bin/bash
docker run --rm -i -t --privileged --network npractica -h samba --name samba --ip 192.168.2.4 -v /run/media/user/Compartido/Z/documentos/academico/carrera_3/cuatrimestre_2/distribuidos/practicas/trabajo_final/distribuidos-final/fuentes/maquinas/samba:/mnt elswork/samba -u "1000:1000:user1:user1:user1" -s "imagenes:/mnt/imagenes:rw:user1" -s "filtros:/mnt/filtros:rw:user1"   /mnt/comando.sh' > maquinas/samba/ejecuta.sh
chmod a+x maquinas/samba/ejecuta.sh


#========================================
echo 'creando central1 192.168.2.5: p 10006, a 10007, m 10008'
mkdir maquinas/central1

cp bin/servidor-utils.jar maquinas/central1/
cp bin/servidor-central.jar maquinas/central1/

#----------------------------------------
echo '#!/bin/bash
export CLASSPATH=servidor-utils.jar:servidor-central.jar
cd /mnt
java Central 10006 10007 10008 > /mnt/maquina.log 2>&1' > maquinas/central1/comando.sh
chmod a+x maquinas/central1/comando.sh

#----------------------------------------
echo '#!/bin/bash
docker run --rm --network npractica -h central1 --name central1 --ip 192.168.2.5 -v /run/media/user/Compartido/Z/documentos/academico/carrera_3/cuatrimestre_2/distribuidos/practicas/trabajo_final/distribuidos-final/fuentes/maquinas/central1:/mnt java  /mnt/comando.sh &
sleep 2 
tail -f maquinas/central1/maquina.log' > maquinas/central1/ejecuta.sh
chmod a+x maquinas/central1/ejecuta.sh


#========================================
echo 'creando autenticacion1 192.168.2.6: p 10009, a 10010, sp: 10002'
mkdir maquinas/autenticacion1

cp bin/seguridad-autenticacion.jar maquinas/autenticacion1/
cp bin/BBDDUsuarios.csv maquinas/autenticacion1/

#----------------------------------------
echo '#!/bin/bash
export CLASSPATH=seguridad-autenticacion.jar
cd /mnt
java Autenticacion 10009 10010 /mnt/BBDDUsuarios.csv 10003 192.168.2.2 10002 > /mnt/maquina.log 2>&1' > maquinas/autenticacion1/comando.sh
chmod a+x maquinas/autenticacion1/comando.sh

#----------------------------------------
echo '#!/bin/bash
docker run --rm --network npractica -h autenticacion1 --name autenticacion1 --ip 192.168.2.6 -v /run/media/user/Compartido/Z/documentos/academico/carrera_3/cuatrimestre_2/distribuidos/practicas/trabajo_final/distribuidos-final/fuentes/maquinas/autenticacion1:/mnt java  /mnt/comando.sh &
sleep 2 
tail -f maquinas/autenticacion1/maquina.log' > maquinas/autenticacion1/ejecuta.sh
chmod a+x maquinas/autenticacion1/ejecuta.sh


#========================================
echo 'creando adminusuarios1 192.168.2.2: p 10001, sp: 10003'
mkdir maquinas/adminusuarios1

cp bin/seguridad-admin.jar maquinas/adminusuarios1/

#----------------------------------------
echo '#!/bin/bash
export CLASSPATH=seguridad-admin.jar
cd /mnt
java AdminUsuarios 10010 192.168.2.6 10003' > maquinas/adminusuarios1/comando.sh
chmod a+x maquinas/adminusuarios1/comando.sh

#----------------------------------------
echo '#!/bin/bash
docker run --rm -i -t --network npractica -h adminusuarios1 --name adminusuarios1 --ip 192.168.2.2 -v /run/media/user/Compartido/Z/documentos/academico/carrera_3/cuatrimestre_2/distribuidos/practicas/trabajo_final/distribuidos-final/fuentes/maquinas/adminusuarios1:/mnt java  /mnt/comando.sh' > maquinas/adminusuarios1/ejecuta.sh
chmod a+x maquinas/adminusuarios1/ejecuta.sh


#========================================
echo 'creando adminfiltrosb1 192.168.2.8:10012'
mkdir maquinas/adminfiltrosb1

mkdir -p /run/media/user/Compartido/Z/documentos/academico/carrera_3/cuatrimestre_2/distribuidos/practicas/trabajo_final/distribuidos-final/fuentes/maquinas/adminfiltrosb1/{filtros,catalogo}
cp -r /run/media/user/Compartido/Z/documentos/academico/carrera_3/cuatrimestre_2/distribuidos/practicas/trabajo_final/distribuidos-final/fuentes/bin/filtros/* /run/media/user/Compartido/Z/documentos/academico/carrera_3/cuatrimestre_2/distribuidos/practicas/trabajo_final/distribuidos-final/fuentes/maquinas/adminfiltrosb1/catalogo/
cp bin/servidor-b-adminfiltros.jar maquinas/adminfiltrosb1/

#----------------------------------------
echo '#!/bin/bash
export CLASSPATH=servidor-b-adminfiltros.jar
cd /mnt
mount -t cifs -o username=user1,password=user1,dir_mode=0755,file_mode=0755 //192.168.2.4/filtros /mnt/filtros
echo "ls -lah . catalogo filtros
mount |grep cifs"
java AdminFiltrosB /mnt/catalogo/ 10012 10007 192.168.2.5 2> /mnt/maquina.log' > maquinas/adminfiltrosb1/comando.sh
chmod a+x maquinas/adminfiltrosb1/comando.sh

#----------------------------------------
echo '#!/bin/bash
docker run --rm -i -t --privileged --network npractica -h adminfiltrosb1 --name adminfiltrosb1 --ip 192.168.2.8 -v /run/media/user/Compartido/Z/documentos/academico/carrera_3/cuatrimestre_2/distribuidos/practicas/trabajo_final/distribuidos-final/fuentes/maquinas/adminfiltrosb1:/mnt java  /mnt/comando.sh' > maquinas/adminfiltrosb1/ejecuta.sh
chmod a+x maquinas/adminfiltrosb1/ejecuta.sh


#========================================
echo 'creando proxy1 192.168.2.9:10013'
mkdir maquinas/proxy1

cp bin/front-proxy.jar maquinas/proxy1/
cp bin/peticion.jar maquinas/proxy1/
cp lib/glassfish-corba-internal-api-4.2.4.jar maquinas/proxy1/
cp lib/glassfish-corba-omgapi-4.2.4.jar maquinas/proxy1/
cp lib/glassfish-corba-orb-4.2.4.jar maquinas/proxy1/
cp lib/gmbal-4.0.2.jar maquinas/proxy1/
cp lib/pfl-basic-4.1.2.jar maquinas/proxy1/
cp lib/pfl-dynamic-4.1.2.jar maquinas/proxy1/
cp lib/pfl-tf-4.1.2.jar maquinas/proxy1/
cp lib/webservices-rt-3.0.0.jar maquinas/proxy1/

#----------------------------------------
echo '#!/bin/bash
export CLASSPATH=front-proxy.jar:peticion.jar:glassfish-corba-internal-api-4.2.4.jar:glassfish-corba-omgapi-4.2.4.jar:glassfish-corba-orb-4.2.4.jar:gmbal-4.0.2.jar:pfl-basic-4.1.2.jar:pfl-dynamic-4.1.2.jar:pfl-tf-4.1.2.jar:webservices-rt-3.0.0.jar
cd /mnt
java Proxy 10009 192.168.2.6 10002 192.168.2.6 10006 192.168.2.5 -ORBInitialPort 1050 -ORBInitialHost 192.168.2.3 > /mnt/maquina.log 2>&1' > maquinas/proxy1/comando.sh
chmod a+x maquinas/proxy1/comando.sh

#----------------------------------------
echo '#!/bin/bash
docker run --rm --network npractica -h proxy1 --name proxy1 --ip 192.168.2.9 -v /run/media/user/Compartido/Z/documentos/academico/carrera_3/cuatrimestre_2/distribuidos/practicas/trabajo_final/distribuidos-final/fuentes/maquinas/proxy1:/mnt java  /mnt/comando.sh &
sleep 2 
tail -f maquinas/proxy1/maquina.log' > maquinas/proxy1/ejecuta.sh
chmod a+x maquinas/proxy1/ejecuta.sh


#========================================
echo 'creando cliente1 192.168.2.10'
mkdir maquinas/cliente1

cp -r /run/media/user/Compartido/Z/documentos/academico/carrera_3/cuatrimestre_2/distribuidos/practicas/trabajo_final/distribuidos-final/fuentes/infra/gatos /run/media/user/Compartido/Z/documentos/academico/carrera_3/cuatrimestre_2/distribuidos/practicas/trabajo_final/distribuidos-final/fuentes/maquinas/cliente1/
mkdir -p /run/media/user/Compartido/Z/documentos/academico/carrera_3/cuatrimestre_2/distribuidos/practicas/trabajo_final/distribuidos-final/fuentes/maquinas/cliente1/imagenes
cp bin/front-cliente.jar maquinas/cliente1/
cp bin/peticion.jar maquinas/cliente1/
cp lib/glassfish-corba-internal-api-4.2.4.jar maquinas/cliente1/
cp lib/glassfish-corba-omgapi-4.2.4.jar maquinas/cliente1/
cp lib/glassfish-corba-orb-4.2.4.jar maquinas/cliente1/
cp lib/gmbal-4.0.2.jar maquinas/cliente1/
cp lib/pfl-basic-4.1.2.jar maquinas/cliente1/
cp lib/pfl-dynamic-4.1.2.jar maquinas/cliente1/
cp lib/pfl-tf-4.1.2.jar maquinas/cliente1/
cp lib/webservices-rt-3.0.0.jar maquinas/cliente1/

#----------------------------------------
echo '#!/bin/bash
export CLASSPATH=front-cliente.jar:peticion.jar:glassfish-corba-internal-api-4.2.4.jar:glassfish-corba-omgapi-4.2.4.jar:glassfish-corba-orb-4.2.4.jar:gmbal-4.0.2.jar:pfl-basic-4.1.2.jar:pfl-dynamic-4.1.2.jar:pfl-tf-4.1.2.jar:webservices-rt-3.0.0.jar
echo "mount |grep cifs
mount -t cifs -o username=user1,password=user1,dir_mode=0755,file_mode=0755 //192.168.2.4/imagenes /mnt/imagenes"
echo "cp gatos/gatos1.jpg imagenes/"
echo "java Cliente -ORBInitialPort 1050 -ORBInitialHost 192.168.2.3"
echo "imagenes/gatos1.jpg
usuario1@correo.xyz
hash1
acuarelas.py"
cd /mnt
/bin/bash' > maquinas/cliente1/comando.sh
chmod a+x maquinas/cliente1/comando.sh

#----------------------------------------
echo '#!/bin/bash
docker run --rm -i -t --privileged --network npractica -h cliente1 --name cliente1 --ip 192.168.2.10 -v /run/media/user/Compartido/Z/documentos/academico/carrera_3/cuatrimestre_2/distribuidos/practicas/trabajo_final/distribuidos-final/fuentes/maquinas/cliente1:/mnt java  /mnt/comando.sh' > maquinas/cliente1/ejecuta.sh
chmod a+x maquinas/cliente1/ejecuta.sh


#========================================
echo 'creando cliente2 192.168.2.11'
mkdir maquinas/cliente2

cp -r /run/media/user/Compartido/Z/documentos/academico/carrera_3/cuatrimestre_2/distribuidos/practicas/trabajo_final/distribuidos-final/fuentes/infra/gatos /run/media/user/Compartido/Z/documentos/academico/carrera_3/cuatrimestre_2/distribuidos/practicas/trabajo_final/distribuidos-final/fuentes/maquinas/cliente2/
mkdir -p /run/media/user/Compartido/Z/documentos/academico/carrera_3/cuatrimestre_2/distribuidos/practicas/trabajo_final/distribuidos-final/fuentes/maquinas/cliente2/imagenes
cp bin/front-cliente.jar maquinas/cliente2/
cp bin/peticion.jar maquinas/cliente2/
cp lib/glassfish-corba-internal-api-4.2.4.jar maquinas/cliente2/
cp lib/glassfish-corba-omgapi-4.2.4.jar maquinas/cliente2/
cp lib/glassfish-corba-orb-4.2.4.jar maquinas/cliente2/
cp lib/gmbal-4.0.2.jar maquinas/cliente2/
cp lib/pfl-basic-4.1.2.jar maquinas/cliente2/
cp lib/pfl-dynamic-4.1.2.jar maquinas/cliente2/
cp lib/pfl-tf-4.1.2.jar maquinas/cliente2/
cp lib/webservices-rt-3.0.0.jar maquinas/cliente2/

#----------------------------------------
echo '#!/bin/bash
export CLASSPATH=front-cliente.jar:peticion.jar:glassfish-corba-internal-api-4.2.4.jar:glassfish-corba-omgapi-4.2.4.jar:glassfish-corba-orb-4.2.4.jar:gmbal-4.0.2.jar:pfl-basic-4.1.2.jar:pfl-dynamic-4.1.2.jar:pfl-tf-4.1.2.jar:webservices-rt-3.0.0.jar
echo "mount |grep cifs
mount -t cifs -o username=user1,password=user1,dir_mode=0755,file_mode=0755 //192.168.2.4/imagenes /mnt/imagenes"
echo "cp gatos/gatos1.jpg imagenes/"
echo "java Cliente -ORBInitialPort 1050 -ORBInitialHost 192.168.2.3"
echo "imagenes/gatos1.jpg
usuario1@correo.xyz
hash1
acuarelas.py"
cd /mnt
/bin/bash' > maquinas/cliente2/comando.sh
chmod a+x maquinas/cliente2/comando.sh

#----------------------------------------
echo '#!/bin/bash
docker run --rm -i -t --privileged --network npractica -h cliente2 --name cliente2 --ip 192.168.2.11 -v /run/media/user/Compartido/Z/documentos/academico/carrera_3/cuatrimestre_2/distribuidos/practicas/trabajo_final/distribuidos-final/fuentes/maquinas/cliente2:/mnt java  /mnt/comando.sh' > maquinas/cliente2/ejecuta.sh
chmod a+x maquinas/cliente2/ejecuta.sh


#========================================
echo 'creando multiservidorb1 192.168.2.12: c 10017, a 10016'
mkdir maquinas/multiservidorb1

cp bin/servidor-utils.jar maquinas/multiservidorb1/
cp bin/servidor-b-multiservidor.jar maquinas/multiservidorb1/

#----------------------------------------
echo '#!/bin/bash
export CLASSPATH=servidor-utils.jar:servidor-b-multiservidor.jar
cd /mnt
java MultiservidorB 10008 192.168.2.5 10017 10016 > /mnt/maquina.log 2>&1' > maquinas/multiservidorb1/comando.sh
chmod a+x maquinas/multiservidorb1/comando.sh

#----------------------------------------
echo '#!/bin/bash
docker run --rm --network npractica -h multiservidorb1 --name multiservidorb1 --ip 192.168.2.12 -v /run/media/user/Compartido/Z/documentos/academico/carrera_3/cuatrimestre_2/distribuidos/practicas/trabajo_final/distribuidos-final/fuentes/maquinas/multiservidorb1:/mnt java  /mnt/comando.sh &
sleep 2 
tail -f maquinas/multiservidorb1/maquina.log' > maquinas/multiservidorb1/ejecuta.sh
chmod a+x maquinas/multiservidorb1/ejecuta.sh


#========================================
echo 'creando nodoanillob1 192.168.2.13:10018 192.168.2.14:10019'
mkdir maquinas/nodoanillob1

mkdir -p /run/media/user/Compartido/Z/documentos/academico/carrera_3/cuatrimestre_2/distribuidos/practicas/trabajo_final/distribuidos-final/fuentes/maquinas/nodoanillob1/{filtros,imagenes}
cp bin/servidor-utils.jar maquinas/nodoanillob1/
cp bin/servidor-b-nodoanillo.jar maquinas/nodoanillob1/

#----------------------------------------
echo '#!/bin/bash
export CLASSPATH=servidor-utils.jar:servidor-b-nodoanillo.jar
cd /mnt
mount -t cifs -o username=user1,password=user1,dir_mode=0755,file_mode=0755 //192.168.2.4/imagenes /mnt/imagenes
java NodoAnilloB 1 10018 10019 192.168.2.14 > /mnt/maquina.log 2>&1' > maquinas/nodoanillob1/comando.sh
chmod a+x maquinas/nodoanillob1/comando.sh

#----------------------------------------
echo '#!/bin/bash
docker run --rm --privileged --network npractica -h nodoanillob1 --name nodoanillob1 --ip 192.168.2.13 -v /run/media/user/Compartido/Z/documentos/academico/carrera_3/cuatrimestre_2/distribuidos/practicas/trabajo_final/distribuidos-final/fuentes/maquinas/nodoanillob1:/mnt javapython  /mnt/comando.sh &
sleep 2 
tail -f maquinas/nodoanillob1/maquina.log' > maquinas/nodoanillob1/ejecuta.sh
chmod a+x maquinas/nodoanillob1/ejecuta.sh


#========================================
echo 'creando nodoanillob2 192.168.2.14:10019 192.168.2.15:10020'
mkdir maquinas/nodoanillob2

mkdir -p /run/media/user/Compartido/Z/documentos/academico/carrera_3/cuatrimestre_2/distribuidos/practicas/trabajo_final/distribuidos-final/fuentes/maquinas/nodoanillob2/{filtros,imagenes}
cp bin/servidor-utils.jar maquinas/nodoanillob2/
cp bin/servidor-b-nodoanillo.jar maquinas/nodoanillob2/

#----------------------------------------
echo '#!/bin/bash
export CLASSPATH=servidor-utils.jar:servidor-b-nodoanillo.jar
cd /mnt
mount -t cifs -o username=user1,password=user1,dir_mode=0755,file_mode=0755 //192.168.2.4/imagenes /mnt/imagenes
java NodoAnilloB 2 10019 10020 192.168.2.15 > /mnt/maquina.log 2>&1' > maquinas/nodoanillob2/comando.sh
chmod a+x maquinas/nodoanillob2/comando.sh

#----------------------------------------
echo '#!/bin/bash
docker run --rm --privileged --network npractica -h nodoanillob2 --name nodoanillob2 --ip 192.168.2.14 -v /run/media/user/Compartido/Z/documentos/academico/carrera_3/cuatrimestre_2/distribuidos/practicas/trabajo_final/distribuidos-final/fuentes/maquinas/nodoanillob2:/mnt javapython  /mnt/comando.sh &
sleep 2 
tail -f maquinas/nodoanillob2/maquina.log' > maquinas/nodoanillob2/ejecuta.sh
chmod a+x maquinas/nodoanillob2/ejecuta.sh


#========================================
echo 'creando nodoanillob3 a:1 n:3 192.168.2.15:10020 192.168.2.13:10018 [ 10016 192.168.2.12  10021 192.168.2.16 ]'
mkdir maquinas/nodoanillob3

mkdir -p /run/media/user/Compartido/Z/documentos/academico/carrera_3/cuatrimestre_2/distribuidos/practicas/trabajo_final/distribuidos-final/fuentes/maquinas/nodoanillob3/{filtros,imagenes}
cp bin/servidor-utils.jar maquinas/nodoanillob3/
cp bin/servidor-b-nodoanillo.jar maquinas/nodoanillob3/

#----------------------------------------
echo '#!/bin/bash
export CLASSPATH=servidor-utils.jar:servidor-b-nodoanillo.jar
cd /mnt
mount -t cifs -o username=user1,password=user1,dir_mode=0755,file_mode=0755 //192.168.2.4/imagenes /mnt/imagenes
java NodoAnilloB 3 1 10020 10018 192.168.2.13 10012 192.168.2.8  10016 192.168.2.12  10021 192.168.2.16  > /mnt/maquina.log 2>&1' > maquinas/nodoanillob3/comando.sh
chmod a+x maquinas/nodoanillob3/comando.sh

#----------------------------------------
echo '#!/bin/bash
docker run --rm --privileged --network npractica -h nodoanillob3 --name nodoanillob3 --ip 192.168.2.15 -v /run/media/user/Compartido/Z/documentos/academico/carrera_3/cuatrimestre_2/distribuidos/practicas/trabajo_final/distribuidos-final/fuentes/maquinas/nodoanillob3:/mnt javapython  /mnt/comando.sh &
sleep 2 
tail -f maquinas/nodoanillob3/maquina.log' > maquinas/nodoanillob3/ejecuta.sh
chmod a+x maquinas/nodoanillob3/ejecuta.sh


#========================================
echo 'creando multiservidorb2 192.168.2.16: c 10022, a 10021'
mkdir maquinas/multiservidorb2

cp bin/servidor-utils.jar maquinas/multiservidorb2/
cp bin/servidor-b-multiservidor.jar maquinas/multiservidorb2/

#----------------------------------------
echo '#!/bin/bash
export CLASSPATH=servidor-utils.jar:servidor-b-multiservidor.jar
cd /mnt
java MultiservidorB 10008 192.168.2.5 10022 10021 > /mnt/maquina.log 2>&1' > maquinas/multiservidorb2/comando.sh
chmod a+x maquinas/multiservidorb2/comando.sh

#----------------------------------------
echo '#!/bin/bash
docker run --rm --network npractica -h multiservidorb2 --name multiservidorb2 --ip 192.168.2.16 -v /run/media/user/Compartido/Z/documentos/academico/carrera_3/cuatrimestre_2/distribuidos/practicas/trabajo_final/distribuidos-final/fuentes/maquinas/multiservidorb2:/mnt java  /mnt/comando.sh &
sleep 2 
tail -f maquinas/multiservidorb2/maquina.log' > maquinas/multiservidorb2/ejecuta.sh
chmod a+x maquinas/multiservidorb2/ejecuta.sh


#========================================
echo 'creando nodoanillob4 192.168.2.17:10023 192.168.2.18:10024'
mkdir maquinas/nodoanillob4

mkdir -p /run/media/user/Compartido/Z/documentos/academico/carrera_3/cuatrimestre_2/distribuidos/practicas/trabajo_final/distribuidos-final/fuentes/maquinas/nodoanillob4/{filtros,imagenes}
cp bin/servidor-utils.jar maquinas/nodoanillob4/
cp bin/servidor-b-nodoanillo.jar maquinas/nodoanillob4/

#----------------------------------------
echo '#!/bin/bash
export CLASSPATH=servidor-utils.jar:servidor-b-nodoanillo.jar
cd /mnt
mount -t cifs -o username=user1,password=user1,dir_mode=0755,file_mode=0755 //192.168.2.4/imagenes /mnt/imagenes
java NodoAnilloB 4 10023 10024 192.168.2.18 > /mnt/maquina.log 2>&1' > maquinas/nodoanillob4/comando.sh
chmod a+x maquinas/nodoanillob4/comando.sh

#----------------------------------------
echo '#!/bin/bash
docker run --rm --privileged --network npractica -h nodoanillob4 --name nodoanillob4 --ip 192.168.2.17 -v /run/media/user/Compartido/Z/documentos/academico/carrera_3/cuatrimestre_2/distribuidos/practicas/trabajo_final/distribuidos-final/fuentes/maquinas/nodoanillob4:/mnt javapython  /mnt/comando.sh &
sleep 2 
tail -f maquinas/nodoanillob4/maquina.log' > maquinas/nodoanillob4/ejecuta.sh
chmod a+x maquinas/nodoanillob4/ejecuta.sh


#========================================
echo 'creando nodoanillob5 192.168.2.18:10024 192.168.2.19:10025'
mkdir maquinas/nodoanillob5

mkdir -p /run/media/user/Compartido/Z/documentos/academico/carrera_3/cuatrimestre_2/distribuidos/practicas/trabajo_final/distribuidos-final/fuentes/maquinas/nodoanillob5/{filtros,imagenes}
cp bin/servidor-utils.jar maquinas/nodoanillob5/
cp bin/servidor-b-nodoanillo.jar maquinas/nodoanillob5/

#----------------------------------------
echo '#!/bin/bash
export CLASSPATH=servidor-utils.jar:servidor-b-nodoanillo.jar
cd /mnt
mount -t cifs -o username=user1,password=user1,dir_mode=0755,file_mode=0755 //192.168.2.4/imagenes /mnt/imagenes
java NodoAnilloB 5 10024 10025 192.168.2.19 > /mnt/maquina.log 2>&1' > maquinas/nodoanillob5/comando.sh
chmod a+x maquinas/nodoanillob5/comando.sh

#----------------------------------------
echo '#!/bin/bash
docker run --rm --privileged --network npractica -h nodoanillob5 --name nodoanillob5 --ip 192.168.2.18 -v /run/media/user/Compartido/Z/documentos/academico/carrera_3/cuatrimestre_2/distribuidos/practicas/trabajo_final/distribuidos-final/fuentes/maquinas/nodoanillob5:/mnt javapython  /mnt/comando.sh &
sleep 2 
tail -f maquinas/nodoanillob5/maquina.log' > maquinas/nodoanillob5/ejecuta.sh
chmod a+x maquinas/nodoanillob5/ejecuta.sh


#========================================
echo 'creando nodoanillob6 a:2 n:6 192.168.2.19:10025 192.168.2.17:10023 [ 10021 192.168.2.16  10016 192.168.2.12 ]'
mkdir maquinas/nodoanillob6

mkdir -p /run/media/user/Compartido/Z/documentos/academico/carrera_3/cuatrimestre_2/distribuidos/practicas/trabajo_final/distribuidos-final/fuentes/maquinas/nodoanillob6/{filtros,imagenes}
cp bin/servidor-utils.jar maquinas/nodoanillob6/
cp bin/servidor-b-nodoanillo.jar maquinas/nodoanillob6/

#----------------------------------------
echo '#!/bin/bash
export CLASSPATH=servidor-utils.jar:servidor-b-nodoanillo.jar
cd /mnt
mount -t cifs -o username=user1,password=user1,dir_mode=0755,file_mode=0755 //192.168.2.4/imagenes /mnt/imagenes
java NodoAnilloB 6 2 10025 10023 192.168.2.17 10012 192.168.2.8  10021 192.168.2.16  10016 192.168.2.12  > /mnt/maquina.log 2>&1' > maquinas/nodoanillob6/comando.sh
chmod a+x maquinas/nodoanillob6/comando.sh

#----------------------------------------
echo '#!/bin/bash
docker run --rm --privileged --network npractica -h nodoanillob6 --name nodoanillob6 --ip 192.168.2.19 -v /run/media/user/Compartido/Z/documentos/academico/carrera_3/cuatrimestre_2/distribuidos/practicas/trabajo_final/distribuidos-final/fuentes/maquinas/nodoanillob6:/mnt javapython  /mnt/comando.sh &
sleep 2 
tail -f maquinas/nodoanillob6/maquina.log' > maquinas/nodoanillob6/ejecuta.sh
chmod a+x maquinas/nodoanillob6/ejecuta.sh


#========================================
echo 'creando maquinas/ejecuta.sh'
echo '#!/bin/bash
xfce4-terminal --geometry 160x20 --color-bg "#000" -T detiene -x maquinas/detiene.sh
sleep 0.6
xfce4-terminal --tab --geometry 160x20 --color-bg "#800" -T orbd -x maquinas/orbd/ejecuta.sh
sleep 0.6
xfce4-terminal --tab --geometry 160x20 --color-bg "#840" -T samba -x maquinas/samba/ejecuta.sh
sleep 0.6
xfce4-terminal --tab --geometry 160x20 --color-bg "#046" -T central1 -x maquinas/central1/ejecuta.sh
sleep 0.6
xfce4-terminal --tab --geometry 160x20 --color-bg "#024" -T autenticacion1 -x maquinas/autenticacion1/ejecuta.sh
sleep 0.6
xfce4-terminal --tab --geometry 160x20 --color-bg "#044" -T adminusuarios1 -x maquinas/adminusuarios1/ejecuta.sh
sleep 0.6
xfce4-terminal --tab --geometry 160x20 --color-bg "#642" -T adminfiltrosb1 -x maquinas/adminfiltrosb1/ejecuta.sh
sleep 0.6
xfce4-terminal --tab --geometry 160x20 --color-bg "#400" -T proxy1 -x maquinas/proxy1/ejecuta.sh
sleep 0.6
xfce4-terminal --tab --geometry 160x20 --color-bg "#004" -T cliente1 -x maquinas/cliente1/ejecuta.sh
sleep 0.6
xfce4-terminal --tab --geometry 160x20 --color-bg "#004" -T cliente2 -x maquinas/cliente2/ejecuta.sh
sleep 0.6
xfce4-terminal --tab --geometry 160x20 --color-bg "#042" -T multiservidorb1 -x maquinas/multiservidorb1/ejecuta.sh
sleep 0.6
xfce4-terminal --tab --geometry 160x20 --color-bg "#442" -T nodoanillob1 -x maquinas/nodoanillob1/ejecuta.sh
sleep 0.6
xfce4-terminal --tab --geometry 160x20 --color-bg "#442" -T nodoanillob2 -x maquinas/nodoanillob2/ejecuta.sh
sleep 0.6
xfce4-terminal --tab --geometry 160x20 --color-bg "#442" -T nodoanillob3 -x maquinas/nodoanillob3/ejecuta.sh
sleep 0.6
xfce4-terminal --tab --geometry 160x20 --color-bg "#042" -T multiservidorb2 -x maquinas/multiservidorb2/ejecuta.sh
sleep 0.6
xfce4-terminal --tab --geometry 160x20 --color-bg "#442" -T nodoanillob4 -x maquinas/nodoanillob4/ejecuta.sh
sleep 0.6
xfce4-terminal --tab --geometry 160x20 --color-bg "#442" -T nodoanillob5 -x maquinas/nodoanillob5/ejecuta.sh
sleep 0.6
xfce4-terminal --tab --geometry 160x20 --color-bg "#442" -T nodoanillob6 -x maquinas/nodoanillob6/ejecuta.sh
' > maquinas/ejecuta.sh
chmod a+x maquinas/ejecuta.sh


#========================================
echo 'creando maquinas/detiene.sh'
echo '#!/bin/bash
sleep 20
docker ps
read -n 1 -s -r -p "Pulsa cualquier tecla para terminar"
docker kill orbd samba central1 autenticacion1 adminusuarios1 adminfiltrosb1 proxy1 cliente1 cliente2 multiservidorb1 nodoanillob1 nodoanillob2 nodoanillob3 multiservidorb2 nodoanillob4 nodoanillob5 nodoanillob6 
killall tail ' > maquinas/detiene.sh
chmod a+x maquinas/detiene.sh

