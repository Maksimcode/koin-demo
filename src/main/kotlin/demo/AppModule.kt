package demo

import org.koin.core.qualifier.named
import org.koin.dsl.module

/** Квалификатор скоупа «экран». */
val ScreenScope = named("screen")

/**
 * Описание графа зависимостей.
 * Каждая строка превращается в BeanDefinition + InstanceFactory,
 * которые при startKoin кладутся в InstanceRegistry (HashMap по ключу "класс:квалификатор:скоуп").
 */
val appModule = module {
    // SingleInstanceFactory — один экземпляр на всё приложение
    single { Database() }
    single { NoteRepository(get()) }

    // Два определения одного типа различаются квалификатором
    single<NoteFormatter> { PlainFormatter() }
    single<NoteFormatter>(named("upper")) { UpperFormatter() }

    // FactoryInstanceFactory — новый экземпляр на каждый get()
    factory { NoteEditor(get(), get()) }

    // Параметр title приходит из get(parameters = { parametersOf("...") })
    factory { (title: String) -> Note(title, get()) }

    // ScopedInstanceFactory — один экземпляр на каждый скоуп "screen"
    scope(ScreenScope) {
        scoped { EditSession(get()) }
    }
}
