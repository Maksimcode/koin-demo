# koin-demo

**Автор:** Шевцов Максим

**Библиотека:** [Koin](https://github.com/InsertKoinIO/koin), версия **4.2.2**
(исходники этой версии: <https://github.com/InsertKoinIO/koin/tree/4.2.2>)

**Исследуемый механизм:** резолвинг зависимостей и фабрики экземпляров:
как `get<T>()` находит определение в реестре и решает, создать новый объект или вернуть готовый
(`single` / `factory` / `scoped`).

## Запуск

Нужен JDK 17+ (Gradle сам подберёт toolchain 17).

```bash
./gradlew run
```

С внутренними логами Koin (регистрация определений и каждый шаг резолвинга):

```bash
./gradlew run --args=debug
```

## Что показывает демо

Приложение «Заметки», граф зависимостей описан в `src/main/kotlin/demo/AppModule.kt`,
сценарии — в `src/main/kotlin/demo/Main.kt`. Каждый класс печатает момент своего создания.

| № | Сценарий | Что видно | Кто внутри Koin отвечает |
|---|---|---|---|
| 1 | `single` | два `get()` → один объект, лямбда вызвана один раз | `SingleInstanceFactory` |
| 2 | `factory` | два `get()` → два объекта, но общий single внутри | `FactoryInstanceFactory` |
| 3 | `qualifier` | два `NoteFormatter` различаются по `named("upper")` | ключ реестра `класс:квалификатор:скоуп` |
| 4 | `parameters` | заголовок заметки передан через `parametersOf` | `CoreResolverV2`, шаг «injected parameters» |
| 5 | `scoped` | один объект на скоуп, после `close()` — исключение | `ScopedInstanceFactory`, `Scope` |
| 6 | нет определения | `NoDefinitionFoundException` | `CoreResolverV2.throwNoDefinitionFound` |

## Путь `get()` и точки останова

IntelliJ IDEA сама скачивает исходники `koin-core-jvm-4.2.2` (Ctrl/Cmd+клик по `get`).
Номера строк — для тега 4.2.2.

| Шаг | Файл:строка | Что происходит |
|---|---|---|
| регистрация | `InstanceRegistry.kt:100` | `_instances[mapping] = factory` — определение кладётся в HashMap |
| вход | `Scope.kt:278` | собирается `ResolutionContext` (класс, квалификатор, скоуп, параметры) |
| проверка скоупа | `Scope.kt:283` | закрытый скоуп → `ClosedScopeException` |
| поиск | `CoreResolverV2.kt:48` | цепочка: параметры → реестр → связанные скоупы → расширения |
| реестр | `CoreResolverV2.kt:64` → `InstanceRegistry.kt:114` | `_instances[indexKey]` — поиск фабрики по ключу |
| связанные скоупы | `CoreResolverV2.kt:83` | из скоупа `screen` ищем в корневом (так `EditSession` получает `NoteEditor`) |
| single | `SingleInstanceFactory.kt:56` | создать, только если ещё не создан (двойная проверка под `synchronized`) |
| factory | `FactoryInstanceFactory.kt:38` | всегда `create()` |
| scoped | `ScopedInstanceFactory.kt:78` | взять объект из `values[scopeId]` или создать |
| создание | `InstanceFactory.kt:49` | вызов лямбды из модуля, например `{ NoteRepository(get()) }` |

## Что можно поменять, чтобы проверить понимание

- `single { NoteRepository(get()) }` → `factory { ... }`: сценарий 2 покажет разные репозитории.
- `scoped { EditSession(get()) }` → `factory { ... }` внутри `scope`: в одном скоупе станут разные объекты.
- Убрать `named("upper")` у второго форматтера: у обоих станет один ключ, и внутри одного модуля
  второе определение молча заменит первое (`Module.saveMapping`: `mappings[key] = factory`).
  `get<NoteFormatter>()` вернёт `UpperFormatter`, а сценарий 3 упадёт на `get(named("upper"))`
  с `NoDefinitionFoundException`.
- Тот же дубль, но во втором модуле (`modules(appModule, extraModule)`): перезапись идёт в
  `InstanceRegistry.saveMapping` — с `--args=debug` видно `[WARNING] override index`. Если в `startKoin`
  добавить `allowOverride(false)`, старт упадёт с `DefinitionOverrideException`.
- `single(createdAtStart = true) { Database() }`: `Database` создастся при `startKoin`, а не при первом `get()`.

## Использование ИИ

См. [AI_USAGE.md](AI_USAGE.md).
