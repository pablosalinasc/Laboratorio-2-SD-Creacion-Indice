Instrucciones de ejecución:

Primero se debe abrir MongoDB en un servidor local en el puerto 27017 (por defecto), y luego abrir el proyecto de NetBeans y correr el archivo "Laboratorio_2_SD.java". En la consola aparecerá información de los datos que se están almacenando.

Instrucciones de configuración:

El archivo "config.ini" posee los parámetros de entrada del programa. Cada parámetro debe estar en un linea independiente y se debe dejar una última linea sin caracteres. El primer parámetro corresponde a la cantidad de particiones que va a manejar el índice invertido. El segundo parámetro corresponde a la ruta del archivo .xml con los documentos de la base de datos de wikipedia. Y finalmente el tercer parámetro corresponde a la ruta del archivo con los stopwords que va a manejar el sistema.

Información Importante:
Si corre MongoDB en un versión de 32 bit, asegurese de usar el comando "mongod --journal"