import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.ServerSocket;
import java.math.BigInteger;
import java.net.Socket;
import java.net.ServerSocket;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.util.ArrayList;
import java.util.List;

public class AdminUsuarios {
    private int puertoAuth;
    private String direccionAuth;

    AdminUsuarios(int puertoAuth, String direccionAuth) {
        this.puertoAuth = puertoAuth;
        this.direccionAuth = direccionAuth;
    }

    private void menu(){
        System.out.println(System.nanoTime() + "Entra en menu");
        String opcion="";
        List<String> orden; //nombre, contraseña
		while(!opcion.equals("q")) {
			System.out.println("Opciones:");
			System.out.println("    Pulsa a para añadir un usuario");
			System.out.println("    Pulsa e eliminar un usuario");
			System.out.println("    Pulsa q para terminar");
			opcion=System.console().readLine();
            System.out.println();

            switch (opcion) {
                case "q": return;
                case "a": 
                    boolean valid = false;
                    orden = new ArrayList<String>();
                    
                    while(!valid){
                        System.out.print("Correo de usuario: ");
                        orden.add(System.console().readLine());
                        if(orden.get(0).matches("\\w+@\\w+\\.\\w{2,3}")) valid = true;
                    }

                    System.out.print("Contraseña: ");
                    orden.add(passwd4hash(System.console().readLine()));
                    System.out.println();

                    enviaOrden(orden);
                    break;

                case "e":
                    orden = new ArrayList<String>();

                    System.out.print("Nombre de usuario: ");
                    orden.add(System.console().readLine());
                    System.out.println();

                    enviaOrden(orden);
                    break;

                default:
            }
		}
    }
    private String passwd4hash(String passwd){
        System.out.println(System.nanoTime() + " Convirtiendo contraseña");
        String hash = passwd;
        try {
            MessageDigest md=null;
			md = MessageDigest.getInstance("MD5");
            md.update(passwd.getBytes(StandardCharsets.UTF_8));
            byte[] digest = md.digest();
            hash = String.format("%032x", new BigInteger(1, digest));
            System.out.println(hash);
            System.out.println("Contraseña convertida sin problemas");
		}catch(Exception e){e.printStackTrace(System.out);}
        return hash;
    }

    private void enviaOrden(List<String> orden){
        System.out.println(System.nanoTime() + "Envio de orden a autenticacion");
        try{
            Socket datosAuth = new Socket(direccionAuth, puertoAuth);
            ObjectOutputStream salidaAuth = new ObjectOutputStream(datosAuth.getOutputStream());

            salidaAuth.writeObject(orden);
        }catch(Exception e){e.printStackTrace(System.out);}
    }

    public static void main(String args[]){
        //cristian89 - servidor a Autenticacion
        try{
            System.out.println("Servidor cristian89 a Autenticacion");
			ServerSocket socketConexion = new ServerSocket(Integer.valueOf(args[2]));
			Socket socketDatos = socketConexion.accept();

			ObjectOutputStream flujoSalida = new ObjectOutputStream(socketDatos.getOutputStream());
			ObjectInputStream flujoEntrada = new ObjectInputStream(socketDatos.getInputStream());
			
			for(int i=0; i<10; i++){
				String in = (String) flujoEntrada.readObject();
				long t = System.nanoTime();
				flujoSalida.writeObject(t);
			}

		}catch(Exception e){e.printStackTrace(System.out);}


        AdminUsuarios admin = new AdminUsuarios(Integer.valueOf(args[0]), args[1]);
        admin.menu();
    }
}
