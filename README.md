# Referee-Coach: ProBasket

[![Java CI with Maven](https://github.com/sjucker/referee-coach-probasket/actions/workflows/maven.yml/badge.svg?branch=main)](https://github.com/sjucker/referee-coach-probasket/actions/workflows/maven.yml)
[![CI / CD for Angular](https://github.com/sjucker/referee-coach-probasket/actions/workflows/angular.yml/badge.svg?branch=main)](https://github.com/sjucker/referee-coach-probasket/actions/workflows/angular.yml)

## Development

* Start DB in Docker container:  
  `docker compose -p referee-coach-probasket -f src/main/docker/postgres.yml down && docker compose -p referee-coach-probasket -f src/main/docker/postgres.yml up --build`

* Generate the jOOQ-code by running the following command (make sure Docker is running):  
  `mvn clean test-compile -Djooq-codegen-skip=false`
  Or use the run configuration `generate jOOQ code`.

## Video snippet uploads (object storage)

Referee-coaches can upload short video clips (recorded on their smartphone) as video-snippet comments. The
files are stored in an **S3-compatible object store** (not in Postgres); the browser uploads and plays them
back directly via short-lived presigned URLs, so the bytes never pass through the dyno. See
[etc/video-upload-feature.md](etc/video-upload-feature.md) for the full design.

Configured via `probasket.storage.*` (see `application.properties`).

### Production (Heroku Bucketeer)

* `heroku addons:create bucketeer:hobbyist --app referee-coach-probasket`
  (provisions an AWS S3 bucket and sets `BUCKETEER_BUCKET_NAME`, `BUCKETEER_AWS_REGION`,
  `BUCKETEER_AWS_ACCESS_KEY_ID`, `BUCKETEER_AWS_SECRET_ACCESS_KEY`).
* Set a **bucket CORS policy** allowing `PUT` and `GET` from the app origin (`probasket.base-url`) — otherwise
  the browser's direct upload is blocked. Example rule: allowed origins = the app URL, allowed methods =
  `PUT,GET`, allowed headers = `*`.
* Alternatively use Cloudflare R2 / Backblaze B2: set `STORAGE_ENDPOINT` to the endpoint plus the
  `BUCKETEER_*` (or equivalent) credentials/bucket vars.

### Local development (MinIO)

* Start MinIO (S3-compatible, creates the `probasket-videos` bucket):
  `docker compose -p referee-coach-probasket -f src/main/docker/minio.yml up`
  (console at http://localhost:9001, user/pass `probasket`/`probasket`).
* Add to `src/main/resources/application-local.properties`:
  ```properties
  probasket.storage.bucket     = probasket-videos
  probasket.storage.region     = us-east-1
  probasket.storage.access-key = probasket
  probasket.storage.secret-key = probasket
  probasket.storage.endpoint   = http://localhost:9000
  ```

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
    * `ng update @angular/core@22 @angular/cli@22 --allow-dirty`
    * `ng update @angular/material@22 --allow-dirty`
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
  `heroku pg:backups:schedule DATABASE_URL --at '04:00 CET' --app referee-coach-probasket`
  `heroku pg:backups --app referee-coach-probasket`
  `heroku pg:backups:capture --app referee-coach-probasket`  
  `heroku pg:backups:download --app referee-coach-probasket`

* Restore locally:
    * Drop all tables
    * `pg_restore --no-owner -h localhost -U probasket -d probasket -W latest.dump`

## Infrastructure

* Heroku: https://dashboard.heroku.com/apps/referee-coach-probasket
