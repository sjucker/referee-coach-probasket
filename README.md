# Referee-Coach: Probasket

## Development

* Start DB in Docker container:  
  `docker compose -p referee-coach-probasket -f src/main/docker/postgres.yml down && docker compose -p referee-coach-probasket -f src/main/docker/postgres.yml up --build`

* Generate the jOOQ-code by running the following command (make sure Docker is running):  
  `mvn clean test-compile -Djooq-codegen-skip=false`
  Or use the run configuration `generate jOOQ code`.
