package br.com.brittosw.stockmanagement.config;

import com.zaxxer.hikari.HikariConfig;
import com.zaxxer.hikari.HikariDataSource;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import javax.sql.DataSource;

@Slf4j
@Configuration
public class RdsConnectionConfig {

    @Value("${spring.datasource.url}")
    private String jdbcUrl;

    @Value("${spring.datasource.username}")
    private String username;

    @Value("${spring.datasource.password}")
    private String password;


    @Bean
    public DataSource dataSource() {
        HikariConfig config = new HikariConfig();

        log.info("Tentando conectar ao banco de dados em: {}", jdbcUrl);

        // Configuração explícita do driver
        config.setDriverClassName("org.postgresql.Driver");
        config.setJdbcUrl(jdbcUrl);
        config.setUsername(username);
        config.setPassword(password);

        // Configurações de conexão
        config.addDataSourceProperty("sslmode", "disable");
        config.setMaximumPoolSize(10);
        config.setMinimumIdle(2);
        config.setIdleTimeout(300000);
        config.setConnectionTimeout(20000);
        config.setConnectionTestQuery("SELECT 1");
        config.addDataSourceProperty("ssl", "true");
        config.addDataSourceProperty("sslmode", "require");

        // Debug
        config.addDataSourceProperty("loggerLevel", "TRACE");

        log.info("Configuração do DataSource concluída. Tentando criar pool de conexões...");

        try {
            return new HikariDataSource(config);
        } catch (Exception e) {
            log.error("Erro ao criar pool de conexões: ", e);
            throw e;
        }
    }
}
