# seProxy

To use _SeProxy_ you should config `seproxy.prop` for your database first, for example:
  ```
  DB_CLASS=mysql
  DB_URL=jdbc:mysql://localhost:3306/<db_name>
  DB_UNAME=root
  DB_PASSWORD=12345
  KEYSTORE_PATH=/path/to/keystore
  KEYSTORE_PASSWORD=<password>
  IS_DB_INIT=true
  ```
  __Notice__: the param `IS_DB_INIT` should be false if your database has been inited. 
  
  
Also SeProxy provide a simple REPL for interactive using, you can use `gradle distTar` get the tarball of the REPL cli.
  
