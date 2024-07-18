# CienaDataCollector
Ciena data retrieve

Tools:
- Intellij Idea Program
- JDK 11
- JSON library

NOTE:
- Within the code of the TopologyMain class, you can find a broader documentation of what each one does
- Inside the OwnedNodePoint class, you can find some new things added and the rest is equal to TopologyMain
- All classes is equal to OwnedNodePoint and TopologyMain

- Description of the Application:

  Read, parse, and populate, all using JSON. While creating
  tables both matrix and dictionary, everything would be filled within
  a DB.

- What does the application do:

  It makes it easy to read, parse and populate a JSON. Everything would be done with
  just add a new JSON.

- Execution of the application:
  - For that I am using the command console "gitbash"
  - We put the command "mvn clean package"
  - Then we add the command "java -cp cienaInsert-0.0.1-SNAPSHOT.jar interfaz.Interfaz"

- Explanation of all the code:


 - Explanation of the interface:

   The interface is a window where a JSON file can be uploaded dynamically. It is connected with the methods of Topology and Physical.

- Explanation of the connection with the Database:

  We have a class "Connection" that is connected to the Database where it has the IP address, the name of the DB, the user and the password.

- Explanation of the Util class:

  Here we can find some methods that will be reusable for all methods. For example: "returnListPropertiesParentAssociates" which is a method where the "Thirdnode" is   an Object. That it would no longer be necessary to do a whole code, if not just by calling it we would be using it

- Explanation of the code:

  * To explain the code we have to bear in mind that we are using the JSON library that we can find in the "pom.xml" this will help me to position myself within it. *

 In the class "TopologyMain" we can find how the code is made. We start with a "Main" method that has as parameters a "file path, grandfather node, father node, child   node" and this serves to test all the code.

 Then we have a method that can be used to delete the table and then create it.

 Then we have a method to be able to analyze and position ourselves within JSON. For example "AnalyzeInformationTopology" that we have as parameters "file path,         grandfather node, father node, child node". From the try catch is where we position ourselves within the JSON, in this case with the JSONObject of the file path and   as we look within the JSON most of them are objects so we will use "JSONObject" and it is an arrangement we use a "JSONArray".

 We continue making a List of columns and use the Util class to reuse some method. From there we create some methods that are for the Dictionary and the Matrix (It is   everything that brings an Object or Arrangement)

- Explanation of the Dictionary table:

  First we have to configure a table with columns that we want to have, second we name the table, third we use a method of the Util class to be able to create it in     this case "createTableDictionary". Then we do a whole tour of the columns that we have left and we finally insert it into the DB.

- Explanation of the Matrix table:

 First we make a Map to bring everything from a place in the JSON, then we go through the list of columns and validate that we want the UUID to be its primaryKey,       also if necessary with a "put" we can add a new column in the table and we could add a "Foreign Key" if necessary. Then to create the table we use a Util method       that is "createTableMap". From there we go through the JSON and make a whole Map and finally insert it into the DB.


