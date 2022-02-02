package client;


import manager.OrmManager;

public class App {
    public static void main(String[] args) {
        OrmManager orm = OrmManager.get("H2.db");
    }
}
