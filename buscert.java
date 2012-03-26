import java.io.*;
import java.net.*;
import java.util.*;
import java.rmi.*;
//import nanoxml.*;

public class buscert extends java.rmi.server.UnicastRemoteObject implements OperBus{

        static List<String []> servidores = Collections.synchronizedList(new ArrayList<String []>()); 
        static List<String []> clientes = Collections.synchronizedList(new ArrayList<String []>()); 
        static List<String []> certificados = Collections.synchronizedList(new ArrayList<String []>());

    public buscert() throws java.rmi.RemoteException{
        super();
    }

    public int eraseServ(String host, String port){
        int h=0; 
        for(String a[] : servidores){
            if(a[0].equals(host) && a[1].equals(port)){
                break;
            }
            h++;
        }

        if(h>=servidores.size()){
            return 1;
        }else{
            servidores.remove(h);
            return 0;
        }

    }

    public List<String> OperPrueba(String asd){

        List<String> lc = Collections.synchronizedList(new ArrayList<String>());
        lc.add(asd);

        return lc;
    }

    public static void conectar(int puertoclientes){

        //Creación del registro y objeto remoto para los clientes
        try{
            java.rmi.registry.LocateRegistry.createRegistry(puertoclientes);
        }catch (RemoteException ex){
            System.err.println("Error creando el registry, el puerto ya esta en uso.");
            System.exit(-1);
        }

        OperBus operbus = null;
        try{
            operbus = new buscert();
            if(operbus!=null)
                System.out.println("no es null");
            else
                System.out.println("si es que ladilla");
        }catch(RemoteException ex){
            System.out.println("Error en la creacion del objeto remoto.");
        }

        String dirbus = "rmi://localhost:"+puertoclientes+"/buscert";
        System.out.println(dirbus);
        try{
            Naming.rebind(dirbus,operbus);
        }catch(Exception e){
            System.err.println("Error binding. "+e);
            System.exit(-1);
        }

    }

    public static int est1(String host){
        if(host.equals("")){
            return certificados.size();
        }else{
            for(String a[] : clientes){
                if(a[1].equals(host)){
                    return Integer.parseInt(a[2]);
                }
            }
        }
        return 0;
    }

    public static int est2(String cert){
        
        for(String a[] : certificados){
            if(a[0].equals(cert)){
                return Integer.parseInt(a[1]);
            }
        }

        return 0;
    }

    public static void est3(){
        for(String a[] : servidores){
            System.out.println("Servidor "+a[0]+ " puerto "+a[1]+": "+ a[2]);
        }
    }

    public static void est4(){
        for(String a[] : certificados){
            System.out.println(a[0]);
        } 
    }

    public int signin(String servhost, int servport) throws java.rmi.RemoteException{

        String []a = new String[3];
        a[0]= servhost;
        a[1]= servport+"";
        a[2]= "0";
        servidores.add(a);
        System.out.println("Inscribi el servidor: "+servhost+" con puerto: "+servport);
        return 0;
    }

    public void signout(String servhost, int servport) throws java.rmi.RemoteException{

        if(eraseServ(servhost, servport+"")==1){
            System.err.println("Servidor: "+servhost+" con puerto: "+servport+" no inscrito");
        }else{
            System.err.println("Servidor: "+servhost+" con puerto: "+servport+" eliminado de la lista de servidores");
        }
    }

    public int incli(String host) throws java.rmi.RemoteException{
        String []a = new String[3];
        a[0]=clientes.size()+"";
        a[1]=host;
        a[2]="0";

        clientes.add(a);
        System.out.println("Inscribi al cliente: "+host+" con id: "+a[0]);
        return clientes.size();
    }

    public void outcli(String host, int id) throws java.rmi.RemoteException{
        int h=0; 
        for(String a[] : clientes){
            if(a[0].equals(id+"")){
                break;
            }
            h++;
        }

        if(h>=servidores.size()){
            System.err.println("Cliente id: "+id+" no existe.");
        }else{
            servidores.remove(h);
        }

    }

    public ArrayList<String> searchCert(String query, int id) throws java.rmi.RemoteException{

        ArrayList <String> res = new ArrayList<String>();
        ArrayList <String> certr = null;
        OperServ opserv = null;
        String dirserv = "";
        for(String a[] : servidores){

            dirserv = "rmi://"+a[0]+":"+a[1]+"/servcert";
            try{
                opserv = (OperServ) Naming.lookup(dirserv);
            }catch(MalformedURLException murle) {
                System.out.println();
                System.out.println(
                        "MalformedURLException");
                System.out.println(murle);
            }
            catch(RemoteException re) {
                System.out.println();
                System.out.println(
                        "RemoteException");
                System.out.println(re);
            }
            catch(NotBoundException nbe) {
                System.out.println();
                System.out.println(
                        "NotBoundException");
                System.out.println(nbe);
            }

            certr = opserv.searchCert(query);
            res.addAll(certr);
        }

        for(String a[] : clientes){
            if(a[0].equals(id+"")){
                int cant = Integer.parseInt(a[2]);
                //Certf.log(cant);
                a[2]=(cant+1)+"";
                break;
            }
        }

        /*for(String r : res){
            Certf.log(res);
            
        }*/

        return res;
    }

    public static void main(String args[]){
        int port = 4000;
        ServerSocket busqsocket = null;

        if(args.length==2 && args[0].equals("-p")){
            try{
                port = Integer.parseInt(args[1]);
            }catch(NumberFormatException n){
                System.err.println("Error en las opciones: "+n.getMessage());
                System.exit(1);
            }
        }else if(args.length==1 && args[0].equals("-help")){
            System.out.println("Uso: java buscert [-p <puerto>]");
            System.out.println("\t-p <puerto>: numero del puerto de escucha de buscert, por defecto, 4000.");
            System.exit(0);
        }else if(args.length==0){
            
        }else{
            System.out.println("Error en las opciones. Para ver una ayuda ejecute java buscert -help");
            System.exit(1);
        }

        conectar(port);

        boolean listening = true;
        BufferedReader prompt = null;
        String comando = "";
        String[] est;

        while(listening){
            System.out.print("buscert>: ");
            prompt = new BufferedReader(new InputStreamReader(System.in));
            try{
                comando = prompt.readLine();
            }catch(Exception e){
                System.err.println("Error leyendo el comando. Intente de nuevo.");
            }

            est = comando.split(" ");

            if(est[0].equals("clicert")){
                if(est.length>1)
                    Certf.log("Certificados solicitados: "+est1(est[1]));
                else
                    Certf.log("Certificados solicitados"+est1(""));
            }else if(est[0].equals("solcli")){
                if(est.length>1)
                    Certf.log("Certificados solicitados: "+est2(est[1]));
                else
                    Certf.log("Especifique un cliente");
            }else if(est[0].equals("cerreq")){
                est3(); 
            }else if(est[0].equals("liscert")){
                est4();
            }else{
                System.out.println("Comando no soportado.");
            }
        }

        System.exit(0);
    }
} 
