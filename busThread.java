import java.net.*;
import java.io.*;
import java.util.*;
//import nanoxml.*;

public class busThread implements Runnable{

    Socket client=null;
    List<String []> Serv;
    List<String []> Cli;
    List<String []> Cert;
    int type;

    public busThread(Socket client, List<String []> serv, List<String []> cli, List<String[]> cert, int f){
        this.client=client;
        this.Serv=serv;
        this.Cli=cli;
        this.Cert=cert;
        this.type=f;
    }

    public busThread(List<String []> serv, List<String []> cli, List<String[]> cert, int f){
        this.Serv=serv;
        this.Cli=cli;
        this.Cert=cert;
        this.type=f;
    }

    public busThread(List<String []> serv, int f){
        this.Serv=serv;
        this.type=f;
    } 

    public void certEst(String cert){
        Certf c = new Certf();
        String[] cert_= new String[2];
        cert_[0]= c.xmlName(cert);
        //Certf.log(cert_[0]);
        int h = 0;

        for(String a[] : Cert){
            if(a[0].equals(cert_[0])){
                int cant = Integer.parseInt(a[1]);
                //Certf.log(cant);
                a[1]=(cant+1)+"";
                break;
            }
            h++;
        }

        //Certf.log(h);
        if(h==Cert.size()){
            cert_[1]="1";
            Cert.add(cert_);
        }
    }

    public void cliEst(String[] cliente){
        int h = 0;

        for(String a[] : Cli){
            if(a[0].equals(cliente[0]) && a[1].equals(cliente[1])){
                a[2]=(Integer.parseInt(a[2])+1)+"";
                break;
            }
            h++;
        }

    }

