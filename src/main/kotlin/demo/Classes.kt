package demo

/** Короткий id объекта, чтобы глазами видеть: тот же это экземпляр или новый. */
fun Any.id(): String = "${this::class.simpleName}@" + Integer.toHexString(System.identityHashCode(this))

/** Печатает момент создания — так видно, когда Koin вызвал лямбду из модуля. */
private fun created(obj: Any) = println("    (создан ${obj.id()})")

// --- Классы приложения «Заметки». Сами они ничего не знают про Koin. ---

class Database {
    init { created(this) }
}

class NoteRepository(val db: Database) {
    init { created(this) }
}

interface NoteFormatter {
    fun format(text: String): String
}

class PlainFormatter : NoteFormatter {
    init { created(this) }
    override fun format(text: String) = text
}

class UpperFormatter : NoteFormatter {
    init { created(this) }
    override fun format(text: String) = text.uppercase()
}

/** Редактор: каждый раз новый (factory), но внутри — общий репозиторий (single). */
class NoteEditor(val repository: NoteRepository, val formatter: NoteFormatter) {
    init { created(this) }
}

/** Заметка: заголовок передаётся снаружи через параметры get(). */
class Note(val title: String, val formatter: NoteFormatter) {
    init { created(this) }
    override fun toString() = formatter.format(title)
}

/** Сессия редактирования: одна на экран (scoped). */
class EditSession(val editor: NoteEditor) {
    init { created(this) }
}
