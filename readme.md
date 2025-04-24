# Sistema de Gerenciamento de Estoque (StockManagement)

Um sistema completo de gerenciamento de estoque desenvolvido com Spring Boot, oferecendo uma solução robusta para controle de produtos, pedidos e clientes.

## 🚀 Funcionalidades

- Gestão de Produtos
    - Cadastro e atualização de produtos
    - Controle de estoque
    - Alertas de estoque baixo via email
    - Cache para otimização de consultas

- Gestão de Pedidos
    - Criação e confirmação de pedidos
    - Adição/remoção de itens
    - Validação automática de estoque

- Gestão de Clientes
    - Cadastro completo de clientes
    - Validação de documentos
    - Histórico de pedidos

## 🛠️ Tecnologias Utilizadas

- **Spring Boot 3.4.4**
- **Spring Cloud 2024.0.1**
- **Banco de Dados**
    - PostgreSQL (principal)
    - MongoDB (dados não estruturados)
    - Redis (cache)

- **Mensageria**
    - RabbitMQ

- **Monitoramento**
    - Spring Actuator
    - Micrometer
    - Prometheus

- **Documentação**
    - SpringDoc OpenAPI 2.3.0

- **Outros**
    - Flyway (migrations)
    - Lombok
    - Jakarta Mail
    - Spring AI

## 📋 Pré-requisitos

- JDK 21+
- Maven 3.9+
- Docker & Docker Compose
- PostgreSQL 15+
- MongoDB 6+
- Redis 7+
- RabbitMQ 3.12+

## 🔧 Configuração

1. Clone o repositório:
   bash git clone [https://github.com/seu-usuario/StockManagement.git](https://github.com/seu-usuario/StockManagement.git)

2. Configure as variáveis de ambiente:
   
# Database
SPRING_DATASOURCE_URL=jdbc:postgresql://localhost:5432/stockmanagement SPRING_DATASOURCE_USERNAME=seu_usuario SPRING_DATASOURCE_PASSWORD=sua_senha
# MongoDB
SPRING_DATA_MONGODB_URI=mongodb://localhost:27017/stockmanagement
# Redis
SPRING_REDIS_HOST=localhost SPRING_REDIS_PORT=6379
# RabbitMQ
SPRING_RABBITMQ_HOST=localhost SPRING_RABBITMQ_PORT=5672 SPRING_RABBITMQ_USERNAME=guest SPRING_RABBITMQ_PASSWORD=guest
# Email
SPRING_MAIL_HOST=smtp.gmail.com SPRING_MAIL_PORT=587 SPRING_MAIL_USERNAME=seu.email@gmail.com SPRING_MAIL_PASSWORD=sua_senha_de_app

3. Execute o Docker Compose:
bash docker-compose up -d

4. Execute a aplicação:
bash ./mvnw spring-boot:run


## 📦 Estrutura do Projeto
## 🔍 Documentação API

A documentação da API está disponível através do Swagger UI:
[http://localhost:8080/swagger-ui.html](http://localhost:8080/swagger-ui.html)

## ⚙️ Monitoramento

- Métricas Prometheus: `http://localhost:8080/actuator/prometheus`
- Health Check: `http://localhost:8080/actuator/health`

## 🧪 Testes

Execute os testes com:
bash ./mvnw test

## 📫 Contribuindo

1. Fork o projeto
2. Crie sua branch de feature (`git checkout -b feature/AmazingFeature`)
3. Commit suas mudanças (`git commit -m 'Add some AmazingFeature'`)
4. Push para a branch (`git push origin feature/AmazingFeature`)
5. Abra um Pull Request

## 📄 Licença

Este projeto está sob a licença MIT. Veja o arquivo [LICENSE](LICENSE) para mais detalhes.

## ✒️ Autores

* **Bruno Britto** - *Desenvolvedor* - [brsalles87](https://github.com/brsalles87)

