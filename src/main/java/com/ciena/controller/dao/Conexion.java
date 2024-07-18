package com.ciena.controller.dao;

import java.sql.*;

public class Conexion {
    public static class DBConnector {
        //AQUI ESTOY CONECTANDOME A LA DB CREADA
        final static String dbAddress = "localhost";  //todo: move them to properties
        final static String dbName = "db_ciena_collecter";
        final static String dbUser = "root";
        final static String dbPassword = "gokussj50A";
        Connection conn;

        public DBConnector() throws SQLException, ClassNotFoundException {
            System.out.println("Writing to database");
            Class.forName("com.mysql.cj.jdbc.Driver");
            System.out.println("Mysql driver registered");
            final String dbUrl = "jdbc:mysql://" + dbAddress + "/" + dbName + "?serverTimezone=UTC";

            conn = DriverManager.getConnection(dbUrl, dbUser, dbPassword);

        }

        public DBTable deleteTableIfExsist(String tableName) throws SQLException {
            return new DBTable(tableName, conn);
        }

        public void update(String query) throws SQLException {

            PreparedStatement stat = conn.prepareStatement(query);
            stat.executeUpdate();
        }

        public Connection getConexion() {
            return conn;
        }

        public void cerrarConexion() throws SQLException {
            conn.close();
        }
    }
}


