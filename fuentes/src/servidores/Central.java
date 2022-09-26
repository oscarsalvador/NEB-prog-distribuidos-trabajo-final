import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.ArrayList;
import java.util.List;

import javax.swing.text.MutableAttributeSet;

public class Central {
    private class MultiServ{
        public int puerto;
        public String direccion;

        MultiServ(int p, String d){
            puerto = p;
            direccion = d;
        }
    }
    private List<MultiServ> multiservs = new ArrayList<MultiServ>(); //FIFO
    private List<String> filtrosCorriendo = new ArrayList<String>();
    //puertos de escucha 
    private int pProxy;
    private int pAdmin;
    private int pMults;





    Central(int puertoEscuchaProxy, int puertoEscuchaAdmin, int puertoEscuchaMultiltiservidores){
        pProxy = puertoEscuchaProxy;
        pAdmin = puertoEscuchaAdmin;
        pMults = puertoEscuchaMultiltiservidores;
        filtrosCorriendo.add("acuarelas");
    }






    private void escuchaMultiservs(){
        System.out.println("escuchaMultiservs");
        try{
            ServerSocket conexionMults = new ServerSocket(pMults);
            Socket datosMults;

            while( (datosMults = conexionMults.accept()) != null){
                ObjectInputStream entradaMult = new ObjectInputStream(datosMults.getInputStream());
                int pMult = (int) entradaMult.readObject();
                String dMult = (String) entradaMult.readObject();
                multiservs.add(new MultiServ(pMult, dMult));
                System.out.println("\n\nNuevo multiservidor en " + dMult + ":" + pMult + "\n\n");
            }
        }catch(Exception e){e.printStackTrace(System.out);}
    }
    
    private void escuchaAdmin(){
        System.out.println("escuchaAdmin");
        try{
            ServerSocket conexionAdmin = new ServerSocket(pAdmin);
            Socket datosAdmin;

            while( (datosAdmin = conexionAdmin.accept()) != null){
                System.out.println("escuchaAdminnueva peticion");

                ObjectInputStream entradaAdmin = new ObjectInputStream(datosAdmin.getInputStream());
                filtrosCorriendo = (List<String>) entradaAdmin.readObject();
            }
        }catch(Exception e){e.printStackTrace(System.out);}
    }
    
    private void escuchaProxy(){
        System.out.println("escuchaProxy");
        try{
            ServerSocket conexionProxy = new ServerSocket(pProxy);
            Socket datosProxy;

            while( (datosProxy = conexionProxy.accept()) != null){
                System.out.println("escuchaProxy nueva peticion");

                AtiendeProxy atiende = this.new AtiendeProxy(this, datosProxy);
                Thread hilo = new Thread(atiende);
                hilo.start();
            }
        }catch(Exception e){e.printStackTrace(System.out);}
    }
    private void atiendeProxy(Socket datosProxy){
        System.out.println("atiendeProxy");
        boolean resultado = false;
        MultiServ elegido;
        int intentos = 0;
        do{
            System.out.println("intentos de asignar multiservidor " + intentos);
            elegido = eligeMulti();
            intentos++;
        }while(!healthcheck(elegido) && (intentos <6));
        
        try{
            ObjectOutputStream salidaProxy = new ObjectOutputStream(datosProxy.getOutputStream());
            ObjectInputStream entradaProxy = new ObjectInputStream(datosProxy.getInputStream());
            
            String nombreImg = (String) entradaProxy.readObject();
            List<String> filtros = (List<String>) entradaProxy.readObject();

            if(!filtrosCorriendo.containsAll(filtros)){
                System.out.println("Filtros no soportados, cancela");
                resultado = false;
            }else if(elegido != null){
                System.out.println("Propagar peticion " + nombreImg + " a multiservidor");
                Socket datosMult = new Socket(elegido.direccion, elegido.puerto);
                ObjectOutputStream salidaMult = new ObjectOutputStream(datosMult.getOutputStream());
                salidaMult.writeObject(nombreImg);
                salidaMult.writeObject(filtros);
                ObjectInputStream entradaMult = new ObjectInputStream(datosMult.getInputStream());
                resultado = (boolean) entradaMult.readObject();
            }

            System.out.println("Devuelve resultado a proxy");
            salidaProxy.writeObject(resultado  + nombreImg);
        }catch(Exception e){e.printStackTrace(System.out);}
    }
    private MultiServ eligeMulti(){
        if(multiservs.isEmpty()) return null;
        
        //FIFO
        MultiServ elegido = multiservs.get(0);
        multiservs.remove(0);
        multiservs.add(elegido);

        return elegido;
    }
    private boolean healthcheck(MultiServ elegido){
        System.out.println("Healthcheck");
        if(elegido == null) return false;

        boolean commExitosa = false;
        try{
            Socket datosMult = new Socket(elegido.direccion, elegido.puerto);
            ObjectOutputStream salidaMult = new ObjectOutputStream(datosMult.getOutputStream());
            salidaMult.writeObject("healthcheck");
            
            ObjectInputStream entradaMult = new ObjectInputStream(datosMult.getInputStream());
            commExitosa = (boolean) entradaMult.readObject();
        }catch(Exception e){
            multiservs.remove(elegido);
        }

        return commExitosa;
    }





    public static void main(String[] args) {
        Central c = new Central(Integer.valueOf(args[0]), Integer.valueOf(args[1]), Integer.valueOf(args[2]));
        AtiendeMults m = c.new AtiendeMults(c);
        Thread h1 = new Thread(m);
        h1.start();

        AtiendeAdmin a = c.new AtiendeAdmin(c);
        Thread h2 = new Thread(a);
        h2.start();

        c.escuchaProxy();
    }






    private class AtiendeMults implements Runnable{
        Central central;

        AtiendeMults(Central central){
            this.central = central;
        }

        @Override
        public void run(){
            central.escuchaMultiservs();
        }
    }

    private class AtiendeAdmin implements Runnable{
        Central central;

        AtiendeAdmin(Central central){
            this.central = central;
        }

        @Override
        public void run(){
            central.escuchaAdmin();
        }
    }

    private class AtiendeProxy implements Runnable{
        Central central;
        Socket datosProxy;

        AtiendeProxy(Central central, Socket datosProxy){
            this.central = central;
            this.datosProxy = datosProxy;
        }

        @Override
        public void run(){
            central.atiendeProxy(datosProxy);
        }    
    }
}
