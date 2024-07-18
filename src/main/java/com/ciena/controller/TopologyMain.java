package com.ciena.controller;

import com.ciena.controller.dao.Conexion;
import com.ciena.controller.dao.Conexion.DBConnector;
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

public class TopologyMain {

	//TENER EN CUENTA QUE ES CASI EL MISMO METODO PARA TODAS LAS CLASES

	//CREO TABLAS Y A LA VEZ UNA CONEXION CON LA DB
	private static Conexion.DBConnector dataBase;
	private static DBTable tableTopology;
	private static DBTable tableDicTopology;

	//CREO UN METODO MAIN PARA PROBAR SI SE EJECUTO TODA LA DATA
	// CON PARAMENTROS COMO LA RUTA DEL ARCHIVO Y TAMBIEN LO QUE QUIERO EXTRAER DEL JSON
	public static void main(String[] args) throws SQLException, ClassNotFoundException {
		TopologyMain main = new TopologyMain();
		main.analyzeInformationTopology("D:\\archivos\\objetociena.json", "tapi-common:context", "tapi-topology:topology-context",
				"topology");
	}
	//UN METODO PARA ELIMINAR LA TABLA Y CREAR AUTOMATICAMENTE
	public TopologyMain() throws SQLException, ClassNotFoundException {
		dataBase = new Conexion.DBConnector();
		tableTopology = dataBase.deleteTableIfExsist("exp_topology");
	}
	//UN METODO DONDE PUEDA A ANALIZAR, POSICIONARME DENTRO DEL JSON
	//CON PARAMETROS QUE QUIERO IDENTIFICAR
	public Boolean analyzeInformationTopology(String fileRoute, String tapiContext, String tapiTopology,
											  String topology) {
		boolean analyze = false;
		boolean insertDictionaryTopology = false;
		boolean insertMatrixTopology = false;
		System.out.println("-------------Processing Information From: " + topology + "------- \n");
		try {
			//ME POSICIONO EN EL LUEGAR QUE QUIERO DENTRO DEL JSON
			//AQUI ME ESTOY POSICIONANDO EN TAPITOPOLOGY
			JSONObject totalObjects = Util.parseJSONFile(fileRoute);
			JSONObject objectTopologyContext = Util.returnListPropertiesParentAssociatesChildNode(totalObjects,
					tapiContext, tapiTopology);

			//AQUI ESTOY TRAYENDO LOS OBJETOS DE TOPOLOGY, POR ESO ME ESTOY POSICIONANDO EN ÉL
			// UTIIZO LA CLASE UTIL PARA REUTILIZAR UN METODO
			List<String> columnList = Util.columnListParentObject(objectTopologyContext, topology);

			//CREO UN METODO DONDE ME INSERTE TANTO EL DICCIONARIO Y MATRIZ
			insertDictionaryTopology = insertDictionaryTopology(columnList, dataBase);

			insertMatrixTopology = insertMatrizTopology(columnList, dataBase, objectTopologyContext, topology);
			System.out.println("-------------Procesando ejecutado con exito: " + insertDictionaryTopology + "/ "
					+ insertMatrixTopology);
			analyze = insertDictionaryTopology && insertMatrixTopology ? true : false;
		} catch (Exception e) {
			analyze = false;
			System.out.println("-------------Procesando con errores: " + e.getMessage());
			e.printStackTrace();
		}

		return analyze;
	}
	// CREO EL METODO DE LA MATRIZ CON CIERTOS PARAMENTROS QUE HE PEDIDO
	// LOS PARAMENTROS PEDIDOS ESTAN DENTRO DEL UTIL
	private boolean insertMatrizTopology(List<String> columnList, DBConnector dataBase, JSONObject evaluateTopology,
										 String topology){
		//HAGO UN MAPEO DE TODO LO QUIERO EXTRAER DEL JSON
		Map<String, String> exp_topology = new HashMap<String, String>();
		//HAGO UN RECORRIDO DE TODA LA TABLA Y PIDO QUE EL UUID SEA UN PRIMARY KEY
		for (String objects : columnList) {
			//COMO EN EL MYSQL NO ME PERMITE EL "-" Y EL ":" TODO ES LO REEMPLAZO POR "_"
			String columnName = objects.replaceAll("-", "_").replaceAll(":", "_");
			if (columnName.equals("uuid")) {
				exp_topology.put(columnName, "varchar(50) primary key");
			} else {
				exp_topology.put(columnName, "MEDIUMTEXT");
			}
		}
		// GRACIAS AL PUT PUEDO AGREGAR UNA NUEVA COLUMNA PARA LA TABLA
		// TAMBIEN PUEDO AÑADIR UNA FK SI ES NECESARIO
		exp_topology.put("uuid_topology_context", "varchar(250)");
		try {
			//CREO LA TABLA Y UTILIZO UN METODO DEL UTIL QUE SIRVE PARA CREAR TABLAS MATRIZ
			tableTopology = Util.createTableMap(dataBase, "exp_topology", tableTopology,
					exp_topology);
			DBRecord record = tableTopology.newRecord();
			//MI NODO PADRE ES UN OBJETO Y YO QUIERO MI NODO HIJO, LO QUE TENDRIA QUE HACER ES POSICIONARME
			//DEBERIA HACER MI NODO PADRE UN ARREGLO Y TRAER A MI HIJO QUE EN ESTE CASO SERIA TOPOLOGYCONTEXT
			JSONArray evaluateTopologyContext = evaluateTopology.getJSONArray(topology);
			//HAGO TODO UN RECORRIDO PARA PODER HACER DESPUES HACER EL MAP Y INSERTARLO A LA DB
			for (Object objectsEvaluate : evaluateTopologyContext) {
				JSONObject objectsEvaluateJson = (JSONObject) objectsEvaluate;
				Map<String, Object> objectsMap = objectsEvaluateJson.toMap();
				record = tableTopology.newRecord();
				for (Map.Entry<String, Object> entry : objectsMap.entrySet()) {
					if (columnList.stream().filter(x -> entry.getKey().equals(x)).findFirst().isPresent()) {
						record.addField(entry.getKey().replaceAll("-", "_").replaceAll(":", "_"),
								entry.getValue().toString());
					} else {
						record.addField(entry.getKey().replaceAll("-", "_").replaceAll(":", "_"), null);
					}
				}
				try {
					//APARTIR DE AQUI ESTOY INSERTANDO LA TABLA
					tableTopology.insert(record);
				} catch (SQLException exception) {
					exception.printStackTrace();
				}
			}
		} catch (SQLException | ClassNotFoundException e) {
			e.printStackTrace();
			return false;
		}
		return true;
	}
	// CREO EL METODO PARA LA TABLA DICCIONARIO
	// CON PARAMETROS QUE HABIA PEDIDO ANTES GRACIAS AL UTIL
	// LO PRIMERO SERA LA CONFIGURACION DE LA TABLA, CON CIERTAS COLUMNAS QUE QUIERO AGREGARLOS
	private boolean insertDictionaryTopology(List<String> columnList, DBConnector dataBase) {
		String[][] dicTopology = new String[][] { { "id", "int(11) NOT NULL AUTO_INCREMENT PRIMARY KEY" },
				{ "atribute_name", "varchar(250)" } };
		columnList = columnList.stream().distinct().collect(Collectors.toList());
		try {
			//NOMBRE LA TABLA
			String tableName = "dic_topology";
			System.out.println("	-------------Creating table: " + tableName);
			//CREO LA TABLA Y UTILIZO UN UTIL QUE SIRVE PARA CREAR LAS TABLAS DICCIONARIO
			tableDicTopology = Util.createTableDictionary(dataBase, tableName, tableDicTopology, dicTopology);

			DBRecord record = tableDicTopology.newRecord();
			for (String object : columnList) {
				record = tableDicTopology.newRecord();
				//HAGO UN RECORRIDO A CIERTA COLUMNA DENTRO DE LA TABLA Y LO LLENO DE DATA
				record.addField("atribute_name", object);
				// Y FINALMENTE LO INSERTO A LA DB
				tableDicTopology.insert(record);
			}
		} catch (SQLException | ClassNotFoundException e) {
			return false;
		}
		return true;
	}
}
