# information-systems-lab-1

## Connection Pool (Apache Commons DBCP2)
- `spring.datasource.type=org.apache.commons.dbcp2.BasicDataSource`
- Тюнинги (env overrides в скобках): initial-size (`DBCP2_INITIAL_SIZE`, 5), max-total (`DBCP2_MAX_TOTAL`, 20), max-idle (`DBCP2_MAX_IDLE`, 10), min-idle (`DBCP2_MIN_IDLE`, 5), max-wait-millis (`DBCP2_MAX_WAIT`, 10000), validation-query `SELECT 1`, test-on-borrow/while-idle включены, eviction каждые 30s.

## Hibernate L2 Cache (Infinispan)
- Region factory: `org.infinispan.hibernate.cache.v62.InfinispanRegionFactory`, конфиг `infinispan.xml`.
- Включены 2-й уровень и query cache; `hibernate.generate_statistics=true`.
- AOP-логирование статистики переключается `app.cache.stats-logging-enabled` (порог логирования `app.cache.stats-log-threshold`).
- Кэши в `infinispan.xml`: entity (max-count 1000, 1h), query (500, 30m), timestamps (2m).

## MinIO (S3-совместимое хранилище)
- Вкл/выкл: `minio.enabled` (test-профиль = false).
- Настройки: endpoint (`MINIO_ENDPOINT`, по умолчанию http://localhost:9000), access/secret, bucket (`imports`), region, secure=false.
- Загруженные файлы импорта пишутся в `imports/{operationId}/{uuid}-<filename>` и доступны для скачивания из лога импорта; загрузка участвует в транзакции (rollback удаляет объект).

## docker-compose
- Postgres 15 (порт 5434) и MinIO (9000/9090), volume `pgdata` и `minio-data`.
- Запуск: `docker compose up -d`
- Консоль MinIO: http://localhost:9090 (minio/minio123 по умолчанию).

## Быстрый тест импорта (curl)
```powershell
curl -F "file=@valid.json" -H "X-User: user1" -H "X-Role: ADMIN" http://localhost:8082/api/groups/import
curl -H "X-User: user1" -H "X-Role: ADMIN" http://localhost:8082/api/imports
curl -OJ -H "X-User: user1" -H "X-Role: ADMIN" http://localhost:8082/api/imports/ID/file
```