package kotlin.nosql

import java.util.ArrayList

abstract class Session () {
    abstract fun <T : TableSchema>T.create()

    abstract fun <T : TableSchema>T.drop()

    abstract fun <T : DocumentSchema<P, V>, P, V> T.insert(v: () -> V)

    abstract fun <T : AbstractSchema> insert(columns: Array<Pair<AbstractColumn<*, T, *>, *>>)

    abstract fun <T : AbstractSchema> delete(table: T, op: Op)

    abstract fun <T : TableSchema, C> Query1<T, C>.set(c: () -> C)

    abstract fun <T : TableSchema, C> AbstractColumn<C, T, *>.forEach(statement: (C) -> Unit)

    abstract fun <T : TableSchema, C> AbstractColumn<C, T, *>.iterator(): Iterator<C>

    abstract fun <T : TableSchema, C, M> AbstractColumn<C, T, *>.map(statement: (C) -> M): List<M>

    abstract fun <T : PKTableSchema<P>, P, C> AbstractColumn<C, T, *>.get(id: () -> P): C

    abstract fun <T : PKTableSchema<P>, P, A, B> Template2<T, A, B>.get(id: () -> P): Pair<A, B>

    abstract fun <T : TableSchema, A, B> Template2<T, A, B>.forEach(statement: (A, B) -> Unit)
    abstract fun <T : TableSchema, A, B> Template2<T, A, B>.iterator(): Iterator<Pair<A, B>>
    abstract fun <T : TableSchema, A, B, M> Template2<T, A, B>.map(statement: (A, B) -> M): List<M>

    abstract fun <T : TableSchema, A, B> Query2<T, A, B>.forEach(statement: (A, B) -> Unit)
    abstract fun <T : TableSchema, A, B> Query2<T, A, B>.iterator(): Iterator<Pair<A, B>>

    fun <T : TableSchema, A, B> Template2<T, A, B>.filter(op: T.() -> Op): Query2<T, A, B> {
        return Query2<T, A, B>(a, b).where(table.op())
    }

    fun <T : PKTableSchema<P>, P, A, B> Template2<T, A, B>.find(id: () -> P): Query2<T, A, B> {
        return Query2<T, A, B>(a, b).where(table.pk eq id())
    }

    fun <T : TableSchema, A> AbstractColumn<A, T, *>.filter(op: T.() -> Op): Query1<T, A> {
        return Query1<T, A>(this).where(table.op())
    }

    fun <T : PKTableSchema<P>, A, P> AbstractColumn<A, T, *>.find(id: () -> P): Query1<T, A> {
        return Query1<T, A>(this).where(table.pk eq (id() as P))
    }

    fun <T : TableSchema, A> Query1<T, List<A>>.range1(range: () -> IntRange): RangeQuery<T, A> {
        return RangeQuery<T, A>(this, range())
    }

    fun <T : TableSchema, A> Query1<T, List<A>>.get(range: () -> IntRange): List<A> {
        return RangeQuery<T, A>(this, range()).list()
    }

    fun <T : TableSchema, A> Query1<T, List<A>>.range(range: () -> IntRange): RangeQuery<T, A> {
        return RangeQuery<T, A>(this, range())
    }

    abstract fun <T : TableSchema, C> RangeQuery<T, C>.forEach(st: (c: C) -> Unit)

    fun <T : TableSchema, C> RangeQuery<T, C>.list(): List<C> {
        val results = ArrayList<C>()
        forEach {
            results.add(it)
        }
        return results
    }

    fun <A, B> values(a: A, b: B): Pair<A, B> {
        return Pair(a, b)
    }

    fun <A, B, C> values(a: A, b: B, c: C): Triple<A, B, C> {
        return Triple(a, b, c)
    }

    fun <A, B, C, D> values(a: A, b: B, c: C, d: D): Quadruple<A, B, C, D> {
        return Quadruple(a, b, c, d)
    }

    class object {
        val threadLocale = ThreadLocal<Session>()

        fun get(): Session {
            return threadLocale.get()!!
        }
    }

    fun <T: DocumentSchema<P, C>, C, P> T.get(id: () -> P): C {
        return this.filter({ pk eq id() }).next()
    }

    abstract fun <T: DocumentSchema<P, C>, P, C> T.filter(op: T.() -> Op): Iterator<C>


    abstract fun <T : TableSchema, C, CC : Collection<*>> Query1<T, CC>.add(c: () -> C)
    abstract fun <T : TableSchema> Query1<T, Int>.add(c: () -> Int): Int

    abstract fun <T : KeyValueSchema, C> T.get(c: T.() -> AbstractColumn<C, T, *>): C
    abstract fun <T : KeyValueSchema> T.next(c: T.() -> AbstractColumn<Int, T, *>): Int
    abstract fun <T : KeyValueSchema, C> T.set(c: () -> AbstractColumn<C, T, *>, v: C)
    abstract fun <T : TableSchema> AbstractColumn<Int, T, *>.add(c: () -> Int): Int
    abstract fun <T : TableSchema, A, B> Query2<T, A, B>.get(statement: (A, B) -> Unit)
}
