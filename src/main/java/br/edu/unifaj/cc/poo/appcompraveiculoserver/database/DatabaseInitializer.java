package br.edu.unifaj.cc.poo.appcompraveiculoserver.database;

import br.edu.unifaj.cc.poo.appcompraveiculoserver.util.ConnectionFactory;

import java.sql.Connection;
import java.sql.Statement;

public class DatabaseInitializer {

    public static void init() {
        try (Connection conn = ConnectionFactory.getConnection();
             Statement stmt = conn.createStatement()) {

            // Tabela Carro com todas as informações da entidade
            stmt.executeUpdate("""
                CREATE TABLE IF NOT EXISTS Carro (
                    id INT AUTO_INCREMENT PRIMARY KEY,
                    carro_nome VARCHAR(100),
                    carro_cor VARCHAR(50),
                    carro_ano INT,
                    carro_valor FLOAT
                );
            """);

            // Tabela Moto com todas as informações da entidade
            stmt.executeUpdate("""
                CREATE TABLE IF NOT EXISTS Moto (
                    id INT AUTO_INCREMENT PRIMARY KEY,
                    moto_nome VARCHAR(100),
                    moto_cor VARCHAR(50),
                    moto_ano INT,
                    moto_valor FLOAT
                );
            """);

            // Tabela Login com todas as informações da entidade
            stmt.executeUpdate("""
                CREATE TABLE IF NOT EXISTS Login (
                    id INT AUTO_INCREMENT PRIMARY KEY,
                    nome VARCHAR(100),
                    senha VARCHAR(100),
                    telefone INT,
                    carteira INT
                );
            """);

            // Inserir dados iniciais
            stmt.executeUpdate("""
                INSERT INTO Carro (carro_nome, carro_cor, carro_ano, carro_valor) 
                VALUES 
                ('Maverick V8', 'Azul', 1978, 55000),
                ('Opala Diplomata', 'Preto', 1985, 30000),
                ('Fiat Uno', 'Vermelho', 2006, 15000)
            """);

            stmt.executeUpdate("""
                INSERT INTO Moto (moto_nome, moto_cor, moto_ano, moto_valor) 
                VALUES 
                ('Hornet 600', 'Vermelha', 2011, 25000),
                ('Twister 250', 'Prata', 2008, 10000),
                ('Intruder 125', 'Preta', 2002, 6000)
            """);

            stmt.executeUpdate("""
                INSERT INTO Login (nome, senha, telefone, carteira) 
                VALUES ('henrique', 'henrique', 1234, 1000)
            """);

            System.out.println("✅ Banco de dados H2 inicializado com sucesso!");

        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}