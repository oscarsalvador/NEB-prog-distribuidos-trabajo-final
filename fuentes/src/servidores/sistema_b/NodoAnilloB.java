import com.sun.management.OperatingSystemMXBean;
import java.lang.management.ManagementFactory;

import java.io.*;
import java.net.*;
import java.util.*;
import servidor.utils.NodoUso;
import servidor.utils.Tarea;

public class NodoAnilloB {
    //nodos del anillo del que recibe y al que envia
    private int mPuertoIzquierda;
    private int mPuertoDerecha;
    private String mDireccionDerecha;

    //multiservidores
    private static class DetMultsrv{
        public int puerto;
        public String direccion;

        DetMultsrv(int puerto, String direccion){
            this.puerto = puerto;
            this.direccion = direccion;
        }
    }
    private List<DetMultsrv> multiservidores;
    
    private int pAdminFiltros;
    private String dAdminFiltros;
    
    private int timeoutLenght = 60000;

    //identificacion del nodo
    private boolean esPuente = false;
    private int mNumeroNodo;
    private int mNumeroAnillo;

    //valores para sobreescribir
    private static String filtroNulo = "";
    //para el siguiente testigo
    private String filtroActual = filtroNulo;
    private List<String> codigoFiltro = new ArrayList<String>();
    private List<Tarea> tareasPendientes = new ArrayList<Tarea>();
    private List<NodoUso> dispPrevias = new ArrayList<NodoUso>();




    NodoAnilloB(int numeroNodo, int puertoIzquierda, int puertoDerecha, String direccion){
        this.mNumeroNodo = numeroNodo;
        this.mPuertoIzquierda = puertoIzquierda;
        this.mPuertoDerecha = puertoDerecha;
        this.mDireccionDerecha = direccion;
    }
    NodoAnilloB(int numeroNodo, int numeroAnillo, int puertoIzquierda, int puertoDerecha, String dirDerecha, 
        int puertoAdminFiltros, String direccionAdminFiltros, List<DetMultsrv> mltServidores
    ){
        this.mNumeroNodo = numeroNodo;
        this.mNumeroAnillo = numeroAnillo;
        this.mPuertoIzquierda = puertoIzquierda;
        this.mPuertoDerecha = puertoDerecha;
        this.mDireccionDerecha = dirDerecha;
        this.multiservidores = mltServidores;
        this.pAdminFiltros = puertoAdminFiltros;
        this.dAdminFiltros = direccionAdminFiltros;
        this.esPuente = true;
    }




    private long getUsoNodo(){
        OperatingSystemMXBean os = ManagementFactory.getPlatformMXBean(OperatingSystemMXBean.class);
        long uso = os.getFreePhysicalMemorySize() * (long) os.getSystemCpuLoad();
        return uso;
    }
    private void nuevoTestigo(TestigoB testigo){
        System.out.println(mNumeroNodo + " nuevoTestigo");
        
        if(esPuente){
            llamaAdmin();
            llamaMultisrv();
        }
        try {
            Thread.sleep(2000);
        } catch (Exception e){}
        
        testigo.nombreFiltro = filtroActual;
        testigo.codigo = codigoFiltro;
        testigo.tareas = tareasPendientes;

        pasarTestigo(testigo);
        
        filtroActual = filtroNulo;
        tareasPendientes.clear();
    }
    private void pasarTestigo(TestigoB testigo){
        System.out.println(mNumeroNodo + " pasarTestigo " + mDireccionDerecha + " " + mPuertoDerecha);
        try{
            Socket socketDatosDerecha = new Socket(mDireccionDerecha, mPuertoDerecha);
            ObjectOutputStream flujoSalida = new ObjectOutputStream(socketDatosDerecha.getOutputStream());
            flujoSalida.writeObject(testigo);

        }catch(Exception e){e.printStackTrace(System.out);}
    }


    private void esperarTestigo(){
        System.out.println("esperarTestigo inicial");
        try{
            ServerSocket socketConexionIzquierda = new ServerSocket(mPuertoIzquierda);
            socketConexionIzquierda.setSoTimeout(timeoutLenght);
            //System.out.println(socketConexionIzquierda.getSoTimeout());
            Socket socketDatosIzquierda;

            while((socketDatosIzquierda = socketConexionIzquierda.accept()) != null){
                System.out.println("esperarTestigo");
                nuevoTestigo(recibirTestigo(socketDatosIzquierda));
            }
        }catch(Exception e){e.printStackTrace(System.out);}
    }
    private TestigoB recibirTestigo(Socket socketDatosIzquierda){
        System.out.println(mNumeroNodo + " recibirTestigo");
        try{
            ObjectInputStream flujoEntrada = new ObjectInputStream(socketDatosIzquierda.getInputStream());
            TestigoB testigo = (TestigoB) flujoEntrada.readObject();

            socketDatosIzquierda.close();

            parsearEntrada(testigo);

            return testigo;
        }catch(Exception e){e.printStackTrace(System.out);}
        return null;
    }


