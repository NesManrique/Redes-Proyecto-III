import java.util.*;

/**
* @author Nestor Marique 07-41150
* @author Samuel Bartoli 07-40632
*/

public interface OperServ extends java.rmi.Remote{
    
    public List<String> OperPrueba(String asd) throws java.rmi.RemoteException;
    public ArrayList<String> searchCert(String query) throws java.rmi.RemoteException; 
}
