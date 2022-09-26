import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.InetAddress;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.ArrayList;
import java.util.List;
import servidor.utils.NodoUso;
import servidor.utils.Tarea;

public class MultiservidorA {
    private class Peticion{
        public Socket socketPeticion;
        public String nombreImg;
        public List<String> filtros;
        public boolean uso = false;
    }
    private List<Peticion> cola = new ArrayList<Peticion>();

    //para emitir a central
    private int pSalidaCentral;
    private String dCentral;
    //puertos de escucha
    private int pCentral;
    private int pAnillos;




    MultiservidorA(int puertoRegistroCentral, String dCentral, int puertoEscucharCentral, int puertoEscucharAnillos){
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
    
                    cola.add(encolar);
                }
            }
        }catch(Exception e){e.printStackTrace(System.out);}
    }

    private void escuchaAnillo(){
        System.out.println("escuchaAnillo");
        try{
            ServerSocket conexionAnillo = new ServerSocket(pAnillos);
            Socket datosAnillo;

            while( (datosAnillo = conexionAnillo.accept()) != null){
                System.out.println("escuchaAnillo nueva peticion");
                //notificar central
                Peticion terminada=null;
                for(Peticion i: cola) {
                    if (i.uso) {
                        terminada = i;
                        break;
                    }
                }
                if(terminada!=null){
                    cola.remove(terminada);
                
                    System.out.println("peticion " + terminada.nombreImg + " atendida, devolver true");
                    ObjectOutputStream salidaCentral = 
                        new ObjectOutputStream(terminada.socketPeticion.getOutputStream());
                    salidaCentral.writeObject(true);
                }

                //devolver orden
                ObjectOutputStream salidaAnillo = new ObjectOutputStream(datosAnillo.getOutputStream());
                ObjectInputStream entradaAnillo = new ObjectInputStream(datosAnillo.getInputStream());
                if(!cola.isEmpty()){
                    Peticion peticion = cola.get(0);
                    
                    List<NodoUso> nodos = (List<NodoUso>) entradaAnillo.readObject();
                    nodos.sort((NodoUso a, NodoUso b) -> (int) (a.usoNodo - b.usoNodo));
                    nodos = nodos.subList(0, peticion.filtros.size());
                    
                    List<Tarea> tareas = new ArrayList<Tarea>();
                    for(int i=0; i<peticion.filtros.size(); i++){
                        tareas.add(new Tarea(nodos.get(i).numeroNodo, peticion.filtros.get(i)));
                    }

                    salidaAnillo.writeObject(peticion.nombreImg);
                    salidaAnillo.writeObject(tareas);
                    peticion.uso = true;
                    System.out.println("orden a anillo enviada");
                }else{
                    salidaAnillo.writeObject("");
                    salidaAnillo.writeObject(new ArrayList<String>());
                    System.out.println("orden vacia enviada");
                }
            }
        }catch(Exception e){e.printStackTrace(System.out);}
    }


    public static void main(String[] args) {
        MultiservidorA a = new MultiservidorA(Integer.valueOf(args[0]), args[1], Integer.valueOf(args[2]), Integer.valueOf(args[3]));
        a.registrarse();
        
        AtiendeCentral atiende = a.new AtiendeCentral(a);
        Thread hilo = new Thread(atiende);
        hilo.start();

        a.escuchaAnillo();
    }





    private class AtiendeCentral implements Runnable{
        MultiservidorA mA;

        AtiendeCentral(MultiservidorA mA){
            this.mA = mA;
        }

        @Override
        public void run(){
            mA.escuchaCentral();
        }
    }
}
