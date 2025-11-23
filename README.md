# ShopWave E-Commerce Application

ShopWave is a Spring Boot-based e-commerce application featuring product browsing, shopping cart functionality, user authentication, order management, and an admin interface.

## Table of Contents
- [Prerequisites & Dependencies](#prerequisites--dependencies)
- [Local Setup & Running](#local-setup--running)
- [AWS Deployment (EC2 + RDS)](#aws-deployment-ec2--rds--secrets-manager)
- [GCP Deployment (Compute Engine + Cloud SQL)](#gcp-deployment-compute-engine--cloud-sql--secret-manager)
- [Containerization & Cloud Deployment](#containerization--container-deployment)

---

## Prerequisites & Dependencies

### System Requirements
- **Java Development Kit (JDK)**: Version 17 or higher.
- **Maven**: Version 3.8+ (or use the included `mvnw` wrapper).
- **MySQL**: Version 8.0+.

### Key Dependencies (pom.xml)
- Spring Boot Starter Web
- Spring Boot Starter Data JPA
- Spring Boot Starter Thymeleaf
- Spring Boot Starter Security
- MySQL Connector/J
- Thymeleaf Extras Spring Security

---

## Local Setup & Running

1.  **Clone the Repository**
    ```bash
    git clone <repository-url>
    cd java-springboot-shoppingcart-prj
    ```

2.  **Database Setup**
    - Ensure MySQL is running locally on port `3306`.
    - Create a database named `shoppingcart` (optional, as `createDatabaseIfNotExist=true` is set).
    - Update `src/main/resources/application.properties` if your credentials differ from `root`/`password`.

3.  **Build the Application**
    ```bash
    mvn clean package
    ```

4.  **Run the Application**
    ```bash
    java -jar target/shoppingcart-0.0.1-SNAPSHOT.jar
    ```
    Alternatively, using Maven:
    ```bash
    mvn spring-boot:run
    ```

5.  **Access the Application**
    - Open browser: `http://localhost:8080`
    - **Default Admin User**: You may need to insert an admin user directly into the database or register a user and manually update their role to `ROLE_ADMIN` in the `users` table to access admin features.

---

## AWS Deployment (EC2 + RDS + Secrets Manager)

### 1. Infrastructure Setup
- **RDS (MySQL)**:
  - Create a MySQL RDS instance.
  - Ensure the Security Group allows traffic on port 3306 from your EC2 instance's Security Group.
- **Secrets Manager**:
  - Store database credentials (username, password, url) in a new secret (e.g., `prod/shoppingcart/db`).
- **EC2 Instance**:
  - Launch an Amazon Linux 2023 or Ubuntu instance.
  - Attach an **IAM Role** with permissions to read from Secrets Manager (`secretsmanager:GetSecretValue`).
  - Ensure Security Group allows inbound HTTP (port 80/8080) and SSH (port 22).

### 2. Deployment Steps
1.  **SSH into EC2**:
    ```bash
    ssh -i key.pem ec2-user@<public-ip>
    ```
2.  **Install Java 17**:
    ```bash
    sudo yum install java-17-amazon-corretto -y
    ```
3.  **Fetch Secrets & Run**:
    - Install `jq` and `aws-cli` if not present.
    - Create a startup script (`start.sh`) to fetch secrets and export them as environment variables:
    ```bash
    #!/bin/bash
    SECRET_JSON=$(aws secretsmanager get-secret-value --secret-id prod/shoppingcart/db --query SecretString --output text)
    
    export SPRING_DATASOURCE_URL=$(echo $SECRET_JSON | jq -r .url) # e.g., jdbc:mysql://<rds-endpoint>:3306/shoppingcart
    export SPRING_DATASOURCE_USERNAME=$(echo $SECRET_JSON | jq -r .username)
    export SPRING_DATASOURCE_PASSWORD=$(echo $SECRET_JSON | jq -r .password)
    
    java -jar shoppingcart-0.0.1-SNAPSHOT.jar
    ```
4.  **Copy JAR**: Use `scp` to upload the built JAR file to the EC2 instance.
5.  **Execute**: `chmod +x start.sh && ./start.sh`

---

## GCP Deployment (Compute Engine + Cloud SQL + Secret Manager)

### 1. Infrastructure Setup
- **Cloud SQL (MySQL)**:
  - Create a MySQL instance.
  - Enable **Private IP** for secure access from Compute Engine.
- **Secret Manager**:
  - Create secrets for `DB_USER`, `DB_PASS`, and `DB_URL`.
- **Compute Engine**:
  - Create a VM instance.
  - **Service Account**: Ensure the attached service account has `Secret Manager Secret Accessor` role.
  - **Network**: Ensure it's in the same VPC as Cloud SQL.

### 2. Deployment Steps
1.  **SSH into VM**:
    ```bash
    gcloud compute ssh <instance-name>
    ```
2.  **Install Java 17**:
    ```bash
    sudo apt update
    sudo apt install openjdk-17-jdk -y
    ```
3.  **Fetch Secrets & Run**:
    - Create a startup script:
    ```bash
    #!/bin/bash
    export SPRING_DATASOURCE_USERNAME=$(gcloud secrets versions access latest --secret="DB_USER")
    export SPRING_DATASOURCE_PASSWORD=$(gcloud secrets versions access latest --secret="DB_PASS")
    export SPRING_DATASOURCE_URL=$(gcloud secrets versions access latest --secret="DB_URL")
    
    java -jar shoppingcart-0.0.1-SNAPSHOT.jar
    ```
4.  **Upload & Run**: Upload the JAR and execute the script.

---

## Containerization & Container Deployment

### 1. Docker Setup

**Create a `Dockerfile` in the project root:**
```dockerfile
FROM eclipse-temurin:17-jdk-alpine
VOLUME /tmp
COPY target/shoppingcart-0.0.1-SNAPSHOT.jar app.jar
ENTRYPOINT ["java","-jar","/app.jar"]
```

**Build & Run Locally:**
```bash
docker build -t shopwave-app .
docker run -p 8080:8080 -e SPRING_DATASOURCE_URL=jdbc:mysql://host.docker.internal:3306/shoppingcart -e SPRING_DATASOURCE_USERNAME=root -e SPRING_DATASOURCE_PASSWORD=password shopwave-app
```

### 2. AWS Container Deployment (ECS/Fargate)
1.  **Push Image**:
    - Create an ECR repository.
    - Tag and push your Docker image to ECR.
2.  **Task Definition**:
    - Create a Task Definition in ECS.
    - Add container definition using your ECR image URI.
    - Under **Environment Variables**, map `SPRING_DATASOURCE_URL`, `SPRING_DATASOURCE_USERNAME`, etc.
    - *Best Practice*: Use **ValueFrom** syntax in the environment variables to reference ARN of secrets in AWS Secrets Manager directly.
3.  **Service**:
    - Create a Service using the Task Definition.
    - Configure Network (VPC, Subnets, Security Groups) to allow access to RDS.

### 3. GCP Container Deployment (Cloud Run)
1.  **Push Image**:
    - Tag and push your image to Google Artifact Registry (GAR) or GCR.
    ```bash
    docker tag shopwave-app gcr.io/<project-id>/shopwave-app
    docker push gcr.io/<project-id>/shopwave-app
    ```
2.  **Deploy to Cloud Run**:
    ```bash
    gcloud run deploy shopwave-service \
      --image gcr.io/<project-id>/shopwave-app \
      --platform managed \
      --region us-central1 \
      --allow-unauthenticated \
      --set-env-vars SPRING_DATASOURCE_URL=jdbc:mysql://<cloud-sql-ip>:3306/shoppingcart \
      --set-secrets SPRING_DATASOURCE_PASSWORD=DB_PASS:latest,SPRING_DATASOURCE_USERNAME=DB_USER:latest
    ```
    - *Note*: Ensure the Cloud Run service account has permissions to access the secrets and the Cloud SQL Client role (if using Cloud SQL Auth Proxy connection string).
