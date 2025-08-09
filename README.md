# Referee-Coach: Probasket

## Development

* Start DB in Docker container:  
  `docker compose -p referee-coach-probasket -f src/main/docker/postgres.yml down && docker compose -p referee-coach-probasket -f src/main/docker/postgres.yml up --build`

* Generate the jOOQ-code by running the following command (make sure Docker is running):  
  `mvn clean test-compile -Djooq-codegen-skip=false`
  Or use the run configuration `generate jOOQ code`.

## Updates

* Update Maven Parent
    * `mvn -U versions:display-parent-updates`
    * `mvn -U versions:update-parent`
* Update Versions in Properties
    * `mvn -U versions:display-property-updates`
    * `mvn -U versions:update-properties`

## Heroku

### Database

* `heroku pg:info --app referee-coach-probasket`

### Database Backup

* Prod:  
  `heroku pg:backups:capture --app referee-coach-probasket`  
  `heroku pg:backups:download --app referee-coach-probasket`

* Restore locally:
    * Drop all tables
    * `pg_restore --no-owner -h localhost -U probasket -d probasket -W latest.dump`

## Infrastructure

* Heroku: https://dashboard.heroku.com/apps/referee-coach-probasket