    private TestigoB parsearEntrada(TestigoB testigo){
        System.out.println("anillo " + mNumeroAnillo + " nodo " + mNumeroNodo + " parsearEntrada");

        boolean contieneEsteNodo = false;
        for(NodoUso actual : testigo.disponibles){
            if(actual.numeroNodo == mNumeroNodo){
                actual.usoNodo = getUsoNodo();
                contieneEsteNodo = true;
                continue;
            }
        }
        if(!contieneEsteNodo){
            NodoUso este = new NodoUso(getUsoNodo(), mNumeroNodo);
            testigo.disponibles.add(este);
        }
        dispPrevias = testigo.disponibles;
        
        if(!testigo.nombreFiltro.equals(filtroActual)) creaFiltro(testigo);

        System.out.println("tareas en el testigo");
        for(Tarea actual : testigo.tareas){
            System.out.println("tarea " + actual.encargo + " a nodo " + actual.nodoEncargado);
            if(actual.nodoEncargado == mNumeroNodo){
                System.out.println("Este nodo tiene  que aplicar filtro");

                aplicarFiltro(actual.encargo, testigo.nombreFiltro);
            }
        }

        return testigo;
    }
    private void creaFiltro(TestigoB testigo){
        
    }
    private void aplicarFiltro(String nombreImg, String filtroEncargado){
        System.out.println("Mandar imagen a python para aplicar filtro");

        System.out.println(filtroEncargado);
        try {
            System.out.println("python3 filtros/"+filtroEncargado+" "+nombreImg);
            Process exec = Runtime.getRuntime().exec(
                "python3 filtros/"+filtroEncargado+" "+nombreImg
            );
            exec.waitFor();            
            BufferedReader reader = new BufferedReader(new InputStreamReader(exec.getInputStream()));
            String linea;
            while((linea = reader.readLine())!=null)
                System.out.println(linea);
        }catch(Exception e){e.printStackTrace(System.out);}
    }
    
    private void llamaAdmin(){
        System.out.println("llamaAdmin in "+dAdminFiltros+":"+pAdminFiltros);
        try{
            Socket datosAdmin = new Socket(dAdminFiltros, pAdminFiltros);
            ObjectOutputStream salidaAdmin = new ObjectOutputStream(datosAdmin.getOutputStream());
            salidaAdmin.writeObject(mNumeroAnillo);
            salidaAdmin.writeObject(filtroActual);

            ObjectInputStream entradaAdmin = new ObjectInputStream(datosAdmin.getInputStream());
            String nombreFiltro = (String) entradaAdmin.readObject();
            if(nombreFiltro.equals("ok")) return;
            filtroActual = nombreFiltro.split(",")[0]; //normaliza nombre

            codigoFiltro = (List<String>) entradaAdmin.readObject();
        }catch(Exception e){e.printStackTrace(System.out);}
        System.out.println("llamaAdmin out");
    }
    private void llamaMultisrv(){
        System.out.println("Actualizar multiservidor, recuperar ordenes");
        List<NodoUso> libres = new ArrayList<NodoUso>();
        libres.addAll(dispPrevias);
        
        for(DetMultsrv i : multiservidores){
            if(libres.isEmpty()) return;
            try{
                System.out.println("llamaMultisrv in "+i.direccion+":"+i.puerto);
                Socket datosMultisrv = new Socket(i.direccion, i.puerto);
                ObjectOutputStream flujoSalida = new ObjectOutputStream(datosMultisrv.getOutputStream());
                flujoSalida.writeObject(mNumeroAnillo);
                //flujoSalida.flush();
                flujoSalida.writeObject(filtroActual);
                flujoSalida.writeObject(libres);
    
                ObjectInputStream flujoEntrada = new ObjectInputStream(datosMultisrv.getInputStream());
                List<Tarea> tareas = (List<Tarea>) flujoEntrada.readObject();
                datosMultisrv.close();
                
                for(Tarea j : tareasPendientes){
                    tareasPendientes.add(j);
                    for(NodoUso k : libres){
                        if(k.numeroNodo == j.nodoEncargado) libres.remove(k);
                    }
                }
                System.out.println("llamaMultisrv out");
            }catch(Exception e){e.printStackTrace(System.out);}
        }
    }


    public static void main(String[] args) {
        try {
            List<Tarea> tareas = new ArrayList<Tarea>();

            if(args.length == 4){
                NodoAnilloB nodo = new NodoAnilloB(Integer.valueOf(args[0]), 
                                                Integer.valueOf(args[1]), 
                                                Integer.valueOf(args[2]), 
                                                args[3]);
                TestigoB testigo = new TestigoB("", tareas); //iniciador
                nodo.esperarTestigo();

            }else {
                List<DetMultsrv> mltServidores = new ArrayList<DetMultsrv>();
                for(int i=7; i<args.length; i=i+2){
                    DetMultsrv n = new NodoAnilloB.DetMultsrv(Integer.valueOf(args[i]), args[i+1]);
                    mltServidores.add(n);
                }

                NodoAnilloB nodo = new NodoAnilloB(Integer.valueOf(args[0]), 
                                                Integer.valueOf(args[1]), 
                                                Integer.valueOf(args[2]), 
                                                Integer.valueOf(args[3]),
                                                args[4],
                                                Integer.parseInt(args[5]),
                                                args[6],
                                                mltServidores
                                                );
                TestigoB testigo = new TestigoB("", tareas);

                nodo.esPuente = true;
                nodo.nuevoTestigo(testigo);
                nodo.esperarTestigo();
            }
        }catch(Exception e){e.printStackTrace(System.out);}
    }
}
