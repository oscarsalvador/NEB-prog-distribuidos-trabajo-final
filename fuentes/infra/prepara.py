import sys
import os

class Network:
    ipbase='192.168.2.'
    ipnum=1
    puerto=10000
    nombre="npractica"
    @staticmethod
    def ippuerto():
        Network.ipnum+=1
        Network.puerto+=1
        ip=Network.ipbase+str(Network.ipnum)
        return ip, Network.puerto
    @staticmethod
    def auxpuerto():
        Network.puerto+=1
        return Network.puerto
    @staticmethod
    def instala(c):
        c.append("echo 'preparando la red'")
        c.append('docker network create --driver=bridge --subnet='+ \
            Network.ipbase+'0/24 --gateway='+ \
            Network.ipbase+'1 '+Network.nombre)
    @staticmethod
    def desinstala(c):
        c.append("echo 'eliminando la red'")
        c.append('docker network rm '+Network.nombre)

class Modelo:
    @staticmethod
    def maquina(nombre):
        ip,p=Network.ippuerto()
        return Maquina(nombre,ip,p)

class Orbd(Modelo):
    @staticmethod
    def maquina():
        maquina=Modelo.maquina("orbd")
        maquina.color="#800"
        maquina.dependencias=[]
        maquina.puerto=1050
        maquina.comando=[
            "orbd -ORBInitialPort %s -ORBInitialHost %s > /mnt/maquina.log 2>&1",
            (maquina.puerto,maquina.ip)
        ]
        maquina.descripcion=[
            "%s %s:%d",
            (maquina.nombre,maquina.ip,maquina.puerto)
        ]
        return maquina

class Samba(Modelo):
    @staticmethod
    def maquina():
        maquina=Modelo.maquina("samba")
        maquina.color="#840"
        maquina.privileged=True
        maquina.interactivo=True
        cwd=os.getcwd()
        maquina.previo=[
            "mkdir -p %s/maquinas/%s/{filtros,imagenes}" % (cwd,maquina.nombre)
        ]
        maquina.dependencias=[]
        maquina.comando=["/bin/bash",()]
        maquina.docker=maquina.docker.replace("java","elswork/samba \
-u \"1000:1000:user1:user1:user1\" \
-s \"imagenes:/mnt/imagenes:rw:user1\" \
-s \"filtros:/mnt/filtros:rw:user1\" ")
        maquina.descripcion=["%s //%s/",(maquina.nombre,maquina.ip)]
        return maquina

class AdminFiltrosA(Modelo):
    serial=0
    @staticmethod
    def maquina(centralip,centralpuerto,sambaip):
        AdminFiltrosA.serial +=1
        maquina=Modelo.maquina("adminfiltrosa%d" % AdminFiltrosA.serial)
        maquina.color="#642"
        maquina.privileged=True
        maquina.interactivo=True
        cwd=os.getcwd()
        maquina.previo=[
            "mkdir -p %s/maquinas/%s/{filtros,catalogo}" \
            % (cwd,maquina.nombre),
            "cp -r %s/bin/filtros/* %s/maquinas/%s/catalogo/" \
            % (cwd,cwd,maquina.nombre)
        ]
        maquina.dependencias=[
            "bin/servidor-a-adminfiltros.jar"
        ]
        maquina.comando=["\
cd /mnt\n\
mount -t cifs -o username=user1,password=user1,dir_mode=0755,file_mode=0755 //%s/filtros /mnt/filtros\n\
echo \"ls -lah . catalogo filtros\nmount |grep cifs\"\n\
echo \"java AdminFiltrosA /mnt/catalogo/ /mnt/filtros/ %s %s\"\n\
/bin/bash",
            (sambaip,centralpuerto,centralip)
        ]
        maquina.descripcion=[
            "%s %s",
            (maquina.nombre,maquina.ip)
        ]
        return maquina