    public void run(){

        if(this.type==2){
            PrintWriter outclient = null;
            PrintWriter outserv = null;
            BufferedReader inclient = null; 
            BufferedReader inserv = null;
            Socket server=null; 

            String inputLine, outputLine;
            String fromClient = "";
            String query = "";
            String fromServ = "";
            String c="";
            int ncs=0;

            try{
                outclient = new PrintWriter(this.client.getOutputStream(), true);
                inclient = new BufferedReader(new InputStreamReader(this.client.getInputStream()));
            }catch (IOException e){
                System.err.println("Error obteniendo input/output stream");    
            }

            String[] cliente = new String[3]; 
            cliente[0] = client.getInetAddress().toString().replace("/","");
            cliente[1] = client.getPort()+"";
            cliente[2] = "0";
            Cli.add(cliente);

            while(true){
                try{
                    //Leemos el input del socket
                    query="";
                    fromClient = inclient.readLine();
                    if(fromClient!=null && fromClient.equals("CLIENTE")){

                        //Leemos el query
                        while(!(fromClient=inclient.readLine()).equals("ENDCLIENTE")){

                            if(fromClient.equals("END")){
                                break;
                            }else{
                                query = query+fromClient;
                            }
                        }

                        //Certf.log("Llego el query: "+query);

                        //Enviamos el query a los servidores
                        for(int i=0; i < Serv.size(); i++){
                            try{
                                server = new Socket(Serv.get(i)[0], Integer.parseInt(Serv.get(i)[1]));
                                outserv = new PrintWriter(server.getOutputStream(), true);
                                inserv = new BufferedReader(new InputStreamReader(
                                            server.getInputStream()));}
                            catch(UnknownHostException u){
                                System.err.println("Error en el serv con puerto: "+
                                        Serv.get(i)[1]);
                                //System.exit(1);
                                continue;
                            }catch(IOException y){
                                System.err.println("Couldn't get I/O for the connection to: "+
                                        Serv.get(i)[0]);
                                //System.exit(1);
                                continue;
                            }

                            outserv.println("BUSCADOR");
                            outserv.println(query); 
                            outserv.println("ENDBUSCADOR");

                            //Leemos respuesta del servidor
                            fromServ=inserv.readLine();

                            if(fromServ.equals("YESCERT")){

                                //Mientras no termine de enviar todos los certificados
                                while(!(fromServ=inserv.readLine()).equals("ENDCERT")){

                                    if(fromServ.equals("BEGIN")){
                                        while(!(fromServ=inserv.readLine()).equals("END")){
                                            //Leemos el certificado
                                            c=c+fromServ;
                                        }

                                        //Certf.log("Llego el certificado: "+c+"\n");

                                        //Estadistica del certificado
                                        certEst(c);
                      
                                        //Estadistica del Cliente
                                        cliEst(cliente);

                                        //Estadistica del Servidor
                                        Serv.get(i)[3]=(Integer.parseInt(Serv.get(i)[3])+1)+"";

                                        //Enviamos certificado al cliente
                                        outclient.println("BUSCADOR");
                                        outclient.println(c);
                                        outclient.println("ENDCERT");

                                        c="";
                                    }
                                }

                            }else if(fromServ.equals("NOCERT")){
                                //Si este servidor no tiene certificados aumento un contador
                                ncs++;
                            }

                            outserv.close();
                            inserv.close();
                            server.close();
                        }

                        if(ncs==this.Serv.size()){
                            //Si ningun servidor tenia el certificado lo digo
                            outclient.println("NOCERT");
                            ncs=0;
                        }

                        outclient.println("ENDBUSCADOR");

                    }else if(fromClient!=null && fromClient.equals("SERVER")){

                        fromClient = inclient.readLine();
                        if(fromClient.equals("SIGNIN")){
                            String[] info = new String[4];
                            info[0] = client.getInetAddress().toString().replace("/","");
                            info[1] = inclient.readLine();
                            info[2] = "1";
                            info[3] = "0";

                            int i=0;
                            for(String[] a : Serv){
                                if(a[0].equals(info[0]) && a[1].equals(info[1])){
                                    a[2]="1";
                                    a[3]="0";
                                    break;
                                }
                                i++;
                            }
                           
                            if(i==Serv.size()){        
                                Serv.add(info);
                                Certf.log("Añadi el servidor "+info[0]+" "+info[1]+"\n");
                            }

                            /*for(i=0; i<Serv.size(); i++){
                                String a[]=Serv.get(i);
                                //Certf.log(a.length);
                                for(int j=0; j<a.length; j++){
                                    Certf.log(a[j]);
                                }
                            }*/
                            
                            break;
                        }
                    }else if(fromClient!=null && fromClient.equals("SALIDA")){
                        break;
                    }else if(fromClient==null){
                        //System.err.println("fromclient es null");
                        break;
                    }

                }catch(IOException e){
                    System.out.println("Error procesando datos buscert");
                    break;
                }

            }

            try{
                outclient.close();
                inclient.close();
                this.client.close();
            }catch(IOException e){
                System.err.println("Error cerrando conexion");
                System.exit(1);
            }

            Cli.remove(cliente);

            //System.out.println("termine con una solicitud");

        }else if(this.type==1){
            //Rutina de estadisticas
            BufferedReader stdIn = new BufferedReader(new InputStreamReader(System.in));
            String userInput="";
            String arg[];
            boolean flag=true;

            try{
                while((userInput = stdIn.readLine())!=null){

                    flag=true;
                    arg = userInput.split(" ");

                    if(arg.length==1 && arg[0].equals("clicert")){
                        Certf.log("La cantidad de certificados consultados es: "+Cert.size()+"\n");

                    }else if(arg.length==2 && arg[0].equals("clicert")){
                        for(String[] a : this.Cli){
                            if(arg[1].equals(a[0]) /*&& arg[2].equals(a[1])*/){
                                Certf.log("La cantidad de certificados consultados por "+a[0]+ " fue: "+ a[2] + "\n");
                                flag=false;
                                break;
                            }
                        }
                        if(flag)
                        Certf.log("El cliente no esta conectado\n");

                    }else if(arg.length==2 && arg[0].equals("solcli")){
                        for(String[] a : this.Cert){
                            if(a[0].equals(arg[1])){
                                Certf.log("\nLa cantidad de clientes que solicitaron fue: "+a[1]);
                                flag=false;
                                break;
                            }
                        }
                        if(flag)
                        Certf.log("El certificado no ha sido solicitado\n");

                    }else if(arg[0].equals("cerreq")){
                        if(this.Serv.size()==0){
                            Certf.log("No hay servidores listados\n");
                            continue;
                        }
                        for(String[] a : this.Serv){
                            if(a[2].equals("1")){
                                Certf.log("La cantidad de certificados consultados por "+a[0]+ " fue: "+ a[3]+"\n");
                            }
                        }

                    }else if(arg[0].equals("liscert")){
                        if(this.Cert.size()==0){
                            Certf.log("No hay certificados que listar\n");
                            continue;
                        }
                        for(String[] a : this.Cert){
                            Certf.log(a[0]);
                        }

                    }else{
                        System.err.println("algun error");
                    }
                }
            }catch(IOException e){
                System.err.println("Error leyendo query de estadistica");
            }
        }else if(this.type==0){

            Socket server;
            PrintWriter outserv;
            BufferedReader inserv;

            while(true){

                try{
                    Thread.sleep(10000);
                }catch(InterruptedException t){
                    Certf.log(t.getMessage());
                }

                Certf.log("Chequeando servidores");

                for(int i=0; i < Serv.size(); i++){
                    try{
                        server = new Socket(Serv.get(i)[0], Integer.parseInt(Serv.get(i)[1]));
                        outserv = new PrintWriter(server.getOutputStream(), true);
                        inserv = new BufferedReader(new InputStreamReader(
                                    server.getInputStream()));
                    }catch(UnknownHostException u){
                            System.err.println("Error en el serv con puerto: "+
                                    Serv.get(i)[1]);
                            continue;
                    }catch(IOException y){
                            Serv.get(i)[2]="0";
                    }

                }


            }
        }
    }
}
