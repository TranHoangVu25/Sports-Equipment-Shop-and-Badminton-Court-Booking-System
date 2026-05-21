# GIAI ĐOẠN 1: BUILD (Sử dụng JDK để compile và tạo JAR)
# --------------------------------------------------------
FROM eclipse-temurin:21-jdk-jammy AS build
WORKDIR /app
# Copy các file build config và source code
COPY pom.xml .
COPY src src
# Copy wrapper Maven nếu có
COPY .mvn .mvn
COPY mvnw .

# Thực thi lệnh package để tạo ra file .jar trong thư mục target/
# Giả sử bạn đã chạy `mvnw install` trên máy host để tạo ra wrapper
RUN ./mvnw clean package -DskipTests -Dcheckstyle.skip=true

# GIAI ĐOẠN 2: RUN (Sử dụng JRE nhẹ để chạy JAR)
# ---------------------------------------------
# Sử dụng JRE 21 nhẹ hơn để chạy ứng dụng
FROM eclipse-temurin:21-jre-jammy
WORKDIR /app

# Lấy file JAR từ giai đoạn 'build' và đặt tên là app.jar trong container
# Tên file JAR được lấy là search_service-0.0.1-SNAPSHOT.jar (dựa trên tên bạn cung cấp)
COPY --from=build /app/target/system-0.0.1-SNAPSHOT.jar app.jar

EXPOSE 8086

# Lệnh chạy ứng dụng Spring Boot
ENTRYPOINT ["java", "-jar", "app.jar"]