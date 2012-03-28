import java.util.*;

/**
* @author Nestor Marique 07-41150
* @author Samuel Bartoli 07-40632
*/

public interface OperBus extends java.rmi.Remote{
    
    public int signin(String servhost, int servport) throws java.rmi.RemoteException;
    public void signout(String servhost, int servport) throws java.rmi.RemoteException;
    public int incli(String host) throws java.rmi.RemoteException;
    public void outcli(String host, int id) throws java.rmi.RemoteException;
    public ArrayList<String> searchCert(String query,int id) throws java.rmi.RemoteException; 

}
