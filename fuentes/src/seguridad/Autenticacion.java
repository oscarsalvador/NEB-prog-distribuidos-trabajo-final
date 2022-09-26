import java.io.*;
import java.net.*;
import java.util.*;


public class Autenticacion {
    private int pEscuchaProxy;
    private int pEscuchaAdmin;
    private String pathCSV;
    //parejas de correo y hash pwd
    private Map<String,String> usuarios = new HashMap<String,String>();

    long correccion;


    

    Autenticacion(int pEscuchaProxy, int pEscuchaAdmin, String pathCSV, long correccion){
        this.pEscuchaProxy = pEscuchaProxy;
        this.pEscuchaAdmin = pEscuchaAdmin;
        this.pathCSV = pathCSV;
        this.correccion = correccion;
    }

    


    private void cargaMemoria(){
        System.out.println( (System.nanoTime() - correccion) + " Carga del CSV a memoria");
        try{
            Scanner scan = new Scanner(new File(pathCSV));
            while(scan.hasNextLine()){
                String[] campos = scan.nextLine().split(",");
                System.out.println("Añadiendo: " + campos[0] + campos[1]);
                usuarios.put(campos[0], campos[1]);
            }
        }catch(Exception e){e.printStackTrace(System.out);}
    }

    private boolean checksOut(String correo, String hash){
        System.out.println((System.nanoTime() - correccion) + " Buscando usuario "+correo);

        if(!usuarios.containsKey(correo)) return false;
        if(!usuarios.get(correo).equals(hash)) return false;
        System.out.println(correo + " autenticado");
        return true;
    }

    private void escuchaProxy(){
        System.out.println((System.nanoTime() - correccion) + " escuchaProxy");
        try{
            ServerSocket conexionProxy = new ServerSocket(pEscuchaProxy);
            Socket datosProxy;
            
            while( (datosProxy = conexionProxy.accept()) != null){
                System.out.println("Peticion del proxy recibida");
                AtiendeProxy atiende = this.new AtiendeProxy(this, datosProxy);

                Thread hilo = new Thread(atiende);
                hilo.start();
            }
        }catch(Exception e){e.printStackTrace(System.out);}
    }


    private void escuchaAdmin(){
        System.out.println((System.nanoTime() - correccion) + " escuchaAdmin");
        try{
            ServerSocket conexionAdmin = new ServerSocket(pEscuchaAdmin);
            Socket datosAdmin;

            while( (datosAdmin = conexionAdmin.accept()) != null){
                ObjectInputStream entradaAdmin = new ObjectInputStream(datosAdmin.getInputStream());
                List<String> orden = (List<String>) entradaAdmin.readObject();

                //2 campos => correo y hash, solo necesitas correo para borrar
                if(orden.size() == 2){
                    creaUsuario(orden.get(0),orden.get(1));
                }else if(orden.size() == 1){
                    eliminaUsuario(orden.get(0));
                }
            }
        }catch(Exception e){e.printStackTrace(System.out);}
    }
    private void creaUsuario(String correo, String hash){
        System.out.println((System.nanoTime() - correccion) + " creaUsuario");

        usuarios.put(correo, hash);
        if(usuarios.containsKey(correo)) System.out.println("Nuevo usuario " + correo + " añadido a memoria");
        
        try {
            System.out.println("Añadiendo a csv");

            PrintWriter output = new PrintWriter(new FileWriter(pathCSV,true));
            output.print("\n"+correo+","+hash);
            output.flush();
            output.close();
        }catch(Exception e){e.printStackTrace(System.out);}
    }
    private void eliminaUsuario(String correo){
        System.out.println((System.nanoTime() - correccion) + " eliminaUsuario");

        if(!usuarios.containsKey(correo)) {
            System.out.println("Usuario " + correo + " no estaba registrado");
            return;
        }

        usuarios.remove(correo);
        if(!usuarios.containsKey(correo)) System.out.println("Usuario " + correo + " eliminado");
        
        try {
            System.out.println("Borrando de csv");

            File original = new File(pathCSV);
            File nuevo = new File("copia.csv");
            
            Scanner scan = new Scanner(original);
            PrintWriter copia = new PrintWriter(new FileWriter(nuevo,true));
            
            while(scan.hasNextLine()){
                String siguiente = scan.nextLine();
                // System.out.println("pensando si añadir " + siguiente);
                // System.out.println(correo);
                // System.out.println(siguiente.contains(correo));
                if(!siguiente.contains(correo)){
                    // System.out.println("linea añadida");
                    copia.println(siguiente);  
                    copia.flush();
                }
            }

            nuevo.renameTo(original); //sobreescribe la original, no hace falta renombrarla y borrarla
        }catch(Exception e){e.printStackTrace(System.out);}
    }



