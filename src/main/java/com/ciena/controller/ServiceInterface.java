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

public class ServiceInterface {

    //ESTA CLASE LO AÑADI PARA EL NUEVO JSON Y ES IGUAL A LA CLASE TOPOLOGYMAIN

    private static Conexion.DBConnector dataBase;
    private static DBTable tableService;
    private static DBTable tableDicService;

    public ServiceInterface() throws SQLException, ClassNotFoundException {
        dataBase = new Conexion.DBConnector();
        tableService = dataBase.deleteTableIfExsist("exp_service_interface");
    }
    public static void main(String[] args) throws SQLException, ClassNotFoundException {
        ServiceInterface serviceInterface = new ServiceInterface();
        serviceInterface.analyzeInformationConecctivity("D:\\archivos\\nuevoJSON.json","tapi-common:context",
                "service-interface-point");
    }
    public Boolean analyzeInformationConecctivity(String fileRoute, String tapiContext, String serviceInterface){
        boolean analyze = false;
        boolean insertDictionaryService = false;
        boolean insertMatrixService = false;
        System.out.println("-------------Processing Information From: " + serviceInterface + "------- \n");
        try{
            JSONObject totalObjectsJSON = Util.parseJSONFile(fileRoute);
            JSONObject objectService = Util.returnListPropertiesParentAssociates(totalObjectsJSON,tapiContext);

            List<String> columnList = Util.columnListParentObject(objectService, serviceInterface);

            insertDictionaryService = insertDictionaryService(columnList, dataBase);
            insertMatrixService = insertMatrizService(columnList, dataBase, objectService, serviceInterface);

            System.out.println("-------------Process executed successfully: " + insertDictionaryService + "/ "
                    + insertMatrixService);
            analyze = insertDictionaryService && insertMatrixService ? true : false;

        } catch (Exception e) {
            analyze = false;
            System.out.println("-------------Buggy process: " + e.getMessage());
            e.printStackTrace();
        }
        return analyze;
    }

    private boolean insertMatrizService(List<String> columnList, Conexion.DBConnector dataBase, JSONObject objectService, String serviceInterface) {
        Map<String, String> exp_device_interface = new HashMap<>();
        for (String objects : columnList) {
            String columnName = objects.replaceAll("-", "_").replaceAll(":", "_");
            if (columnName.equals("uuid")) {
                exp_device_interface.put(columnName, "varchar(50) primary key");
            } else {
                exp_device_interface.put(columnName, "MEDIUMTEXT");
            }
        }
        try{
            tableService = Util.createTableMap(dataBase, "exp_service_interface", tableService,
                    exp_device_interface);
            DBRecord record = tableService.newRecord();
            JSONArray evaluateService = objectService.getJSONArray(serviceInterface);
            for (Object objectEvaluate : evaluateService) {
                JSONObject objectsEvaluateJson = (JSONObject) objectEvaluate;
                Map<String, Object> objectsMap = objectsEvaluateJson.toMap();
                record = tableService.newRecord();
                for (Map.Entry<String, Object> entry : objectsMap.entrySet()) {
                    if (columnList.stream().filter(x -> entry.getKey().equals(x)).findFirst().isPresent()) {
                        record.addField(entry.getKey().replaceAll("-", "_").replaceAll(":", "_"),
                                entry.getValue().toString());
                    } else {
                        record.addField(entry.getKey().replaceAll("-", "_").replaceAll(":", "_"), null);
                    }
                }
                try {
                    tableService.insert(record);
                } catch (SQLException exception) {
                    exception.printStackTrace();
                }
            }
        }catch (SQLException | ClassNotFoundException e) {
            e.printStackTrace();
            return false;
        }
        return true;
    }

    private boolean insertDictionaryService(List<String> columnList, Conexion.DBConnector dataBase) {
        String[][] dicService = new String[][] { { "id", "int(11) NOT NULL AUTO_INCREMENT PRIMARY KEY" },
                { "atribute_name", "varchar(250)" } };
        columnList = columnList.stream().distinct().collect(Collectors.toList());
        try{
            String tableName = "dic_service_interface";
            System.out.println("	-------------Creating table: " + tableName);
            tableDicService = Util.createTableDictionary(dataBase, tableName, tableDicService, dicService);

            DBRecord record = tableDicService.newRecord();
            for (String objects : columnList) {
                record = tableDicService.newRecord();
                record.addField("atribute_name", objects);
                tableDicService.insert(record);
            }
        }catch (SQLException | ClassNotFoundException e) {
            return false;
        }
        return true;
    }
}
