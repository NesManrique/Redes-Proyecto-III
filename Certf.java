import nanoxml.*; 
import java.io.*;
import java.util.*;

/**
 * @author Nestor Manrique
 * @author Samuel Bartoli
 */

public class Certf{

    String Fname;
    ArrayList<String> Fields;

    /**
    * Constructor vacio
    */
    public Certf(){};

    /**
    * Constructor no vacio recibe
    * @param fname: nombre del certificado
    * @param fields: campos del certificado
    */
    public Certf(String fname, ArrayList<String> fields){
        Fname=fname;
        Fields=fields;
    }

    /**
    * Imprime un certificado
    */
    public void printcert(){
        log(this.Fname);

        ListIterator<String> it = this.Fields.listIterator();
        while(it.hasNext()){
            log(it.next());
        }

    }

    /**
    * Imprime los certificados de una lista de certificados
    * @param l: lista de certificados
    */
    public static void printList(List<Certf> l){
        ListIterator<Certf> it = l.listIterator();

        while(it.hasNext()){
            it.next().printcert();
            log("\n");
        }
    }

    /**
    * Obtiene las informaciones que se encuentran codificadas dentro del
    * certificado
    * @param fname: nombre del certificado
    *
    */
    public Certf descifrado(String fname){
        String[] s = new String[6];
        ArrayList<String> ss = new ArrayList<String>();

        try{
            String[] command = {"openssl x509 -noout -in " + fname + " -issuer",
                "openssl x509 -noout -in " + fname + " -subject",
                "openssl x509 -noout -in " + fname + " -hash",
                "openssl x509 -noout -in " + fname + " -fingerprint",
                "openssl x509 -noout -in " + fname + " -dates",
            } ;

            for(int i=0; i<command.length; i++){
                Process child = Runtime.getRuntime().exec(command[i]);

                BufferedReader stdInput = new BufferedReader(new 
                        InputStreamReader(child.getInputStream()));

                if(i==4){
                    if((s[i]=stdInput.readLine())==null){
                        return null;
                    }else{
                        s[i]=s[i].replaceFirst("[\\w ]+=\\s*","");
                    }

                    if((s[i+1]=stdInput.readLine())==null){
                        return null;
                    }else{
                        s[i+1]=s[i+1].replaceFirst("[\\w ]+=\\s*","");
                    }
                }else{
                    if((s[i]=stdInput.readLine())==null){
                        return null;
                    }else{
                        s[i]=s[i].replaceFirst("[\\w ]+=\\s*","");
                    }
                }
                
                stdInput.close();
            }

            ss.add(s[0]);
            ss.add(s[1]);
            ss.add(s[4]);
            ss.add(s[5]);
            ss.add(s[2]);
            ss.add(s[3]);

            Certf c = new Certf(fname,ss);

            return c;

        } catch(IOException e){
            System.out.println("Error");
            return null;
        }

    }

    /**
    * Contruye una lista con las informaciones que se encuentran codificadas
    * para cada certifico que se encuentre en un directorio.
    * @param dir: nombre del directorio
    * @param L: lista de certificados
    *
    */
    public void abrirdir(File dir,List<Certf> L) throws NullPointerException{
        Certf cer;
        File files[] = dir.listFiles();
        if(files == null){
            throw new NullPointerException();
        }
        for(int i = 0; i< files.length; i++ ){
            if(files[i].isFile()){
                if((cer=descifrado(files[i].getPath()))!=null){
                    L.add(cer);
                }else{
                    continue;
                }
            } else if(files[i].isDirectory()){
                abrirdir(files[i],L);
            }
        }
    }

    /**
    * Funcion auxiliar que sintetiza la impresion en pantalla.
    */
    public static void log(Object aMessage){
        System.out.println(aMessage);
    }

