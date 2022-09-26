import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.InetAddress;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;

import javax.sql.rowset.spi.SyncResolver;

import servidor.utils.NodoUso;
import servidor.utils.Tarea;

public class MultiservidorB {
    private class Peticion{
        public Socket socketPeticion;
        public String nombreImg;
        public List<String> filtros;
        public int anilloUsandolo = -1;
    }
    List<Peticion> cola = new ArrayList<Peticion>();

    //para emitir a central
    private int pSalidaCentral;
    private String dCentral;
    //puertos de escucha
    private int pCentral;
    private int pAnillos;





    MultiservidorB(int puertoRegistroCentral, String dCentral, int puertoEscucharCentral, int puertoEscucharAnillos){
        pSalidaCentral = puertoRegistroCentral;
        this.dCentral = dCentral;
        pCentral = puertoEscucharCentral;
        pAnillos = puertoEscucharAnillos;
    }





    private void registrarse(){
        System.out.println("registrarse");
        try{
            Socket datosCentral = new Socket(dCentral, pSalidaCentral);
            ObjectOutputStream salidaCentral = new ObjectOutputStream(datosCentral.getOutputStream());
            salidaCentral.writeObject(pCentral);
            salidaCentral.writeObject(InetAddress.getLocalHost().getHostAddress());
        }catch(Exception e){e.printStackTrace(System.out);}
    }

    private void escuchaCentral(){
        System.out.println("escuchaCentral");
        try{
            ServerSocket conexionCentral = new ServerSocket(pCentral);
            Socket datosCentral;

            while( (datosCentral = conexionCentral.accept()) != null){
                System.out.println("\nescuchaCentral nueva peticion\n");
                ObjectInputStream entradaCentral = new ObjectInputStream(datosCentral.getInputStream());
                String msg = (String) entradaCentral.readObject();

                if(msg.equals("healthcheck")){
                    System.out.println(msg);
                    ObjectOutputStream salidaCentral = new ObjectOutputStream(datosCentral.getOutputStream());
                    salidaCentral.writeObject(true);

                }else{
                    System.out.println("escuchaCentral atiende peticion");

                    Peticion encolar = new Peticion();
                    encolar.socketPeticion = datosCentral;
    
                    encolar.nombreImg = msg;
                    encolar.filtros = (List<String>) entradaCentral.readObject();
    
                    synchronized(cola){
                        cola.add(encolar);
                    }
                }
            }
            // ServerSocket conexionCentral = new ServerSocket(pCentral);
            // Socket datosCentral;

            // while( (datosCentral = conexionCentral.accept()) != null){
            //     System.out.println("escuchaCentral nueva peticion");
            //     ObjectInputStream entradaCentral = new ObjectInputStream(datosCentral.getInputStream());
            //     String msg = (String) entradaCentral.readObject();

            //     if(msg.equals("healthcheck")){
            //         System.out.println("recibe healthcheck, responde");
            //         ObjectOutputStream salidaCentral = 
            //             new ObjectOutputStream(datosCentral.getOutputStream());
            //         salidaCentral.writeObject("vivo");
            //     }else{
            //         Peticion encolar = new Peticion();
            //         encolar.socketPeticion = datosCentral;
    
            //         encolar.nombreImg = msg;
            //         encolar.filtros = (List<String>) entradaCentral.readObject();
    
            //         cola.add(encolar);
            //     }
            // }
        }catch(Exception e){e.printStackTrace(System.out);}
    }

