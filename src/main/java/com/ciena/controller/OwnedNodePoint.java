package com.ciena.controller;

import com.ciena.controller.dao.Conexion;
import com.ciena.controller.dao.DBRecord;
import com.ciena.controller.dao.DBTable;
import org.json.JSONArray;
import org.json.JSONObject;
import util.Util;
import java.io.IOException;
import java.sql.SQLException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

public class OwnedNodePoint {

	//TENER EN CUENTA QUE ES CASI EL MISMO METODO PARA TODAS LAS CLASES

	//HASTA CIERTO PUNTO ES IGUAL A LA CLASE TOPOLOGYMAIN
	//SEGUIR LEYENDO POR ALGUNAS COSAS NUEVAS AGREGADAS
	private static Conexion.DBConnector dataBase;
	private static DBTable tableOwnedNode;
	private static DBTable tableDicOwnedNode;
	public OwnedNodePoint() throws SQLException, ClassNotFoundException {
		dataBase = new Conexion.DBConnector();
		tableOwnedNode = dataBase.deleteTableIfExsist("exp_topology_node_onep");
	}
	// Se crea un metodo main, para poder ejecutarlo y tambien conectarnos al json
	public static void main(final String[] args) throws IOException, SQLException, ClassNotFoundException {
		OwnedNodePoint mainTopology = new OwnedNodePoint();
		mainTopology.analyzeInformationOwned("D:\\archivos\\objetociena.json", "tapi-common:context",
				"tapi-topology:topology-context", "topology", "node", "owned-node-edge-point");
	}

	public Boolean analyzeInformationOwned(String fileRoute, String tapiContext, String tapiTopology,
										   String topology, String node, String ownedPoint) {
		boolean analyze = false;
		boolean insertDictionaryOwned = false;
		boolean insertMatrixOwned = false;
		System.out.println("-------------Processing Information From: " + ownedPoint + "------- \n");
		try {

			JSONObject totalObjects = Util.parseJSONFile(fileRoute);
			JSONObject objectTopologyContext = Util.returnListPropertiesParentAssociatesChildNode(totalObjects,
					tapiContext, tapiTopology);
			JSONArray topologyArray = objectTopologyContext.getJSONArray(topology);
			JSONObject ownedNode = (JSONObject) topologyArray.get(0);
			JSONArray nodeList = ownedNode.getJSONArray(node);

			List<String> columnsList = Util.columnListParentArray(nodeList, ownedPoint);

			//AÑADO UNAS VARIABLES QUE SERAN UTILIZADAS PARA LA TABLA DICCIONARIO
			//QUE ESTO ME PERMETIRA SABER A QUE TABLA ESTAN RELACIONADAS Y QUE COLUMNA ESTAN RELACIONADAS
			String referenceTable = "exp_topology_node";
			String referenceColumn = "uuid";
			String columnName = "uuid_node";
			insertMatrixOwned = insertMatrizOwnedNode(columnsList, dataBase, nodeList, ownedPoint, referenceTable,referenceColumn,columnName);
			insertDictionaryOwned = insertDictionaryOwned(columnsList, dataBase,referenceTable,referenceColumn,columnName);
			System.out.println("-------------Process executed successfully: " + insertDictionaryOwned  + "/ "+ insertMatrixOwned);
			analyze = insertDictionaryOwned && insertMatrixOwned ? true : false;
		} catch (Exception e) {
			analyze = false;
			System.out.println("-------------Buggy process: " + e.getMessage());
			e.printStackTrace();
		}

		return analyze;
	}

