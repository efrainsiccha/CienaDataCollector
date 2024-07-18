package com.ciena.controller;

import com.ciena.controller.dao.Conexion;
import com.ciena.controller.dao.DBRecord;
import com.ciena.controller.dao.DBTable;
import org.json.JSONArray;
import org.json.JSONObject;
import util.Util;
import java.sql.SQLException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

public class NodeMain {

    //ES IGUAL QUE LA CLASE OWNEDNODEPOINT

    private static Conexion.DBConnector dataBase;
    private static DBTable tableNode;
    private static DBTable tableDicNode;

    public NodeMain() throws SQLException, ClassNotFoundException {
        dataBase = new Conexion.DBConnector();
        tableNode = dataBase.deleteTableIfExsist("exp_topology_node");
    }
    public static void main(String[] args) throws SQLException, ClassNotFoundException {
        NodeMain main = new NodeMain();
        main.analyzeInformationNode("D:\\archivos\\objetociena.json","tapi-common:context",
                "tapi-topology:topology-context","topology","node");
    }
    public Boolean analyzeInformationNode(String fileRoute, String tapiContext, String tapiTopology,
                                          String topology, String node) {
        boolean analyze = false;
        boolean insertDictionaryNode = false;
        boolean insertMatrixNode = false;
        System.out.println("-------------Processing Information From: " + node + "------- \n");
        try {

            JSONObject totalObjects = Util.parseJSONFile(fileRoute);
            JSONObject objectTopologyContext = Util.returnListPropertiesParentAssociatesChildNode(totalObjects,
                    tapiContext, tapiTopology);
            JSONArray topologyArray = objectTopologyContext.getJSONArray(topology);

            List<String> columnsList = Util.columnListParentArray(topologyArray, node);
            String referenceTable = "exp_topology";
            String referenceColumn = "uuid";
            String columnNameUuid = "uuid_topology";
            insertMatrixNode = insertMatrizNode(columnsList, dataBase, topologyArray,node,referenceTable,referenceColumn,columnNameUuid);
            insertDictionaryNode = insertDictionaryNode(columnsList, dataBase,referenceTable,referenceColumn,columnNameUuid);
            System.out.println("-------------Process executed successfully: " + insertDictionaryNode  + "/ "+ insertMatrixNode);
            analyze = insertDictionaryNode && insertMatrixNode ? true : false;
        } catch (Exception e) {
            analyze = false;
            System.out.println("-------------Buggy process: " + e.getMessage());
            e.printStackTrace();
        }
        return analyze;
    }

    private boolean insertMatrizNode(List<String> columnsList, Conexion.DBConnector dataBase, JSONArray evaluateNode, String node, String referenceTable, String referenceColumn, String columnNameUuid) {
        Map<String, String> exp_Node = new HashMap<>();
        for (String objects : columnsList) {
            // INSERTANDO DATA
            String columnName = objects.replaceAll("-", "_").replaceAll(":", "_");
            if (columnName.equals(referenceColumn)) {
                exp_Node.put(columnName, "varchar(50) primary key");
            } else {
                exp_Node.put(columnName, "MEDIUMTEXT");
            }
        }
        exp_Node.put(columnNameUuid, "varchar(250) , FOREIGN KEY (uuid_topology) REFERENCES exp_topology(uuid)");
        try{
            tableNode = Util.createTableMap(dataBase, "exp_topology_node", tableNode, exp_Node);
            DBRecord record = tableNode.newRecord();
            for (Object objectsNode : evaluateNode){
                JSONObject topologyUuid = (JSONObject) objectsNode;
                String columnUuid = topologyUuid.get(referenceColumn).toString();
                JSONArray listNode = topologyUuid.getJSONArray(node);
                for (Object objectEvaluate : listNode){
                    JSONObject objectEvaluateJson = (JSONObject) objectEvaluate;
                    Map<String, Object> objectMap = objectEvaluateJson.toMap();
                    record = tableNode.newRecord();
                    for (Map.Entry<String, Object> entry : objectMap.entrySet()){
                        if (columnsList.stream().filter(x -> entry.getKey().equals(x)).findFirst().isPresent()){
                            record.addField(entry.getKey().replaceAll("-","_").replaceAll(":","_"), entry.getValue().toString());
                        } else {
                            record.addField(entry.getKey().replaceAll("-","_").replaceAll(":","_"), null);
                        }
                    }
                    try {
                        record.addField(columnNameUuid, columnUuid);
                        tableNode.insert(record);

                    } catch (SQLException exception) {
                        exception.printStackTrace();
                    }
                }
            }
        }catch (SQLException | ClassNotFoundException e) {
            e.printStackTrace();
            return false;
        }
        return true;
    }

    private boolean insertDictionaryNode(List<String> columnList, Conexion.DBConnector dataBase, String referenceTable, String referenceColumn, String columnNameUuid) {
        String[][] dicTopology = new String[][] { { "id", "int(11) NOT NULL AUTO_INCREMENT PRIMARY KEY" },
                { "atribute_name", "varchar(250)" },{ "flag_fk","int(11)"},{"fk_foreign_object_name","varchar(250)"},
                {"fk_foreign_object_name_atribute","varchar(250)"}};
        columnList = columnList.stream().distinct().collect(Collectors.toList());
        try{
            String tableName = "dic_topology_node";
            System.out.println("	-------------Creating table: " + tableName);
            tableDicNode = Util.createTableDictionary(dataBase, tableName, tableDicNode, dicTopology);
            DBRecord record = tableDicNode.newRecord();
            for (String objetos : columnList) {
                record = tableDicNode.newRecord();
                record.addField("atribute_name", objetos);
                record.addField("flag_fk",0);
                tableDicNode.insert(record);
            }
            record = tableDicNode.newRecord();
            record.addField("atribute_name",columnNameUuid);
            record.addField("flag_fk", 1);
            record.addField("fk_foreign_object_name",referenceTable);
            record.addField("fk_foreign_object_name_atribute",referenceColumn);
            tableDicNode.insert(record);
        } catch (Exception e) {
            System.out.println("-------------Buggy process: " + e.getMessage());
            e.printStackTrace();
        }
        return true;
    }

}
