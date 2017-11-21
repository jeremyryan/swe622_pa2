File Sharing System Readme
===========================

The File Sharing System program is implemented as an executable jar file. To start the program, run the java executable
with the pa2.jar file as the argument to the -jar option, and include command line options according to the descriptions
in the Usage section below:

java -jar pa2.jar <arguments>

If the program is run with incorrect input parameters, it will print usage information and exit.

If either the client or server reports a problem with the requested action, the process will print an error
message and exit with an exit status of 1.

The PA2_SERVER environment variable must be set for the client to contact the server. It should have a value
containing the server host name and port separated by a colon: "hostname:port".

Note that all file and directory paths sent in requests to the server will be resolved relative to the directory
in which the server was started.

Usage:

$ java -jar pa2.jar server start <portnumber>
Starts the FSS server listening on the specified port. Requests for files from clients will be interpreted
relative to the directory in which the server was started.

$ java -jar pa2.jar client upload <path_on_client> </path/filename/on/server>
Uploads a file to the server. If the file already exists and the file length reported by the server is less than
the total file length, then it is assumed that a previous upload operation was interrupted and the upload will
be resumed. If the file exists but is the same length or larger than the one to be uploaded, the server
copy will be overwritten. If the uploaded file should overwrite the existing file instead of resuming the upload,
then you will need to delete the existing file from the server and start the upload again.

$ java -jar pa2.jar client download </path/existing_filename/on/server> <path_on_client>
Downloads a file from the server to the specified path. If the file already exists at the specified path and the file
size is less than the total file size reported by the server, then the file download will be resumed. If the file
exists and the file size is equal to or greater than the total, the existing file will be overwritten. If the file
exists but the download should not be resumed, then you will need to delete the existing file and start the download
again.

$ java -jar pa2.jar client dir <path/existing_directory/on/server>
Lists the contents (files and directories) of a directory on the server. If the directory does not exist, an error will
be reported.

$ java -jar pa2.jar client mkdir </path/new_directory/on/server>
Creates a directory on the server.

$ java -jar pa2.jar client rmdir <path/existing_directory/on/server>
Removes a directory from the server. If the directory does not exist or is not empty, an error will be reported.

$ java -jar pa2.jar client rm </path/existing_filename/on/server>
Deletes a file from the server. If the file does not exist, an error will be reported.

$ java -jar pa2.jar client shutdown
Shuts down the server.
