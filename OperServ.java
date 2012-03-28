import java.util.*;

/**
* @author Nestor Marique 07-41150
* @author Samuel Bartoli 07-40632
*/

public interface OperServ extends java.rmi.Remote{
    
    /**
    * Busca el certificado  de acuerdo a los parametros especificados con sus
    * valores
    * @param query: parametros que determinan la busqueda del certificado
    * @throws RemoteException
    */
    public ArrayList<String> searchCert(String query) throws java.rmi.RemoteException; 
}
