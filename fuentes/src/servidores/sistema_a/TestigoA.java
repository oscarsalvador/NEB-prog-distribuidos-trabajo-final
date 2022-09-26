import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;
import servidor.utils.NodoUso;
import servidor.utils.Tarea;

public class TestigoA implements Serializable {
    public String nombreImg;
    public List<Tarea> tareas = new ArrayList<Tarea>(); //numero de nodo y filtro que tiene que aplicar
    public List<NodoUso> disponibles = new  ArrayList<NodoUso>(); //numero de nodo y CPU * RAM del mismo
    
    TestigoA(String nombreImg, List<Tarea> tareas){
        this.nombreImg = nombreImg;
        this.tareas = tareas;
    }
}