class AdminFiltrosB(Modelo):
    serial=0
    @staticmethod
    def maquina(centralip,centralpuerto,sambaip):
        AdminFiltrosB.serial +=1
        maquina=Modelo.maquina("adminfiltrosb%d" % AdminFiltrosB.serial)
        maquina.color="#642"
        maquina.privileged=True
        maquina.interactivo=True
        cwd=os.getcwd()
        maquina.previo=[
            "mkdir -p %s/maquinas/%s/{filtros,catalogo}" \
            % (cwd,maquina.nombre),
            "cp -r %s/bin/filtros/* %s/maquinas/%s/catalogo/" \
            % (cwd,cwd,maquina.nombre)
        ]
        maquina.dependencias=[
            "bin/servidor-b-adminfiltros.jar"
        ]
        maquina.comando=["\
cd /mnt\n\
mount -t cifs -o username=user1,password=user1,dir_mode=0755,file_mode=0755 //%s/filtros /mnt/filtros\n\
echo \"ls -lah . catalogo filtros\nmount |grep cifs\"\n\
java AdminFiltrosB /mnt/catalogo/ %d %d %s 2> /mnt/maquina.log",
            (sambaip,maquina.puerto,centralpuerto,centralip)
        ]
        maquina.descripcion=[
            "%s %s:%d",
            (maquina.nombre,maquina.ip,maquina.puerto)
        ]
        return maquina

def setIpP(m,ip,p):
    m.docker=m.docker.replace(m.ip,ip)
    m.ip=ip
    m.puerto=p
    m.descripcion[1]=(m.nombre,m.ip,m.puerto,m.pSyncAut2AdminU)

class AdminUsuarios(Modelo):
    serial=0
    @staticmethod
    def maquina(autip,autpuerto,pSyncAut2AdminU):
        AdminUsuarios.serial +=1
        maquina=Modelo.maquina("adminusuarios%d" % AdminUsuarios.serial)
        maquina.color="#044"
        maquina.interactivo=True
        maquina.pSyncAut2AdminU=pSyncAut2AdminU
        maquina.dependencias=["bin/seguridad-admin.jar"
        ]
        maquina.comando=[
            "cd /mnt\njava AdminUsuarios %s %s %s",
            (autpuerto,autip,maquina.pSyncAut2AdminU)
        ]
        maquina.descripcion=[
            "%s %s: p %d, sp: %d",
            (maquina.nombre,maquina.ip,maquina.puerto,maquina.pSyncAut2AdminU)
        ]
        maquina.setIpP=setIpP
        return maquina

class Autenticacion(Modelo):
    serial=0
    @staticmethod
    def maquina(pSyncAut2AdminU,ipadminu,pSyncProxy2Aut):
        Autenticacion.serial +=1
        maquina=Modelo.maquina("autenticacion%d" % Autenticacion.serial)
        maquina.puertoeadmin=Network.auxpuerto()
        maquina.pSyncAut2AdminU=pSyncAut2AdminU
        maquina.ipadminu=ipadminu
        maquina.pSyncProxy2Aut=pSyncProxy2Aut
        maquina.color="#024"
        maquina.dependencias=["bin/seguridad-autenticacion.jar",
            "bin/BBDDUsuarios.csv"
        ]
        maquina.comando=[
            "cd /mnt\njava Autenticacion %s %s /mnt/BBDDUsuarios.csv %s %s %s > /mnt/maquina.log 2>&1",
            (maquina.puerto,maquina.puertoeadmin,pSyncAut2AdminU,ipadminu,maquina.pSyncProxy2Aut)
        ]
        maquina.descripcion=[
            "%s %s: p %d, a %d, sp: %d",
            (maquina.nombre,maquina.ip,maquina.puerto,maquina.puertoeadmin,maquina.pSyncProxy2Aut)
        ]
        return maquina