    //<puerto para escuchar al proxy> <puerto para escuchar al administrador> <path local a BBDDUsuarios.csv>
    public static void main(String args[]){
        //cristian89 
            long correccionMedia = 0;
            //cliente a AdminUsuarios
            try{
                System.out.println("Cliente cristian89 a AdminUsuarios");

                Socket socketDatos = new Socket(args[4], Integer.valueOf(args[3]));
                ObjectOutputStream salidaAdmin = new ObjectOutputStream(socketDatos.getOutputStream());
                ObjectInputStream entradaAdmin = new ObjectInputStream(socketDatos.getInputStream());

                for(int i=0; i<10; i++){
                    long t1 = System.nanoTime();
                    salidaAdmin.writeObject("t");
                    long t = (long) entradaAdmin.readObject();
                    long t2 = System.nanoTime();

                    long Tround = t2-t1;
                    long correccion = t+(Tround/2);
                    correccionMedia += correccion;

                    System.out.println("correccion" + i + " = " + correccion);
                }
                correccionMedia = correccionMedia/10;
                System.out.println("correccion media = " + correccionMedia);
            }catch(Exception e){e.printStackTrace(System.out);}
            System.out.println(correccionMedia);
            //servidor a Proxy
            try{
                System.out.println("Servidor cristian89 a Proxy");

                ServerSocket socketConexion = new ServerSocket(Integer.valueOf(args[5]));
                Socket socketDatos = socketConexion.accept();
    
                ObjectOutputStream flujoSalida = new ObjectOutputStream(socketDatos.getOutputStream());
                ObjectInputStream flujoEntrada = new ObjectInputStream(socketDatos.getInputStream());
                
                for(int i=0; i<10; i++){
                    String in = (String) flujoEntrada.readObject();
                    long t = System.nanoTime();
                    flujoSalida.writeObject(t);
                    System.out.println("Tiempo " + t);
                }
    
                flujoSalida.writeObject(correccionMedia);
            }catch(Exception e){e.printStackTrace(System.out);}

        Autenticacion auth = new Autenticacion(Integer.valueOf(args[0]), Integer.valueOf(args[1]), args[2], correccionMedia);
        auth.cargaMemoria();
        AtiendeAdmin atiende = auth.new AtiendeAdmin(auth);
        Thread hilo = new Thread(atiende);
        hilo.start();
        auth.escuchaProxy();
    }
            


    private class AtiendeProxy implements Runnable{
        Autenticacion auth;
        Socket datosProxy;

        AtiendeProxy(Autenticacion auth, Socket datosProxy){
            this.auth = auth;
            this.datosProxy = datosProxy;
        }

        @Override
        public void run(){
            try {
                ObjectInputStream entradaProxy;
                entradaProxy = new ObjectInputStream(datosProxy.getInputStream());
                List<String> credenciales = (List<String>)entradaProxy.readObject();
                
                ObjectOutputStream salidaProxy = new ObjectOutputStream(datosProxy.getOutputStream());
                salidaProxy.writeObject(this.auth.checksOut(credenciales.get(0), credenciales.get(1))); //devuelve el booleano de la autenticacion

            }catch(Exception e){e.printStackTrace(System.out);}
        }
    }
    private class AtiendeAdmin implements Runnable{
        Autenticacion auth;

        AtiendeAdmin(Autenticacion auth){
            this.auth = auth;
        }

        @Override
        public void run(){
            auth.escuchaAdmin();
        }
    }
}
