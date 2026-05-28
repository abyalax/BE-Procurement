ifeq ($(OS),Windows_NT)
MVNW := mvnw.cmd
else
MVNW := ./mvnw
endif

.PHONY: dev db-up db-down test build deps format migrate seed all

dev: 
	$(MVNW) spring-boot:run

migrate:
	$(MVNW) spring-boot:run -Dspring-boot.run.main-class=com.procurement.tools.DatabaseMaintenanceApplication -Dspring-boot.run.arguments="--maintenance.command=migrate"

seed:
	$(MVNW) spring-boot:run -Dspring-boot.run.main-class=com.procurement.tools.DatabaseMaintenanceApplication -Dspring-boot.run.arguments="--maintenance.command=seed"

all:
	$(MVNW) spring-boot:run -Dspring-boot.run.main-class=com.procurement.tools.DatabaseMaintenanceApplication -Dspring-boot.run.arguments="--maintenance.command=all"

db-up:
	docker compose up -d

db-down:
	docker compose down

test:
	$(MVNW) test

build:
	$(MVNW) clean package

deps:
	$(MVNW) dependency:resolve

format:
	npm run format