class Cliente(Modelo):
    serial=0
    @staticmethod
    def maquina(orbdip,orbdport,sambaip):
        Cliente.serial +=1
        maquina=Modelo.maquina("cliente%d" % Cliente.serial)
        maquina.color="#004"
        maquina.privileged=True
        maquina.interactivo=True
        cwd=os.getcwd()
        maquina.previo=[
            "cp -r %s/infra/gatos %s/maquinas/%s/" \
            % (cwd,cwd,maquina.nombre),
             "mkdir -p %s/maquinas/%s/imagenes" \
            % (cwd,maquina.nombre)
        ]
        maquina.dependencias=["bin/front-cliente.jar",
            "bin/peticion.jar",
            "lib/glassfish-corba-internal-api-4.2.4.jar",
            "lib/glassfish-corba-omgapi-4.2.4.jar",
            "lib/glassfish-corba-orb-4.2.4.jar",
            "lib/gmbal-4.0.2.jar",
            "lib/pfl-basic-4.1.2.jar",
            "lib/pfl-dynamic-4.1.2.jar",
            "lib/pfl-tf-4.1.2.jar",
            "lib/webservices-rt-3.0.0.jar"
        ]
        maquina.comando=[
'echo "mount |grep cifs\nmount -t cifs -o username=user1,password=user1,dir_mode=0755,file_mode=0755 //%s/imagenes /mnt/imagenes"\n\
echo "cp gatos/gatos1.jpg imagenes/"\n\
echo "java Cliente -ORBInitialPort %s -ORBInitialHost %s"\n\
echo "imagenes/gatos1.jpg\nusuario1@correo.xyz\nhash1\nacuarelas.py"\n\
cd /mnt\n\
/bin/bash',
            (sambaip,orbdport,orbdip)
        ]
        maquina.descripcion=["%s %s", (maquina.nombre,maquina.ip)]
        return maquina

class Proxy(Modelo):
    serial=0
    @staticmethod
    def maquina(orbdip,orbdport,autip,autport,syncip,syncport,centralip,centralport):
        Proxy.serial +=1
        maquina=Modelo.maquina("proxy%d" % Proxy.serial)
        maquina.color="#400"
        maquina.dependencias=["bin/front-proxy.jar",
            "bin/peticion.jar",
            "lib/glassfish-corba-internal-api-4.2.4.jar",
            "lib/glassfish-corba-omgapi-4.2.4.jar",
            "lib/glassfish-corba-orb-4.2.4.jar",
            "lib/gmbal-4.0.2.jar",
            "lib/pfl-basic-4.1.2.jar",
            "lib/pfl-dynamic-4.1.2.jar",
            "lib/pfl-tf-4.1.2.jar",
            "lib/webservices-rt-3.0.0.jar"
        ]
        maquina.comando=[
            "cd /mnt\njava Proxy %s %s %s %s %s %s -ORBInitialPort %s -ORBInitialHost %s > /mnt/maquina.log 2>&1",
            (autport,autip,syncport,syncip,centralport,centralip,orbdport,orbdip)
        ]
        return maquina

class Central(Modelo):
    serial=0
    @staticmethod
    def maquina():
        Central.serial +=1
        maquina=Modelo.maquina("central%d" % Central.serial)
        maquina.color="#046"
        maquina.pAdmin=Network().auxpuerto()
        maquina.pMServ=Network().auxpuerto()
        maquina.dependencias=["bin/servidor-utils.jar",
            "bin/servidor-central.jar"
        ]
        maquina.comando=[
            "cd /mnt\njava Central %d %d %d > /mnt/maquina.log 2>&1",
            (maquina.puerto,maquina.pAdmin,maquina.pMServ)
        ]
        maquina.descripcion=[
            "%s %s: p %d, a %d, m %d",
            (maquina.nombre,maquina.ip,maquina.puerto,maquina.pAdmin,maquina.pMServ)
        ]
        return maquina

class MultiservidorA(Modelo):
    serial=0
    @staticmethod
    def maquina(centralip,centralp):
        MultiservidorA.serial +=1
        maquina=Modelo.maquina("multiservidora%d" % MultiservidorA.serial)
        maquina.color="#042"
        maquina.pcentral=Network.auxpuerto()
        maquina.dependencias=["bin/servidor-utils.jar",
            "bin/servidor-a-multiservidor.jar"
        ]
        maquina.comando=[
            "cd /mnt\njava MultiservidorA %d %s %d %d > /mnt/maquina.log 2>&1", 
            (centralp,centralip,maquina.pcentral,maquina.puerto)
        ]
        maquina.descripcion=[
            "%s %s: c %d, a %d",
            (maquina.nombre,maquina.ip,maquina.pcentral,maquina.puerto)
        ]
        return maquina

