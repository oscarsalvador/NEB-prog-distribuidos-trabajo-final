#!/bin/bash
if [ ! -d bin ] || [ ! -d lib ] || [ ! -d maquinas ]; then
    echo "la estructura de carpetas no es valida"
    exit -1
fi
echo "carpeta correcta, instalando sistema A"
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
echo 'creando adminfiltrosa1 192.168.2.8'
mkdir maquinas/adminfiltrosa1

mkdir -p /run/media/user/Compartido/Z/documentos/academico/carrera_3/cuatrimestre_2/distribuidos/practicas/trabajo_final/distribuidos-final/fuentes/maquinas/adminfiltrosa1/{filtros,catalogo}
cp -r /run/media/user/Compartido/Z/documentos/academico/carrera_3/cuatrimestre_2/distribuidos/practicas/trabajo_final/distribuidos-final/fuentes/bin/filtros/* /run/media/user/Compartido/Z/documentos/academico/carrera_3/cuatrimestre_2/distribuidos/practicas/trabajo_final/distribuidos-final/fuentes/maquinas/adminfiltrosa1/catalogo/
cp bin/servidor-a-adminfiltros.jar maquinas/adminfiltrosa1/

#----------------------------------------
echo '#!/bin/bash
export CLASSPATH=servidor-a-adminfiltros.jar
cd /mnt
mount -t cifs -o username=user1,password=user1,dir_mode=0755,file_mode=0755 //192.168.2.4/filtros /mnt/filtros
echo "ls -lah . catalogo filtros
mount |grep cifs"
echo "java AdminFiltrosA /mnt/catalogo/ /mnt/filtros/ 10007 192.168.2.5"
/bin/bash' > maquinas/adminfiltrosa1/comando.sh
chmod a+x maquinas/adminfiltrosa1/comando.sh

#----------------------------------------
echo '#!/bin/bash
docker run --rm -i -t --privileged --network npractica -h adminfiltrosa1 --name adminfiltrosa1 --ip 192.168.2.8 -v /run/media/user/Compartido/Z/documentos/academico/carrera_3/cuatrimestre_2/distribuidos/practicas/trabajo_final/distribuidos-final/fuentes/maquinas/adminfiltrosa1:/mnt java  /mnt/comando.sh' > maquinas/adminfiltrosa1/ejecuta.sh
chmod a+x maquinas/adminfiltrosa1/ejecuta.sh


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
echo 'creando multiservidora1 192.168.2.12: c 10017, a 10016'
mkdir maquinas/multiservidora1

cp bin/servidor-utils.jar maquinas/multiservidora1/
cp bin/servidor-a-multiservidor.jar maquinas/multiservidora1/

#----------------------------------------
echo '#!/bin/bash
export CLASSPATH=servidor-utils.jar:servidor-a-multiservidor.jar
cd /mnt
java MultiservidorA 10008 192.168.2.5 10017 10016 > /mnt/maquina.log 2>&1' > maquinas/multiservidora1/comando.sh
chmod a+x maquinas/multiservidora1/comando.sh

#----------------------------------------
echo '#!/bin/bash
docker run --rm --network npractica -h multiservidora1 --name multiservidora1 --ip 192.168.2.12 -v /run/media/user/Compartido/Z/documentos/academico/carrera_3/cuatrimestre_2/distribuidos/practicas/trabajo_final/distribuidos-final/fuentes/maquinas/multiservidora1:/mnt java  /mnt/comando.sh &
sleep 2 
tail -f maquinas/multiservidora1/maquina.log' > maquinas/multiservidora1/ejecuta.sh
chmod a+x maquinas/multiservidora1/ejecuta.sh


#========================================
echo 'creando nodoanilloa1 192.168.2.13:10018 192.168.2.14:10019'
mkdir maquinas/nodoanilloa1

mkdir -p /run/media/user/Compartido/Z/documentos/academico/carrera_3/cuatrimestre_2/distribuidos/practicas/trabajo_final/distribuidos-final/fuentes/maquinas/nodoanilloa1/{filtros,imagenes}
cp bin/servidor-utils.jar maquinas/nodoanilloa1/
cp bin/servidor-a-nodoanillo.jar maquinas/nodoanilloa1/

#----------------------------------------
echo '#!/bin/bash
export CLASSPATH=servidor-utils.jar:servidor-a-nodoanillo.jar
cd /mnt
mount -t cifs -o username=user1,password=user1,dir_mode=0755,file_mode=0755 //192.168.2.4/imagenes /mnt/imagenes
mount -t cifs -o username=user1,password=user1,dir_mode=0755,file_mode=0755 //192.168.2.4/filtros /mnt/filtros
java NodoAnilloA 1 10018 10019 192.168.2.14 > /mnt/maquina.log 2>&1' > maquinas/nodoanilloa1/comando.sh
chmod a+x maquinas/nodoanilloa1/comando.sh

#----------------------------------------
echo '#!/bin/bash
docker run --rm --privileged --network npractica -h nodoanilloa1 --name nodoanilloa1 --ip 192.168.2.13 -v /run/media/user/Compartido/Z/documentos/academico/carrera_3/cuatrimestre_2/distribuidos/practicas/trabajo_final/distribuidos-final/fuentes/maquinas/nodoanilloa1:/mnt javapython  /mnt/comando.sh &
sleep 2 
tail -f maquinas/nodoanilloa1/maquina.log' > maquinas/nodoanilloa1/ejecuta.sh
chmod a+x maquinas/nodoanilloa1/ejecuta.sh


#========================================
echo 'creando nodoanilloa2 192.168.2.14:10019 192.168.2.15:10020'
mkdir maquinas/nodoanilloa2

mkdir -p /run/media/user/Compartido/Z/documentos/academico/carrera_3/cuatrimestre_2/distribuidos/practicas/trabajo_final/distribuidos-final/fuentes/maquinas/nodoanilloa2/{filtros,imagenes}
cp bin/servidor-utils.jar maquinas/nodoanilloa2/
cp bin/servidor-a-nodoanillo.jar maquinas/nodoanilloa2/

#----------------------------------------
echo '#!/bin/bash
export CLASSPATH=servidor-utils.jar:servidor-a-nodoanillo.jar
cd /mnt
mount -t cifs -o username=user1,password=user1,dir_mode=0755,file_mode=0755 //192.168.2.4/imagenes /mnt/imagenes
mount -t cifs -o username=user1,password=user1,dir_mode=0755,file_mode=0755 //192.168.2.4/filtros /mnt/filtros
java NodoAnilloA 2 10019 10020 192.168.2.15 > /mnt/maquina.log 2>&1' > maquinas/nodoanilloa2/comando.sh
chmod a+x maquinas/nodoanilloa2/comando.sh

#----------------------------------------
echo '#!/bin/bash
docker run --rm --privileged --network npractica -h nodoanilloa2 --name nodoanilloa2 --ip 192.168.2.14 -v /run/media/user/Compartido/Z/documentos/academico/carrera_3/cuatrimestre_2/distribuidos/practicas/trabajo_final/distribuidos-final/fuentes/maquinas/nodoanilloa2:/mnt javapython  /mnt/comando.sh &
sleep 2 
tail -f maquinas/nodoanilloa2/maquina.log' > maquinas/nodoanilloa2/ejecuta.sh
chmod a+x maquinas/nodoanilloa2/ejecuta.sh


#========================================
echo 'creando nodoanilloa3 192.168.2.15:10020 192.168.2.13:10018 192.168.2.12:10016'
mkdir maquinas/nodoanilloa3

mkdir -p /run/media/user/Compartido/Z/documentos/academico/carrera_3/cuatrimestre_2/distribuidos/practicas/trabajo_final/distribuidos-final/fuentes/maquinas/nodoanilloa3/{filtros,imagenes}
cp bin/servidor-utils.jar maquinas/nodoanilloa3/
cp bin/servidor-a-nodoanillo.jar maquinas/nodoanilloa3/

#----------------------------------------
echo '#!/bin/bash
export CLASSPATH=servidor-utils.jar:servidor-a-nodoanillo.jar
cd /mnt
mount -t cifs -o username=user1,password=user1,dir_mode=0755,file_mode=0755 //192.168.2.4/imagenes /mnt/imagenes
mount -t cifs -o username=user1,password=user1,dir_mode=0755,file_mode=0755 //192.168.2.4/filtros /mnt/filtros
java NodoAnilloA 3 10020 10018 192.168.2.13 10016 192.168.2.12 > /mnt/maquina.log 2>&1' > maquinas/nodoanilloa3/comando.sh
chmod a+x maquinas/nodoanilloa3/comando.sh

#----------------------------------------
echo '#!/bin/bash
docker run --rm --privileged --network npractica -h nodoanilloa3 --name nodoanilloa3 --ip 192.168.2.15 -v /run/media/user/Compartido/Z/documentos/academico/carrera_3/cuatrimestre_2/distribuidos/practicas/trabajo_final/distribuidos-final/fuentes/maquinas/nodoanilloa3:/mnt javapython  /mnt/comando.sh &
sleep 2 
tail -f maquinas/nodoanilloa3/maquina.log' > maquinas/nodoanilloa3/ejecuta.sh
chmod a+x maquinas/nodoanilloa3/ejecuta.sh


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
xfce4-terminal --tab --geometry 160x20 --color-bg "#642" -T adminfiltrosa1 -x maquinas/adminfiltrosa1/ejecuta.sh
sleep 0.6
xfce4-terminal --tab --geometry 160x20 --color-bg "#400" -T proxy1 -x maquinas/proxy1/ejecuta.sh
sleep 0.6
xfce4-terminal --tab --geometry 160x20 --color-bg "#004" -T cliente1 -x maquinas/cliente1/ejecuta.sh
sleep 0.6
xfce4-terminal --tab --geometry 160x20 --color-bg "#004" -T cliente2 -x maquinas/cliente2/ejecuta.sh
sleep 0.6
xfce4-terminal --tab --geometry 160x20 --color-bg "#042" -T multiservidora1 -x maquinas/multiservidora1/ejecuta.sh
sleep 0.6
xfce4-terminal --tab --geometry 160x20 --color-bg "#442" -T nodoanilloa1 -x maquinas/nodoanilloa1/ejecuta.sh
sleep 0.6
xfce4-terminal --tab --geometry 160x20 --color-bg "#442" -T nodoanilloa2 -x maquinas/nodoanilloa2/ejecuta.sh
sleep 0.6
xfce4-terminal --tab --geometry 160x20 --color-bg "#442" -T nodoanilloa3 -x maquinas/nodoanilloa3/ejecuta.sh
' > maquinas/ejecuta.sh
chmod a+x maquinas/ejecuta.sh


#========================================
echo 'creando maquinas/detiene.sh'
echo '#!/bin/bash
sleep 20
docker ps
read -n 1 -s -r -p "Pulsa cualquier tecla para terminar"
docker kill orbd samba central1 autenticacion1 adminusuarios1 adminfiltrosa1 proxy1 cliente1 cliente2 multiservidora1 nodoanilloa1 nodoanilloa2 nodoanilloa3 
killall tail ' > maquinas/detiene.sh
chmod a+x maquinas/detiene.sh

