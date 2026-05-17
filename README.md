# Task Management Platform

[![Spring Boot](https://img.shields.io/badge/Spring%20Boot-3.2.5-brightgreen)](https://spring.io/projects/spring-boot)
[![Java Version](https://img.shields.io/badge/Java-17-orange)](https://www.oracle.com/java/technologies/downloads/)
[![PostgreSQL](https://img.shields.io/badge/PostgreSQL-15-blue)](https://www.postgresql.org/)
[![Docker](https://img.shields.io/badge/Docker-Enabled-blue)](https://www.docker.com/)

O aplicatie backend pentru managementul proiectelor si task-urilor intr-un mediu colaborativ.
Este concentrata pe optimizarea resurselor, securitate prin token-uri si validari stricte ale datelor.
Aplicatia ajuta in organizarea echipelor si a gestionii progresului proiectelor.

---

## Cuprins
1. [Managementul Proiectului prin Jira](#managementul-proiectului-prin-jira)
2. [Decizii Arhitecturale si de Design (Rationale)](#decizii-arhitecturale-si-de-design-rationale)
3. [Blocaje Tehnice si Solutii (Technical Roadblocks)](#blocaje-tehnice-si-solutii-technical-roadblocks)
4. [Matricea Functionalitatilor](#matricea-functionalitatilor)
5. [Stack Tehnologic](#stack-tehnologic)
6. [Ghid de Pornire Rapida (DevOps Integration)](#ghid-de-pornire-rapida-devops-integration)
7. [Kit de Testare API (Postman)](#kit-de-testare-api-postman)

---

## Managementul Proiectului prin Jira

Pentru planificarea, urmarirea si lansarea versiunilor software s-a utilizat **Jira**.
Task-urile au fost structurate in tichete specifice, documentand cerintele tehnice si criteriile de acceptare identificate in faza de analiza.

Mai jos se regaseste o captura de ecran a board-ului Kanban utilizat in dezvoltare, ilustrand fluxul tichetelor si trasabilitatea acestora:

![Jira Kanban Board](img_1.png)

---

## Decizii Arhitecturale si de Design

In dezvoltarea acestei platforme, s-a pus accent pe adoptarea standardelor de industrie enterprise.

### 1. Modelare `Set<User>` in Many-to-Many
* **Abordare:** Utilizarea colectiilor JPA `Set` in locul `List` pentru relatia Many-to-Many dintre Proiecte si Membri.
* **Ratiune:** Hibernate orienteaza colectiile `List` in relatiile Many-to-Many intr-un mod ineficient, prin stergerea si re-insertia completa a randurilor din tabela de legatura la orice modificare a listei.
Alegerea `Set` asigura unicitatea membrilor intr-un si optimizeaza operatiunile de modificare pe tabela de legatura.

### 2. Strategia de Soft Delete
* **Abordare:** Implementarea (Soft Delete prin flag-ul `deleted = true`.
* **Ratiune:** In sistemele de management, hard-delete-ul este o operatiune distructiva care elimina complet tabela. S-a implementat o solutie bazata pe flag, unde obiectele sunt marcate ca sterse, dar raman fizic pe disc. 
Prin integrarea filtrelor, proiectele sterse sunt omise din fluxul operational, dar raman pe memoria RAM.

---

## Blocaje Tehnice si Solutii

Dezvoltarea a implicat rezolvarea unor probleme critice specifice Spring Boot si Hibernate:

### Capcana Lombok `@Data` in Entitatile JPA
* **Problema:** S-a identificat o exceptie `duplicate key` aruncata de PostgreSQL la adaugarea membrilor intr-un proiect, chiar daca utilizatorul nu se afla anterior acolo.
* **Cauza:** `@Data` de la Lombok genereaza automat metodele `equals()` si `hashCode()` scanand toate campurile, inclusiv colectiile Lazy. 
Cand starea unei colectii se modifica in memorie, codul hash se schimba, destabilizand mecanismul de *checking* al Hibernate.
* **Solutia:** S-a eliminat `@Data` de pe entitatile strategice, trecandu-se la adnotari `@Getter`, `@Setter` si configurand explicit `@EqualsAndHashCode(onlyExplicitlyIncluded = true)` raportat strict la ID-ul unic al entitatii.

### Recursivitatea Infinita in Serializarea JSON
* **Problema:** Atunci cand se interogau relatiile bidirectionale dintre entitati, serverul intra intr-un infinite loop de mapari ciclice.
* **Solutia:** Integrarea adnotarilor specializate precum `@JsonIgnoreProperties` si `@JsonIgnore` direct pe campurile relationale de tip proxy/lazy load.

---

## Matricea Functionalitatilor

Aplicatia implementeaza complet toate cerintele functionale, organizate pe module clare:

### Functionalitati Standard
* **Autentificare si RBAC Security:** Inregistrare si autentificare securizata prin JWT, cu protectie granulara pe baza de roluri (`ROLE_ADMIN`, `ROLE_USER`).
* **Management Utilizatori:** Permite vizualizarea si actualizarea informatiilor de profil pentru utilizatorii autentificati, dar si actiuni administrative de listare globala, promovare la gradul de Admin si dezactivare conturi.
* **Management Proiecte:** Flux operational complet de creare, adaugare membri, editare si stergere logica. Utilizatorii pot accesa exclusiv datele proiectelor in care sunt owner sau membru.
* **Management Task-uri:** Permite actiuni de tip CRUD, setare deadline, actualizare prioritati si schimbare controlata de status. Include suport pentru filtrare dupa status si prioritate.
* **Sistem de Validare si Global Exception Handler:** Toate request-urile primite in sistem sunt verificate prin adnotari de validare (`@Valid`, `@NotBlank`, `@Email`). 
Exceptiile de sistem si erorile de validare sunt interceptate de `GlobalExceptionHandler`, returnand raspunsuri HTTP clare si status codes corecte.
* **Audit si Logging:** Utilizarea Slf4j pentru deosebirea operatiunilor critice in consola aplicatiei (login succes, inregistrare useri, creari si stergeri de resurse).

### Functionalitati Extra
* **Checklist cu Progres Dinamic:** Un task poate stoca multiple subtask-uri. Sistemul intercepteaza automat adaugarea, stergerea sau comutarea starii unui subtask si recalculeaza instant procentul global de progres al task-ului parinte in DB.
* **Smart Workload Router:** Algoritm de asignare automata a task-urilor pe baza availability-ului membrilor. Noul task este distribuit automat programatorului cu cele mai putine sarcini active (`TODO`, `IN_PROGRESS`).
Avem o regula de *Tie-Breaker* ce favorizeaza Owner-ul de proiect in caz de egalitate perfecta, iar in caz in care acesta are mai multe task-uri este aplica ordinea adaugarii membrilor in proiect.
* **Monitorizare Proactiva:** Un serviciu automatizat `@Scheduled` ruleaza recurent in fundal pentru a analiza performanta.Avem task-uri de diferite prioritati(`LOW`,`MEDIUM`,`HIGH`,`CRITICAL`). 
Acesta marcheaza automat task-urile `CRITICAL` blocate in lucru de mai mult de 24 de ore cu flag-ul `needs_attention = true`, alertand echipa prin log-uri.

---

## Stack Tehnologic

* **Framework Principal:** Spring Boot 3.2.5
* **Limbaj de Programare:** Java 17
* **Baza de Date:** PostgreSQL 15
* **Sistem de Migrare:** Flyway DB
* **Securitate:** Spring Security + io.jsonwebtoken
* **Utilitare:** Lombok

---

## Guide de Pornire Rapida (DevOps Integration)

Aplicatia este containerizata complet. S-a utilizat un mecanism de **Multi-Stage Build** in Dockerfile pentru a compila codul si a rula aplicatia intr-un mediu izolat si securizat.

### Pornirea aplicatiei (Deployment local)
Navigheaza in folderul radacina al proiectului si ruleaza in terminal:
```bash
docker compose up --build -d