class MultiservidorB(Modelo):
    serial=0
    @staticmethod
    def maquina(centralip,centralp):
        MultiservidorB.serial +=1
        maquina=Modelo.maquina("multiservidorb%d" % MultiservidorB.serial)
        maquina.serie=MultiservidorB.serial
        maquina.color="#042"
        maquina.pcentral=Network.auxpuerto()
        maquina.dependencias=["bin/servidor-utils.jar",
            "bin/servidor-b-multiservidor.jar"
        ]
        maquina.comando=[
            "cd /mnt\njava MultiservidorB %d %s %d %d > /mnt/maquina.log 2>&1", 
            (centralp,centralip,maquina.pcentral,maquina.puerto)
        ]
        maquina.descripcion=[
            "%s %s: c %d, a %d",
            (maquina.nombre,maquina.ip,maquina.pcentral,maquina.puerto)
        ]
        return maquina

def NodoAnilloASetDerecha(maquina, derecha): 
    maquina.ipdcha=derecha.ip 
    maquina.puertodcha=derecha.puerto 
    maquina.comando=["\
cd /mnt\n\
mount -t cifs -o username=user1,password=user1,dir_mode=0755,file_mode=0755 //%s/imagenes /mnt/imagenes\n\
mount -t cifs -o username=user1,password=user1,dir_mode=0755,file_mode=0755 //%s/filtros /mnt/filtros\n\
java NodoAnilloA %s %s %s %s > /mnt/maquina.log 2>&1",
        (maquina.sambaip,maquina.sambaip,maquina.nnodo,maquina.puerto,maquina.puertodcha,maquina.ipdcha)
    ]
    maquina.descripcion=[
        "%s %s:%d %s:%d",
        (maquina.nombre,maquina.ip,maquina.puerto,maquina.ipdcha,maquina.puertodcha)
    ]

class NodoAnilloA(Modelo):
    serial=0
    @staticmethod
    def maquina(sambaip):
        NodoAnilloA.serial +=1
        maquina=Modelo.maquina("nodoanilloa%d" % NodoAnilloA.serial)
        maquina.color="#442"
        maquina.privileged=True
        maquina.nnodo=NodoAnilloA.serial
        maquina.dependencias=["bin/servidor-utils.jar",
            "bin/servidor-a-nodoanillo.jar"
        ]
        maquina.sambaip=sambaip
        cwd=os.getcwd()
        maquina.previo=[
            "mkdir -p %s/maquinas/%s/{filtros,imagenes}" \
            % (cwd,maquina.nombre)
        ]
        maquina.docker = maquina.docker.replace("java","javapython")
        maquina.setDerecha = NodoAnilloASetDerecha
        return maquina

def NodoAnilloBSetDerecha(maquina, derecha): 
    maquina.ipdcha=derecha.ip 
    maquina.puertodcha=derecha.puerto 
    maquina.comando=["\
cd /mnt\n\
mount -t cifs -o username=user1,password=user1,dir_mode=0755,file_mode=0755 //%s/imagenes /mnt/imagenes\n\
java NodoAnilloB %s %s %s %s > /mnt/maquina.log 2>&1",
        (maquina.sambaip,maquina.nnodo,maquina.puerto,maquina.puertodcha,maquina.ipdcha)
    ]
    maquina.descripcion=[
        "%s %s:%d %s:%d",
        (maquina.nombre,maquina.ip,maquina.puerto,maquina.ipdcha,maquina.puertodcha)
    ]

class NodoAnilloB(Modelo):
    serial=0
    @staticmethod
    def maquina(sambaip):
        NodoAnilloB.serial +=1
        maquina=Modelo.maquina("nodoanillob%d" % NodoAnilloB.serial)
        maquina.color="#442"
        maquina.privileged=True
        maquina.nnodo=NodoAnilloB.serial
        maquina.dependencias=["bin/servidor-utils.jar",
            "bin/servidor-b-nodoanillo.jar"
        ]
        maquina.sambaip=sambaip
        cwd=os.getcwd()
        maquina.previo=[
            "mkdir -p %s/maquinas/%s/{filtros,imagenes}" \
            % (cwd,maquina.nombre)
        ]
        maquina.docker = maquina.docker.replace("java","javapython")
        maquina.setDerecha = NodoAnilloBSetDerecha
        return maquina

