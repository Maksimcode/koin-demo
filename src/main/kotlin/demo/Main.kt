package demo

import org.koin.core.Koin
import org.koin.core.context.startKoin
import org.koin.core.error.ClosedScopeException
import org.koin.core.error.NoDefinitionFoundException
import org.koin.core.logger.Level
import org.koin.core.parameter.parametersOf
import org.koin.core.qualifier.named

/**
 * Запуск: ./gradlew run              — только сценарии
 *         ./gradlew run --args=debug — плюс внутренние логи Koin (регистрация и резолвинг)
 */
fun main(args: Array<String>) {
    val debug = "debug" in args

    val koin: Koin = startKoin {
        printLogger(if (debug) Level.DEBUG else Level.ERROR)
        modules(appModule)
    }.koin

    singleScenario(koin)
    factoryScenario(koin)
    qualifierScenario(koin)
    parametersScenario(koin)
    scopeScenario(koin)
    missingDefinitionScenario(koin)
}

/** 1. single: SingleInstanceFactory создаёт объект при первом get() и дальше отдаёт его же. */
fun singleScenario(koin: Koin) {
    step("1. single")
    val first = koin.get<NoteRepository>()
    val second = koin.get<NoteRepository>()
    println("  первый: ${first.id()}, второй: ${second.id()}")
    println("  один и тот же объект: ${first === second}")
}

/** 2. factory: FactoryInstanceFactory вызывает лямбду на каждый get(). */
fun factoryScenario(koin: Koin) {
    step("2. factory")
    val first = koin.get<NoteEditor>()
    val second = koin.get<NoteEditor>()
    println("  редакторы разные: ${first !== second}")
    println("  а репозиторий внутри общий (single): ${first.repository === second.repository}")
}

/** 3. qualifier: ключ в реестре — "класс:квалификатор:скоуп", поэтому два NoteFormatter не конфликтуют. */
fun qualifierScenario(koin: Koin) {
    step("3. qualifier")
    val plain = koin.get<NoteFormatter>()
    val upper = koin.get<NoteFormatter>(named("upper"))
    println("  без квалификатора: ${plain.id()} -> ${plain.format("hello")}")
    println("  named(\"upper\"):   ${upper.id()} -> ${upper.format("hello")}")
}

/** 4. parameters: CoreResolverV2 сначала ищет тип среди переданных параметров. */
fun parametersScenario(koin: Koin) {
    step("4. parameters")
    val note = koin.get<Note> { parametersOf("купить хлеб") }
    println("  заметка: $note")
}

/** 5. scoped: ScopedInstanceFactory хранит Map<ScopeID, T> — по объекту на скоуп. */
fun scopeScenario(koin: Koin) {
    step("5. scoped")
    val screenA = koin.createScope("screenA", ScreenScope)
    val screenB = koin.createScope("screenB", ScreenScope)

    val a1 = screenA.get<EditSession>()
    val a2 = screenA.get<EditSession>()
    val b1 = screenB.get<EditSession>()
    println("  в одном скоупе один объект: ${a1 === a2}")
    println("  в разных скоупах разные:    ${a1 !== b1}")

    screenA.close()
    try {
        screenA.get<EditSession>()
    } catch (e: ClosedScopeException) {
        println("  после close(): ${e::class.simpleName}: ${e.message}")
    }
    screenB.close()
}

/** 6. Нет определения: резолвер прошёл все шаги поиска и ничего не нашёл. */
fun missingDefinitionScenario(koin: Koin) {
    step("6. нет определения")
    try {
        koin.get<StringBuilder>()
    } catch (e: NoDefinitionFoundException) {
        println("  ${e::class.simpleName}:")
        println("  ${e.message}")
    }
}

private fun step(title: String) = println("\n=== $title ===")
