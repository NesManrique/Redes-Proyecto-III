//import nanoxml.*;
import java.io.*;
import java.net.*;

public class clicert{

    public static void main(String args[]) throws IOException{

        String dir = null;
        String bushost = null;
        int busport = 4000;
        
        Socket socket = null;
        PrintWriter out = null;
        BufferedReader in = null;

        if(args.length==6 && args[0].equals("-d") && args[2].equals("-h") && args[4].equals("-p")){
            dir = args[1];
            bushost = args[3];
            busport = Integer.parseInt(args[5]);
        }else if(args.length==4 && args[0].equals("-d") && args[2].equals("-h")){
            dir = args[1];
            bushost = args[3];
        }else if(args.length==1 && args[0].equals("-help")){
            System.out.println("Uso: java clicert -d <directorio de certificados descargados> -h <nombre de dominio, o direccion de ip de buscert>");
            System.out.println("\t\t[-p <puerto de buscCert>]\n");
            System.out.println("\t<directorio de certificados descargados>: contiene el camino absoluto o relativo \n\t\t del directorio donde estarán los certificados digitales descargados por el cliente.");
            System.out.println("\t<nombre de dominio, o direccion de ip de buscert>: maquina donde se encuentra corriendo el buscador");
            System.out.println("\t<puerto de busCert>: puerto de escucha del buscador busCert. 4000 por default.");
            System.exit(0);
        }else{
            System.out.println("Error en las opciones. Para ver una ayuda ejecute java clicert -help");
            System.exit(1);
        }

        if(dir.charAt(dir.length()-1)!='/') dir = dir+"/";
        System.out.println("dir dond guardar los certificados "+dir
                            +"\nip o nombre del buscador "+bushost
                            +"\npuerto de escucha del buscador "+busport+"\n");

       try{
            socket = new Socket(bushost, busport);
            out =  new PrintWriter(socket.getOutputStream(),true);
            in = new BufferedReader(new InputStreamReader(socket.getInputStream()));
       }catch(UnknownHostException e){
            System.err.println("Erro al conctarse al host: "+bushost);
            System.exit(1);
        }catch(IOException e){
            System.err.println("Couldn't get I/O for the connection to: "+bushost);
            System.exit(1);
        }

        BufferedReader stdIn = new BufferedReader(new InputStreamReader(System.in));

        String userInput="";
        String query="";
        String fromBus="";
        String certific="";
        Certf cc = new Certf();
        int ln=0;

        while((userInput = stdIn.readLine())!=null){
            if(userInput.equals("")){
                if(ln==0){
                    ln=ln+1;
                    System.out.println("Si inserta otra linea vacía el programa clicert terminara.");
                }else if(ln==1){
                    out.println("SALIDA");
                    //System.out.println("Clicer terminara ahora.");
                    System.exit(0);
                }
            }else{
                ln=0;

                //Transformar la entrada en XML
                query = cc.query2xml(userInput);
                //Certf.log("cliente"+query);

                if(query.equals("")){
                    Certf.log("Solicitud inválida. Inténtelo de nuevo.");
                    continue;
                }
            
                //Enviarla al buscert
                out.println("CLIENTE");
                out.println(query);
                out.println("ENDCLIENTE");
                
                //Recibir el Certificado
                while(!(fromBus=in.readLine()).equals("ENDBUSCADOR")){ 
                    if(fromBus.equals("BUSCADOR")){
                        fromBus = in.readLine();
                        certific = certific+fromBus;
                    }else if(fromBus.equals("ENDCERT")){
                        //Imprimir el certificado
                        Certf.log("Certificado guardado: "+ cc.xmlName(certific) + "\n");
                        //Guardar el certificado
                        cc.xml2cert(dir,certific);
                        certific="";
                    }else if(fromBus.equals("NOCERT")){
                        Certf.log("No se encontraron certificados con la búsqueda especificada");
                    }else{
                        fromBus = "";
                    }
                }

            }
        }

        out.close();
        in.close();
        stdIn.close();
        socket.close();
    }
}