def NodoAnilloAPuenteSetDerecha(maquina,derecha):
    maquina.ipdcha=derecha.ip
    maquina.puertodcha=derecha.puerto
    maquina.comando=["\
cd /mnt\n\
mount -t cifs -o username=user1,password=user1,dir_mode=0755,file_mode=0755 //%s/imagenes /mnt/imagenes\n\
mount -t cifs -o username=user1,password=user1,dir_mode=0755,file_mode=0755 //%s/filtros /mnt/filtros\n\
java NodoAnilloA %s %s %s %s %s %s > /mnt/maquina.log 2>&1",
        (maquina.sambaip,maquina.sambaip,maquina.nnodo,maquina.puerto,maquina.puertodcha,maquina.ipdcha,maquina.puertomulti,maquina.ipmulti)
    ]
    maquina.descripcion=[
        "%s %s:%d %s:%d %s:%d",
        (maquina.nombre,maquina.ip,maquina.puerto,maquina.ipdcha,maquina.puertodcha,maquina.ipmulti,maquina.puertomulti)
    ]

class NodoAnilloAPuente(NodoAnilloA):
    @staticmethod
    def maquina(ipmulti,puertomulti,sambaip):
        maquina=NodoAnilloA.maquina(sambaip)
        maquina.ipmulti, maquina.puertomulti = ipmulti,puertomulti
        maquina.setDerecha = NodoAnilloAPuenteSetDerecha
        return maquina

def NodoAnilloBPuenteSetDerecha(maquina,derecha):
    maquina.ipdcha=derecha.ip
    maquina.puertodcha=derecha.puerto
    NodoAnilloBPuenteCompone(maquina)

def NodoAnilloBPuenteAddMulti(maquina,multi):
    maquina.multis.append(multi)
    NodoAnilloBPuenteCompone(maquina)

def NodoAnilloBPuenteCompone(maquina):
    smultis=""
    for m in maquina.multis:
        smultis="%s %d %s " % (smultis,m.puerto,m.ip)
    maquina.comando=["\
cd /mnt\n\
mount -t cifs -o username=user1,password=user1,dir_mode=0755,file_mode=0755 //%s/imagenes /mnt/imagenes\n\
java NodoAnilloB %s %s %s %s %s %s %s %s > /mnt/maquina.log 2>&1",
        (maquina.sambaip,maquina.nnodo,maquina.anillonum,maquina.puerto,maquina.puertodcha,maquina.ipdcha,maquina.admfilt.puerto,maquina.admfilt.ip,smultis)
    ]
    maquina.descripcion=[
        "%s a:%s n:%s %s:%d %s:%d [%s]",
        (maquina.nombre,maquina.anillonum,maquina.nnodo,maquina.ip,maquina.puerto,maquina.ipdcha,maquina.puertodcha,smultis)
    ]

class NodoAnilloBPuente(NodoAnilloB):
    @staticmethod
    def maquina(multiservidor,sambaip,admfilt):
        maquina=NodoAnilloB.maquina(sambaip)
        maquina.multis=[multiservidor]
        maquina.admfilt=admfilt
        maquina.anillonum = multiservidor.serie
        maquina.setDerecha = NodoAnilloBPuenteSetDerecha
        return maquina

