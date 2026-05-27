
## Содержание

- [Функциональность](#функциональность)
- [Структура проекта](#структура-проекта)
- [Требования](#требования)
- [Зависимости](#зависимости)
- [Сборка](#сборка)
- [Запуск](#запуск)
- [API](#api)
- [Примеры](#примеры)
- [Тестирование](#тестирование)

## Функциональность

- Управление продавцами
- Управление транзакциями
- Аналитика
- REST API

## Зависимости

**Core:**
- Spring Boot v4
- Spring Data JPA
- Spring Boot Validation
- Lombok

**Database:**
- H2 (тесты)
- PostgreSQL (продакшен)

**Testing:**
- JUnit 5
- Mockito
- Spring Boot Test
- JaCoCo

**Other:**
- Gradle

## Сборка

```bash
# в корне проекта
./gradlew build
```

**Выходные файлы:**
- `build/classes/java/` — скомпилированные классы
- `build/libs/crm-*.jar` — JAR файл
- `build/reports/jacoco/test/html/index.html` — отчёт покрытия

## Запуск

### Запускаем бд

```bash
docker compose up -d
```

### Запускаем приложение

```bash
./gradlew bootRun
```
или так

```bash
java -jar build/libs/crm-*.jar
```

Приложение доступно: **http://localhost:8080**

### Конфигурация

`src/main/resources/application.properties`

## API

### Продавцы

| Метод | Endpoint | Описание |
|-------|----------|---------|
| `GET` | `/api/sellers` | Все продавцы |
| `GET` | `/api/sellers/{id}` | Продавец по ID |
| `POST` | `/api/sellers` | Создать |
| `PUT` | `/api/sellers/{id}` | Обновить |
| `DELETE` | `/api/sellers/{id}` | Удалить |

### Транзакции

| Метод | Endpoint | Описание |
|-------|----------|---------|
| `GET` | `/api/transactions` | Все транзакции |
| `GET` | `/api/transactions/{id}` | Транзакция по ID |
| `POST` | `/api/transactions` | Создать |
| `PUT` | `/api/transactions/{id}` | Обновить |
| `DELETE` | `/api/transactions/{id}` | Удалить |

### Аналитика

| Метод | Endpoint | Описание |
|-------|----------|-----------|
| `GET` | `/api/analytics/best-seller` | Самый продуктивный продавец за период |
| `GET` | `/api/analytics/sellers-below-threshold` | Продавцы с суммой ниже порога |
| `GET` | `/api/analytics/best-period/{sellerId}` | Наилучший период активности продавца (*) |

(*) Ищет наилучший **по плотности** период среди возможных `[DAY, MONTH, QUARTER, YEAR]`, причем с границами, соответствующие периоду, например месяц начинается только с первого числа, а год с января.

## Примеры

### Создать продавца

```bash
curl -X POST http://localhost:8080/api/sellers \
  -H "Content-Type: application/json" \
  -d '{
    "name": "Leon Kozeev",
    "contactInfo": "leon.kozeev@gmail.com"
  }'
```

Response:
```json
{
  "id": 1,
  "name": "Leon Kozeev",
  "contactInfo": "leon.kozeev@gmail.com",
  "registrationDate": "27-05-2026T22:57:18"
}

```

### Получить всех продавцов

```bash
curl -X GET http://localhost:8080/api/sellers
```

Response:
```json
[
  {
    "id": 1,
    "name": "Leon Kozeev",
    "contactInfo": "leon.kozeev@gmail.com",
    "registrationDate": "27-05-2026T22:57:18"
  },
  {
    "id": 2,
    "name": "Lada Kozeeva",
    "contactInfo": "lada.kozeeva@gmail.com",
    "registrationDate": "27-05-2026T22:58:38"
  }
]
```

### Создать транзакцию

```bash
curl -X POST http://localhost:8080/api/transactions \
  -H "Content-Type: application/json" \
  -d '{
    "sellerId": 1,
    "amount": 5000.00,
    "paymentType": "CASH"
  }'
```

Response:
```json
{
  "id": 1,
  "sellerId": 1,
  "sellerName": "Leon Kozeev",
  "amount": 5000.0,
  "paymentType": "CASH",
  "transactionDate": "27-05-2026T23:02:07"
}

```

### Аналитика

## best seller
```bash
curl -X GET "http://localhost:8080/api/analytics/best-seller?period=MONTH"
```

```bash
curl -X GET "http://localhost:8080/api/analytics/best-seller?period=MONTH&referenceDate=16-07-6767"
```

Response:
```json
{
  "id": 1,
  "name": "Leon Kozeev",
  "contactInfo": "leon.kozeev@gmail.com",
  "registrationDate": "27-05-2026T22:57:18"
}
```

## sellers below threshold

```bash
curl -X GET "http://localhost:8080/api/analytics/sellers-below-threshold?startDate=01-01-2026&endDate=31-12-2026&threshold=10000"
```

Response:
```json
[
  {
    "id": 1,
    "name": "Leon Kozeev",
    "contactInfo": "leon.kozeev@gmail.com",
    "registrationDate": "27-05-2026T22:57:18"
  }
]
```

## best period

```bash
curl -X GET http://localhost:8080/api/analytics/best-period/1
```

Response:
```json
{
  "sellerId": 1,
  "sellerName": "Leon Kozeev",
  "startDate": "27-05-2026",
  "endDate": "27-05-2026",
  "transactionCount": 3,
  "periodType": "DAY"
}
```



## Тестирование

```bash
# все тесты
./gradlew test

# конкретный класс
./gradlew test --tests ru.nsu.shift.crm.service.SellerServiceTest

# отчёт покрытия
./gradlew jacocoTestReport
```

Отчёты:
- `build/test-results/test/` — результаты тестов
- `build/reports/jacoco/test/html/` — покрытие кода