    /**
     * 
     * Construye un xml a partir de un certificado
     * @param filecert: nombre del certificado
     *
     */
    public static String cert2xml(String filecert){
        String all = "";
        try{
            BufferedReader input = new BufferedReader(new FileReader(filecert));
            String line = null;
            while((line = input.readLine())!=null){
                all = all + line + "\n"; 
            }
            input.close();
        }catch(IOException ex){
            log("Error abriendo el archivo: "+filecert);
        }
        XMLElement certf = new XMLElement();
        XMLElement certname = new XMLElement();
        certf.setName("Certificate");
        certname.setName(filecert.replaceAll("([\\w-]*/)+",""));
        certname.setContent(all);
        certf.addChild(certname);
        all = certf.toString();
        if(all.contains("&#xa;")){
            all = all.replace("&#xa;","\n");
        }
        return all;
    }

    /**
     * conviert un xml a un certificado
     * @param dir: directorio del certificado
     * @param certxml: xml a convertir
     */
    public static void xml2cert(String dir, String certxml){
        String fname="";
        String content="";
        String aux="";
        int i=0;
        Vector<XMLElement> v = new Vector<XMLElement>();

        XMLElement certf = new XMLElement();
            certf.parseString(certxml);

        v = (Vector<XMLElement>)certf.getChildren();

        fname = ((XMLElement)v.get(0)).getName();
        content = ((XMLElement)v.get(0)).getContent();

        try {
            BufferedWriter out = new BufferedWriter(new FileWriter(dir+fname));
            log(fname);
            out.write(content);
            out.close();
        } catch (IOException e) {
            log("Problema Escribiendo el archivo: "+fname+" "+e.getMessage());
        }
    }

    public static String xmlName(String certxml){
        Vector<XMLElement> v = new Vector<XMLElement>();

        XMLElement certf = new XMLElement();
            certf.parseString(certxml);

        v = (Vector<XMLElement>)certf.getChildren();

        return ((XMLElement)v.get(0)).getName();

    }

    public boolean hasChildren(XMLElement x, String a){
        Vector<XMLElement> v = (Vector<XMLElement>)x.getChildren();
        if(v!=null){
            for(int i=0; i<v.size(); i++){
                if(a.equals(((XMLElement)v.get(i)).getName())){
                    return true;
                }
            }
        }
        
        return false;
    }

    /**
    * construye el xml a partir de la busqueda del usuario
    * @param query: busqueda con campos y sus valores
    */
    public static String query2xml(String query){
		String[] subs;
        if(query.trim().equals("")){
			return "";
		}else
			subs = query.trim().split("\\s+");

        XMLElement qy = new XMLElement();
        XMLElement iss = new XMLElement();
        XMLElement sub = new XMLElement();
        XMLElement vala = new XMLElement();
        XMLElement valb = new XMLElement();
        XMLElement hash = new XMLElement();
        XMLElement fing = new XMLElement();
        qy.setName("Query");
        iss.setName("Issuer");
        sub.setName("Subject");
        vala.setName("DatesA");
        valb.setName("DatesB");
        hash.setName("Hash");
        fing.setName("Fingerprint");

        for(int i=0; i<subs.length; i++){

            if(i+1<subs.length && subs[i+1].endsWith(":")) continue;
            if(i==subs.length-1 && subs[i].endsWith(":")) continue; 
 
            if(subs[i].equalsIgnoreCase("Issuer:")){
                if(iss.getContent()!=""){
                    iss.setContent((iss.getContent())+" "+subs[++i]);
                }else{
                    iss.setContent(subs[++i]);
                }
 
            }else if(subs[i].equalsIgnoreCase("Subject:")){
                if(sub.getContent()!=""){
                    sub.setContent((sub.getContent())+" "+subs[++i]);
                }else{
                    sub.setContent(subs[++i]);
                }
            }else if(subs[i].equalsIgnoreCase("Dates:")){
                i=i+1;
                int flag=0;
                //Mientras no termine con :
                while(i<subs.length && !subs[i].endsWith(":")){
                    //Si empieza con notBefore
                    if(subs[i].startsWith("notBefore=")){
                        //Si el nodo valb esta vacio
                        valb.setContent(subs[i++].substring(10));
                        flag=1;
                    }else if(subs[i].startsWith("notAfter=")){
                        //Si el nodo valb esta vacio
                        vala.setContent(subs[i++].substring(9));
                        flag=2;
                    }else{
                        if(flag==1){
                            valb.setContent((valb.getContent())+" "+subs[i++]);
                        }else if(flag==2){
                            vala.setContent((vala.getContent())+" "+subs[i++]);
                        }else{
                            if(valb.getContent()!=""){
                                valb.setContent((valb.getContent())+" "+subs[i]);
                            }else{
                                valb.setContent(subs[i]);
                            }

                            if(vala.getContent()!=""){
                                vala.setContent((vala.getContent())+" "+subs[i]);
                            }else{
                                vala.setContent(subs[i]);
                            }
                            i=i+1;
                        }
                    }
                }
                i=i-1;
            }else if(subs[i].equalsIgnoreCase("Hash:")){
                if(hash.getContent()!=""){
                    hash.setContent((hash.getContent())+" "+subs[++i]);
                }else{
                    hash.setContent(subs[++i]);
                }
            }else if(subs[i].equalsIgnoreCase("Fingerprint:")){
                if(fing.getContent()!=""){
                    fing.setContent((fing.getContent())+" "+subs[++i]);
                }else{
                    fing.setContent(subs[++i]);
                }
            }else{
            }
        }

        qy.addChild(iss);
        qy.addChild(sub);
        qy.addChild(valb);
        qy.addChild(vala);
        qy.addChild(hash);
        qy.addChild(fing);

        Vector<XMLElement> v = new Vector<XMLElement>();
        v = (Vector<XMLElement>)qy.getChildren();

        for(XMLElement e : v){
           if(!e.getContent().equals("")){
             String res = qy.toString();
             return res;
            }
        }

        return "";
    }