class Maquina:
    maquinas=[]
    @staticmethod
    def instalalas(c):
        for m in Maquina.maquinas:
            m.instala(c)
        dockerKill=[m.nombre for m in Maquina.maquinas]

        c.append("\n#========================================\necho 'creando maquinas/ejecuta.sh'")
        c.append("echo '#!/bin/bash")
        c.append("xfce4-terminal --geometry 160x20 --color-bg \"#000\" -T detiene -x maquinas/detiene.sh")
        for m in Maquina.maquinas:
            c.append("sleep 0.6")
            c.append("xfce4-terminal --tab --geometry 160x20 --color-bg \"%s\" -T %s -x maquinas/%s/ejecuta.sh" % (
                m.color,m.nombre,m.nombre))
        c.append("' > maquinas/ejecuta.sh")
        c.append("chmod a+x maquinas/ejecuta.sh\n")

        c.append("\n#========================================\necho 'creando maquinas/detiene.sh'")
        c.append("echo '#!/bin/bash")
        c.append("sleep 20\ndocker ps")
        c.append("read -n 1 -s -r -p \"Pulsa cualquier tecla para terminar\"")
        c.append("docker kill %s " % ' '.join(dockerKill))
        c.append("killall tail ' > maquinas/detiene.sh")
        c.append("chmod a+x maquinas/detiene.sh\n")
    def __init__(self,nombre,ip,puerto):
        self.nombre=nombre
        self.color="#000"
        self.ip=ip
        self.puerto=puerto
        self.privileged=False
        self.interactivo=False
        self.previo=[]
        self.dependencias=[]
        self.comando=["java -jar -cp $CLASSPATH %s ", ("")]
        cwd=os.getcwd()
        self.docker="docker run --rm --network %s -h %s --name %s --ip %s -v %s/maquinas/%s:/mnt java " % (
            Network.nombre,self.nombre,self.nombre,self.ip,cwd,self.nombre)
        self.descripcion=["%s %s:%d", (self.nombre,self.ip,self.puerto)]
        Maquina.maquinas.append(self)
    def instala(self,c):
        descripcion=self.descripcion[0] % self.descripcion[1]
        c.append("\n#========================================\necho 'creando %s'" \
             % descripcion)
        c.append("mkdir maquinas/%s\n" % self.nombre)
        c.extend(self.previo)
        c.extend([
            "cp %s maquinas/%s/" % (
                f,self.nombre
            ) for f in self.dependencias
        ]+[""])
        cpl=[l.split('/')[1] for l in self.dependencias if l.endswith(".jar")]
        cp=":".join(cpl)
        c.append("#----------------------------------------\necho '#!/bin/bash")
        c.append("export CLASSPATH=%s" % (cp))
        c.append("%s' > maquinas/%s/comando.sh" % (
            self.comando[0] % self.comando[1], self.nombre))
        c.append("chmod a+x maquinas/%s/comando.sh\n" % (
            self.nombre))
        if self.privileged:
            self.docker = self.docker.replace("run --rm","run --rm --privileged")
        if self.interactivo:
            self.docker = self.docker.replace("run --rm","run --rm -i -t")
            c.append("#----------------------------------------\necho '#!/bin/bash")
            c.append("%s /mnt/comando.sh' > maquinas/%s/ejecuta.sh" % (
                self.docker,self.nombre))
        else:
            c.append("#----------------------------------------\necho '#!/bin/bash")
            c.append("%s /mnt/comando.sh &" % (
                self.docker))
            c.append("sleep 2 \ntail -f maquinas/%s/maquina.log' > maquinas/%s/ejecuta.sh" % (
                self.nombre,self.nombre))
        c.append("chmod a+x maquinas/%s/ejecuta.sh\n" % (
            self.nombre))
        return self

comandos=[]

if len(sys.argv)!=2:
    print("infra/compila.sh")
    print("python infra/prepara.py a > infra/instalaa.sh")
    print("python infra/prepara.py b > infra/instalab.sh")
    print("infra/instalaa.sh")
    print("infra/instalab.sh")
    print("maquinas/ejecuta.sh")
    exit(0)

if sys.argv[1]=='a':
    comandos.append(
'#!/bin/bash\n\
if [ ! -d bin ] || [ ! -d lib ] || [ ! -d maquinas ]; then\n\
    echo "la estructura de carpetas no es valida"\n\
    exit -1\n\
fi\n\
echo "carpeta correcta, instalando sistema A"\n\
rm -rf maquinas/*\n\n#========================================')
    Network.desinstala(comandos)
    Network.instala(comandos)
    admu1ip, admu1puerto = Network.ippuerto()
    pSyncProxy2Aut=Network.auxpuerto()
    pSyncAut2AdminU=Network.auxpuerto()


    orbd=Orbd.maquina()
    samba1=Samba.maquina()
    central=Central.maquina()
    aut1=Autenticacion.maquina(pSyncAut2AdminU,admu1ip,pSyncProxy2Aut)
    admu1=AdminUsuarios.maquina(aut1.ip,aut1.puertoeadmin,pSyncAut2AdminU)
    admu1.setIpP(admu1,admu1ip, admu1puerto)
    admf1=AdminFiltrosA.maquina(central.ip,central.pAdmin,samba1.ip)
    proxy1=Proxy.maquina(orbd.ip,orbd.puerto,aut1.ip,aut1.puerto,aut1.ip,pSyncProxy2Aut,central.ip,central.puerto)
    cliente1=Cliente.maquina(orbd.ip,orbd.puerto,samba1.ip)
    cliente2=Cliente.maquina(orbd.ip,orbd.puerto,samba1.ip)
    multiservidora1=MultiservidorA.maquina(central.ip,central.pMServ)
    nodoanilloa1=NodoAnilloA.maquina(samba1.ip)
    nodoanilloa2=NodoAnilloA.maquina(samba1.ip)
    nodoanilloa3=NodoAnilloAPuente.maquina(multiservidora1.ip,multiservidora1.puerto,samba1.ip)
    nodoanilloa3.setDerecha(nodoanilloa3,nodoanilloa1)
    nodoanilloa2.setDerecha(nodoanilloa2,nodoanilloa3)
    nodoanilloa1.setDerecha(nodoanilloa1,nodoanilloa2)

    Maquina.instalalas(comandos)

    print(*comandos, sep='\n')

