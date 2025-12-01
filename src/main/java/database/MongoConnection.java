package database;

import com.mongodb.client.MongoClient;
import com.mongodb.client.MongoClients;
import com.mongodb.client.MongoDatabase;

public class MongoConnection {
    private static MongoConnection instance;
    private MongoClient mongoClient;
    private MongoDatabase database;
    
    private static final String CONNECTION_STRING = "mongodb://localhost:27017";
    private static final String DATABASE_NAME = "QCM";
    
    private MongoConnection() {
        try {
            mongoClient = MongoClients.create(CONNECTION_STRING);
            database = mongoClient.getDatabase(DATABASE_NAME);
            System.out.println("Connexion MongoDB établie avec succès!");
        } catch (Exception e) {
            System.err.println("Erreur de connexion MongoDB: " + e.getMessage());
            e.printStackTrace();
        }
    }
    
    public static MongoConnection getInstance() {
        if (instance == null) {
            synchronized (MongoConnection.class) {
                if (instance == null) {
                    instance = new MongoConnection();
                }
            }
        }
        return instance;
    }
    //test
    
    public MongoDatabase getDatabase() {
        return database;
    }
    
    public void close() {
        if (mongoClient != null) {
            mongoClient.close();
        }
    }
}