    /**
    * Extrae los valores de los campos que esten en el xml y los almacena como
    * un arreglo de string
    * @param xml: busqueda en formato xml
    * @param query: contiene los valores de los campos 
    *
    */
    public static void xml2query(String xml, ArrayList<String[]> query){
        XMLElement q = new XMLElement();
        q.parseString(xml); //Transformo el query en un arbol
        Vector childs = q.getChildren(); //Saco los hijos de la raiz
        String content;
        String scont[];
        
        if(childs!=null){
            //Recorro los campos y guardo los valores de los campos
            //como un arraylist de arreglos de string
            for(int i=0; i<childs.size(); i++){
                content = ((XMLElement)childs.get(i)).getContent();
                    scont = content.split("\\s+");
                    query.add(scont);
            }
        }

        
    }

    /**
    * busca y devuelve el certificado correspondiente a la soliticud de acuerdo
    * a los campos de busqueda
    * @param query: informacion para buscar el certificado
    * @param cert: lista que contiene los certificados
    * @param find: arreglo con los certificados que hacen match con los
    * parametros de busqueda
    */
    public static void searchCert(List<String[]> query, List<Certf> cert, 
                                List<String> find){
        String a[];
        Certf c;

        ListIterator<Certf> cer = cert.listIterator();
        ListIterator<String[]> str = query.listIterator();

        //Recorro la lista de certf en el servidor
        int k=0;
        while(cer.hasNext()){
            c = cer.next();
            int i=0;
            boolean aflag=false, vflag=false;
            //int aflag=0;
            //Recorro la lista de atributos
            while(str.hasNext()){
                a = str.next();
                //Recorro la lista de valores para cada atributo
                for(int j=0; j<a.length; j++){

                    if(a[j]=="") continue;

                    if(c.Fields.get(i)!="" && !(c.Fields.get(i)).contains(a[j])){
                        vflag=true;
                        break;
                    }
                }

                if(vflag){
                    aflag=true;
                    vflag=false;
                    break;
                }
                i++;
            }

            if(aflag){
                aflag=false;
            }else{
                find.add(c.Fname);
            }
            
            str = query.listIterator();
            k++;
            i=0;
        }

    }

}
