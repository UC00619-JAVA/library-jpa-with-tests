Exercise 3 — Testar o BookService com Mockito

    ./mvnw test -Dtest=BookServiceTest ->  8 testes do enunciado + 1 do Extra (ArgumentCaptor) = 9

Exercise 4 — Testar o BookRepository (@DataJpaTest)

    ./mvnw test -Dtest=BookRepositoryTest  ->  Tests run: 6, Failures: 0

Exercise 5 — Testes de integração da API

    ./mvnw verify -Pits    ->   surefire: todos os *Test; failsafe: BookApiIT (9 testes) + InfoApiIT (1, o Extra)