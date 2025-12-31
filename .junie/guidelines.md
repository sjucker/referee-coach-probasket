### Guidelines for AI Code Agents

These guidelines are designed for AI agents working on the **Referee Coach ProBasket** project, which consists of a **Spring Boot** backend and an **Angular** frontend.

---

### 1. General Architecture

- **Monorepo-like structure**: The project contains both backend (Java/Spring Boot) and frontend (Angular) in a single repository.
    - Backend: `src/main/java`
    - Frontend: `src/main/webapp`
- **Database**: PostgreSQL (managed via Flyway migrations in `src/main/resources/db/migration`).
- **Data Access**: jOOQ is used for type-safe database interactions.
- **API**: RESTful endpoints provided by Spring Boot, consumed by Angular.

---

### 2. Backend (Spring Boot & jOOQ)

#### Coding Standards

- **Java Version**: Java 21+ (utilize records, pattern matching, and sealed classes where appropriate).
- **Lombok**: Use `@RequiredArgsConstructor`, `@Slf4j`, and `@Data` sparingly.
- **REST Endpoints**:
    - Located in `ch.refereecoach.probasket.rest`.
    - Use `@RestController` and `@RequestMapping("/api/...")`.
    - Secure endpoints using `@Secured({"ROLE_NAME"})`.
    - Inject the current user using `@AuthenticationPrincipal Jwt jwt`.
- **Service Layer**:
    - Located in `ch.refereecoach.probasket.service`.
    - Business logic should reside here, not in controllers.
- **Data Transfer Objects (DTOs)**:
    - Use Java **records** for all DTOs (located in `ch.refereecoach.probasket.dto`).
    - Use `jakarta.validation.constraints` (e.g., `@NotNull`) for input validation.

### Generated Code

If you cannot execute the following commands yourself, ask a developer for help.

- the jooQ code generator is used to generate all Java records from the database schema, use `mvn clean test-compile -Djooq-codegen-skip=false`
- Use `mvn typescript-generator:generate -f pom.xml` to generate TypeScript interfaces/types from Java records (resulting in `rest.ts` file used by frontend)

#### jOOQ Best Practices

- Use `DSLContext` for all database queries.
- Prefer `multiset` and `mapping` for complex nested queries (see `ReportSearchService.java` for examples).
- Use generated `Tables` and `Records` from `ch.refereecoach.probasket.jooq`.
- Avoid raw SQL; stick to the jOOQ DSL.

---

### 3. Frontend (Angular 20+)

#### Core Paradigms

- **Standalone Components**: Do NOT use `NgModules`. All components, directives, and pipes must be standalone.
- **Signals**: Use signals for all reactive state management (`signal`, `computed`, `effect`, `input()`, `output()`).
- **Change Detection**: Always use `ChangeDetectionStrategy.OnPush`.
- **Control Flow**: Use native `@if`, `@for`, and `@switch` in templates.

#### Component Structure

- **Organization**: Keep logic in `.ts`, styles in `.scss`, and templates in `.html`.
- **Injection**: Use the `inject()` function instead of constructor injection.
- **Host Bindings**: Do NOT use `@HostBinding` or `@HostListener`. Use the `host` property in the `@Component` decorator.
- **Styles**: Use CSS class bindings instead of `ngClass`. Avoid `ngStyle`.

#### Angular Material & UI

- **Components**: Extensively use Angular Material (cards, buttons, form fields, signals, etc.).
- **Forms**: Prefer **Reactive Forms** (`FormGroup`, `FormControl`).
- **Icons**: Use `<mat-icon>`.

---

### 4. Workflow & Tooling

- **Database Migrations**: Every schema change requires a new `.sql` file in `src/main/resources/db/migration`.
- **Frontend/Backend Sync**: When changing the API, update both the Java DTOs and the corresponding TypeScript interfaces/types.
- **Proxy**: The frontend uses a `proxy-config.json` to route `/api` calls to the Spring Boot backend (typically on port 8080).

---

### 5. Example: Modern Angular Component (Signal-based)

```typescript

@Component({
    selector: 'app-example',
    templateUrl: './example.html',
    styleUrl: './example.scss',
    standalone: true, // Though implied in this project
    imports: [CommonModule, MatButtonModule],
    changeDetection: ChangeDetectionStrategy.OnPush,
})
export class ExampleComponent {
    private readonly dataService = inject(DataService);

    // State
    readonly count = signal(0);
    readonly doubleCount = computed(() => this.count() * 2);

    // Inputs/Outputs
    readonly label = input<string>('Default Label');
    readonly clicked = output<number>();

    increment() {
        this.count.update(c => c + 1);
        this.clicked.emit(this.count());
    }
}
```

### 6. Example: jOOQ Multiset Query

```java
class JooqExample {
    public Optional<ReportDTO> getReport() {
        return jooqDsl.select(REPORT,
                              multiset(
                                      select(REPORT_COMMENT.ID, REPORT_COMMENT.COMMENT)
                                              .from(REPORT_COMMENT)
                                              .where(REPORT_COMMENT.REPORT_ID.eq(REPORT.ID))
                                      ).convertFrom(it -> it.map(mapping(ReportCommentDTO::of)))
                             )
                      .from(REPORT)
                      .where(REPORT.EXTERNAL_ID.eq(externalId))
                      .fetchOptional();
    }
}
```