if sys.argv[1]=='b':
    comandos.append(
'#!/bin/bash\n\
if [ ! -d bin ] || [ ! -d lib ] || [ ! -d maquinas ]; then\n\
    echo "la estructura de carpetas no es valida"\n\
    exit -1\n\
fi\n\
echo "carpeta correcta, instalando sistema B"\n\
rm -rf maquinas/*\n\n#========================================')
    Network.desinstala(comandos)
    Network.instala(comandos)
    admu1ip, admu1puerto = Network.ippuerto()
    pSyncProxy2Aut=Network.auxpuerto()
    pSyncAut2AdminU=Network.auxpuerto()


    orbd=Orbd.maquina()
    samba1=Samba.maquina()
    central=Central.maquina()
    aut1=Autenticacion.maquina(pSyncAut2AdminU,admu1ip,pSyncProxy2Aut)
    admu1=AdminUsuarios.maquina(aut1.ip,aut1.puertoeadmin,pSyncAut2AdminU)
    admu1.setIpP(admu1,admu1ip, admu1puerto)
    admf1=AdminFiltrosB.maquina(central.ip,central.pAdmin,samba1.ip)
    proxy1=Proxy.maquina(orbd.ip,orbd.puerto,aut1.ip,aut1.puerto,aut1.ip,pSyncProxy2Aut,central.ip,central.puerto)
    cliente1=Cliente.maquina(orbd.ip,orbd.puerto,samba1.ip)
    cliente2=Cliente.maquina(orbd.ip,orbd.puerto,samba1.ip)
    multiservidorb1=MultiservidorB.maquina(central.ip,central.pMServ)
    nodoanillob1=NodoAnilloB.maquina(samba1.ip)
    nodoanillob2=NodoAnilloB.maquina(samba1.ip)
    nodoanillob3=NodoAnilloBPuente.maquina(multiservidorb1,samba1.ip,admf1)
    nodoanillob3.setDerecha(nodoanillob3,nodoanillob1)
    nodoanillob2.setDerecha(nodoanillob2,nodoanillob3)
    nodoanillob1.setDerecha(nodoanillob1,nodoanillob2)
    multiservidorb2=MultiservidorB.maquina(central.ip,central.pMServ)
    nodoanillob4=NodoAnilloB.maquina(samba1.ip)
    nodoanillob5=NodoAnilloB.maquina(samba1.ip)
    # nodoanillob4.nnodo=1
    # nodoanillob5.nnodo=2
    # nodoanillob6.nnodo=3
    nodoanillob6=NodoAnilloBPuente.maquina(multiservidorb2,samba1.ip,admf1)
    nodoanillob6.setDerecha(nodoanillob6,nodoanillob4)
    nodoanillob5.setDerecha(nodoanillob5,nodoanillob6)
    nodoanillob4.setDerecha(nodoanillob4,nodoanillob5)
    NodoAnilloBPuenteAddMulti(nodoanillob3,multiservidorb2)
    NodoAnilloBPuenteAddMulti(nodoanillob6,multiservidorb1)

    Maquina.instalalas(comandos)

    print(*comandos, sep='\n')

