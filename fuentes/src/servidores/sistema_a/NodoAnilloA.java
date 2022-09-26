import com.sun.management.OperatingSystemMXBean;
import java.lang.management.ManagementFactory;

import java.io.*;
import java.net.*;
import java.util.*;
import servidor.utils.NodoUso;
import servidor.utils.Tarea;

public class NodoAnilloA {
    //nodos del anillo del que recibe y al que envia
    private int mPuertoIzquierda;
    private int mPuertoDerecha;
    private String mDireccionDerecha;
    //multiservidor
    private int puertoMult;
    private String direcMult;
    
    private int timeoutLenght = 40000;

    //identificacion del nodo
    private boolean esPuente = false;
    private int mNumeroNodo;

    //valores para sobreescribir
    private static String imgNulo = "";
    //para el siguiente Testigo
    public String imgPendiente = imgNulo;
    public List<Tarea> tareasPendientes = new ArrayList<Tarea>();
    public List<NodoUso> dispPrevias = new ArrayList<NodoUso>();




    NodoAnilloA(int numeroNodo, int puertoIzquierda, int puertoDerecha, String direccion){
        this.mNumeroNodo = numeroNodo;
        this.mPuertoIzquierda = puertoIzquierda;
        this.mPuertoDerecha = puertoDerecha;
        this.mDireccionDerecha = direccion;
    }
    NodoAnilloA(int numeroNodo, int puertoIzquierda, int puertoDerecha, String dirDerecha, int puertoMult, String direcMult){
        this.mNumeroNodo = numeroNodo;
        this.mPuertoIzquierda = puertoIzquierda;
        this.mPuertoDerecha = puertoDerecha;
        this.mDireccionDerecha = dirDerecha;
        this.puertoMult = puertoMult;
        this.direcMult = direcMult;
        this.esPuente = true;
    }




    private long getUsoNodo(){
        OperatingSystemMXBean os = ManagementFactory.getPlatformMXBean(OperatingSystemMXBean.class);
        long uso = os.getFreePhysicalMemorySize() * (long) os.getSystemCpuLoad();
        return uso;
    }
    private void nuevoTestigo(TestigoA Testigo){
        System.out.println(mNumeroNodo + " nuevoTestigo");
        
        if(esPuente){
            llamaMultisrv();
        }
        try {
            Thread.sleep(2000);
        } catch (Exception e){}
        
        Testigo.nombreImg = imgPendiente;
        System.out.println(Testigo.nombreImg);
        Testigo.tareas = tareasPendientes;

        pasarTestigo(Testigo);
        
        imgPendiente = imgNulo;
        tareasPendientes.clear();
    }
    private void pasarTestigo(TestigoA Testigo){
        System.out.println(mNumeroNodo + " pasarTestigo " + mDireccionDerecha + " " + mPuertoDerecha);
        try{
            Socket socketDatosDerecha = new Socket(mDireccionDerecha, mPuertoDerecha);
            ObjectOutputStream flujoSalida = new ObjectOutputStream(socketDatosDerecha.getOutputStream());
            flujoSalida.writeObject(Testigo);

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
    private TestigoA recibirTestigo(Socket socketDatosIzquierda){
        System.out.println(mNumeroNodo + " recibirTestigo");
        try{
            ObjectInputStream flujoEntrada = new ObjectInputStream(socketDatosIzquierda.getInputStream());
            TestigoA Testigo = (TestigoA) flujoEntrada.readObject();

            socketDatosIzquierda.close();

            parsearEntrada(Testigo);

            return Testigo;
        }catch(Exception e){e.printStackTrace(System.out);}
        return null;
    }


    private TestigoA parsearEntrada(TestigoA Testigo){
        System.out.println(mNumeroNodo + " parsearEntrada");

        boolean contieneEsteNodo = false;
        for(NodoUso actual : Testigo.disponibles){
            if(actual.numeroNodo == mNumeroNodo){
                actual.usoNodo = getUsoNodo();
                contieneEsteNodo = true;
                continue;
            }
        }
        if(!contieneEsteNodo){
            NodoUso este = new NodoUso(getUsoNodo(), mNumeroNodo);
            Testigo.disponibles.add(este);
        }
        dispPrevias = Testigo.disponibles;
        
        for(Tarea actual : Testigo.tareas){
            if(actual.nodoEncargado == mNumeroNodo){
                System.out.println("Este nodo tiene  que aplicar filtro");
                aplicarFiltro(Testigo.nombreImg, actual.encargo);
            }
        }

        return Testigo;
    }


    private void aplicarFiltro(String nombreImg, String filtroEncargado){
        System.out.println("Mandar imagen a python para aplicar filtro");
        // filtroEncargado = filtroEncargado.split(".")[0]+"py";
        
        System.out.println(filtroEncargado);

        try {
            System.out.println("python3 filtros/"+filtroEncargado+" "+nombreImg);
            Process exec = Runtime.getRuntime().exec(
                "python3 filtros/"+filtroEncargado+" "+nombreImg
            );
            System.out.println(exec.waitFor());
            BufferedReader reader = new BufferedReader(new InputStreamReader(exec.getInputStream()));
            String linea;
            while((linea = reader.readLine())!=null)
                System.out.println(linea);
        }catch(Exception e){e.printStackTrace(System.out);}
    }
    
    private void llamaMultisrv(){
        System.out.println("Actualizar multiservidor, recuperar ordenes");
        try{
            Socket datosMultisrv = new Socket(direcMult, puertoMult);
            ObjectOutputStream flujoSalida = new ObjectOutputStream(datosMultisrv.getOutputStream());
            flujoSalida.writeObject(dispPrevias);
            
            flujoSalida.flush();

            ObjectInputStream flujoEntrada = new ObjectInputStream(datosMultisrv.getInputStream());
            imgPendiente = (String) flujoEntrada.readObject();
            System.out.println(imgPendiente);
            tareasPendientes = (List<Tarea>) flujoEntrada.readObject();

            datosMultisrv.close();
        }catch(Exception e){e.printStackTrace(System.out);}

        for(Tarea i: tareasPendientes) System.out.println(i.encargo);
    }


    //numero de este nodo, puerto iquierdo, puerto derecho, direccion derecha, [puerto multiservidor, direccion multiservidor]
    public static void main(String[] args) {
        try {
            List<Tarea> tareas = new ArrayList<Tarea>();

            if(args.length == 4){
                NodoAnilloA nodo = new NodoAnilloA(Integer.valueOf(args[0]), 
                                                Integer.valueOf(args[1]), 
                                                Integer.valueOf(args[2]), 
                                                args[3]);
                TestigoA Testigo = new TestigoA("", tareas); //iniciador
                nodo.esperarTestigo();

            }else if(args.length == 6){
                NodoAnilloA nodo = new NodoAnilloA(Integer.valueOf(args[0]), 
                                                Integer.valueOf(args[1]), 
                                                Integer.valueOf(args[2]), 
                                                args[3], 
                                                Integer.valueOf(args[4]), args[5]);
                TestigoA Testigo = new TestigoA("", tareas);

                nodo.esPuente = true;
                nodo.nuevoTestigo(Testigo);
                nodo.esperarTestigo();
            }
        }catch(Exception e){e.printStackTrace(System.out);}
    }
}
