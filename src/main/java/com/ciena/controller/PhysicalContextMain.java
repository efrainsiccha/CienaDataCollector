package com.ciena.controller;

import com.ciena.controller.dao.Conexion;
import com.ciena.controller.dao.DBRecord;
import com.ciena.controller.dao.DBTable;
import org.json.JSONObject;
import util.Util;

import java.io.IOException;
import java.sql.SQLException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

public class PhysicalContextMain {

    //ES IGUAL A LA CLASE TOPOLOGYMAIN

    private static Conexion.DBConnector dataBase;
    private static DBTable tableDicPhysical;
    private static DBTable tableExpPhysical;

    public PhysicalContextMain() throws SQLException, ClassNotFoundException {
        dataBase = new Conexion.DBConnector();
        tableExpPhysical = dataBase.deleteTableIfExsist("exp_physical");
    }
    //  solo esta para probar mis metodos
    public static void main(final String[] args) throws IOException, SQLException, ClassNotFoundException {
        PhysicalContextMain physical = new PhysicalContextMain();
        physical.analyzeInformationPhysicalContext("D:\\archivos\\objetociena.json","tapi-common:context",
                "tapi-equipment:physical-context");
    }

    public Boolean analyzeInformationPhysicalContext(String fileRoute, String tapiContext, String physicalContext){
        boolean analyze = false;
        boolean insertDictionaryPhysical = false;
        boolean insertMatrixPhysical = false;
        System.out.println("-------------Processing Information From: " + physicalContext + "------- \n");
        try{

            JSONObject totalObjects = Util.parseJSONFile(fileRoute);
            JSONObject objectTopologyContext = Util.returnListPropertiesParentAssociates(totalObjects,
                    tapiContext);

            List<String> columnList = Util.parentObject(objectTopologyContext, physicalContext);
            insertDictionaryPhysical = insertDictionaryPhysical(columnList, dataBase);
            insertMatrixPhysical = insertMatrizPhysical(columnList, dataBase, objectTopologyContext, physicalContext);
            System.out.println("-------------Process executed successfully: " + insertDictionaryPhysical  + "/ "+ insertMatrixPhysical);
            analyze = insertDictionaryPhysical && insertMatrixPhysical ? true : false;

            analyze = true;
        }catch (Exception e) {
            analyze = false;
            System.out.println("-------------Buggy process: " + e.getMessage());
            e.printStackTrace();
        }
        return analyze;
    }

    private boolean insertMatrizPhysical(List<String> columnList, Conexion.DBConnector dataBase,
                                         JSONObject objectTopologyContext, String physicalContext) {
        Map<String, String> exp_Physical = new HashMap<>();
        for (String objects : columnList) {
            String columnName = objects.replaceAll("-", "_").replaceAll(":", "_");
            if (columnName.equals("uuid")) {
                exp_Physical.put(columnName, "varchar(50) primary key");
            } else {
                exp_Physical.put(columnName, "MEDIUMTEXT");
            }
        }
        try{
            tableExpPhysical = Util.createTableMap(dataBase, "exp_physical", tableExpPhysical,
                    exp_Physical);
            DBRecord record = tableExpPhysical.newRecord();
            JSONObject objectTopology = objectTopologyContext.getJSONObject(physicalContext);
            Map<String, Object> objectsMap = objectTopology.toMap();
            record = tableExpPhysical.newRecord();
            for (Map.Entry<String, Object> entry : objectsMap.entrySet()) {
                if (columnList.stream().filter(x -> entry.getKey().equals(x)).findFirst().isPresent()) {
                    record.addField(entry.getKey().replaceAll("-", "_").replaceAll(":", "_"), entry.getValue().toString());
                } else {
                    record.addField(entry.getKey().replaceAll("-", "_").replaceAll(":", "_"), null);
                }
            }
            try {
                tableExpPhysical.insert(record);
            } catch (SQLException exception) {
                exception.printStackTrace();
            }
        }catch (SQLException | ClassNotFoundException e) {
            e.printStackTrace();
            return false;
        }
        return true;
    }

    private boolean insertDictionaryPhysical(List<String> columnList, Conexion.DBConnector dataBase) {
        String[][] dicPhysical = new String[][] { { "id", "int(11) NOT NULL AUTO_INCREMENT PRIMARY KEY" },
                { "atribute_name", "varchar(250)" }};
        columnList = columnList.stream().distinct().collect(Collectors.toList());
        try {
            String tableName = "dic_physical";
            System.out.println("	-------------Creating table: " + tableName);
            tableDicPhysical = Util.createTableDictionary(dataBase, tableName, tableDicPhysical, dicPhysical);
            DBRecord record = tableDicPhysical.newRecord();
            for (String objects : columnList) {
                record = tableDicPhysical.newRecord();
                record.addField("atribute_name", objects);
                tableDicPhysical.insert(record);
            }
        } catch (SQLException | ClassNotFoundException e) {
            return false;
        }
        return true;
    }

}