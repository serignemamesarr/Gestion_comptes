package com.xyz.comptesManagement;

import java.sql.*;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

public class Program {

    public static Connection connect() throws SQLException {
        String url = "jdbc:mysql://localhost:3306/banque";
        String user = "serigne";
        String password = "fallou1999";
        return DriverManager.getConnection(url, user, password);
    }

    public static void creerCompte(Compte compte) {
        try (Connection connection = connect()) {
            String insertClientQuery = "INSERT INTO clients (nom, prenom) VALUES (?, ?)";
            try (PreparedStatement clientStatement = connection.prepareStatement(insertClientQuery, PreparedStatement.RETURN_GENERATED_KEYS)) {
                clientStatement.setString(1, compte.getClient().getNom());
                clientStatement.setString(2, compte.getClient().getPrenom());
                clientStatement.executeUpdate();

                try (ResultSet generatedKeys = clientStatement.getGeneratedKeys()) {
                    if (generatedKeys.next()) {
                        int clientId = generatedKeys.getInt(1);

                        String insertCompteQuery = "INSERT INTO comptes (numero, solde_courant, id_client) VALUES (?, ?, ?)";
                        try (PreparedStatement compteStatement = connection.prepareStatement(insertCompteQuery)) {
                            compteStatement.setString(1, compte.getNumero());
                            compteStatement.setDouble(2, compte.getSoldeCourant());
                            compteStatement.setInt(3, clientId);
                            compteStatement.executeUpdate();
                        }
                    } else {
                        throw new SQLException("Échec de la création du client, aucun ID généré.");
                    }
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    public static void realiserOperation(int compteId, Operation operation) {
        try (Connection connection = connect()) {
            double soldeCourant = recupererSoldeCompte(connection, compteId);
            if (operation.getType().equalsIgnoreCase("versement")) {
                soldeCourant += operation.getMontant();
            } else if (operation.getType().equalsIgnoreCase("retrait")) {
                soldeCourant -= operation.getMontant();
            }

            String insertOperationQuery = "INSERT INTO operations (date_operation, type_operation, montant, id_compte) VALUES (?, ?, ?, ?)";
            try (PreparedStatement operationStatement = connection.prepareStatement(insertOperationQuery)) {
                operationStatement.setDate(1, new java.sql.Date(operation.getDate().getTime()));
                operationStatement.setString(2, operation.getType());
                operationStatement.setDouble(3, operation.getMontant());
                operationStatement.setInt(4, compteId);
                operationStatement.executeUpdate();
            }

            mettreAJourSoldeCompte(connection, compteId, soldeCourant);

        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    private static double recupererSoldeCompte(Connection connection, int compteId) throws SQLException {
        String selectCompteQuery = "SELECT solde_courant FROM comptes WHERE id = ?";
        try (PreparedStatement compteStatement = connection.prepareStatement(selectCompteQuery)) {
            compteStatement.setInt(1, compteId);
            try (ResultSet resultSet = compteStatement.executeQuery()) {
                if (resultSet.next()) {
                    return resultSet.getDouble("solde_courant");
                } else {
                    throw new SQLException("Compte non trouvé.");
                }
            }
        }
    }

    private static void mettreAJourSoldeCompte(Connection connection, int compteId, double soldeCourant) throws SQLException {
        String updateSoldeQuery = "UPDATE comptes SET solde_courant = ? WHERE id = ?";
        try (PreparedStatement updateSoldeStatement = connection.prepareStatement(updateSoldeQuery)) {
            updateSoldeStatement.setDouble(1, soldeCourant);
            updateSoldeStatement.setInt(2, compteId);
            updateSoldeStatement.executeUpdate();
        }
    }

    public static Compte afficherDetailCompte(int compteId) {
        try (Connection connection = connect()) {
            String selectCompteQuery = "SELECT * FROM comptes WHERE id = ?";
            try (PreparedStatement compteStatement = connection.prepareStatement(selectCompteQuery)) {
                compteStatement.setInt(1, compteId);
                try (ResultSet resultSet = compteStatement.executeQuery()) {
                    if (resultSet.next()) {
                        String selectOperationsQuery = "SELECT * FROM operations WHERE id_compte = ?";
                        try (PreparedStatement operationsStatement = connection.prepareStatement(selectOperationsQuery)) {
                            operationsStatement.setInt(1, compteId);
                            try (ResultSet operationsResultSet = operationsStatement.executeQuery()) {
                                List<Operation> operations = new ArrayList<>();
                                while (operationsResultSet.next()) {
                                    Date date = operationsResultSet.getDate("date_operation");
                                    String type = operationsResultSet.getString("type_operation");
                                    double montant = operationsResultSet.getDouble("montant");
                                    operations.add(new Operation(date, type, montant));
                                }

                                String numero = resultSet.getString("numero");
                                double soldeCourant = resultSet.getDouble("solde_courant");
                                int clientId = resultSet.getInt("id_client");

                                String selectClientQuery = "SELECT * FROM clients WHERE id = ?";
                                try (PreparedStatement clientStatement = connection.prepareStatement(selectClientQuery)) {
                                    clientStatement.setInt(1, clientId);
                                    try (ResultSet clientResultSet = clientStatement.executeQuery()) {
                                        if (clientResultSet.next()) {
                                            String nom = clientResultSet.getString("nom");
                                            String prenom = clientResultSet.getString("prenom");
                                            Client client = new Client(clientId, nom, prenom);

                                            System.out.println("Nom du client : " + client.getNom() + " " + client.getPrenom());
                                            System.out.println("Numéro du compte : " + numero);
                                            System.out.println("Solde courant : " + soldeCourant);
                                            System.out.println("Opérations associées : " + operations);

                                            return new Compte(compteId, numero, operations, soldeCourant, client);
                                        } else {
                                            System.out.println("Client non trouvé.");
                                        }
                                    }
                                }
                            }
                        }
                    } else {
                        System.out.println("Compte non trouvé.");
                    }
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return null;
    }

    public static void main(String[] args) {
        // Test de création d'un compte
        Client client = new Client(1, "John", "Doe");
        Compte compte = new Compte(1, "123456", new ArrayList<>(), 1000.0, client);
        creerCompte(compte);

        // Test de réalisation d'opération (versement)
        Operation versement = new Operation(new Date(), "versement", 500.0);
        realiserOperation(1, versement);

        // Test de réalisation d'opération (retrait)
        Operation retrait = new Operation(new Date(), "retrait", 200.0);
        realiserOperation(1, retrait);

        // Afficher les détails du compte
        afficherDetailCompte(1);
    }
}
 