    private void escuchaAnillo(){
        System.out.println("escuchaAnillo");
        try{
            ServerSocket conexionAnillo = new ServerSocket(pAnillos);
            Socket datosAnillo;

            while( (datosAnillo = conexionAnillo.accept()) != null){
                System.out.print("escuchaAnillo nueva peticion");
                
                ObjectOutputStream salidaAnillo = new ObjectOutputStream(datosAnillo.getOutputStream());
                ObjectInputStream entradaAnillo = new ObjectInputStream(datosAnillo.getInputStream());

                int numAnillo = (int) entradaAnillo.readObject();
                String filtroAnillo = (String) entradaAnillo.readObject();
                List<NodoUso> nodos = (List<NodoUso>) entradaAnillo.readObject();
                nodos.sort((NodoUso a, NodoUso b) -> (int) (a.usoNodo - b.usoNodo));
                int indiceOcupados = 0;

                System.out.println(" de " + numAnillo);

                List<Tarea> tareas = new ArrayList<Tarea>();

                // List<Peticion> clonCola = new ArrayList<Peticion>(cola);
                // Collections.copy(clonCola, cola);
                
                if(!cola.isEmpty()){

                    // ArrayList<Peticion> notificar = new ArrayList<Peticion>();
                    List<Peticion> borrar = new ArrayList<Peticion>();
                    List<Peticion> actualizados = new ArrayList<Peticion>();

                    for(Peticion i : cola){
                        boolean contieneAnillo = false;
                        try{
                            contieneAnillo = i.filtros.contains(filtroAnillo);
                        }catch(Exception e){}

                        //liberar recursos compartidos para el siguiente anillo
                        if(i.anilloUsandolo == numAnillo) i.anilloUsandolo = -1;
                        //carga de tareas
                        if((indiceOcupados < nodos.size()) &&
                            contieneAnillo && 
                            (i.anilloUsandolo == -1)                    
                        ){
                            Tarea tarea = new Tarea(nodos.get(indiceOcupados).numeroNodo, i.nombreImg);
                            System.out.println("tarea " + nodos.get(indiceOcupados).numeroNodo + " " + i.nombreImg);
                            tareas.add(tarea);
                            indiceOcupados = indiceOcupados + 1;
                            
                            borrar.add(i);
                            Peticion iNueva = new Peticion();
                            iNueva.socketPeticion = i.socketPeticion;
                            iNueva.nombreImg = i.nombreImg;
                            try{
                                iNueva.filtros.addAll(i.filtros);
                                iNueva.filtros.remove(filtroAnillo);
                            }catch(Exception e){}    
                            iNueva.anilloUsandolo = numAnillo;
                            actualizados.add(iNueva);

                            // i.filtros.remove(filtroAnillo);
                            i.anilloUsandolo = numAnillo;
                        }
                        //notificar central de petición resuelta por completo
                        //asume que ya se asigno la tarea con el testigo anterior, y ahora está cumplida

                        boolean vacio = false;
                        try{
                            vacio = i.filtros.isEmpty();
                        }catch(Exception e){}
                        if(vacio && i.anilloUsandolo == -1){
                            borrar.add(i);
                            // notificar.add(i);
                            // cola.remove(i);
                        }

                        cola.removeAll(borrar);
                        cola.addAll(actualizados);
                    }

                    for (Peticion i: borrar){
                        System.out.println("peticion " + i.nombreImg + " atendida, devolver true");
                        ObjectOutputStream salidaCentral = 
                        new ObjectOutputStream(i.socketPeticion.getOutputStream());
                        salidaCentral.writeObject(true);
                    }
                    
                    // Collections.copy(cola, clonCola);
                    
                }
                salidaAnillo.writeObject(tareas);
            }
        }catch(Exception e){e.printStackTrace(System.out);}
    }





    public static void main(String[] args) {
//    MultiservidorB(int puertoRegistroCentral, String dCentral, int puertoEscucharCentral, int puertoEscucharAnillos){

        MultiservidorB b = new MultiservidorB(Integer.valueOf(args[0]), args[1], Integer.valueOf(args[2]), Integer.valueOf(args[3]));
        b.registrarse();

        AtiendeCentral atiende = b.new AtiendeCentral(b);
        Thread hilo = new Thread(atiende);
        hilo.start();

        b.escuchaAnillo();
    }





    private class AtiendeCentral implements Runnable{
        MultiservidorB mB;

        AtiendeCentral(MultiservidorB mB){
            this.mB = mB;
        }

        @Override
        public void run(){
            mB.escuchaCentral();
        }
    }
}
