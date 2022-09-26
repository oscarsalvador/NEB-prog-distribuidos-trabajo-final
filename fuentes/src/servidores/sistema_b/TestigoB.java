import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;
import servidor.utils.NodoUso;
import servidor.utils.Tarea;

public class TestigoB implements Serializable {
    public String nombreFiltro;
    public List<String> codigo;
    public List<Tarea> tareas = new ArrayList<Tarea>(); //numero de nodo y filtro que tiene que aplicar
    public List<NodoUso> disponibles = new  ArrayList<NodoUso>(); //numero de nodo y CPU * RAM del mismo
    
    TestigoB(String nombreFiltro, List<Tarea> tareas){
        this.nombreFiltro = nombreFiltro;
        this.tareas = tareas;
    }
}
