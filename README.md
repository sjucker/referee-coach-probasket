# Referee-Coach: Probasket

## Development

* Start DB in Docker container:  
  `docker compose -p referee-coach-probasket -f src/main/docker/postgres.yml down && docker compose -p referee-coach-probasket -f src/main/docker/postgres.yml up --build`

* Generate the jOOQ-code by running the following command (make sure Docker is running):  
  `mvn clean test-compile -Djooq-codegen-skip=false`
  Or use the run configuration `generate jOOQ code`.

## Releases

* `npm run release`
* Answer the prompts:
    * next version: normally "patch"
    * commit: yes
    * tag: yes
    * push: yes
* To install release in production, merge the corresponding tag into main branch:
    * `git merge <TAG>`, e.g. `git merge 1.2.10`

## Updates

* Update Maven Parent
    * `mvn -U versions:display-parent-updates`
    * `mvn -U versions:update-parent`
* Update Versions in Properties
    * `mvn -U versions:display-property-updates`
    * `mvn -U versions:update-properties`

* Update Angular
    * `ng update @angular/core@20 @angular/cli@20 --allow-dirty`
    * `ng update @angular/material@20 --allow-dirty`
    * `ncu`
    * `ncu -u`, or
    * `ncu -i` for interactive update
    * `npm install`
    * `npm run build`
    * `npm run lint`
    * `npm run lint-fix` (if there are linting issues)

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
