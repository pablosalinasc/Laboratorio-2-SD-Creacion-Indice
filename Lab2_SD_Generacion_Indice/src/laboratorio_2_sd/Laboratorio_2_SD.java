/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package laboratorio_2_sd;

import com.mongodb.*;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.util.ArrayList;
import java.util.StringTokenizer;


/**
 *
 * @author pablo salinas
 */

public class Laboratorio_2_SD {
    
    static public void pasarGarbageCollector(){
        Runtime garbage = Runtime.getRuntime();
        garbage.gc();
    }
    static boolean imprime=true;
    
    public static void main(String[] args) throws Exception{

            //archivo de configuracion
            File archivo = new File ("config.ini");
            FileReader fr = new FileReader (archivo);
            BufferedReader br = new BufferedReader(fr);
            String linea = br.readLine();
            int cantParticiones=Integer.parseInt(linea);
            linea = br.readLine();
            String[] data=linea.split("\n");
            String rutaDocumentos=data[0];
            linea = br.readLine();
            data=linea.split("\n");
            String rutaStopwords=data[0];       
            if(imprime)
            System.out.println("Se configura con:\n- Particiones: "+cantParticiones+
                    "\n- Ruta Documentos: '"+rutaDocumentos+
                    "'\n- Ruta StopWords: '"+rutaStopwords+"'\n");
            
            //Archivo stopwords
            File archivo3 = new File (rutaStopwords);
            FileReader fr3 = new FileReader (archivo3);
            BufferedReader br3 = new BufferedReader(fr3);
            ArrayList<String> stopwords=new ArrayList<>();
            if(imprime){
                System.out.println("StopWords: \n");
                int contador=0;
                while((linea = br3.readLine())!=null&&linea.length()!=0){//mientras no sea EOF
                    stopwords.add(linea);
                    if(contador<9){
                        System.out.print(linea+" ");
                        contador++;
                    }else if (contador==9){
                        contador=0;
                        System.out.println(linea);
                    }
                }
                System.out.println("");
            }
            //Crea db y tablas
            MongoClient mongoClient = new MongoClient( "localhost" , 27017 );
            mongoClient.dropDatabase("indexDB");
            DB db = mongoClient.getDB( "indexDB" );            
            mongoClient.setWriteConcern(WriteConcern.JOURNALED);
            db.setWriteConcern(WriteConcern.JOURNALED);
            
            DBCollection colDocumentos = db.getCollection("Documentos");
            DBCollection colIndiceInvertido = db.getCollection("IndiceInvertido");
            DBCollection colVocabulario = db.getCollection("Vocabulario");
            
            colDocumentos.createIndex( new BasicDBObject("idDoc", 1));
            colIndiceInvertido.createIndex( new BasicDBObject("idPalabra", 1));
            colVocabulario.createIndex( new BasicDBObject("palabra", 1));
            
            //Archivo de documentos
            File archivo2 = new File (rutaDocumentos);
            FileReader fr2 = new FileReader (archivo2);
            BufferedReader br2 = new BufferedReader(fr2);
            
            int idDoc=0;
            int idPalabraActual=0;
            
            while((linea = br2.readLine())!=null&&!linea.contains("</mediawiki>")){//mientras no sea EOF

                while(!linea.contains("<page>")){
                    linea=br2.readLine();
                }
                //guarda el titulo
                linea=br2.readLine();
                int indice=linea.indexOf(">");
                String sub=linea.substring(indice+1);
                indice=sub.indexOf("<");
                String titulo=sub.substring(0,indice);
                
                //guarda el username
                while(!linea.contains("<username>")){
                    linea=br2.readLine();
                }
                indice=linea.indexOf(">");
                sub=linea.substring(indice+1);
                indice=sub.indexOf("<");
                String username=sub.substring(0,indice);
                
                while(linea.contains("<text")==false){
                    linea=br2.readLine();
                }
                
                //Aqui comienza a leer el contenido del documento
                ArrayList<String> palabrasTemp = new ArrayList<String>();
                
                while(linea.contains("</text>")==false){
                    
                    linea=br2.readLine();
                    
                    if(!linea.contains("</text>")){
                        StringTokenizer st = new StringTokenizer(linea, " #%_•-*·.,;:|/\\(){}[]=&+'\"?¿¡!");
                        while(st.hasMoreTokens()) {
                            String palabra = st.nextToken();
                            palabra=palabra.toLowerCase();
                            if(palabra.length()>1&&!stopwords.contains(palabra)){
                                palabrasTemp.add(palabra);
                            }
                        }
                    }
                    
                    
                }
                Documento docTemp=new Documento(idDoc,palabrasTemp,titulo,username);
                if(imprime)docTemp.print();
                //Se agrega el documento directamente a la coleccion documentos
                colDocumentos.insert(docTemp.toDBObject());
                
                
                for(int i=0;i<docTemp.cantPalabras;i++){
                    
                    String palabra=docTemp.palabras.get(i);
                    if(imprime)System.out.println("***********************");
                    if(imprime)System.out.println("Palabra: "+palabra);
                    //revisa si la palabra esta en la coleccion vocabulario
                    DBCursor cursor = colVocabulario.find((DBObject) new BasicDBObject("palabra", palabra));
                    if(cursor.hasNext()){ //si esta
                        if(imprime)System.out.println("Esta en vocabulario");
                        Vocabulario vocTemp=new Vocabulario((BasicDBObject) cursor.next());
                        if(imprime)System.out.println("idPalabra: "+vocTemp.idPalabra);
                        DBCursor cursor2 = colIndiceInvertido.find((DBObject) new BasicDBObject("idPalabra", vocTemp.idPalabra));
                        BasicDBObject find=(BasicDBObject) cursor2.next();
                        IndiceInvertido indiceTemp=new IndiceInvertido(find);
                        //revisa si ya está ingresado el documento actual en el indiceInvertido
                        int contador=0;
                        int frec=0;
                        for(int j=0;j<indiceTemp.frecuencias.size();j++){
                            if(indiceTemp.frecuencias.get(j).idDocumento==idDoc){
                                contador=1;
                                frec=indiceTemp.frecuencias.get(j).frecuencia;
                                break;
                            }                            
                        }
                        if(contador==1){ //si encontro el id del documento actual
                            if(imprime)System.out.println("Esta en indice invertido");
                            //actualizar frecuencia en indice Invertido
                            indiceTemp.ActualizarFrecuencias(frec+1,idDoc);
                            colIndiceInvertido.update(find, indiceTemp.toDBObject(), false, true);
                            if(imprime)indiceTemp.print();
                        }else{//si no está
                            if(imprime)System.out.println("No está en indice invertido");
                            //actualizar la cantidad de documentos del vocabulario
                            vocTemp.cantDocumentos++;
                            colVocabulario.insert(vocTemp.toDBObject());
                            if(imprime)vocTemp.print();
                            //agregar nueva tupla de frecuencia/idDoc a indice
                            indiceTemp.ActualizarFrecuencias(1, idDoc);
                            if(imprime)indiceTemp.print();
                            colIndiceInvertido.insert(indiceTemp.toDBObject());

                        }
                    }else{//no esta
                        if(imprime)System.out.println("No esta en vocabulario\nInserta nuevo elemento");
                        if(idDoc==0 && i==0){ //no se ha insertado ningun dato
                            //inserta palabra en vocabulario
                            Vocabulario vocTemp=new Vocabulario(palabra,0,1);
                            colVocabulario.insert(vocTemp.toDBObject());
                            if(imprime)vocTemp.print();
                            //inserta entrada en indice invertido
                            IndiceInvertido indiceTemp=new IndiceInvertido(vocTemp.idPalabra,1,idDoc);
                            colIndiceInvertido.insert(indiceTemp.toDBObject());
                            if(imprime)indiceTemp.print();
                            idPalabraActual++;
                        }
                        else{
                            //se obtiene un nuevo id
                            //se inserta a vocabulario
                            Vocabulario vocTemp=new Vocabulario(palabra,idPalabraActual,1);
                            colVocabulario.insert(vocTemp.toDBObject());
                            if(imprime)vocTemp.print();
                            //se inserta a indice invertido
                            IndiceInvertido indiceTemp=new IndiceInvertido(vocTemp.idPalabra,1,idDoc);
                            if(imprime)indiceTemp.print();
                            colIndiceInvertido.insert(indiceTemp.toDBObject());
                            idPalabraActual++;
                        }
                    }
                }
                
                idDoc++;
                while(!linea.contains("</page>")){
                    linea=br2.readLine();
                }
                pasarGarbageCollector();
            }

        
    }
}
