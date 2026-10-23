# Retail Inventory Backend

Spring Boot REST API for managing retail stores, products, per-store inventory,
customer orders, and MongoDB product reviews.

## Run locally

Start MySQL with an `inventory` database and MongoDB with a `reviews` database,
then run the backend from `back-end`:

```powershell
.\mvnw.cmd spring-boot:run
```

The backend defaults to local MySQL (`localhost:3306`) and MongoDB
(`localhost:27017`). Override connection settings with environment variables:

| Variable | Default |
| --- | --- |
| `MYSQL_URL` | `jdbc:mysql://localhost:3306/inventory?useSSL=false&allowPublicKeyRetrieval=true&serverTimezone=UTC` |
| `MYSQL_USERNAME` | `root` |
| `MYSQL_PASSWORD` | empty |
| `MONGODB_URI` | `mongodb://localhost:27017/reviews` |
| `JPA_DDL_AUTO` | `update` |
| `RETAIL_API_URL` | `http://localhost:8080` (frontend API base URL) |

The API runs on port `8080`. Configure the UI's API base with
`window.RETAIL_API_URL` before loading `front-end/script.js` when using a
non-default backend address. The frontend allows cross-origin requests to the API.

## Sample data and reports

- `insert_data.sql` loads the provided MySQL sample data.
- `reviews.json` contains the MongoDB review sample data.
- `stored_procedures.sql` creates `GetMonthlySalesForEachStore(year, month)` and
  `GetAggregateSalesForCompany(year, month)` in the `inventory` database.

Set the MySQL and MongoDB environment variables for the lab services before
starting the backend; do not store database passwords in source files.
