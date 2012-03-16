import java.net.*;
import java.io.*;
import java.util.*;
//import nanoxml.*;

public class servThread implements Runnable{

    Socket Client;
    List<Certf> Cert;

    public servThread(Socket client, List<Certf> cert){
        
        this.Client=client;
        this.Cert = cert;
    }

    public void run(){

        try{
            PrintWriter out = null;
            BufferedReader in = null; 

            String fromBusq = "";
            String query = "";
            Certf c = new Certf();
            ArrayList<String []> q = new ArrayList<String[]>();
            ArrayList<String> encontrados = new ArrayList<String>();

            try{
                out = new PrintWriter(Client.getOutputStream(), true);
                in = new BufferedReader(new InputStreamReader(Client.getInputStream()));
            }catch (IOException e){
                System.err.println("Error obteniendo input/output stream");    
            }

            fromBusq=in.readLine();
            
            if(fromBusq!=null && fromBusq.equals("BUSCADOR")){
                while(!(fromBusq = in.readLine()).equals("ENDBUSCADOR")){
                    //Certf.log("fromBusq "+fromBusq);
                    //Leo el query
                    query = query+fromBusq;

                    Certf.log("Recibi una solicitud");

                    //Transformo el xml en query manejable
                    c.xml2query(query,q);

                    /*for(int i=0; i<q.size(); i++){
                        String a[]=q.get(i);
                        //c.log(a.length);
                        for(int j=0; j<a.length; j++){
                            Certf.log(a[j]);
                        }
                    }*/

                    //Busco los Certificados
                    c.searchCert(q,Cert,encontrados);
                    Certf.log("Certificados encontrados:");
                    for(String r : encontrados){
                        Certf.log(r);
                    }

                    if(encontrados.size()>0){
                        out.println("YESCERT");
                        //Envio los Certificados
                        for(String t : encontrados){
                            t = c.cert2xml(t);
                            out.println("BEGIN");
                            out.println(t);
                            out.println("END");  
                        }
                        out.println("ENDCERT");
                    }else{
                        out.println("NOCERT");
                    }
                }
            }else{
                System.err.println("Error en el protocolo");
            }
                    
        }catch(IOException e){
            System.err.println("Error aceptando la conexion");
        }
    }
}
