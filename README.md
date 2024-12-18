# Projekt Voting App - Setup i Uruchomienie

## Setup

### 1. Uruchomienie Docker
Aby uruchomić bazę danych PostgreSQL w kontenerze Docker:

```bash
docker run --name my_postgres -e POSTGRES_USER=myuser -e POSTGRES_PASSWORD=mypassword -e POSTGRES_DB=mydb -p 5432:5432 -d postgres
```

### 2. Uruchomienie klasy startowej

Aby uruchomić aplikację, przejdź do folderu projektu i uruchom klasę `main.Main` (to jest główny punkt wejścia do aplikacji, najlepiej uruchomić z Intellij).
Domyślnie aplikacja czyści wszystie dostępne tabele podczas uruchamiania

`application.conf:`
```
db.drop.data = true
```

### 3. Uruchomienie demo

Po uruchomieniu klasy startowej, można uruchomić demo, które znajduje się w folderze testowym:

```bash
test/scala/demo/RunDemo
```

### 4. Przykładowe requesty do API

Przykładowe zapytania HTTP do API są zapisane w pliku `requests.http`. W skrócie:

Lista wszystkich dostępnych kandydatów:
```
GET: http://localhost:8080/api/candidate/all
```

Lista wszystkich dostępnych wyborców:
```
GET: http://localhost:8080/api/voter/all
```

Tworzenie kandydata:
```
POST http://localhost:8080/api/candidate
Content-Type: application/json

{
  "firstName": "John",
  "lastName": "Adams"
}
```

Tworzenie wyborcy:
```
POST http://localhost:8080/api/voter
Content-Type: application/json

{
  "firstName": "John",
  "lastName": "Adams"
}
```

Kandydat głosuje na wyborcę (kandydat i wyborca muszą być najpierw zarejestrowani):
```
POST http://localhost:8080/api/vote
Content-Type: application/json

{
  "candidateId": 1,
  "voterId": 12
}
```
## Uwagi

1. **Testy uruchamiają się na Dockerze** – Ze względu na brak czasu nie udało się zintegrować testów z TestContainers. Testy wymagają uruchomienia lokalnego kontenera PostgreSQL.

2. **API wymaga poprawek** – Aktualna wersja API nie obsługuje błędów w pełni (np. brak odpowiednich komunikatów błędów przy niepoprawnych danych).

3. **Brak wpisów logowania** – Choć logback jest zintegrowany, brak jest odpowiednich wpisów logów w aplikacji.

4. **Brak testów dla API** – Brak jest testów jednostkowych/integracyjnych dla API, co jest ważnym punktem do poprawy w przyszłości.

5. **Brak integracji z OpenAPI** – Nie zintegrowano OpenAPI dla kontraktu API, brak wsparcia dla Swaggera i dokumentacji API.

6. **Projekt przekracza 3-4 godziny pracy** – Całe zadanie wymaga więcej niż 3-4 godzin pracy, szczególnie w kwestii implementacji testów, logowania, i integracji z OpenAPI.

7. **Problemy z uruchomieniem z poziomu `sbt`**

8. **Klasa Main wymaga refaktoru - trzeba przenieść tworzenie zależności w taki sposób aby można było je mockować.**

9. **Deprecated użyte w Demo, sporo ostrzeżeń z 'Code Analysis'**

## Wymagania

- **Docker** - do uruchomienia kontenera PostgreSQL.
- **sbt** - do budowania i uruchamiania projektu.
- **JDK 11+** - projekt wymaga JDK 11 lub nowszego.

## Licencja

Wszystkie prawa zastrzeżone. Projekt dostępny na licencji MIT.
