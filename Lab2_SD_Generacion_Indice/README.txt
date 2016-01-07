Instrucciones de ejecuci�n:

Primero se debe abrir MongoDB en un servidor local en el puerto 27017 (por defecto), y luego abrir el proyecto de NetBeans y correr el archivo "Laboratorio_2_SD.java". En la consola aparecer� informaci�n de los datos que se est�n almacenando.

Instrucciones de configuraci�n:

El archivo "config.ini" posee los par�metros de entrada del programa. Cada par�metro debe estar en un linea independiente y se debe dejar una �ltima linea sin caracteres. El primer par�metro corresponde a la cantidad de particiones que va a manejar el �ndice invertido. El segundo par�metro corresponde a la ruta del archivo .xml con los documentos de la base de datos de wikipedia. Y finalmente el tercer par�metro corresponde a la ruta del archivo con los stopwords que va a manejar el sistema.

Informaci�n Importante:
Si corre MongoDB en un versi�n de 32 bit, asegurese de usar el comando "mongod --journal"