	// CONFIGURO LA TABLA CON ALGUNAS COSAS NUEVAS POR QUE ESTA CLASE TIENE RELACIONES CON OTRAS CLASES
	private Boolean insertDictionaryOwned(List<String> columnList, Conexion.DBConnector dataBase, String referenceTable, String referenceColumn, String columnName) {
		String[][] dicOwnedpoint = new String[][] { { "id", "int(11) NOT NULL AUTO_INCREMENT PRIMARY KEY" },
				{ "atribute_name", "varchar(250)" },{ "flag_fk","int(11)"},{"fk_foreing_object_name","varchar(250)"},
				{"fk_foreing_object_name_atribute","varchar(250)"}};
		columnList = columnList.stream().distinct().collect(Collectors.toList());
		try {
			String tableName = "dic_topology_node_onep";
			System.out.println("	-------------Creating table: " + tableName);
			tableDicOwnedNode = Util.createTableDictionary(dataBase, tableName, tableDicOwnedNode, dicOwnedpoint);
			DBRecord record = tableDicOwnedNode.newRecord();
			for (String objects : columnList) {
				record = tableDicOwnedNode.newRecord();
				record.addField("atribute_name", objects);
				//AQUI ESTOY PIDIENDO QUE EL FK SERIA TODO 0, EXCEPTO QUE EL QUE ESTA RELACIONADO
				record.addField("flag_fk",0);
				tableDicOwnedNode.insert(record);
			}
			//HAGO UN RECORRIDO A LAS TABLAS Y LAS JUNTO A LAS VARIABLES ANTES CREADAS
			record = tableDicOwnedNode.newRecord();
			record.addField("atribute_name",columnName);
			record.addField("flag_fk", 1);
			record.addField("fk_foreing_object_name",referenceTable);
			record.addField("fk_foreing_object_name_atribute",referenceColumn);
			tableDicOwnedNode.insert(record);
		} catch (SQLException | ClassNotFoundException e) {
			return false;
		}
		return true;
	}

	private Boolean insertMatrizOwnedNode(List<String> columnList, Conexion.DBConnector dataBase,
										  JSONArray evaluateNode, String insertDataNodeChild, String referenceTable, String referenceColumn, String columnName) {
		Map<String, String> tablaOwned = new HashMap<String, String>();
		for (String objects : columnList) {
			// INSERTANDO DATA
			String columnNameUuid = objects.replaceAll("-", "_").replaceAll(":", "_");
			if (columnNameUuid.equals("uuid")) {
				tablaOwned.put(columnNameUuid, "varchar(50) primary key");
			} else {
				tablaOwned.put(columnNameUuid, "MEDIUMTEXT");
			}
		}
		//APARTE DE AGREGAR UNA NUEVA COLUMNA A LA TABLA MATRIZ
		//TAMBIEN AGREGO UN FK QUE ME PERMITIRA CREAR LAS RELACIONES
		tablaOwned.put(columnName, "varchar(250) , FOREIGN KEY  (uuid_node) REFERENCES exp_topology_node(uuid)");
		try {
			tableOwnedNode = Util.createTableMap(dataBase, "exp_topology_node_onep", tableOwnedNode,
					tablaOwned);
			DBRecord record = tableOwnedNode.newRecord();
			for (Object objectsNode : evaluateNode) {
				// JSONOBJECT ES PARA TRAER UN OBJETO DEL JSON
				JSONObject ownedNode = (JSONObject) objectsNode;
				// QUIERO ATRAER EL UUID DE OWNEDNODE PARA LUEGO IMPLEMENTARLO EN LA BD
				String columnUuid = ownedNode.get(referenceColumn).toString();
				JSONArray listEdgePoint = ownedNode.getJSONArray(insertDataNodeChild);
				for (Object objectEvaluate : listEdgePoint) {
					JSONObject objectsEvaluateJson = (JSONObject) objectEvaluate;
					Map<String, Object> objectsMap = objectsEvaluateJson.toMap();
					record = tableOwnedNode.newRecord();
					for (Map.Entry<String, Object> entry : objectsMap.entrySet()) {
						if (columnList.stream().filter(x -> entry.getKey().equals(x)).findFirst().isPresent()) {
							record.addField(entry.getKey().replaceAll("-", "_").replaceAll(":", "_"),
									entry.getValue().toString());
						} else {
							record.addField(entry.getKey().replaceAll("-", "_").replaceAll(":", "_"), null);
						}
					}
					record.addField(columnName, columnUuid);
					tableOwnedNode.insert(record);

				}
			}
		} catch (SQLException | ClassNotFoundException e) {
			e.printStackTrace();
			return false;
		}
		return true;
	}
}