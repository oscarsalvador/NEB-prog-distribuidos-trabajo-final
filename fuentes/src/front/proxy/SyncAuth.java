import java.net.Socket;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;

public class SyncAuth{

    public static int pAuth;
    public static String dAuth;

    SyncAuth(){}
    SyncAuth(int puertoAuth, String direccionAuth){
        pAuth = puertoAuth;
        dAuth = direccionAuth;
        System.out.println(dAuth+":"+pAuth);
    }

    public long getCorreccion(){
        //cristian89 - cliente a Autenticacion
        long correccionMedia = 0;
        long correcAuthAdmin = 0;
        try{
            Socket socketDatos = new Socket(dAuth, pAuth);
            ObjectOutputStream salidaAuth = new ObjectOutputStream(socketDatos.getOutputStream());
            ObjectInputStream entradaAuth = new ObjectInputStream(socketDatos.getInputStream());

            for(int i=0; i<10; i++){
                long t1 = System.nanoTime();
                salidaAuth.writeObject("t");
                long t = (long) entradaAuth.readObject();
                long t2 = System.nanoTime();

                long Tround = t2-t1;
                long correccion = t+(Tround/2);
                correccionMedia += correccion;

                System.out.println("correccion" + i + " = " + correccion);
            }
            correccionMedia = correccionMedia/10;
            System.out.println("correccion media = " + correccionMedia);

            correcAuthAdmin = (long) entradaAuth.readObject();
        }catch(Exception e){e.printStackTrace(System.out);}

        return correccionMedia + correcAuthAdmin;
    }
}

